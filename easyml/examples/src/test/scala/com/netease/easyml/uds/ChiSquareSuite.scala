package com.netease.easyml.uds

import com.holdenkarau.spark.testing.SharedSparkContext
import com.netease.easyml.uds.examples.{ChiSquareArgs, ChiSquareUDS}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.DoubleType
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2020/12/4.
 */
class ChiSquareSuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  val input = "input"
  val output = "output"

  val labelCol = "gender"
  val inputCol = "tx"
  val countCol = "cnt"
  val sign = true

  val corpus = "toy_dataset/query/gender.csv"

  def dataset(): DataFrame = {
    spark.read.option("header", "true")
      .csv(corpus)
      .withColumn(countCol, col(countCol).cast(DoubleType))
  }

  test("chi") {
    val training = dataset()
    training.createOrReplaceTempView(input)
    val args = ChiSquareArgs(input = input, output = output, labelCol = labelCol, inputCol = inputCol, countCol = countCol, sign = sign)
    ChiSquareUDS.run(spark, args)
  }
}
