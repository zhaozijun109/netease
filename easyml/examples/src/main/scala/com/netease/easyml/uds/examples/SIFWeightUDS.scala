package com.netease.easyml.uds.examples

import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.{IOUtil, SparkUtil}
import com.netease.easyml.ml.feature.SIFWordWeight
import com.netease.easyml.ml.util.MLUtils
import org.apache.spark.sql.SparkSession;

/**
 * Created by linjiuning on 2020/10/22.
 * Calculate word weight of sif algorithm a / (a + p(w))
 * <p>
 * data schema:
 * [input] tokens: Seq[String]
 * [output] word: String, weight: Float
 * <p>
 * params:
 * input: input table
 * output: output table/path
 * inputCol: tokens col name
 * a: the parameter in the SIF weighting scheme
 * docLevel: p(w) in doc level or word level
 */
case class SIFWeightArgs(input: String, output: String, inputCol: String, a: Double = 1e-3, docLevel: Boolean = false)

object SIFWeightUDS extends UDS[SIFWeightArgs] {

  def run(spark: SparkSession, args: Args): Unit = {
    val df = spark.sql(s"select ${args.inputCol} from ${args.input}")

    val model = new SIFWordWeight()
      .setInputCol(args.inputCol)
      .setA(args.a)
      .setDocLevel(args.docLevel)
      .fit(df)

    if (SparkUtil.isLocalMaster(spark.sparkContext.getConf) || IOUtil.isHdfs(args.output)) {
      //      MLUtils.saveWordWeight(output, model.wordWeights)
      model.save(args.output)
    } else {
      val wordWeights = model.wordWeights.map {
        case (key, value) => (key, value)
      }.toSeq

      MLUtils.saveWordWeightToHive(spark, args.output, wordWeights)
    }
  }
}
