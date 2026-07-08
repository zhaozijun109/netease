package com.netease.easyml.ml.metric.classification

import com.holdenkarau.spark.testing.SharedSparkContext
import org.apache.spark.sql.SparkSession
import org.scalatest.{FunSuite, Matchers}

/**
 * Created by linjiuning on 2020/7/16.
 */
class LogLossSuite extends FunSuite with Matchers with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()
  lazy val metric = new LogLoss()

  test("logloss") {
    import spark.sqlContext.implicits._
    var df = sc.makeRDD(Seq(
      (1, Array(.1, .9)),
      (0, Array(.9, .1)),
      (0, Array(.8, .2)),
      (1, Array(.35, .65)))
    ).toDF(metric.getLabelCol, metric.getRawPredictionCol)

    var acc = metric.evaluate(df)
    acc shouldBe (0.21616 +- 1e-5)

    df = sc.makeRDD(Seq(
      (1, Array(0.2, 0.7, 0.1)),
      (0, Array(0.6, 0.2, 0.2)),
      (2, Array(0.6, 0.1, 0.3)))
    ).toDF(metric.getLabelCol, metric.getRawPredictionCol)

    acc = metric.evaluate(df)
    acc shouldBe (0.69049 +- 1e-5)
  }
}
