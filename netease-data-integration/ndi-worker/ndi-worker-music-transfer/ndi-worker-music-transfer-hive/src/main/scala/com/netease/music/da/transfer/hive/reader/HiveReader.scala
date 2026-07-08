package com.netease.music.da.transfer.hive.reader

import com.netease.music.da.transfer.common.reader.AbstractDataReader
import com.netease.music.da.transfer.hive.conf.HiveProperties._
import org.apache.spark.sql.catalyst.TableIdentifier
import org.apache.spark.sql.{DataFrame, SparkSession}

class HiveReader(spark: SparkSession) extends AbstractDataReader(spark) {
  override def confPrefix: String = "spark.transmit.reader.hive"

  def isValueToCache(dataFrame: DataFrame): Boolean = {
    try {
      dataFrame.queryExecution.executedPlan.treeString.toLowerCase().contains(" exchange ")
    } catch {
      case e: Exception =>
        LOG.warn("Get executedPlan failed.", e)
        true
    }
  }

  override def read(): DataFrame = {
    val table = this.properties.getProperty(TABLE).get
    val tableIdentifier = TableIdentifier(table, this.properties.getProperty(DATABASE))
    val columns = this.properties.getProperty(COLUMNS).getOrElse("*")
    val condition =
      this.properties.getProperty(PARTITION).map(List(_)).getOrElse(List()) ++
        List[String](this.properties.getProperty(CONDITION).get)
    val sql =
      s"""
         |SELECT
         |  $columns
         |FROM
         |  ${tableIdentifier.quotedString}
         |WHERE
         |  ${condition.mkString("(", ") AND (", ")")}
     """.stripMargin
    LOG.info(s"Execute sql: $sql")
    spark.sql(sql)
  }
}
