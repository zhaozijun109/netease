package com.netease.easyml.uds.examples

import com.netease.easyml.common.cmds.UserDefinedCmd
import com.netease.easyml.common.uds.UDS
import org.apache.spark.sql.{DataFrame, SparkSession}

import java.util

case class EchoArgs(message: String)

object EchoUDS extends UDS[EchoArgs] {
  override def run(spark: SparkSession, args: Args): Unit = {
    spark.sparkContext.parallelize(Seq(args.message)).foreach(println)
  }
}

class Echo extends UserDefinedCmd {
  override def apply(spark: SparkSession, params: util.Map[String, String]): DataFrame = {
    println(params.get("message"))
    null
  }
}
