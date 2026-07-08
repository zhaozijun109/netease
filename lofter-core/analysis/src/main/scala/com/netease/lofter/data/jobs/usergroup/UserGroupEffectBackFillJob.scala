package com.netease.lofter.data.jobs.usergroup

import com.github.nscala_time.time.Imports.DateTime
import com.netease.lofter.data.common.databases
import com.netease.lofter.data.common.spark.SparkSqlImplicits.rowParam
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

import java.sql.Connection

/**
 * back user group effect of prev 30 days
 */
object UserGroupEffectBackFillJob {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val yesterday = DateTime.yesterday.toString("yyyy-MM-dd")
    val dt = pargs.optional("date").getOrElse(yesterday)
    val theDay = DateTime.parse(dt).plusDays(1).toString("yyyy-MM-dd")
    val monthAgo = DateTime.parse(dt).minusDays(30).toString("yyyy-MM-dd")

    import com.netease.wm.util.Sql._

    spark.sql("set spark.sql.crossJoin.enabled=true")
    spark.sql("set hive.exec.max.dynamic.partitions=3000")

    spark.read
      .format("jdbc")
      .option("url", "jdbc:mysql://10.59.186.164:6000/public_mirror_gz")
      .option("dbtable", "Cmb_CrowdPackage")
      .option("user", "lofter_bi")
      .option("password", "WndkIpgkr")
      .option("driver", "com.mysql.jdbc.Driver")
      .load()
      .cache()
      .createOrReplaceTempView("realtime_cmb_crowd_packages")

    spark.sql(s"select id as job_id from realtime_cmb_crowd_packages where status = 0 and from_unixtime(cast(createTime/1000 as bigint), 'yyyy-MM-dd') <= '$theDay' and from_unixtime(cast(createTime/1000 as bigint), 'yyyy-MM-dd') >= '$dt' ")
      .cache().createOrReplaceTempView("new_user_group")

    spark.sql(s"select job_id, dt from new_user_group a cross join (select rollingDate as dt from lofter.dim_date_rolling where period=30 and id = '$dt' ) b ")
      .cache().createOrReplaceTempView("new_user_group_daily")

