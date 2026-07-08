package com.netease.wm.ad

import com.github.nscala_time.time.Imports._
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

object AdxAction {

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Ad Actions")
      .config("spark.sql.parquet.binaryAsString", true)
      .getOrCreate()

    val yesterday = DateTime.yesterday.toString("yyyy-MM-dd")
    val day = pargs.optional("date").getOrElse(yesterday)
    val dayAgo = DateTime.parse(day).minusDays(1).toString("yyyy-MM-dd")

    spark.sql("create temporary function version_compare as 'com.netease.wm.udf.VersionCompare'")

    val cacheReqMeta =
     s"""
        |SELECT  a.cache_req_id, b.positionName, b.positionId
        |from (
        |  select case when eventid in ('ad-67','ad-69','ad-73') and get_json_object(params['ext'], '$$.fromCache') = TRUE then get_json_object(params['ext'], '$$.bindReqId') else null end as cache_req_id
        |  from lofter.ods_mda_app_partition_di
        |  where dt = '$day' and actionType = 'advertisement' and eventId in ('ad-67','ad-69','ad-73') and
        |     get_json_object(params['ext'], '$$.fromCache') = TRUE
        |  union all
        |   select case when eventid in ('ad-67','ad-69','ad-73') and get_json_object(params['ext'], '$$.fromCache') = TRUE then get_json_object(params['ext'], '$$.bindReqId') else null end as cache_req_id
        |   from avg.ods_mda_app_raw_di
        |   where dt = '$day' and eventid in ('ad-67','ad-69','ad-73') and get_json_object(params['ext'], '$$.fromCache') = TRUE
        |) a
        |left join (
        |   select reqid, max(positionName) as positionName, max(positionId) as positionId
        |   from lofter.ods_log_ad_dsp_di
        |   where dt <= '$day' and dt >= date_sub('$day',1) and length(reqid) > 0
        |   group by reqid
        |) b on a.cache_req_id = b.reqid
        |""".stripMargin

    spark.sql(cacheReqMeta).cache().createOrReplaceTempView("cache_req_meta")

    val merge_ext_func = "aggregate(collect_list(ext), cast(null as map<string, string>), (acc, x) -> if(acc is null, x, map_concat(acc,x))) "

