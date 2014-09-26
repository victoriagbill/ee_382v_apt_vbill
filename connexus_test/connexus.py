import os
import urllib
from datetime import datetime

from google.appengine.api import users
from google.appengine.api import mail, images
from google.appengine.ext import ndb
from google.appengine.ext import blobstore, deferred
from google.appengine.ext.webapp import blobstore_handlers

import jinja2
import webapp2
import re
import json


img_info = {} #for now this is empty at startup, when app is deployed need to figure out how to store img_info to datastore and retrieve for each user

def cleanup(blob_keys):
    blobstore.delete(blob_keys)


JINJA_ENVIRONMENT = jinja2.Environment(
	loader=jinja2.FileSystemLoader(os.path.dirname(__file__)),
  extensions=['jinja2.ext.autoescape'],
  autoescape=True)


'''
DEQUE INIT'S -SR
'''
from collections import deque
from datetime import datetime, timedelta

#A list of queues, where each queue holds the timestapmps of when each stream is accessed
recent = []
index_of_new_stream = 0




class MainPage(webapp2.RequestHandler):
	#main page = login, should check for login then dump to manage page?
  def get(self):
		user = users.get_current_user()
		if user:
			greeting = ('Welcome, %s!' % (user.nickname()))
			url = users.create_logout_url('/')
			url_linktext = 'Logout'
			template_values = {
 			'greeting': greeting,
			'url': url,
 			'url_linktext': url_linktext,
 			}
			template = JINJA_ENVIRONMENT.get_template('manage.html')
			self.response.write(template.render(template_values))
		else:
			greeting = 'Sign in or register:'
			url = users.create_login_url(self.request.uri)
			url_linktext = 'Login'
			template_values = {
 				'greeting': greeting,
				'url': url,
 				'url_linktext': url_linktext,
 			}
			template = JINJA_ENVIRONMENT.get_template('index.html')
 			self.response.write(template.render(template_values))


class Manage(webapp2.RequestHandler):
	def get(self):
		user = users.get_current_user()
		if user:
			greeting = ('Welcome, %s!' % (user.nickname()))
			url = users.create_logout_url('/')
			url_linktext = 'Logout'
			template_values = {
			'greeting': greeting,
      'url': url,
      'url_linktext': url_linktext,
  		}
			print 'img_info'
			print img_info
			if len(img_info) > 0:
				template_values['streams'] = img_info
			else:
				template_values['command'] = 'Nothing here yet, either create or subscribe to streams to manage'
		else:
			url = users.create_login_url(self.request.uri)
			url_linktext = 'Login'
			template_values = {
      'url': url,
      'url_linktext': url_linktext,
  		}

		template = JINJA_ENVIRONMENT.get_template('manage.html')
		self.response.write(template.render(template_values))


class Create(webapp2.RequestHandler):
	def get(self):
		user = users.get_current_user()
		if user:
			url = users.create_logout_url('/')
			url_linktext = 'Logout'
		else:
			url = users.create_login_url(self.request.uri)
			url_linktext = 'Login'

		# the create page serves as the MainHandler for the create stream UploadHandler, ServeHandler
		upload_url = blobstore.create_upload_url('/upload')

		template_values = {
      'url': url,
      'url_linktext': url_linktext,
			'upload_url': upload_url,
  	}
		template = JINJA_ENVIRONMENT.get_template('create_stream.html')
		self.response.write(template.render(template_values))


		'''
		Each time a stream is created, create a queue for that stream -SR
		'''
		recent[index_of_new_stream] = (streamName, deque() )
		index_of_new_stream += 1


class Delete(webapp2.RequestHandler):
	def get(self):
		user = users.get_current_user()
		if user:
			url = users.create_logout_url('/')
			url_linktext = 'Logout'
		else:
			url = users.create_login_url(self.request.uri)
			url_linktext = 'Login'

		template_values = {
      'url': url,
      'url_linktext': url_linktext,
			'upload_url': upload_url,
  		}
		template = JINJA_ENVIRONMENT.get_template('create_stream.html')
		self.response.write(template.render(template_values))

		#TODO remove stream from store?
		index_of_new_stream -= 1


#class CreateStreamHandler(webapp2.RequestHandler):
	#def post(self):
 
# Need to add tuple of date/time to image url, ndb.DateTimeProperty()? 
# Or, use python datetime.datetime module

