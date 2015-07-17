package com.sungevity.analytics.protocol

import scala.collection.mutable.{Set => MutableSet}
import scala.concurrent.duration.Duration

object ApplicationRegistryEntryConfirmed extends Serializable

object PleaseUpdateApplicationRegistry extends Serializable

object RemoveApplicationRegistryEntryConfirmed extends Serializable

case class GetApplicationRegistryEntry(applicationName: String)

case class RemoveApplicationRegistryEntry(applicationName: String)

case class ApplicationRegistryEntry(applicationName: String, address: String, estimatedMaxRuntime: Duration) {

  override def equals(that: Any) = {
    that.isInstanceOf[ApplicationRegistryEntry] && that.asInstanceOf[ApplicationRegistryEntry].applicationName == this.applicationName
  }

  override def hashCode(): Int = this.applicationName.hashCode

}


