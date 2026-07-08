package com.netease.bdms.ndi.client.instance
import com.netease.bdms.ndi.client.property.{Properties, Property}
import org.apache.commons.io.FileUtils
import org.json4s.{DefaultFormats, JValue}

import scala.collection.JavaConverters._

object HiveParser extends Parser {
  override def parseReader(readerData: JValue): Unit = {
    val prefix = readerPrefix("hive")
    implicit val formats: DefaultFormats.type = DefaultFormats
    val properties = Properties.properties
    addSparkConf(properties, "spark.transmit.reader", "com.netease.music.da.transfer.hive.reader.HiveReader")

    val workerHome = properties.getProperty(Property.WORKER_HOME).get
    val jars = properties.getList(Property.SPARK_ARGUMENT_PREFIX + "jars", List())
    val extraJars =
      FileUtils.listFiles(FileUtils.getFile(workerHome, "hive"), null, false).asScala.map(_.getAbsolutePath)
    addSparkArgument(properties, "jars", (jars ++ extraJars).toSet.mkString(Properties.DEFAULT_LIST_SEPARATOR))

    val datasource = readerData \ "dataSources"
    val database = (datasource \ "database").extract[String]
    addSparkConf(properties, combineKey(prefix, "database"), database)
    val table = (datasource \ "table").extract[String]
    addSparkConf(properties, combineKey(prefix, "table"), table)

    val conditionOpt = (readerData \ "conditions").extractOpt[String]
    conditionOpt.foreach{ condition =>
      if (condition.nonEmpty) {
        addSparkConf(properties, combineKey(prefix, "condition"), condition)
      }
    }

    val advancedConf = (readerData \ "conf").extract[List[Map[String, String]]]
    addAdvancedSparkConf(advancedConf, properties, prefix)
  }

  override def parseWriter(writerData: JValue): Unit = {
    val prefix = writerPrefix("hive")
    implicit val formats: DefaultFormats.type = DefaultFormats
    val properties = Properties.properties
    addSparkConf(properties, "spark.transmit.writer", "com.netease.music.da.transfer.hive.writer.HiveWriter")
    addSparkConf(properties, "spark.sql.catalogImplementation", "hive")

    val workerHome = properties.getProperty(Property.WORKER_HOME).get
    val jars = properties.getList(Property.SPARK_ARGUMENT_PREFIX + "jars", List())
    val extraJars =
      FileUtils.listFiles(FileUtils.getFile(workerHome, "hive"), null, false).asScala.map(_.getAbsolutePath)
    addSparkArgument(properties, "jars", (jars ++ extraJars).toSet.mkString(Properties.DEFAULT_LIST_SEPARATOR))

    val dataSource = writerData \ "dataSource"
    addSparkConf(properties, combineKey(prefix, "database"), (dataSource \ "database").extract[String])
    addSparkConf(properties, combineKey(prefix, "table"), (dataSource \ "table").extract[String])

    (dataSource \ "partition").extractOpt[List[Map[String, String]]].foreach { list =>
      val value = list.map { map =>
        map("key") + "='" + map("value") + "'"
      }
      if (value.nonEmpty) {
        addSparkConf(properties, combineKey(prefix, "partition"), value.mkString(","))
      }
    }
    (writerData \ "insertType").extract[String] match {
      case "into" =>
        addSparkConf(properties, combineKey(prefix, "saveMode"), "insertInto")
      case "overwrite" =>
        addSparkConf(properties, combineKey(prefix, "saveMode"), "insertOverwrite")
      case _ =>
    }

    val partitionsOpt = (writerData \ "partition").extractOpt[List[Map[String, String]]]
    partitionsOpt.foreach { partitions =>
      val partitionValues = partitions.map { partition =>
        val key = partition("key")
        val value = partition("value")
        s"$key='$value'"
      }
      if (partitionValues.nonEmpty) {
        addSparkConf(properties, combineKey(prefix, "partition"), partitionValues.mkString(","))
      }
    }

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

    val advancedConf = (writerData \ "conf").extract[List[Map[String, String]]]
    addAdvancedSparkConf(advancedConf, properties, prefix)
  }
}
