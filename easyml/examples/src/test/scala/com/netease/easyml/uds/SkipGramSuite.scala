package com.netease.easyml.uds

import com.holdenkarau.spark.testing.SharedSparkContext
import com.netease.easyml.uds.examples.{SkipGramArgs, SkipGramUDS}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2020/12/28.
 */
class SkipGramSuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  val input = "input"
  val output = "output"

  val inputCol = "tokens"
  val window = 5
  val sample = 0
  val seed = 2020

  def dataset(): DataFrame = {
    import spark.implicits._
    spark.sparkContext.parallelize(Seq(
      Seq(1, 3, 4, 5, 10, 11).map(_.toDouble),
      Seq(4, 11, 44, 32, 88, 1).map(_.toDouble)
    )).toDF(inputCol)
  }


  test("sg") {
    val ds = dataset()
    ds.createOrReplaceTempView(input)
    val args = SkipGramArgs(input = input, output = output, inputCol = inputCol, window = window, sample = sample, seed = seed)
    SkipGramUDS.run(spark, args)
  }
}
