package com.netease.easyml.ml.metric.classification

import com.holdenkarau.spark.testing.SharedSparkContext
import org.apache.spark.sql.SparkSession
import org.scalatest.{FunSuite, Matchers}

/**
 * Created by linjiuning on 2020/7/16.
 */
class ConfusionMatrixSuite extends FunSuite with Matchers with SharedSparkContext {

  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()
  lazy val metric = new ConfusionMatrix()

  test("test_confusion_matrix_multiclass") {
    import spark.sqlContext.implicits._
    val y1 = Array(2, 0, 2, 2, 0, 1)
    val y2 = Array(0, 0, 2, 2, 0, 2)
    val df = sc.makeRDD(y1.zip(y2)).toDF(metric.getLabelCol, metric.getPredictionCol)

    var arr = metric.confusionMatrix(df)

    assert(arr.deep == Array(
      Array(2, 0, 0),
      Array(0, 0, 1),
      Array(1, 0, 2)).deep)

    metric.setNormalize("true")

    arr = metric.confusionMatrix(df)

    assert(arr.deep == Array(
      Array(1, 0, 0),
      Array(0, 0, 1),
      Array(1 / 3.0, 0, 2 / 3.0)).deep)

    metric.setNormalize("pred")

    arr = metric.confusionMatrix(df)

    assert(arr.deep == Array(
      Array(2 / 3.0, 0, 0),
      Array(0, 0, 1 / 3.0),
      Array(1 / 3.0, 0, 2 / 3.0)).deep)

    metric.setNormalize("all")

    arr = metric.confusionMatrix(df)

    assert(arr.deep == Array(
      Array(1 / 3.0, 0, 0),
      Array(0, 0, 1 / 6.0),
      Array(1 / 6.0, 0, 1 / 3.0)).deep)
  }

  test("test_confusion_matrix_binary") {
    import spark.sqlContext.implicits._
    val y1 = Array(0, 1, 0, 1)
    val y2 = Array(1, 1, 1, 0)
    val df = sc.makeRDD(y1.zip(y2)).toDF(metric.getLabelCol, metric.getPredictionCol)

    val arr = metric.confusionMatrix(df)

    assert(arr.deep == Array(
      Array(0, 2),
      Array(1, 1)).deep)
  }
}
