package com.netease.wm.util.view.excel.renderer

import java.io.{FilterOutputStream, OutputStream}
import java.util.zip.ZipOutputStream

import com.netease.wm.util.view.ZipWriter

private[excel] class XWorkbookWriter(os: OutputStream) {

  private[this] val writer: ZipWriter = ZipWriter(new ZipOutputStream(new NoClosingOutputStream(os)))

  class NoClosingOutputStream(os: OutputStream) extends FilterOutputStream(os) {
    override def close(): Unit = super.flush()
  }

  def startPart(path: String): Unit = writer.putNextEntry(path)

  def writePart(part: XPart): Unit = part.content.foreach(writer.print)

  def writeSegment(segment: String): Unit = writer.print(segment)

  def endPart(): Unit = writer.closeEntry()

  def addPart(part: XPart): Unit = {
    startPart(part.path)
    writePart(part)
    endPart()
  }

  def close(): Unit = {
    writer.flush()
    writer.close()
  }
}