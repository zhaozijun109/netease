package com.netease.easyml.uds.examples

import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.SparkUtil
import com.netease.easyml.ml.transform.MiaobiNLPPredictor
import org.apache.spark.sql.SparkSession;

/**
 * Created by linjiuning on 2020/11/11.
 * Miaobinlp predictor
 * <p>
 * data schema:
 * [input] text: String
 * [output] result: Json String
 * <p>
 * params:
 * input: input table
 * output: output table/path
 * inputCol: text col name
 * predictor: transformers_tagger or text_classifier
 * model: torch model
 */
case class MiaobiNLPPredictorArgs(input: String, output: String, inputCol: String, predictor: String, model: String)

object MiaobiNLPPredictorUDS extends UDS[MiaobiNLPPredictorArgs] {

  def run(spark: SparkSession, args: Args): Unit = {
    var df = spark.sql(s"select * from ${args.input}")

    val mbPredictor = new MiaobiNLPPredictor()
      .setFeaturesCol(args.inputCol)
      .setPredictor(args.predictor)
      .setPath(args.model)

    df = mbPredictor.transform(df)

    if (SparkUtil.isLocalMaster(spark.sparkContext.getConf)) {
      df.show(false)
    } else {
      SparkUtil.saveAsTable(df, args.output)
    }
  }
}
