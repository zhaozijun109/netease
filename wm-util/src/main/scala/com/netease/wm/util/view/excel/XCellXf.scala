package com.netease.wm.util.view.excel

case class XNumberFormat(id: Int, formatCode: String)

// rgb: hex color such as 00FF00
case class XFill(id: Int, rgb: String) {
  require(rgb.toLowerCase.matches("[0-9a-f]{6}"))
}

// rgb: hex color such as 00FF00
case class XFontData(size: Option[Int] = None, rgb: Option[String] = None, bold: Boolean = false, italic: Boolean = false) {
  size.foreach(value => require(value > 0))
  rgb.foreach(value => require(value.toLowerCase.matches("[0-9a-f]{6}")))
}

case class XFont(id: Int, fontData: XFontData)

private[excel] case class XCellXfData(numFmtId: Int, alignment: Option[XAlignment], fontId: Int, fillId: Int, borderId: Int)

private[excel] case class XCellXf(id: Int, cellXfData: XCellXfData)

