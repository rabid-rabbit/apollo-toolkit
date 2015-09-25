name := "apollo-toolkit"

organization := "com.sungevity.analytics"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.11.6"

val sparkVersion = "1.4.1"

val sprayVersion = "1.3.+"

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.0",
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "io.spray" %% "spray-http" % sprayVersion,
  "io.spray" %% "spray-can" % sprayVersion,
  "com.datastax.spark" %% "spark-cassandra-connector" % "1.4.+",
  "mysql"         % "mysql-connector-java" % "5.1.24",
  "joda-time" % "joda-time" % "2.8.1",
  "com.github.nscala-time" %% "nscala-time" % "2.0.0",
  "org.scalanlp" % "breeze_2.10" % "0.11.2",
  "org.apache.commons" % "commons-lang3" % "3.4",
  "org.scalaz" %% "scalaz-core" % "7.1.3",
  "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"
)
