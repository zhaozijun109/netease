package com.netease.easyml.ml

import com.holdenkarau.spark.testing.SharedSparkContext
import com.microsoft.azure.synapse.ml.lightgbm.LightGBMClassifier
import com.netease.easyml.common.util.SparkUtil
import ml.dmlc.xgboost4j.scala.spark.{TrackerConf, XGBoostClassifier}
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature.{StringIndexer, VectorAssembler}
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.apache.spark.sql.{Dataset, Row, SparkSession}
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2020/8/24.
 */
class GBDTSuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  def dataset(): (Dataset[Row], Dataset[Row]) = {
    val schema = new StructType(Array(
      StructField("sepal length", DoubleType, true),
      StructField("sepal width", DoubleType, true),
      StructField("petal length", DoubleType, true),
      StructField("petal width", DoubleType, true),
      StructField("class", StringType, true)))
    val rawInput = spark.read.schema(schema).csv("examples/toy_dataset/iris/iris.data")

    val Array(training, test) = rawInput.randomSplit(Array(0.8, 0.2), 123)
    (training, test)
  }

  test("xgboost") {
    val (training, test) = dataset()

    val assembler = new VectorAssembler().
      setInputCols(Array("sepal length", "sepal width", "petal length", "petal width")).
      setOutputCol("features")

    val stringIndexer = new StringIndexer().
      setInputCol("class").
      setOutputCol("label")

    val booster = new XGBoostClassifier()
      .setEta(0.1f)
      .setMissing(-999)
      .setObjective("multi:softprob")
      .setNumClass(3)
      .setNumRound(100)
      .setNumWorkers(SparkUtil.getParallelism(spark.sparkContext.getConf))
      .setFeaturesCol("features")
      .setLabelCol("label")

    booster.set(booster.trackerConf, TrackerConf(0, "scala"))

    val pipeline = new Pipeline()
      .setStages(Array(assembler, stringIndexer, booster))
    val model = pipeline.fit(training)

    val prediction = model.transform(test)
    val evaluator = new MulticlassClassificationEvaluator()
    val accuracy = evaluator.evaluate(prediction)

    println(accuracy)
  }

  test("lightgbm") {
    val (training, test) = dataset()

    val assembler = new VectorAssembler().
      setInputCols(Array("sepal length", "sepal width", "petal length", "petal width")).
      setOutputCol("features")

    val stringIndexer = new StringIndexer().
      setInputCol("class").
      setOutputCol("label")

    val booster = new LightGBMClassifier()
      .setLearningRate(0.1)
      .setLambdaL1(0.1)
      .setLambdaL2(0.2)
      .setMaxDepth(4)
      .setObjective("multiclass")
      .setFeaturesCol("features")
      .setLabelCol("label")

    val pipeline = new Pipeline()
      .setStages(Array(assembler, stringIndexer, booster))
    val model = pipeline.fit(training)

    val prediction = model.transform(test)
    val evaluator = new MulticlassClassificationEvaluator()
    var accuracy = evaluator.evaluate(prediction)

    println(accuracy)
  }
}
