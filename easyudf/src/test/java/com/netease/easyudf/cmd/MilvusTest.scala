package com.netease.easyudf.cmd

import com.holdenkarau.spark.testing.SharedSparkContext
import com.netease.easyudf.udf.util.MilvusUtil
import org.apache.spark.sql.functions.{array, col}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

import java.util.Random

class MilvusTest extends FunSuite with SharedSparkContext {
  MilvusUtil.setEnv("dev")

  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  def dataset(): DataFrame = {
    import spark.implicits._
    val rng = new Random(1234)
    spark.sparkContext.parallelize((0 until 100).map(i => {
      (rng.nextInt(10).toString, "item_" + i, i, (0 until 128).map(_ => rng.nextDouble))
    }
    )).toDF("pt", "item_id", "post_id", "vector")

  }

  test("build") {
    dataset().createOrReplaceTempView("a")
    val args = MilvusInsertArgs(input = "a", collectionName = "test_spark", primaryCol = "post_id", sleep = 1, overwrite = true)
    new MilvusInsert().run(spark, args)
  }

  test("search") {
    dataset().createOrReplaceTempView("a")
    val args = MilvusSearchArgs(input = "a", collectionName = "test_spark", primaryCol = "post_id", fieldCol = "item_id")
    new MilvusSearch().apply(spark, args).show()
  }

  test("search filter_not") {
    dataset().withColumn("not", array(col("item_id"))).createOrReplaceTempView("a")
    val args = MilvusSearchArgs(input = "a", collectionName = "test_spark", primaryCol = "item_id", filterNotInCol = "not")
    new MilvusSearch().apply(spark, args).show()
  }

  test("build partition") {
    dataset().createOrReplaceTempView("a")
    val args = MilvusInsertArgs(input = "a", collectionName = "test_spark", primaryCol = "item_id", partitionCol = "pt", sleep = 1, overwrite = true)
    new MilvusInsert().run(spark, args)
  }

  test("search partition") {
    dataset().createOrReplaceTempView("a")
    val args = MilvusSearchArgs(input = "a", collectionName = "test_spark", primaryCol = "item_id", partitionCol = "pt")
    new MilvusSearch().apply(spark, args).show()
  }

  test("search partition filter_not") {
    dataset().withColumn("not", array(col("item_id"))).createOrReplaceTempView("a")
    val args = MilvusSearchArgs(input = "a", collectionName = "test_spark", primaryCol = "item_id", partitionCol = "pt", filterNotInCol = "not")
    new MilvusSearch().apply(spark, args).show()
  }
}