package com.netease.wm.util.view.excel

import com.netease.wm.util.StringUtil._
import com.netease.wm.util.view.excel.renderer.XWorksheetPart

case class XColumn(minColIndex: Int, maxColIndex: Int, width: Double)

case class XFreeze(rowCount: Int, colCount: Int)

case class XWorksheetNotWritableException(m: String) extends RuntimeException(m)

class XWorksheet(name: String, val index: Int, val workbook: XWorkbook, val columns: Seq[XColumn] = Nil, val freeze: Option[XFreeze] = None) {

  lazy val fixedName: String = name.replaceAll("""[*\[\]\\:：?？/\x00-\x1F\x80-\x9F]""", "_").take(31)
  require(fixedName.isNotBlank)

  private[excel] val matrix: XMatrix = new XMatrix

  private[excel] val part: XWorksheetPart = new XWorksheetPart(this)

  def addRow(height: Option[Double] = None)(addCells: XRow => Any): Unit = {
    ensureWritable()

    val row = new XRow(this, matrix.addRow(), height)
    addCells(row)
    workbook.writer.writeSegment(part.rowDec(row))
  }

  private[excel] def ensureWritable(): Unit = {
    if (index < workbook.worksheets.size - 1) {
      throw XWorksheetNotWritableException("Trying to write to a worksheet that's already finished.")
    }
  }

  private[excel] def writeSubordinateRows(): Unit = {
    val rowIndexTo = matrix.mergedRegions.map(_.rowIndexTo).reduceOption(_ max _).getOrElse(0)
    (matrix.getCurrentRowIndex + 1 to rowIndexTo).foreach { rowIndex =>
      workbook.writer.writeSegment(part.subordinateRowDec(rowIndex))
    }
  }
}