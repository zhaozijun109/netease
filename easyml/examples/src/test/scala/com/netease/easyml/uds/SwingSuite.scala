package com.netease.easyml.uds

import com.holdenkarau.spark.testing.SharedSparkContext
import com.netease.easyml.uds.examples.{SwingArgs, SwingUDS}
import com.netease.easyml.uds.util.Constant._
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2021/6/26.
 */
class SwingSuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  val actionTable = "actionTable"
  val targetTable = "target"
  val restrictTable = "restrict"
  val output = "@output"
  val k = 0

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
    ).toDF(ITEM, USER)
  }

  def target(): DataFrame = {
    import spark.implicits._
    spark.sparkContext.parallelize(Seq(
      "i1", "i2"
    )
    ).toDF(ITEM)
  }

  def restrict(): DataFrame = {
    import spark.implicits._
    spark.sparkContext.parallelize(Seq(
      ("u1", "i1", "i2"),
      ("u4", "i1", "i2")
    )
    ).toDF(USER, ITEM + "_src", ITEM + "_dst")
  }

  test("swing") {
    val ds = dataset()
    ds.createOrReplaceTempView(actionTable)
    if (!targetTable.equals(NULL)) {
      val ts = target()
      ts.createOrReplaceTempView(targetTable)
    }
    if (!restrictTable.equals(NULL)) {
      val ts = restrict()
      ts.createOrReplaceTempView(restrictTable)
    }
    val args = SwingArgs(actionTable = actionTable, targetTable = targetTable, output = output, k = k)
    SwingUDS.run(spark, args)
  }
}
