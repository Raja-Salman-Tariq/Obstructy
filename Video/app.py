import flask
import werkzeug
import pickle
import os
from flask import send_file
import base64
from flask import jsonify
from PIL import Image
import io

app = flask.Flask(__name__)

@app.route('/', methods = ['GET', 'POST'])
def handle_request():
  videofile = flask.request.files['video']
  filename = werkzeug.utils.secure_filename(videofile.filename)
  print("\nReceived video File name : " + videofile.filename)
  videofile.save(filename)
  os.system("mkdir imgs")
  os.system("ffmpeg -ss 00:00:00 -i videoClip.mp4 -qscale:v 2 -r 1.0 -start_number 0 imgs/00001_I%1d.png")
  # os.system("python test_fence.py")
  os.system("python moveImage.py")


  img = Image.open("C:/Users/huzai/Desktop/Video/output/00001_I5.png", mode='r')
  img_byte_arr = io.BytesIO()
  img.save(img_byte_arr, format='PNG')
  my_encoded_img = base64.encodebytes(img_byte_arr.getvalue()).decode('ascii')


  response={'image':my_encoded_img}
  return jsonify(response)

app.run(host='0.0.0.0', port=5000) #updated from 5000 to 8080