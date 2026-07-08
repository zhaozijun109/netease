package com.netease.wm.ad

import com.github.nscala_time.time.Imports._
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

object AdxRequest {

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter AdxRequest")
      .config("spark.sql.parquet.binaryAsString", true)
      .getOrCreate()

    val yesterday = DateTime.yesterday.toString("yyyy-MM-dd")
    val day = pargs.optional("date").getOrElse(yesterday)

    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")
    spark.sql("create temporary function version_compare as 'com.netease.wm.udf.VersionCompare'")

    val sdkAdsource = "(1016,1027,1022,1028,1037,1042)"
    val sqlStatistical =
      s"""
         |SELECT
         |   a.req_id,appId,deviceudid,a.userid, adCategory,location, deviceos, appversion,map('wakeupboot',a.wakeupboot) as ext,
         |   store_count,
         |   requestCount,
         |   nonfreeFillCount,
         |   server_return_size,
         |   client_win_count,
         |   client_win_adsource,
         |   client_win_slotId,
         |   client_win_advertiser_type,
         |   bgPv,
         |   clickPv,
         |   client_bg_count,
         |   client_click_count,
         |   bg_bid_amount,
         |   dp_count,
         |   dp_success_count,
         |   nonFilteredCount
         |from(
         |   select
         |      userid,deviceudid,req_id,adCategory,location,max(wakeupboot) as wakeupboot,deviceos,appversion,
         |      max(case when eventid in ('ad-80') then retListSize else 0 end) as server_return_size,
         |      max(case when eventid in ('ad-14') then 1 else 0 end) as store_count
         |  from(
         |    select userid,deviceudid,deviceos,appversion, eventId,
         |      params["req_id"] as req_id,
         |      params["adCategory"] as adCategory,
         |      params["location"] as location,
         |      get_json_object(params["ext"],"$$.wakeupboot") wakeupboot,
         |      get_json_object(params["ext"],"$$.retListSize") as retListSize
         |    from lofter.ods_mda_app_partition_di
         |    where dt = '$day' and actionType = 'advertisement' and
         |          version_compare(appVersion, '7.4.2') >= 0 and
         |          eventId in ('ad-80','ad-14')
         |   union all
         |    select userid,deviceudid,deviceos,appversion, eventId,
         |      params["req_id"] as req_id,
         |      params["adCategory"] as adCategory,
         |      params["location"] as location,
         |      get_json_object(params["ext"],"$$.wakeupboot") wakeupboot,
         |      get_json_object(params["ext"],"$$.retListSize") as retListSize
         |    from avg.ods_mda_app_raw_di
         |    where dt = '$day' and
         |          eventId in ('ad-80','ad-14')
         |  ) t
         |  group by deviceudid,req_id,adCategory,location,deviceos,appversion,userid
         |) a
         |left join(
         |   select
         |      appId,userid,req_id,
         |      max(case when client_win_count >0 then dspId else null end) client_win_adsource,
         |      max(case when client_win_count >0 then slotId else null end) client_win_slotId,
         |      max(case when client_win_count >0 then advertiser_type else null end) client_win_advertiser_type,
         |      max(case when requestCount > 0 then 1 else 0 end) requestCount,
         |      max(case
         |            when bid_amount > 0 and requestCount > 0 and(
         |                dspId not in $sdkAdsource
         |                or (dspId in $sdkAdsource and (appId != '6ED29071' or version_compare(version, '7.6.20') >= 0) and client_fill_count > 0)
         |                or (dspId in $sdkAdsource and version_compare(version, '7.6.20') < 0 and client_win_count > 0)
         |            )  then 1
         |            else 0
         |        end
         |      ) nonfreeFillCount,
         |      max(case
         |            when bid_amount > 0 and requestCount > 0 and winpv > 0 and(
         |                dspId not in $sdkAdsource
         |                or (dspId in $sdkAdsource and client_win_count > 0)
         |            )  then 1
         |            else 0
         |        end
         |      ) nonFilteredCount,
         |      max(case when bgPv >0 then client_win_price*client_win_factor else 0 end) bg_bid_amount,
         |      max(case when client_win_count > 0 then 1 else 0 end) client_win_count,
         |      max(case when bgPv > 0 then 1 else 0 end) bgPv,
         |      max(case when clickPv > 0 then 1 else 0 end) clickPv,
         |      max(case when client_bg_count > 0 then 1 else 0 end) client_bg_count,
         |      max(case when client_click_count > 0 then 1 else 0 end) client_click_count,
         |      max(case when clickPv >0 and ext["hasDeeplink"] = 1 then 1 else 0 end) dp_count,
         |      max(case when clickPv >0 and ext["hasDeeplink"] = 1 and dp_count > 0 then 1 else 0 end) dp_success_count
         |   from lofter.dwd_ad_actions_di
         |   where dt = '$day'
         |   and (appId != '6ED29071' or version_compare(version, '7.4.2') >= 0)
         |   group by appId,userid,req_id, positionName, positionId, os,version
         |)b
         |on a.req_id = b.req_id
         |where a.store_count > 0
         """.stripMargin

    val adStatistical = spark.sql(sqlStatistical)
      .repartition(1)
      .cache()
      .createOrReplaceTempView("statistical")


    spark.sql(
      s"""
         |insert overwrite table lofter.dwd_ad_req_di partition(dt = '$day')
         |select * from statistical
         |""".stripMargin
    )


    spark.close()
  }
}
