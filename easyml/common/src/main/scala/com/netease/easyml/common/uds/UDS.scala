package com.netease.easyml.common.uds

import com.netease.easyml.common.util.{ArgsUtil, ConvertUtil}
import org.apache.spark.internal.Logging
import org.apache.spark.sql.SparkSession

/**
 * Created by linjiuning on 2022/9/19.
 */
abstract class UDS[T: Manifest] extends Serializable with Logging {
  type Args = T

  def run(spark: SparkSession, args: Array[String]): Unit = {
    val params = ArgsUtil.parse(args)
    val cArgs = ConvertUtil.fromMap[T](params)
    println(s"Args: ${ConvertUtil.toJson(cArgs)}")
    run(spark, cArgs)
  }

  def run(spark: SparkSession, args: Args): Unit
}
