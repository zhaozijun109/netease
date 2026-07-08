package com.netease.wm.util.view.excel
package renderer

import com.netease.wm.util.Implicits._
import com.netease.wm.util.StringUtil._
import com.netease.wm.util.view.excel.XBorderStyle._

private[excel] class XStylesPart(workbook: XWorkbook) extends XPart {

  import workbook._

  def path: String = "xl/styles.xml"

  def content: Seq[String] = {
    s"""<?xml version="1.0" encoding="utf-8"?>
       |<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
       |  <numFmts count="${numberFormatCache.size}">
       |    $numberFormatDecs
       |  </numFmts>
       |  <fonts count="${fontCache.size + 2}">
       |    <font>
       |      <sz val="11.0"/>
       |      <color indexed="8"/>
       |      <name val="Calibri"/>
       |      <family val="2"/>
       |      <scheme val="minor"/>
       |    </font>
       |    <font>
       |      <name val="Arial"/>
       |      <sz val="10.0"/>
       |    </font>
       |    $fontDecs
       |  </fonts>
       |  <fills count="${fillCache.size + 2}">
       |    <fill>
       |      <patternFill patternType="none"/>
       |    </fill>
       |    <fill>
       |      <patternFill patternType="darkGray"/>
       |    </fill>
       |    $fillDecs
       |  </fills>
       |  <borders count="${borderCache.size + 1}">
       |    <border>
       |      <left/>
       |      <right/>
       |      <top/>
       |      <bottom/>
       |      <diagonal/>
       |    </border>
       |    $borderDecs
       |  </borders>
       |  <cellStyleXfs count="1">
       |    <xf numFmtId="0" fontId="0" fillId="0" borderId="0"/>
       |  </cellStyleXfs>
       |  <cellXfs count="${cellXfCache.size}">
       |    $cellXfDecs
       |  </cellXfs>
       |</styleSheet>""".stripMargin :: Nil
  }

  private[this] def numberFormatDecs: String = numberFormatCache.values.map { numberFormat =>
    import numberFormat._
    s"""<numFmt numFmtId="$id" formatCode="${formatCode.xmlSafe}"/>"""
  }.mkString("\n")

  private[this] def fontDecs: String = fontCache.values.map { font =>
    import font.fontData._
    val clr = rgb.map(rgb => s"""<color rgb="ff$rgb"/>""").getOrElse("")

    s"""<font>
       |  ${el(bold, "<b/>")}
       |  ${el(italic, "<i/>")}
       |  <sz val="${size.getOrElse(11)}"/>
       |  $clr
       |  <name val="Arial"/>
       |</font>
         """.stripMargin
  }.mkString("\n")

  private[this] def fillDecs: String = fillCache.values.map { fill =>
    import fill._
    s"""<fill>
       |  <patternFill patternType="solid">
       |    <fgColor rgb="ff$rgb"/>
       |  </patternFill>
       |</fill>
         """.stripMargin
  }.mkString("\n")

  private[this] def edgeDec(el: String, edge: Option[IEdge]): String = edge match {
    case Some(e) if e.style != HIDDEN =>
      s"""<$el style="${e.style}">
         |  <color rgb="ff${e.rgb}"/>
         |</$el>""".stripMargin
    case _ => s"<$el/>"
  }

  private[this] def borderDecs: String = borderCache.values.map { border =>
    import border.borderData._
    val diagonalUp = diagonal.flatMap(flag => flag.up.option(1))
    val diagonalDown = diagonal.flatMap(flag => flag.down.option(1))
    s"""<border ${attr("diagonalUp", diagonalUp)} ${attr("diagonalDown", diagonalDown)}>
       |  ${edgeDec("left", left)}
       |  ${edgeDec("right", right)}
       |  ${edgeDec("top", top)}
       |  ${edgeDec("bottom", bottom)}
       |  ${edgeDec("diagonal", diagonal)}
       |</border>""".stripMargin
  }.mkString("\n")

  private[this] def cellXfDecs: String = cellXfCache.values.map { cellXf =>
    import cellXf.cellXfData._
    alignment match {
      case Some(XAlignment(hAlign, vAlign, wrapText, indent)) =>
        s"""<xf numFmtId="$numFmtId" fontId="$fontId" fillId="$fillId" borderId="$borderId" xfId="0" applyFont="true">
           |  <alignment ${attr("horizontal", hAlign)} ${attr("vertical", vAlign)} wrapText="$wrapText" ${attr("indent", indent)}/>
           |</xf>
         """.stripMargin
      case _ =>
        s"""<xf numFmtId="$numFmtId" fontId="$fontId" fillId="$fillId" borderId="$borderId" xfId="0" applyFont="true" />"""
    }
  }.mkString("\n")
}

private[excel] object XStylesPart {
  def apply(workbook: XWorkbook): XStylesPart = new XStylesPart(workbook)
}