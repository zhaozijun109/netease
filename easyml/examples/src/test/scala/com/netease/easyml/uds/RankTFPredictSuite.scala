package com.netease.easyml.uds

import com.netease.easyml.uds.examples.{RankTFPredictArgs, RankTFPredictUDS}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2021/3/15.
 */
class RankTFPredictSuite extends FunSuite {
  lazy val spark: SparkSession = SparkSession.builder().master("local[4]").getOrCreate()

  val inputPath = "/Users/linjiuning/Downloads/rank_u2u2i_multi_shortterm_tfrecords"
  val input = "input"
  val output = "target/tmp/rank_predict"

  val recordType = "Example"
  val model = "/Users/linjiuning/Downloads/1645673537"

  def dataset(): DataFrame = {
    //    var df = spark.read.format("tfrecord").option("recordType", "Example").load(inputPath)
    var df = spark.read.parquet("/Users/linjiuning/Downloads/000000_0 (3)")
    //    df = df.sample(0.0001)
    //    Array("i_ra_ctr_1_day", "i_ra_ctr_3_day", "i_ra_ctr_7_day",
    //      "i_ra_cvr_1_day", "i_ra_cvr_3_day", "i_ra_cvr_7_day",
    //      "i_ra_cost_time_1_day", "i_ra_cost_time_3_day", "i_ra_cost_time_7_day")
    //      .foreach(name => df = df.withColumn(name, lit(0.0)))
    df.show(false)
    df
  }

  test("predict") {
    val training = dataset()
    println(training.columns.mkString(","))
    //    training.show(false)
    training.createOrReplaceTempView(input)
    val args = RankTFPredictArgs(input = input, output = output, model = model)
    RankTFPredictUDS.run(spark, args)
  }
}
