package com.netease.easyml.uds.examples

import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.SparkUtil
import com.netease.easyml.ml.nlp.RoBERTTFRecordData
import org.apache.spark.sql.SparkSession

/**
 * Created by linjiuning on 2021/5/24.
 */
case class RoBertTFRecordDataArgs(input: String, output: String, inputCol: String, dupeFactor: Int = 10, lowerCase: Boolean = true,
                                  wholeWordMask: Boolean = true, sentenceSep: String = "\n", maxSeqLength: Int = 512)

object RoBertTFRecordDataUDS extends UDS[RoBertTFRecordDataArgs] {

  def run(spark: SparkSession, args: Args): Unit = {
    val df = spark.sql(s"select ${args.inputCol} from ${args.input} where ${args.inputCol} is not null")

    val conf = spark.sparkContext.getConf
    val numPartitions = SparkUtil.getDefaultParallelism(conf)
    logInfo(s"Set numPartitions = $numPartitions")

    val transform = new RoBERTTFRecordData()
      .setInputCol(args.inputCol)
      .setDupeFactor(args.dupeFactor)
      .setLowercase(args.lowerCase)
      .setWholeWordMask(args.wholeWordMask)
      .setSentenceSep(args.sentenceSep)
      .setMaxSeqLength(args.maxSeqLength)
      .setNumPartitions(numPartitions)

    val newDf = transform.transform(df)

    if (SparkUtil.isLocalMaster(spark.sparkContext.getConf)) {
      newDf.show(false)
    } else {
      SparkUtil.saveAsTable(newDf, args.output)
    }
  }

}
