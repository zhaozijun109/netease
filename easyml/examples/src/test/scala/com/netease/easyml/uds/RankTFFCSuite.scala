package com.netease.easyml.uds

import com.netease.easyml.common.util.IOUtil
import com.netease.easyml.uds.examples.{RankTFFCArgs, RankTFFCUDS}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2021/3/15.
 */
class RankTFFCSuite extends FunSuite {
  lazy val spark: SparkSession = SparkSession.builder().master("local[4]").getOrCreate()

  val basedir = "/Users/linjiuning/workspace/git/netease/nlp_zoo/tutorial/toy_dataset/estimator/rank"
  val inputPath = IOUtil.join(basedir, "data.parquet")
  val config = IOUtil.join(basedir, "features.tsv")
  val output = IOUtil.join(basedir, "/tmp/tfrecords_fc/train")

  //  val input = "input"
  val input = IOUtil.join(basedir, "/tmp/tfrecords/train")
  val recordType = "Example"

  val model = IOUtil.join(basedir, "tmp/fc_models/best/saved_models/1631693101")

  def dataset(): DataFrame = {
    spark.read.parquet(inputPath)
  }

  test("preprocess") {
    if (!IOUtil.isDirectory(input)) {
      val training = dataset()
      training.createOrReplaceTempView(input)
    }
    val args = RankTFFCArgs(input, output, config, model)
    RankTFFCUDS.run(spark, args)
  }
}
