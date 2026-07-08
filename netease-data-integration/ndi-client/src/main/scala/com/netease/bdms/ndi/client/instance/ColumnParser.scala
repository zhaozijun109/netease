package com.netease.bdms.ndi.client.instance

import com.netease.bdms.ndi.client.property.Properties
import com.netease.bdms.ndi.client.util.LogTrait
import org.apache.commons.lang.StringUtils
import org.json4s.JsonAST.JValue
import org.json4s._

import scala.collection.mutable.ArrayBuffer

object ColumnParser extends LogTrait {
  def parse(data: JValue, readerTypeValue: String, writerTypeValue: String): Unit = {
    val prefix = handlerPrefix("column")
    val properties = Properties.properties
    implicit val formats: DefaultFormats.type = DefaultFormats
    val outputColumns = new ArrayBuffer[String]()
    val inputColumns = new ArrayBuffer[String]()
    addSparkConf(properties, "spark.transmit.handler", "com.netease.music.da.transfer.common.handler.ColumnHandler")

    val map = (data \ "map").extract[List[Map[String, String]]]
    map.foreach { mapColumnData =>
      val newName = mapColumnData("newName")
      val oldName = mapColumnData("oldName")
      if (StringUtils.isNotEmpty(oldName) && StringUtils.isNotEmpty(newName)) {
        outputColumns += newName
        inputColumns += oldName
        addSparkConf(properties, combineKey(prefix, "outputColumns", newName, "inputColumn"), oldName)
      }
    }
    addSparkConf(properties, combineKey(prefix, "outputColumns"), outputColumns.mkString(","))
    addSparkConf(
      properties,
      combineKey(readerPrefix(typeToPrefix(readerTypeValue)), "columns"),
      inputColumns.mkString(","))

  }
}