    val sqlDetail =
      s"""
         |select
         |   appId,null as deviceudid, a.positionName, a.positionId, os, adId, dspId, a.slotId, advertiser_type, a.reqid as req_id,version,adtrace,adtracename,
         |   requestCount,fillCount,timeoutCount,winPv,forbiddenCount,
         |   COALESCE(bidPrice*bidFactor,bid_amount) as bid_amount,
         |   c.fill_count as client_fill_count,
         |   win_count as client_win_count,bgPv,clickPv,wakeupboot,
         |   case
         |    when a.ext is null then c.ext
         |    when c.ext is null then a.ext
         |    else map_concat(a.ext,c.ext)
         |   end as ext,
         |   a.userid as userid,client_bg_count,client_click_count,dp_count,
         |   uuid,bidPrice as client_win_price,bidFactor as client_win_factor,dp_has_app_count,
         |   c.cache_req_id
         |from (
         |   select appId, positionName, positionId, os, adId, dspId, slotId, advertiser_type,uuid,reqid,version,wakeupboot,blogid as userid,COLLECT_LIST(ext)[0] as ext,
         |      max(if(msg not like '%504%', 1, 0)) requestCount,
         |      max(case when success=1 then 1 else 0 end) fillCount,
         |      max(case when success=0  and (msg like '%504%' or msg = '读取超时') then 1 else 0 end) timeoutCount,
         |      max(case when winflag=1 then 1 else 0 end) winPv,
         |      max(case when success=1 and winflag=0 and msg='素材被屏蔽' then 1 else 0 end) forbiddenCount,
         |      max(price * bidfactor) as bid_amount
         |   from lofter.ods_log_ad_dsp_di
         |   where dt = '$day'
         |   and appId in ('6ED29071','1L9KQGDL','Y8KJDFYR')
         |   group by appId, positionName, positionId, os, adId, dspId, slotId, advertiser_type, uuid,version,wakeupboot,reqid,blogid
         |) a
         |left join (
         |   select requestUuid, adSource,max(adtrace) as adtrace,max(adtracename) as adtracename,
         |    max(case when expose=1 then 1 else 0 end) bgPv,
         |    max(case when click=1 then 1 else 0 end) clickPv
         |   from lofter.ods_log_ad_client_di
         |   where dt = '$day'
         |   group by requestUuid, adSource
         |) b
         |on a.uuid = b.requestUuid and a.dspId = b.adSource
         |left join (
         |   select
         |      ad_source,req_uid,
         |      max(fill_count) as fill_count,
         |      max(win_count) as win_count,
         |      max(client_bg_count) as client_bg_count,
         |      max(client_click_count) as client_click_count,
         |      max(dp_count) as dp_count,
         |      max(dp_has_app_count) as dp_has_app_count,
         |      max(bidPrice) as bidPrice,
         |      max(bidFactor) as bidFactor,
         |      $merge_ext_func as ext,
         |      max(cache_req_id) as cache_req_id
         |   from(
         |      select
         |         deviceudid,userid,
         |         params["adId"] as adId,
         |         params["ad_source"] as ad_source,
         |         params["slotId"] as slotId,
         |         params["req_uid"] as req_uid,
         |         params["req_id"] as req_id,
         |         case
         |          when eventid in ('ad-66','ad-68','ad-72')
         |          then map("client_bid_price",get_json_object(params["ext"],"$$.bidPrice"),"client_bid_floor",get_json_object(params["ext"],"$$.bidFloor"))
         |          else map()
         |         end as ext,
         |         case when eventid in ('ad-66','ad-68','ad-72') then 1 else 0 end as fill_count,
         |         case when eventid in ('ad-67','ad-69','ad-73') then 1 else 0 end as win_count,
         |         case when eventid in ('ad-67','ad-66') then get_json_object(params["ext"],"$$.bidPrice") else null end as bidPrice,
         |         case when eventid in ('ad-67','ad-66') then get_json_object(params["ext"],"$$.bidFactor") else null end as bidFactor,
         |         case when eventid in ('ad-1') then 1 else 0 end as client_bg_count,
         |         case when eventid in ('ad-2') then 1 else 0 end as client_click_count,
         |         case when eventid in ('ad-34') then 1 else 0 end as dp_count,
         |         case when eventid in ('ad-81') then 1 else 0 end as dp_has_app_count,
         |         case when eventid in ('ad-67','ad-69','ad-73') and get_json_object(params['ext'], '$$.fromCache') = TRUE then get_json_object(params['ext'], '$$.bindReqId') else null end as cache_req_id
         |      from lofter.ods_mda_app_partition_di
         |      where dt = '$day' and actionType = 'advertisement' and
         |            eventId in ('ad-1','ad-2','ad-34','ad-66','ad-67','ad-68','ad-69','ad-72','ad-73','ad-81')
         |    union all
         |      select deviceudid,userid,
         |         params["adId"] as adId,
         |         params["ad_source"] as ad_source,
         |         params["slotId"] as slotId,
         |         params["req_uid"] as req_uid,
         |         params["req_id"] as req_id,
         |         case
         |          when eventid in ('ad-66','ad-68','ad-72')
         |          then map("client_bid_price",get_json_object(params["ext"],"$$.bidPrice"),"client_bid_floor",get_json_object(params["ext"],"$$.bidFloor"))
         |          else map()
         |         end as ext,
         |         case when eventid in ('ad-66','ad-68','ad-72') then 1 else 0 end as fill_count,
         |         case when eventid in ('ad-67','ad-69','ad-73') then 1 else 0 end as win_count,
         |         case when eventid in ('ad-67','ad-66') then get_json_object(params["ext"],"$$.bidPrice") else null end as bidPrice,
         |         case when eventid in ('ad-67','ad-66') then get_json_object(params["ext"],"$$.bidFactor") else null end as bidFactor,
         |         case when eventid in ('ad-1') then 1 else 0 end as client_bg_count,
         |         case when eventid in ('ad-2') then 1 else 0 end as client_click_count,
         |         case when eventid in ('ad-34') then 1 else 0 end as dp_count,
         |         case when eventid in ('ad-81') then 1 else 0 end as dp_has_app_count,
         |         case when eventid in ('ad-67','ad-69','ad-73') and get_json_object(params['ext'], '$$.fromCache') = TRUE then get_json_object(params['ext'], '$$.bindReqId') else null end as cache_req_id
         |      from avg.ods_mda_app_raw_di
         |      where dt = '$day' and
         |            eventId in ('ad-1','ad-2','ad-34','ad-66','ad-67','ad-68','ad-69','ad-72','ad-73','ad-81')
         |   ) t
         |   group by ad_source,req_uid
         |) c
         |on a.uuid = c.req_uid and a.dspId = c.ad_source
      """.stripMargin

