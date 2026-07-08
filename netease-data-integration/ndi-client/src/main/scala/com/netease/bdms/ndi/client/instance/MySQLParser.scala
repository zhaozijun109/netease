package com.netease.bdms.ndi.client.instance
import com.netease.bdms.ndi.client.property.{Properties, Property}
import org.apache.commons.io.FileUtils
import org.json4s.{DefaultFormats, JValue}

import scala.collection.mutable
import scala.collection.JavaConverters._

object MySQLParser extends Parser {
  override def parseReader(readerData: JValue): Unit = {
    val prefix = readerPrefix("mysql")
    implicit val formats: DefaultFormats.type = DefaultFormats
    val properties = Properties.properties
    addSparkConf(properties, "spark.transmit.reader", "com.netease.music.da.transfer.mysql.reader.MySQLReader")

    val workerHome = properties.getProperty(Property.WORKER_HOME).get
    val jars = properties.getList(Property.SPARK_ARGUMENT_PREFIX + "jars", List())
    val extraJars =
      FileUtils.listFiles(FileUtils.getFile(workerHome, "mysql"), null, false).asScala.map(_.getAbsolutePath)
    addSparkArgument(properties, "jars", (jars ++ extraJars).toSet.mkString(Properties.DEFAULT_LIST_SEPARATOR))

    val datasourceList = (readerData \ "dataSources").extract[List[JValue]]
    val sources = new mutable.ArrayBuffer[String]()
    var index = 0
    datasourceList.foreach { data =>
      val prefix = combineKey(readerPrefix("mysql"), "sources")
      val id = index.toString
      sources += id
      val url = (data \ "connectionInformation" \ "url").extract[String]

      addSparkConf(properties, combineKey(prefix, id, "url"), url)

      val user = (data \ "connectionInformation" \ "userName").extract[String]
      addSparkConf(properties, combineKey(prefix, id, "user"), user)

      val password = (data \ "connectionInformation" \ "password").extract[String]
      addSparkConf(properties, combineKey(prefix, id, "password"), password)

      val database = (data \ "database").extract[String]
      addSparkConf(properties, combineKey(prefix, id, "database"), database)

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
  }

  override def parseWriter(writerData: JValue): Unit = {
    val prefix = writerPrefix("mysql")
    implicit val formats: DefaultFormats.type = DefaultFormats
    val properties = Properties.properties
    addSparkConf(properties, "spark.transmit.writer", "com.netease.music.da.transfer.mysql.writer.MySQLWriter")

    val workerHome = properties.getProperty(Property.WORKER_HOME).get
    val jars = properties.getList(Property.SPARK_ARGUMENT_PREFIX + "jars", List())
    val extraJars =
      FileUtils.listFiles(FileUtils.getFile(workerHome, "mysql"), null, false).asScala.map(_.getAbsolutePath)
    addSparkArgument(properties, "jars", (jars ++ extraJars).toSet.mkString(Properties.DEFAULT_LIST_SEPARATOR))

    val dataSource = writerData \ "dataSource"
    val database = (dataSource \ "database").extract[String]
    addSparkConf(properties, combineKey(prefix, "database"), database)
    val table = (dataSource \ "table").extract[List[String]].head
    addSparkConf(properties, combineKey(prefix, "table"), table)
    val url = (dataSource \ "connectionInformation" \ "url").extract[String]
    addSparkConf(properties, combineKey(prefix, "url"), url)
    val user = (dataSource \ "connectionInformation" \ "userName").extract[String]
    addSparkConf(properties, combineKey(prefix, "user"), user)
    val password = (dataSource \ "connectionInformation" \ "password").extract[String]
    addSparkConf(properties, combineKey(prefix, "password"), password)

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

    val advancedConf = (writerData \ "conf").extract[List[Map[String, String]]]
    addAdvancedSparkConf(advancedConf, properties, prefix)
  }}
