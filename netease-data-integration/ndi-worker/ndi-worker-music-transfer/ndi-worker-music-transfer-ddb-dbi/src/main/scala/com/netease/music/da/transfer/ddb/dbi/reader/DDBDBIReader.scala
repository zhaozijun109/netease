package com.netease.music.da.transfer.ddb.dbi.reader

import com.netease.backend.db.{DBConnection => DDBConnection}
import com.netease.music.da.transfer.common.conf.Properties
import com.netease.music.da.transfer.ddb.dbi.connection.DDBDBIConnection
import com.netease.music.da.transfer.jdbc.conf.JDBCProperties
import com.netease.music.da.transfer.jdbc.connection.DBConnection
import com.netease.music.da.transfer.jdbc.reader.JDBCReader
import com.netease.music.da.transfer.mysql.connection.MySQLConnection
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.TableIdentifier

class DDBDBIReader(@transient spark: SparkSession) extends JDBCReader(spark) {
  override def confPrefix: String = "spark.transmit.reader.ddb.dbi"

  override def createDBConnection(properties: Properties): DBConnection = {
    new MySQLConnection(properties)
  }

  override def tableProperties: List[Properties] = {
    val table = this.properties.getProperty(JDBCProperties.TABLE).get
    val ddbDBIConnection = new DDBDBIConnection(this.properties)
    val connection = ddbDBIConnection.connection.asInstanceOf[DDBConnection]
    val dbns = connection.getDbnUrlByTable(connection.getDdbNameStr, table).toList.distinct
    dbns.filter { dbn =>
      val properties =
        new Properties()
          .putProperties(this.properties)
          .put(JDBCProperties.URL.key, dbn)
      val mysqlConnection = new MySQLConnection(properties)
      val skip = mysqlConnection.getTableMetaData(TableIdentifier(table, Option.empty)) == null
      mysqlConnection.closeQuietly()
      if (skip) {
        LOG.warn(s"Skip dbn $dbn")
      } else {
        LOG.info(s"Find dbn $dbn")
      }
      !skip
    }
    ddbDBIConnection.closeQuietly()
    dbns.map { dbn =>
      Properties()
        .putProperties(this.properties)
        .put(JDBCProperties.URL.key, dbn)
    }
  }
}