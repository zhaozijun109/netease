package com.netease.wm.util.view.table

import com.netease.wm.util.view.Styling

import scala.language.implicitConversions

case class Title(value: String, style: Map[String, Any] = Map.empty) extends Styling {

  type SelfType = Title

  def withNewStyle(style: Map[String, Any]): SelfType = copy(style = style)
}

case class Note(value: String, style: Map[String, Any] = Map.empty) extends Styling {

  type SelfType = Note

  def withNewStyle(style: Map[String, Any]): SelfType = copy(style = style)
}

class Footnote(val notes: Note*)(val style: Map[String, Any] = Map.empty) extends Styling {

  type SelfType = Footnote

  def withNewStyle(style: Map[String, Any]): SelfType = new Footnote(notes: _*)(style)
}

object Footnote {
  def apply(notes: Note*): Footnote = {
    new Footnote(notes: _*)()
  }
}

case class Table(title: Option[Title] = None,
                 hHeaderRows: Seq[Row[Cell]] = Nil,
                 vHeaderRows: Seq[Row[Cell]] = Nil,
                 dataRows: Seq[Row[Cell]],
                 footnote: Option[Footnote] = None,
                 style: Map[String, Any] = Map.empty) extends Styling {

  type SelfType = Table

  def body: Seq[(Row[Cell], Row[Cell])] = vHeaderRows.zipAll(dataRows, Row(), Row())

  def withNewStyle(style: Map[String, Any]): SelfType = copy(style = style)

  def width(width: Int): SelfType = withStyle("width" -> s"${width}px")

  def columnCount: Int = {
    hHeaderRows match {
      case firstHeadRow :: _ => columnCount(firstHeadRow)
      case _ =>
        body.headOption.map {
          case (vHeaderRow, dataRow) => columnCount(vHeaderRow) + columnCount(dataRow)
        }.sum
    }
  }

  private[this] def columnCount[T <: Cell](row: Row[T]): Int = row.cells.map(_.colspan).sum
}
