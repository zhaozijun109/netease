package com.netease.easyml.uds

import com.holdenkarau.spark.testing.SharedSparkContext
import com.netease.easyml.uds.examples.{KMeansArgs, KMeansUDS}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

import scala.util.Random

/**
 * Created by linjiuning on 2020/11/11.
 */
class KMeansSuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  val input = "input"
  val output = "output"

  val outModelPath = "target/tmp/kmeans"
  val maxIter = 5
  val k = 5
  val mode = "train"
  val top = 2
  val dropInput = true

  def dataset(): DataFrame = {
    import spark.implicits._
    val rng = new Random(90)
    val rawInput = spark.sparkContext
      .parallelize((0 until 100).map(i => (i, Array(rng.nextDouble(), rng.nextDouble()))))
      .toDF("id", "features")

    rawInput
  }

  test("kmeans") {
    val training = dataset()
    training.createOrReplaceTempView(input)
    val args = KMeansArgs(input = input, output = output, modelPath = outModelPath, inputCol = "features", maxIter = maxIter, k = k, mode = mode,
      top = top, dropInput = dropInput)
    KMeansUDS.run(spark, args)
  }
}
