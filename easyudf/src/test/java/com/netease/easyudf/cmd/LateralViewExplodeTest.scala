package com.netease.easyudf.cmd

import com.holdenkarau.spark.testing.SharedSparkContext
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

import java.util

class LateralViewExplodeTest extends FunSuite with SharedSparkContext {

  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()


  def dataset(): DataFrame = {
    import spark.implicits._
    spark.sparkContext.parallelize(Seq(
      ("i1", Seq("a1", "a2"), Seq(1, 2)),
      ("i2", Seq("b1", "b2"), Seq(2, 3))
    )
    ).toDF("f0", "f1", "f2")
  }

  test("testApply") {
    dataset().createOrReplaceTempView("a")
    val params = new util.HashMap[String, String]()
    params.put("input", "a")
    params.put("keys", "f1,f2")

    new LateralViewExplode().apply(spark, params).show()
  }

}
