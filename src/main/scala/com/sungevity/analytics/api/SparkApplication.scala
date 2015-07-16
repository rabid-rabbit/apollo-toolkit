package com.sungevity.analytics.api

import java.util.concurrent.TimeoutException

import akka.actor.{ActorRef, Actor}
import com.typesafe.config.Config

import akka.pattern.ask

import scala.concurrent.Await

import scala.concurrent.duration._

import akka.util.Timeout

abstract class ResultMarshaller[T] {

  def produce(client: ActorRef, result: T): Unit

}

object SparkApplication {

  class StringIteratorMarshaller extends ResultMarshaller[Iterator[String]] {

    override def produce(client: ActorRef, result: Iterator[String]) = {

      result.flatMap(_.getBytes("UTF-8")).sliding(1024, 1024).foreach {
        chunk =>
          client ! chunk.toArray
      }
      client ! "done"
    }

  }

  implicit val stringIteratorMarshaller = new StringIteratorMarshaller

}

abstract class SparkApplication[T <: SparkApplicationContext, R](val appConfiguration: Config)(implicit marshaller: ResultMarshaller[R]) extends Actor {

  import context.dispatcher

  val applicationContext: T = initializeContext

  def run(): R

  protected def initializeApplicationContext(config: Config): T

  override def receive = {

    case "run" => {

      marshaller.produce(sender,  run)

    }

  }

  private def initializeContext: T = {

    implicit val timeout: Timeout = 10 seconds

    val router = context.actorSelection("akka.tcp://Apollo@127.0.0.1:2552/user/configuration")

    try{
      Await.result(
        router ? "get-configuration" map {
          case c: Config => {
            initializeApplicationContext(appConfiguration.withFallback(c))
          }
        }, timeout.duration
      )
    } catch {
      case e: TimeoutException => initializeContext
    }

  }



}
