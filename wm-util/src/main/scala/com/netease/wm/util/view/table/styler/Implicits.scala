package com.netease.wm.util.view.table.styler

import com.netease.wm.util.Implicits._

object Implicits {

  implicit class StyleWrapper(style: Map[String, Any]) {

    def getDouble(name: String): Option[Double] = style.get(name).map(_.asInstanceOf[Double])

    def getString(name: String): Option[String] = style.get(name).map(_.toString)

    def merge(other: Map[String, Any]): Map[String, Any] = style.toListMap ++ other
  }

}