package com.sungevity.analytics.api

import com.typesafe.config.Config
import org.apache.spark.{SparkContext, SparkConf}
import org.slf4j.LoggerFactory

import com.sungevity.analytics.utils.Reflection._

abstract class SparkApplicationContext(@transient val config: Config) extends Serializable {

  def applicationName: String

  @transient
  val log = LoggerFactory.getLogger(getClass.getName)

  @transient
  val settings = Seq(
    if(config.hasPath("spark.default.parallelism"))
      Option("spark.default.parallelism" -> config.getInt("spark.default.parallelism").toString)
    else None,
//    Some("spark.cassandra.connection.host" -> config.getString("cassandra.connection-host")),
    Some("spark.cleaner.ttl" -> config.getString("cassandra.spark-cleaner-ttl"))
  ).flatten ++
    jarOf(this.getClass).map{
      jar =>
        Seq(
          Some("spark.jars" -> jar.getAbsolutePath),
          Some("spark.master" -> config.getString("spark.master"))
        )
    }.getOrElse(
        Seq(Some("spark.master" -> "local"))
      ).flatten

  @transient
  val conf = new SparkConf().
    setAppName(applicationName).
    setAll(settings)

  @transient
  val sc = new SparkContext(conf)

  @transient
  val dataSources = new DataSources(config, sc)

}
