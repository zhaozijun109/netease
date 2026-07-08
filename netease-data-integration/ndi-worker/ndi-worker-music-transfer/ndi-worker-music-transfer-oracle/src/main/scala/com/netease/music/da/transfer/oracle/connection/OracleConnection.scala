package com.netease.music.da.transfer.oracle.connection

import java.sql.{Connection, DriverManager}

import com.netease.music.da.transfer.common.conf.Properties
import com.netease.music.da.transfer.jdbc.connection.DBConnection
import com.netease.music.da.transfer.jdbc.vo.{ColumnMeta, TableMeta}
import com.netease.music.da.transfer.oracle.conf.OracleConstants
import org.apache.spark.sql.catalyst.TableIdentifier

class OracleConnection(properties: Properties) extends DBConnection(properties) {
  override protected def createConnection(): Connection = {
    Class.forName(OracleConstants.DEFAULT_DRIVER)
    val finalUrl = addSuffix(url, urlSuffix)
    println(s"Create connection to $finalUrl")
    DriverManager.getConnection(finalUrl, user, password)
  }

  override def getTables(regex: String, database: Option[String]): List[String] = {
    throw new UnsupportedOperationException
  }

  override def getTableMetaData(tableIdentifier: TableIdentifier): TableMeta = {
    val columnList = getColumnMetaDataList(tableIdentifier)
    val table = tableIdentifier.table
    val sql =
      s"""
         |SELECT
         |	USER_TAB_COMMENTS.COMMENTS AS TABLE_COMMENT,
         |	USER_TABLES.NUM_ROWS AS TABLE_ROWS,
         |	USER_TABLES.AVG_ROW_LEN AS AVG_ROW_LENGTH,
         |	USER_TABLES.NUM_ROWS * USER_TABLES.AVG_ROW_LEN AS DATA_LENGTH
         |FROM
         |  USER_TABLES,USER_TAB_COMMENTS
         |WHERE
         |  USER_TABLES.TABLE_NAME = '${table.toUpperCase()}'
         |AND
         |  USER_TABLES.TABLE_NAME = USER_TAB_COMMENTS.TABLE_NAME
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

  override def getSchema(tableIdentifier: TableIdentifier): String = {
    tableIdentifier.database.getOrElse(connection.getSchema)
  }

  override def getCatalog(tableIdentifier: TableIdentifier): String = {
    tableIdentifier.database.getOrElse(connection.getCatalog)
  }

  override def getMinMaxSql(tableName: String, column: String, condition: String): String = {
    s"""
      |SELECT
      |  (SELECT min($column) FROM $tableName WHERE $condition) minVal,
      |  (SELECT max($column) FROM $tableName WHERE $condition) maxVal
      |FROM
      |  dual
    """.stripMargin
  }
}
