package com.netease.easyml.ml.metric.classification

import com.holdenkarau.spark.testing.SharedSparkContext
import org.apache.spark.sql.SparkSession
import org.scalatest.{FunSuite, Matchers}

/**
 * Created by linjiuning on 2020/7/16.
 */
class F1ScoreSuite extends FunSuite with Matchers with SharedSparkContext {

  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()
  lazy val metric = new F1Score()

  test("test_f1_multiclass") {
    import spark.sqlContext.implicits._
    val y1 = Array(0, 1, 2, 0, 1, 2)
    val y2 = Array(0, 2, 1, 0, 0, 1)
    val df = sc.makeRDD(y1.zip(y2)).toDF(metric.getLabelCol, metric.getPredictionCol)

    metric.setAverage("macro")
    var arr = metric.evaluate(df)

    arr shouldBe (0.26666 +- 1e-5)

    metric.setAverage("micro")
    arr = metric.evaluate(df)

    arr shouldBe (1.0 / 3)

    metric.setAverage("weighted")
    arr = metric.evaluate(df)

    arr shouldBe (0.26666 +- 1e-5)

    metric.setAverage("none")
    val params = metric.evaluateJson(df)

    val fscores = params.get(metric.shortName).asInstanceOf[Array[Double]]
    fscores(0) shouldBe 0.8
    fscores(1) shouldBe 0
    fscores(2) shouldBe 0
  }

  test("test_f1_binary") {
    import spark.sqlContext.implicits._
    val y1 = Array(0, 1, 1, 0, 1, 1)
    val y2 = Array(0, 1, 1, 0, 0, 1)
    val df = sc.makeRDD(y1.zip(y2)).toDF(metric.getLabelCol, metric.getPredictionCol)

    metric.setAverage("macro")
    var arr = metric.evaluate(df)

    arr shouldBe (0.82857 +- 1e-5)

    metric.setAverage("micro")
    arr = metric.evaluate(df)

    arr shouldBe (0.83333 +- 1e-5)

    metric.setAverage("weighted")
    arr = metric.evaluate(df)

    arr shouldBe (0.83809 +- 1e-5)

    metric.setAverage("none")
    val params = metric.evaluateJson(df)

    val fscores = params.get(metric.shortName).asInstanceOf[Array[Double]]
    fscores(0) shouldBe 0.8
    fscores(1) shouldBe (0.85714 +- 1e-5)
  }
}
