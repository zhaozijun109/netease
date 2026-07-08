package com.netease.music.da.transfer.jdbc.connection

import java.sql.{Connection, ResultSet, SQLException, Statement}

import com.netease.music.da.transfer.common.conf.Properties
import com.netease.music.da.transfer.jdbc.conf.JDBCProperties._
import com.netease.music.da.transfer.common.log.LogTrait
import com.netease.music.da.transfer.jdbc.util.JDBCUtils
import com.netease.music.da.transfer.jdbc.vo.{ColumnMeta, TableMeta}
import org.apache.spark.sql.catalyst.TableIdentifier
import org.apache.spark.sql.execution.datasources.jdbc.JdbcUtils
import org.apache.spark.sql.jdbc.JdbcDialects
import org.apache.spark.sql.types.StructType

import scala.collection.mutable.ListBuffer
import scala.util.Try

abstract class DBConnection(val properties: Properties)
  extends Serializable with LogTrait {

  @transient protected val url: String = properties.getProperty(URL).get
  @transient protected val urlSuffix: Option[String] = properties.getProperty(URL_SUFFIX)
  @transient protected val user: String = properties.getProperty(USER).get
  @transient protected val password: String = properties.getProperty(PASSWORD).get

  @transient lazy val connection: Connection = createConnection()

  protected def createConnection(): Connection

  def closeQuietly(): Unit = {
    Try {
      connection.close()
    }
  }

  def executeQuery[T](sql: String, callback: ResultSet => T): Option[T] = {
    val statement = createStatement()
    statement.execute(sql)
    val resultSet = statement.getResultSet
    val result = callback(resultSet)
    statement.close()
    Option.apply(result)
  }

  def getResultSet(sql: String): ResultSet = {
    val statement = createStatement()
    statement.execute(sql)
    statement.getResultSet
  }

  protected def createStatement(): Statement = {
    val statement = connection.createStatement()
    val fetchSize = properties.getProperty(FETCH_SIZE).get
    statement.setFetchSize(fetchSize)
    statement
  }

  def getTables(regex: String, database: Option[String]): List[String]

  def getTableMetaData(tableIdentifier: TableIdentifier): TableMeta

  def getSchemaOption(tableIdentifier: TableIdentifier): Option[StructType] = {
    val dialect = JdbcDialects.get(url)
    try {
      val query = dialect.getSchemaQuery(JDBCUtils.getQueryTableName(tableIdentifier, properties))
      val statement = connection.prepareStatement(query)
      try {
        Some(JdbcUtils.getSchema(statement.executeQuery(), dialect))
      } catch {
        case _: SQLException =>
          LOG.error(s"Get schema of ${tableIdentifier.quotedString} failed.")
          None
      } finally {
        statement.close()
      }
    } catch {
      case _: SQLException =>
        LOG.error(s"Get schema of ${tableIdentifier.quotedString} failed.")
        None
    }
  }

  def getSchema(tableIdentifier: TableIdentifier): String

  def getCatalog(tableIdentifier: TableIdentifier): String

  def getColumnMetaDataList(tableIdentifier: TableIdentifier): List[ColumnMeta] = {
    val result = new ListBuffer[ColumnMeta]
    val metaData = connection.getMetaData
    val table = tableIdentifier.table
    val schema = getSchema(tableIdentifier)
    val catalog = getCatalog(tableIdentifier)
    val columns = metaData.getColumns(catalog, schema, table, "%")
    val rs = metaData.getPrimaryKeys(catalog, schema, table)
    var primaryKeys = new ListBuffer[String]
    while (rs.next()) {
      primaryKeys += rs.getString("COLUMN_NAME")
    }
    while (columns.next()) {
      val columnName = columns.getString("COLUMN_NAME")
      val columnPosition = columns.getInt("ORDINAL_POSITION")
      val columnType = columns.getString("DATA_TYPE")
      val columnComment = columns.getString("REMARKS")
      if (primaryKeys.contains(columnName)) {
        result += ColumnMeta(tableIdentifier, columnName, columnType, columnComment, columnPosition, pri = true)
      } else {
        result += ColumnMeta(tableIdentifier, columnName, columnType, columnComment, columnPosition, pri = false)
      }
    }
    result.toList
  }

  def tableExist(table: String): Boolean = {
    Try {
      executeQuery(s"SELECT * FROM $table WHERE 1=0", _ => {})
    }.isSuccess
  }

  def isClosed: Boolean = {
    this.connection != null && this.connection.isClosed
  }

  def getMinMaxSql(tableName: String, column: String, condition: String): String = {
    s"""
      |SELECT
      |  min($column) minVal,
      |  max($column) maxVal
      |FROM
      |  $tableName
      |WHERE
      |  $condition
    """.stripMargin
  }

  def addSuffix(url: String, suffix: Option[String]): String = {
    if (suffix.isDefined) {
      if (url.contains("?")) {
        url + "&" + suffix.get
      } else {
        url + "?" + suffix.get
      }
    } else {
      url
    }
  }
}
