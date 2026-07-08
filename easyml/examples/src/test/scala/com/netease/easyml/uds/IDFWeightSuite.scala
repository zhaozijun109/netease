package com.netease.easyml.uds

import com.holdenkarau.spark.testing.SharedSparkContext
import com.netease.easyml.uds.examples.{IDFWeightArgs, IDFWeightUDS}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2020/12/29.
 */
class IDFWeightSuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  val input = "input"
  val output = "output"

  val inputCol = "tag"
  val minDf = 1

  def dataset(): DataFrame = {
    import spark.implicits._
    spark.sparkContext.parallelize(Seq(
      Array("i1", "u1"),
      Array("i2", "u1"),
      Array("i1", "u2"),
      Array("i3", "u2"),
      Array("i1", "u3"),
      Array("i3", "u3"),
      Array("i1", "u4"),
      Array("i2", "u4"),
      Array("i3", "u4")
    )
    ).toDF(inputCol)
  }

  test("idf") {
    val training = dataset()
    training.createOrReplaceTempView(input)
    val args = IDFWeightArgs(input = input, output = output, inputCol = inputCol, minDf = minDf)
    IDFWeightUDS.run(spark, args)
  }
}
