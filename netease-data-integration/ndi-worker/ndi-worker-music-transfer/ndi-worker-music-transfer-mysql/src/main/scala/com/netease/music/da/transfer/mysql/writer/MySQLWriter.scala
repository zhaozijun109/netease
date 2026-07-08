package com.netease.music.da.transfer.mysql.writer

import com.netease.music.da.transfer.common.conf.Properties
import com.netease.music.da.transfer.jdbc.conf.JDBCProperties._
import com.netease.music.da.transfer.jdbc.connection.DBConnection
import com.netease.music.da.transfer.jdbc.writer.JDBCWriter
import com.netease.music.da.transfer.mysql.conf.MySQLConstants
import com.netease.music.da.transfer.mysql.conf.MySQLProperties._
import com.netease.music.da.transfer.mysql.connection.MySQLConnection
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.jdbc.JdbcDialect
import org.apache.spark.sql.types.StructType

class MySQLWriter(@transient spark: SparkSession) extends JDBCWriter(spark) {
  override def confPrefix: String = "spark.transmit.writer.mysql"

  override def addDefaultProperties(props: Properties): Unit = {
    super.addDefaultProperties(props)
    props.put(DRIVER.key, MySQLConstants.DEFAULT_DRIVER)
    props.put(SAVE_MODE.key, "insertInto")
  }

  override def createDBConnection(properties: Properties): DBConnection = {
    new MySQLConnection(properties)
  }

  override def getInsertStatement(table: String,
                                  rddSchema: StructType,
                                  tableSchema: Option[StructType],
                                  isCaseSensitive: Boolean,
                                  dialect: JdbcDialect): String =  {

    def getStatement(prefix: String): String = {
      val columns = if (tableSchema.isEmpty) {
        rddSchema.fields.map(x => dialect.quoteIdentifier(x.name)).mkString(",")
      } else {
        val columnNameEquality = if (isCaseSensitive) {
          org.apache.spark.sql.catalyst.analysis.caseSensitiveResolution
        } else {
          org.apache.spark.sql.catalyst.analysis.caseInsensitiveResolution
        }
        val tableColumnNames = tableSchema.get.fieldNames
        rddSchema.fields.map { col =>
          val normalizedName = tableColumnNames.find(f => columnNameEquality(f, col.name)).getOrElse {
            throw new Exception(s"""Column "${col.name}" not found in schema $tableSchema""")
          }
          dialect.quoteIdentifier(normalizedName)
        }.mkString(",")
      }
      val placeholders = rddSchema.fields.map(_ => "?").mkString(",")
      s"$prefix $table ($columns) VALUES ($placeholders)"
    }
    this.properties.getProperty(SAVE_MODE).get match {
      case "insertInto" =>
        getStatement("INSERT INTO")
      case "replaceInto" =>
        getStatement("REPLACE INTO")
      case "insertIgnore" =>
        getStatement("INSERT IGNORE")
      case value =>
        throw new IllegalArgumentException(s"Unsupported value `$value` for parameter `${SAVE_MODE.key}`.")
    }
  }
}
