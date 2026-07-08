package com.netease.wm.util.view.excel

import java.util.Date

import scala.collection.mutable.ListBuffer

class XRow private[excel](worksheet: XWorksheet, val rowIndex: Int, val height: Option[Double] = None) {
  private[excel] val cells: ListBuffer[XCellDef] = ListBuffer.empty

  def addCell[T: XCellType](cell: XCell[T]): Unit = {
    import cell._
    addCell(value = value, colspan = colspan, rowspan = rowspan, cellStyle = cellStyle)
  }

  def addCell[T: XCellType](value: T, colspan: Int = 1, rowspan: Int = 1, cellStyle: XStyle = XStyle.empty): Unit = {
    val cellType = implicitly[XCellType[T]]
    val v = value match {
      case date: Date => (date.getTime / 1000.0 + 8 * 3600) / 86400 + 70 * 365 + 19 + ""
      case _ => value.toString
    }

    import cellStyle._
    val cellXf = worksheet.workbook.getCellXf(formatCode, alignment, fontData, fillColor, border = borderData)

    val cellIndex = worksheet.matrix.getNextCellIndex
    worksheet.matrix.addCell(colspan, rowspan, cellXf)
    cells += XCellDef(
      value = v,
      rowIndex = worksheet.matrix.getCurrentRowIndex,
      cellIndex = cellIndex,
      cellType = cellType.value,
      cellXf = cellXf
    )
  }
}