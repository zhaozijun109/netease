package com.netease.lofter.etl.youdata

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.{SaveMode, SparkSession}

object AbnormalBehaviorUserDetailStats {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Abnormal behavior user Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")
    spark.sql("create temporary function resolve_ip as 'com.netease.wm.udf.ResolveIp'")
    val date = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))

    val sql_low_score_stat =
      s"""
         |select '$date' as dt,a.*,b.queryName
         |from
         |(select distinct userId,blogName,allNum
         | from lofter_dm.ads_abnormal_behavior_user_overall_stats_di
         | where dt between date_sub('$date',30) and '$date' and allNum between 0 and 5 and num>=3) a
         |join (
         |    select distinct userId, search_query as queryName
         |    from lofter.dwd_search_action_di
         |    where dt='$date' and eventId in ('b5-13','b5-12')
         |  union
         |    select distinct userId, search_query as queryName
         |    from lofter.dwd_search_action_di
         |    where  dt='$date'  and eventId in ('b5-10','b5-20')
         |) b on a.userId=b.userId
         |""".stripMargin

    val sql_warning_user =
      s"""
         |select blogId,blogName
         | from lofter_db_dump.ods_db_blog_info_nd
         | where blogId in (select blogId from lofter.zq_lofter_warning_blogid_zeppelin)
         |""".stripMargin

    spark.sql(sql_warning_user).cache().createOrReplaceTempView("warningUsers")

    val sql_warning_user_search =
      s"""
         |select '$date' as dt,'搜索词' as type,b.userId,b.queryName as target,a.blogName
         |from warningUsers a
         |join (
         |    select distinct userId, search_query as queryName
         |    from lofter.dwd_search_action_di
         |    where dt='$date' and eventId in ('b5-13','b5-12')
         |  union
         |    select distinct userId, search_query as queryName
         |    from lofter.dwd_search_action_di
         |    where dt='$date' and eventId in ('b5-10','b5-20')
         |) b on a.blogId=b.userId
         |""".stripMargin

    val sql_warning_user_offset =
      s"""
         |select '$date' as dt,b.type,b.userId,b.target,a.blogName
         |from
         |warningUsers a
         |join
         |(select userid,
         |        case when uri like '%/dwr/call/plaincall/TagBean.search.dwr%' or  uri like '%/dwr/mobile/call/plaincall/MobileTagBean.search.dwr%'
         |                  or  uri like '%/oldapi/tagPosts.api%' or  uri like '%/tag/post.json%'  then '标签相详情页翻页'
         |             when uri like '%/blogindex.do%' or  uri like '%/mobile/blogindex.do%' or  uri like '%/v2.0/blogHomePage.api%'
         |                  or uri like '%/dwr/call/plaincall/ArchiveBean.getArchivePostByTime.dwr% ' then '个人主页翻页'
         |             when uri like '%/blog.json%' then '搜索博客翻页'
         |             else '搜索文章翻页' end as type,
         |             target,max(offset) as num
         |
         | from lofter.ods_log_monitor_crab_suspect_di where dt='$date' and
         | (uri like '%/dwr/call/plaincall/TagBean.search.dwr%' or  uri like '%/dwr/mobile/call/plaincall/MobileTagBean.search.dwr%'
         |  or  uri like '%/oldapi/tagPosts.api%' or  uri like '%/tag/post.json%' or  uri like '%/blogindex.do%'
         |  or  uri like '%/mobile/blogindex.do%' or  uri like '%/v2.0/blogHomePage.api%'  or  uri like '%/blog.json%'
         |  or  uri like '%/post.json%'
         | )
         |and userid rlike '^[1-9][0-9]+$$'
         |
         |group by 1,2,3) b
         |on a.blogId=b.userid
         |""".stripMargin

    val sql_warning_user_article =
      s"""
         |select '$date' as dt,'文章浏览' as type, b.userId, b.post as target, a.blogName
         |from
         |warningUsers a
         |join
         |      (select  distinct userid,conv(regexp_extract(urlpath,'/post/(\\w+)_(\\w+)',2),16,10) as post
         |      from lofter.ods_mda_web_di
         |      where dt='$date' and eventid='da_screen' and urlpath like '%/post/%'
         |             and userid rlike '^[1-9][0-9]+$$'
         |
         |      union all
         |      select  distinct userid, params['tid'] as post  from lofter.ods_mda_app_partition_di
         |      where dt='$date' and eventid='g1-8' and actionType = 'page_view'
         |      ) b
         |on  a.blogId=b.userid
         |""".stripMargin

    val sql_warning_user_copy_link =
      s"""
         |select '$date' as dt,'复制链接' as type, b.userId, b.post as target,a.blogName
         |from
         |warningUsers a
         |join
         |    (select userid, itemid as post
         |     from lofter.ods_mda_app_partition_di where dt='$date' and actionType = 'other'
         |     and eventid='z4-10' and  itemid is not null
         |     group by  dt, userid, itemid
         |     ) b
         |on  a.blogId=b.userid
         |""".stripMargin

    val sql_warning_user_ip =
      s"""
         |select '$date' as dt,b.*,a.blogName
         |from
         |warningUsers a
         |join
         |(select distinct a.userId,a.ip,a.country, a.province, a.city
         |from
         |      (select a.userid,a.ip ,inline(Array(resolve_ip(a.ip))) as (country, province, city)
         |       from
         |          (select distinct userid,userip as ip from lofter.ods_log_monitor_crab_suspect_di where dt='$date'
         |           union all
         |           select distinct userid,ip  from lofter.ods_mda_app_partition_di where dt='$date'
         |           ) a
         |      ) a
         |) b
         |on a.blogId=b.userId
         |""".stripMargin

    val df_summary = spark.sql(sql_warning_user_search)
        .union(spark.sql(sql_warning_user_offset))
        .union(spark.sql(sql_warning_user_article))
        .union(spark.sql(sql_warning_user_copy_link))

    spark.sql(sql_low_score_stat)
      .repartition(1)
      .select("userId","blogName","allNum","queryName","dt")
      .write
      .mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_abnormal_low_score_user_query_stats_di")

    df_summary
      .repartition(1)
      .select("userId","blogName","target","type","dt")
      .write
      .mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_dangerous_user_monitor_stats_di")

    spark.sql(sql_warning_user_ip)
      .repartition(1)
      .select("userId","blogName","ip","country","province","city","dt")
      .write
      .mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_dangerous_user_ip_monitor_stats_di")

    spark.close()

  }

}
