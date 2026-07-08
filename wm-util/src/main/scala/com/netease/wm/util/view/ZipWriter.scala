package com.netease.wm.util.view

import java.io.{OutputStreamWriter, PrintWriter}
import java.util.zip.{ZipEntry, ZipOutputStream}

class ZipWriter(zos: ZipOutputStream) extends PrintWriter(new OutputStreamWriter(zos, "UTF-8")) {

  def putNextEntry(path: String): Unit = zos.putNextEntry(new ZipEntry(path))

  def closeEntry(): Unit = {
    flush()
    zos.closeEntry()
  }

  def addEntry(path: String, content: Seq[String]): Unit = {
    putNextEntry(path)
    content.foreach(print)
    closeEntry()
  }
}

object ZipWriter {
  def apply(zos: ZipOutputStream): ZipWriter = new ZipWriter(zos)
}