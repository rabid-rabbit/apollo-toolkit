package com.sungevity.analytics.api

import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicBoolean

import akka.actor._
import com.sungevity.analytics.protocol.{ApplicationResult, StartApplication}
import com.typesafe.config.Config

import akka.pattern.ask
import org.slf4j.LoggerFactory

import scala.concurrent.Await

import scala.concurrent.duration._

import akka.util.Timeout

trait SparkApplicationContextAware {

  protected val applicationContext: SparkApplicationContext

}

trait StackableApplication extends SparkApplicationContextAware {

  def receive: PartialFunction[Any, Unit]

  def self: ActorRef

  protected def apollo(serviceID: String): ActorSelection

}

abstract class SparkApplication[T <: SparkApplicationContext](val appConfiguration: Config) extends Actor with StackableApplication {

  import context.dispatcher

  private val log = LoggerFactory.getLogger(getClass.getName)

  protected val applicationContext: T = initializeContext

  protected def apollo(serviceID: String) = context.actorSelection(s"akka.tcp://Apollo@127.0.0.1:2552/user/$serviceID")

  protected def initializeApplicationContext(config: Config): T

  protected def run(applicationContext: T): Int

  override def receive = {

    case StartApplication => {
      log.debug(s"Starting [${applicationContext.applicationName}]")
      try {
        sender ! ApplicationResult(run(applicationContext))
      } catch {
        case t: Throwable => log.error(s"failed to run [${applicationContext.applicationName}]", t)
      }
    }

    case m => {

      log.error(s"Unknown message [$m].")

    }
  }

  private def initializeContext: T = {

    implicit val timeout: Timeout = 10 seconds

    try {
      Await.result(
        apollo("configuration") ? "get-configuration" map {
          case c: Config => {
            val effectiveConfig = appConfiguration.withFallback(c)
            try {
              initializeApplicationContext(effectiveConfig)
            } catch {
              case t: Throwable => {
                throw new Error(s"Could not start [${context.self.toString()}]", t)
              }
            }
          }
        }, timeout.duration
      )
    } catch {
      case e: TimeoutException => initializeContext
    }

  }

}
