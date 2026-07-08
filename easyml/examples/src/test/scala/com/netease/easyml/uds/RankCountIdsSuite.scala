package com.netease.easyml.uds

import com.holdenkarau.spark.testing.SharedSparkContext
import com.netease.easyml.common.util.IOUtil
import com.netease.easyml.uds.examples.{RankCountIdsArgs, RankCountIdsUDS}
import com.netease.easyml.uds.util.Constant.NULL
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2021/3/15.
 */
class RankCountIdsSuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  val basedir = "/Users/linjiuning/workspace/git/netease/nlp_zoo/tutorial/toy_dataset/estimator/rank"
  val inputPath = IOUtil.join(basedir, "data.parquet")
  val config = IOUtil.join(basedir, "features.tsv")
  val output = IOUtil.join(basedir, "/tmp/res_dir")

  val input = "input"
  val recordType = "Example"

  val startDay = NULL
  val endDay = NULL
  val minCount = 10

  def dataset(): DataFrame = {
    spark.read.parquet(inputPath)
  }

  test("count ids") {
    val training = dataset()
    training.createOrReplaceTempView(input)

    val args = RankCountIdsArgs(input, output, config, minCount = minCount)
    RankCountIdsUDS.run(spark, args)
  }
}
