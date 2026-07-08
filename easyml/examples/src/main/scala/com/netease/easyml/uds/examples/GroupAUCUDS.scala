package com.netease.easyml.uds.examples

import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.SparkUtil
import com.netease.easyml.ml.metric.ranking.GroupAUC
import org.apache.spark.sql.SparkSession;

/**
 * Created by linjiuning on 2021/3/22.
 * Calculate Group AUC.
 * <p>
 * data schema:
 * [input] userId: String, prediction: Double, label: Double
 * [output] score: Double
 * <p>
 * params:
 * input: input table
 * userIdCol: col name of userId
 * predictionCol: col name of prediction
 * labelCol: col name of label
 */
case class GroupAUCArgs(input: String, userIdCol: String, predictionCol: String, labelCol: String)

object GroupAUCUDS extends UDS[GroupAUCArgs] {

  def run(spark: SparkSession, args: Args): Unit = {
    val df = spark.sql(s"select ${args.userIdCol}, ${args.predictionCol}, ${args.labelCol} from ${args.input}")
    val conf = spark.sparkContext.getConf
    val numPartitions = SparkUtil.getDefaultParallelism(conf)
    logInfo(s"Set numPartitions = $numPartitions")

    val auc = new GroupAUC()
      .setUserId(args.userIdCol)
      .setPredictionCol(args.predictionCol)
      .setLabelCol(args.labelCol)
      .evaluate(df)

    println(s"Group AUC: $auc")
  }
}
