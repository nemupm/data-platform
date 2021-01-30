// scalastyle:off println
package com.nemupm.spark

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.Dataset
import org.apache.spark.rdd.{RDD}

import java.sql.Timestamp
import java.net.InetAddress
import org.chasen.mecab.MeCab
import org.chasen.mecab.Tagger
import org.chasen.mecab.Model
import org.chasen.mecab.Lattice
import org.chasen.mecab.Node

import scala.collection.mutable.HashSet
import scala.collection.mutable.Map
import play.api.libs.json._
import com.datastax.spark.connector.cql._

case class Tweet(log: String, stream: String, tag: Option[String], time: Double)

object Tweet {
  implicit val jsonWrites = Json.writes[Tweet]
  implicit val jsonReads = Json.reads[Tweet]
}

case class TweetLog(
                     id: String,
                     created_at: String,
                     text: String,
                     lang: String,
                   )

object TweetLog {
  implicit val jsonWrites = Json.writes[TweetLog]
  implicit val jsonReads = Json.reads[TweetLog]
}

case class WordCooccurrence(
                             word: String,
                             cnt: Int,
                             co_cnt: Map[String, Int],
                             updated_at: Map[String, Timestamp]
                           )

/** ETL job from tweets to co-occurrence of words */
object TweetsToCooccurrence {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder
      .appName("Tweets to Co-occurrence")
      .getOrCreate()
    import spark.implicits._
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

    val lines: RDD[String] = sc.textFile("s3a://twitter/topics/twitter.sampled-stream/year=2021/month=01/day=03/twitter.sampled-stream+0+0000019000.json")
    val setList = lines
      .map(rawLine => {
        // FIXME: This is temporary solution for json parse error; Fix kafka-connect setting.
        // RawLine's both ends' double quotes are needless.
        // * rawLine(incorrect): "{\"log\":\"...\"}"
        // * should be(correct): {"log":"..."}
        val line = rawLine
          .substring(1, rawLine.length() - 1)
          .replaceAll("([^\\\\])\\\\\"", "$1\"") // \" -> "
          .replaceAll("([^\\\\])\\\\\\\\\\\\\"", "$1\\\\\"") // \\\" -> \"
          .replace("\\\\\\\\\"", "\"") // \\\\\\\" -> \\\" or ...
          .replace("}\\\\n\"", "}\"")
        var tweet = ""
        val res: JsResult[Tweet] = Json.parse(line).validate[Tweet]
        res match {
          case JsSuccess(t: Tweet, _) => tweet = t.log
          case e: JsError => tweet = ""
        }
        tweet
      })
      .map(log => {
        System.loadLibrary("MeCab")
        val res = Json.parse(log).validate[TweetLog]
        val text = res.map(detail => detail.text).get

        val tagger = new Tagger
        var node = tagger.parseToNode(text)

        var nouns = HashSet[String]()
        while (node != null) {
          if (node.getFeature.split(',')(0) == "名詞") {
            nouns += node.getSurface
          }
          node = node.getNext
        }
        nouns
      })
      .collect()

    var pairs = Map[String, Map[String, Int]]()
    setList.foreach(set => {
      set.foreach(word1 => {
        set.foreach(word2 => {
          if (word1 != word2 && word1.nonEmpty && word2.nonEmpty) {
            var value = pairs.getOrElseUpdate(word1, Map[String, Int]())
            value.update(word2, value.getOrElse(word2, 0) + 1)
          }
        })
      })
    })
    
    println(s"word cnt is ${pairs.size}")

    var rows = pairs
      .toList
      .map(value => {
        WordCooccurrence(value._1, 1, value._2, Map[String, Timestamp]())
      })
    spark.conf.set(s"spark.sql.catalog.catalog-development", "com.datastax.spark.connector.datasource.CassandraCatalog")
    spark.conf.set(s"spark.sql.catalog.catalog-development.spark.cassandra.connection.host", "cassandra.default.svc.cluster.local")
    var ds: Dataset[WordCooccurrence] = spark.createDataset(rows)
    ds.writeTo("`catalog-development`.development.word_cooccurrences").append()
    // spark.sql("SHOW NAMESPACES FROM `catalog-development`").show
    spark.stop()
  }
}

// scalastyle:on println