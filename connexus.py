import os
import urllib
from datetime import datetime, timedelta
import time
import collections

from google.appengine.api import users
from google.appengine.api import mail, images
from google.appengine.ext import ndb
from google.appengine.ext import blobstore, deferred
from google.appengine.ext.webapp import blobstore_handlers

import jinja2
import webapp2
import re
import json


# img_info = {} #for now this is empty at startup, when app is deployed need to figure out how to store img_info to datastore and retrieve for each user

def cleanup(blob_keys):
    blobstore.delete(blob_keys)


JINJA_ENVIRONMENT = jinja2.Environment(
	loader=jinja2.FileSystemLoader(os.path.dirname(__file__)),
  extensions=['jinja2.ext.autoescape'],
  autoescape=True)

popStreams = []
popStreams2 = []
trendingStreams = collections.OrderedDict()
buttonPosition=''
#topViews = []

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

			all_streams_user = ImageStream.query(ImageStream.owner == user.nickname()).fetch()
			img_info = {}
			for x in xrange(len(all_streams_user)):
				img_info[all_streams_user[x].stream_name] = all_streams_user[x].info
			if len(img_info) > 0:
				template_values['streams'] = img_info
			else:
				template_values['command'] = 'Nothing here yet, either create or subscribe to streams to manage'

			# add all_streams_subs for streams the user subscribes to
			all_streams_subs = ImageStream.query(ImageStream.subscribers > '').fetch()
			print all_streams_subs
			this_sub = {}
			for x in xrange(len(all_streams_subs)):
				if any(user.nickname() in y for y in all_streams_subs[x].subscribers):
					this_sub[all_streams_subs[x].stream_name] = all_streams_subs[x].info
			if len(this_sub) > 0:
				template_values['subs'] = this_sub
			
		else:
			url = users.create_login_url(self.request.uri)
			url_linktext = 'Login'
			template_values = {
      'url': url,
      'url_linktext': url_linktext,
  		}

		template = JINJA_ENVIRONMENT.get_template('manage.html')
		self.response.write(template.render(template_values))
		
	def post(self):
		if self.request.get_all('delete'):
			to_delete = self.request.get_all('delete')
			if len(to_delete) < 2:
				del_stream = ImageStream.query(ImageStream.stream_name == to_delete[0]).fetch()
				print del_stream
				del_key = del_stream[0].key
				print del_key
				del_key.delete()
				time.sleep(0.1)
				self.redirect('/manage')
			else:
				for x in xrange(len(to_delete)):
					del_stream = ImageStream.query(ImageStream.stream_name == to_delete[x]).fetch()
					del_key = del_stream[0].key
					del_key.delete()
				time.sleep(0.1)
				self.redirect('/manage')

		elif self.request.get_all('unsub'):
			user = users.get_current_user()
			to_unsub = self.request.get_all('unsub')
			if len(to_unsub) < 2:
				unsub_stream = ImageStream.query(ImageStream.stream_name == to_unsub[0]).fetch()
				print unsub_stream
				unsub_stream[0].subscribers.remove(user.email())
				print unsub_stream
				unsub_stream[0].put()
				time.sleep(0.1)
				self.redirect('/manage')
			else:
				for x in xrange(len(to_unsub)):
					unsub_stream = ImageStream.query(ImageStream.stream_name == to_unsub[x]).fetch()
					unsub_stream[0].subscribers.remove(user.nickname())
					unsub_stream[0].put()
				time.sleep(0.1)
				self.redirect('/manage')
		else:
			self.redirect('/manage')

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


# Need to add tuple of date/time to image url, ndb.DateTimeProperty()? 
# Or, use python datetime.datetime module

