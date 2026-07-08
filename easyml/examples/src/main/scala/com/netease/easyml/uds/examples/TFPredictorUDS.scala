package com.netease.easyml.uds.examples

import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.{IOUtil, SparkUtil}
import com.netease.easyml.ml.transform.TensorFlowPredictor
import com.netease.easyml.uds.util.Constant._
import org.apache.spark.sql.SparkSession;

/**
 * Created by linjiuning on 2021/2/7.
 * TensorFlow predictor
 * <p>
 * data schema:
 * [inputs] inputs: Array[Numeric]
 * [outputs] outputs: Array[Numeric]
 * <p>
 * params:
 * input: input table
 * output: output table
 * inputCols: input col names
 * model: saved model path
 * batchSize: batch size
 * numThreads: num threads
 * dropInputs: whether drop input cols
 * exampleName: whether do serialize
 * format: hive storage format
 */
case class TFPredictorArgs(input: String, output: String, inputCols: String = NULL, model: String,
                           batchSize: Int = 128, numThreads: Int = 0, dropInputs: Boolean = true, exampleName: String = NULL,
                           signatureKey: String = NULL, numPartition: Int = 0, format: String = PARQUET)

object TFPredictorUDS extends UDS[TFPredictorArgs] {

  def run(spark: SparkSession, args: Args): Unit = {
    var df = if (IOUtil.isDirectory(args.input)) {
      spark.read.format("tfrecord").option("recordType", "Example").load(args.input)
    } else {
      spark.sql(s"select * from ${args.input}")
    }

    val tfPredictor = new TensorFlowPredictor()
      .setBatchSize(args.batchSize)
      .setDropInputs(args.dropInputs)
      .setPath(args.model)

    if (args.numPartition > 0) {
      tfPredictor.setNumPartitions(args.numPartition)
    }

    if (args.inputCols.equals(NULL)) {
      tfPredictor.setInputCols(args.inputCols.split(";"))
    }

    if (args.exampleName.equals(NULL)) {
      tfPredictor.setExampleName(args.exampleName)
    }

    if (args.numThreads > 0) {
      tfPredictor.setNumThreads(args.numThreads)
    }

    if (args.signatureKey.equals(NULL)) {
      tfPredictor.setSignatureKey(args.signatureKey)
    }

    df = tfPredictor.transform(df)

    if (SparkUtil.isLocalMaster(spark.sparkContext.getConf)) {
      df.show(false)
    } else {
      SparkUtil.saveAsTable(df, args.output, format = args.format)
    }
  }
}
