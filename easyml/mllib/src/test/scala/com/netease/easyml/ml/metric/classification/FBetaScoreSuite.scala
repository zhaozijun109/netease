package com.netease.easyml.ml.metric.classification

import com.holdenkarau.spark.testing.SharedSparkContext
import org.apache.spark.sql.SparkSession
import org.scalatest.{FunSuite, Matchers}

/**
 * Created by linjiuning on 2020/7/16.
 */
class FBetaScoreSuite extends FunSuite with Matchers with SharedSparkContext {

  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()
  lazy val metric = new FBetaScore()

  test("test_fbeta_multiclass") {
    import spark.sqlContext.implicits._
    val y1 = Array(0, 1, 2, 0, 1, 2)
    val y2 = Array(0, 2, 1, 0, 0, 1)
    val df = sc.makeRDD(y1.zip(y2)).toDF(metric.getLabelCol, metric.getPredictionCol)

    metric.setBeta(0.5)
    metric.setAverage("macro")
    var arr = metric.evaluate(df)

    arr shouldBe (0.23809 +- 1e-5)

    metric.setBeta(0.5)
    metric.setAverage("micro")
    arr = metric.evaluate(df)

    arr shouldBe (1.0 / 3)

    metric.setBeta(0.5)
    metric.setAverage("weighted")
    arr = metric.evaluate(df)

    arr shouldBe (0.23809 +- 1e-5)

    metric.setBeta(0.5)
    metric.setAverage("none")
    val params = metric.evaluateJson(df)

    val fscores = params.get(metric.shortName).asInstanceOf[Array[Double]]
    fscores(0) shouldBe (0.71428 +- 1e-5)
    fscores(1) shouldBe 0
    fscores(2) shouldBe 0
  }

}
