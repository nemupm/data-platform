// scalastyle:off println
package com.nemupm.spark

import org.apache.spark.sql.SparkSession
import java.net.InetAddress
import org.chasen.mecab.MeCab
import org.chasen.mecab.Tagger
import org.chasen.mecab.Model
import org.chasen.mecab.Lattice
import org.chasen.mecab.Node
import scala.collection.mutable.Set
import play.api.libs.json._

case class Tweet(log: String, stream: String, tag: Option[String], time: Double)
object Tweet {
  implicit val jsonWrites = Json.writes[Tweet]
  implicit val jsonReads = Json.reads[Tweet]
}

case class TweetLog(
                     id: String,
                     created_at: String,
                     text: String,
                     author_id: String,
                     in_reply_to_user_id: Option[String],
                     possibly_sensitive: Boolean,
                     lang: String,
                     source: String,
                     format: String
                   )
object TweetLog {
  implicit val jsonWrites = Json.writes[TweetLog]
  implicit val jsonReads = Json.reads[TweetLog]
}

/** ETL job from tweets to co-occurrence of words */
object TweetsToCooccurrence {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder
      .appName("Tweets to Co-occurrence")
      .getOrCreate()
    val sc = spark.sparkContext
    sc.hadoopConfiguration.set("fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem")
    sc.hadoopConfiguration.set("fs.s3a.access.key", sys.env("S3_ACCESS_KEY"))
    sc.hadoopConfiguration.set("fs.s3a.secret.key", sys.env("S3_SECRET_KEY"))
    sc.hadoopConfiguration.set("fs.s3a.path.style.access", "true")
    sc.hadoopConfiguration.set("fs.s3a.connection.ssl.enabled", "false")

    // Use ip address instead of hostname because "{bucket name}.hostname" cannot be resolved.
    val address: InetAddress = InetAddress.getByName(sys.env("S3_ENDPOINT_HOSTNAME"));
    val endpoint = "http://" + address.getHostAddress() + ":" + sys.env("S3_ENDPOINT_PORT");
    sc.hadoopConfiguration.set("fs.s3a.endpoint", endpoint);

    val lines = sc.textFile("s3a://twitter/topics/twitter.sampled-stream/year=2020/month=08/day=23/twitter.sampled-stream+0+0000262373.json")
    val setList = lines
      .map(line => {
        val res = Json.parse(line).validate[Tweet]
        val log: JsResult[String] = res.map( tweet => tweet.log )
        log.get
      })
      .map(log => {
        System.loadLibrary("MeCab")
        val res = Json.parse(log).validate[TweetLog]
        System.out.println(res)
        val text = res.map(detail => detail.text).get

        val tagger = new Tagger
        System.out.println(tagger.parse(text))
        var node = tagger.parseToNode(text)

        var nouns = Set[String]()
        while ( node != null ) {
          System.out.println(node.getSurface + "\t" + node.getFeature)
          if (node.getFeature.split(',')(0) == "名詞") {
            nouns += node.getSurface
          }
          node = node.getNext
        }
        println(nouns)
        nouns
      }).collect()
    setList.foreach(set => println(set))
    println("--test--")
    println(setList(0))
//    val totalLength = lineLengths.reduce((a, b) => a + b)
//    println(s"total length is ${totalLength}")
    spark.stop()
  }
}
// scalastyle:on println