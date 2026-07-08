package com.netease.easyml.uds.examples

import com.netease.easyml.common.uds.UDS
import com.netease.easyml.uds.examples.{RankCountIdsUDS => CountIds}
import com.netease.easyml.uds.util.Constant.NULL
import com.netease.easyml.uds.util.LoadFeatureDump.loadAndParseFeatureDf
import com.netease.easyml.uds.util.RankUtil
import com.netease.easyml.uds.util.RankUtil._
import org.apache.spark.sql.SparkSession

/**
 * Created by linjiuning on 2021/6/5.
 * count ids dump feature.
 */
case class RankFeatureDumpCountIdsArgs(sql: String, output: String, modelConfig: String, config: String,
                                       feature: String = NULL, minCount: Int = MIN_COUNT,
                                       startDay: String = NULL, endDay: String = NULL, env: String = "",
                                       isFea: Boolean = true, music: Boolean = false)

object RankFeatureDumpCountIdsUDS extends UDS[RankFeatureDumpCountIdsArgs] {

  def run(spark: SparkSession, args: RankFeatureDumpCountIdsArgs): Unit = {
    val mConfig = readServerConfig(args.modelConfig)
    var configs = readConfig(args.config).filterNot(_.embedName.isEmpty)
    if (args.isFea) {
      configs = configs.filter(_.isFea)
    }
    val keys = (configs.map(_.name) ++ Array(SESSION_ID, USER_ID, ITEM_ID)).distinct

    if (!args.feature.equals(NULL)) {
      val featureDf = loadAndParseFeatureDf(spark, args.feature, args.startDay, args.endDay, mConfig, keys = Some(keys), music = args.music)
      featureDf.createOrReplaceTempView("a")
    }

    val result = RankUtil.sql(spark, args.sql, args.env)
    result.createOrReplaceTempView("input")

    val nArgs = new CountIds.Args(input = "input", output = args.output, config = args.config, minCount = args.minCount, isFea = args.isFea)
    CountIds.run(spark, nArgs)
  }
}
