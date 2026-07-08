package com.netease.easyml.uds.examples

import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.{IOUtil, SparkUtil}
import com.netease.easyml.ml.feature.NSWord2Vec
import com.netease.easyml.ml.util.MLUtils
import org.apache.spark.ml.feature_.Word2Vec
import org.apache.spark.sql.SparkSession;

/**
 * Created by linjiuning on 2020/10/16.
 * Train WordVec based on spark
 * <p>
 * data schema:
 * [input] tokens: Seq[String]
 * [output] word: String, vector: Seq[Float]
 * <p>
 * params:
 * input: input table
 * checkpoint: path to save Word2VecModel
 * output: output table/path
 * inputCol: col name of tokens
 * iter: num of iter
 * lr: learning rate
 * vectorSize: embedding size
 * minCount: min token count
 * negative: num of negative sample, If set to 0, no negative sampling is used
 * shuffle: whether shuffle tokens every epoch
 */
case class WordVecArgs(input: String, checkpoint: String, output: String, inputCol: String,
                       iter: Int = 5, lr: Double = 0.025, vectorSize: Int = 100, minCount: Int = 5,
                       negative: Int = 5, shuffle: Boolean = true)

object WordVecUDS extends UDS[WordVecArgs] {

  def run(spark: SparkSession, args: Args): Unit = {
    val df = spark.sql(s"select ${args.inputCol} from ${args.input} where ${args.inputCol} is not null")

    val conf = spark.sparkContext.getConf
    val numPartitions = SparkUtil.getDefaultParallelism(conf)
    logInfo(s"Set numPartitions = $numPartitions")
    val estimator = if (args.negative > 0) {
      new NSWord2Vec()
        .setInputCol(args.inputCol)
        .setMinCount(args.minCount)
        .setMaxIter(args.iter)
        .setAlpha(args.lr.toFloat)
        .setVectorSize(args.vectorSize)
        .setNegative(args.negative)
        .setShuffle(args.shuffle)
        .setNumPartitions(numPartitions)
    } else {
      new Word2Vec()
        .setInputCol(args.inputCol)
        .setMinCount(args.minCount)
        .setMaxIter(args.iter)
        .setStepSize(args.lr.toFloat)
        .setVectorSize(args.vectorSize)
        .setShuffle(args.shuffle)
        .setNumPartitions(numPartitions)
    }

    val model = estimator.fit(df)
    model.write.overwrite().save(args.checkpoint)

    if (SparkUtil.isLocalMaster(conf) || IOUtil.isHdfs(args.output)) {
      logInfo("Parquet wordvec to text")
      MLUtils.parquetWordVecToText(spark, args.checkpoint, args.output, numPartitions = Some(1))
    } else {
      logInfo("Parquet wordvec to hive")
      MLUtils.parquetWordVecToHive(spark, args.checkpoint, args.output)
    }
  }
}
