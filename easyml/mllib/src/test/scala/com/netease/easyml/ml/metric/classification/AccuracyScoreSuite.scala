package com.netease.easyml.ml.metric.classification

import com.holdenkarau.spark.testing.SharedSparkContext
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.SparkSession
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2020/7/16.
 */
class AccuracyScoreSuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()
  lazy val metric = new AccuracyScore()

  test("multiclass") {
    import spark.sqlContext.implicits._
    val df = sc.makeRDD(Seq(
      (0, 0),
      (2, 1),
      (1, 2),
      (3, 3))
    ).toDF(metric.getLabelCol, metric.getPredictionCol)

    var acc = metric.evaluate(df)
    assert(acc == 0.5)

    metric.setNormalize(false)

    acc = metric.evaluate(df)
    assert(acc == 2)
  }

  test("multilabel") {
    import spark.sqlContext.implicits._
    var df = sc.makeRDD(Seq(
      (Array(0, 1), Array(1, 1)),
      (Array(1, 1), Array(1, 1)))
    ).toDF(metric.getLabelCol, metric.getPredictionCol)

    var acc = metric.evaluate(df)
    assert(acc == 0.5)

    df = sc.makeRDD(Seq(
      (Vectors.dense(Array(0.0, 1.0)), Vectors.dense(1, 1)),
      (Vectors.dense(1, 1), Vectors.dense(1, 1)))
    ).toDF(metric.getLabelCol, metric.getPredictionCol)

    acc = metric.evaluate(df)
    assert(acc == 0.5)

    metric.setWeightCol("weight")
    df = sc.makeRDD(Seq(
      (Array(0, 1), Array(1, 1), 0.8),
      (Array(1, 1), Array(1, 1), 1.2))
    ).toDF(metric.getLabelCol, metric.getPredictionCol, metric.getWeightCol)

    acc = metric.evaluate(df)
    assert(acc == 0.6)
  }
}