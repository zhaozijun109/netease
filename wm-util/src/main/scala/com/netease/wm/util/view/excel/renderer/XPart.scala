package com.netease.wm.util.view.excel.renderer

private[excel] trait XPart {

  def path: String

  def content: Seq[String]

  protected def attr(name: String, value: Option[Any]): String = value match {
    case Some(v) => s"""$name="$v""""
    case _ => ""
  }

  protected def el(flag: Boolean, el: String): String = if (flag) el else ""
}