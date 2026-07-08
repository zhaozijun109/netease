package com.netease.easyml.uds

import com.holdenkarau.spark.testing.SharedSparkContext
import com.netease.easyml.uds.examples.{TfidfFeatureMinerArgs, TfidfFeatureMinerUDS}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2020/12/29.
 */
class TfidfFeatureMinerSuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  val input = "input"
  val output = "output"

  val labelCol = "tag"
  val featureCol = "name"
  var model = ""
  val minDf = 20
  val oneVsRest = true
  val keepPositive = false

  val corpus = "/Users/linjiuning/workspace/git/netease/py_scripts/music/data/child_song_seg.csv"

  def dataset(): DataFrame = {
    spark.read.option("header", "true")
      .csv(corpus)
  }

  test("lr") {
    model = "lr"
    val training = dataset()
    training.createOrReplaceTempView(input)
    val args = TfidfFeatureMinerArgs(input = input, output = output, labelCol = labelCol, featureCol = featureCol, model = model,
      minDf = minDf, oneVsRest = oneVsRest, keepPositive = keepPositive)
    TfidfFeatureMinerUDS.run(spark, args)
  }

  test("nb") {
    model = "nb"
    val training = dataset()
    training.createOrReplaceTempView(input)
    val args = TfidfFeatureMinerArgs(input = input, output = output, labelCol = labelCol, featureCol = featureCol, model = model,
      minDf = minDf, oneVsRest = oneVsRest, keepPositive = keepPositive)
    TfidfFeatureMinerUDS.run(spark, args)
  }
}
