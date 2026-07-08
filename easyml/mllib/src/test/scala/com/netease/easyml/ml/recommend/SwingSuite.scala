package com.netease.easyml.ml.recommend

import com.holdenkarau.spark.testing.SharedSparkContext
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2020/12/31.
 */
class SwingSuite extends FunSuite with SharedSparkContext {
  lazy val spark = SparkSession.builder().getOrCreate()

  val itemCol = "item"
  val userCol = "user"

  def dataset(): DataFrame = {
    import spark.implicits._
    spark.sparkContext.parallelize(Seq(
      ("i1", "u1"),
      ("i2", "u1"),
      ("i1", "u2"),
      ("i3", "u2"),
      ("i1", "u3"),
      ("i3", "u3"),
      ("i1", "u4"),
      ("i2", "u4"),
      ("i3", "u4")
    )
    ).toDF(itemCol, userCol)
  }

  test("swing") {
    val df = dataset()
    new Swing()
      .setUserCol(userCol)
      .setItemCol(itemCol)
      .transform(df)
      .show(false)
  }

}
