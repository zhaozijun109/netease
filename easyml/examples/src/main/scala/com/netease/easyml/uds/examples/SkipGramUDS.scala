package com.netease.easyml.uds.examples

import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.SparkUtil
import com.netease.easyml.ml.feature.SkipGram
import org.apache.spark.sql.SparkSession;

/**
 * Created by linjiuning on 2020/12/28.
 * Skip Gram.
 * <p>
 * data schema:
 * [input] tokens: Array[String] or Array[Numeric]
 * [output] src: String or Numeric, dst: String or Numeric
 * <p>
 * params:
 * input: input table
 * output: output table
 * inputCol: col name of input
 * window: Maximum distance between the current and predicted word within a sentence.
 * sample: The threshold for configuring which higher-frequency words are randomly downsampled
 * seed: random seed
 */
case class SkipGramArgs(input: String, output: String, inputCol: String, window: Int = 5, sample: Double = 1e-3, seed: Long = 1)

object SkipGramUDS extends UDS[SkipGramArgs] {

  def run(spark: SparkSession, args: Args): Unit = {
    var df = spark.sql(s"select ${args.inputCol} from ${args.input} where ${args.inputCol} is not null")
    val conf = spark.sparkContext.getConf
    val numPartitions = SparkUtil.getDefaultParallelism(conf)
    logInfo(s"Set numPartitions = $numPartitions")
    df = df.repartition(numPartitions)

    df = new SkipGram()
      .setInputCol(args.inputCol)
      .setWindow(args.window)
      .setSample(args.sample)
      .setSeed(args.seed)
      .transform(df)

    if (SparkUtil.isLocalMaster(spark.sparkContext.getConf)) {
      df.show(false)
    } else {
      SparkUtil.saveAsTable(df, args.output)
    }
  }
}
