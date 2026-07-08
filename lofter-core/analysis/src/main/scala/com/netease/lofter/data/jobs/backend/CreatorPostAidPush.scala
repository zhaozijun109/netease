package com.netease.lofter.data.jobs.backend

import com.github.nscala_time.time.Imports._
import com.netease.lofter.data.common.kafkaConfig
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.lit

/**
 * OMLOFTER-54781: 创作者分等级文章扶持私信
 * 1. 计算推荐场景T+7曝光数据
 * 2. 根据不同创作等级设定不同的推送阈值
 */
object CreatorPostAidPush {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(DateTime.yesterday().toString("yyyy-MM-dd"))

    val sql =
      s"""
         |select *
         |from lofter_dm.ads_par_creator_post_aid_push_di
         |where dt = '$date'
         |""".stripMargin

    spark.sql(sql)
      .withColumn("time", lit(System.currentTimeMillis()))
      .selectExpr("CAST(blogId as STRING) as key", """concat('{"userId":', blogId, ',"time":', time, ',"stimulateType": "creator_post_aid"', ',"data": {"postId":', postId, ',"hot":', actionCount, ',"sense_type":"', sense_type, '","praises":', likeCount, ',"exposeCount": ', exposeCount,'}}') as value""")
      .write
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaConfig.BOOTSTRAP_SERVERS)
      .option("topic", "lofter.creator-stimulus-pm.staging")
      .save()

    spark.close()

  }
}
