package com.netease.lofter.etl.youdata

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

object OperationDarenVideoStats {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Operate Daren Video Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))
    val oneMonthAgo = DateTime.parse(date).minusDays(29).toString("yyyy-MM-dd")
    val yearStartFrom2022 = "2022-01-01"

    val sql_daren =
      s"""
         |select a1.blogId ,a2.blogName,a2.blogNickName,a3.level
         |from
         |(select userid as blogId
         |from lofter_db_dump.ods_db_cmb_business_introduction_nd
         |where status=0 and level is not null
         |group by userid) a1
         |join  lofter.dim_blog  a2
         |on a1.blogId = a2.id
         |left join
         |(select userId as blogId, level from lofter.dws_par_creator_dd where dt='$date') a3
         |on a1.blogId=a3.blogId
         |""".stripMargin

    val sql_auth_daren =
      s"""
         |select blogid   from lofter_db_dump.ods_db_authenticate_blog_nd group by blogid
         |union
         |select blogid from   lofter_db_dump.ods_db_verify_blog_nd group by blogid
         |""".stripMargin

    spark.sql(sql_daren).cache().createOrReplaceTempView("darenBlogInfo")
    spark.sql(sql_auth_daren).cache().createOrReplaceTempView("authDaren")

    val sql_daren_play_result =
      s"""
         |select a.blogId,a.blogName,a.blogNickName,a.level,
         |       playPostNum,playPv,playUv,realPlayPv,realPlayUv,finishPlayPv,finishPlayUv,
         |       c.fans30day,fans,hotPv,hdPv,
         |       f.lastlogindate as last_login_date,
         |       datediff('$date',lastlogindate) as no_active_days,
         |       d.post_num_today,post_num_from2022,last_post_date,
         |       datediff('$date',last_post_date) as no_post_days,
         |       case when e.blogid is not null then '达人' else '非达人' end  as auth_flag,
         |       a.dt
         |from (
         |    select a.dt,b.blogId,b.blogName,b.blogNickName,b.level,
         |            count(distinct a.postId) as playPostNum,
         |            sum(a.playPv) as playPv,sum(a.playUv) as playUv,sum(a.realPlayPv) as realPlayPv,sum(a.realPlayUv) as realPlayUv,
         |            sum(a.finishPlayPv) as finishPlayPv,sum(a.finishPlayUv) as finishPlayUv,
         |            sum(a.posHotPv) as hotPv, sum(a.hdPv) as hdPv
         |    from (
         |        select dt,postId,userId as blogId,
         |               browsePv as playPv, browseUv as playUv,
         |               realBrowsePv as realPlayPv,
         |               realBrowseUv as realPlayUv,
         |               finishPlayPv, finishPlayUv,
         |               posHotPv,hdPv
         |        from lofter.dws_post_base_stats_di
         |        where dt ='$date' and contentType='视频' and browsePv > 0
         |    ) a
         |    join darenBlogInfo b on a.blogId=b.blogId
         |    group by 1,2,3,4,5
         |) a
         |left join (
         |    select blogId,
         |           count(distinct if(from_unixtime(cast(followTime / 1000 AS BIGINT), 'yyyy-MM-dd') between '$oneMonthAgo' and '$date',
         |                 userId, null)) as fans30day,
         |          count(distinct userId) as fans
         |    from lofter_db_dump.ods_db_user_following_nd
         |    group by blogId
         |) c on a.blogId=c.blogId
         |
         |left  join (
         |  select accountId as userid,
         |         from_unixtime(cast(time/1000 as bigint),'yyyy-MM-dd') as lastLoginDate
         |  from lofter.dws_evt_login_user_last_dd
         |  where dt = '$date'
         |) f on a.blogid=f.userid
         |
         |left join (
         |   select blogid,
         |          count(case when publishdate ='$date' then id else null end) as post_num_today,
         |          count(case when publishdate between '$yearStartFrom2022' and '$date' then id else null end) as post_num_from2022,
         |          max(case when publishdate>='$date' then '$date' else publishdate end) as last_post_date
         |   from lofter.dim_post
         |   where contentType!='问答' and isPublished=true and valid=0 and allowview=0 and isCitedPost=false
         |   group by blogid
         |) d on a.blogid=d.blogid
         |
         |left join authDaren e on a.blogid=e.blogid
         |""".stripMargin

    val sql_platform_result =
      s"""
         |select a.platformType, case when  b.blogId is null then 0 else 1 end as operateDarenFlag,
         |        case when c.blogid is not null then '达人' else '非达人' end  as auth_flag,
         |        count(distinct case when dt='$date' then a.postId else null end) as playPostNum,
         |        sum(case when dt='$date' then a.playPv else 0 end) as playPv,
         |        sum(case when dt='$date' then a.playUv else 0 end) as playUv,
         |        sum(case when dt='$date' then a.realPlayPv else 0 end) as realPlayPv,
         |        sum(case when dt='$date' then a.realPlayUv else 0 end) as realPlayUv,
         |        sum(case when dt='$date' then a.finishPlayPv else 0 end) as finishPlayPv,
         |        sum(case when dt='$date' then a.finishPlayUv else 0 end) as finishPlayUv,
         |        count(distinct case when publish_date between '$oneMonthAgo' and '$date' then a.postId else null end) as playPostNum30days,
         |        sum(case when publish_date between '$oneMonthAgo' and '$date' then a.playPv else 0 end) as playPv30days,
         |        sum(case when publish_date between '$oneMonthAgo' and '$date' then a.playUv else 0 end) as playUv30days,
         |        sum(case when publish_date between '$oneMonthAgo' and '$date' then a.realPlayPv else 0 end) as realPlayPv30days,
         |        sum(case when publish_date between '$oneMonthAgo' and '$date' then a.realPlayUv else 0 end) as realPlayUv30days,
         |        sum(case when publish_date between '$oneMonthAgo' and '$date' then a.finishPlayPv else 0 end) as finishPlayPv30days,
         |        sum(case when publish_date between '$oneMonthAgo' and '$date' then a.finishPlayUv else 0 end) as finishPlayUv30days,
         |        '$date' as dt
         |from (
         |    select dt,postId,userId as blogId,
         |          browsePv as playPv, browseUv as playUv, realBrowsePv as realPlayPv,
         |          realBrowseUv as realPlayUv, finishPlayPv, finishPlayUv,
         |          platformType, substr(publishDate,0,10) as publish_date
         |    from lofter.dws_post_base_stats_di
         |    where contentType='视频' and browsePv > 0 and
         |          dt between '$oneMonthAgo' and '$date'
         |) a
         |
         |left join darenBlogInfo b on a.blogId=b.blogId
         |
         |left join authDaren c on a.blogid=c.blogid
         |group by a.platformType, operateDarenFlag, auth_flag
         |""".stripMargin

    val sql_video_source_post =
      s"""
         |select case when a.importPlatformType is not null then a.importPlatformType else '站内' end as platform_type,
         |       case when  d.blogid is null then 0 else 1 end as operate_daren_flag,
         |       case when c.blogid is not null then '达人' else '非达人' end  as auth_flag,
         |       count(distinct case when a.publishdate='$date' then a.id else null end ) as post_num_today,
         |       count(distinct a.id) as post_num_30days,
         |       count(distinct case when a.publishdate='$date' then a.blogid else null end ) as post_uv_today,
         |       count(distinct a.blogid) as post_uv_30days,
         |       '$date' as dt
         |from (
         |   select id, blogid, publishDate, importPlatformType
         |   from lofter.dim_post
         |   where contentType='视频' and isPublished=true and valid=0 and allowview in(0,50) and
         |         isCitedPost = false and
         |         publishDate between '$oneMonthAgo' and '$date'
         |) a
         |left join authDaren c on a.blogid=c.blogid
         |left join darenBlogInfo d on a.blogid=d.blogid
         |group by dt,platform_type,operate_daren_flag,auth_flag
         |grouping sets( (dt,platform_type,operate_daren_flag,auth_flag),
         |               (dt,platform_type,operate_daren_flag),
         |               (dt,platform_type),(dt) )
         |""".stripMargin

    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")

    spark.sql(sql_daren_play_result)
      .write
      .mode("overwrite")
      .insertInto("lofter_dm.ads_operate_daren_video_detail_stat_di")

    spark.sql(sql_platform_result)
      .write
      .mode("overwrite")
      .insertInto("lofter_dm.ads_video_source_stat_di")

    spark.sql(sql_video_source_post)
      .write
      .mode("overwrite")
      .insertInto("lofter_dm.ads_video_source_post_stat_di")

    spark.close()
  }

}
