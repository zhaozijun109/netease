package com.netease.wm.ad

import com.github.nscala_time.time.Imports._
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

object AdxDwsPosition {

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter AdxDwsPosition")
      .config("spark.sql.parquet.binaryAsString", true)
      .getOrCreate()

    val yesterday = DateTime.yesterday.toString("yyyy-MM-dd")
    val day = pargs.optional("date").getOrElse(yesterday)

    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")
//    spark.sql("create temporary function version_compare as 'com.netease.wm.udf.VersionCompare'")

    val sqlStatistical =
      s"""
         |SELECT
         |   appId, adCategory, location, os, version,ext1,client_win_adsource,null,client_win_advertiser_type,
         |   sum(storecount),
         |   sum(requestCount),
         |   sum(nonfreeFillCount),
         |   sum(case when server_return_size > 0 then 1 else 0 end),
         |   sum(client_win_count),
         |   sum(bgPv),
         |   sum(clickPv),
         |   sum(client_bg_count),
         |   sum(client_click_count),
         |   sum(bg_bid_amount),
         |   sum(dp_count),
         |   sum(dp_success_count),
         |   sum(nonFilteredCount)
         |from(
         |  select
         |    CONCAT('wakeupboot-',ext['wakeupboot']) as ext1,*
         |  from lofter.dwd_ad_req_di
         |  where dt = '$day'
         |  and storecount > 0
         |)a
         |group by appId,adCategory, location, os,version,client_win_adsource,client_win_advertiser_type,ext1
      """.stripMargin

    val adStatistical = spark.sql(sqlStatistical)
      .repartition(1)
      .cache()
      .createOrReplaceTempView("statistical")


    spark.sql(
      s"""
         |insert overwrite table lofter_dm.ads_ad_position_di partition(dt = '$day')
         |select * from statistical
         |""".stripMargin
    )


    spark.close()
  }
}
