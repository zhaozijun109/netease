package com.netease.music.da.transfer.ddb.qs.writer

import com.netease.music.da.transfer.common.conf.Properties
import com.netease.music.da.transfer.jdbc.conf.JDBCProperties
import com.netease.music.da.transfer.mysql.writer.MySQLWriter
import org.apache.spark.sql.SparkSession

class DDBQSWriter(@transient spark: SparkSession) extends MySQLWriter(spark) {
  override def confPrefix: String = "spark.transmit.writer.ddb.qs"

  override def addDefaultProperties(properties: Properties) {
    super.addDefaultProperties(properties)
    properties.put(JDBCProperties.NEED_REPARTITION.key, "true")
    properties.put(JDBCProperties.REPARTITION_NUM.key, "1")
  }
}
