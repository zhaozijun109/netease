package com.netease.lofter.data.jobs.backend

import com.github.nscala_time.time.Imports._
import com.netease.lofter.data.common.kafkaConfig
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.lit

object CreatorCallbackStimulus {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(DateTime.yesterday().toString("yyyy-MM-dd"))

    val creatorCallBack =
      s"""
        |select * from lofter_dm.ads_creator_callback_push_di where dt = '$date'
        |""".stripMargin

    spark.sql(creatorCallBack)
      .withColumn("stimulusTime", lit(System.currentTimeMillis()))
      .selectExpr("CAST(userId as STRING) as key", """concat('{"userId":', userId, ',"time":', stimulusTime, ',"stimulateType": "creator_callback"', ',"data": {"praises":', nvl(praises, 'null'), ', "callback_type": "', callback_type, '","hot":', hot_30d, ',"like":', like_30d, ',"subscribe":', subscribe_30d ,', "hottest_tag": "', nvl(tags,''), '", "fan_loyalist":', nvl(loyalistUserId, 'null'), ',"fan_loyalist_hot":', nvl(loyalistHot,'null'), ',"fan_first_support":', nvl(firstFollowUserId,'null'), ',"fan_last_praise":', nvl(lastPraiseUserId, 'null'), ',"fan_last_waiting":', nvl(lastFollowWaiting, 'null'), ', "fan_last_waiting_days":', nvl(lastFollowWaitingDays, 'null'),'}}') as value""")
      .write
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaConfig.BOOTSTRAP_SERVERS)
      .option("topic", "lofter.creator-stimulus-pm.staging")
      .save()

    spark.stop()
  }
}
