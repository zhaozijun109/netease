package com.netease.wm.util.view.table
package styler

import com.netease.wm.util.view.table.styler.Implicits._

import scala.collection.immutable.ListMap

trait TableStyler {
  self =>

  def cellStyle(cell: Cell, hHeader: Boolean, vHeader: Boolean): Map[String, Any]

  def rowStyle[T <: Cell](row: Row[T], hHeader: Boolean, rowIndex: Int): Map[String, Any]

  def titleStyle(title: Title): Map[String, Any]

  def tableStyle(table: Table): Map[String, Any]

  def noteStyle(note: Note, noteIndex: Int): Map[String, Any]

  def footnoteStyle(footnote: Footnote): Map[String, Any]

  def +(other: TableStyler): TableStyler = new TableStyler {

    override def cellStyle(cell: Cell, hHeader: Boolean, vHeader: Boolean): Map[String, Any] =
      self.cellStyle(cell, hHeader, vHeader) merge other.cellStyle(cell, hHeader, vHeader)

    override def rowStyle[T <: Cell](row: Row[T], hHeader: Boolean, rowIndex: Int): Map[String, Any] =
      Seq(self, other).map(_.rowStyle(row, hHeader, rowIndex)).reduce(_ merge _)

    override def titleStyle(title: Title): Map[String, Any] =
      Seq(self, other).map(_.titleStyle(title)).reduce(_ merge _)

    override def tableStyle(table: Table): Map[String, Any] =
      Seq(self, other).map(_.tableStyle(table)).reduce(_ merge _)

    override def noteStyle(note: Note, noteIndex: Int): Map[String, Any] =
      Seq(self, other).map(_.noteStyle(note, noteIndex)).reduce(_ merge _)

    override def footnoteStyle(footnote: Footnote): Map[String, Any] =
      Seq(self, other).map(_.footnoteStyle(footnote)).reduce(_ merge _)
  }
}

class NoopTableStyler extends TableStyler {

  def cellStyle(cell: Cell, hHeader: Boolean, vHeader: Boolean): Map[String, Any] = Map.empty

  def rowStyle[T <: Cell](row: Row[T], hHeader: Boolean, rowIndex: Int): Map[String, Any] = Map.empty

  def titleStyle(title: Title): Map[String, Any] = Map.empty

  def tableStyle(table: Table): Map[String, Any] = Map.empty

  def noteStyle(note: Note, noteIndex: Int): Map[String, Any] = Map.empty

  def footnoteStyle(footnote: Footnote): Map[String, Any] = Map.empty
}

case class StaticTableStyler(cellStyle: Map[String, Any],
                             rowStyle: Map[String, Any],
                             titleStyle: Map[String, Any],
                             tableStyle: Map[String, Any],
                             noteStyle: Map[String, Any],
                             footnoteStyle: Map[String, Any]) extends TableStyler {

  def cellStyle(cell: Cell, hHeader: Boolean, vHeader: Boolean): Map[String, Any] = cellStyle

  def rowStyle[T <: Cell](row: Row[T], hHeader: Boolean, rowIndex: Int): Map[String, Any] = rowStyle

  def titleStyle(title: Title): Map[String, Any] = titleStyle

  def tableStyle(table: Table): Map[String, Any] = tableStyle

  def noteStyle(note: Note, noteIndex: Int): Map[String, Any] = noteStyle

  def footnoteStyle(footnote: Footnote): Map[String, Any] = footnoteStyle
}

object TableStyler {
  val noopTableStyler = new NoopTableStyler

  val defaultTableStyler = StaticTableStyler(
    cellStyle = ListMap(
      "height" -> "28px",
      "text-align" -> "center",
      "border" -> "1px solid #d3cadd",
      "padding" -> "0 10px"
    ),
    rowStyle = Map.empty,
    titleStyle = ListMap(
      "caption-side" -> "top",
      "text-align" -> "left",
      "font-weight" -> "bold",
      "padding" -> "2px 0 2px 10px",
      "color" -> "#fff",
      "background" -> "#8066a0"
    ),
    tableStyle = ListMap(
      "border-collapse" -> "collapse",
      "border-spacing" -> "0",
      "border-color" -> "#d3cadd",
      "color" -> "#000",
      "font" -> "12px/1.5 Tahoma, simsun, arial, 宋体"
    ),
    noteStyle = Map.empty,
    footnoteStyle = ListMap(
      "font" -> "12px/1.5 Tahoma, simsun, arial, 宋体"
    )
  )

}