    val adActions = spark.sql(sqlDetail)
    adActions.createOrReplaceTempView("actions")

    val sqlStore =
      s"""
         |  select a.*,b.id as positionId
         |  from(
         |    select
         |      userid,deviceudid,req_id,adCategory,location,max(ex_info1) as ex_info1,max(ex_info2) as ex_info2,deviceos,appversion,
         |      max(case when eventid in ('ad-14','g3-24','b1-45','g1-40') then 1 else 0 end) as store_count
         |    from(
         |      select userId, deviceudid, eventId, deviceos, appversion,
         |        COALESCE(params["req_id"],get_json_object(params["ext"],"$$.req_id")) as req_id,
         |        COALESCE(params["adCategory"],get_json_object(params["ext"],"$$.adCategory")) as adCategory,
         |        COALESCE(params["location"],get_json_object(params["ext"],"$$.location")) as location,
         |        case
         |          when eventid in ('ad-14') then get_json_object(params["ext"],"$$.wakeupboot")
         |          when eventid in ('g3-24') then get_json_object(params["playAgainCount"],"$$.playAgainCount")
         |          else null
         |        end ex_info1,
         |        case
         |          when eventid in ('g3-24') then get_json_object(params["type"],"$$.type")
         |          else null
         |        end ex_info2
         |      from lofter.ods_mda_app_partition_di
         |      where dt = '$day'
         |      and version_compare(appVersion, '8.0.0') >= 0
         |      and eventid in ('b1-45','g1-40','g3-24','ad-14')
         |    union all
         |      select userId, deviceudid, eventId, deviceos, appversion,
         |        COALESCE(params["req_id"],get_json_object(params["ext"],"$$.req_id")) as req_id,
         |        COALESCE(params["adCategory"],get_json_object(params["ext"],"$$.adCategory")) as adCategory,
         |        COALESCE(params["location"],get_json_object(params["ext"],"$$.location")) as location,
         |        case
         |          when eventid in ('ad-14') then get_json_object(params["ext"],"$$.wakeupboot")
         |          when eventid in ('g3-24') then get_json_object(params["playAgainCount"],"$$.playAgainCount")
         |          else null
         |        end ex_info1,
         |        case
         |          when eventid in ('g3-24') then get_json_object(params["type"],"$$.type")
         |          else null
         |        end ex_info2
         |      from avg.ods_mda_app_raw_di
         |      where dt = '$day' and eventId in ('b1-45','g1-40','g3-24','ad-14')
         |    ) t
         |    where req_id is not null and location is not null and adCategory is not null
         |    group by deviceudid,req_id,adCategory,location,deviceos,appversion,userid,ex_info1,ex_info2
         |  )a
         |  join(
         |    select * from lofter_db_dump.ods_db_ad_position_nd
         |  )b
         |  on a.adCategory=b.category and a.location=b.location
         """.stripMargin

    val adStore = spark.sql(sqlStore)
      .createOrReplaceTempView("store")


    val adRes = spark.sql(
      s"""
         |select appId, a.deviceudid, a.positionName, a.positionId, a.os, a.adId, a.dspId, a.slotId, a.advertiser_type, a.req_id, a.version, a.adtrace, a.adtracename,
         |       requestCount,fillCount,timeoutCount,winPv,forbiddenCount,
         |       bid_amount,client_fill_count,client_win_count,bgPv,clickPv,wakeupboot,
         |       ext,a.userid,client_bg_count,client_click_count,dp_count,
         |       uuid, client_win_price, client_win_factor,dp_has_app_count,
         |       b.ex_info1, b.ex_info2, b.store_count,
         |       a.cache_req_id
         |from actions a
         |full outer join store b
         |on a.req_id = b.req_id and a.positionId = b.positionId
         |""".stripMargin
    )

    adRes.createOrReplaceTempView("res")

    spark.sql(
      s"""
        |insert overwrite table lofter.dwd_ad_actions_di partition(dt = '$day')
        |select a.*,
        |       if(a.cache_req_id is not null, b.positionId, null) as cache_position_id,
        |       if(a.cache_req_id is not null, b.positionName, null) as cache_position_name
        |from res a
        |left join cache_req_meta b on nvl(a.cache_req_id, a.req_id) = b.cache_req_id
        |""".stripMargin
    )

    spark.close()
  }
}
