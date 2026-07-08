package com.netease.easyml.uds.examples

import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.SparkUtil
import com.netease.easyml.ml.feature.RecallNegativeSample
import org.apache.spark.sql.SparkSession;

/**
 * Created by linjiuning on 2021/1/21.
 * Rank negative sample.
 * <p>
 * data schema:
 * [input] tokens: Array[Any]
 * [output] positive: Array[Any], negative: Array[Any]
 * <p>
 * params:
 * input: input table
 * output: output table
 * inputCol: col name of input
 * sample: The threshold for configuring which higher-frequency words are randomly downsampled
 * seed: random seed
 * iter: num of iter
 * negative: num of negative sample
 */
case class RecallNegativeSampleArgs(input: String, output: String, inputCol: String, sample: Double = 1e-3, seed: Int = 1,
                                    iter: Int = 1, negative: Int = 5)

object RecallNegativeSampleUDS extends UDS[RecallNegativeSampleArgs] {

  def run(spark: SparkSession, args: Args): Unit = {
    var df = spark.sql(s"select * from ${args.input} where ${args.inputCol} is not null")
    val conf = spark.sparkContext.getConf
    val numPartitions = SparkUtil.getDefaultParallelism(conf)
    logInfo(s"Set numPartitions = $numPartitions")
    df = df.repartition(numPartitions)

    df = new RecallNegativeSample()
      .setInputCol(args.inputCol)
      .setMaxIter(args.iter)
      .setSample(args.sample)
      .setNegative(args.negative)
      .setSeed(args.seed)
      .transform(df)

    if (SparkUtil.isLocalMaster(spark.sparkContext.getConf)) {
      df.show(false)
    } else {
      SparkUtil.saveAsTable(df, args.output)
    }
  }
}
