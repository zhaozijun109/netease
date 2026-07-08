package com.netease.easyml.uds.examples

import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.SparkUtil
import com.netease.easyml.ml.nlp.WordDiscovery
import org.apache.spark.sql.SparkSession;

/**
 * Created by linjiuning on 2020/12/14.
 * Word discovery.
 * <p>
 * data schema:
 * [input] text: String or Array[String]
 * [output] word: String, count: Long
 * <p>
 * params:
 * input: input table
 * output: output table
 * inputCol: col name of text
 * ngram
 * minCount: min count of ngram
 * minPMI: min pmi for each ngram
 * minLength: min length of vocab
 */
case class WordDiscoveryArgs(input: String, output: String, inputCol: String,
                             ngram: Int = 4, minCount: Int = 32, minPMI: String = "0;2;4;6", minLength: Int = 1)

object WordDiscoveryUDS extends UDS[WordDiscoveryArgs] {

  def run(spark: SparkSession, args: Args): Unit = {
    var df = spark.sql(s"select ${args.inputCol} from ${args.input} where ${args.inputCol} is not null")
    val conf = spark.sparkContext.getConf
    val numPartitions = SparkUtil.getDefaultParallelism(conf)
    logInfo(s"Set numPartitions = $numPartitions")

    df = df.repartition(numPartitions)

    df = new WordDiscovery()
      .setInputCol(args.inputCol)
      .setNgram(args.ngram)
      .setMinCount(args.minCount)
      .setNumPartitions(numPartitions)
      .setMinPMI(args.minPMI.split(";").map(_.toDouble))
      .setMinLength(args.minLength)
      .transform(df)

    if (SparkUtil.isLocalMaster(spark.sparkContext.getConf)) {
      df.show(false)
    } else {
      SparkUtil.saveAsTable(df, args.output, "textfile")
    }
  }
}
