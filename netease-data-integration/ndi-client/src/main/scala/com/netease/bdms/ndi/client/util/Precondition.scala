package com.netease.bdms.ndi.client.util

import com.netease.bdms.ndi.client.property.{Properties, Property}

object Precondition {
  def check(expression: Boolean, information: Option[String] = Option.empty): Unit = {
    if (!expression) {
      throw information.map(new IllegalArgumentException(_)).orElse(Option.apply(new IllegalArgumentException())).get
    }
  }

  def checkPropertyExist[T](property: Property[T]): Unit =
    check(Properties.properties.contains(property.key), Option.apply(s"Missing argument for option '${property.key}'."))
}