    def creator(): Unit = {
      val creatorSql =
        s"""
           |insert overwrite table lofter_dm.ads_user_group_creator_effect_di
           |select count(distinct a.userId) as user_count,
           |       count(distinct if(b.level = 'S', b.userId, null)) level_s_creators,
           |       count(distinct if(b.level = 'A', b.userId, null)) level_a_creators,
           |       count(distinct if(b.level = 'B', b.userId, null)) level_b_creators,
           |       count(distinct if(b.level = 'C', b.userId, null)) level_c_creators,
           |       count(distinct if(b.level = 'D', b.userId, null)) level_d_creators,
           |       count(distinct if(b.level = 'D*', b.userId, null)) level_d_star_creators,
           |       count(distinct if(size(c.authdomainids) > 0, c.id, null)) daren_creators,
           |       count(distinct d.userId) as active_creators,
           |       count(distinct if( e.post_count_1d > 0, e.userId,null)) as post_creators,
           |       sum(e.post_count_1d) as post_count_1d,
           |       sum(e.post_count_7d) as post_count_7d,
           |       sum(e.post_count_30d) as post_count_30d,
           |       sum(f.revenue) as revenue,
           |       sum(g.fans) as new_fans,
           |       sum(h.receive_hot) as receive_hot,
           |       sum(j.clickPv) as click_pv,
           |       sum(j.browsePv) as browse_pv,
           |       sum(j.exposurePv) as exposure_pv,
           |       sum(j.playPv) as play_pv,
           |       count(distinct if(j.clickPv > 0, j.userId, null)) click_uv,
           |       count(distinct if(j.browsePv > 0, j.userId, null)) browse_uv,
           |       count(distinct if(j.exposurePv > 0, j.userId, null)) exposure_uv,
           |       count(distinct if(j.playPv > 0, j.userId, null)) play_uv,
           |       sum(m.exposurePv) as folder_exposure_pv,
           |       sum(m.clickPv) as folder_click_pv,
           |       sum(m.browsePv) as folder_browse_pv,
           |       sum(j.hdpv) as hd_pv,
           |       count(distinct if(j.hdPv > 0, j.userId, null)) hd_uv,
           |       sum(k.exposurePv) as collection_exposure_pv,
           |       sum(k.clickPv) as collection_click_pv,
           |       sum(k.browsePv) as collection_browse_pv,
           |       x.dt as dt,
           |       a.job_id
           |from (
           |    select *
           |    from lofter.dwd_user_group_user_list_di
           |    where dt='$dt'
           |) a join new_user_group_daily x on a.job_id = x.job_id
           |    left join (select * from lofter.dws_par_creator_dd where dt >= '$monthAgo' and dt <= '$dt') b on a.userId = b.userId and x.dt = b.dt
           |    left join lofter.dim_blog c on a.userId = c.id
           |    left join (select dt, userId from lofter.dws_par_user_active_di where dt >= '$monthAgo' and dt <= '$dt' group by dt, userId) d on a.userId = d.userId and x.dt = d.dt
           |    left join (select dt, userId, post_count_1d, post_count_7d, post_count_30d from lofter.dws_par_user_post_dd where dt >= '$monthAgo' and dt <= '$dt') e on a.userId = e.userId and x.dt = e.dt
           |    left join (select dt, userId, nvl(receive_gift_amount_1d,0) + nvl(receive_reward_amount_1d,0) as revenue from lofter.dws_par_user_revenue_dd where dt >= '$monthAgo' and dt <= '$dt') f on a.userId = f.userId and x.dt = f.dt
           |    left join (select from_unixtime(cast(followTime/1000 as bigint), 'yyyy-MM-dd') as dt, blogId, count(distinct userId) fans from lofter_db_dump.ods_db_user_following_nd where from_unixtime(cast(followTime/1000 as bigint), 'yyyy-MM-dd') <= '$dt' and from_unixtime(cast(followTime/1000 as bigint), 'yyyy-MM-dd') >= '$monthAgo' group by from_unixtime(cast(followTime/1000 as bigint), 'yyyy-MM-dd'), blogId) g on a.userId = g.blogId and x.dt = g.dt
           |    left join (select dt, userId, receive_hot from lofter.dws_par_creator_interaction_di where dt >= '$monthAgo' and dt <= '$dt') h on a.userId = h.userId and x.dt = h.dt
           |    left join (select dt, userId, sum(clickPv) clickPv, sum(browsePv) browsePv, sum(exposurePv) exposurePv, sum(playPv) playPv, sum(hdpv) hdPv from lofter.dws_post_base_stats_di where dt >= '$monthAgo' and dt <= '$dt' group by dt, userId) j on a.userId = j.userId and x.dt = j.dt
           |    left join (select dt, folderUserId , sum(isExposure) as exposurePv, sum(isClick) clickPv, sum(isBrowse) as browsePv from lofter.dwd_userfolder_action_di where dt >= '$monthAgo' and dt <= '$dt' group by dt, folderUserId) m on a.userId = m.folderUserId and x.dt = m.dt
           |    left join (select dt, post_userid , sum(if(action_type = 'expose', 1, 0)) as exposurePv, sum(if(action_type = 'click', 1, 0)) clickPv, sum(if(action_type = 'browse', 1, 0)) as browsePv from lofter.dwd_post_collection_di where dt >= '$monthAgo' and dt <= '$dt' group by dt, post_userid) k on a.userId = k.post_userid and x.dt = k.dt
           |group by x.dt, a.job_id
           |""".stripMargin

      spark.sql(creatorSql)
    }

