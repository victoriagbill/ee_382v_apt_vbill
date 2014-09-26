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

#A list of queues, where each queue holds the timestamps of when each stream is accessed
recent = []
index_of_new_stream = 0


class ImageStream(ndb.Model):
	# models image stream with stream_name, owner, subscriber?
	# need json dumb/property for all other info?
	# stream1.put() creates in datastore, stream1.get()?
	# can use @classmethod to create a default query? get all?
	stream_name = ndb.StringProperty()
	owner = ndb.StringProperty()
	subscribers = ndb.StringProperty(repeated=True)
	tags = ndb.StringProperty(repeated=True)
	info = ndb.JsonProperty()
	timestamps = ndb.StringProperty(repeated=True)


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
			user = users.get_current_user()
			print users.get_current_user()
			print type(user.nickname())
			
			upload_files = self.get_uploads('cover_url')  # 'file' is file upload field in the form
			stream_name = self.request.get('stream_name')
			blob_info = upload_files[0]
			print 'stream name = %s' % stream_name
			img_info = {}
			img_info[stream_name] = {}
			upload_time = datetime.now()
			img_info[stream_name]['cover'] = (images.get_serving_url(blob_info.key()), str(upload_time.date()))
			img_info[stream_name]['stream_urls'] = [(images.get_serving_url(blob_info.key()), str(upload_time.date()))]
			img_info[stream_name]['stream_len'] = 1
			img_info[stream_name]['subscribers'] = [self.request.get('subscribers')] #need regex to parse multiple subscribers??????
			img_info[stream_name]['tags'] = [self.request.get('tags')] # should this be a list or just string? need regex?
			img_info[stream_name]['views'] = 1
			img_info[stream_name]['timestamps'] = [ str(datetime.now()) ]
			invite_message = self.request.get('invite_message') 
			data_stream = ImageStream(stream_name=self.request.get('stream_name'), owner=user.nickname(), subscribers = [self.request.get('subscribers')], tags=[self.request.get('tags')], info = img_info)
			data_stream_key = data_stream.put()
			#print img_info
			#print images.get_serving_url(blob_info.key())
			#print data_stream_key
			time.sleep(0.1)
			self.redirect('/manage',permanent=True)

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

		# img_info[stream_name]['views'] += 1
		# the create page serves as the MainHandler for the create stream UploadHandler, ServeHandler
		upload_url = blobstore.create_upload_url('/upload')
		user = users.get_current_user()
		if user:
			url = users.create_logout_url('/')
			url_linktext = 'Logout'
			template_values = {
		    'url': url,
		    'url_linktext': url_linktext,
				'upload_url': upload_url,
			}

			single_stream = ImageStream.query(ImageStream.stream_name == stream_name).fetch()
			single_stream[0].info[stream_name]['views'] += 1
			single_stream[0].info[stream_name]['time_stamps'].append( str(datetime.now()) )
			single_stream[0].put()
			print single_stream
			
			img_info = {}
			for x in xrange(len(single_stream)):
				img_info[single_stream[x].stream_name] = single_stream[x].info

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

	
		search_results = ImageStream.query(ImageStream.stream_name == search_data).fetch()

		for tag in ImageStream.stream_name['tags']
			search_results.append( ImageStream.query(ImageStream.stream_name['tags'][tag] == search_data).fetch() )

		search_results.sort

		#return stream names/urls of top 5 streams
		top_five_results = unique_search_list[:5][0]

		if len(search_results) > 0:
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
	Load up all the stream info
	'''
	all_streams = ImageStream.query().fetch()
	img_info = {}
	for x in xrange(len(all_streams)):
		img_info[all_streams[x].stream_name] = all_streams[x].info


	'''
	Clear out times older than hour
	'''
	current_time = str(datetime.now())
	#are python queues indexed?
	for stream in img_info
		index = 0
		converted_time = time.strptime(img_info[stream]['time_stamps']][index], "%Y-%m-%d %H:%M:%S.%f" )
		while (current_time- converted_time) > timedelta(hours = 1):
			del img_info[stream]['time_stamps'].remove(converted_time)
			index += 1
			converted_time = time.strptime( stream.string_timestamps[index], "%Y-%m-%d %H:%M:%S.%f" )

	'''
	Get most viewed streams for past hour
	'''
	popStreams []
	for stream in img_info:
		popStreams[stream] = (len(img_info[stream]['time_stamps']), streamName)
	popStreams.sort()

	return


#returns the top 3 trends
def getTrends():
	if len(popStreams) < 3
		freshenTrends() 
	return popStreams[0] : popStreams[3]


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



