package com.netease.wm.util.view.excel

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.{BitSet => MBitSet, ArrayBuffer => MArrayBuffer}
import com.netease.wm.util.view.excel.XCellType._

private[excel] case class XMergedRegion(cellIndexFrom: Int, cellIndexTo: Int, rowIndexFrom: Int, rowIndexTo: Int, cellXf: XCellXf)

private[excel] class XMatrix {

  private[this] var currentRowIndex: Int = -1

  private[this] var nextCellIndex: Int = 0

  private[this] var matrix: MArrayBuffer[MBitSet] = MArrayBuffer.fill(100)(MBitSet.empty)

  private[excel] val mergedRegions: ListBuffer[XMergedRegion] = ListBuffer.empty

  def addRow(): Int = {
    currentRowIndex += 1
    nextCellIndex = 0
    currentRowIndex
  }

  def addCell(colspan: Int, rowspan: Int, cellXf: XCellXf): Unit = {
    addCell(getNextCellIndex, colspan, rowspan, cellXf)
  }

  def nonEmptyAt(rowIndex: Int, cellIndex: Int): Boolean = rowIndex < matrix.length && matrix(rowIndex)(cellIndex)

  def emptyAt(rowIndex: Int, cellIndex: Int): Boolean = !nonEmptyAt(rowIndex, cellIndex)

  def ensureSize(size: Int): Unit = {
    if (size > 100 * 10000) throw new IllegalArgumentException("Maximum row size exceeded.")

    if (size > matrix.length) {
      val buf = MArrayBuffer.fill(matrix.length.max(size - matrix.length))(MBitSet.empty)
      buf.insertAll(0, matrix)
      matrix = buf
    }
  }

  def addCell(cellIndex: Int, colspan: Int, rowspan: Int, cellXf: XCellXf): Unit = {
    ensureSize(currentRowIndex + rowspan)

    for {r <- currentRowIndex until currentRowIndex + rowspan
         c <- cellIndex until cellIndex + colspan} {
      require(emptyAt(r, c))
      matrix(r) += c
    }

    nextCellIndex = cellIndex + colspan

    if (colspan > 1 || rowspan > 1) {
      mergedRegions += XMergedRegion(
        cellIndexFrom = cellIndex,
        cellIndexTo = cellIndex + colspan - 1,
        rowIndexFrom = currentRowIndex,
        rowIndexTo = currentRowIndex + rowspan - 1,
        cellXf = cellXf
      )
    }
  }

  def getCurrentRowIndex: Int = currentRowIndex

  def getNextCellIndex: Int = Stream.from(nextCellIndex).find(emptyAt(currentRowIndex, _)).get

  def getSubordinateCells(rowIndex: Int): Seq[XCellDef] = {
    mergedRegions.flatMap {
      case region if region.rowIndexFrom <= rowIndex && region.rowIndexTo >= rowIndex =>
        (region.cellIndexFrom to region.cellIndexTo).collect {
          case cellIndex if (rowIndex, cellIndex) != (region.rowIndexFrom, region.cellIndexFrom) =>
            XCellDef(value = "", rowIndex = rowIndex, cellIndex = cellIndex, cellType = UNIT_CELL_TYPE.value, cellXf = region.cellXf)
        }
      case _ => Nil
    }
  }
}