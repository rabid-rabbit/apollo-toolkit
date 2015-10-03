package com.sungevity.analytics.api

import com.typesafe.config.Config
import org.apache.spark.{SparkContext, SparkConf}
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._

import com.sungevity.analytics.utils.Reflection._


abstract class SparkApplicationContext(@transient val config: Config, @transient val additional: Set[(String, String)] = Set.empty) extends Serializable {

  def applicationName: String

  private def customSettings = config.entrySet().map(entry => entry.getKey -> entry.getValue.unwrapped.toString) ++ additional

  private def sparkSettings = {
    val reservedSettings = Seq(
      "spark.jars",
      "spark.master"
    )
    customSettings.filter(_._1.startsWith("spark.")).
      filter(e => !reservedSettings.exists(e._1.endsWith))
  }

  @transient
  val log = LoggerFactory.getLogger(getClass.getName)

  @transient
  val settings = sparkSettings ++
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
