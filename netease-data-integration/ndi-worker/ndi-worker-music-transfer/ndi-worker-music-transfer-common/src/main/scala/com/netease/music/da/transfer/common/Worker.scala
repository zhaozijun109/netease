package com.netease.music.da.transfer.common

import com.netease.music.da.transfer.common.handler.DataHandler
import com.netease.music.da.transfer.common.log.LogTrait
import com.netease.music.da.transfer.common.metrics.Metrics
import com.netease.music.da.transfer.common.reader.DataReader
import com.netease.music.da.transfer.common.writer.DataWriter
import org.apache.spark.sql.SparkSession

object Worker extends LogTrait {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().getOrCreate()
    Metrics.addListener(spark.sparkContext)
    val reader = DataReader(spark)
    val handler = DataHandler(spark)
    val writer = DataWriter(spark)
    writer.write(handler.handler(reader.read()))
    spark.stop()
  }
}
