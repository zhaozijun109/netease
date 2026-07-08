package com.netease.easyml.ml.sklearn.feature_selection

import com.holdenkarau.spark.testing.SharedSparkContext
import com.microsoft.azure.synapse.ml.lightgbm.LightGBMClassifier
import com.netease.easyml.ml.metric.classification.AccuracyScore
import ml.dmlc.xgboost4j.scala.spark.XGBoostClassifier
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.feature.StringIndexer
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2020/11/4.
 */
class RFECVSuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  def dataset(): DataFrame = {
    val schema = new StructType(Array(
      StructField("sepal length", DoubleType, true),
      StructField("sepal width", DoubleType, true),
      StructField("petal length", DoubleType, true),
      StructField("petal width", DoubleType, true),
      StructField("class", StringType, true)))
    val rawInput = spark.read.schema(schema).csv("examples/toy_dataset/iris/iris.data")

    val stringIndexer = new StringIndexer()
      .setInputCol("class")
      .setOutputCol("label")
    stringIndexer.fit(rawInput)
      .transform(rawInput)
  }

  test("xgb") {
    val training = dataset()

    val booster = new XGBoostClassifier()
      .setEta(0.1f)
      .setMissing(-999)
      .setObjective("multi:softprob")
      .setNumClass(3)
      .setNumRound(100)
      .setNumWorkers(2)

    val model = new RFECV()
      .setInputCols(Array("sepal length", "sepal width", "petal length", "petal width"))
      .setLabelCol("label")
      .setEvaluator(new AccuracyScore())
      .setEstimator(booster)

    model.fit(training).show(false)
  }

  test("lightgbm") {
    val training = dataset()

    val booster = new LightGBMClassifier()
      .setLearningRate(0.1)
      .setLambdaL1(0.1)
      .setLambdaL2(0.2)
      .setMaxDepth(4)
      .setObjective("multiclass")

    val model = new RFECV()
      .setInputCols(Array("sepal length", "sepal width", "petal length", "petal width"))
      .setLabelCol("label")
      .setEvaluator(new AccuracyScore())
      .setEstimator(booster)

    model.fit(training).show(false)
  }

  test("lr") {
    val training = dataset()

    val lr = new LogisticRegression()
      .setElasticNetParam(1.0)

    val model = new RFECV()
      .setInputCols(Array("sepal length", "sepal width", "petal length", "petal width"))
      .setLabelCol("label")
      .setEvaluator(new AccuracyScore())
      .setEstimator(lr)

    model.fit(training).show(false)
  }
}