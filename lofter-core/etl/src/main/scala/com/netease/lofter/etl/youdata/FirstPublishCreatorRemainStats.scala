package com.netease.lofter.etl.youdata

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}

object FirstPublishCreatorRemainStats {

  val batchSize = 100
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter First Publish Creator Retain Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .enableHiveSupport()
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))

    val userRetainDays = Seq(2,7,30)
    val userHotBehaviorRetainDays = Seq(1,6)
    firstPublishCreatorStats(spark,date)

    // stat the retain ratio for 1DaysAgo, 3DaysAgo, 7DaysAgo
    for (daysAgo <- userRetainDays){
      updateFirstPublishUserRetain(spark, date, daysAgo)
    }

    for (daysAgo <- userHotBehaviorRetainDays){
      updateFirstPublishUserHotRetain(spark, date, daysAgo)
    }

  }

  def firstPublishCreatorStats(spark: SparkSession, date: String): Unit = {
    val sql_first_publish_user_and_post =
      s"""
         |select dt,blogid as userId,postid
         |from lofter.dwd_par_creator_first_publish_di
         |where dt='$date' and publishdate='$date'
         |""".stripMargin

    spark.sql(sql_first_publish_user_and_post).cache().createOrReplaceTempView("baseTable")

    val sql_safety_audit =
      s"""
         |select a.dt,avg(a.audit_time/1000)  avg_safety_audit_time
         |from
         |(select dt,postid,blogid,version,uuid,level,post_type,(person_audit_time-machine_audit_time) as audit_time
         |from lofter.dwd_post_audit_di
         |where dt='$date' and machine_status=1 and person_status in(2,3) and person_audit_time > machine_audit_time
         |) a
         |inner join
         |(
         |select blogid,publishdate,postid
         |from
         |(
         |select blogid,publishdate,id postid,publishtime,contenttype,userpostindex,valid,isforbidden,
         |    row_number()over(partition by blogid order by publishtime ) rk
         |from lofter.dim_post
         |where ispublished=true and iscitedpost=false
         |and allowview=0
         |and contenttype in ("图片","文字","视频")
         |and isactivityautopost=0 and isimported=0 and ismoved=0
         |)
         |where rk=1 and publishdate='$date'
         |) b
         |on a.blogid=b.blogid and a.postid=b.postid
         |group by a.dt
         |""".stripMargin

    val sql_enter_rec =
      s"""
         |select dt,avg(times/1000) avg_rec_audit_time,count(distinct blogid) enter_rec_uv
         |from
         |(
         |select a.dt,a.blogid,a.postid,reviewtime-lastPendingTime times
         |from
         |(
         |    select from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') dt,postid,recomstatus,reviewaccount,blogid,createtime lastPendingTime
         |    from lofter_db_dump.ods_db_recommend_review_post_nd
         |    where recomstatus=1
         |    group by blogid,postid,recomstatus,reviewaccount,createtime,reviewtime,lastPendingTime
         |) a
         |inner join
         |(
         |    select postid,createtime reviewtime
         |    from
         |    (select postid,createtime,row_number()over(partition by postid order by createtime) rk
         |    from lofter_db_dump.ods_db_recommend_post_trace_log_nd
         |    where  from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') = '$date'
         |    and content='更新文章【推荐状态】' and operator is not null and operator<>'N/A')
         |    where rk=1
         |) a1
         |on a.postid=a1.postid
         |inner join
         |baseTable b
         |on a.postid=b.postid
         |inner join
         |(
         |select reviewaccount
         |from lofter.zq_lofter_recommendwork_name_zeppelin
         |group by reviewaccount
         |) c
         |on a.reviewaccount=c.reviewaccount
         |group by a.dt,a.blogid,a.postid,times
         |)
         |group by dt
         |""".stripMargin

    val sql_filter_posts =
      s"""
         |select '$date' as dt,count(a.postid) filter_post_num
         |from
         |baseTable b
         |join
         |(
         |select postid,recomstatus,reviewaccount,blogid
         |from lofter_db_dump.ods_db_recommend_review_post_nd
         |group by blogid,postid,recomstatus,reviewaccount,createtime
         |) a
         |on a.blogid=b.userId
         |left join
         |(
         |select reviewaccount
         |from lofter.zq_lofter_recommendwork_name_zeppelin
         |group by reviewaccount
         |) c
         |on a.reviewaccount=c.reviewaccount
         |where  c.reviewaccount is null
         |""".stripMargin

    val sql_post_exposure =
      s"""
         |select a.dt,count(distinct a.userId) as uv,
         |    avg(centralized_exposurepv) avg_centralized_exposure_pv,
         |    avg(decentralized_exposurepv) avg_decentralized_exposure_pv
         |from
         |baseTable a
         |left join
         |(
         |select postId,
         |      sum(centralized_expose_pv) as centralized_exposurepv,
         |	    sum(non_centralized_expose_pv) as decentralized_exposurepv
         |from lofter.dws_post_traffic_di
         |where dt='$date'
         |group by postId
         |) b
         |on a.postid = b.postId
         |group by a.dt
         |""".stripMargin

    val sql_post_hot =
      s"""
         |select  a.dt,avg(realpv) avg_real_pv,avg(hotpv) avg_hot_pv,avg(commentpv) avg_comment_pv,avg(sharepv) avg_share_pv
         |from
         |baseTable a
         |left join
         |(
         |    select postid,sum(realbrowsepv+realplaypv) as realpv,sum(poshotpv-neghotpv) hotpv,
         |           sum(sharepv) sharepv,sum(poscommentpv-negcommentpv) commentpv
         |    from lofter.dws_post_base_stats_di
         |    where dt = '$date'
         |    group by postid
         |) b
         |on a.postid=b.postid
         |group by a.dt
         |""".stripMargin

    spark.sql(sql_enter_rec).createOrReplaceTempView("enterRec")
    spark.sql(sql_filter_posts).createOrReplaceTempView("filterPost")
    spark.sql(sql_post_exposure).createOrReplaceTempView("postExposure")
    spark.sql(sql_post_hot).createOrReplaceTempView("postHot")
    spark.sql(sql_safety_audit).createOrReplaceTempView("safeAudit")

    val sql_merge =
      s"""
         |select a.uv,avg_centralized_exposure_pv,avg_decentralized_exposure_pv,
         |    avg_real_pv,avg_hot_pv,avg_comment_pv,avg_share_pv,
         |    avg_rec_audit_time, enter_rec_uv,
         |    filter_post_num,avg_safety_audit_time
         |from postExposure a
         |left join postHot b on a.dt=b.dt
         |left join enterRec c on a.dt=c.dt
         |left join filterPost d on a.dt=d.dt
         |left join safeAudit e on a.dt=e.dt
         |""".stripMargin

    spark.sql(sql_merge)
      .withColumn("dt", lit(date))
      .repartition(1)
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_creator_first_publish_di")
  }

  def updateFirstPublishUserRetain(spark: SparkSession, date: String, daysAgo: Int): Unit = {
    val newDay = DateTime.parse(date).minusDays(daysAgo).toString("yyyy-MM-dd")
    val remainStartDay = DateTime.parse(newDay).plusDays(1).toString("yyyy-MM-dd")

    val sql_retain_active_and_post =
      s"""
         |select '$newDay' as baseDate,$daysAgo as period,
         |        count(distinct b.userId) as remain_uv,
         |        count(distinct c.userId) as remain_post_uv,
         |        sum(d.post_count) as remain_post_count
         |from
         |(
         |select dt,blogId as userid
         |from lofter.dwd_par_creator_first_publish_di
         |where dt='$newDay' and publishdate='$newDay'
         |group by  dt,blogId
         |) a
         |left join
         |(
         |select userid
         |from lofter.device_active
         |lateral view explode(userids) t1 as userid
         |where dt between '$remainStartDay' and '$date'
         |group by userid
         |) b
         |on a.userid=b.userid
         |left join
         |(
         |select userid
         |from lofter.dws_par_creator_di
         |where dt between '$remainStartDay' and '$date' and post_count>0
         |group by userid
         |) c
         |on a.userid=c.userid
         |left join
         |(select userId, post_count, dt
         |from lofter.dws_par_creator_di
         |where dt between '$newDay' and '$date' and post_count>0) d
         |on a.userid=d.userId
       """.stripMargin

    spark.sql(sql_retain_active_and_post).createOrReplaceTempView("userRetain")

    val sql_user_level =
      s"""
         |select '$newDay' as baseDate,level,count(b.userid) as level_uv
         |from
         |(
         |select dt,blogId as userid
         |from lofter.dwd_par_creator_first_publish_di
         |where dt='$newDay'
         |group by dt,blogId
         |) a
         |left join
         |(
         |select dt, userId, level
         |from lofter.dws_par_creator_dd
         |where dt='$date'
         |) b
         |on a.userid=b.userId
         |group by level
         |""".stripMargin

    spark.sql(sql_user_level).createOrReplaceTempView("t1")

    val sql_level_uv =
      s"""
         |select baseDate,$daysAgo as period,
         |  max(case when level='C' then level_uv else 0 end) as level_c_uv,
         |  max(case when level='D' then level_uv else 0 end) as level_d_uv,
         |  max(case when level='D*' then level_uv else 0 end) as level_d_start_uv
         |from t1
         |group by baseDate
         |""".stripMargin

    spark.sql(sql_level_uv).createOrReplaceTempView("userLevel")

    val sql_user_fans =
      s"""
         |select '$newDay' as baseDate,$daysAgo as period,avg(c.ljfans-b.ljfans) avg_add_fans
         |from
         |(
         |select blogId as userid
         |from lofter.dwd_par_creator_first_publish_di
         |where dt='$newDay'
         |group by  userid
         |) a
         |left join
         |(
         |select userId, fans_std as ljfans
         |from lofter.dws_par_creator_dd
         |where dt = '$newDay'
         |) b
         |on a.userid=b.userId
         |left join
         |(
         |select userId, fans_std as ljfans
         |from lofter.dws_par_creator_dd
         |where dt = '$date'
         |) c
         |on a.userid=c.userId
         |""".stripMargin

    spark.sql(sql_user_fans).createOrReplaceTempView("userFans")

    val sql_result =
      s"""
         |select a.baseDate,remain_uv,remain_post_uv,remain_post_count,
         |    b.level_c_uv,level_d_uv,level_d_start_uv,
         |    c.avg_add_fans
         |from userRetain a
         |left join userLevel b on a.baseDate=b.baseDate and a.period=b.period
         |left join userFans c on a.baseDate=c.baseDate and a.period=c.period
         |""".stripMargin

    spark.sql(sql_result)
      .withColumn("dt", lit(date))
      .withColumn("period", lit(daysAgo))
      .repartition(1)
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_creator_first_publish_active_remain_di")

  }

  def updateFirstPublishUserHotRetain(spark: SparkSession, date: String, daysAgo: Int): Unit = {
    val newDay = DateTime.parse(date).minusDays(daysAgo).toString("yyyy-MM-dd")
    val remainStartDay = DateTime.parse(newDay).plusDays(1).toString("yyyy-MM-dd")

    val sql_first_publish_user_and_post =
      s"""
         |select dt,blogid as userId,postid
         |from lofter.dwd_par_creator_first_publish_di
         |where dt='$newDay' and publishdate='$newDay'
         |""".stripMargin

    spark.sql(sql_first_publish_user_and_post).cache().createOrReplaceTempView("baseTable")

    val sql_enter_rec =
      s"""
         |select dt,avg(times/1000) avg_rec_audit_time,count(distinct blogid) enter_rec_uv
         |from
         |(
         |select a.dt,a.blogid,a.postid,reviewtime-lastPendingTime times
         |from
         |(
         |    select from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') dt,postid,recomstatus,reviewaccount,blogid,createtime lastPendingTime
         |    from lofter_db_dump.ods_db_recommend_review_post_nd
         |    where recomstatus=1
         |    group by blogid,postid,recomstatus,reviewaccount,createtime,reviewtime,lastPendingTime
         |) a
         |inner join
         |(
         |    select postid,createtime reviewtime
         |    from
         |    (select postid,createtime,row_number()over(partition by postid order by createtime) rk
         |    from lofter_db_dump.ods_db_recommend_post_trace_log_nd
         |    where  from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') between '$newDay' and  '$date'
         |    and content='更新文章【推荐状态】' and operator is not null and operator<>'N/A')
         |    where rk=1
         |) a1
         |on a.postid=a1.postid
         |inner join
         |baseTable b
         |on a.postid=b.postid
         |inner join
         |(
         |select reviewaccount
         |from lofter.zq_lofter_recommendwork_name_zeppelin
         |group by reviewaccount
         |) c
         |on a.reviewaccount=c.reviewaccount
         |group by a.dt,a.blogid,a.postid,times
         |)
         |group by dt
         |""".stripMargin

    val sql_post_exposure =
      s"""
         |select a.dt,count(distinct a.userId) as uv,
         |    avg(centralized_exposurepv) avg_centralized_exposure_pv,
         |    avg(decentralized_exposurepv) avg_decentralized_exposure_pv
         |from
         |baseTable a
         |left join
         |(
         |select postId,
         |      sum(centralized_expose_pv) centralized_exposurepv,
         |	    sum(non_centralized_expose_pv) decentralized_exposurepv
         |from lofter.dws_post_traffic_di
         |where dt between '$newDay' and  '$date'
         |group by postId
         |) b
         |on a.postid = b.postId
         |group by a.dt
         |""".stripMargin

    val sql_post_hot =
      s"""
         |select  a.dt,avg(realpv) avg_real_pv,avg(hotpv) avg_hot_pv,avg(commentpv) avg_comment_pv,avg(sharepv) avg_share_pv
         |from
         |baseTable a
         |left join
         |(
         |    select postid,sum(realbrowsepv+realplaypv) as realpv,sum(poshotpv-neghotpv) hotpv,
         |           sum(sharepv) sharepv,sum(poscommentpv-negcommentpv) commentpv
         |    from lofter.dws_post_base_stats_di
         |    where dt between '$newDay' and  '$date'
         |    group by postid
         |) b
         |on a.postid=b.postid
         |group by a.dt
         |""".stripMargin

    spark.sql(sql_enter_rec).createOrReplaceTempView("enterRec")
    spark.sql(sql_post_exposure).createOrReplaceTempView("postExposure")
    spark.sql(sql_post_hot).createOrReplaceTempView("postHot")

    val sql_merge =
      s"""
         |select '$newDay' as baseDate,avg_centralized_exposure_pv,avg_decentralized_exposure_pv,
         |    avg_real_pv,avg_hot_pv,avg_comment_pv,avg_share_pv,
         |    avg_rec_audit_time, enter_rec_uv
         |from postExposure a
         |left join postHot b on a.dt=b.dt
         |left join enterRec c on a.dt=c.dt
         |""".stripMargin

    spark.sql(sql_merge)
      .withColumn("dt", lit(date))
      .withColumn("period", lit(daysAgo))
      .repartition(1)
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_creator_first_publish_hot_remain_di")
  }

}
