package com.sungevity.analytics.api

import java.util.concurrent.TimeoutException

import akka.actor.{ActorRef, Actor}
import com.sungevity.analytics.protocol.{ApplicationRegistryEntryConfirmed, ApplicationRegistryEntry}
import com.typesafe.config.Config

import akka.pattern.ask
import org.slf4j.LoggerFactory

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

  private val log = LoggerFactory.getLogger(getClass.getName)

  protected val applicationContext: T = initializeContext

  registerApplication

  log.info(s"Application ${applicationContext.applicationName} has been successfully registered in Apollo.")

  def run(applicationContext: T): R

  protected def initializeApplicationContext(config: Config): T

  override def receive = {

    case "run" => {

      try {
        marshaller.produce(sender,  run(applicationContext))
      } catch {
        case t: Throwable => log.error("Application failed.", t)
      }

    }

  }

  private def initializeContext: T = {

    implicit val timeout: Timeout = 10 seconds

    val router = context.actorSelection("akka.tcp://Apollo@127.0.0.1:2552/user/router/configuration")

    try{
      Await.result(
        router ? "get-configuration" map {
          case c: Config => {
            val effectiveConfig = appConfiguration.withFallback(c)
            initializeApplicationContext(effectiveConfig)
          }
        }, timeout.duration
      )
    } catch {
      case e: TimeoutException => initializeContext
    }

  }

  private def registerApplication: Unit = {

    implicit val timeout: Timeout = 10 seconds

    val port = appConfiguration.getInt("akka.remote.netty.tcp.port")

    val lifecycle = context.actorSelection("akka.tcp://Apollo@127.0.0.1:2552/user/router/lifecycle")

    try{
      Await.result(
        lifecycle ? ApplicationRegistryEntry(applicationContext.applicationName, s"akka.tcp://${applicationContext.applicationName}@127.0.0.1:${port}/user/${applicationContext.applicationName.toLowerCase}", 1 day) map {
          case ApplicationRegistryEntryConfirmed => // do nothing
        }, timeout.duration
      )
    } catch {
      case e: TimeoutException => registerApplication
    }

  }



}
