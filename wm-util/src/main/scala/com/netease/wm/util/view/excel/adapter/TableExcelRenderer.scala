package com.netease.wm.util.view.excel
package adapter

import java.io.OutputStream
import java.util.Date

import com.netease.wm.util.Implicits._
import com.netease.wm.util.view.table._
import com.netease.wm.util.view.table.styler.{HtmlColorName, NoopTableStyler, TableStyler}

import scala.collection.immutable.ListMap
import scala.util.matching.Regex
import com.netease.wm.util.view.table.styler.Implicits._
import com.netease.wm.util.view.excel.XBorderStyle._

import com.netease.wm.util.view.table.styler.TableStyler._

case class TableAdapter(table: Table, cols: Seq[XColumn] = Nil, freeze: Option[XFreeze] = None, tableStyler: Option[TableStyler] = None)

private[this] case class CellNotSupportedException() extends RuntimeException

private[this] case class CompositeCssNotSupportedException(m: String) extends RuntimeException(m)

private[this] case class FontSizeNotSupportedException(m: String) extends RuntimeException(m)

/**
  * All data is cached in memory.
  * Should not be used with very large tables, otherwise it may fail due to large memory footprint.
  */
object TableExcelRenderer {

  private[this] val DIGITS: Regex = """^(\d+)$""".r

  private[this] val RGB: Regex = """.*?#([0-9a-fA-F]{6}).*""".r

  private[this] val ShortRGB: Regex = """.*?#([0-9a-fA-F]{3}).*""".r

  private[this] val PIXELS: Regex = """.*?(\d+)px.*""".r

  def cellStyle(cell: Cell, hHeader: Boolean, vHeader: Boolean): Map[String, Any] = Map.empty

  def rowStyle[T <: Cell](row: Row[T], hHeader: Boolean, rowIndex: Int): Map[String, Any] = Map.empty

  def titleStyle(title: Title): Map[String, Any] = Map.empty

  def tableStyle(table: Table): Map[String, Any] = Map.empty

  def noteStyle(note: Note, noteIndex: Int): Map[String, Any] = Map.empty

  def footnoteStyle(footnote: Footnote): Map[String, Any] = Map.empty

  val defaultTableStyler: TableStyler = new NoopTableStyler() {

    override def tableStyle(table: Table): Map[String, Any] = ListMap(
      "border" -> "1px solid #d3cadd"
    )

    override def cellStyle(cell: Cell, hHeader: Boolean, vHeader: Boolean): Map[String, Any] = {
      if (hHeader || vHeader) {
        ListMap(
          "text-align" -> "center",
          "vertical-align" -> "center",
          "font-weight" -> "bold"
        )
      } else {
        ListMap(
          "vertical-align" -> "center"
        )
      }
    }

    override def titleStyle(title: Title): Map[String, Any] = ListMap(
      "text-align" -> "center",
      "font-weight" -> "bold",
      "color" -> "#fff",
      "background" -> "#8066a0"
    )
  }

