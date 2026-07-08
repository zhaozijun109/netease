package com.netease.easyml.ml

import com.holdenkarau.spark.testing.SharedSparkContext
import org.apache.spark.ml.classification_.FMClassifier
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{Dataset, Row, SparkSession}
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2020/12/16.
 */
class FMClassifierSuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  val path = "toy_dataset/fm/a9a"

  def dataset(): (Dataset[Row], Dataset[Row]) = {
    val rawInput = spark.read.format("libsvm").load(path)
      .withColumn("label", col("label").equalTo(1).cast("double"))
    rawInput.show()
    val Array(training, test) = rawInput.randomSplit(Array(0.8, 0.2), 123)
    (training, test)
  }

  test("fm") {
    val (training, test) = dataset()

    val fm = new FMClassifier()

    val model = fm.fit(training)

    val prediction = model.transform(test)
    val evaluator = new MulticlassClassificationEvaluator()
    val accuracy = evaluator.evaluate(prediction)

    println(accuracy)
  }
}
