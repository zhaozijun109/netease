package com.netease.wm.util.view.excel
package renderer

private[excel] class XContentTypesPart(workbook: XWorkbook) extends XPart {

  def path: String = "[Content_Types].xml"

  def content: Seq[String] = {
    val sheetDecs = (1 to workbook.worksheets.size).map { i =>
      s"""<Override PartName="/xl/worksheets/sheet$i.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>"""
    }.mkString("\n")

    s"""<?xml version="1.0" encoding="UTF-8"?>
       |<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
       |  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
       |  <Default Extension="xml" ContentType="application/xml"/>
       |  <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
       |  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
       |  $sheetDecs
       |</Types>""".stripMargin :: Nil
  }
}

private[excel] object XContentTypesPart {
  def apply(workbook: XWorkbook): XContentTypesPart = new XContentTypesPart(workbook)
}