class UploadHandler(blobstore_handlers.BlobstoreUploadHandler):
  def post(self):
		#print upload_type
		print 'entered upload'
		if self.request.get('stream_name'):
			stream_name = self.request.get('stream_name')
			all_streams = ImageStream.query().fetch()
			for x in xrange(len(all_streams)):
				if all_streams[x].stream_name == stream_name:
					print 'duplicate found'
					self.redirect('/error/samename')
					return
			print 'entered create'
			user = users.get_current_user()
			img_info = {}
			img_info[stream_name] = {}
			upload_time = datetime.now()
			if self.get_uploads('cover_url'):
				upload_files = self.get_uploads('cover_url')  # 'file' is file upload field in the form
				blob_info = upload_files[0]
				img_info[stream_name]['cover'] = (images.get_serving_url(blob_info.key()), str(upload_time.date()))
				img_info[stream_name]['stream_urls'] = [(images.get_serving_url(blob_info.key()), str(upload_time.date()))]
				img_info[stream_name]['stream_len'] = 1
			else:
				img_info[stream_name]['cover'] = ()
				img_info[stream_name]['stream_urls'] = [(None, str(upload_time.date()))]
				img_info[stream_name]['stream_len'] = 0
			if len(self.request.get('subscribers')) > 0:
				img_info[stream_name]['subscribers'] = re.findall(r'[\w\.-]+@[\w\.-]+', self.request.get('subscribers'))
			img_info[stream_name]['tags'] = [self.request.get('tags')] # should this be a list or just string? need regex?
			img_info[stream_name]['views'] = 1
			img_info[stream_name]['recent'] = 0
			invite_message = self.request.get('invite_message') 
			data_stream = ImageStream(stream_name=self.request.get('stream_name'), owner=user.nickname(), subscribers = [self.request.get('subscribers')], tags=[self.request.get('tags')], info = img_info, timestamps=[str(datetime.now())])
			data_stream_key = data_stream.put()
			print img_info
			time.sleep(0.1)
			self.redirect('/manage')

		elif self.request.get('file_name'):
			print 'entered add image'
			stream_name = self.request.get('this_stream')
			print stream_name			
			upload_files = self.get_uploads('new_image')  # 'file' is file upload field in the form
			print upload_files
			blob_info = upload_files[0]
			upload_time = datetime.now()
			single_stream = ImageStream.query(ImageStream.stream_name == stream_name).fetch()
			single_stream[0].info[stream_name]['stream_urls'].append((images.get_serving_url(blob_info.key()), str(upload_time.date())))
			single_stream[0].info[stream_name]['stream_len'] += 1
			single_stream[0].info[stream_name]['comments'] = [self.request.get('comments')] # needs to be tuple to find associated image?
			single_stream[0].put()
			time.sleep(0.1)
			print single_stream
			if len(single_stream[0].subscribers) > 0:
				self.redirect('/emails/'+stream_name)
			else:
				time.sleep(0.1)
				self.redirect('/viewsingle'+'/'+stream_name)
		elif self.request.get('stream_name') == '':
			self.redirect('/error/noname')

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
			all_streams = ImageStream.query().fetch()
			img_info = {}
			for x in xrange(len(all_streams)):
				img_info[all_streams[x].stream_name] = all_streams[x].info
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
			single_stream[0].timestamps.append(str(datetime.now()))
			single_stream[0].put()
			print single_stream

			img_info = {}
			for x in xrange(len(single_stream)):
				img_info[single_stream[x].stream_name] = single_stream[x].info

			#if self.request.get('more_check') == None:
			if not self.request.get('more_check'):
				img_info[stream_name][stream_name]['stream_urls'] = img_info[stream_name][stream_name]['stream_urls'][0:3]
				template_values['more_check'] = 0
			else:
				template_values['more_check'] = 1
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

	def post(self, stream_name):
		# add to subscribers 
		user = users.get_current_user()
		if user:
			sub_stream = ImageStream.query(ImageStream.stream_name == stream_name).fetch()
			sub_stream[0].subscribers.append(unicode(user.email()))
			sub_stream[0].put()
			time.sleep(0.1)
			self.redirect('/viewsingle/'+stream_name)
		else:
			self.redirect('/')



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
		print 'in trending, trendingStreams'
		print trendingStreams
		while len(trendingStreams) > 0:
			trendingStreams.popitem(last=True)
		freshenTrends()
		if len(trendingStreams) > 0:
			template_values['streams'] = trendingStreams
		template = JINJA_ENVIRONMENT.get_template('trending.html')
		self.response.write(template.render(template_values))

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

