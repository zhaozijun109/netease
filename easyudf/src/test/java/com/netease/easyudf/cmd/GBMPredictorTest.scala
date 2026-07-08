package com.netease.easyudf.cmd

import com.holdenkarau.spark.testing.SharedSparkContext
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

class GBMPredictorTest extends FunSuite with SharedSparkContext {

  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  def dataset(): DataFrame = {
    val rawInput = spark.read.parquet("/Users/linjiuning/Downloads/000000_0 (10)")
    rawInput
  }

  test("testApply") {
    dataset().createOrReplaceTempView("a")
    val args = GBMPredictorArgs(input = "a", path = "/Users/linjiuning/Downloads/regression.model", params = "{\"type\": \"XGBoostRegressor\"}")
    new GBMPredictor().apply(spark = spark, args = args).show()
  }

}
