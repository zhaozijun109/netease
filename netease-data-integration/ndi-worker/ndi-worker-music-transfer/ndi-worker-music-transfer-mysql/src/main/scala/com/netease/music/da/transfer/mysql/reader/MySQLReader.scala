package com.netease.music.da.transfer.mysql.reader

import com.netease.music.da.transfer.common.conf.Properties
import com.netease.music.da.transfer.jdbc.conf.JDBCProperties
import com.netease.music.da.transfer.jdbc.connection.DBConnection
import com.netease.music.da.transfer.jdbc.reader.JDBCReader
import com.netease.music.da.transfer.mysql.conf.MySQLConstants
import com.netease.music.da.transfer.mysql.connection.MySQLConnection
import org.apache.spark.sql.SparkSession

class MySQLReader(@transient spark: SparkSession) extends JDBCReader(spark) {
  override def confPrefix: String = "spark.transmit.reader.mysql"

  override def addDefaultProperties(props: Properties): Unit = {
    super.addDefaultProperties(props)
    props.put(JDBCProperties.DRIVER.key, MySQLConstants.DEFAULT_DRIVER)
  }

  override def createDBConnection(properties: Properties): DBConnection = {
    new MySQLConnection(properties)
  }
}
