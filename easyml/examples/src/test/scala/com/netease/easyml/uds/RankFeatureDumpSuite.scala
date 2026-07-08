package com.netease.easyml.uds

import com.holdenkarau.spark.testing.SharedSparkContext
import com.netease.easyml.uds.examples._
import com.netease.easyml.uds.util.RankUtil
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2022/8/16.
 */
class RankFeatureDumpSuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  val inputPath = "examples/toy_dataset/dump/examples.txt"
  val labelPath = "examples/toy_dataset/dump/labels.txt"
  val inputMusicPath = "examples/toy_dataset/dump/examples.music.txt"
  val labelMusicPath = "examples/toy_dataset/dump/labels.music.txt"
  val config = "examples/toy_dataset/dump/config.yaml"
  val feature = "feature"
  val label = "label"
  val output = "target/tmp/eie"
  val numPartition = 0
  val music = true

  def dataset(): DataFrame = {
    spark.read.text(if (music) inputMusicPath else inputPath)
  }

  def labelDataset(): DataFrame = {
    spark.read.json(if (music) labelMusicPath else labelPath)
  }

  test("dump") {
    val featureDf = dataset()
    featureDf.createOrReplaceTempView(feature)

    val labelDf = labelDataset()
    labelDf.createOrReplaceTempView(label)

    val sql =
      s"""
         |select $label.ctr, $label.cvr, a.* from a join $label on a.rid=$label.rid and a.user_id=$label.user_id and a.item_id=$label.item_id
         |""".stripMargin

    val args = RankFeatureDumpArgs(feature = feature, sql = sql, output = output, config = config, asInt = false, music = music)
    RankFeatureDumpUDS.run(spark, args)
  }

  test("dump eie") {
    val featureDf = dataset()
    featureDf.createOrReplaceTempView(feature)

    val labelDf = labelDataset()
    labelDf.createOrReplaceTempView(label)

    val args = RankFeatureDumpEIEArgs(feature = feature, labelInput = label, output = output, config = config, numPartitions = numPartition, multiType = true, partitionByHour = true)
    RankFeatureDumpEIEUDS.run(spark, args)
  }

  test("join eie") {
    val labelDf = labelDataset()
    labelDf.createOrReplaceTempView(label)

    val args = EIEJoinArgs(input = output, exampleInput = label, output = output + "_join", numPartitions = numPartition, multiType = true)
    EIEJoinUDS.run(spark, args)
  }

  test("dump music") {
    RankUtil.readServerConfig(config)
    spark.read.text("/Users/linjiuning/Downloads/part-0-195-d53dc8.lzo").show()
  }
}