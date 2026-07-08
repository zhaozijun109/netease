package com.netease.wm.util.view.excel

import java.io.OutputStream

import com.netease.wm.util.view.excel.renderer._

import scala.collection.mutable

private[excel] class IdGen(start: Int) {

  private[this] var nextId: Int = start

  def gen: Int = {
    val next = nextId
    nextId += 1
    next
  }
}

class XWorkbook(os: OutputStream) {

  private[excel] val writer: XWorkbookWriter = new XWorkbookWriter(os)

  private[excel] val worksheets: mutable.LinkedHashMap[String, XWorksheet] = mutable.LinkedHashMap.empty

  private[excel] val numberFormatCache: mutable.LinkedHashMap[String, XNumberFormat] = mutable.LinkedHashMap.empty
  private[this] val nextNumFmtId: IdGen = new IdGen(165)

  private[excel] val fontCache: mutable.LinkedHashMap[XFontData, XFont] = mutable.LinkedHashMap.empty
  private[this] val nextFontId: IdGen = new IdGen(2)

  private[excel] val fillCache: mutable.LinkedHashMap[String, XFill] = mutable.LinkedHashMap.empty
  private[this] val nextFillId: IdGen = new IdGen(2)

  private[excel] val borderCache: mutable.LinkedHashMap[XBorderData, XBorder] = mutable.LinkedHashMap.empty
  private[this] val nextBorderId: IdGen = new IdGen(1)

  private[excel] val cellXfCache: mutable.LinkedHashMap[XCellXfData, XCellXf] = mutable.LinkedHashMap.empty

  val DEFAULT_CELL_XF: XCellXf = getCellXf(formatCode = None, alignment = None)

  private[this] var sheetOpened: Boolean = false

  private[this] def getOrCreate[K, V](key: K, cache: mutable.LinkedHashMap[K, V], idGen: IdGen, creator: Int => V): V = {
    cache.get(key) match {
      case Some(value) => value
      case _ =>
        val value = creator(idGen.gen)
        cache += key -> value
        value
    }
  }

  private[this] def getNumberFormat(formatCode: String): XNumberFormat =
    getOrCreate(formatCode, numberFormatCache, nextNumFmtId, XNumberFormat(_, formatCode))

  private[this] def getFont(fontData: XFontData): XFont =
    getOrCreate(fontData, fontCache, nextFontId, XFont(_, fontData))

  private[this] def getFill(rgb: String): XFill =
    getOrCreate(rgb, fillCache, nextFillId, XFill(_, rgb))

  private[this] def getBorder(borderData: XBorderData): XBorder =
    getOrCreate(borderData, borderCache, nextBorderId, XBorder(_, borderData))

  private[excel] def getCellXf(formatCode: Option[String] = None,
                               alignment: Option[XAlignment] = None,
                               font: Option[XFontData] = None,
                               fill: Option[String] = None,
                               border: Option[XBorderData] = None): XCellXf = {
    val numFmtId = formatCode.map(format => getNumberFormat(format).id).getOrElse(0)
    val fontId = font.map(fontData => getFont(fontData).id).getOrElse(1)
    val fillId = fill.map(rgb => getFill(rgb).id).getOrElse(0)
    val borderId = border.map(borderData => getBorder(borderData).id).getOrElse(0)

    val data = XCellXfData(numFmtId, alignment, fontId = fontId, fillId = fillId, borderId = borderId)
    cellXfCache.get(data) match {
      case Some(cellXf) => cellXf
      case _ =>
        val cellXf = XCellXf(cellXfCache.size, data)
        cellXfCache += data -> cellXf
        cellXf
    }
  }

  def addWorksheet(name: String, columns: Seq[XColumn] = Nil, freeze: Option[XFreeze] = None): XWorksheet = {
    closeLastSheet()

    val worksheet = new XWorksheet(name = name, index = worksheets.size, workbook = this, columns = columns, freeze = freeze)
    require(!worksheets.contains(worksheet.fixedName))
    worksheets += worksheet.fixedName -> worksheet
    writer.startPart(worksheet.part.path)
    writer.writeSegment(worksheet.part.head)
    sheetOpened = true
    worksheet
  }

  def finish(): Unit = {
    closeLastSheet()

    writer.addPart(XRelsPart)
    writer.addPart(XContentTypesPart(this))
    writer.addPart(XStylesPart(this))
    writer.addPart(XWorkbookPart(this))
    writer.addPart(XWorkbookRelsPart(this))
    writer.close()
  }

  private[this] def closeLastSheet(): Unit = {
    if (sheetOpened) {
      val worksheet = worksheets.values.last
      worksheet.writeSubordinateRows()
      writer.writeSegment(worksheet.part.last)
      writer.endPart()
      sheetOpened = false
    }
  }
}