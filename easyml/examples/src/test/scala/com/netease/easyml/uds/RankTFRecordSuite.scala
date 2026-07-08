package com.netease.easyml.uds

import com.holdenkarau.spark.testing.SharedSparkContext
import com.netease.easyml.common.util.IOUtil
import com.netease.easyml.uds.examples.{RankTFRecordArgs, RankTFRecordUDS}
import com.netease.easyml.uds.util.Constant.NULL
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.{FunSuite, stats}

/**
 * Created by linjiuning on 2021/3/15.
 */
class RankTFRecordSuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  val basedir = "/Users/linjiuning/workspace/git/netease/nlp_zoo/tutorial/toy_dataset/estimator/rank"
  val inputPath = IOUtil.join(basedir, "data.parquet")
  val config = IOUtil.join(basedir, "features.tsv")
  val output = IOUtil.join(basedir, "/tmp/tfrecords")

  val input = "input"
  val recordType = "Example"

  val splitTrainEval = true
  val keepFeaOnly = false
  val gzip = true

  def dataset(): DataFrame = {
    spark.read.parquet(inputPath)
  }

  test("write") {
    val training = dataset()
    training.createOrReplaceTempView(input)

    val args = RankTFRecordArgs(input = input, output = output, config = config,
      recordType = recordType, splitTrainEval = splitTrainEval, procFeaOnly = keepFeaOnly,
      gzip = gzip)
    RankTFRecordUDS.run(spark, args)
  }
}
