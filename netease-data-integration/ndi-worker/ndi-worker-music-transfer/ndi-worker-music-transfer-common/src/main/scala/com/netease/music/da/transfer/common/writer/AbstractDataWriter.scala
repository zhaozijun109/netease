package com.netease.music.da.transfer.common.writer

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

abstract class AbstractDataWriter(@transient spark: SparkSession) extends DataWriter {
  override val sparkConf: SparkConf = spark.sparkContext.getConf
}
