package com.netease.easyml.ml.metric.regression

import com.holdenkarau.spark.testing.SharedSparkContext
import org.apache.spark.sql.SparkSession
import org.scalatest.{FunSuite, Matchers}

/**
 * Created by linjiuning on 2020/7/16.
 */
class MaxErrorSuite extends FunSuite with Matchers with SharedSparkContext {

  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()
  lazy val metric = new MaxError()

  test("test_max_error") {
    import spark.sqlContext.implicits._
    val y1 = Array(3, 2, 7, 1)
    val y2 = Array(4, 2, 7, 1)
    val df = sc.makeRDD(y1.zip(y2)).toDF(metric.getLabelCol, metric.getPredictionCol)

    val acc = metric.evaluate(df)
    acc shouldBe 1
  }

}
