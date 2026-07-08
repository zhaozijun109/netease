package com.netease.easyml.uds.examples

import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.SparkUtil
import com.netease.easyml.uds.examples.{RankDiscretizeUDS => Discretize}
import com.netease.easyml.uds.util.LoadFeatureDump.loadAndParseFeatureDf
import com.netease.easyml.uds.util.RankUtil
import com.netease.easyml.uds.util.RankUtil._
import org.apache.spark.sql.SparkSession

/**
 * Created by linjiuning on 2021/6/5.
 * discretize dump feature.
 */
case class RankFeatureDumpDiscretizeArgs(sql: String, output: String, modelConfig: String, config: String,
                                         feature: String = SparkUtil.NULL, numBucket: Int = NUM_BUCKETS,
                                         startDay: String = SparkUtil.NULL, endDay: String = SparkUtil.NULL,
                                         env: String = "", isFea: Boolean = false, music: Boolean = false)

object RankFeatureDumpDiscretizeUDS extends UDS[RankFeatureDumpDiscretizeArgs] {

  def run(spark: SparkSession, args: RankFeatureDumpDiscretizeArgs): Unit = {
    val mConfig = readServerConfig(args.modelConfig)
    var configs = readConfig(args.config, nameAsEmbedName = true)
      .filter(it => it.featureColumn.contains(BUCKET))
    if (args.isFea) {
      configs = configs.filter(_.isFea)
    }
    val keys = (configs.map(_.name) ++ Array(SESSION_ID, USER_ID, ITEM_ID)).distinct

    if (!args.feature.equals(SparkUtil.NULL)) {
      val featureDf = loadAndParseFeatureDf(spark, args.feature, args.startDay, args.endDay, mConfig, keys = Some(keys), music = args.music)
      featureDf.createOrReplaceTempView("a")
    }

    val result = RankUtil.sql(spark, args.sql, args.env).repartition(200)
    result.createOrReplaceTempView("input")
    val nArgs = new Discretize.Args(input = "input", output = args.output, config = args.config, numBucket = args.numBucket, isFea = args.isFea)
    Discretize.run(spark, nArgs)
  }
}
