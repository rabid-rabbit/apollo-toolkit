package com.sungevity.analytics.helpers.csv

import org.apache.commons.lang3.StringEscapeUtils
import org.apache.spark.rdd.RDD

object Csv {

  trait Reportable {

    def asCSVHeader = cells.map(v => s"""${v._1}""").mkString(",")

    def cells: Iterable[(String, Any)]

    def asCSV: String = cells.map(_._2) collect {
      case s: String => StringEscapeUtils.escapeCsv(s)
      case v => Option(v) map (_.toString) getOrElse("")
    } mkString(",")

  }


  implicit class IterableReportFormat[T <: Reportable](iterable: Iterator[T]) extends Iterator[String] {

    lazy val firstLine = if(iterable.hasNext) Option(iterable.next()) else None

    lazy val header = firstLine.map{
      line =>
        Iterator.single{
          line.asCSVHeader
        } ++ Iterator.single{
          line.asCSV
        }
    } getOrElse(Iterator.empty)

    override def hasNext: Boolean = header.hasNext || iterable.hasNext

    override def next(): String = header.hasNext match {
      case true => header.next
      case false => iterable.next.asCSV

    }

    def asCSV = this

  }

  implicit class RDDReportFormat[T <: Reportable](rdd: RDD[T]) {

    def asCSV = {

      rdd.zipWithIndex().flatMap{
        r =>
          if(r._2 == 0) Seq(r._1.asCSVHeader, r._1.asCSV)
          else Seq(r._1.asCSV)
      }

    }

  }


}
