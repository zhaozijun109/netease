package com.netease.easyml.uds

import com.netease.easyml.uds.examples.{BuildLuceneArgs, BuildLuceneUDS}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2021/7/26.
 */
class BuildLuceneSuite extends FunSuite {
  val spark: SparkSession = SparkSession.builder().master("local[4]").getOrCreate()
  spark.sparkContext.setLogLevel("ERROR")

  val docIdCol = "doc_id"
  val numericCol = "numeric"
  val textCol = "text"
  val keywordCol = "keyword"

  val cols = Array(docIdCol, numericCol, textCol, keywordCol)

  val input = "input"
  val output = "target/tmp/lucene"
  val config = "examples/toy_dataset/lucene/config.json"
  val overwrite = true
  val batchSize = 3

  def dataset(): DataFrame = {
    import spark.implicits._
    spark.sparkContext.parallelize(Seq(
      ("d1", 1.0, Array("w1", "w2"), "k1"),
      ("d2", 3.0, null, "k2"),
      ("d3", 4.0, Array("w4", "w2"), null),
      ("d4", 0.0, Array("w1", "w3"), "k4")
    )
    ).toDF(cols: _*)
  }

  test("lucene") {
    val ds = dataset()
    ds.createOrReplaceTempView(input)

    val args = BuildLuceneArgs(input = input, output = output, inputCols = cols.mkString(";"), config = config, overwrite = overwrite, batchSize = batchSize)

    BuildLuceneUDS.run(spark, args)
  }
}
