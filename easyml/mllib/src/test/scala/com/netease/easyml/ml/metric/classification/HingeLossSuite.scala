package com.netease.easyml.ml.metric.classification

import com.holdenkarau.spark.testing.SharedSparkContext
import org.apache.spark.sql.SparkSession
import org.scalatest.{FunSuite, Matchers}

/**
 * Created by linjiuning on 2020/7/16.
 */
class HingeLossSuite extends FunSuite with Matchers with SharedSparkContext{

  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()
  lazy val metric = new HingeLoss()

  test("test_hinge_loss_binary") {
    import spark.sqlContext.implicits._
    var df = sc.makeRDD(Seq(
      (-1, -8.5),
      (1, 0.5),
      (1, 1.5),
      (-1, -0.3))
    ).toDF(metric.getLabelCol, metric.getRawPredictionCol)

    var acc = metric.evaluate(df)
    acc shouldBe 0.3

    df = sc.makeRDD(Seq(
      (0, -8.5),
      (2, 0.5),
      (2, 1.5),
      (0, -0.3))
    ).toDF(metric.getLabelCol, metric.getRawPredictionCol)

    acc = metric.evaluate(df)
    acc shouldBe 0.3
  }

  test("test_hinge_loss_multiclass") {
    import spark.sqlContext.implicits._
    val df = sc.makeRDD(Seq(
      (0, Array(+0.36, -0.17, -0.58, -0.99)),
      (1, Array(-0.54, -0.37, -0.48, -0.58)),
      (2, Array(-1.45, -0.58, -0.38, -0.17)),
      (1, Array(-0.54, -0.38, -0.48, -0.58)),
      (3, Array(-2.36, -0.79, -0.27, +0.24)),
      (2, Array(-1.45, -0.58, -0.38, -0.17)))
    ).toDF(metric.getLabelCol, metric.getRawPredictionCol)

    val acc = metric.evaluate(df)
    acc shouldBe (0.86166 +- 1e-5)
  }
}
