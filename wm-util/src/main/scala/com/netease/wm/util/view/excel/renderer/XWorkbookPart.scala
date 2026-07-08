package com.netease.wm.util.view.excel
package renderer

import com.netease.wm.util.StringUtil._

private[excel] class XWorkbookPart(workbook: XWorkbook) extends XPart {
  def path: String = "xl/workbook.xml"

  def content: Seq[String] = {
    val sheetDecs = workbook.worksheets.values.map { worksheet =>
      val ordinal = worksheet.index + 1
      s"""<sheet name="${worksheet.fixedName.xmlSafe}" r:id="rId$ordinal" sheetId="$ordinal"/>"""
    }.mkString("\n")

    s"""<?xml version="1.0" encoding="utf-8"?>
       |<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
       |  <sheets>
       |    $sheetDecs
       |  </sheets>
       |</workbook>""".stripMargin :: Nil
  }
}

private[excel] object XWorkbookPart {
  def apply(workbook: XWorkbook): XWorkbookPart = new XWorkbookPart(workbook)
}