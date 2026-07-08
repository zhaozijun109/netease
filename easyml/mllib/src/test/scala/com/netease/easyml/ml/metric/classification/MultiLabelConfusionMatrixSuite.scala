package com.netease.easyml.ml.metric.classification

import com.holdenkarau.spark.testing.SharedSparkContext
import org.apache.spark.sql.SparkSession
import org.scalatest.{FunSuite, Matchers}

/**
 * Created by linjiuning on 2020/7/16.
 */
class MultiLabelConfusionMatrixSuite extends FunSuite with Matchers with SharedSparkContext {

  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()
  lazy val metric = new MultiLabelConfusionMatrix()

  test("test_confusion_matrix_multiclass") {
    import spark.sqlContext.implicits._
    val y1 = Array(2, 0, 2, 2, 0, 1)
    val y2 = Array(0, 0, 2, 2, 0, 2)
    val df = sc.makeRDD(y1.zip(y2)).toDF(metric.getLabelCol, metric.getPredictionCol)

    val arr = metric.confusionMatrix(df)

    assert(arr.deep == Array(
      Array(
        Array(3, 1),
        Array(0, 2)
      ),
      Array(
        Array(5, 0),
        Array(1, 0)
      ),
      Array(
        Array(2, 1),
        Array(1, 2)
      )).deep)
  }

  test("test_confusion_matrix_multilabel") {
    import spark.sqlContext.implicits._
    val y1 = Array(Array(1, 0, 1), Array(0, 1, 0))
    val y2 = Array(Array(1, 0, 0), Array(0, 1, 1))
    val df = sc.makeRDD(y1.zip(y2)).toDF(metric.getLabelCol, metric.getPredictionCol)

    val arr = metric.confusionMatrix(df)

    assert(arr.deep == Array(
      Array(
        Array(1, 0),
        Array(0, 1)
      ),
      Array(
        Array(1, 0),
        Array(0, 1)
      ),
      Array(
        Array(0, 1),
        Array(1, 0)
      )).deep)
  }
}
