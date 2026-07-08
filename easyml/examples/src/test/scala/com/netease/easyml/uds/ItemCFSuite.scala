package com.netease.easyml.uds

import com.netease.easyml.ml.recommend.ItemCF
import com.netease.easyml.uds.examples.{ItemCFArgs, ItemCFUDS}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2021/6/26.
 */
class ItemCFSuite extends FunSuite {
  lazy val spark: SparkSession = SparkSession.builder()
    .master("local[4]")
    .config("spark.ps.instances", "1")
    .config("spark.ps.cores", "1")
    .config("spark.ps.jars", "")
    .getOrCreate()

  val actionTable = "actionTable"
  val targetTable = "target"
  val output = "output"
  val itemCol = "item"
  val userCol = "user"
  val alpha = 1.0
  val minSameUser = 0
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
    ).toDF(itemCol, userCol)
  }

  def target(): DataFrame = {
    import spark.implicits._
    spark.sparkContext.parallelize(Seq(
      "i1", "i2"
    )
    ).toDF(itemCol)
  }

  test("itemCF") {
    val ds = dataset()
    ds.createOrReplaceTempView(actionTable)
    if (!targetTable.equals("null")) {
      val ts = target()
      ts.createOrReplaceTempView(targetTable)
    }
    val args = ItemCFArgs(actionTable = actionTable, targetTable = targetTable, output = output, userCol = userCol, itemCol = itemCol,
      alpha = alpha, minSameUser = minSameUser, k = k, persist = false)
    ItemCFUDS.run(spark, args)
  }

  test("itemCF transform") {
    val ds = dataset()
    new ItemCF().setUserCol(userCol).setItemCol(itemCol).setAlpha(alpha).setMinSameUser(minSameUser).transform(ds)
      .show()
  }

}
