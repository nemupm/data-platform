name := "tweets-to-co-occurrence-project"

version := "1.0"

scalaVersion := "2.12.10"

resolvers += "Palantir repository" at "https://dl.bintray.com/palantir/releases/"

libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.0.0"
libraryDependencies += "org.apache.hadoop" % "hadoop-common" % "2.7.4"
libraryDependencies += "org.apache.hadoop" % "hadoop-aws" % "2.7.4"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.9.0"
libraryDependencies += "com.datastax.spark" %% "spark-cassandra-connector" % "3.0.0-beta"
libraryDependencies += "io.github.ikegami-yukino" % "neologdn" % "0.0.1"

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
  case "application.conf"                             => MergeStrategy.concat
  case "unwanted.txt"                                 => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
