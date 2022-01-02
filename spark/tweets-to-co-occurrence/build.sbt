name := "tweets-to-co-occurrence-project"

version := "1.0"

scalaVersion := "2.12.10"

resolvers += "Palantir repository" at "https://dl.bintray.com/palantir/releases/"

libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.2.0"
//libraryDependencies += "org.apache.spark" %% "spark-mllib" % "3.2.0"
libraryDependencies += "org.apache.hadoop" % "hadoop-common" % "2.7.4"
libraryDependencies += "org.apache.hadoop" % "hadoop-aws" % "2.7.4" exclude ("joda-time", "joda-time")
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.9.0"
libraryDependencies += "com.datastax.spark" %% "spark-cassandra-connector" % "3.0.0-beta"
libraryDependencies += "io.github.ikegami-yukino" % "neologdn" % "0.0.1"
libraryDependencies += "com.nemupm.library" % "twitter" % "1.0" from "https://s3.ap-northeast-1.amazonaws.com/share.nemupm.com/library/twitter-library-project-assembly-1.0.jar" changing ()
//libraryDependencies += "com.nemupm.library" % "twitter" % "1.0" from "https://s3.ap-northeast-1.amazonaws.com/share.nemupm.com/library/twitter-library-project-assembly-1.0.jar"
//libraryDependencies += "net.moraleboost.cmecab-java" % "cmecab-java" % "2.1.0" from "https://github.com/takscape/cmecab-java/releases/download/2.1.0/cmecab-java-2.1.0.tar.gz"
//libraryDependencies += "com.nativelibs4java" % "bridj" % "0.7.0" from "https://github.com/takscape/cmecab-java/releases/download/2.1.0/cmecab-java-2.1.0.tar.gz"
//libraryDependencies += "com.holdenkarau" %% "spark-testing-base" % "3.0.1_1.0.0" % "test"

//fork in Test := true
//javaOptions ++= Seq("-Xms512M", "-Xmx2048M", "-XX:MaxPermSize=2048M", "-XX:+CMSClassUnloadingEnabled")

//enablePlugins(PackPlugin)

assemblyMergeStrategy in assembly := {
  case PathList("javax", "servlet", xs @ _*) => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".properties" =>
    MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".xml"   => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".types" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".class" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".txt"   => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".json"  => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".css"   => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".dtd"   => MergeStrategy.first
  case "application.conf"                             => MergeStrategy.concat
  case "unwanted.txt"                                 => MergeStrategy.discard
  case ".gitkeep"                                     => MergeStrategy.discard
  case "META-INF/jersey-module-version"               => MergeStrategy.first
  case "META-INF/taglib.tld"                          => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
