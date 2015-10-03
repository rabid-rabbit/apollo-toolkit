package com.sungevity.analytics.api

import com.sungevity.analytics.helpers.sql.ConnectionStrings
import com.sungevity.analytics.helpers.sql.Queries
import com.typesafe.config.Config
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.joda.time.DateTime

class DataSources(config: Config, sparkContext: SparkContext) {

  val sqlContext = new SQLContext(sparkContext)

  def allSystemsData = sqlContext.read.format("jdbc").options(
    Map("url" -> ConnectionStrings.current(config),
      "dbtable" -> s"(${Queries.allSystems}) as all_systems")).load()

  def systemData(start: DateTime, end: DateTime, nDays: Int) = sqlContext.read.format("jdbc").options(
    Map("url" -> ConnectionStrings.current(config),
      "dbtable" -> s"(${Queries.systemData(start, end, nDays)}) as system_data")).load()

  def productionData(start: DateTime, end: DateTime) = sqlContext.read.format("jdbc").options(
    Map("url" -> ConnectionStrings.current(config),
      "dbtable" -> s"(${Queries.productionData(start, end)}) as production_data")).load()

  def estimatedPerformance = sqlContext.read.format("jdbc").options(
    Map("url" -> ConnectionStrings.current(config),
      "dbtable" -> s"(${Queries.estimatedPerformance}) as estimated_performance")).load()

}
