package com.netease.easyml.uds.examples

import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.{IOUtil, SparkUtil}
import com.netease.easyml.ml.sklearn.feature_extraction.TfidfVectorizer
import com.netease.easyml.ml.util.{MLUtils, SchemaUtils}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.concat_ws;

/**
 * Created by linjiuning on 2020/10/22.
 * Calculate idf value
 * <p>
 * data schema:
 * [input] tokens: Seq[String]
 * [output] word: String, weight: Float
 * <p>
 * params:
 * input: input table
 * output: output table/path
 * inputCol: tokens col name
 * minDf: Specifies the minimum number of different documents a term must appear in to be included in the vocabulary
 */
case class IDFWeightArgs(input: String, output: String, inputCol: String, minDf: Float)

object IDFWeightUDS extends UDS[IDFWeightArgs] {

  def run(spark: SparkSession, args: Args): Unit = {
    var df = spark.sql(s"select ${args.inputCol} from ${args.input}")

    if (SchemaUtils.isArrayType(df.schema, args.inputCol)) {
      df = df.withColumn(args.inputCol, concat_ws(" ", df(args.inputCol)))
    }

    val model = new TfidfVectorizer()
      .setInputCol(args.inputCol)
      .setMinDF(args.minDf)
      .setTokenPattern("")
      .fit(df)

    val wordWeights = model.wordWeights.map { case (key, value) => (key, value.toFloat) }
    if (SparkUtil.isLocalMaster(spark.sparkContext.getConf) || IOUtil.isHdfs(args.output)) {
      MLUtils.saveWordWeight(args.output, wordWeights)
    } else {
      MLUtils.saveWordWeightToHive(spark, args.output, wordWeights.toSeq)
    }
  }
}
