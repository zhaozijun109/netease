package com.netease.easyml.uds

import com.holdenkarau.spark.testing.SharedSparkContext
import com.netease.easyml.common.util.IOUtil
import com.netease.easyml.uds.examples.{RandomWalkArgs, RandomWalkUDS}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2020/11/25.
 */
class RandomWalkSuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  val dataPath = "examples/toy_dataset/graph/graph.csv"

  val inputTable = "rw_tmp"
  val outDir = "target/tmp/random_walk"
  val numWalk = 10
  val walkLength = 10
  val p = 1.0
  val q = 1.0

  def dataset(): DataFrame = {
    val rawInput = spark.read.option("header", "true").csv(dataPath)

    rawInput
  }

  test("walk") {
    if (IOUtil.exists(outDir)) {
      IOUtil.delete(outDir)
    }
    val training = dataset()
    training.createOrReplaceTempView(inputTable)

    val args = RandomWalkArgs(input = inputTable, output = outDir, walkLength = walkLength, numWalks = numWalk, p = p, q = q)
    RandomWalkUDS.run(spark, args)
  }
}
