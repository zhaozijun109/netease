package com.netease.easyudf.cmd

import com.netease.easyml.common.cmds.VoidUserDefinedCmd
import org.apache.spark.sql.SparkSession

case class TrainTestSplitArgs(input: String, seed: Long = 1023, ratio: Double = 0.7,
                              train: String = "train", test: String = "test")

class TrainTestSplit extends VoidUserDefinedCmd[TrainTestSplitArgs] {

  override def run(spark: SparkSession, args: TrainTestSplitArgs): Unit = {
    val df = spark.table(args.input)
    val Array(training, test) = df.randomSplit(Array(args.ratio, 1 - args.ratio), args.seed)
    training.createOrReplaceTempView(args.train)
    test.createOrReplaceTempView(args.test)
  }

}
