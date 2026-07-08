package com.netease.easyml.ml.metric.classification

import com.holdenkarau.spark.testing.SharedSparkContext
import org.apache.spark.sql.SparkSession
import org.scalatest.{FunSuite, Matchers}

/**
 * Created by linjiuning on 2020/7/16.
 */
class HammingLossSuite extends FunSuite with Matchers with SharedSparkContext {

  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()
  lazy val metric = new HammingLoss()

  test("test_hamming_loss_multiclass") {
    import spark.sqlContext.implicits._
    val df = sc.makeRDD(Seq(
      (1, 2),
      (2, 2),
      (3, 3),
      (4, 4))
    ).toDF(metric.getLabelCol, metric.getPredictionCol)

    val acc = metric.evaluate(df)
    acc shouldBe 0.25
  }

  test("test_hamming_loss_multilabel") {
    import spark.sqlContext.implicits._
    val y1 = Seq(Array(0, 1, 1), Array(1, 0, 1))
    val y2 = Seq(Array(0, 0, 1), Array(1, 0, 1))
    val w = Seq(1, 3)
    var df = sc.makeRDD(y1.zip(y2)).toDF(metric.getLabelCol, metric.getPredictionCol)

    var acc = metric.evaluate(df)
    acc shouldBe (1.0 / 6)

    df = sc.makeRDD(y1.zip(y1)).toDF(metric.getLabelCol, metric.getPredictionCol)

    acc = metric.evaluate(df)
    acc shouldBe 0

    metric.setWeightCol("weight")
    df = sc.makeRDD((y1, y2, w).zipped.toSeq).toDF(metric.getLabelCol, metric.getPredictionCol, metric.getWeightCol)

    acc = metric.evaluate(df)
    acc shouldBe (1.0 / 12)
  }
}
