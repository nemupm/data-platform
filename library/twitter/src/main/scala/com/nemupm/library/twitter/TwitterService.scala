// scalastyle:off println
package com.nemupm.library.twitter

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, LocatedFileStatus, Path}
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import play.api.libs.json._

import java.net.{InetAddress, URI}
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import scala.collection.mutable.ListBuffer

abstract class SparkEnvironment {
  val sparkContext: SparkContext
  val hadoopConfiguration: Configuration
}

class MinioEnvironment(spark: SparkSession) extends SparkEnvironment {
  // Use ip address instead of hostname because "{bucket name}.hostname" cannot be resolved.
  private val address: InetAddress =
    InetAddress.getByName(sys.env("S3_ENDPOINT_HOSTNAME"));
  private val endpoint: String =
    "http://" + address.getHostAddress + ":" + sys.env("S3_ENDPOINT_PORT");

  override val sparkContext: SparkContext = {
    val sc = spark.sparkContext
    sc.hadoopConfiguration.set(
      "fs.s3a.impl",
      "org.apache.hadoop.fs.s3a.S3AFileSystem"
    )
    sc.hadoopConfiguration.set("fs.s3a.access.key", sys.env("S3_ACCESS_KEY"))
    sc.hadoopConfiguration.set("fs.s3a.secret.key", sys.env("S3_SECRET_KEY"))
    sc.hadoopConfiguration.set("fs.s3a.path.style.access", "true")
    sc.hadoopConfiguration.set("fs.s3a.connection.ssl.enabled", "false")
    sc.hadoopConfiguration.set("fs.s3a.endpoint", endpoint)
    sc
  }
  override val hadoopConfiguration: Configuration = {
    val conf = new Configuration()
    conf.set("fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem")
    conf.set("fs.s3a.access.key", sys.env("S3_ACCESS_KEY"))
    conf.set("fs.s3a.secret.key", sys.env("S3_SECRET_KEY"))
    conf.set("fs.s3a.path.style.access", "true")
    conf.set("fs.s3a.connection.ssl.enabled", "false")
    conf.set("fs.s3a.endpoint", endpoint)
    conf
  }
}

/** Fetch tweets from s3 */
class TwitterService(environment: SparkEnvironment) {

  /** @return array of tweets during [from, to].
    */
  def fetchTweets(from: LocalDate, to: LocalDate): List[Tweet] = {
    fetchTweetRdd(from, to).collect.toList
  }

  def fetchTweetRdd(from: LocalDate, to: LocalDate): RDD[Tweet] = {
    fetchTweetRdd(from, to, 0)
  }

  def fetchTweetRdd(from: LocalDate, to: LocalDate, limit: Int): RDD[Tweet] = {
    environment.sparkContext.union(
      Range(0, from.until(to, ChronoUnit.DAYS).toInt + 1, 1)
        .map(from.plusDays(_))
        .flatMap(day =>
          limit match {
            case 0 => getTweetFilePaths(day)
            case _ => getTweetFilePaths(day).take(limit)
          }
        )
        .map(tweetFilePath => {
          environment.sparkContext
            .textFile(tweetFilePath)
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
              var result: Option[Tweet] = None
              val res: JsResult[Tweet] = Json.parse(line).validate[Tweet]
              res match {
                case JsSuccess(t: Tweet, _) => result = Some(t)
                case JsError(errors)        =>
              }
              result
            })
            .filter { v => v.isDefined }
            .map { case Some(v) => v }
        })
    )
  }

  private def getTweetFilePaths(day: LocalDate): List[String] = {
    val targetDay =
      f"s3a://twitter/topics/twitter.sampled-stream/year=${day.getYear}/month=${day.getMonthValue}%02d/day=${day.getDayOfMonth}%02d"
    val fileSystem =
      FileSystem.get(URI.create(targetDay), environment.hadoopConfiguration)
    val itr = fileSystem.listFiles(new Path(targetDay), true)
    val result = ListBuffer[String]()
    while (itr.hasNext) {
      val fileStatus: LocatedFileStatus = itr.next()
      result += fileStatus.getPath.toString
    }
    result.toList
  }
}

// scalastyle:on println
