// scalastyle:off println
package com.nemupm.spark

import org.apache.spark.sql.SparkSession
import java.net.InetAddress
import org.chasen.mecab.MeCab;
import org.chasen.mecab.Tagger;
import org.chasen.mecab.Model;
import org.chasen.mecab.Lattice;
import org.chasen.mecab.Node;

/** ETL job from tweets to co-occurrence of words */
object TweetsToCooccurrence {
  def main(args: Array[String]): Unit = {
    try System.load("/usr/local/lib/libMeCab.so")
    catch {
      case e : Throwable =>
        System.err.println("Cannot load the example native code.\nMake sure your LD_LIBRARY_PATH contains \'.\'\n" + e)
        System.exit(1)
    }

    val tagger = new Tagger()
    val str = "太郎は二郎にこの本を渡した。"
    System.out.println(tagger.parse(str))

    val spark = SparkSession
      .builder
      .appName("Tweets to Co-occurrence")
      .getOrCreate()
    // Thread.sleep(1000000)
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

    val lines = sc.textFile("s3a://twitter/topics/twitter.sampled-stream/year=2020/month=08/day=10/twitter.sampled-stream+0+0000075019.json")
    val lineLengths = lines.map(s => s.length)
    val totalLength = lineLengths.reduce((a, b) => a + b)
    println(s"total length is ${totalLength}")
    spark.stop()
  }
}
// scalastyle:on println