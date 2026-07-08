package com.netease.wm.util.view.table

import java.util.Date

import com.netease.wm.util.view.{DateFormatter, NumberFormatter, Styling}

sealed trait Cell extends Styling {

  type SelfType <: Cell

  def text: String

  def colspan: Int

  def rowspan: Int
}

object Cell {

  def apply[T: Numeric](value: T, numberFormatter: NumberFormatter): NumericCell[T] = {
    NumericCell(Some(value), numberFormatter)
  }

  def apply[T: Numeric](value: Option[T], numberFormatter: NumberFormatter): NumericCell[T] = {
    NumericCell(value, numberFormatter)
  }

  def apply(value: String, colspan: Int = 1, rowspan: Int = 1): TextCell = {
    TextCell(value, colspan, rowspan)
  }
}

case class CellWrapper[+T <: Cell](cell: T, hHeader: Boolean = false, vHeader: Boolean = false) extends Cell {

  type SelfType <: CellWrapper[T]

  def style: Map[String, Any] = cell.style

  def text: String = cell.text

  def colspan: Int = cell.colspan

  def rowspan: Int = cell.rowspan

  def withNewStyle(style: Map[String, Any]): SelfType = CellWrapper(cell.withNewStyle(style), hHeader, vHeader).asInstanceOf[SelfType]
}

case class NumericCell[T : Numeric](value: Option[T], numberFormatter: NumberFormatter, colspan: Int = 1, rowspan: Int = 1, style: Map[String, Any] = Map.empty) extends Cell {

  type SelfType = NumericCell[T]

  def doubleValue: Option[Double] = value.map(implicitly[Numeric[T]].toDouble)

  def text: String = value match {
    case Some(_) => numberFormatter.format(doubleValue.get)
    case _ => "--"
  }

  def withNewStyle(style: Map[String, Any]): SelfType = copy(style = style)
}

case class DateCell(value: Date, formatter: DateFormatter, colspan: Int = 1, rowspan: Int = 1, style: Map[String, Any] = Map.empty) extends Cell {

  type SelfType = DateCell

  def text: String = formatter.format(value)

  def withNewStyle(style: Map[String, Any]): SelfType = copy(style = style)
}

case class TextCell(value: String, colspan: Int = 1, rowspan: Int = 1, style: Map[String, Any] = Map.empty) extends Cell {

  type SelfType = TextCell

  def text: String = value

  def withNewStyle(style: Map[String, Any]): SelfType = copy(style = style)
}