class UploadHandler(blobstore_handlers.BlobstoreUploadHandler):
  def post(self):
		#print upload_type
		print 'entered upload'
		if self.request.get('stream_name'):
			print 'entered create'
			upload_files = self.get_uploads('cover_url')  # 'file' is file upload field in the form
			stream_name = self.request.get('stream_name')
			blob_info = upload_files[0]
			print 'stream name = %s' % stream_name
			img_info[stream_name] = {}
			upload_time = datetime.now()
			img_info[stream_name]['cover'] = (images.get_serving_url(blob_info.key()), str(upload_time.date()))
			img_info[stream_name]['stream_urls'] = [(images.get_serving_url(blob_info.key()), str(upload_time.date()))]
			img_info[stream_name]['stream_len'] = 1
			img_info[stream_name]['subscribers'] = [self.request.get('subscribers')] #need regex to parse multiple subscribers??????
			img_info[stream_name]['tags'] = [self.request.get('tags')] # should this be a list or just string? need regex?
			img_info[stream_name]['views'] = 1
			invite_message = self.request.get('invite_message')
			print img_info
			print images.get_serving_url(blob_info.key())
			self.redirect('/manage')
			#self.redirect('/serve/%s' % blob_info.key())
		elif self.request.get('file_name'):
			print 'entered add image'
			stream_name = self.request.get('this_stream')
			print stream_name			
			upload_files = self.get_uploads('new_image')  # 'file' is file upload field in the form
			blob_info = upload_files[0]
			upload_time = datetime.now()
			img_info[stream_name]['stream_urls'].append((images.get_serving_url(blob_info.key()), str(upload_time.date())))
			img_info[stream_name]['stream_len'] += 1
			img_info[stream_name]['comments'] = [self.request.get('comments')] # needs to be tuple to find associated image?
			print img_info
			print images.get_serving_url(blob_info.key())
			self.redirect('/viewsingle'+'/'+stream_name)

class ServeHandler(blobstore_handlers.BlobstoreDownloadHandler):
  def get(self, resource):
		print 'entered serve'
		resource = str(urllib.unquote(resource))
		blob_info = blobstore.BlobInfo.get(resource)
		print images.get_serving_url(blob_info.key())
		img_url = images.get_serving_url(blob_info.key())
		self.send_blob(blob_info)
		#self.redirect('/manage')

class View(webapp2.RequestHandler):
	#clicking on view tab should take you to view all streams page
	def get(self):
		user = users.get_current_user()
		if user:
			url = users.create_logout_url('/')
			url_linktext = 'Logout'
			template_values = {
		    'url': url,
		    'url_linktext': url_linktext,
			}
			if len(img_info) > 0:
				template_values['streams'] = img_info
			else:
				template_values['command'] = 'Nothing here yet, either create or subscribe to streams to view all'
		else:
			url = users.create_login_url(self.request.uri)
			url_linktext = 'Login'
			template_values = {
		    'url': url,
		    'url_linktext': url_linktext,
			}
		template = JINJA_ENVIRONMENT.get_template('view_astreams.html')
		self.response.write(template.render(template_values))

class ViewSingle(webapp2.RequestHandler):
	#clicking on view tab should take you to view all streams page
	def get(self, stream_name):
		print 'entered single stream handler' #debugging, clean up later
		print stream_name
		img_info[stream_name]['views'] += 1
		# the create page serves as the MainHandler for the create stream UploadHandler, ServeHandler
		upload_url = blobstore.create_upload_url('/upload')
		user = users.get_current_user()

		'''
		UPDATE VIEWS
		Each time a stream has been viewed
		'''
		print "time viewed" + timestamp
		timestamp =  datetime.now()
		currentTIme = datetime.now()
		streamIndex = recent.index(streamName) #don't need to give index from deque, right?
		#streamIndex is where the tuple of name/deque is and [1] is the deque itself
		recent[streamIndex][1].append(timestamp)


		if user:
			url = users.create_logout_url('/')
			url_linktext = 'Logout'
			template_values = {
		    'url': url,
		    'url_linktext': url_linktext,
				'upload_url': upload_url,
			}
			if len(img_info) > 0:
				template_values['streams'] = img_info
				template_values['this_stream'] = stream_name
		else:
			url = users.create_login_url(self.request.uri)
			url_linktext = 'Login'
			template_values = {
		    'url': url,
		    'url_linktext': url_linktext,
			}
		template = JINJA_ENVIRONMENT.get_template('view_sstream.html')
		self.response.write(template.render(template_values))

class Search(webapp2.RequestHandler):
	def get(self):
		user = users.get_current_user()
		if user:
			url = users.create_logout_url('/')
			url_linktext = 'Logout'
		else:
			url = users.create_login_url(self.request.uri)
			url_linktext = 'Login'
		template_values = {
      'url': url,
      'url_linktext': url_linktext,
  	}
		template = JINJA_ENVIRONMENT.get_template('search.html')
		self.response.write(template.render(template_values))

'''
START OF STEVE'S FUNCTIONS
'''

from difflib import SequenceMatcher

class SearchStreams(webapp2.RequestHandler):
	def post(self):
		search_data = self.request.get('cxus_search')
		print search_data

		search_list = []

		#TODO streamName/streamURL/stream tag names and structure
		for streamName in streamDict:
			search_list.append( SequenceMatcher(None, search_data, streamName).ratio()), (streamName, streamURL) )
	
		for tag in StreamDict:
			search_list.append( SequenceMatcher(None, search_data, tag).ratio()), (streamName, streamURL) )

		unique_search_list = set(search_list)

		unique_search_list.sort

		#return stream names/urls of top 5 streams
		top_five_results = unique_search_list[:5][0]

		if len(unique_search_list) > 0:
				template_values['streams'] = unique_search_list[:5][0]
			else:
				template_values['command'] = 'No matching streams found'

		template = JINJA_ENVIRONMENT.get_template('search_results.html')
		self.response.write(template.render(template_values))
			


