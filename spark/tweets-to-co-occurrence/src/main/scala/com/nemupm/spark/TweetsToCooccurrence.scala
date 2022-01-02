// scalastyle:off println
package com.nemupm.spark

import com.fasterxml.jackson.core.JsonParseException
import com.nemupm.library.twitter.{MinioEnvironment, TweetLog, TwitterService}
import io.github.ikegamiyukino.neologdn.NeologdNormalizer
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.SparkSession
import play.api.libs.json._

import java.sql.Timestamp
import java.time.LocalDate
import scala.collection.mutable
import scala.collection.Map
//import org.chasen.mecab.Tagger
import net.moraleboost.mecab.impl.StandardTagger

case class WordCooccurrence(
    word: String,
    cnt: Int,
    co_cnt: Map[String, Int],
    sum_cnt: Map[String, Int],
    updated_at: Map[String, Timestamp]
)

class MyNeologdNormalizer extends NeologdNormalizer with Serializable

/** ETL job from tweets to co-occurrence of words */
object TweetsToCooccurrence {

  def main(args: Array[String]): Unit = {
    assert(args.length == 4)
    val yyyy = args(0) // 2021
    val mm = args(1) // 02
    val dd = args(2) // 18
    val days = args(3) // 1

    val from = LocalDate.of(yyyy.toInt, mm.toInt, dd.toInt)
    val to = from.plusDays(days.toInt)

    println(s"Starting Tweets-to-Co-occurrence job")

    val spark = SparkSession.builder
      .appName("Tweets to Co-occurrence")
      .getOrCreate()
    import spark.implicits._
    val environment = new MinioEnvironment(spark)

    val tweets = new TwitterService(environment).fetchTweetRdd(from, to)

    System.loadLibrary("MeCab")
    val normalizer = new MyNeologdNormalizer()
    val wordSetCollection: RDD[mutable.HashSet[String]] =
      tweets
        .map(tweet => {
          var res: Option[JsResult[TweetLog]] = None
          try {
            res = Some(Json.parse(tweet.log).validate[TweetLog])
          } catch {
            case e: JsonParseException => // failed to parse json
          }
          if (res.isEmpty) "" else res.get.map(detail => detail.text).get
        })
        .filter(tweet => tweet.nonEmpty)
        .map(tweet => {
          val text = normalizer
            .normalize(tweet)
            .replaceAll(
              "(https?|ftp)(:\\/\\/[-_\\.!~*\'()a-zA-Z0-9;\\/?:\\@&=\\+$,%#]+)",
              ""
            )
          val tagger = new StandardTagger("")
          val lattice = tagger.createLattice()
          lattice.setSentence(text)
          tagger.parse(lattice)
          var node = lattice.bosNode()
          val nouns = mutable.HashSet[String]()
          while (node != null) {
            //if (node.getFeature.split(',')(0) == "名詞") {
            val wordInfo = node.feature().split(',')
            if (wordInfo.length >= 2 && wordInfo(1).contains("固有")) {
              nouns += node.surface()
            }
            node = node.next()
          }
          lattice.destroy()
          tagger.destroy()
          nouns
        })

    wordSetCollection.cache()

    // The number of tweets.
    val docSize = wordSetCollection.count()

    println(s"The number of tweets is ${docSize}.")

    // Document frequency.
    // Map[word] = the number of tweets where the word is included.
    val docFreq: Map[String, Int] = wordSetCollection
      .flatMap(words => words.map(word => (word, 1)))
      .aggregateByKey(0)((cnt, add) => cnt + add, (cnt1, cnt2) => cnt1 + cnt2)
      .collectAsMap()

    println(s"The number of words is ${docFreq.size}.")

    // Drop words which occur too frequently
    val validateKey: (String) => Boolean = word =>
      docSize > docFreq.getOrElse(word, 0) * 1000

    // List of co-occurrence
    val pairsOfWord: RDD[(String, String)] = wordSetCollection
      .flatMap(wordSet => {
        val filteredWordSet = wordSet.filter(validateKey)
        filteredWordSet.flatMap(word1 =>
          filteredWordSet
            .filter(word => word != word1)
            .flatMap(word2 =>
              List((word1, word2), (word2, word1))
            ) // List[(String, String)]
        ) // List[(String, String)]
      })

    // Rows to be inserted
    val rows: RDD[WordCooccurrence] = pairsOfWord.groupByKey
      .flatMap {
        case (key: String, words: Iterable[String]) => {
          val coCnt: Map[String, Int] = words
            .groupBy(word => word)
            .map { case (word, list) => (word, list.size) }
          val sumCnt = coCnt.map { case (word, cnt) =>
            (word, docFreq.getOrElse(word, 0))
          }
          List(
            WordCooccurrence(
              key,
              docFreq.getOrElse(key, 0),
              coCnt,
              sumCnt,
              mutable.HashMap[String, Timestamp]()
            )
          )
        }
      }
    spark.conf.set(
      s"spark.sql.catalog.catalog-development",
      "com.datastax.spark.connector.datasource.CassandraCatalog"
    )
    spark.conf.set(
      s"spark.sql.catalog.catalog-development.spark.cassandra.connection.host",
      "cassandra.default.svc.cluster.local"
    )
    val ds: Dataset[WordCooccurrence] = spark.createDataset(rows)
    ds.writeTo("`catalog-development`.development.word_cooccurrences")
      .append()
    spark.sql("SHOW NAMESPACES FROM `catalog-development`").show
    spark.stop()
  }
}

// scalastyle:on println
