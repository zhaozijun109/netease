package com.netease.vc.data.ads

import com.netease.wm.util.Args
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.joda.time.DateTime

object GrowthAdPeriodCvr {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Growth Trace Source")
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val dt = pargs.required("date")

    Seq(1, 2, 3, 7, 15, 30).foreach { period =>
      runForNewUserPeriod(spark, dt, period)
    }

    spark.sql(
     s"""
        |insert overwrite table vc_dm.ads_vc_growth_ad_period_cvr_di
        |select device_type,deviceos,advertiserid,appchannel,campaignid,aid,cid,media,proxy,custom_ouid,photoid,newuv,loguv,reguv,per_activedays,active_2days_uv,n_day_uv,per_duration_minutes,interactionuv,hotuv,hotpv,commenduv,commendpv,postuv,postpv,duration_uv,excellent_uv,impounding_uv,whiteboard_uv,photo_url,photo_caption,star_user_id,star_name,invest_amount,period,dt
        |from vc.ads_vc_growth_ad_period_cvr_tmp
        |where dt = '$dt'
        |""".stripMargin
    )
  }

  def runForNewUserPeriod(spark: SparkSession, dt: String, period: Int) = {
    val start = DateTime.parse(dt).minusDays(period - 1).toString("yyyy-MM-dd")
    val end = dt

    val deviceUsers =
      s"""
         |select deviceUdid, userId
         |from vc.dwd_device_active_di
         |     lateral view explode(userIds) as userId
         |where dt >= '$start' and dt <= '$end'
         |group by 1, 2
         |""".stripMargin

      spark.sql(deviceUsers).createOrReplaceTempView("device_user")

    val growthDevice =
      s"""
         |select deviceudid,deviceos,appversion,appchannel,source,matchtype,proxy,media,advertiserid,campaignid,aid,cid,appid,IF(device_type = 'return','return_30',device_type) as device_type,is_ad_attributed,customudid,custom_ouid,photoid,dt
         |from lofter.dwd_ad_growth_device_di
         |where dt = '$start' and nvl(is_ad_attributed, 1) > 0 and appid = 'virtualapp'
         |
         |""".stripMargin

    spark.sql(growthDevice).cache().createOrReplaceTempView("growth_new")

    val activeSql =
      s"""
         |select deviceUdid, count(1) dayCount,
         |       sum(if(dt = '$end', 1, 0)) as nDay
         |from vc.dwd_device_active_di
         |where dt >= '$start' and dt <= '$end'
         |group by deviceUdid
         |""".stripMargin

    val userLoginSql =
      s"""
         |select g.deviceUdid
         |from growth_new g
         |     join device_user m on g.deviceUdid = m.deviceUdid
         |     join vc.dim_user u on m.userId = u.id
         |where u.is_anonymous = 0
         |group by g.deviceUdid
         |""".stripMargin

    val userRegisterSql =
      s"""
         |select g.deviceUdid
         |from growth_new g
         |     join device_user m on g.deviceUdid = m.deviceUdid
         |     join vc.dim_user u on m.userId = u.id
         |where u.is_anonymous = 0 and
         |      u.create_date >= '$start' and u.create_date <= '$end'
         |group by g.deviceUdid
         |""".stripMargin

    val sessionSql =
      s"""
         |select g.deviceUdid, s.sessionTimeInMinutes
         |from growth_new g
         |join (
         |        select deviceUdid, sum(sessionTime) / 60000 as sessionTimeInMinutes
         |        from vc.dws_par_device_session_di
         |        where dt>='$start' and dt<='$end'
         |        group by deviceUdid
         |) s on g.deviceUdid = s.deviceUdid
         |""".stripMargin

    val interactionSql =
      s"""
         |select g.deviceUdid, sum(message_count) as message_count
         |from growth_new g
         |join device_user m on m.deviceUdid = g.deviceUdid
         |join (
         |  select user_id as userId, count(1) as message_count
         |  from vc.ods_db_vc_user_message_record_nd
         |  where from_unixtime(cast(create_time/1000 as bigint), 'yyyy-MM-dd') >='$start' and
         |        from_unixtime(cast(create_time/1000 as bigint), 'yyyy-MM-dd') <='$end' and
         |        sender = 2
         |  group by 1
         |) c on c.userid = m.userId
         |group by g.deviceUdid
         |""".stripMargin

    spark.sql(activeSql).createOrReplaceTempView("device_active")
    spark.sql(userLoginSql).createOrReplaceTempView("device_login_user")
    spark.sql(userRegisterSql).createOrReplaceTempView("device_register_user")
    spark.sql(sessionSql).createOrReplaceTempView("device_session")
    spark.sql(interactionSql).createOrReplaceTempView("device_interaction")

    val result =
      s"""
         |insert overwrite table vc.ads_vc_growth_ad_period_cvr_tmp partition(dt = '$dt', period = $period)
         |select x.*, z.video_url as photo_url, z.caption as photo_caption, z.star_user_id, z.star_name, y.invest_amount
         |from (
         |    select  n.device_type,
         |            n.deviceOs,
         |            n.advertiserId,
         |            case when n.source = '渠道包归因' then n.appChannel else null end appChannel,
         |            n.campaignId, n.aid, n.cid, n.media, n.proxy,
         |            n.custom_ouid, n.photoId,
         |            count(distinct n.deviceUdid) as newUv,
         |            count(distinct u.deviceUdid) as logUv,
         |            count(distinct r.deviceUdid) as regUv,
         |            sum(dayCount) / count(distinct n.deviceUdid) per_activeDays,
         |            count(distinct if(a.dayCount >= 2, a.deviceUdid, null)) as active_2days_uv,
         |            sum(nDay) as n_day_uv,
         |            sum(sessionTimeInMinutes) / count(distinct s.deviceUdid) as per_duration_minutes,
         |            count(distinct if(t.message_count > 0, n.deviceUdid, null)) as interactionUv,
         |            0 as hotUv,
         |            0 as hotPv,
         |            0 as commendUv,
         |            0 as commendPv,
         |            0 as postUv,
         |            sum(t.message_count) as postPv,
         |            count(distinct s.deviceUdid) as duration_uv,
         |            count(distinct e.deviceUdid) as excellent_uv,
         |            count(distinct m.deviceUdid) as impounding_uv,
         |            count(distinct if(w.is_active > 0, null, n.deviceUdid)) as whiteboard_uv
         |    from growth_new n
         |         left join device_active a on a.deviceUdid = n.deviceUdid
         |         left join device_login_user u on u.deviceUdid = n.deviceUdid
         |         left join device_register_user r on r.deviceUdid = n.deviceUdid
         |         left join device_session s on s.deviceUdid = n.deviceUdid
         |         left join device_interaction t on t.deviceUdid = n.deviceUdid
         |         left join (
         |             select b.deviceUdid
         |             from vc.ods_db_vc_user_message_record_nd a join device_user b on a.user_id = b.userId
         |             group by 1
         |         ) e on n.deviceUdid = e.deviceUdid
         |         left join (
         |             select deviceUdid
         |             from vc.dwd_device_active_di
         |             where dt = '$end'
         |             group by 1
         |         ) m on n.deviceUdid = m.deviceUdid
         |         left join (
         |             select a.deviceUdid, 1 as is_active
         |             from device_user a join vc.ods_db_vc_user_message_record_nd b on a.userId = b.user_id
         |             group by 1
         |         ) w on n.deviceUdid = w.deviceUdid
         |    group by n.device_type, n.deviceOs, n.advertiserId,
         |             case when n.source = '渠道包归因' then n.appChannel else null end,
         |             n.campaignId, n.aid, n.cid, n.proxy, n.media, n.custom_ouid, n.photoId
         |) x
         |left join (
         |    select task_id, campaign_id, video_url, caption, star_user_id, star_name, sum(consume_amount_yuan) as invest_amount
         |    from vc.ods_log_ad_linkup_ks_stat_di
         |    where dt>='$start' and dt<='$end'
         |    group by 1, 2, 3, 4, 5, 6
         |) y on x.aid = y.task_id and x.campaignId = y.campaign_id
         |left join (
         |    select task_id, campaign_id, video_url, caption, star_user_id, star_name
         |    from vc.ods_log_ad_linkup_ks_stat_di
         |    where dt <= '$end'
         |    group by 1, 2, 3, 4, 5, 6
         |) z on x.aid = z.task_id and x.campaignId = z.campaign_id
         |""".stripMargin

    spark.sql(result)
  }

}