class Trending(webapp2.RequestHandler):
	def get(self):
		user = users.get_current_user()
		if user:
			url = users.create_logout_url('/')
			url_linktext = 'Logout'
		else:
			url = users.create_login_url(self.request.uri)
			url_linktext = 'Login'
		template_values = {
      'url': url,
      'url_linktext': url_linktext,
  	}
		template = JINJA_ENVIRONMENT.get_template('trending.html')
		self.response.write(template.render(template_values))

		trending_streams = getTrends()
		if len(trending_streams) > 0:
			template_values['streams'] = trending_streams
		else:
			template_values['command'] = 'No trending streams found'

		#TODO DISPLAY THE TRENDING STREAMS

class TrendingShort(webapp2.RequestHandler):
	def post(self):
		freshenTrends()
		if buttonPosition == short_update:
			sendTrends() 

class TrendingHourly(webapp2.RequestHandler):
	def post(self):
		#CHECK LAST UPDATED TREND TIME BUTTON
		if buttonPosition == hourly_update:
			sendTrends() 


class TrendingDaily(webapp2.RequestHandler):
	def post(self):
		#CHECK LAST UPDATED TREND TIME BUTTON
		if buttonPosition == daily_update:
			sendTrends() 


#Called every 5 minutes to clean out old timestamps 
def freshenTrends():
	'''
	Clear times over an hour
	'''

	#are python queues indexed?
	for stream in streamDict
		index = 0
		while if (timestamp - recent[stream][1][index]) > timedelta(hours = 1):
			recent[stream[1].popleft()
			index += 1

	return


#returns the top 3 trends
def getTrends():
	popStreams []
	for i in recent:
		popStreams[i] = len(recent[0][1])

	popStreams.sort()

	topStreams[] = (popStreams[0] : popStreams[3])

	return topStreams


def sendTrends():
	trending_streams = getTrends()

	#TODO FIX SENDING EMAIL
	message = mail.EmailMessage(sender=user.email(), subject="Trending Update")
	message.to = user.email
	message.body = "CONNEXUS.US\n" +
		"Daily Trending Report\n" +
		"------------------------------------------\n" +
		"1. " + trending_streams[0] + "\n" +
		"2. " + trending_streams[1] + "\n" +
		"3. " + trending_streams[2] + "\n"
		#"Update Trending Prefrerences (Link)"
	message.send()
	self.redirect('/')


'''
EBD IF STEVE'S CHANGES
'''
class Social(webapp2.RequestHandler):
	def get(self):
		user = users.get_current_user()
		if user:
			url = users.create_logout_url('/')
			url_linktext = 'Logout'
		else:
			url = users.create_login_url(self.request.uri)
			url_linktext = 'Login'
		template_values = {
      'url': url,
      'url_linktext': url_linktext,
  	}
		template = JINJA_ENVIRONMENT.get_template('social.html')
		self.response.write(template.render(template_values))



class InviteFriendHandler(webapp2.RequestHandler):
	def post(self):
		user = users.get_current_user()
		if user is None:
			login_url = users.create_login_url(self.request.path)
			self.redirect(login_url)
			return
		to_addr = self.request.get("invite_email")
		if not mail.is_email_valid(to_addr):
  		# Return an error message...
			pass

		message = mail.EmailMessage(sender=user.email(), subject="test invite")
		message.to = to_addr
		message.body = "this is a test message"
		message.send()
		self.redirect('/')


class NotFoundPageHandler(webapp2.RequestHandler):
	def get(self):
		self.error(404)
		user = users.get_current_user()
		if user:
			url = users.create_logout_url('/')
			url_linktext = 'Logout'
		else:
			url = users.create_login_url(self.request.uri)
			url_linktext = 'Login'
		template_values = {
      'url': url,
      'url_linktext': url_linktext,
  	}
		template = JINJA_ENVIRONMENT.get_template('error.html')
		self.response.write(template.render(template_values))	


application = webapp2.WSGIApplication([
  ('/', MainPage),
  ('/invite', InviteFriendHandler),
	('/manage', Manage),
	('/create', Create),
	('/viewall', View),
	('/viewsingle/([^/]+)?', ViewSingle),
	('/search', Search),
	('/searchstreams', SearchStreams),
	('/trending', Trending),
	('/trendingShort', TrendingShort),
	('/TrendingHourly', TrendingHourly),
	('/trendingDaily', TrendingDaily),
	('/social', Social),
	('/upload', UploadHandler),
	('/serve/([^/]+)?', ServeHandler),
	('/.*', NotFoundPageHandler),
], debug=True)



