// scalastyle:off println
package com.nemupm.spark

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.Dataset
import org.apache.spark.rdd.RDD

import java.net.URI
import java.sql.Timestamp
import java.net.InetAddress
import org.chasen.mecab.MeCab
import org.chasen.mecab.Tagger
import org.chasen.mecab.Model
import org.chasen.mecab.Lattice
import org.chasen.mecab.Node
import io.github.ikegamiyukino.neologdn.NeologdNormalizer;
import org.apache.hadoop.fs.{FileSystem, LocatedFileStatus, Path}
import org.apache.hadoop.conf.Configuration

import scala.collection.mutable.{ArrayBuffer, HashSet, Map}
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
    lang: String
)

object TweetLog {
  implicit val jsonWrites = Json.writes[TweetLog]
  implicit val jsonReads = Json.reads[TweetLog]
}

case class WordCooccurrence(
    word: String,
    cnt: Int,
    co_cnt: Map[String, Int],
    sum_cnt: Map[String, Int],
    updated_at: Map[String, Timestamp]
)

/** ETL job from tweets to co-occurrence of words */
object TweetsToCooccurrence {
  def main(args: Array[String]): Unit = {
    assert(args.length == 3)
    val yyyy = args(0) // 2021
    val mm = args(1) // 02
    val dd = args(2) // 18

    val spark = SparkSession.builder
      .appName("Tweets to Co-occurrence")
      .getOrCreate()
    import spark.implicits._
    val sc = spark.sparkContext
    sc.hadoopConfiguration.set(
      "fs.s3a.impl",
      "org.apache.hadoop.fs.s3a.S3AFileSystem"
    )
    sc.hadoopConfiguration.set("fs.s3a.access.key", sys.env("S3_ACCESS_KEY"))
    sc.hadoopConfiguration.set("fs.s3a.secret.key", sys.env("S3_SECRET_KEY"))
    sc.hadoopConfiguration.set("fs.s3a.path.style.access", "true")
    sc.hadoopConfiguration.set("fs.s3a.connection.ssl.enabled", "false")

    // Use ip address instead of hostname because "{bucket name}.hostname" cannot be resolved.
    val address: InetAddress =
      InetAddress.getByName(sys.env("S3_ENDPOINT_HOSTNAME"));
    val endpoint =
      "http://" + address.getHostAddress() + ":" + sys.env("S3_ENDPOINT_PORT");
    sc.hadoopConfiguration.set("fs.s3a.endpoint", endpoint)

    val setList = new ArrayBuffer[HashSet[String]]()
    val targetDay =
      s"s3a://twitter/topics/twitter.sampled-stream/year=${yyyy}/month=${mm}/day=${dd}"
    val conf = new Configuration()
    conf.set("fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem")
    conf.set("fs.s3a.access.key", sys.env("S3_ACCESS_KEY"))
    conf.set("fs.s3a.secret.key", sys.env("S3_SECRET_KEY"))
    conf.set("fs.s3a.path.style.access", "true")
    conf.set("fs.s3a.connection.ssl.enabled", "false")
    conf.set("fs.s3a.endpoint", endpoint)
    val fileSystem = FileSystem.get(URI.create(targetDay), conf)
    val itr = fileSystem.listFiles(new Path(targetDay), true)
    while (itr.hasNext) {
      val fileStatus: LocatedFileStatus = itr.next()
      val lines: RDD[String] = sc.textFile(fileStatus.getPath.toString)
      val currentSetList = lines
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
            case e: JsError             => tweet = ""
          }
          tweet
        })
        .map(log => {
          System.loadLibrary("MeCab")
          val res = Json.parse(log).validate[TweetLog]
          val text = new NeologdNormalizer()
            .normalize(res.map(detail => detail.text).get)
            .replaceAll(
              "(https?|ftp)(:\\/\\/[-_\\.!~*\'()a-zA-Z0-9;\\/?:\\@&=\\+$,%#]+)",
              ""
            )

          val tagger = new Tagger
          var node = tagger.parseToNode(text)

          var nouns = HashSet[String]()
          while (node != null) {
            //if (node.getFeature.split(',')(0) == "名詞") {
            if (node.getFeature.split(',')(1).contains("固有")) {
              nouns += node.getSurface
            }
            node = node.getNext
          }
          nouns
        })
        .collect()
      setList ++= currentSetList
    }

    var pairs = Map[String, Map[String, Int]]()
    var sumCnts = Map[String, Int]()
    setList.foreach(set => {
      set.foreach(word1 => {
        // update sumCnt
        var sumValue = sumCnts.getOrElseUpdate(word1, 0)
        sumCnts.update(word1, sumValue + 1)
        // update coCnt
        set.foreach(word2 => {
          if (word1 != word2 && word1.nonEmpty && word2.nonEmpty) {
            var value = pairs.getOrElseUpdate(word1, Map[String, Int]())
            value.update(word2, value.getOrElse(word2, 0) + 1)
          }
        })
      })
    })

    println(s"word cnt is ${pairs.size}")

    var rows = pairs.toList
      .map(value => {
        val word = value._1
        val cnt = sumCnts(word)
        val coCnt = value._2
        val sumCnt = coCnt.map(p => (p._1, sumCnts(p._1)))
        WordCooccurrence(word, cnt, coCnt, sumCnt, Map[String, Timestamp]())
      })
    spark.conf.set(
      s"spark.sql.catalog.catalog-development",
      "com.datastax.spark.connector.datasource.CassandraCatalog"
    )
    spark.conf.set(
      s"spark.sql.catalog.catalog-development.spark.cassandra.connection.host",
      "cassandra.default.svc.cluster.local"
    )
    var ds: Dataset[WordCooccurrence] = spark.createDataset(rows)
    ds.writeTo("`catalog-development`.development.word_cooccurrences").append()
    // spark.sql("SHOW NAMESPACES FROM `catalog-development`").show
    spark.stop()
  }
}

// scalastyle:on println
