package com.netease.music.da.transfer.jdbc.reader

import com.netease.music.da.transfer.common.conf.Properties
import com.netease.music.da.transfer.common.reader.AbstractDataReader
import com.netease.music.da.transfer.jdbc.conf.JDBCProperties._
import com.netease.music.da.transfer.jdbc.connection.DBConnection
import com.netease.music.da.transfer.jdbc.reader.split.{TableSplit, TableSplitBuilder}
import com.netease.music.da.transfer.jdbc.util.JDBCUtils
import com.netease.music.da.transfer.jdbc.vo.TableMeta
import org.apache.spark.sql.catalyst.TableIdentifier
import org.apache.spark.sql.execution.datasources.jdbc.{JDBCOptions, JDBCRDD}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, Row, SparkSession}

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}

abstract class JDBCReader(spark: SparkSession) extends AbstractDataReader(spark) {

  @transient private var dbConnections: mutable.Map[String, DBConnection] = _

  def getOrCreateDBConnection(properties: Properties): DBConnection = {
    this.synchronized {
      val url = properties.getProperty(URL).get
      if (dbConnections == null) {
        dbConnections = mutable.Map[String, DBConnection]()
      }
      if (dbConnections.contains(url)) {
        val connection = dbConnections(url)
        if (connection.isClosed) {
          connection.closeQuietly()
          dbConnections.put(url, createDBConnection(properties))
        }
      } else {
        dbConnections.put(url, createDBConnection(properties))
      }
      dbConnections(url)
    }
  }

  def closeDBConnection(properties: Properties): Unit = {
    this.synchronized {
      val url = properties.getProperty(URL).get
      val connection = dbConnections.remove(url)
      connection.foreach(_.closeQuietly())
    }
  }

  def createDBConnection(properties: Properties): DBConnection

  override def confPrefix: String = "spark.transmit.reader.jdbc"

  private[transfer] def tableProperties: List[Properties] = {
    val columns = properties.getProperty(COLUMNS).get
    LOG.info("columns: " + columns.mkString(","))

    val condition = properties.getProperty(CONDITION).get
    LOG.info("conditions: " + condition)

    val ret = ListBuffer[Properties]()
    val sourcesProperties = this.properties.getProperties(SOURCES)
    sourcesProperties.foreach { properties =>
      val databaseOpt = properties.getProperty(DATABASE)
      val url = properties.getProperty(URL).get
      val user = properties.getProperty(USER).get
      val password = properties.getProperty(PASSWORD).get

      val tables = if (properties.contains(TABLE_REGEX.key)) {
        val tableRegex = properties.getProperty(TABLE_REGEX).get
        val connection = getOrCreateDBConnection(
          Properties()
            .putProperties(this.properties)
            .put(URL.key, url)
            .put(USER.key, user)
            .put(PASSWORD.key, password)
            .put(CONDITION.key, condition)
            .put(COLUMNS.key, columns.mkString(",")))
        connection.getTables(tableRegex, databaseOpt)
      } else if (properties.contains(TABLES.key)) {
        properties.getProperty(TABLES).get
      } else {
        throw new IllegalArgumentException("Neither 'tables' nor 'tableRegex' could be found in properties.")
      }
      tables.foreach { table =>
        val tableIdentifier = TableIdentifier(table, databaseOpt)
        LOG.info(
          s"""
             |Find table:
             |  url: $url
             |  table: ${tableIdentifier.unquotedString}
            """.stripMargin)
        ret += {
          val properties = new Properties()
          properties
            .putProperties(this.properties)
            .put(URL.key, url)
            .put(USER.key, user)
            .put(PASSWORD.key, password)
            .put(TABLE.key, table)
            .put(CONDITION.key, condition)
            .put(COLUMNS.key, columns.mkString(","))
          databaseOpt.foreach(properties.put(DATABASE.key, _))
          properties
        }
      }
    }
    ret.toList
  }

  def optimizeSplits(origin: Array[TableSplit]): Array[TableSplit] = {
    val map = mutable.Map[String, ArrayBuffer[TableSplit]]()
    var ret = ArrayBuffer[TableSplit]()
    origin.foreach { tableSplit =>
      val url = tableSplit.properties.getProperty(URL).get
      if (map.contains(url)) {
        map(url) += tableSplit
      } else {
        map(url) = ArrayBuffer[TableSplit](tableSplit)
      }
    }
    var current = 0
    val size = origin.length
    while (current < size) {
      var isFetched = false
      map.foreach { pair =>
        val splitList =  pair._2
        if (splitList.nonEmpty) {
          val tail = splitList.remove(splitList.length - 1)
          ret += tail
          current += 1
          isFetched = true
        }
      }
      if (!isFetched) {
        throw new Exception("Optimize splits failed.")
      }
    }

    ret.toArray
  }

