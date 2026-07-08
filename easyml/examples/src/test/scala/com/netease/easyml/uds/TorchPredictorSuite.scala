package com.netease.easyml.uds

import com.holdenkarau.spark.testing.SharedSparkContext
import com.netease.easyml.uds.examples.{TorchPredictorArgs, TorchPredictorUDS}
import org.apache.spark.sql.catalyst.expressions.GenericRow
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2021/2/18.
 */
class TorchPredictorSuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  val input = "input"
  val output = "output"

  val inputCols = "age;gender"
  val outputCols = "probability"
  val model = "/Users/linjiuning/workspace/git/netease/easyml_test/target/cross_domain_json/model/best.jit"
  val batchSize = 32
  val dropInputs = false

  def dataset(): DataFrame = {
    val testRows: Array[Row] = Array(
      new GenericRow(Array[Any](11, 0, 1)),
      new GenericRow(Array[Any](21, 1, 1)))
    val schema = StructType(List(StructField("id", IntegerType),
      StructField("age", IntegerType),
      StructField("gender", IntegerType)
    ))

    val rows = (0 until 1000).flatMap(_ => testRows)

    val rdd = spark.sparkContext.parallelize(rows)

    spark.createDataFrame(rdd, schema)
  }

  test("predict") {
    val training = dataset()
    training.show(false)
    training.createOrReplaceTempView(input)
    val args = TorchPredictorArgs(input = input, output = output, inputCols = inputCols, outputCols = outputCols,
      model = model, batchSize = batchSize, dropInputs = dropInputs)
    TorchPredictorUDS.run(spark, args)
  }
}
