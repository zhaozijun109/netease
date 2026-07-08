package com.netease.easyudf.cmd

import com.holdenkarau.spark.testing.SharedSparkContext
import org.apache.spark.sql.SparkSession
import org.scalatest.FunSuite

import java.util
import java.util.Random

class KMeansTest extends FunSuite with SharedSparkContext {

  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  def dataset(): Unit = {
    import spark.implicits._
    val rng = new Random()
    spark.sparkContext.parallelize((0 until 1000).map(i =>
      (s"i$i", (0 until 128).map(_ => rng.nextDouble()))
    )).toDF("id", "vector")
      .createOrReplaceTempView("a")
  }

  test("testApply") {
    dataset()
    new KMeans().apply(spark, KMeansArgs(input = "a", k = 2, top = 3)).createOrReplaceTempView("label")
    new KMeans().apply(spark, KMeansArgs(input = "a", center = "kmeans_center")).show()
  }

}