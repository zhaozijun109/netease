package com.netease.easyudf.cmd

import com.holdenkarau.spark.testing.SharedSparkContext
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

import java.util

class RtrsSerializerTest extends FunSuite with SharedSparkContext {

  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  val pk = "item_id"
  val table = "item_fea"
  val path = "target/rtrs_serializer"

  def dataset(): DataFrame = {
    import spark.implicits._
    spark.sparkContext.parallelize(Seq(
      ("i1", "aa", 1),
      ("i2", "bb", 2)
    )
    ).toDF(pk, "f1", "f2")
  }

  test("testApply") {
    dataset().createOrReplaceTempView("a")
    val params = new util.HashMap[String, String]()
    params.put("input", "a")
    params.put("pk", pk)
    params.put("table", table)
    params.put("path", path)

    new RtrsSerializer().apply(spark, params)
  }

}
