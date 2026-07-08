package com.netease.wm.util.view.excel
package renderer

import XColumnLabelUtil._
import com.netease.wm.util.StringUtil._
import com.netease.wm.util.view.excel.XCellType.UNIT_CELL_TYPE

private[excel] class XWorksheetPart(worksheet: XWorksheet) {

  import worksheet._

  def path: String = s"xl/worksheets/sheet${index + 1}.xml"

  def head: String =
    s"""<?xml version="1.0" encoding="utf-8"?>
       |<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
       |  ${freezeDec(freeze)}
       |  <sheetFormatPr defaultRowHeight="15.0"/>
       |  ${colDecs(columns)}
       |  <sheetData>""".stripMargin

  def last: String =
    s"""|  </sheetData>
        |  ${mergeCellDecs(matrix.mergedRegions)}
        |</worksheet>""".stripMargin

  def rowDec(row: XRow): String = {
    val subCells = matrix.getSubordinateCells(row.rowIndex)
    val cellDecs = (row.cells ++: subCells).sortBy(_.cellIndex).map { cell =>
      import cell._
      if (cell.cellType == UNIT_CELL_TYPE.value) {
        s"""<c r="${coordinate(rowIndex, cellIndex)}" s="${cellXf.id}" />"""
      } else {
        s"""<c r="${coordinate(rowIndex, cellIndex)}" s="${cellXf.id}" t="$cellType">
           |  <v>${value.xmlSafe}</v>
           |</c>""".stripMargin
      }
    }.mkString("\n")

    val heightDec = row.height.map(height => s"""ht="$height" customHeight="true"""").getOrElse("")

    s"""<row r="${row.rowIndex + 1}" $heightDec>
       |  $cellDecs
       |</row>""".stripMargin
  }

  def subordinateRowDec(rowIndex: Int): String = {
    val subCells = matrix.getSubordinateCells(rowIndex)
    require(subCells.nonEmpty)
    val cellDecs = subCells.map { cell =>
      s"""<c r="${coordinate(rowIndex, cell.cellIndex)}" s="${cell.cellXf.id}" />"""
    }.mkString("\n")

    s"""<row r="${rowIndex + 1}">
       |  $cellDecs
       |</row>""".stripMargin
  }

  private[this] def freezeDec(freeze: Option[XFreeze]): String = freeze match {
    case Some(XFreeze(rowCount, colCount)) =>
      s"""<sheetViews>
         |  <sheetView workbookViewId="0">
         |    <pane xSplit="$colCount" ySplit="$rowCount" state="frozen" topLeftCell="${coordinate(rowCount, colCount)}"/>
         |  </sheetView>
         |</sheetViews>
       """.stripMargin
    case _ => ""
  }

  private[this] def colDecs(columns: Seq[XColumn]): String = columns match {
    case Nil => ""
    case _ =>
      columns.map {
        case XColumn(minColIndex, maxColIndex, width) =>
          s"""<col min="${minColIndex + 1}" max="${maxColIndex + 1}" width="$width" customWidth="true"/>"""
      }.mkString("<cols>", "\n", "</cols>")
  }

  private[this] def mergeCellDecs(mergedRegions: Seq[XMergedRegion]): String = mergedRegions match {
    case Nil => ""
    case _ =>
      mergedRegions.map { region =>
        val topLeft = coordinate(region.rowIndexFrom, region.cellIndexFrom)
        val bottomRight = coordinate(region.rowIndexTo, region.cellIndexTo)
        s"""<mergeCell ref="$topLeft:$bottomRight"/>"""
      }.mkString("<mergeCells>", "\n", "</mergeCells>")
  }
}