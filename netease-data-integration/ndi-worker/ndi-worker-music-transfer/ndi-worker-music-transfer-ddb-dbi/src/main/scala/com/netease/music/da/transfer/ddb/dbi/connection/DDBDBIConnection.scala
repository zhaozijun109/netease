package com.netease.music.da.transfer.ddb.dbi.connection

import java.sql.{Connection, DriverManager}

import com.netease.music.da.transfer.common.conf.Properties
import com.netease.music.da.transfer.ddb.dbi.conf.DDBDBIProperties
import com.netease.music.da.transfer.jdbc.connection.DBConnection
import com.netease.music.da.transfer.jdbc.vo.TableMeta
import org.apache.spark.sql.catalyst.TableIdentifier


class DDBDBIConnection(properties: Properties) extends DBConnection(properties) {

  override protected def createConnection(): Connection = {
    Class.forName("com.netease.backend.db.DBDriver")
    val urlSuffix = this.properties.getProperty(DDBDBIProperties.DDB_URL_SUFFIX)
    val finalUrl = addSuffix(url, urlSuffix)
    println(s"Create connection to $finalUrl")
    DriverManager.getConnection(finalUrl, user, password)
  }

  override def getTables(regex: String, database: Option[String]): List[String] = {
    throw new UnsupportedOperationException
  }

  override def getTableMetaData(tableIdentifier: TableIdentifier): TableMeta = {
    throw new UnsupportedOperationException
  }

  override def getSchema(tableIdentifier: TableIdentifier): String = {
    throw new UnsupportedOperationException
  }

  override def getCatalog(tableIdentifier: TableIdentifier): String = {
    throw new UnsupportedOperationException
  }
}
