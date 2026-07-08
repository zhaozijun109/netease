package com.netease.easyudf.cmd

import com.holdenkarau.spark.testing.SharedSparkContext
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

import java.util
import java.util.Random

class RsvdTest extends FunSuite with SharedSparkContext {

  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  def dataset(): DataFrame = {
    import spark.implicits._
    val matHeight = 2000 // 2K
    val matWidth = 2000 // 2K
    val numNonZeroEntries = 4000 // 4K

    // Generate a sparse random matrix as an input (doesn't have to be symmetric)
    sc.parallelize(0 until numNonZeroEntries).map {
      idx =>
        val random = new Random(42 + idx)
        (random.nextInt(matHeight), //row index
          random.nextInt(matWidth), //column index
          random.nextGaussian()) //entry value
    }.toDF("i", "j", "value")

  }

  test("run") {
    dataset().createOrReplaceTempView("a")
    val map = new util.HashMap[String, String]()
    map.put("input", "a")
    map.put("computeRightSingularVectors", "true")
    new Rsvd().apply(spark, map).show()
  }


}