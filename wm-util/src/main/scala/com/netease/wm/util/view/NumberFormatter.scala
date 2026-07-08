package com.netease.wm.util.view

import java.text.DecimalFormat
import com.netease.wm.util.Implicits._

class NumberFormatter(val format: String, val excelFormat: Option[String] = None, val prepare: Double => Double = identity) {
  def format(value: Double): String = if(value.isNaN) "-" else new DecimalFormat(format).format(prepare(value))
}

object NumberFormatter {

  def apply(format: String, excelFormat: Option[String] = None, prepare: Double => Double = identity): NumberFormatter =
    new NumberFormatter(format, excelFormat, prepare)

  val format0f: NumberFormatter = NumberFormatter("0", "0".some)

  val format2f: NumberFormatter = NumberFormatter("0.00", "0.00".some)

  val format1fp: NumberFormatter = NumberFormatter("0.0%", "0.0%".some)

  val format2f_w: NumberFormatter = NumberFormatter("0.00", "0.00".some, _ / 10000)
}