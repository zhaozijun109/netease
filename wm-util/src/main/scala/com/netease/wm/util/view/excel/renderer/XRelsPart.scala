package com.netease.wm.util.view.excel.renderer

private[excel] object XRelsPart extends XPart {
  def path: String = "_rels/.rels"

  def content: Seq[String] =
    """<?xml version="1.0" encoding="UTF-8"?>
      |<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
      |  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
      |</Relationships>""".stripMargin :: Nil
}