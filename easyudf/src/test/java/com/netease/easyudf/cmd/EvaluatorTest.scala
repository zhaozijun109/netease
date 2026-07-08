package com.netease.easyudf.cmd

import com.holdenkarau.spark.testing.SharedSparkContext
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

import java.util.Random

class EvaluatorTest extends FunSuite with SharedSparkContext {

  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  def dataset(): DataFrame = {
    import spark.implicits._
    val rng = new Random()
    spark.sparkContext.parallelize((0 until 1000).map(i =>
      (rng.nextInt(10).toString, rng.nextDouble(), if (rng.nextDouble() > 0.5) 1.0 else 0.0)
    )).toDF("user_id", "pcvr", "cvr")
  }

  test("testApply") {
    dataset().createOrReplaceTempView("a")
    val args = EvaluatorArgs(input = "a", params = "[{\"type\":\"auc\", \"labelCol\":\"cvr\", \"rawPredictionCol\":\"pcvr\"},{\"type\":\"gauc\", \"userIdCol\":\"user_id\", \"labelCol\":\"cvr\", \"predictionCol\":\"pcvr\"}]")
    new Evaluator().apply(spark = spark, args = args)
  }

}