    def consumer(): Unit = {
      val consumerSql =
        s"""
           |insert overwrite table lofter_dm.ads_user_group_consumer_effect_di
           |select count(distinct a.userId) user_count,
           |       sum(b.post_count) as post_count, sum(b.video_count) video_count,
           |       sum(if(b.session_time > 24 * 3600 * 1000, 0, b.session_time)) session_time,
           |       sum(c.click_ad_count) as click_ad_count,
           |       sum(d.send_like_cnt) as praise_count,
           |       sum(d.send_comment_cnt) as comment_count,
           |       count(distinct if(send_hot > 0 or send_comment_cnt > 0, d.userId, null)) as interaction_uv,
           |       count(distinct if(e.gift_reward_amount > 0, e.userId, null)) as buy_uv,
           |       count(distinct if(e.send_gift_amount > 0, e.userId, null)) as gift_uv,
           |       count(distinct if(e.send_reward_amount > 0, e.userId, null)) as reward_uv,
           |       sum(e.gift_reward_amount) as buy_amount,
           |       sum(e.send_gift_amount) as gift_amount,
           |       sum(e.send_reward_amount) as reward_amount,
           |       sum(f.subscribe_tag_cnt) as subscribe_tag_cnt,
           |       sum(g.subscribe_collection_cnt) as subscribe_collection_cnt,
           |       sum(h.followCount) as followCount,
           |       count(distinct j.userId) as firstPostUv,
           |       count(distinct k.userId) as activeUv,
           |       count(distinct if(b.live_count > 0, b.userId, null)) as live_uv,
           |       x.dt as dt,
           |       a.job_id
           |from (
           |    select *
           |    from lofter.dwd_user_group_user_list_di
           |    where dt='$dt'
           |) a join new_user_group_daily x on a.job_id = x.job_id
           |  left join (
           |  select dt, userId, sum(browse_text_photo_post_count) as post_count,
           |         sum(browse_video_count) as video_count, sum(session_time) as session_time,
           |         0 as live_count
           |  from lofter.dws_par_user_content_di where dt >= '$monthAgo' and dt <= '$dt'
           |  group by dt, userId
           |) b on a.userId = b.userId and x.dt = b.dt
           |  left join (
           |    select dt, userId, click_ad_count
           |    from lofter.dws_par_user_ad_di
           |    where dt >= '$monthAgo' and dt <= '$dt'
           |) c on a.userId = c.userId and x.dt = c.dt
           |  left join (
           |    select dt, userId, send_like_cnt, send_comment_cnt, send_hot
           |    from lofter.dws_par_user_interaction_di
           |    where dt >= '$monthAgo' and dt <= '$dt'
           |  ) d on a.userId = d.userId and x.dt = d.dt
           |  left join (
           |    select dt, userId, nvl(send_gift_amount_1d,0) + nvl(send_reward_amount_1d,0) as gift_reward_amount,
           |            send_gift_amount_1d as send_gift_amount, send_reward_amount_1d as send_reward_amount
           |    from lofter.dws_par_user_revenue_dd
           |    where dt >= '$monthAgo' and dt <= '$dt'
           |  ) e on a.userId = e.userId and x.dt = e.dt
           |  left join (
           |      select from_unixtime(cast(addTagTime/1000 as bigint), 'yyyy-MM-dd') as dt, userId, count(distinct tagName) subscribe_tag_cnt
           |      from lofter_db_dump.ods_db_favorite_tag_nd
           |      where from_unixtime(cast(addTagTime/1000 as bigint), 'yyyy-MM-dd') >= '$monthAgo' and
           |            from_unixtime(cast(addTagTime/1000 as bigint), 'yyyy-MM-dd') <= '$dt'
           |      group by from_unixtime(cast(addTagTime/1000 as bigint), 'yyyy-MM-dd'), userId
           |  ) f on a.userId = f.userId and x.dt = f.dt
           |  left join (
           |    select from_unixtime(cast(createTime/1000 as bigint), 'yyyy-MM-dd') as dt, userId, count(distinct collectionId) subscribe_collection_cnt
           |    from lofter_db_dump.ods_db_subscribe_collection_nd
           |    where from_unixtime(cast(createTime/1000 as bigint), 'yyyy-MM-dd') >= '$monthAgo' and
           |          from_unixtime(cast(createTime/1000 as bigint), 'yyyy-MM-dd') <= '$dt'
           |    group by from_unixtime(cast(createTime/1000 as bigint), 'yyyy-MM-dd'), userId
           |  ) g on a.userId = g.userId and x.dt = g.dt
           |  left join (
           |    select from_unixtime(cast(followTime/1000 as bigint), 'yyyy-MM-dd') as dt, userId, count(distinct blogId) followCount
           |    from lofter_db_dump.ods_db_user_following_nd
           |    where from_unixtime(cast(followTime/1000 as bigint), 'yyyy-MM-dd') >= '$monthAgo' and
           |          from_unixtime(cast(followTime/1000 as bigint), 'yyyy-MM-dd') <= '$dt'
           |    group by from_unixtime(cast(followTime/1000 as bigint), 'yyyy-MM-dd'), userId
           |  ) h on a.userId = h.userId and x.dt = h.dt
           |  left join (
           |    select dt, userId
           |    from lofter.dws_par_creator_di
           |    where dt >= '$monthAgo' and dt <= '$dt' and
           |          is_first_publish > 0
           |  ) j on a.userId = j.userId and x.dt = j.dt
           |  left join (
           |    select dt, userId
           |    from lofter.dws_par_user_active_di
           |    where dt >= '$monthAgo' and dt <= '$dt'
           |    group by dt, userId
           |  ) k on a.userId = k.userId and x.dt = k.dt
           |group by x.dt, a.job_id
           |""".stripMargin

      spark.sql(consumerSql)
    }

