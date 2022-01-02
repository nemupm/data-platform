name := "twitter-library-project"

version := "1.0"

scalaVersion := "2.12.10"

resolvers += "Palantir repository" at "https://dl.bintray.com/palantir/releases/"

libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.2.0" % "provided"
libraryDependencies += "org.apache.hadoop" % "hadoop-common" % "2.7.4" % "provided"
libraryDependencies += "org.apache.hadoop" % "hadoop-aws" % "2.7.4" % "provided"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.9.0"

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
  case "application.conf"                             => MergeStrategy.concat
  case "unwanted.txt"                                 => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
