# https://developer.twitter.com/en/docs/labs/sampled-stream/quick-start

import os, sys
import requests
import json
from requests.auth import AuthBase
from requests.auth import HTTPBasicAuth
from time import sleep

consumer_key = os.getenv("TWITTER_CONSUMER_KEY")
consumer_secret = os.getenv("TWITTER_CONSUMER_SECRET")

assert consumer_key, "TWITTER_CONSUMER_KEY should be set as environment variable."
assert consumer_secret, "TWITTER_CONSUMER_SECRET should be set as environment variable."

lang = None
args = sys.argv
assert len(args) == 2, "usage: python sampled_stream.py <lang | all>"
if args[1] != "all":
  lang = args[1]

stream_url = "https://api.twitter.com/labs/1/tweets/stream/sample"

# Gets a bearer token
class BearerTokenAuth(AuthBase):
  def __init__(self, consumer_key, consumer_secret):
    self.bearer_token_url = "https://api.twitter.com/oauth2/token"
    self.consumer_key = consumer_key
    self.consumer_secret = consumer_secret
    self.bearer_token = self.get_bearer_token()

  def get_bearer_token(self):
    response = requests.post(
      self.bearer_token_url,
      auth=(self.consumer_key, self.consumer_secret),
      data={'grant_type': 'client_credentials'},
      headers={"User-Agent": "bot"})

    if response.status_code is not 200:
      raise Exception("Cannot get a Bearer token (HTTP {}): {}".format(response.status_code, response.text))

    body = response.json()
    return body['access_token']

  def __call__(self, r):
    r.headers['Authorization'] = "Bearer {}".format(self.bearer_token)
    return r

def stream_connect(auth):
  payload = {"format":"detailed"}
  headers = {"User-Agent": "bot"}
  response = requests.get(stream_url, auth=auth, headers=headers, params=payload, stream=True)
  for response_line in response.iter_lines():
    if response_line:
      data = json.loads(response_line)["data"]
      if lang and data["lang"] != lang:
        continue
      print(json.dumps(data, ensure_ascii=False))

bearer_token = BearerTokenAuth(consumer_key, consumer_secret)

# Listen to the stream. This reconnection logic will attempt to reconnect as soon as a disconnection is detected.
while True:
  stream_connect(bearer_token)
  sleep(10)