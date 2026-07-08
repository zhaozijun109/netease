package com.netease.music.da.transfer.common.handler

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

abstract class AbstractHandler(@transient val spark: SparkSession) extends DataHandler {
  override val sparkConf: SparkConf = spark.sparkContext.getConf
}
