package com.netease.easyml.uds.examples

import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.{IOUtil, SparkUtil}
import com.netease.easyml.ml.feature.PSNSWord2Vec
import com.netease.easyml.ml.util.MLUtils
import com.tencent.angel.spark.context.PSContext
import org.apache.spark.sql.SparkSession

/**
 * Created by linjiuning on 2020/10/16.
 * Train WordVec based on Angel
 * <p>
 * data schema:
 * [input] tokens: Seq[String]
 * [output] word: String, vector: Seq[Float]
 * <p>
 * params:
 * input: input table
 * checkpoint: path to save angel ps matrix
 * output: output table/path
 * inputCol: col name of tokens
 * iter: num of iter
 * lr: learning rate
 * vectorSize: embedding size
 * minCount: min token count
 * negative: num of negative sample, If set to 0, no negative sampling is used
 * batchSize: batch size
 */
case class PSWordVecArgs(input: String, checkpoint: String, output: String, inputCol: String,
                         iter: Int = 5, lr: Double = 0.025, vectorSize: Int = 100, minCount: Int = 5,
                         negative: Int = 5, batchSize: Int = 50, shuffle: Boolean = true)

object PSWordVecUDS extends UDS[PSWordVecArgs] {

  def run(spark: SparkSession, args: Args): Unit = {
    PSContext.getOrCreate(spark.sparkContext)
    val df = spark.sql(s"select ${args.inputCol} from ${args.input}")

    val conf = spark.sparkContext.getConf
    val numPartitions = 3 * SparkUtil.getDefaultParallelism(conf)
    logInfo(s"Set numPartitions = $numPartitions")
    val numPsPart = 5 * SparkUtil.getNumAngelPSInstance(conf)
    logInfo(s"Set numPsPart = $numPsPart")
    val model = new PSNSWord2Vec()
      .setInputCol(args.inputCol)
      .setMinCount(args.minCount)
      .setMaxIter(args.iter)
      .setAlpha(args.lr.toFloat)
      .setVectorSize(args.vectorSize)
      .setNegative(args.negative)
      .setNumPartitions(numPartitions)
      .setNumPsPart(numPsPart)
      .setBatchSize(args.batchSize)
      .setShuffle(args.shuffle)
      .setModelPath(args.checkpoint)
      .setReturnNull(true)
      .setCheckpointInterval(1)

    model.fit(df)

    if (SparkUtil.isLocalMaster(conf) || IOUtil.isHdfs(args.output)) {
      logInfo("Angel wordvec to text")
      MLUtils.angelWordVecToText(spark, args.checkpoint, args.output, numPartitions = Some(1))
    } else {
      logInfo("Angel wordvec to hive")
      MLUtils.angelWordVecToHive(spark, args.checkpoint, args.output)
    }

    PSContext.stop()
  }
}
