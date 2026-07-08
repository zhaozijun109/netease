package com.netease.wm.ad

import com.github.nscala_time.time.Imports._
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

object AdxDsp {

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Adx DSP")
      .config("spark.sql.parquet.binaryAsString", true)
      .getOrCreate()

    val yesterday = DateTime.yesterday.toString("yyyy-MM-dd")
    val day = pargs.optional("date").getOrElse(yesterday)

    val sqlStatistical =
      s"""
         |select
         |   appId,
         |   nvl(cache_position_name, positionName) as positionName,
         |   nvl(cache_position_id, positionId) as positionId, os, dspId, slotId, advertiser_type,version,
         |   sum(requestCount) requestCount,
         |   sum(fillCount) fillCount,
         |   sum(timeoutCount) timeoutCount,
         |   sum(winPv) winPv,
         |   sum(forbiddenCount) forbiddenCount,
         |   sum(case when bgPv > 0 then bid_amount else 0 end) bid_amount,
         |   sum(client_fill_count) client_fill_count,
         |   sum(client_win_count) client_win_count,
         |   sum(bgPv) bgPv,
         |   sum(clickPv) clickPv
         |from lofter.dwd_ad_actions_di
         |where dt = '$day'
         |group by 1, 2, 3, 4, 5, 6, 7, 8
      """.stripMargin

    val adStatistical = spark.sql(sqlStatistical)
      .repartition(1)
      .cache()
      .createOrReplaceTempView("statistical")


    spark.sql(
      s"""
         |insert overwrite table lofter_dm.ads_ad_dsp_di partition(dt = '$day')
         |select * from statistical
         |""".stripMargin
    )

    spark.close()
  }
}
