package com.netease.wm.util.view.table
package renderer

import com.netease.wm.util.Implicits._
import com.netease.wm.util.view.Styling._
import com.netease.wm.util.view.table.styler.TableStyler._
import com.netease.wm.util.view.table.styler._

case class TableHtmlRenderer(table: Table, styler: Option[TableStyler]) {

  private def formatText(text: String): String = text.replace("\n", "<br />")

  private def thtd(cell: Cell): String = {
    val (realCell, hHeader, vHeader) = cell match {
      case CellWrapper(x, y, z) => (x, y, z)
      case x => (x, false, false)
    }

    val mergedStyle = defaultTableStyler.cellStyle(realCell, hHeader, vHeader).toListMap ++
      styler.getOrElse(noopTableStyler).cellStyle(realCell, hHeader, vHeader) ++
      cell.style

    val el = if (hHeader || vHeader) "th" else "td"
    import cell._
    s"""<$el style="${css(mergedStyle)}" colspan="$colspan" rowspan="$rowspan">
       |  ${formatText(text)}
       |</$el>""".stripMargin
  }

  private def tr[T <: Cell](row: Row[T], hHeader: Boolean, rowIndex: Int): String = {
    val mergedStyle = defaultTableStyler.rowStyle(row, hHeader, rowIndex).toListMap ++
      styler.getOrElse(noopTableStyler).rowStyle(row, hHeader, rowIndex) ++
      row.style

    import row._
    s"""<tr style="${css(mergedStyle)}">
       |  ${cells.map(thtd).mkString("\n")}
       |</tr>""".stripMargin
  }

  private def caption: String = table.title match {
    case Some(title@Title(text, style)) =>
      val mergedStyle = defaultTableStyler.titleStyle(title).toListMap ++
        styler.getOrElse(noopTableStyler).titleStyle(title) ++
        style

      s"""<caption style="${css(mergedStyle)}">
         |  ${formatText(text)}
         |</caption>""".stripMargin
    case _ => ""
  }

  private def thead: String = {
    val rows = table.hHeaderRows.map { row =>
      Row(row.cells.map(CellWrapper(_, hHeader = true)): _*)
    }
    val rowDecs = rows.zipWithIndex.map {
      case (row, rowIndex) => tr(row, hHeader = true, rowIndex = rowIndex)
    }.mkString("\n")

    s"""<thead>
       |  $rowDecs
       |</thread>""".stripMargin
  }

  private def tbody: String = {

    val bodyDec = table.body.zipWithIndex.map {
      case ((vHeaderRow, dataRow), rowIndex) =>
        val vHeaderCells = vHeaderRow.cells.map { cell =>
          CellWrapper(cell.withNewStyle(vHeaderRow.style.toListMap ++ cell.style), vHeader = true)
        }
        tr(Row(vHeaderCells ++ dataRow.cells: _*).withStyle(dataRow.style), hHeader = false, rowIndex = rowIndex)
    }.mkString("\n")

    s"""<tbody>
       |  $bodyDec
       |</tbody>""".stripMargin
  }

  private def noteDec(note: Note, noteIndex: Int, showOrdinal: Boolean): String = {
    val mergedStyle = defaultTableStyler.noteStyle(note, noteIndex).toListMap ++
      styler.getOrElse(noopTableStyler).noteStyle(note, noteIndex) ++
      note.style

    val ordinal = if (showOrdinal) noteIndex + 1 + ". " else ""
    s"""<div style="${css(mergedStyle)}">$ordinal${formatText(note.value)}</div>""".stripMargin
  }

  private def tfoot: String = {
    table.footnote match {
      case Some(footnote) =>
        val noteDecs = footnote.notes.zipWithIndex.map { case (note, noteIndex) =>
          noteDec(note, noteIndex = noteIndex, showOrdinal = footnote.notes.size > 1)
        }.mkString("\n")

        val mergedStyle = defaultTableStyler.footnoteStyle(footnote).toListMap ++
          styler.getOrElse(noopTableStyler).footnoteStyle(footnote) ++
          footnote.style
        s"""<tfoot>
           |  <td colspan="99" style="${css(mergedStyle)}">
           |    <div style="position: relative; float: left; width: 2em; margin-right: -2em;">注：</div>
           |    <div style="float: right; width: 100%;">
           |      <div style="margin-left: 2em;">
           |        $noteDecs
           |      </div>
           |    </div>
           |    <div style="clear: both; width: 0px; height: 0px;"></div>
           |  </td>
           |</tfoot>""".stripMargin
      case _ => ""
    }
  }

  def html: String = {
    val mergedStyle = defaultTableStyler.tableStyle(table).toListMap ++
      styler.getOrElse(noopTableStyler).tableStyle(table) ++
      table.style

    s"""<table style="${css(mergedStyle)};">
       |  $caption
       |  $thead
       |  $tbody
       |  $tfoot
       |</table>""".stripMargin
  }
}