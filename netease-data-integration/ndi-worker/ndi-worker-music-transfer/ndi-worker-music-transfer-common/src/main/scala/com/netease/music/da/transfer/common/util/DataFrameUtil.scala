package com.netease.music.da.transfer.common.util

import java.util.UUID

import com.netease.music.da.transfer.common.log.LogTrait
import org.apache.spark.sql.{DataFrame, SparkSession}

object DataFrameUtil extends LogTrait {

  def transform(spark: SparkSession, data: DataFrame, columns: String, condition: String): DataFrame = {
    val tmpView = createTempView(data)
    LOG.info("crate temp view: " + tmpView)
    val sql =
      s"""
         |SELECT
         |  $columns
         |FROM $tmpView
         |WHERE $condition
      """.stripMargin

    spark.sql(sql)
  }

  def createTempView(dataFrame: DataFrame): String = {
    val tmpView = "tmp_" + UUID.randomUUID().toString.replaceAll("-", "")
    dataFrame.createOrReplaceTempView(tmpView)
    tmpView
  }
}
