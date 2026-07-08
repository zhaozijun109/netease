package com.netease.music.da.transfer.jdbc.writer

import java.sql.Connection

import com.netease.music.da.transfer.common.conf.Properties
import com.netease.music.da.transfer.common.writer.AbstractDataWriter
import com.netease.music.da.transfer.jdbc.conf.JDBCProperties._
import com.netease.music.da.transfer.jdbc.connection.DBConnection
import com.netease.music.da.transfer.jdbc.util.JDBCUtils
import org.apache.commons.lang.StringUtils
import org.apache.spark.sql.catalyst.TableIdentifier
import org.apache.spark.sql.execution.datasources.jdbc.{JDBCOptions, JdbcUtils}
import org.apache.spark.sql.internal.SQLConf
import org.apache.spark.sql.jdbc.{JdbcDialect, JdbcDialects}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.collection.mutable

abstract class JDBCWriter(spark: SparkSession) extends AbstractDataWriter(spark) {
  @transient private var dbConnections: mutable.Map[String, DBConnection] = _

  override def addDefaultProperties(props: Properties): Unit = {
    super.addDefaultProperties(props)
    props.put(NEED_REPARTITION.key, "true")
    props.put(REPARTITION_NUM.key, "1")
  }

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

  def createDBConnection(properties: Properties): DBConnection

  def doPreSQL(): Unit = {
    val connection = getOrCreateDBConnection(this.properties)
    this.properties.getSerial(PRE_SQL).foreach { sql =>
      if (!StringUtils.isEmpty(sql)) {
        LOG.info(s"Execute preSql: $sql")
        connection.executeQuery(sql, { _ => {} })
      }
    }
    connection.closeQuietly()
  }

  def doPostSQL(): Unit = {
    val connection = getOrCreateDBConnection(this.properties)
    this.properties.getSerial(POST_SQL).foreach { sql =>
      if (!StringUtils.isEmpty(sql)) {
        LOG.info(s"Execute postSql: $sql")
        connection.executeQuery(sql, { _ => {} })
      }
    }
    connection.closeQuietly()
  }


  def getInsertStatement(table: String,
                         rddSchema: StructType,
                         tableSchema: Option[StructType],
                         isCaseSensitive: Boolean,
                         dialect: JdbcDialect): String = {
    JdbcUtils.getInsertStatement(table, rddSchema, tableSchema, isCaseSensitive, dialect)
  }

  override def write(data: DataFrame): Unit = {
    val url = this.properties.getProperty(URL).get
    val tableName = this.properties.getProperty(TABLE).get
    val database = this.properties.getProperty(DATABASE)
    val tableIdentifier = TableIdentifier(tableName, database)
    val tableSchema = getOrCreateDBConnection(this.properties).getSchemaOption(tableIdentifier)
    val table = JDBCUtils.getQueryTableName(tableIdentifier, this.properties)
    val rddSchema = data.schema
    val getConnection: () => Connection = () => {
      getOrCreateDBConnection(this.properties).connection
    }
    val batchSize = this.properties.getProperty(BATCH_SIZE).get
    val isolationLevel = this.properties.getProperty(ISOLATION_LEVEL).get
    val isCaseSensitive = spark.sessionState.conf.getConf(SQLConf.CASE_SENSITIVE)
    val dialect = JdbcDialects.get(url)
    val statement = getInsertStatement(table, rddSchema, tableSchema, isCaseSensitive, dialect)

    val repartitionedData = if (this.properties.getProperty(NEED_REPARTITION).get) {
      this.properties.getProperty(REPARTITION_NUM) match {
        case Some(n) if n <= 0 =>
          LOG.warn(s"Invalid value `$n` for parameter `${PARTITION_NUM.key}` in table writing " +
            "via JDBC. The minimum value is 1, skip it.")
          data
        case Some(n) if n < data.rdd.getNumPartitions =>
          LOG.info(s"Repartitioned data set with size $n")
          data.coalesce(n)
        case Some(n) if n >= data.rdd.getNumPartitions =>
          LOG.warn(s"The value `$n` for parameter `${PARTITION_NUM.key}` in table writing " +
            "via JDBC is bigger than or equals to origin partition size, skip it.")
          data
      }
    } else {
      data
    }
    doPreSQL()
    val urlForPartition = url
    val tableForPartition = table
    val userOpt = this.properties.getOption("user")
    val passwordOpt = this.properties.getOption("password")
    val jdbcOptionParams = scala.collection.mutable.Map[String, String](
      JDBCOptions.JDBC_URL -> urlForPartition,
      JDBCOptions.JDBC_TABLE_NAME -> tableForPartition,
      JDBCOptions.JDBC_BATCH_INSERT_SIZE -> batchSize.toString
    )
    userOpt.foreach(jdbcOptionParams.put("user", _))
    passwordOpt.foreach(jdbcOptionParams.put("password", _))
    val jdbcOptions = new JDBCOptions(jdbcOptionParams.toMap)
    repartitionedData.rdd.foreachPartition(iterator =>
      JdbcUtils.savePartition(tableForPartition, iterator, rddSchema, statement, batchSize, dialect, isolationLevel, jdbcOptions))
    doPostSQL()
  }

  override def confPrefix: String = "spark.transmit.writer.jdbc"
}
