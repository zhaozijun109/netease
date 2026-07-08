package com.netease.wm.ad

import com.github.nscala_time.time.Imports._
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

object AdxConversionFunnel {

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Conversion Funnel")
      .config("spark.sql.parquet.binaryAsString", true)
      .getOrCreate()

    val yesterday = DateTime.yesterday.toString("yyyy-MM-dd")
    val day = pargs.optional("date").getOrElse(yesterday)

    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")
    spark.sql("create temporary function version_compare as 'com.netease.wm.udf.VersionCompare'")

    //TODO:从配置或者数据库中读取，不要写到代码中
    val sdkAdsource = "(1016,1027,1022,1028,1037,1042)"

    val sqlConversionFunnel =
      s"""
         |select
         |  appId,null,null,
         |  max(positionName) as positionName,
         |  positionId,
         |  dspid,os,version,ex_info1,ex_info2,
         |  count(distinct case when store_count > 0 then req_id else null end) store_count,
         |  count(distinct case when valid_store_count > 0 then req_id else null end) as valid_store_count,
         |  count(distinct case when fillCount > 0 then req_id else null end) as fill_count,
         |  count(distinct case when nonFilteredCount > 0 then req_id else null end) as nonFilteredCount,
         |  count(distinct case when client_fill_count > 0 then req_id else null end) as client_fill_count,
         |  count(distinct case when client_win_count > 0 then req_id else null end) as client_win_count,
         |  count(distinct case when bgPv > 0 then req_id else null end) as bgPv,
         |  count(distinct case when clickPv > 0 then req_id else null end) as clickPv,
         |  count(distinct case when client_bg_count > 0 then req_id else null end) as client_bg_count,
         |  count(distinct case when client_click_count > 0 then req_id else null end) as client_click_count,
         |  count(distinct case when bg_bid_amount > 0 then req_id else null end) as bg_bid_amount,
         |  count(distinct case when dp_count > 0 then req_id else null end) as dp_count,
         |  count(distinct case when dp_success_count > 0 then req_id else null end) as dp_success_count,
         |  count(distinct case when dp_has_app_count > 0 then req_id else null end) as dp_has_app_count,
         |  from_cache
         |from (
         |  SELECT
         |    appId,userid,
         |         funnel_req_id as req_id,
         |         funnel_position_id as positionId,
         |         max(funnel_position_name) as positionName,
         |         os,version,
         |    case when dspid is null then '' else dspid end as dspid,
         |    ex_info1,ex_info2,
         |    max(case when if(from_cache > 0, 1, store) > 0 then 1 else 0 end) store_count,
         |    max(case when requestCount > 0 then 1 else 0 end) valid_store_count,
         |    max(case
         |          when requestCount > 0 and (
         |              (dspId not in $sdkAdsource and bid_amount > 0 )
         |              or (dspId in $sdkAdsource and float(ext['client_bid_price']) > 0)
         |          )  then 1
         |          else 0
         |      end
         |    ) fillCount,
         |    max(case
         |          when requestCount > 0 and (
         |              (dspId not in $sdkAdsource and bid_amount > 0 and winpv > 0 and (ext["bidFloorFilter"] is null or ext["bidFloorFilter"] != 1))
         |              or (dspId in $sdkAdsource and float(ext['client_bid_price']) > 0 and float(ext['client_bid_price']) >= float(ext['client_bid_floor']))
         |          )  then 1
         |          else 0
         |      end
         |    )  nonFilteredCount,
         |    max(case when client_fill_count > 0 then 1 else 0 end) client_fill_count,
         |    max(case when bgPv >0 then client_win_price*client_win_factor else 0 end) bg_bid_amount,
         |    max(case when client_win_count > 0 then 1 else 0 end) client_win_count,
         |    max(case when bgPv > 0 then 1 else 0 end) bgPv,
         |    max(case when clickPv > 0 then 1 else 0 end) clickPv,
         |    max(case when client_bg_count > 0 then 1 else 0 end) client_bg_count,
         |    max(case when client_click_count > 0 then 1 else 0 end) client_click_count,
         |    max(case when clickPv >0 and ext["hasDeeplink"] = 1 then 1 else 0 end) dp_count,
         |    max(case when clickPv >0 and ext["hasDeeplink"] = 1 and dp_has_app_count > 0 then 1 else 0 end) dp_has_app_count,
         |    max(case when clickPv >0 and ext["hasDeeplink"] = 1 and dp_count > 0 then 1 else 0 end) dp_success_count,
         |    max(from_cache) as from_cache
         |  from (
         |      select *,
         |             nvl(cache_req_id, req_id) as funnel_req_id,
         |             nvl(cache_position_id, positionId) as funnel_position_id,
         |             nvl(cache_position_name, positionName) as funnel_position_name,
         |             if(cache_req_id is not null, 1, 0) as from_cache
         |      from lofter.dwd_ad_actions_di
         |      where dt = '$day'
         |    union all
         |      select *,
         |             req_id as funnel_req_id,
         |             positionId as funnel_position_id,
         |             positionName as funnel_position_name,
         |             0 as from_cache
         |      from lofter.dwd_ad_actions_di
         |      where dt = '$day' and cache_req_id is not null
         |  ) t
         |  where if(from_cache > 0, 1, store) > 0
         |  and (appId != '6ED29071' or version_compare(version, '8.0.0') >= 0)
         |  and dspid != 0
         |  group by 1,2,3,4,6,7,8,9,10
         |) a
         |group by appId,positionId,os,version,dspid,ex_info1,ex_info2,from_cache
         |GROUPING sets((appId,positionId,os,version,dspid,ex_info1,ex_info2,from_cache),(appId,positionId,os,version,ex_info1,ex_info2,from_cache))
         """.stripMargin

    val adConversionFunnel = spark.sql(sqlConversionFunnel)
      .repartition(1)
      .cache()
      .createOrReplaceTempView("funnel")


    spark.sql(
      s"""
         |insert overwrite table lofter_dm.ads_ad_conversion_funnels_di partition(dt = '$day')
         |select appid, b.category as adcategory, null as location,positionname,positionid,dspid,os,version,
         |       ex_info1,ex_info2,
         |       store_count,valid_store_count,fill_count,nonfilteredcount,
         |       client_fill_count,client_win_count,bgpv,clickpv,client_bg_count,client_click_count,
         |       bg_bid_amount,dp_count,dp_success_count,dp_has_app_count,
         |       from_cache
         |from funnel a
         |left join (select id, category from lofter_db_dump.ods_db_ad_position_nd) b on cast(a.positionId as bigint) = b.id
         |""".stripMargin
    )

    spark.close()
  }
}
