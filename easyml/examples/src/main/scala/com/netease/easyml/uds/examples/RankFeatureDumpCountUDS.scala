package com.netease.easyml.uds.examples

import com.netease.easyml.common.uds.UDS
import com.netease.easyml.uds.util.LoadFeatureDump.loadAndParseFeatureDf
import com.netease.easyml.uds.util.RankUtil._
import org.apache.spark.sql.SparkSession

/**
 * Created by linjiuning on 2021/6/5.
 * count dump feature.
 */
case class RankFeatureDumpCountUDSArgs(config: String, feature: String, day: String, music: Boolean = false)

object RankFeatureDumpCountUDS extends UDS[RankFeatureDumpCountUDSArgs] {

  def run(spark: SparkSession, args: RankFeatureDumpCountUDSArgs): Unit = {
    val mConfig = readServerConfig(args.config)
    val featureDf = loadAndParseFeatureDf(spark, args.feature, startDay = args.day, endDay = args.day, mConfig, music = args.music)
    println(s"DUMP features line count: ${featureDf.count()}")
  }
}
