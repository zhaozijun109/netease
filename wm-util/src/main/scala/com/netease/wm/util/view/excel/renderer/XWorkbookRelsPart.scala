package com.netease.wm.util.view.excel
package renderer

private[excel] class XWorkbookRelsPart(workbook: XWorkbook) extends XPart {
  def path: String = "xl/_rels/workbook.xml.rels"

  def content: Seq[String] = {
    val sheetDecs = (1 to workbook.worksheets.size).map { i =>
      s"""<Relationship Id="rId$i" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet$i.xml"/>"""
    }.mkString("\n")

    s"""<?xml version="1.0" encoding="UTF-8"?>
       |<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
       |  <Relationship Id="rId0" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
       |  $sheetDecs
       |</Relationships>""".stripMargin :: Nil
  }
}

private[excel] object XWorkbookRelsPart {
  def apply(workbook: XWorkbook): XWorkbookRelsPart = new XWorkbookRelsPart(workbook)
}