  def render(tableAdapters: Seq[TableAdapter], os: OutputStream): Unit = {
    val workbook = new XWorkbook(os)

    tableAdapters.zipWithIndex.foreach { case (tableAdapter, index) =>
      import tableAdapter._
      val ordinal = index + 1
      val sheetName = table.title.map(_.value).getOrElse(s"SheetNameGen$ordinal")
      implicit val tbl: Table = table
      implicit val worksheet: XWorksheet = workbook.addWorksheet(sheetName, cols, freeze)
      implicit val mergedTableStyler: TableStyler = defaultTableStyler + tableStyler.getOrElse(noopTableStyler)

      val tableSpan = table.columnCount.max(1)
      table.title.map { title =>
        val mergedStyle = mergedTableStyler.titleStyle(title) merge title.style
        Row(Cell(title.value, colspan = tableSpan).withStyle(mergedStyle))
      }.foreach(addRow(_, hHeaderRow = true))

      table.hHeaderRows.map(row =>
        Row(row.cells.map(CellWrapper(_, hHeader = true)): _*)
      ).foreach(addRow(_, hHeaderRow = true))

      table.body.map { case (vHeaderRow, dataRow) =>
        Row(
          vHeaderRow.cells.map { cell =>
            CellWrapper(cell, vHeader = true).withNewStyle(vHeaderRow.style.toListMap ++ cell.style)
          } ++: dataRow.cells: _*)
      }.foreach(addRow(_))

      table.footnote.foreach { footnote =>
        footnote.notes.zipWithIndex.map {
          case (note, noteIndex) =>
            val ordinal = if(footnote.notes.size > 1) noteIndex + 1 + ". " else ""
            val mergedStyle = mergedTableStyler.footnoteStyle(footnote).
              merge(mergedTableStyler.noteStyle(note, noteIndex)).
              merge(note.style)
            Row(Cell(ordinal + note.value, colspan = tableSpan).withStyle(mergedStyle))
        }.foreach(addRow(_))
      }
    }

    workbook.finish()
  }

  def addRow[T <: Cell](row: Row[T], hHeaderRow: Boolean = false)
                       (implicit table: Table, worksheet: XWorksheet, tableStyler: TableStyler): Unit = {

    worksheet.addRow(row.style.getDouble("height")) { xRow =>
      row.cells.foreach { cell =>
        val (realCell, hHeader, vHeader) = cell match {
          case CellWrapper(x, y, z) => (x, y, z)
          case x => (x, false, false)
        }

        val style = Seq(
          tableStyler.tableStyle(table),
          tableStyler.rowStyle(row, hHeader = hHeaderRow, rowIndex = 0),
          tableStyler.cellStyle(realCell, hHeader = hHeader, vHeader = vHeader),
          table.style, row.style, realCell.style
        ).reduce(_ merge _)

        val hAlign = style.getString("text-align")
        val vAlign = style.getString("vertical-align").map(_.cas("middle", "center"))
        val bold = Stream("font-weight", "font").map(style.getString(_).exists(_.contains("bold"))).exists(identity)
        val italic = Stream("font-style", "font").map(style.getString(_).exists(_.contains("italic"))).exists(identity)
        val fontSize = Stream("font-size", "font").flatMap(style.getString(_).flatMap(parseFontSize)).headOption
        val rgb = style.getString("color").flatMap(toRGB)
        val fill = Stream("background-color", "background").flatMap(style.getString(_).flatMap(toRGB)).headOption

        val xStyle = XStyle(hAlign = hAlign, vAlign = vAlign, fillColor = fill,
          fontSize = fontSize, fontColor = rgb, fontBold = bold, fontItalic = italic,
          borderTop = edge("top", style), borderRight = edge("right", style),
          borderBottom = edge("bottom", style), borderLeft = edge("left", style)
        )

        realCell match {
          case n@NumericCell(Some(_), numberFormatter, colspan, rowspan, _) =>
            val cellStyle = xStyle.copy(formatCode = numberFormatter.excelFormat)
            xRow.addCell(numberFormatter.prepare(n.doubleValue.get), colspan = colspan, rowspan = rowspan, cellStyle = cellStyle)
          case NumericCell(None, _, colspan, rowspan, _) =>
            xRow.addCell("--", colspan = colspan, rowspan = rowspan, cellStyle = xStyle)
          case TextCell(value: String, colspan, rowspan, _) =>
            xRow.addCell(value, colspan = colspan, rowspan = rowspan, cellStyle = xStyle)
          case DateCell(value: Date, dateFormatter, colspan, rowspan, _) =>
            val cellStyle = xStyle.copy(formatCode = dateFormatter.excelFormat)
            xRow.addCell(value, colspan = colspan, rowspan = rowspan, cellStyle = cellStyle)
          case _ => throw CellNotSupportedException()
        }
      }
    }
  }

