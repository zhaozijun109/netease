package com.netease.easyml.ml.sklearn.model_selection

import com.holdenkarau.spark.testing.SharedSparkContext
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2020/8/20.
 */
class RandomOverSamplerSuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  def makeDf(): DataFrame = {
    import spark.implicits._
    val y1 = Array.fill[Int](11)(0)
    val y2 = Array.fill[Int](51)(1)
    val y3 = Array.fill[Int](100)(2)
    sc.makeRDD(y1 ++ y2 ++ y3).toDF("label")
  }

  test("sample") {
    val df = makeDf()
    val sampler = new RandomOverSampler()
      .setRandomState(0)

    val counter = sampler.sample(df).rdd.map(row => (row.get(0), 1)).reduceByKey(_ + _).collectAsMap()
    println(counter)
  }

  test("sample 30") {
    val df = makeDf()
    val sampler = new RandomOverSampler()
      .setRandomState(0)
      .setSamplingNum(30)

    val counter = sampler.sample(df).rdd.map(row => (row.get(0), 1)).reduceByKey(_ + _).collectAsMap()
    println(counter)
  }
}
