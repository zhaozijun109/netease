package com.netease.easyml.uds.examples

import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.SparkUtil
import org.apache.spark.sql.SparkSession

/**
 * Created by linjiuning on 2022/8/20.
 */
case class SQLArgs(file: String, env: String = "")

object SQLUDS extends UDS[SQLArgs] {

  def run(spark: SparkSession, args: SQLArgs): Unit = {
    val envs = SparkUtil.parseEnv(args.env)
    SparkUtil.sql(spark, args.file, envs)
  }
}
