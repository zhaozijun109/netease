package com.netease.easyudf.cmd

import com.holdenkarau.spark.testing.SharedSparkContext
import com.netease.easyml.common.util.Cmds.VARS
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

class FESliceWindowTest extends FunSuite with SharedSparkContext {

  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  def dataset(): DataFrame = {
    import spark.implicits._
    spark.sparkContext.parallelize(Seq(
      ("u1", "i1", "b1", "a1", 1, 0, 0, "2023-03-17"),
      ("u2", "i2", "b2", "a2", 1, 1, 1, "2023-03-17")
    )
    ).toDF("user_id", "item_id", "blog_id", "business_type",
      "exposed_count", "click_count", "return_gift_count", "day")
  }

  test("testApply") {
    val config = "fe_slice_window.yaml"
    dataset().createOrReplaceTempView("a")
    VARS = VARS + ("last_day" -> "2023-03-17")
    new FESliceWindow().apply(spark, FESliceWindowArgs(input = "a", config = config, day = "2023-03-23"))
      .createOrReplaceTempView("b")

    new FEFlattenTable().apply(spark, FEFlattenTableArgs(input = "b", primaryKeys = "user_id",
      config = config, entity = "user,user_x_business_type")).show()
  }

}
