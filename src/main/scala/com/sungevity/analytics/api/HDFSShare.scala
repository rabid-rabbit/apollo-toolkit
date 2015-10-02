package com.sungevity.analytics.api

import org.apache.hadoop.fs.{Path, FileSystem}
import org.apache.spark.rdd.RDD

import scalaz.syntax.std.BooleanOps

trait HDFSShare {

  self: SparkApplicationContextAware =>

  private lazy val basicPath = applicationContext.config.getString("shared.data.path")

  protected def publish(filename: String, truncate: Boolean = false)(data: => RDD[String]) = {

    def commit(source: String, destination: String) {

      val fs = FileSystem.get(applicationContext.sc.hadoopConfiguration)
      val hDestination = new Path(destination)
      val hSource = new Path(source)

      for {
        _ <- new BooleanOps(fs.exists(hDestination)).option()
      } yield {
        new BooleanOps(truncate).option() map (_ => fs.delete(hDestination, true)) orElse(throw new Exception(s"Could not override [$hDestination]"))
      }

      fs.rename(hSource, hDestination)

      data

    }

    def clear(location: String) {

      val fs = FileSystem.get(applicationContext.sc.hadoopConfiguration)
      val hLocation = new Path(location)

      new BooleanOps(fs.exists(hLocation)).option().map(_ => fs.delete(hLocation, true))

    }

    val destination = s"$basicPath/${applicationContext.applicationName}/$filename"
    val tmpDestination = s"$basicPath/${applicationContext.applicationName}/_$filename"

    try {

      data.saveAsTextFile(tmpDestination)

      commit(tmpDestination, destination)

      clear(tmpDestination)

    } finally {

      clear(tmpDestination)

    }

    data

  }

  protected def stream(resource: String): Option[Stream[String]] = {

    val path = s"$basicPath/$resource"

    for {
      _ <- new BooleanOps(resource.length > 0).option()
      _ <- new BooleanOps(FileSystem.get(applicationContext.sc.hadoopConfiguration).exists(new Path(path))).option()
    } yield {
      applicationContext.sc.textFile(path).toLocalIterator.toStream
    }


  }

}
