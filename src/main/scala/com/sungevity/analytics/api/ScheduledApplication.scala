package com.sungevity.analytics.api

import akka.util.Timeout
import com.sungevity.analytics.protocol.{UnSchedule, Schedule, Scheduled}
import org.slf4j.LoggerFactory
import akka.pattern.ask

import scala.concurrent.duration._

import scala.concurrent.ExecutionContext

trait ScheduledApplication extends StackableApplication {

  private val log = LoggerFactory.getLogger(getClass.getName)

  abstract override def receive = super.receive

  protected def schedule(schedule: String, timeout: Timeout = 10 seconds)(implicit ec: ExecutionContext) = apollo("scheduler").ask(Schedule(self, schedule))(timeout) map {
    case Scheduled => log.debug(s"Successfully scheduled [${self.path.name}}]")
    case t: Throwable => throw t
  }

  protected def unschedule(schedule: String, timeout: Timeout = 10 seconds)(implicit ec: ExecutionContext) = apollo("scheduler").ask(UnSchedule(self, schedule))(timeout) map {
    case Scheduled => log.debug(s"Successfully unscheduled [${self.path.name}}]")
    case t: Throwable => throw t
  }

}
