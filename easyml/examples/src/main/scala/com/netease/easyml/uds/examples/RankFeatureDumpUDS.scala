package com.netease.easyml.uds.examples

import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.SparkUtil
import com.netease.easyml.uds.util.Constant.NULL
import com.netease.easyml.uds.util.LoadFeatureDump.loadAndParseFeatureDf
import com.netease.easyml.uds.util.RankUtil
import com.netease.easyml.uds.util.RankUtil._
import org.apache.spark.sql.SparkSession

/**
 * Created by linjiuning on 2021/6/5.
 * parse dump feature.
 */
case class RankFeatureDumpArgs(config: String, sql: String, feature: String = NULL,
                               output: String = NULL, day: String = NULL,
                               env: String = "", gzip: Boolean = true, numPartitions: Int = RESULT_PARTITION,
                               procFeaOnly: Boolean = false, fillNa: Boolean = true, asInt: Boolean = false,
                               multiType: Boolean = false, music: Boolean = false)

object RankFeatureDumpUDS extends UDS[RankFeatureDumpArgs] {

  def run(spark: SparkSession, args: RankFeatureDumpArgs): Unit = {
    val mConfig = readServerConfig(args.config)
    if (!args.feature.equals(NULL)) {
      val featureDf = loadAndParseFeatureDf(spark, args.feature, startDay = args.day, endDay = args.day, mConfig, music = args.music)
      featureDf.createOrReplaceTempView("a")
    }

    var result = RankUtil.sql(spark, args.sql, args.env)

    if (!args.output.equals(NULL)) {
      val feaConfigs = featureConfigFromServerConfig(mConfig)

      result = featureProcess(result, feaConfigs, procFeaOnly = args.procFeaOnly, fillNa = args.fillNa, asInt = args.asInt)
      if (SparkUtil.isLocalMaster(spark.sparkContext.getConf)) {
        result.show(false)
      } else {
        saveTfRecord(result, args.output, gzip = args.gzip, numPartitions = args.numPartitions, multiType = args.multiType)
      }
    }
  }
}
