package com.netease.lofter.data.jobs.mail

import com.github.nscala_time.time.Imports
import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}

object MboJob {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)

    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .enableHiveSupport()
      .getOrCreate()

    val dt = pargs.optional("date").getOrElse(Imports.DateTime.yesterday.toString("yyyy-MM-dd"))
    val dayAgo = DateTime.parse(dt).minusDays(1).toString("yyyy-MM-dd")
    val monthAgo = DateTime.parse(dt).minusDays(30).toString("yyyy-MM-dd")

    spark.sql("create temporary function bitmap_union as 'com.netease.wm.udf.bitmap.BitmapUnionUDAF'")
    spark.sql("create temporary function bitmap_count as 'com.netease.wm.udf.bitmap.BitmapCountUDF'")

    val dstr =
      s"""
         |select '$dt' as dt,
         |  count(distinct if(item_type='ARTICLE' and cost_time > 5000, concat(item_id,user_id), null)) as dstr_text_photo_pv,
         |  count(distinct if(item_type='VIDEO' and cost_time > 3000, concat(item_id,user_id), null)) dstr_video_pv
         |from rec.rec_data_noblog_action_di_user_item
         |where day='$dt' and length(item_id) > 0
         |""".stripMargin

    val rev =
      s"""
         |select '$dt' as dt,
         |       count(distinct userid) users_pay,
         |       sum(money) money_pay,
         |       count(distinct case when type="充值乐乎币" then userid end) users_recharge
         |from (
         |  select buyerid userid, amount as money,"购买市集" as type
         |  from lofter_db_dump.ods_db_benefit_trade_nd
         |  where status in(1,3,4) and
         |      from_unixtime(cast(createTime/1000 as bigint),'yyyy-MM-dd') ='$dt' and
         |      status in(1,3,4)
         |
         | union all
         |
         |  select userid, amount as money, "充值乐乎币" as type
         |  from lofter_db_dump.ods_db_trade_buy_coin_order_nd
         |  where status=1 and
         |      from_unixtime(cast(createtime/1000 as bigint),'yyyy-MM-dd')= '$dt'
         |
         |  union all
         |
         |  select userid, rewardAmount as money, "打赏" as type
         |  from lofter_db_dump.ods_db_trade_reward_order_nd
         |  where status=10 and from_unixtime(cast(createtime as bigint), 'yyyy-MM-dd') = '$dt'
         |)t
         |""".stripMargin

    val social =
      s"""
         |select '$dt' as dt,
         |      bitmap_count(bitmap_union(hd_device_bitmap)) as interaction_uv,
         |      sum(response_pv) / sum(praise_pv) as comment_to_praise_ratio
         |from lofter.dws_post_interaction_di
         |where dt = '$dt'
         |""".stripMargin

    val content =
      s"""
         |select '$dt' as dt,
         |       count(distinct case when isPublished=true and isForbidden=false and isCitedPost=false and allowView=0 then a.id end ) post_count,
         |       count(distinct case when isPublished=true and isForbidden=false and isCitedPost=false and allowView=0 then a.blogid end ) post_user_count,
         |       count(distinct case when isPublished=true and isForbidden=false and isCitedPost=false and allowView=0 and (a1.type<>3 or a1.type is null) then a.id end ) real_post_count,
         |       count(distinct case when isPublished=true and isForbidden=false and isCitedPost=false and allowView=0 and (a1.type<>3 or a1.type is null) then a.blogid end ) real_post_user_count
         |from lofter.dim_post a
         |  left join (
         |     select b.type,a.id
         |     from lofter_db_dump.ods_db_ask_answer_post_nd a
         |       left join lofter_db_dump.ods_db_ask_question_nd b on a.questionId=b.id
         |     group by  b.type,a.id
         |  ) a1 on a.id=a1.id
         |where publishDate = '$dt' and
         |   ((moveFrom not in("blog","lofternetease","blog163like","loftmove","BLOGPOST","bbs","photo-pp","163_news","instagram_mirror","weibo_sync","news","pp","netease_photo" )
         |     and moveFrom not like "%blog%" and  moveFrom not like "%move%") or moveFrom is null) and
         |     isImported = 0
         |""".stripMargin

    val creator =
      s"""
         |select '$dt' as dt, sum(fans) / count(distinct userId) avg_creator_new_fans
         |from (
         | select dt, userId, fans_std - lag(fans_std, 1) over (partition by userId order by dt) as fans
         | from lofter.dws_par_creator_level_scoring_dd
         | where dt >= '$dayAgo' and dt <= '$dt' and level in("A","B","S","C")
         |) a
         |where dt = '$dt'
         |""".stripMargin

    val contentHot =
      s"""
         |select '$dt' as dt, count(distinct postid) as post_count_surpass_1w_hot
         |from (
         |    select dt, postid, hot,
         |           lag(hot, 1) over (partition by postId order by dt) as prevHot
         |    from lofter.dws_post_interaction_dd
         |    where dt >= '$dayAgo' and dt <= '$dt'
         |) t
         |where dt = '$dt' and hot >= 10000 and prevHot < 10000
         |""".stripMargin

    val session =
      s"""
         |select '$dt' as dt, sum(sessionTime) / count(distinct deviceUdid) / 60000 as avg_session_minutes
         |from lofter.dws_par_device_session_di
         |where dt = '$dt'
         |""".stripMargin

    val retention1d =
      s"""
         |select '$dt' as dt,
         |       count(distinct if(a.is_retain > 0, a.deviceUdid, null)) / count(distinct a.deviceUdid) active_retain_1d,
         |       count(distinct if(a.is_retain > 0 and b.is_paid_subscribe = 0, a.deviceUdid, null))/ count(distinct if(b.is_paid_subscribe = 0, b.deviceUdid, null)) as new_retain_1d,
         |       count(distinct if(a.is_retain > 0 and b.is_paid_subscribe > 0, a.deviceUdid, null))/ count(distinct if(b.is_paid_subscribe > 0, b.deviceUdid, null)) as paid_subscribe_new_retain_1d
         |from (
         |    select deviceUdid, is_retain from lofter.device_retain where dt ='$dt' and period = 1
         |) a left join (
         |    select deviceUdid, is_paid_subscribe
         |    from lofter.dwd_device_growth_attribution_di
         |    where dt='$dayAgo' and device_type = 'new'
         |) b on a.deviceUdid = b.deviceUdid
         |""".stripMargin

    val retention30d =
      s"""
         |select '$dt' as dt, count(distinct if(is_retain > 0, deviceUdid, null)) / count(distinct deviceUdid) as active_retain_30d
         |from lofter.device_retain
         |where dt ='$dt' and period = 29
         |""".stripMargin

    val active =
      s"""
         |select '$dt' as dt, count(distinct a.deviceUdid) dau, count(distinct b.deviceUdid) as new_device_count
         |from (
         |    select deviceUdid from lofter.device_active where dt ='$dt'
         |) a left join (select deviceUdid from lofter.device_new where  dt='$dt') b on a.deviceUdid = b.deviceUdid
         |""".stripMargin

    spark.sql(dstr).createOrReplaceTempView("dstr")
    spark.sql(rev).createOrReplaceTempView("rev")
    spark.sql(social).createOrReplaceTempView("social")
    spark.sql(creator).createOrReplaceTempView("creator")
    spark.sql(content).createOrReplaceTempView("content")
    spark.sql(contentHot).createOrReplaceTempView("content_hot")
    spark.sql(session).createOrReplaceTempView("session")
    spark.sql(retention1d).createOrReplaceTempView("retention1")
    spark.sql(retention30d).createOrReplaceTempView("retention30")
    spark.sql(active).createOrReplaceTempView("active")

    val result =
      """
        |select a.dau, a.new_device_count,
        |       b.active_retain_1d, b.new_retain_1d,
        |       c.active_retain_30d, d.avg_session_minutes,
        |       e.post_count, e.post_user_count, e.real_post_count, e.real_post_user_count,
        |       f.post_count_surpass_1w_hot, g.dstr_text_photo_pv, g.dstr_video_pv,
        |       s.interaction_uv, s.comment_to_praise_ratio, t.avg_creator_new_fans,
        |       r.users_pay, r.money_pay, users_recharge,
        |       b.paid_subscribe_new_retain_1d
        |from active a
        |  join retention1 b on a.dt = b.dt
        |  join retention30 c on a.dt = c.dt
        |  join session d on a.dt = d.dt
        |  join content e on a.dt = e.dt
        |  join content_hot f on a.dt = f.dt
        |  join dstr g on a.dt = g.dt
        |  join social s on a.dt = s.dt
        |  join rev r on a.dt = r.dt
        |  left join creator t on a.dt = t.dt
        |""".stripMargin

    spark.sql(result)
      .withColumn("dt", lit(dt))
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_mbo_di")

    spark.close()
  }
}
