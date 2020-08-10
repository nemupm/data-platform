// scalastyle:off println
package com.nemupm.spark

import org.apache.spark.sql.SparkSession

/** ETL job from tweets to co-occurrence of words */
object TweetsToCooccurrence {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder
      .appName("Spark Pi")
      .getOrCreate()
    val sc = spark.sparkContext
    sc.hadoopConfiguration.set("fs.s3a.access.key", sys.env("S3_ACCESS_KEY"))
    sc.hadoopConfiguration.set("fs.s3a.secret.key", sys.env("S3_SECRET_KEY"))
    sc.hadoopConfiguration.set("fs.s3a.endpoint", sys.env("S3_ENDPOINT"))
    sc.hadoopConfiguration.set("fs.s3a.connection.ssl.enabled", "false")

    val lines = sc.textFile("s3a://twitter/topics/twitter.sampled-stream/year=2020/month=08/day=10/twitter.sampled-stream+0+0000075019.json")
    val lineLengths = lines.map(s => s.length)
    val totalLength = lineLengths.reduce((a, b) => a + b)
    println(s"total length is ${totalLength}")
    spark.stop()
  }
}
// scalastyle:on println