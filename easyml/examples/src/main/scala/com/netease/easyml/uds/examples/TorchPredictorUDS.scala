package com.netease.easyml.uds.examples

import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.SparkUtil
import com.netease.easyml.ml.transform.TorchPredictor
import com.netease.easyml.uds.util.Constant._
import org.apache.spark.sql.SparkSession

/**
 * Created by linjiuning on 2021/2/18.
 * Torch predictor
 * <p>
 * data schema:
 * [inputs] inputs: Array[Numeric]
 * [outputs] outputs: Array[Numeric]
 * <p>
 * params:
 * input: input table
 * output: output table
 * inputCols: input col names
 * inputDtypes: input dtypes, optional
 * outputCols: output col names
 * model: saved model path
 * batchSize: batch size
 * numThreads: num threads
 * dropInputs: whether drop input cols
 * format: hive storage format
 */
case class TorchPredictorArgs(input: String, output: String, inputCols: String, outputCols: String,
                              inputDtypes: String = NULL, model: String, batchSize: Int = 128, numThreads: Int = 0,
                              dropInputs: Boolean = true, numPartition: Int = 0, format: String = PARQUET)

object TorchPredictorUDS extends UDS[TorchPredictorArgs] {

  def run(spark: SparkSession, args: Args): Unit = {
    var df = spark.sql(s"select * from ${args.input}")

    val torchPredictor = new TorchPredictor()
      .setInputCols(args.inputCols.split(";"))
      .setOutputCols(args.outputCols.split(";"))
      .setBatchSize(args.batchSize)
      .setDropInputs(args.dropInputs)
      .setPath(args.model)

    if (args.numPartition > 0) {
      torchPredictor.setNumPartitions(args.numPartition)
    }

    if (args.numThreads > 0) {
      torchPredictor.setNumThreads(args.numThreads)
    }

    if (args.inputDtypes.equals(NULL)) {
      torchPredictor.setInputDtypes(args.inputDtypes.split(";"))
    }

    df = torchPredictor.transform(df)

    if (SparkUtil.isLocalMaster(spark.sparkContext.getConf)) {
      df.show(false)
    } else {
      SparkUtil.saveAsTable(df, args.output, format = args.format)
    }
  }
}
