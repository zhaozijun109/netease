package com.netease.music.da.transfer.oracle.reader

import com.netease.music.da.transfer.common.conf.Properties
import com.netease.music.da.transfer.jdbc.conf.JDBCProperties
import com.netease.music.da.transfer.jdbc.connection.DBConnection
import com.netease.music.da.transfer.jdbc.reader.JDBCReader
import com.netease.music.da.transfer.oracle.conf.OracleConstants
import com.netease.music.da.transfer.oracle.connection.OracleConnection
import org.apache.spark.sql.SparkSession

class OracleReader(@transient spark: SparkSession) extends JDBCReader(spark) {
  override def confPrefix: String = "spark.transmit.reader.oracle"

  override def addDefaultProperties(props: Properties): Unit = {
    super.addDefaultProperties(props)
    props.put(JDBCProperties.DRIVER.key, OracleConstants.DEFAULT_DRIVER)
    props.put(JDBCProperties.USE_QUOTE.key, OracleConstants.DEFAULT_USE_QUOTE.toString)
    props.put(JDBCProperties.FETCH_SIZE.key, OracleConstants.DEFAULT_FETCH_SIZE.toString)
  }

  override def createDBConnection(properties: Properties): DBConnection = {
    new OracleConnection(properties)
  }
}
