package com.netease.easyml.uds

import com.holdenkarau.spark.testing.SharedSparkContext
import com.netease.easyml.uds.examples.{RecallNegativeSampleArgs, RecallNegativeSampleUDS}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2021/1/21.
 */
class RecallNegativeSampleSuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  val input = "input"
  val output = "output"

  val inputCol = "tokens"
  val window = 5
  val sample = 0
  val seed = 2020
  val iter = 3
  val negative = 5

  def dataset(): DataFrame = {
    import spark.implicits._
    spark.sparkContext.parallelize(Seq(
      Seq(1, 3, 4, 5, 10, 11).map(_.toDouble),
      Seq(4, 11, 44, 32, 88, 1).map(_.toDouble)
    )).toDF(inputCol)
  }

  test("sample") {
    val ds = dataset()
    ds.createOrReplaceTempView(input)
    val args = RecallNegativeSampleArgs(input = input, output = output, inputCol = inputCol, sample = sample, seed = seed, iter = iter, negative = negative)
    RecallNegativeSampleUDS.run(spark, args)
  }
}
