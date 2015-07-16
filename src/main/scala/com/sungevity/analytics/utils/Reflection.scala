package com.sungevity.analytics.utils

import java.io.File

object Reflection {

  def jarOf(implicit cl: Class[_]) = new File(cl.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()) match {
    case jar if jar.getName.endsWith(".jar") => Some(jar)
    case _ => None
  }

}
