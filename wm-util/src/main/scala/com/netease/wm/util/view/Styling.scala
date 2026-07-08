package com.netease.wm.util
package view

import _root_.com.netease.wm.util.Implicits._

trait Styling {

  type SelfType

  def style: Map[String, Any]

  def withNewStyle(style: Map[String, Any]): SelfType

  def withStyle(style: Map[String, Any], overwrite: Boolean = false): SelfType = {
    if (overwrite) {
      withNewStyle(style)
    } else {
      withNewStyle(this.style.toListMap ++ style)
    }
  }

  def withStyle(style: (String, Any)*): SelfType = withStyle(style.toMap)
}

object Styling {

  def css(style: Map[String, Any]): String = style.map {
    case (key, value) =>
      key + ": " + value
  }.mkString(";")
}