    def retention(): Unit = {
      val retention1 =
        s"""
           |insert overwrite table lofter_dm.ads_user_group_retention_di
           |    select c.baseDate as base_date, a.job_id, count(distinct a.userId) as user_count, count(distinct c.userId) as retention_user_count,
           |           c.dt, 1 as period
           |    from (
           |        select job_id, userId
           |        from lofter.dwd_user_group_user_list_di
           |        where dt='$dt'
           |    ) a join new_user_group b on a.job_id = b.job_id
           |        left join (select userId, baseDate, dt from lofter.dwd_user_retention_di where dt <= '$dt' and dt >= '$monthAgo' and period = 1) c
           |             on a.userId = c.userId
           |    group by a.job_id, c.dt, c.baseDate
           |    distribute by 1
           |""".stripMargin

      val retention3 =
        s"""
           |insert overwrite table lofter_dm.ads_user_group_retention_di
           |    select c.baseDate as base_date, a.job_id, count(distinct a.userId) as user_count, count(distinct c.userId) as retention_user_count,
           |           c.dt, 2 as period
           |    from (
           |        select job_id, userId
           |        from lofter.dwd_user_group_user_list_di
           |        where dt='$dt'
           |    ) a join new_user_group b on a.job_id = b.job_id
           |        left join (select userId, baseDate, dt from lofter.dwd_user_retention_di where dt <= '$dt' and dt >= '$monthAgo' and period = 2) c
           |             on a.userId = c.userId
           |    group by a.job_id, c.dt, c.baseDate
           |    distribute by 1
           |""".stripMargin

      val retention7 =
        s"""
           |insert overwrite table lofter_dm.ads_user_group_retention_di
           |    select c.baseDate as base_date, a.job_id, count(distinct a.userId) as user_count, count(distinct c.userId) as retention_user_count,
           |           c.dt, 6 as period
           |    from (
           |        select job_id, userId
           |        from lofter.dwd_user_group_user_list_di
           |        where dt='$dt'
           |    ) a join new_user_group b on a.job_id = b.job_id
           |        left join (select userId, baseDate, dt from lofter.dwd_user_retention_di where dt <= '$dt' and dt >= '$monthAgo' and period = 6) c
           |             on a.userId = c.userId
           |    group by a.job_id, c.dt, c.baseDate
           |    distribute by 1
           |""".stripMargin

      spark.sql(retention1)
      spark.sql(retention3)
      spark.sql(retention7)
    }

