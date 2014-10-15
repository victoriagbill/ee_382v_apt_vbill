'''
When file uploaded returns a blob key
	blob key is reference to fles in blob
	Blob data created inidrectly by submitted web form or HTTP "POST" request
	Data can be served to user or file-like stream

To serve a blob, your application sets a header on the outgoing response,
 and App Engine replaces the response with the blob value.

user creates a blob by submitting an HTML form that includes one or more
 file input fields. Your application calls create_upload_url() to get 
 the destination (action) of this form, passing the function a URL
  path of a handler in your application.
'''


from google.appengine.ext import blobstore
upload_url = blobstore.create_upload_url('/upload')

#async
create_upload_url_async(). 
#It allows your application code to continue running while Blobstore generates the upload URL.

'''
must include a file upload field, and the form's enctype must be set 
to multipart/form-data. When the user submits the form, the POST is
 handled by the Blobstore API, which creates the blob. The API also 
 creates an info record for the blob and stores the record in the 
 datastore, 
'''

self.response.out.write('<html><body>')
self.response.out.write('<form action="%s" method="POST" enctype="multipart/form-data">' % upload_url)
self.response.out.write("""Upload File: <input type="file" name="file"><br> <input type="submit"
    name="submit" value="Submit"> </form></body></html>""")


'''
store the blob key with the rest of your application's data model. 
The blob key itself remains accessible from the blob info entity in
 the datastore
'''

 from google.appengine.ext.webapp import blobstore_handlers
class UploadHandler(blobstore_handlers.BlobstoreUploadHandler):
  def post(self):
    upload_files = self.get_uploads('file')  # 'file' is file upload field in the form
    blob_info = upload_files[0]
    self.redirect('/serve/%s' % blob_info.key())

'''
To serve blobs, you must include a blob download handler as a path 
in your application. 
'''


from google.appengine.ext import blobstore
from google.appengine.ext.webapp import blobstore_handlers
class ServeHandler(blobstore_handlers.BlobstoreDownloadHandler):
  def get(self, resource):
    resource = str(urllib.unquote(resource))
    blob_info = blobstore.BlobInfo.get(resource)
    self.send_blob(blob_info)



'''
Complete example
'''

import os
import urllib
import webapp2

from google.appengine.ext import blobstore
from google.appengine.ext.webapp import blobstore_handlers

class MainHandler(webapp2.RequestHandler):
  def get(self):
    upload_url = blobstore.create_upload_url('/upload')
    self.response.out.write('<html><body>')
    self.response.out.write('<form action="%s" method="POST" enctype="multipart/form-data">' % upload_url)
    self.response.out.write("""Upload File: <input type="file" name="file"><br> <input type="submit"
        name="submit" value="Submit"> </form></body></html>""")

class UploadHandler(blobstore_handlers.BlobstoreUploadHandler):
  def post(self):
    upload_files = self.get_uploads('file')  # 'file' is file upload field in the form
    blob_info = upload_files[0]
    self.redirect('/serve/%s' % blob_info.key())

class ServeHandler(blobstore_handlers.BlobstoreDownloadHandler):
  def get(self, resource):
    resource = str(urllib.unquote(resource))
    blob_info = blobstore.BlobInfo.get(resource)
    self.send_blob(blob_info)

app = webapp2.WSGIApplication([('/', MainHandler),
                               ('/upload', UploadHandler),
                               ('/serve/([^/]+)?', ServeHandler)],
                              debug=True)



'''
Blob store Thumbnailer
'''


import webapp2
from google.appengine.api import images
from google.appengine.ext import db

class Photo(db.Model):
    title = db.StringProperty()
    full_size_image = db.BlobProperty()

class Thumbnailer(webapp2.RequestHandler):
    def get(self):
        if self.request.get("id"):
            photo = Photo.get_by_id(int(self.request.get("id")))

            if photo:
                img = images.Image(photo.full_size_image)
                img.resize(width=80, height=100)
                img.im_feeling_lucky()
                thumbnail = img.execute_transforms(output_encoding=images.JPEG)

                self.response.headers['Content-Type'] = 'image/jpeg'
                self.response.out.write(thumbnail)
                return

        # Either "id" wasn't provided, or there was no image with that ID
        # in the datastore.
        self.error(404)

'''
get_serving_url() method allows you to generate a stable, 
dedicated URL for serving web-suitable image thumbnails. 
You simply store a single copy of your original image in Blobstore, 
and then request a high-performance per-image URL. This special URL 
can serve that image resized and/or cropped automatically, 

Image size max = 32 MB
'''

#Resize the image to 32 pixels (aspect-ratio preserved)  0-1600 range permitted
http://your_app_id.appspot.com/randomStringImageId=s32

# Crop the image to 32 pixels   0-1600 range permitted
http://your_app_id.appspot.com/randomStringImageId=s32-c



