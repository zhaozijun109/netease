package com.netease.wm.util.view

import java.text.SimpleDateFormat
import java.util.Date
import com.netease.wm.util.Implicits._

case class DateFormatter(format: String, excelFormat: Option[String]) {
  def format(value: Date): String = new SimpleDateFormat(format).format(value)
}

object DateFormatter {

  val DATE: DateFormatter = DateFormatter("yyyy-MM-dd", "yyyy-mm-dd".some)

  val DATETIME: DateFormatter = DateFormatter("yyyy-MM-dd HH:mm:ss", "yyyy-mm-dd hh:mm:ss".some)
}