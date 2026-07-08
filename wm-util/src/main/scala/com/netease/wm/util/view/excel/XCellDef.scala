package com.netease.wm.util.view.excel

import java.util.Date

import scala.annotation.implicitNotFound

@implicitNotFound("Invalid cell value type '${T}'. Supported value types are: numeric types, string and java.util.Date.")
case class XCellType[T] private(value: String)

object XCellType {
  // 需要特殊处理
  private[excel] val UNIT_CELL_TYPE: XCellType[Unit] = XCellType("_unit_")

  implicit val byteCellType: XCellType[Byte] = XCellType("n")
  implicit val shortCellType: XCellType[Short] = XCellType("n")
  implicit val intCellType: XCellType[Int] = XCellType("n")
  implicit val longCellType: XCellType[Long] = XCellType("n")
  implicit val floatCellType: XCellType[Float] = XCellType("n")
  implicit val doubleCellType: XCellType[Double] = XCellType("n")
  implicit val dateCellType: XCellType[Date] = XCellType("n")
  implicit val stringCellType: XCellType[String] = XCellType("str")
}

case class XCell[T : XCellType](value: T, colspan: Int = 1, rowspan: Int = 1, cellStyle: XStyle = XStyle.empty)

private[excel] case class XCellDef(value: String, rowIndex: Int, cellIndex: Int, cellType: String, cellXf: XCellXf)
