package com.netease.lofter.data.common

import org.apache.spark.sql.SparkSession

object CheckPointTask {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Lofter CheckPointTask")
      .getOrCreate()

    val preConditionSql = spark.conf.getOption("spark.check.pre")
    val badConditionSql = spark.conf.getOption("spark.check.bad")

    if(preConditionSql.isEmpty && badConditionSql.isEmpty) {
      throw new RuntimeException("at least one of spark.check.pre or spark.check.bad option should be set")
    }

    val pre = if(preConditionSql.nonEmpty) spark.sql(preConditionSql.get).count() else 1
    val outliers = if(badConditionSql.nonEmpty) spark.sql(badConditionSql.get).count() else 0

    if(pre == 0 || outliers > 0) {
      System.exit(1)
    }

    spark.close()
  }
}