class EmailUpdated(webapp2.RequestHandler):
	def get(self, stream_name):
		print 'entered emails'
		#stream_name = self.request.get('stream_name')
		subs_stream = ImageStream.query(ImageStream.stream_name == stream_name).fetch()
		for x in xrange(len(subs_stream[0].subscribers)):
			if len(subs_stream[0].subscribers[x]) < 2:
				continue
			to_addr = subs_stream[0].subscribers[x]
			if not mail.is_email_valid(to_addr):
				# Return an error message...
				pass
			message = mail.EmailMessage(sender='victoriagbill@gmail.com', subject="Connexus stream updated")
			message.to = to_addr
			message.body = "An image was added to a stream you subscribe to! Check it out at connexus-vbill-sruth.appspot.com"
			message.send()
		self.redirect('/viewsingle/'+stream_name)


class SearchStreams(webapp2.RequestHandler):
	def post(self):
		search_data = self.request.get('cxus_search')
		print search_data

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

  		#get search results from stream name
		search_results = ImageStream.query(ImageStream.stream_name == search_data).fetch()

		this_name = {}
		print search_results
		for x in xrange(len(search_results)):
				this_name[search_results[x].stream_name] = search_results[x].info

		# add all_streams_tags for streams with those tags
		all_streams_tags = ImageStream.query(ImageStream.tags > '').fetch()
		print all_streams_tags
		this_tag = {}
		for x in xrange(len(all_streams_tags)):
				if any(search_data in y for y in all_streams_tags[x].tags):
					this_tag[all_streams_tags[x].stream_name] = all_streams_tags[x].info

		#combine name reslts with tag results
		img_info = dict(this_name.items() + this_tag.items())

		#reduce to 5 results if more than that
		while (len(img_info)) > 5:
			img_info.popitem()

		if (len(img_info)) > 0:
			template_values['streams'] = img_info  #TODO CHANGE TO ONLY SHOW 5
		else:
			template_values['command'] = 'No matching streams found'

		template = JINJA_ENVIRONMENT.get_template('search_results.html')
		self.response.write(template.render(template_values))



#Called every 5 minutes to clean out old timestamps 
def freshenTrends():
	print "in freeshenTrends"
	'''
	Load up all the stream info
	'''

	current_time = datetime.now()
	print current_time
	all_streams = ImageStream.query().fetch()
	for x in xrange(len(all_streams)):
		if len(all_streams[x].timestamps) > 0:
			print "primed"
			converted_time = datetime.strptime( all_streams[x].timestamps[0], "%Y-%m-%d %H:%M:%S.%f" )
			print converted_time
			while (len(all_streams[x].timestamps) > 0) and ((current_time - converted_time) > timedelta(hours = 1)):
				print 'removed timestamps'
				print all_streams[x].stream_name
				all_streams[x].timestamps.remove(str(converted_time))
				if len(all_streams[x].timestamps) > 0:
					converted_time = datetime.strptime( all_streams[x].timestamps[0], "%Y-%m-%d %H:%M:%S.%f" )
		all_streams[x].info[all_streams[x].stream_name]['recent'] = len(all_streams[x].timestamps)
		all_streams[x].put()

	popStreams = []
	popStreams2 = []
	for x in xrange(len(all_streams)):
		popStreams.append((len(all_streams[x].timestamps), all_streams[x].stream_name, all_streams[x].info))
		print all_streams[x].stream_name
		print len(all_streams[x].timestamps)
	print popStreams[0]
	popStreams2 = sorted(popStreams, key=lambda x: x[0], reverse=True)
	print popStreams2[0][1]
	print popStreams2[1][1]
	print popStreams2[2][1]
	for x in xrange(3):
		trendingStreams[popStreams2[x][1]] = popStreams2[x][2]
	print trendingStreams
	return

#returns the top 3 trends
def getTrends():
	print "in getTrends"
	if len(popStreams2) < 3:
		freshenTrends() 
	return popStreams2[0:3]


class UpdateTrending(webapp2.RequestHandler):
	def post(self):
		global buttonPosition
		print 'pushed update trending button'
		buttonPosition = self.request.get("trending")
		print buttonPosition
		self.redirect('/trending')

class TrendingShort(webapp2.RequestHandler):
	def get(self):
		global buttonPosition
		print "Been 5 minutes"
		print buttonPosition
		freshenTrends()
		if buttonPosition == '5mins':
			print "sending 5 minute trend update"
			update_emails = ['victoriagbill@gmail.com','steve.a.rutherford@gmail.com','ragha@utexas.edu','natviv@cs.utexas.edu']
			for x in xrange(len(update_emails)):
				to_addr = update_emails[x]
				if not mail.is_email_valid(to_addr):
					# Return an error message...
					pass
				message = mail.EmailMessage(sender='victoriagbill@gmail.com', subject="Connexus trending update")
				message.to = to_addr
				message.body = "These three streams are now trending!\n" + '\n' + trendingStreams.keys()[0]+ '\n' + trendingStreams.keys()[1] + '\n' + trendingStreams.keys()[2]
				message.send()		
		return

