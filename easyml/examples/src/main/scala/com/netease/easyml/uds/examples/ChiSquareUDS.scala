package com.netease.easyml.uds.examples

import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.SparkUtil
import com.netease.easyml.ml.feature.ChiSquare
import org.apache.spark.sql.SparkSession;

/**
 * Created by linjiuning on 2020/12/4.
 * ChiSquare significant.
 * <p>
 * data schema:
 * [input] label: String, feature: String, count: Numeric
 * [output] score: Double
 * <p>
 * params:
 * input: input table
 * output: output table
 * labelCol: col name of label
 * inputCol: col name of feature
 * countCol: col name of count
 * sign: default true
 */
case class ChiSquareArgs(input: String, output: String, labelCol: String, inputCol: String, countCol: String, sign: Boolean = false)

object ChiSquareUDS extends UDS[ChiSquareArgs] {

  def run(spark: SparkSession, args: Args): Unit = {

    var df = spark.sql(s"select ${args.labelCol}, ${args.inputCol}, ${args.countCol} from ${args.input}")
    val conf = spark.sparkContext.getConf
    val numPartitions = SparkUtil.getDefaultParallelism(conf)
    logInfo(s"Set numPartitions = $numPartitions")

    df = new ChiSquare()
      .setLabelCol(args.labelCol)
      .setInputCol(args.inputCol)
      .setCountCol(args.countCol)
      .setSign(args.sign)
      .transform(df)

    if (SparkUtil.isLocalMaster(spark.sparkContext.getConf)) {
      df.show(false)
    } else {
      SparkUtil.saveAsTable(df, args.output)
    }
  }
}
