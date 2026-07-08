package com.netease.easyudf.cmd

import com.holdenkarau.spark.testing.SharedSparkContext
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

class EstimatorTest extends FunSuite with SharedSparkContext {

  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  def dataset(): DataFrame = {
    val schema = new StructType(Array(
      StructField("sepal_length", DoubleType, true),
      StructField("sepal_width", DoubleType, true),
      StructField("petal_length", DoubleType, true),
      StructField("petal_width", DoubleType, true),
      StructField("label", StringType, true)))
    val rawInput = spark.read.schema(schema).csv("/Users/linjiuning/workspace/git/netease/easyml/examples/toy_dataset/iris/iris.data")
    rawInput
  }

  test("testApply") {
    dataset().createOrReplaceTempView("a")
    new LibsvmEncoder().apply(spark = spark, args = LibsvmEncoderArgs(input = "a", exclude = "label")).createOrReplaceTempView("a")
    new TrainTestSplit().run(spark, TrainTestSplitArgs(input = "a", test = "eval"))
    spark.table("eval").drop("label").createOrReplaceTempView("test")
    var args = EstimatorArgs(input = "train", path = "target/xgboost", eval = "eval", weightCol = null, libsvm = true, include = "features", printFeatureImportance = true, normalizer = "l2",
      params = "{\"type\": \"LightGBMClassifier\", \"objective\": \"multiclass\"}", metric = "[{\"type\": \"accuracy\"}, {\"type\": \"auc\"}, {\"type\": \"fscore\", \"beta\": 2}]")
    new Estimator().apply(spark = spark, args = args)

    args = EstimatorArgs(input = "eval", path = "target/xgboost", mode = "eval", libsvm = true, include = "features")
    new Estimator().apply(spark = spark, args = args)

    args = EstimatorArgs(input = "test", path = "target/xgboost", mode = "predict", libsvm = true, include = "features")
    new Estimator().apply(spark = spark, args = args).show()
  }

}
