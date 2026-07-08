package com.netease.lofter.etl.ads

import com.netease.wm.util.Args
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.joda.time.DateTime

object GrowthTraceSource {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Growth Trace Source")
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val dt = pargs.required("date")
    val output = pargs.required("output")
    val callBackOutput = pargs.required("callback")

    Seq(1, 2, 3, 7, 15, 30).foreach { period =>
      runForCallbackUserPeriod(spark, callBackOutput, dt, period)
      runForNewUserPeriod(spark, output, dt, period)
    }
  }

  def runForNewUserPeriod(spark: SparkSession, output: String, dt: String, period: Int) = {
    val start = DateTime.parse(dt).minusDays(period - 1).toString("yyyy-MM-dd")
    val end = dt

    val growthDevice =
      s"""
         |select *
         |from lofter.dwd_ad_growth_device_di
         |where appid = 'lofter' and device_type = 'new' and dt = '$start' and nvl(is_ad_attributed, 1) > 0
         |""".stripMargin

    spark.sql(growthDevice).cache().createOrReplaceTempView("growth_new")

    val activeSql =
      s"""
         |select deviceUdid, count(1) dayCount,
         |       sum(if(dt = '$end', 1, 0)) as nDay
         |from lofter.device_active
         |where dt >= '$start' and dt <= '$end'
         |group by deviceUdid
         |""".stripMargin

    val userLoginSql =
      s"""
         |select g.deviceUdid
         |from growth_new g
         |     join lofter.dwd_device_mapping m on g.deviceUdid = m.sid
         |     join lofter.dim_user u on m.tid = u.id
         |where u.isAnonymous = 0 and
         |      m.sid_tp = 'deviceudid' and m.tid_tp = 'userid' and
         |      from_unixtime(cast(m.firstTime/1000 as bigint), 'yyyy-MM-dd') >= '$start' and
         |      from_unixtime(cast(m.firstTime/1000 as bigint), 'yyyy-MM-dd') <= '$end'
         |group by g.deviceUdid
         |""".stripMargin

    val userRegisterSql =
      s"""
         |select g.deviceUdid
         |from growth_new g
         |     join lofter.dwd_device_mapping m on g.deviceUdid = m.tid
         |     join lofter.dim_user u on m.sid = u.id
         |where u.isAnonymous = 0 and
         |      u.createDate = from_unixtime(cast(m.firstTime/1000 as bigint), 'yyyy-MM-dd') and
         |      m.sid_tp = 'userid' and m.tid_tp = 'deviceudid' and m.firstNo = 1 and
         |      from_unixtime(cast(m.firstTime/1000 as bigint), 'yyyy-MM-dd') >= '$start' and
         |      from_unixtime(cast(m.firstTime/1000 as bigint), 'yyyy-MM-dd') <= '$end'
         |group by g.deviceUdid
         |""".stripMargin

    val sessionSql =
      s"""
         |select g.deviceUdid, s.sessionTimeInMinutes
         |from growth_new g
         |join (
         |        select deviceUdid, sum(sessionTime) / 60000 as sessionTimeInMinutes
         |        from lofter.dws_par_device_session_di
         |        where dt>='$start' and dt<='$end'
         |        group by deviceUdid
         |) s on g.deviceUdid = s.deviceUdid
         |""".stripMargin

    val interactionSql =
      s"""
         |select g.deviceUdid,
         |   count(distinct case when c.opType in('praise','reproduce','recommend','subscribe') then c.opId else null end) hotPv,
         |   count(distinct case when c.opType='commend' then c.opId else null end) commendPv,
         |   count(distinct case when c.opType='post' then c.opId else null end) postPv
         |from growth_new g
         |join lofter.dwd_device_mapping m on m.sid = g.deviceUdid
         |join (
         |  select dt,userid,opType, postId as opId
         |  from lofter.dwd_post_hot_di
         |  where dt>='$start' and dt<='$end'
         |
         |  union all
         |
         |  select dt,userid,'commend' opType,commentId opId
         |  from lofter.dwd_post_response_di
         |  where dt>='$start' and dt<='$end'
         |
         |  union all
         |
         |  select dt,userid,'post' opType, postId opId
         |  from lofter.dwd_post_publish_di
         |  where dt>='$start' and dt<='$end'
         |) c on c.userid = m.tid
         |where m.sid_tp = 'deviceudid' and m.tid_tp = 'userid' and
         |      from_unixtime(cast(m.firstTime/1000 as bigint), 'yyyy-MM-dd') >= '$start' and
         |      from_unixtime(cast(m.firstTime/1000 as bigint), 'yyyy-MM-dd') <= '$end'
         |group by g.deviceUdid
         |""".stripMargin

    spark.sql(activeSql).createOrReplaceTempView("device_active")
    spark.sql(userLoginSql).createOrReplaceTempView("device_login_user")
    spark.sql(userRegisterSql).createOrReplaceTempView("device_register_user")
    spark.sql(sessionSql).createOrReplaceTempView("device_session")
    spark.sql(interactionSql).createOrReplaceTempView("device_interaction")

    val result =
      s"""
         |select  n.deviceOs,
         |        n.advertiserId,
         |        case when n.source = '渠道包归因' then n.appChannel else null end appChannel,
         |        n.campaignId, n.aid, n.cid, n.proxy, n.media,
         |        count(distinct n.deviceUdid) as newUv,
         |        count(distinct u.deviceUdid) as logUv,
         |        count(distinct r.deviceUdid) as regUv,
         |        sum(dayCount) / count(distinct n.deviceUdid) per_activeDays,
         |        count(distinct if(a.dayCount >= 2, a.deviceUdid, null)) as active_2days_uv,
         |        sum(nDay) as n_day_uv,
         |        sum(sessionTimeInMinutes) / count(distinct s.deviceUdid) as per_duration_minutes,
         |        count(distinct s.deviceUdid) as duration_uv,
         |        count(distinct if(t.hotPv > 0 or commendPv > 0, n.deviceUdid, null)) as interactionUv,
         |        count(distinct if(t.hotPv > 0, n.deviceUdid, null)) as hotUv,
         |        sum(t.hotPv) as hotPv,
         |        count(distinct if(t.commendPv > 0, n.deviceUdid, null)) as commendUv,
         |        sum(t.commendPv) as commendPv,
         |        count(distinct if(t.postPv > 0, n.deviceUdid, null)) as postUv,
         |        sum(t.postPv) as postPv
         |from growth_new n
         |     left join device_active a on a.deviceUdid = n.deviceUdid
         |     left join device_login_user u on u.deviceUdid = n.deviceUdid
         |     left join device_register_user r on r.deviceUdid = n.deviceUdid
         |     left join device_session s on s.deviceUdid = n.deviceUdid
         |     left join device_interaction t on t.deviceUdid = n.deviceUdid
         |group by n.deviceOs, n.advertiserId,
         |         case when n.source = '渠道包归因' then n.appChannel else null end,
         |         n.campaignId, n.aid, n.cid, n.proxy, n.media
         |""".stripMargin

    spark.sql(result)
      .repartition(1)
      .write.mode(SaveMode.Overwrite).parquet(s"$output/period=$period/dt=$start")
  }


  def runForCallbackUserPeriod(spark: SparkSession, output: String, dt: String, period: Int) = {
    val start = DateTime.parse(dt).minusDays(period - 1).toString("yyyy-MM-dd")
    val end = dt

    val growthDevice =
      s"""
         |select *
         |from lofter.dwd_ad_growth_device_di
         |where appid = 'lofter' and device_type = 'return' and dt = '$start'
         |""".stripMargin

    spark.sql(growthDevice).cache().createOrReplaceTempView("growth_new")

    val activeSql =
      s"""
         |select deviceUdid, count(1) dayCount,
         |       sum(if(dt = '$end', 1, 0)) as nDay
         |from lofter.device_active
         |where dt >= '$start' and dt <= '$end'
         |group by deviceUdid
         |""".stripMargin

    val userLoginSql =
      s"""
         |select g.deviceUdid
         |from growth_new g
         |     join (
         |        select deviceUdid, userId
         |        from lofter.device_active lateral view explode(userIds) ut as userId
         |        where dt >= '$start' and dt <= '$end'
         |        group by deviceUdid, userId
         |     ) m on g.deviceUdid = m.deviceUdid
         |     join lofter.dim_user u on m.userId = u.id
         |where u.isAnonymous = 0
         |group by g.deviceUdid
         |""".stripMargin

    val userRegisterSql =
      s"""
         |select g.deviceUdid
         |from growth_new g
         |     join lofter.dwd_device_mapping m on g.deviceUdid = m.tid
         |     join lofter.dim_user u on m.sid = u.id
         |where u.isAnonymous = 0 and
         |      u.createDate = from_unixtime(cast(m.firstTime/1000 as bigint), 'yyyy-MM-dd') and
         |      m.sid_tp = 'userid' and m.tid_tp = 'deviceudid' and m.firstNo = 1 and
         |      from_unixtime(cast(m.firstTime/1000 as bigint), 'yyyy-MM-dd') >= '$start' and
         |      from_unixtime(cast(m.firstTime/1000 as bigint), 'yyyy-MM-dd') <= '$end'
         |group by g.deviceUdid
         |""".stripMargin

    val sessionSql =
      s"""
         |select g.deviceUdid, s.sessionTimeInMinutes
         |from growth_new g
         |join (
         |        select deviceUdid, sum(sessionTime) / 60000 as sessionTimeInMinutes
         |        from lofter.dws_par_device_session_di
         |        where dt>='$start' and dt<='$end'
         |        group by deviceUdid
         |) s on g.deviceUdid = s.deviceUdid
         |""".stripMargin

    val interactionSql =
      s"""
         |select g.deviceUdid,
         |   count(distinct case when c.opType in('praise','reproduce','recommend','subscribe') then c.opId else null end) hotPv,
         |   count(distinct case when c.opType='commend' then c.opId else null end) commendPv,
         |   count(distinct case when c.opType='post' then c.opId else null end) postPv
         |from growth_new g
         |join (
         |   select deviceUdid, userId
         |   from lofter.device_active lateral view explode(userIds) ut as userId
         |   where dt >= '$start' and dt <= '$end'
         |   group by deviceUdid, userId
         |) m on m.deviceUdid = g.deviceUdid
         |join (
         |  select dt,userid,opType,postId as opId
         |  from lofter.dwd_post_hot_di
         |  where dt>='$start' and dt<='$end'
         |
         |  union all
         |
         |  select dt,userid,'commend' opType,commentId opId
         |  from lofter.dwd_post_response_di
         |  where dt>='$start' and dt<='$end'
         |
         |  union all
         |
         |  select dt,userid,'post' opType,postId opId
         |  from lofter.dwd_post_publish_di
         |  where dt>='$start' and dt<='$end'
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
         |select  n.deviceOs,
         |        n.advertiserId,
         |        case when n.source = '渠道包归因' then n.appChannel else null end appChannel,
         |        n.campaignId, n.aid, n.cid, n.proxy, n.media,
         |        count(distinct n.deviceUdid) as callbackUv,
         |        count(distinct u.deviceUdid) as logUv,
         |        count(distinct r.deviceUdid) as regUv,
         |        sum(dayCount) / count(distinct n.deviceUdid) per_activeDays,
         |        count(distinct if(a.dayCount >= 2, a.deviceUdid, null)) as active_2days_uv,
         |        sum(nDay) as n_day_uv,
         |        sum(sessionTimeInMinutes) / count(distinct s.deviceUdid) as per_duration_minutes,
         |        count(distinct s.deviceUdid) as duration_uv,
         |        count(distinct if(t.hotPv > 0 or commendPv > 0, n.deviceUdid, null)) as interactionUv,
         |        count(distinct if(t.hotPv > 0, n.deviceUdid, null)) as hotUv,
         |        sum(t.hotPv) as hotPv,
         |        count(distinct if(t.commendPv > 0, n.deviceUdid, null)) as commendUv,
         |        sum(t.commendPv) as commendPv,
         |        count(distinct if(t.postPv > 0, n.deviceUdid, null)) as postUv,
         |        sum(t.postPv) as postPv
         |from growth_new n
         |     left join device_active a on a.deviceUdid = n.deviceUdid
         |     left join device_login_user u on u.deviceUdid = n.deviceUdid
         |     left join device_register_user r on r.deviceUdid = n.deviceUdid
         |     left join device_session s on s.deviceUdid = n.deviceUdid
         |     left join device_interaction t on t.deviceUdid = n.deviceUdid
         |group by n.deviceOs, n.advertiserId,
         |         case when n.source = '渠道包归因' then n.appChannel else null end,
         |         n.campaignId, n.aid, n.cid, n.proxy, n.media
         |""".stripMargin

    spark.sql(result)
      .repartition(1)
      .write.mode(SaveMode.Overwrite).parquet(s"$output/period=$period/dt=$start")
  }
}
