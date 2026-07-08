package com.netease.easyml.ml.sklearn.feature_selection

import com.holdenkarau.spark.testing.SharedSparkContext
import com.microsoft.azure.synapse.ml.lightgbm.LightGBMClassifier
import ml.dmlc.xgboost4j.scala.spark.XGBoostClassifier
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.feature.{CountVectorizer, StringIndexer}
import org.apache.spark.sql.functions.{array, col, format_string}
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2020/11/4.
 */
class SelectFromModelSuite extends FunSuite with SharedSparkContext {
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

    val model = new SelectFromModel()
      .setInputCols(Array("sepal length", "sepal width", "petal length", "petal width"))
      .setLabelCol("label")
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

    val model = new SelectFromModel()
      .setInputCols(Array("sepal length", "sepal width", "petal length", "petal width"))
      .setLabelCol("label")
      .setEstimator(booster)

    model.fit(training).show(false)
  }

  test("lr") {
    val training = dataset()

    val lr = new LogisticRegression()
      .setElasticNetParam(1.0)

    val model = new SelectFromModel()
      .setInputCols(Array("sepal length", "sepal width", "petal length", "petal width"))
      .setLabelCol("label")
      .setEstimator(lr)

    model.fit(training).show(false)
  }

  test("map") {
    var training = dataset()

    training = training.withColumn("sepal length tmp", array(format_string("%s", col("sepal length"))))

    val vectorizerModel = new CountVectorizer()
      .setInputCol("sepal length tmp")
      .setOutputCol("sepal length flat")
      .fit(training)

    training = vectorizerModel.transform(training).drop(vectorizerModel.getInputCol)

    val lr = new LogisticRegression()
      .setElasticNetParam(1.0)

    val model = new SelectFromModel()
      .setInputCols(Array("sepal length flat", "sepal length", "sepal width", "petal length", "petal width"))
      .setLabelCol("label")
      .setEstimator(lr)

    val fs = model.fit(training)
      .inverseMapIndex(vectorizerModel)
      .setNumTopFeatures(5)

    fs.show(false)
    fs.transform(training).show(false)
  }
}
