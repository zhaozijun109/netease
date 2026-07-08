package com.netease.easyml.uds.examples

import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.SparkUtil
import com.netease.easyml.ml.nlp.PMIStat
import org.apache.spark.sql.SparkSession;

/**
 * Created by linjiuning on 2020/10/16.
 * Train WordVec based on spark
 * <p>
 * data schema:
 * [input] tokens: Seq[String]
 * [output] word1: String, word2: String, pmi: Double
 * <p>
 * params:
 * input: input table
 * output: output table
 * inputCol: col name of tokens
 * minCount: min count[default=1]
 * window: window[default=0]
 * minPMI: min pmi[default=0.0]
 * minCount: min token count
 * normalize: whether do normalize pmi
 */
case class PMIArgs(input: String, output: String, inputCol: String, minCount: Int = 1, window: Int = 0,
                   minPMI: Double = 0.0, normalize: Boolean = true)

object PMIUDS extends UDS[PMIArgs] {

  def run(spark: SparkSession, args: Args): Unit = {
    val df = spark.sql(s"select ${args.inputCol} from ${args.input} where ${args.inputCol} is not null")

    val conf = spark.sparkContext.getConf
    val numPartitions = SparkUtil.getDefaultParallelism(conf)
    logInfo(s"Set numPartitions = $numPartitions")

    val newDf = new PMIStat()
      .setInputCol(args.inputCol)
      .setMinCount(args.minCount)
      .setWindow(args.window)
      .setMinPMI(args.minPMI)
      .setNormalize(args.normalize)
      .setNumPartitions(numPartitions)
      .transform(df)

    if (SparkUtil.isLocalMaster(spark.sparkContext.getConf)) {
      newDf.show(false)
    } else {
      SparkUtil.saveAsTable(newDf, args.output)
    }
  }
}
