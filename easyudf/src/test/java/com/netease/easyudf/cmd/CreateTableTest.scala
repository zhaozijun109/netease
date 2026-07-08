package com.netease.easyudf.cmd

import com.holdenkarau.spark.testing.SharedSparkContext
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

import java.util

class CreateTableTest extends FunSuite with SharedSparkContext {

  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()


  def dataset(): DataFrame = {
    import spark.implicits._
    spark.sparkContext.parallelize(Seq(
      ("i1", "aa", 1),
      ("i2", "bb", 2)
    )
    ).toDF("f0", "f1", "f2")
  }

  test("testApply") {
    dataset().createOrReplaceTempView("a")
    val params = new util.HashMap[String, String]()
    params.put("input", "a")
    params.put("output", "b")
    params.put("partitions", "day,type")

    new CreateTable().apply(spark, params)
  }

}
