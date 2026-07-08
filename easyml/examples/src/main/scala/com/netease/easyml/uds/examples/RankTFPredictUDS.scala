package com.netease.easyml.uds.examples

import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.{IOUtil, SparkUtil}
import com.netease.easyml.ml.transform.TensorFlowPredictor
import com.netease.easyml.uds.util.Constant._
import com.netease.easyml.uds.util.RankUtil._
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.col

/**
 * Created by linjiuning on 2021/6/5.
 * tf predict.
 *
 * params:
 * input: input table
 * output: output table
 * mode: saved model dir
 * keepCols: input columns to keep
 * startDay: table partition
 * endDay: table partition
 * gzip: gzip
 * signatureKey
 * numPartition
 */
case class RankTFPredictArgs(input: String, output: String, model: String, keepCols: String = NULL,
                             startDay: String = "", endDay: String = "", signatureKey: String = NULL,
                             numPartitions: Int = 0)

object RankTFPredictUDS extends UDS[RankTFPredictArgs] {
  val BATCH_SIZE = 512

  def run(spark: SparkSession, args: RankTFPredictArgs): Unit = {
    var df = if (IOUtil.isDirectory(args.input)) {
      SparkUtil.loadFromTfRecordOfDays(spark, args.input, startDay = args.startDay, endDay = args.endDay)
    } else {
      SparkUtil.loadFromTable(spark, args.input, startDay = args.startDay, endDay = args.endDay)
    }

    val nPartitions = if (args.numPartitions <= 0) {
      SparkUtil.getDefaultParallelism(spark.sparkContext.getConf)
    } else {
      args.numPartitions
    }

    val model = resolvePath(args.model)
    println(s"model path = $model")
    var tfPredictor = new TensorFlowPredictor()
      .setExampleName(EXAMPLES)
      .setDropInputs(false)
      .setPath(model)
      .setBatchSize(BATCH_SIZE)
      .setNumPartitions(nPartitions)

    if (!args.signatureKey.equals(NULL)) {
      tfPredictor = tfPredictor.setSignatureKey(args.signatureKey)
    }

    df = tfPredictor.transform(df)

    var outputCols = tfPredictor.getOutputCols
    if (!args.keepCols.equals(NULL)) {
      outputCols = args.keepCols.split(";") ++ outputCols
      outputCols = outputCols.distinct
    }

    df = df.select(outputCols.map(col): _*)

    if (SparkUtil.isLocalMaster(spark.sparkContext.getConf)) {
      df.show(false)
    } else {
      SparkUtil.saveAsTable(df, args.output)
    }
  }
}
