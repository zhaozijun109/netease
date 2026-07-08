package com.netease.wm.util.view.excel

trait IEdge {

  def style: String

  def rgb: String
}

case class XEdge(style: String, rgb: String) extends IEdge {
  require(XBorderStyle.values(style))
  require(rgb.toLowerCase.matches("[0-9a-f]{6}"))
}

case class XDiagonal(style: String, rgb: String, up: Boolean, down: Boolean) extends IEdge {
  require(XBorderStyle.values(style))
  require(rgb.toLowerCase.matches("[0-9a-f]{6}"))
}

case class XBorderData(top: Option[XEdge], right: Option[XEdge], bottom: Option[XEdge], left: Option[XEdge], diagonal: Option[XDiagonal])

case class XBorder(id: Int, borderData: XBorderData)

object XBorderStyle {
  // 需要特殊处理
  private[excel] val HIDDEN = "__hidden__"

  val THIN = "thin"
  val HAIR = "hair"
  val DOTTED = "dotted"
  val DASH_DOT_DOT = "dashDotDot"
  val DASH_DOT = "dashDot"
  val DASHED = "dashed"
  val MEDIUM_DASH_DOT_DOT = "mediumDashDotDot"
  val MEDIUM_DASH_DOT = "mediumDashDot"
  val SLANT_DASH_DOT = "slantDashDot"
  val MEDIUM_DASHED = "mediumDashed"
  val MEDIUM = "medium"
  val DOUBLE = "double"

  val values: Set[String] = Set(
    HIDDEN, THIN, HAIR, DOTTED, DASH_DOT_DOT, DASH_DOT, DASHED,
    MEDIUM_DASH_DOT_DOT, MEDIUM_DASH_DOT, SLANT_DASH_DOT, MEDIUM_DASHED, MEDIUM, DOUBLE
  )
}
