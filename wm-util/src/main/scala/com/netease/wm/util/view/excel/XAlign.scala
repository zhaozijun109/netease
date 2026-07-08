package com.netease.wm.util.view.excel

case class XAlignment(hAlign: Option[String], vAlign: Option[String], wrapText: Boolean, indent: Option[Int] = None) {
  hAlign.foreach(value => require(XHorizontalAlignment.values(value)))
  vAlign.foreach(value => require(XVerticalAlignment.values(value)))
  indent.foreach(value => require(value > 0))
}

object XHorizontalAlignment {
  val LEFT = "left"
  val CENTER = "center"
  val RIGHT = "right"

  val values: Set[String] = Set(LEFT, CENTER, RIGHT)
}

object XVerticalAlignment {
  val TOP = "top"
  val CENTER = "center"
  val BOTTOM = "bottom"

  val values: Set[String] = Set(TOP, CENTER, BOTTOM)
}
