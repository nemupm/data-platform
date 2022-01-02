package com.nemupm.library.twitter

import play.api.libs.json.{Json, OWrites, Reads}

case class Tweet(log: String, stream: String, tag: Option[String], time: Double)

case class TweetLog(
    id: String,
    created_at: String,
    text: String,
    lang: String
)

object Tweet {
  implicit val jsonWrites: OWrites[Tweet] = Json.writes[Tweet]
  implicit val jsonReads: Reads[Tweet] = Json.reads[Tweet]
}

object TweetLog {
  implicit val jsonWrites: OWrites[TweetLog] = Json.writes[TweetLog]
  implicit val jsonReads: Reads[TweetLog] = Json.reads[TweetLog]
}
