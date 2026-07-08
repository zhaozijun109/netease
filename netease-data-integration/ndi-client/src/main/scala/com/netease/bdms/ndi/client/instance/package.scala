package com.netease.bdms.ndi.client

import com.netease.bdms.ndi.client.property.{Properties, Property}
import org.json4s.DefaultFormats
import org.json4s.JsonAST._

package object instance {
  def combineKey(args: String*): String = args.mkString(".")

  def readerPrefix(reader: String) = s"spark.transmit.reader.${reader.toLowerCase}"

  def writerPrefix(writer: String) = s"spark.transmit.writer.${writer.toLowerCase()}"

  def handlerPrefix(handler: String) = s"spark.transmit.handler.${handler.toLowerCase}"

  def typeToPrefix(tpe: String): String = tpe match {
    case "ddbqs" =>
      "ddb.qs"
    case "ddb" =>
      "ddb.dbi"
    case other =>
      other
  }

  def addSparkConf(properties: Properties, key: String, value: String): Properties = {
    properties.put(Property.SPARK_CONF_PREFIX + key, value)
  }

  def addSparkConfIfExist(json: JValue, jsonKey: String, properties: Properties, propertiesKey: String): Unit = {
    implicit val formats: DefaultFormats.type = DefaultFormats
    val condition = (json \ jsonKey).extractOpt[String]
    if (condition.isDefined && !condition.get.trim.isEmpty) {
      addSparkConf(properties, propertiesKey, condition.get)
    }
  }

  def addSparkArgument(properties: Properties, key: String, value: String): Unit = {
    properties.put(Property.SPARK_ARGUMENT_PREFIX + key, value)
  }

  def addAdvancedSparkConf(conf: List[Map[String, String]], properties: Properties, prefix: String): Unit = {
    conf.foreach{ property =>
      val key = property("key")
      val value = property("value")
      if (!value.isEmpty && !key.isEmpty) {
        addSparkConf(properties, combineKey(prefix, key), value)
      }
    }
  }
}
