package com.netease.easyml.uds

import com.holdenkarau.spark.testing.SharedSparkContext
import com.netease.easyml.uds.examples.{WordDiscoveryArgs, WordDiscoveryUDS}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2020/12/14.
 */
class WordDiscoverySuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  val input = "input"
  val output = "output"

  val inputCol = "value"
  val ngram = 4
  val minCount = 32
  val minPMI = "0;2;4;6"

  val corpus = "/Users/linjiuning/Downloads/tags.txt"

  def dataset(): DataFrame = {
    spark.read.text(corpus)
  }

  test("wd") {
    val training = dataset()
    training.createOrReplaceTempView(input)
    val args = WordDiscoveryArgs(input = input, output = output, inputCol = inputCol, ngram = ngram, minCount = minCount, minPMI = minPMI)
    WordDiscoveryUDS.run(spark, args)
  }
}