  override def read(): DataFrame = {
    val allTableProperties = tableProperties
    val splits = optimizeSplits(allTableProperties.map(getTableSplits).reduce(_ ++ _))
    val schemas = allTableProperties.map(getSchema).distinct
    if (schemas.size > 1) {
      LOG.error("Difference found in all tables' schema.")
      throw new IllegalArgumentException("Difference found in all tables' schema.")
    } else if (schemas.isEmpty) {
      LOG.error("No schema was found.")
      throw new IllegalArgumentException("No schema was found.")
    }

    var schema = schemas.head
    val columns = allTableProperties.head.getProperty(COLUMNS).get
    if (columns.nonEmpty && !(columns.size == 1 && columns.head == "*")) {
      schema = StructType(schema.filter { entry =>
        columns.map(_.toLowerCase).contains(entry.name.toLowerCase())
      })
    }

    val jdbcReaderAccumulator = spark.sparkContext.longAccumulator("JDBCReader")
    spark.createDataFrame(
      spark
        .sparkContext
        .parallelize(splits, splits.length)
        .mapPartitions(dumpData(_, schema))
        .map { row =>
          jdbcReaderAccumulator.add(1)
          row
        },
      schema
    )

  }

  def getTableSplits(tableProperties: Properties): Array[TableSplit] = {
    val table = tableProperties.getProperty(TABLE).get
    val condition = tableProperties.getProperty(CONDITION).get
    val database = tableProperties.getProperty(DATABASE)
    val tableIdentifier = TableIdentifier(table, database)
    val split = tableProperties.getProperty(SPLIT)
    LOG.info(
      s"""
         |Split table:
         |    urL: ${tableProperties.getProperty(URL).get}
         |    table: ${tableIdentifier.unquotedString}
         |    splitKey: $split
         |    condition: $condition
      """.stripMargin)
    val dbConnection = getOrCreateDBConnection(tableProperties)
    val tableMeta = dbConnection.getTableMetaData(tableIdentifier)
    if (tableMeta == null) {
      LOG.error("Cannot fetch the information of table.")
    }

    if (split.isDefined) {
      tableMeta.primaryKey = tableMeta.columnList.filter(_.name.equalsIgnoreCase(split.get)).head
    }
    if (tableMeta.primaryKey == null) {
      LOG.warn("Spilt key is empty")
      Array(new TableSplit(tableIdentifier, null, null, tableProperties))
    } else {
      val partitionNum = getPartitionNum(tableMeta)
      LOG.info(s"Partition num: $partitionNum")
      tableMeta.properties.put(PARTITION_NUM.key, partitionNum.toString)
      TableSplitBuilder.buildTableSplits(dbConnection, tableMeta).toArray
    }
  }

  def getPartitionNum(tableMeta: TableMeta): Int = {
    if (tableMeta.properties.contains(PARTITION_NUM.key)) {
      tableMeta.properties.getProperty(PARTITION_NUM).get
    } else {
      val partitionSize = tableMeta.properties.getProperty(PARTITION_SIZE).get
      val partitionNum = Math.max(Math.ceil(1.0 * tableMeta.tableDataLength / partitionSize).toInt, 1)
      LOG.info(s"Property 'partitionNum' could not be found in properties. " +
        s"Calculate the partition number automatically.")
      partitionNum
    }
  }

  def getSchema(tableProperties: Properties): StructType = {
    val table = tableProperties.getProperty(TABLE).get
    val database = tableProperties.getProperty(DATABASE)
    val tableIdentifier = TableIdentifier(table, database)
    val options = tableProperties.toMap + (JDBCOptions.JDBC_TABLE_NAME -> tableProperties.getProperty(TABLE).get)
    val url = tableProperties.getProperty(URL).get
    JDBCRDD.resolveTable(new JDBCOptions(url, JDBCUtils.getQueryTableName(tableIdentifier, tableProperties), options))
  }

  def dumpData(tableSplits: Iterator[TableSplit],
               schema: StructType): Iterator[Row] = {
    new DBRowIterator(this, tableSplits, schema)
  }

  override def close(): Unit = {
    dbConnections.foreach(_._2.closeQuietly())
  }
}
