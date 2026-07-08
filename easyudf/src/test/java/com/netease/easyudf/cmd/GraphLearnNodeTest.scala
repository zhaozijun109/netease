package com.netease.easyudf.cmd

import com.holdenkarau.spark.testing.SharedSparkContext
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

class GraphLearnNodeTest extends FunSuite with SharedSparkContext {

  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  def dataset(): DataFrame = {
    import spark.implicits._
    spark.sparkContext.parallelize(Seq(
      ("u1", "i1", "b1", 1, Seq("i1", "i2"), "2023-03-17"),
      ("u2", "i2", "b2", 1, null, "2023-03-17")
    )
    ).toDF("user_id", "item_id", "blog_id",
      "exposed_count", "sequence", "day")
  }

  test("testApply") {
    dataset().createOrReplaceTempView("a")
    new GraphLearnNode().apply(spark, GraphLearnNodeArgs(input = "a", path = "target/node", keys = "user_id,item_id,day", overwrite = true, hashAlg = "murmur", numPartitions = 10))
  }

}