  def toRGB(color: String): Option[String] = color match {
    case RGB(rgb) => Some(rgb)
    case ShortRGB(rgb) => Some(rgb.map(c => s"$c$c").mkString(""))
    case _ => color.toLowerCase.split(" ").filter(_.nonEmpty).toStream.flatMap {
      HtmlColorName.map.get(_).map(_.substring(1))
    }.headOption
  }

  def parseFontSize(sizeStr: String): Option[Int] = sizeStr match {
    case DIGITS(size) => size.toInt.some
    case PIXELS(size) => size.toInt.some
    case _ => throw FontSizeNotSupportedException(sizeStr)
  }

  def edge(edge: String, style: Map[String, Any]): Option[XEdge] = {
    borderWidth(edge, style).flatMap { bWidth =>
      borderStyle(edge, bWidth, style).flatMap { bStyle =>
        borderColor(edge, style).flatMap { bColor =>
          Some(XEdge(style = bStyle, rgb = bColor))
        }
      }
    }
  }

  def edgeIndexes(edge: String): Seq[Int] = {
    val ordinals = edge match {
      case "top" => Seq(1)
      case "right" => Seq(1, 2)
      case "bottom" => Seq(1, 3)
      case "left" => Seq(1, 2, 4)
    }
    ordinals.map(_ - 1)
  }

  def borderWidth(edge: String, style: Map[String, Any]): Option[Int] = {
    val stream: Stream[Option[String]] =
      style.getString(s"border-$edge-width") #::
        style.getString("border-width").flatMap(_.split(" ").filter(_.nonEmpty).toSeq.lastPossible(edgeIndexes(edge): _*)) #::
        style.getString(s"border-$edge") #::
        style.getString("border") #::
        Stream.empty
    stream.flatten.flatMap(parseBorderWidth).headOption
  }

  def borderStyle(edge: String, width: Int, style: Map[String, Any]): Option[String] = {
    val stream: Stream[Option[String]] =
      style.getString(s"border-$edge-style") #::
        style.getString("border-style").flatMap(_.split(" ").filter(_.nonEmpty).toSeq.lastPossible(edgeIndexes(edge): _*)) #::
        style.getString(s"border-$edge").flatMap(_.split(" ").filter(_.nonEmpty).find(HtmlBordersAdapter.set)) #::
        style.getString("border").flatMap(_.split(" ").filter(_.nonEmpty).find(HtmlBordersAdapter.set)) #::
        Stream.empty
    stream.flatten.flatMap(HtmlBordersAdapter.adapt(_, width)).headOption
  }

  def borderColor(edge: String, style: Map[String, Any]): Option[String] = {
    val stream: Stream[Option[String]] =
      style.getString(s"border-$edge-color") #::
        style.getString("border-color").flatMap(_.split(" ").filter(_.nonEmpty).toSeq.lastPossible(edgeIndexes(edge): _*)) #::
        style.getString(s"border-$edge") #::
        style.getString("border") #::
        Stream.empty
    stream.flatten.flatMap(toRGB).headOption
  }

  def parseBorderWidth(sizeStr: String): Option[Int] = sizeStr match {
    case PIXELS(size) => size.toInt.some
    case _ => None
  }

}

object HtmlBordersAdapter {

  val set: Set[String] = Set("none", "hidden", "dotted", "dashed", "solid", "double", "groove", "ridge", "inset", "outset")

  def adapt(htmlBorderStyle: String, width: Int): Option[String] = htmlBorderStyle match {
    case "none" | "hidden" => HIDDEN.some
    case "dotted" => DOTTED.some
    case "dashed" if width == 1 => DASHED.some
    case "dashed" => MEDIUM_DASHED.some
    case "double" => DOUBLE.some
    case "solid" | "groove" | "ridge" | "inset" | "outset" =>
      if (width == 1) THIN.some else MEDIUM.some
    case _ => None
  }
}
