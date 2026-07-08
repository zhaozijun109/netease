package com.netease.lofter.common

import java.io.{FileNotFoundException, InputStreamReader}
import java.net.URI
import java.util.Calendar

import org.apache.commons.io.IOUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.joda.time.DateTime

import scala.collection.JavaConverters._

object HdfsUtil {

  val CONF = new Configuration()
  val fs: FileSystem = FileSystem.get(new URI("hdfs://gy-cluster8/"),CONF)

  def readLinesFromHdfs(fileName: String): List[String] = {
    var reader: InputStreamReader = null
    try{
      val finput = fs.open(new Path(fileName))
      reader = new InputStreamReader(finput, "UTF-8")
      IOUtils.readLines(reader).asScala.toList
    } finally {
      IOUtils.closeQuietly(reader)
    }
  }

  def isHdfsPathEmpty(path: String): Boolean = {
    var totalSize = 0L
    val srcPath = new Path(path)
    try {
      totalSize = fs.getContentSummary(srcPath).getLength
      println(s"the length of $path is $totalSize")
      if(totalSize > 0L) false
      else true
    } catch {
      case e: FileNotFoundException  =>
        println(e.getMessage)
        true
    }
  }

  def isWeekEnd(date: String): Boolean = {
    val cal = Calendar.getInstance()
    cal.setTime(DateTime.parse(date).toDate)
    if (Calendar.SUNDAY == cal.get(Calendar.DAY_OF_WEEK) | Calendar.SATURDAY == cal.get(Calendar.DAY_OF_WEEK)) {
      true
    } else false

  }

}
