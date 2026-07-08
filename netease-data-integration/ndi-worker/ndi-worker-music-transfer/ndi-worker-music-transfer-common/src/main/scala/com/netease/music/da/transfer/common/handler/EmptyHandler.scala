package com.netease.music.da.transfer.common.handler

import org.apache.spark.sql.{DataFrame, SparkSession}

class EmptyHandler(spark: SparkSession) extends AbstractHandler(spark) {
  override def handler(dataFrame: DataFrame): DataFrame = dataFrame

  override def confPrefix: String = "spark.transmit.handler.empty"
}
