package com.netease.bdms.ndi.client.instance
import com.netease.bdms.ndi.client.property.{Properties, Property}
import org.apache.commons.io.FileUtils
import org.json4s.{DefaultFormats, JValue}

import scala.collection.JavaConverters._

object DDBQSParser extends Parser {
  override def parseReader(readerData: JValue): Unit = {
    val prefix = readerPrefix("ddb.qs")
    implicit val formats: DefaultFormats.type = DefaultFormats
    val properties = Properties.properties
    addSparkConf(properties, "spark.transmit.reader", "com.netease.music.da.transfer.ddb.qs.reader.DDBQSReader")

    val datasource = (readerData \ "dataSources").extract[List[JValue]].head
    val url = (datasource \ "connectionInformation" \ "url").extract[String]
    addSparkConf(properties, combineKey(prefix, "url"), url)
    val user = (datasource \ "connectionInformation" \ "userName").extract[String]
    addSparkConf(properties, combineKey(prefix, "user"), user)
    val password = (datasource \ "connectionInformation" \ "password").extract[String]
    addSparkConf(properties, combineKey(prefix, "password"), password)

    val table = (datasource \ "table").extract[List[String]].head
    addSparkConf(properties, combineKey(prefix, "table"), table)

    addSparkConfIfExist(readerData, "conditions", properties, combineKey(prefix, "condition"))

    val advancedConf = (readerData \ "conf").extract[List[Map[String, String]]]
    addAdvancedSparkConf(advancedConf, properties, prefix)

    val workerHome = properties.getProperty(Property.WORKER_HOME).get
    val jars = properties.getList(Property.SPARK_ARGUMENT_PREFIX + "jars", List())
    val extraJars =
      FileUtils.listFiles(FileUtils.getFile(workerHome, "ddb-qs"), null, false)
        .asScala.filter(!_.isDirectory).map(_.getAbsolutePath).filter(_.endsWith("jar"))
    addSparkArgument(properties, "jars",
      (jars ++ extraJars).toSet.mkString(Properties.DEFAULT_LIST_SEPARATOR))
  }

  override def parseWriter(writerData: JValue): Unit = {
    val prefix = writerPrefix("ddb.qs")
    implicit val formats: DefaultFormats.type = DefaultFormats
    val properties = Properties.properties
    addSparkConf(properties, "spark.transmit.writer", "com.netease.music.da.transfer.ddb.qs.writer.DDBQSWriter")

    val datasource = writerData \ "dataSource"
    val url = (datasource \ "connectionInformation" \ "url").extract[String]
    addSparkConf(properties, combineKey(prefix, "url"), url)
    val user = (datasource \ "connectionInformation" \ "userName").extract[String]
    addSparkConf(properties, combineKey(prefix, "user"), user)
    val password = (datasource \ "connectionInformation" \ "password").extract[String]
    addSparkConf(properties, combineKey(prefix, "password"), password)

    val table = (datasource \ "table").extract[List[String]].head
    addSparkConf(properties, combineKey(prefix, "table"), table)

    val advancedConf = (writerData \ "conf").extract[List[Map[String, String]]]
    addAdvancedSparkConf(advancedConf, properties, prefix)

    val preSQL = (writerData \ "preSQL").extract[List[String]]
    preSQL.indices.foreach{ id =>
      val sql = {
        val origin = preSQL(id).trim
        if (origin.endsWith(";")) {
          origin.substring(0, origin.length - 1)
        } else {
          origin
        }
      }
      addSparkConf(properties, combineKey(prefix, "preSql", id.toString), sql)
    }
    val postSQL: List[String] = (writerData \ "postSQL").extract[List[String]]
    postSQL.indices.foreach{ id =>
      val sql = {
        val origin = postSQL(id).trim
        if (origin.endsWith(";")) {
          origin.substring(0, origin.length - 1)
        } else {
          origin
        }
      }
      addSparkConf(properties, combineKey(prefix, "postSql", id.toString), sql)
    }

    (writerData \ "insertType").extract[String] match {
      case "into" =>
        addSparkConf(properties, combineKey(prefix, "saveMode"), "insertInto")
      case "ignore" =>
        addSparkConf(properties, combineKey(prefix, "saveMode"), "insertIgnore")
      case "overwrite" =>
        addSparkConf(properties, combineKey(prefix, "saveMode"), "replaceInto")
      case _ =>
    }
    val workerHome = properties.getProperty(Property.WORKER_HOME).get
    val jars = properties.getList(Property.SPARK_ARGUMENT_PREFIX + "jars", List())
    val extraJars =
      FileUtils.listFiles(FileUtils.getFile(workerHome, "ddb-qs"), null, false)
        .asScala.filter(!_.isDirectory).map(_.getAbsolutePath).filter(_.endsWith("jar"))
    addSparkArgument(properties, "jars",
      (jars ++ extraJars).toSet.mkString(Properties.DEFAULT_LIST_SEPARATOR))
  }
}
