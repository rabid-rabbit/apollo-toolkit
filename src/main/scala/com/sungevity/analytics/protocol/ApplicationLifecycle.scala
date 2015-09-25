package com.sungevity.analytics.protocol

import scala.collection.mutable.{Set => MutableSet}
import scala.concurrent.duration.Duration

object StartApplication extends Serializable

case class ApplicationResult(code: Int)