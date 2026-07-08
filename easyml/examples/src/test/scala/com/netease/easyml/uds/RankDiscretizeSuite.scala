package com.netease.easyml.uds

import com.holdenkarau.spark.testing.SharedSparkContext
import com.netease.easyml.common.util.IOUtil
import com.netease.easyml.uds.examples.{RankDiscretizeArgs, RankDiscretizeUDS}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2021/3/15.
 */
class RankDiscretizeSuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  val basedir = "/Users/linjiuning/workspace/git/netease/nlp_zoo/tutorial/toy_dataset/estimator/rank"
  val inputPath = IOUtil.join(basedir, "data.parquet")
  val config = IOUtil.join(basedir, "features.tsv")
  val output = IOUtil.join(basedir, "/tmp/res_dir/boundaries.txt")

  val input = "input"

  val numBucket = 10
  val saveAsFile = true

  def dataset(): DataFrame = {
    spark.read.parquet(inputPath)
  }

  test("discretize") {
    val training = dataset()
    training.createOrReplaceTempView(input)

    val args = RankDiscretizeArgs(input = input, output = output, config = config, numBucket = numBucket, saveAsFile = saveAsFile)
    RankDiscretizeUDS.run(spark, args)
  }
}
