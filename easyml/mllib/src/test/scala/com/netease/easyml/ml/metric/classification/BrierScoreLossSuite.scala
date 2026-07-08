package com.netease.easyml.ml.metric.classification

import com.holdenkarau.spark.testing.SharedSparkContext
import org.apache.spark.sql.SparkSession
import org.scalatest.{FunSuite, Matchers}

/**
 * Created by linjiuning on 2020/7/16.
 */
class BrierScoreLossSuite extends FunSuite with Matchers with SharedSparkContext {

  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()
  lazy val metric = new BrierScoreLoss()

  test("test_brier_score_loss_binary") {
    import spark.sqlContext.implicits._
    val y1 = Array(0, 1, 1, 0, 1, 1)
    val y2 = Array(0.1, 0.8, 0.9, 0.3, 1.0, 0.95)
    var df = sc.makeRDD(y1.zip(y1)).toDF(metric.getLabelCol, metric.getRawPredictionCol)

    var acc = metric.evaluate(df)
    acc shouldBe 0.0

    df = sc.makeRDD(y1.zip(y2)).toDF(metric.getLabelCol, metric.getRawPredictionCol)
    acc = metric.evaluate(df)
    acc shouldBe (0.02541 +- 1e-5)
  }
}
