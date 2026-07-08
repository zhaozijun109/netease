package com.netease.easyml.ml.metric.classification

import com.holdenkarau.spark.testing.SharedSparkContext
import org.apache.spark.sql.SparkSession
import org.scalatest.{FunSuite, Matchers}

/**
 * Created by linjiuning on 2020/7/16.
 */
class ZeroOneLossSuite extends FunSuite with Matchers with SharedSparkContext {

  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()
  lazy val metric = new ZeroOneLoss()

  test("test_zero_one_loss_multiclass") {
    import spark.sqlContext.implicits._
    val y1 = Array(1, 2, 3, 4)
    val y2 = Array(2, 2, 3, 4)
    val df = sc.makeRDD(y1.zip(y2)).toDF(metric.getLabelCol, metric.getPredictionCol)

    var acc = metric.evaluate(df)
    acc shouldBe 0.25

    metric.setNormalize(false)
    acc = metric.evaluate(df)
    acc shouldBe 1
  }

  test("test_zero_one_loss_multilabel") {
    import spark.sqlContext.implicits._
    val y1 = Seq(Array(0, 1, 1), Array(1, 0, 1))
    val y2 = Seq(Array(0, 0, 1), Array(1, 0, 1))

    var df = sc.makeRDD(y1.zip(y2)).toDF(metric.getLabelCol, metric.getPredictionCol)

    var acc = metric.evaluate(df)
    acc shouldBe 0.5

    df = sc.makeRDD(y1.zip(y1)).toDF(metric.getLabelCol, metric.getPredictionCol)

    acc = metric.evaluate(df)
    acc shouldBe 0
  }
}
