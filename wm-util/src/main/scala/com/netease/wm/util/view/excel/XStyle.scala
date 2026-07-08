package com.netease.wm.util.view.excel

import com.netease.wm.util.Implicits._

case class XStyle(formatCode: Option[String] = None,
                  hAlign: Option[String] = None, vAlign: Option[String] = None, wrapText: Boolean = true, indent: Option[Int] = None,
                  fillColor: Option[String] = None,
                  fontSize: Option[Int] = None, fontColor: Option[String] = None, fontBold: Boolean = false, fontItalic: Boolean = false,
                  borderTop: Option[XEdge] = None, borderRight: Option[XEdge] = None,
                  borderBottom: Option[XEdge] = None, borderLeft: Option[XEdge] = None,
                  diagonal: Option[XDiagonal] = None) {

  def alignment: Option[XAlignment] = {
    if (hAlign.isDefined || vAlign.isDefined || !wrapText || indent.isDefined) {
      XAlignment(hAlign = hAlign, vAlign = vAlign, wrapText = wrapText, indent = indent).some
    } else {
      None
    }
  }

  def fontData: Option[XFontData] = {
    if (fontSize.isDefined || fontColor.isDefined || fontBold || fontItalic) {
      XFontData(size = fontSize, rgb = fontColor, bold = fontBold, italic = fontItalic).some
    } else {
      None
    }
  }

  def borderData: Option[XBorderData] = {
    if (borderTop.isDefined || borderRight.isDefined || borderBottom.isDefined || borderLeft.isDefined || diagonal.isDefined) {
      XBorderData(top = borderTop, right = borderRight, bottom = borderBottom, left = borderLeft, diagonal = diagonal).some
    } else {
      None
    }
  }

  def withBorderStyle(edge: XEdge): XStyle = {
    val e = edge.some
    copy(borderTop = e, borderRight = e, borderBottom = e, borderLeft = e)
  }
}

object XStyle {
  def empty: XStyle = XStyle()
}