class TrendingHourly(webapp2.RequestHandler):
	def get(self):
		global buttonPosition
		print "Been an hour"
		freshenTrends()
		#CHECK LAST UPDATED TREND TIME BUTTON
		if buttonPosition == 'hour':
			print "sending hourly"
			update_emails = ['victoriagbill@gmail.com','steve.a.rutherford@gmail.com','ragha@utexas.edu','natviv@cs.utexas.edu']
			for x in xrange(len(update_emails)):
				to_addr = update_emails[x]
				if not mail.is_email_valid(to_addr):
					# Return an error message...
					pass
				message = mail.EmailMessage(sender='victoriagbill@gmail.com', subject="Connexus trending update")
				message.to = to_addr
				message.body = "These three streams are now trending!\n" + '\n' + trendingStreams.keys()[0]+ '\n' + trendingStreams.keys()[1] + '\n' + trendingStreams.keys()[2]
				message.send()		
		return

class TrendingDaily(webapp2.RequestHandler):
	def get(self):
		global buttonPosition
		print "Been a day"
		#CHECK LAST UPDATED TREND TIME BUTTON
		if buttonPosition == 'day':
			print "sending daily"
			update_emails = ['victoriagbill@gmail.com','steve.a.rutherford@gmail.com','ragha@utexas.edu','natviv@cs.utexas.edu']
			for x in xrange(len(update_emails)):
				to_addr = update_emails[x]
				if not mail.is_email_valid(to_addr):
					# Return an error message...
					pass
				message = mail.EmailMessage(sender='victoriagbill@gmail.com', subject="Connexus trending update")
				message.to = to_addr
				message.body = "These three streams are now trending!\n" + '\n' + trendingStreams.keys()[0]+ '\n' + trendingStreams.keys()[1] + '\n' + trendingStreams.keys()[2]
				message.send()		
		return



class DeleteStream(webapp2.RequestHandler):
	def get(self):
		print 'entered delete'
		user = users.get_current_user()
		if user is None:
			login_url = users.create_login_url(self.request.path)
			self.redirect(login_url)
			return
		
		to_delete = self.request.get_all('delete')
		print to_delete
		#stream_del = ImageStream.query(ImageStream.stream_name == stream_name).fetch()
		#stream_del[0].delete()
		
		time.sleep(0.1)
		self.redirect('/manage')


class ErrorHandler(webapp2.RequestHandler):
	def get(self, message):
		#self.error(404)
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
		if message == 'samename':
			template_values['error_msg'] = 'Error: that stream name is already being used, you must pick a unique name.'
		elif message == 'noname':
			template_values['error_msg'] = 'Error: you must enter at least a stream name to create a new stream.'
		else:
			template_values['error_msg'] = 'Error: you attempted to reach a stream or page that does not exist.'
		
		template = JINJA_ENVIRONMENT.get_template('error.html')
		self.response.write(template.render(template_values))	


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
		template_values['error_msg'] = 'Error: you attempted to reach a stream or page that does not exist. '
		
		template = JINJA_ENVIRONMENT.get_template('error.html')
		self.response.write(template.render(template_values))	


application = webapp2.WSGIApplication([
  ('/', MainPage),
	('/manage', Manage),
	('/create', Create),
	('/viewall', View),
	('/viewsingle/([^/]+)?', ViewSingle),
	('/emails/([^/]+)?', EmailUpdated),
	('/search', Search),
	('/searchstreams', SearchStreams),
	('/trending', Trending),
	('/uptrend', UpdateTrending),
	('/trendingShort', TrendingShort),
	('/trendingHourly', TrendingHourly),
	('/trendingDaily', TrendingDaily),
	('/social', Social),
	('/upload', UploadHandler),
	('/delete', DeleteStream),
	('/serve/([^/]+)?', ServeHandler),
	('/error/([^/]+)?', ErrorHandler),
	('/.*', NotFoundPageHandler),
], debug=True)



