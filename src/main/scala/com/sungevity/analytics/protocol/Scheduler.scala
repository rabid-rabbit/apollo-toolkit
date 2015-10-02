package com.sungevity.analytics.protocol

import akka.actor.ActorRef

case class Schedule(actor: ActorRef, schedule: String)

object Scheduled extends Serializable

case class UnSchedule(actor: ActorRef, schedule: String)

object Unscheduled extends Serializable

