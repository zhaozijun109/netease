package com.netease.bdms.ndi.client.instance

import com.netease.bdms.ndi.client.property.{Properties, Property}
import org.apache.commons.io.FileUtils
import org.json4s.{DefaultFormats, JValue}

import scala.collection.mutable
import scala.collection.JavaConverters._

object OracleParser extends Parser {
  override def parseReader(readerData: JValue): Unit = {
    val prefix = readerPrefix("oracle")
    implicit val formats: DefaultFormats.type = DefaultFormats
    val properties = Properties.properties
    addSparkConf(properties, "spark.transmit.reader", "com.netease.music.da.transfer.oracle.reader.OracleReader")

    val datasourceList = (readerData \ "dataSources").extract[List[JValue]]
    val sources = new mutable.ArrayBuffer[String]()
    var index = 0
    datasourceList.foreach { data =>
      val prefix = combineKey(readerPrefix("oracle"), "sources")
      val id = index.toString
      sources += id
      val url = (data \ "connectionInformation" \ "url").extract[String]

      addSparkConf(properties, combineKey(prefix, id, "url"), url)

      val user = (data \ "connectionInformation" \ "userName").extract[String]
      addSparkConf(properties, combineKey(prefix, id, "user"), user)

      val password = (data \ "connectionInformation" \ "password").extract[String]
      addSparkConf(properties, combineKey(prefix, id, "password"), password)

      val tableType = (data \ "tableNameType").extract[String]
      if (tableType == "normal") {
        val key = combineKey(prefix, id, "tables")
        val value = (data \ "table").extract[List[String]].mkString(",")
        addSparkConf(properties, key, value)
      } else if (tableType == "regular") {
        val key = combineKey(prefix, id, "tableRegex")
        val value = (data \ "table").extract[List[String]].head
        addSparkConf(properties, key, value)
      }
      index += 1
    }
    addSparkConf(properties, combineKey(prefix, "sources"), sources.mkString(","))
    addSparkConfIfExist(readerData, "conditions", properties, combineKey(prefix, "condition"))

    val advancedConf = (readerData \ "conf").extract[List[Map[String, String]]]
    addAdvancedSparkConf(advancedConf, properties, prefix)

    val version = (readerData \ "version").extractOpt[String]
    val workerHome = properties.getProperty(Property.WORKER_HOME).get
    val jars = properties.getList(Property.SPARK_ARGUMENT_PREFIX + "jars", List())
    val ddbCommonJars =
      FileUtils.listFiles(FileUtils.getFile(workerHome, "oracle"), null, false)
        .asScala.filter(!_.isDirectory).map(_.getAbsolutePath).filter(_.endsWith("jar"))
    val ddbVersionJars =
      FileUtils.listFiles(FileUtils.getFile(workerHome, "oracle", version.getOrElse("ojdbc8")), null, false)
        .asScala.filter(!_.isDirectory).map(_.getAbsolutePath).filter(_.endsWith("jar"))

    addSparkArgument(properties, "jars",
      (jars ++ ddbCommonJars ++ ddbVersionJars).toSet.mkString(Properties.DEFAULT_LIST_SEPARATOR))
  }

  override def parseWriter(writerData: JValue): Unit = {
    val prefix = writerPrefix("oracle")
    implicit val formats: DefaultFormats.type = DefaultFormats
    val properties = Properties.properties
    addSparkConf(properties, "spark.transmit.writer", "com.netease.music.da.transfer.oracle.writer.OracleWriter")

    val dataSource = writerData \ "dataSource"
    val table = (dataSource \ "table").extract[List[String]].head
    addSparkConf(properties, combineKey(prefix, "table"), table)
    val url = (dataSource \ "connectionInformation" \ "url").extract[String]
    addSparkConf(properties, combineKey(prefix, "url"), url)
    val user = (dataSource \ "connectionInformation" \ "userName").extract[String]
    addSparkConf(properties, combineKey(prefix, "user"), user)
    val password = (dataSource \ "connectionInformation" \ "password").extract[String]
    addSparkConf(properties, combineKey(prefix, "password"), password)

    val preSQL = (writerData \ "preSQL").extract[List[String]]
    preSQL.indices.foreach { id =>
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
    postSQL.indices.foreach { id =>
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
      case _ =>
    }

    val advancedConf = (writerData \ "conf").extract[List[Map[String, String]]]
    addAdvancedSparkConf(advancedConf, properties, prefix)

    val version = (writerData \ "version").extractOpt[String]
    val workerHome = properties.getProperty(Property.WORKER_HOME).get
    val jars = properties.getList(Property.SPARK_ARGUMENT_PREFIX + "jars", List())
    val ddbCommonJars =
      FileUtils.listFiles(FileUtils.getFile(workerHome, "oracle"), null, false)
        .asScala.filter(!_.isDirectory).map(_.getAbsolutePath).filter(_.endsWith("jar"))
    val ddbVersionJars =
      FileUtils.listFiles(FileUtils.getFile(workerHome, "oracle", version.getOrElse("ojdbc8")), null, false)
        .asScala.filter(!_.isDirectory).map(_.getAbsolutePath).filter(_.endsWith("jar"))

    addSparkArgument(properties, "jars",
      (jars ++ ddbCommonJars ++ ddbVersionJars).toSet.mkString(Properties.DEFAULT_LIST_SEPARATOR))
  }
}