    def syncData(): Unit = {
      implicit val conn: Connection = databases.getDDBConn

      val creatorSql =
        s"""
           |select cast(a.job_id as bigint) as packId,
           |       cast(replace(dt,'-', '') as bigint) as `time`,
           |       level_s_creators,
           |       level_a_creators,
           |       level_b_creators,
           |       level_c_creators,
           |       daren_creators,
           |       active_creators,
           |       active_creators / user_count as activePeopleCount,
           |       post_creators,
           |       post_count_1d / user_count as postCountAvg,
           |       post_count_7d / user_count as postFreqWeekAvg,
           |       post_count_30d / user_count as postFreqMonthAvg,
           |       revenue / user_count as rewardMoneyAvg,
           |       new_fans / user_count as fansGrowAvg,
           |       receive_hot / user_count as receiveHotAvg,
           |       exposure_pv / user_count as exposureCountAvg,
           |       click_pv / user_count as clickCountAvg,
           |       browse_pv / user_count as browseCountAvg,
           |       play_pv / user_count as playVideoCountAvg,
           |       folder_exposure_pv / user_count as recommendCollectionExposureCountAvg,
           |       folder_click_pv / user_count as recommendCollectionClickCountAvg,
           |       folder_browse_pv / user_count as recommendCollectionBrowseCountAvg,
           |       click_pv / nvl(exposure_pv,1) as clickAvgRate,
           |       hd_pv / nvl(exposure_pv,1) as interactiveAvgRate,
           |       collection_exposure_pv / user_count as collectionExposureCountAvg,
           |       collection_click_pv / user_count as collectionClickCountAvg,
           |       collection_browse_pv / user_count as collectionBrowseCountAvg,
           |       level_d_creators, level_d_star_creators
           |from lofter_dm.ads_user_group_creator_effect_di a
           |     join new_user_group b on a.job_id = b.job_id
           |where a.dt <= '$dt'
           |""".stripMargin

      val consumerSql =
        s"""
           |select cast(a.job_id as bigint) as packId,
           |       cast(replace(dt,'-', '') as bigint) as `time`,
           |       follow_count / user_count as followCountAvg,
           |       subscribe_tag_cnt / user_count as subscribeTagCountAvg,
           |       subscribe_collection_cnt / user_count as subscribeCollectionCountAvg,
           |       first_post_uv as firstPostPeopleCount,
           |       post_count / user_count as browsePostCountAvg,
           |       video_count / user_count as playVideoCountAvg,
           |       session_time / 1000.0 / user_count as stayTimeAvg,
           |       click_ad_count / user_count as clickAdvertisingCountAvg,
           |       comment_count / praise_count as commentLickRatio,
           |       live_uv as watchLivePeopleCount,
           |       interaction_uv as interactivePeopleCount,
           |       reward_uv as rewardPeopleCount,
           |       gift_uv as giftPeopleCount,
           |       buy_uv as buyPeopleCount,
           |       reward_amount / user_count as rewardMoneyAvg,
           |       gift_amount / user_count as giftMoneyAvg,
           |       buy_amount / user_count as buyMoneyAvg
           |from lofter_dm.ads_user_group_consumer_effect_di a
           |      join new_user_group b on a.job_id = b.job_id
           |where a.dt <= '$dt'
           |""".stripMargin

      spark.sql(creatorSql).collect().foreach { row =>
        sql"""delete from Cmb_CreatorObserveData where packId = ${"packId"} and time = ${"time"}""".update(rowParam(row))

        sql"""
             |insert ignore into Cmb_CreatorObserveData(id,packId,time,sLevelPeopleCount,aLevelPeopleCount,bLevelPeopleCount,cLevelPeopleCount,expertPeopleCount,activePeopleCount,activePeopleCountRatio,postPeopleCount,postCountAvg,postFreqWeekAvg,postFreqMonthAvg,rewardMoneyAvg,fansGrowAvg,receiveHotAvg,exposureCountAvg,clickCountAvg,browseCountAvg,playVideoCountAvg,collectionExposureCountAvg,collectionClickCountAvg,collectionBrowseCountAvg,recommendCollectionExposureCountAvg,recommendCollectionClickCountAvg,recommendCollectionBrowseCountAvg,clickAvgRate,interactiveAvgRate,dLevelPeopleCount,dAsteriskLevelPeopleCount)
             |values(seq,${"packId"},${"time"},${"level_s_creators"},${"level_a_creators"},${"level_b_creators"},${"level_c_creators"},${"daren_creators"},${"active_creators"},${"activePeopleCount"},${"post_creators"},${"postCountAvg"},${"postFreqWeekAvg"},${"postFreqMonthAvg"},${"rewardMoneyAvg"},${"fansGrowAvg"},${"receiveHotAvg"},${"exposureCountAvg"},${"clickCountAvg"},${"browseCountAvg"},${"playVideoCountAvg"},${"collectionExposureCountAvg"},${"collectionClickCountAvg"},${"collectionBrowseCountAvg"},${"recommendCollectionExposureCountAvg"},${"recommendCollectionClickCountAvg"},${"recommendCollectionBrowseCountAvg"},${"clickAvgRate"},${"interactiveAvgRate"},${"level_d_creators"},${"level_d_star_creators"})
        """.stripMargin.update(rowParam(row))
      }

      spark.sql(consumerSql).collect().foreach { row =>
        sql"""delete from Cmb_ConsumerObserveData where packId = ${"packId"} and time = ${"time"}""".update(rowParam(row))

        sql"""
             |insert ignore into Cmb_ConsumerObserveData(id,packId,time,followCountAvg,subscribeTagCountAvg,subscribeCollectionCountAvg,firstPostPeopleCount,browsePostCountAvg,playVideoCountAvg,stayTimeAvg,clickAdvertisingCountAvg,commentLickRatio,watchLivePeopleCount,interactivePeopleCount,rewardPeopleCount,giftPeopleCount,buyPeopleCount,rewardMoneyAvg,giftMoneyAvg,buyMoneyAvg)
             |values(seq,${"packId"},${"time"},${"followCountAvg"},${"subscribeTagCountAvg"},${"subscribeCollectionCountAvg"},${"firstPostPeopleCount"},${"browsePostCountAvg"},${"playVideoCountAvg"},${"stayTimeAvg"},${"clickAdvertisingCountAvg"},${"commentLickRatio"},${"watchLivePeopleCount"},${"interactivePeopleCount"},${"rewardPeopleCount"},${"giftPeopleCount"},${"buyPeopleCount"},${"rewardMoneyAvg"},${"giftMoneyAvg"},${"buyMoneyAvg"})
             |""".stripMargin.update(rowParam(row))
      }

      val retention1dSql =
        s"""
           |select a.job_id, cast(replace(base_date,'-', '') as bigint) as `time`, retention_user_count / user_count as r1
           |from lofter_dm.ads_user_group_retention_di a
           |     join new_user_group b on a.job_id = b.job_id
           |where a.dt <='$dt' and a.period = 1
           |""".stripMargin

      spark.sql(retention1dSql).collect().foreach { row =>
        sql"""
             |update Cmb_ConsumerObserveData set retentionD1 = ${"r1"} where packId = ${"job_id"} and time = ${"time"}
             |""".stripMargin.update(rowParam(row))
      }

      val retention2dSql =
        s"""
           |select a.job_id, cast(replace(base_date,'-', '') as bigint) as `time`, retention_user_count / user_count as r2
           |from lofter_dm.ads_user_group_retention_di a
           |     join new_user_group b on a.job_id = b.job_id
           |where a.dt <= '$dt' and a.period = 2
           |""".stripMargin

      spark.sql(retention2dSql).collect().foreach { row =>
        sql"""
             |update Cmb_ConsumerObserveData set retentionD3 = ${"r2"} where packId = ${"job_id"} and time = ${"time"}
             |""".stripMargin.update(rowParam(row))
      }

      val retention6dSql =
        s"""
           |select a.job_id, cast(replace(base_date,'-', '') as bigint) as `time`, retention_user_count / user_count as r6
           |from lofter_dm.ads_user_group_retention_di a
           |      join new_user_group b on a.job_id = b.job_id
           |where a.dt <= '$dt' and a.period = 6
           |""".stripMargin

      spark.sql(retention6dSql).collect().foreach { row =>
        sql"""
             |update Cmb_ConsumerObserveData set retentionD7 = ${"r6"} where packId = ${"job_id"} and time = ${"time"}
             |""".stripMargin.update(rowParam(row))
      }

      conn.close()
    }

    creator()
    consumer()
    retention()
    syncData()
    spark.close()
  }
}
