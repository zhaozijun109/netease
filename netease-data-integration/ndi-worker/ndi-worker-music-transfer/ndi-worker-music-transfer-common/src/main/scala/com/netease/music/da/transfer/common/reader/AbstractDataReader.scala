package com.netease.music.da.transfer.common.reader

import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}

abstract class AbstractDataReader(@transient spark: SparkSession) extends DataReader {
  override val sparkConf: SparkConf = spark.sparkContext.getConf

  def close(): Unit = {}
}
