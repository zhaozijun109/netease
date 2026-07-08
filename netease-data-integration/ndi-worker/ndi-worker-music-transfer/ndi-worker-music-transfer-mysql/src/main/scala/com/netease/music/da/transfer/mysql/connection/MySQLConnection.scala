package com.netease.music.da.transfer.mysql.connection

import java.sql.{Connection, DriverManager, Statement}

import com.netease.music.da.transfer.common.conf.Properties
import com.netease.music.da.transfer.jdbc.connection.DBConnection
import com.netease.music.da.transfer.jdbc.vo.{ColumnMeta, TableMeta}
import org.apache.spark.sql.catalyst.TableIdentifier

import scala.collection.mutable.ArrayBuffer

class MySQLConnection(properties: Properties) extends DBConnection(properties) {

  override protected def createConnection(): Connection = {
    Class.forName("com.mysql.jdbc.Driver")
    val finalUrl = addSuffix(url, urlSuffix)
    println(s"Create connection to $finalUrl")
    DriverManager.getConnection(finalUrl, user, password)
  }

  override protected def createStatement(): Statement = {
    val statement =
      connection.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY)
    statement.setFetchSize(Integer.MIN_VALUE)
    statement
  }

  override def getTableMetaData(tableIdentifier: TableIdentifier): TableMeta = {
    val columnList = getColumnMetaDataList(tableIdentifier)
    val table = tableIdentifier.table
    val sql =
      s"""
         |SELECT
         |  *
         |FROM
         |  information_schema.TABLES
         |WHERE
         |  TABLE_NAME = '$table' AND TABLE_SCHEMA = '${getSchema(tableIdentifier)}'
        """.stripMargin
    var primaryKey: ColumnMeta = null
    val primaryKeyList = columnList.filter(_.pri == true)
    if (primaryKeyList.nonEmpty) {
      primaryKey = primaryKeyList.head
    }
    this.executeQuery[TableMeta](sql, { resultSet =>
      if (resultSet.next()) {
        val tableComment = resultSet.getString("TABLE_COMMENT")
        val tableRows = resultSet.getLong("TABLE_ROWS")
        val tableAvgRowLength = resultSet.getLong("AVG_ROW_LENGTH")
        val tableDataLength = resultSet.getLong("DATA_LENGTH")
        TableMeta(tableIdentifier, primaryKey, columnList, tableComment, properties,
          tableRows, tableAvgRowLength, tableDataLength)
      } else {
        null
      }
    }).orNull
  }

  override def getTables(regex: String, database: Option[String]): List[String] = {
    val sql =
      s"""
         |SELECT
         |  TABLE_NAME
         |FROM
         |  information_schema.TABLES
         |WHERE
         |  TABLE_SCHEMA = '${getSchema(TableIdentifier("fake", database))}'
         |AND
         |  TABLE_NAME regexp '$regex'
      """.stripMargin
    LOG.info(s"Execute sql to match tables: $sql")
    this.executeQuery(sql, { resultSet =>
      val ret = ArrayBuffer[String]()
      while (resultSet.next()) {
        ret += resultSet.getString(1)
      }
      ret
    }).get.toList
  }

  override def getSchema(tableIdentifier: TableIdentifier): String = {
    tableIdentifier.database.getOrElse(connection.getCatalog)
  }

  override def getCatalog(tableIdentifier: TableIdentifier): String = {
    tableIdentifier.database.getOrElse(connection.getCatalog)
  }
}
