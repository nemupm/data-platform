# https://developer.twitter.com/en/docs/labs/sampled-stream/quick-start

import os, sys
import requests
import json
from requests.auth import AuthBase
from requests.auth import HTTPBasicAuth
from time import sleep, time

consumer_key = os.getenv("TWITTER_CONSUMER_KEY")
consumer_secret = os.getenv("TWITTER_CONSUMER_SECRET")

assert consumer_key, "TWITTER_CONSUMER_KEY should be set as environment variable."
assert consumer_secret, "TWITTER_CONSUMER_SECRET should be set as environment variable."

lang = None
args = sys.argv
limit_per_minute = 5
assert 3 >= len(args) and len(args) >= 2, "usage: python sampled_stream.py <lang | all> [limit per minute]"
if args[1] != "all":
  lang = args[1]
if len(args) == 3:
  limit_per_minute = int(args[2])

stream_url = "https://api.twitter.com/2/tweets/sample/stream"

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

    if response.status_code != 200:
      raise Exception("Cannot get a Bearer token (HTTP {}): {}".format(response.status_code, response.text))

    body = response.json()
    return body['access_token']

  def __call__(self, r):
    r.headers['Authorization'] = "Bearer {}".format(self.bearer_token)
    return r

def stream_connect(auth):
  payload = {"tweet.fields":"created_at,lang"}
  headers = {"User-Agent": "bot"}
  response = requests.get(stream_url, auth=auth, headers=headers, params=payload, stream=True)
  count = 0
  prev_time = time()
  for response_line in response.iter_lines():
    now = time()
    if now - prev_time > 60:
      prev_time = now
      count = 0
    elif count >= limit_per_minute:
      continue
    if response_line:
      obj = json.loads(response_line)
      if "data" not in obj:
        print("key data is not included in: ", obj, file=sys.stderr)
        continue
      data = obj["data"]
      if lang and data["lang"] != lang:
        continue
      print(json.dumps(data, ensure_ascii=False))
      count += 1

bearer_token = BearerTokenAuth(consumer_key, consumer_secret)

# Listen to the stream. This reconnection logic will attempt to reconnect as soon as a disconnection is detected.
while True:
  stream_connect(bearer_token)
  sleep(10)
