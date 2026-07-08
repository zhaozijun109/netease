package com.netease.easyml.uds

import com.holdenkarau.spark.testing.SharedSparkContext
import com.netease.easyml.common.util.IOUtil
import com.netease.easyml.uds.examples.{RankTFRecordEIEArgs, RankTFRecordEIEUDS}
import com.netease.easyml.uds.util.RankUtil._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2021/3/15.
 */
class RankTFRecordEIESuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  val basedir = "examples/toy_dataset/rank"
  val inputPath = IOUtil.join(basedir, "data.parquet")
  val config = IOUtil.join(basedir, "features.tsv")
  val output = "target/tmp/eie"

  val labelInput = "labelInput"
  val contextInput = "contextInput"
  val exampleInput = "exampleInput"
  val mergeInput = "mergeInput"

  def contextDataset(): DataFrame = {
    val cols = readConfig(config).filter(_.group.contains("user")).map(_.key) :+ SESSION_ID
    spark.read.parquet(inputPath).select(cols.distinct.map(col): _*)
  }

  def exampleDataset(): DataFrame = {
    val cols = readConfig(config).filter(_.group.contains("item")).map(_.key) :+ SESSION_ID

    spark.read.parquet(inputPath).select(cols.distinct.map(col): _*)
  }

  def mergeDataset(): DataFrame = {
    val cols = readConfig(config).filter(it => it.isFea).map(_.key) :+ SESSION_ID

    spark.read.parquet(inputPath).select(cols.distinct.map(col): _*)
  }

  def labelDataset(): DataFrame = {
    val keep = Array(SESSION_ID, IS_TRAIN, DAY, USER_ID, ITEM_ID, LABEL)
    val cols = readConfig(config).filter(it => keep.contains(it.name)).map(_.key)

    spark.read.parquet(inputPath).select(cols.distinct.map(col): _*)
  }

  test("write") {
    val labelDf = labelDataset()
    labelDf.createOrReplaceTempView(labelInput)

    val contextDf = contextDataset()
    contextDf.createOrReplaceTempView(contextInput)

    val exampleDf = exampleDataset()
    exampleDf.createOrReplaceTempView(exampleInput)

    val mergeDf = mergeDataset()
    mergeDf.createOrReplaceTempView(mergeInput)

//    val args = RankTFRecordEIEArgs(labelInput = labelInput, contextInput = contextInput,
//      exampleInput = exampleInput, output = output, config = config)
//    RankTFRecordEIEUDS.run(spark, args)

    val args = RankTFRecordEIEArgs(labelInput = labelInput,
      exampleInput = mergeInput, output = output, config = config)
    RankTFRecordEIEUDS.run(spark, args)
  }
}
