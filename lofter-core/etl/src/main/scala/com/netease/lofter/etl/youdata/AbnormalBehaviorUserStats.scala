package com.netease.lofter.etl.youdata

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.{SaveMode, SparkSession}

object AbnormalBehaviorUserStats {

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
    val day7Ago = DateTime.parse(date).minusDays(6).toString("yyyy-MM-dd")

    // get the top 2000 pv tag(between one week ago) which not in black and gray list
   val sql_tag_top2000 =
     s"""
        |select c.tag
        |from
        |-- 获取标签曝光量的top2000
        |  (select b.tag,sum(a.pv) as pv
        |  from
        |     (select itemid ,count(1) as  pv
        |     from lofter.ods_mda_app_partition_di
        |     where dt between '$day7Ago' and '$date'  and  action in (0,100) and
        |     itemtype in ('TEXT','PHOTO','AUDIO','VIDEO','ARTICLE') group by itemid) a
        |  join
        |     (select id as  postid ,tag
        |     from lofter.dim_post
        |     lateral view explode(tags) myTable as tag ) b
        |  on a.itemid=b.postid
        |  group by b.tag
        |  order by pv desc limit 2000) c
        |
        |left join
        |-- 黑产和灰产的标签
        |   (select target from lofter.zq_lofter_yichanguser_blacktext_zeppelin
        |   union all
        |   select target from lofter.zq_lofter_yichanguser_graytext_zeppelin) d
        |on c.tag=d.target
        |where d.target is null
        |""".stripMargin

    // get the good blogId from different behavior based on different condition
    val sql_good_blog =
      s"""
         |select distinct d.blogid
         |from
         |(select distinct blogid ,'green' as type from lofter_db_dump.ods_db_authenticate_blog_nd
         |union all
         |select distinct blogid,'yellow' as type from   lofter_db_dump.ods_db_verify_blog_nd
         |union all
         |select distinct a.blogid,'post' as type from
         |(select blogid,count(distinct id) as num from lofter_db_dump.ods_db_post_nd where valid=0 and allowview=0  and type!=5 and citerootpostid=0  group by blogid ) a
         |where a.num>=100
         |
         |union all
         |select distinct a.blogid ,'follow' as type from
         |(select blogid ,count(distinct userid) as uv from lofter_db_dump.ods_db_user_following_nd group by blogid) a
         |where a.uv>=100
         |) d
         |""".stripMargin

    spark.sql(sql_tag_top2000).createOrReplaceTempView("goodTag")
    spark.sql(sql_good_blog).createOrReplaceTempView("goodBlog")

    // get the suspect target which not in good Tag
    val sql_suspect_target =
      s"""
         |select a.* from
         |(select * from  lofter.ods_log_monitor_crab_suspect_di where dt='$date') a
         |left join
         |goodTag b
         |on a.target =b.tag
         |where b.tag is null
         |""".stripMargin

    spark.sql(sql_suspect_target).cache().createOrReplaceTempView("suspectTarget")

    // get the suspect user in suspectTarget and  not in good Blog, classify to different type
    val sql_suspect_user =
      s"""
         |select aa.* from
         |(
         |    select dt,userid,
         |           case when uri like '%/dwr/call/plaincall/TagBean.search.dwr%' or  uri like '%/dwr/mobile/call/plaincall/MobileTagBean.search.dwr%'
         |                     or  uri like '%/oldapi/tagPosts.api%' or  uri like '%/tag/post.json%'  then '标签相详情页翻页'
         |               when uri like '%/blogindex.do%' or  uri like '%/mobile/blogindex.do%' or  uri like '%/v2.0/blogHomePage.api%'
         |                     or uri like '%/dwr/call/plaincall/ArchiveBean.getArchivePostByTime.dwr% ' then '个人主页翻页'
         |               when uri like '%/blog.json%' then '搜索博客翻页'
         |               else '搜索文章翻页' end as type,
         |    max(offset) as num   ----最大翻页值分布
         |
         |    from suspectTarget
         |    where  dt='$date' and userid rlike '^[1-9][0-9]+$$' and
         |     (uri like '%/dwr/call/plaincall/TagBean.search.dwr%' or  uri like '%/dwr/mobile/call/plaincall/MobileTagBean.search.dwr%'
         |      or  uri like '%/oldapi/tagPosts.api%' or  uri like '%/tag/post.json%' or  uri like '%/blogindex.do%'
         |      or  uri like '%/mobile/blogindex.do%' or  uri like '%/v2.0/blogHomePage.api%'  or  uri like '%/blog.json%'
         |      or  uri like '%/post.json%'
         |     )
         |    group by 1,2,3
         |
         |    union all
         |    select dt,userid,
         |           case when uri like '%/dwr/call/plaincall/TagBean.search.dwr%' or  uri like '%/dwr/mobile/call/plaincall/MobileTagBean.search.dwr%'
         |                     or  uri like '%/oldapi/tagPosts.api%' or  uri like '%/tag/post.json%'  then '标签相详情页次数'
         |                when uri like '%/blogindex.do%' or  uri like '%/mobile/blogindex.do%' or  uri like '%/v2.0/blogHomePage.api%'
         |                     or uri like '%/dwr/call/plaincall/ArchiveBean.getArchivePostByTime.dwr% ' then '个人主页次数'
         |                when uri like '%/blog.json%' then '搜索博客次数'
         |                else '搜索文章次数' end as type,
         |          count(distinct target) as num   ----目标触发词数分布
         |    from suspectTarget
         |    where  dt='$date' and userid rlike '^[1-9][0-9]+$$' and
         |     (uri like '%/dwr/call/plaincall/TagBean.search.dwr%' or  uri like '%/dwr/mobile/call/plaincall/MobileTagBean.search.dwr%'
         |      or  uri like '%/oldapi/tagPosts.api%' or  uri like '%/tag/post.json%' or  uri like '%/blogindex.do%'
         |      or  uri like '%/mobile/blogindex.do%' or  uri like '%/v2.0/blogHomePage.api%'  or  uri like '%/blog.json%'
         |      or  uri like '%/post.json%'
         |     )
         |    group by 1,2,3
         |
         |    union all
         |    select b.* from
         |    (select a.dt,a.userid,'文章浏览数' as type,sum(a.postnum) as num  ----文章浏览数分布
         |    from
         |      ( select userid,count(distinct urlpath) as postnum,dt
         |        from lofter.ods_mda_web_di
         |        where dt='$date' and eventid='da_screen' and urlpath like '%/post/%'
         |        and userid rlike '^[1-9][0-9]+$$'
         |        group by  dt,userid
         |
         |        union all
         |        select  userid,count(distinct params['tid']) as postnum,dt  from lofter.ods_mda_app_partition_di where dt='$date' and eventid='g1-8' and actionType = 'page_view'
         |        group by  dt,userid) a
         |    group by a.userid,a.dt) b
         |    where b.num>=1
         |
         |    union all
         |    select a.dt,a.userid,a.type,a.num    ---复制链接
         |    from
         |        (select  userid,  dt ,'复制链接' as type, count(distinct itemid) as num
         |        from lofter.ods_mda_app_partition_di
         |        where dt='$date' and eventid='z4-10' and  itemid is not null and actionType = 'other'
         |        group by  dt,userid) a
         |    where a.num>=1
         |
         |    union all
         |    select c.dt, c.userid, c.type, c.num    ---搜索按钮点击无结果
         |    from (
         |        select a.dt, a.userid, '搜索按钮点击无结果' as type, count(distinct a.queryname) as num
         |        from (
         |            select dt, userid, search_query as queryname
         |            from lofter.dwd_search_action_di
         |            where dt='$date' and eventId in ('b5-13','b5-12')
         |            group by 1, 2, 3
         |        ) a
         |        join (
         |            select dt, userid, search_query as queryname
         |            from lofter.dwd_search_action_di
         |            where dt='$date' and eventId in ('b5-20') and tab ='all'
         |            group by 1,2,3
         |        ) b on a.dt=b.dt and a.userid=b.userid and a.queryname=b.queryname
         |        group by a.userid,a.dt,'搜索按钮点击无结果'
         |    ) c
         |    where c.num>=1
         |) aa
         |
         |left join
         | goodBlog  bb ---剔除豁免的博客
         | on aa.userid=bb.blogid
         | where bb.blogid is null and aa.userid rlike '^[1-9][0-9]+$$'
         |""".stripMargin

    spark.sql(sql_suspect_user).cache().createOrReplaceTempView("suspectUser")

    val sql_user_score =
      s"""
         |select distinct cc.dt,cast(cc.userId as bigint) as userId, e.blogName, cc.allNum,d.type,d.num
         |from
         |
         |(select distinct bb.dt,bb.userid,bb.allnum
         |from
         |    (select aa.dt,aa.userid, round(sum(aa.num*aa.score)) as allnum
         |    from
         |        (
         |        select a.dt,a.type,a.userid,((a.num-b.minnum)/(b.maxnum-b.minnum) )  as num,
         |               case when a.type='标签相详情页翻页' then 20
         |                    when a.type='个人主页翻页' then 10
         |                    when a.type='搜索博客翻页' then 5
         |                    when a.type='搜索文章翻页' then 10
         |                    when a.type='标签相详情页次数' then 5
         |                    when a.type='个人主页次数' then 5
         |                    when a.type='搜索博客次数' then 5
         |                    when a.type='搜索文章次数' then 5
         |                    when a.type='文章浏览数' then 10
         |                    when a.type='复制链接' then 10
         |                    when a.type='搜索按钮点击无结果' then 15
         |                    else 0 end as score
         |        from
         |        suspectUser a
         |
         |        join
         |        (select dt,type,max(num) as maxnum,min(num) as minnum from suspectUser  group by dt,type ) b
         |        on a.dt=b.dt and a.type=b.type
         |        ) aa
         |    group by aa.dt,aa.userid) bb
         |) cc
         |
         |join
         |
         |(select dt,userid,type, num from suspectUser ) d
         |on cc.dt=d.dt and cc.userid=d.userid
         |join
         |(select blogid,blogname from lofter_db_dump.ods_db_blog_info_nd) e
         |on cc.userid=e.blogid
         |""".stripMargin

    val sql_search_no_result =
      s"""
         |select a.dt,a.userid,count(distinct a.target) as num
         |from (
         |       select distinct dt,userid, target from  suspectTarget
         |
         |       union
         |
         |       select a.dt,a.userid, a.queryname as target
         |       from ( -- a表  某个用户主动搜索的内容 或者点击sug页联想词
         |           select distinct userid,dt, search_query as queryname, ip
         |           from lofter.dwd_search_action_di
         |           where dt='$date' and eventId in ('b5-13','b5-12')
         |       ) a
         |       join ( -- b表， 搜索列表无结果上报
         |           select userid,dt, search_query as queryname, ip
         |           from lofter.dwd_search_action_di
         |           where dt='$date' and eventId in ('b5-20') and tab ='all'
         |           group by 1, 2, 3, 4
         |       ) b on a.dt=b.dt and a.userid=b.userid and a.queryname=b.queryname and a.ip=b.ip
         |) a
         | cross join (
         |     select target from lofter.zq_lofter_yichanguser_blacktext_zeppelin
         | ) b
         |  where locate(a.target,b.target)>0 or locate(b.target,a.target)>0
         |group by a.dt,a.userid
         |""".stripMargin

    spark.sql(sql_user_score).cache().createOrReplaceTempView("userScore")
    spark.sql(sql_search_no_result).cache().createOrReplaceTempView("searchNoResult")

    val sql_suspect_high_score =
      s"""
         |select * from userScore where allNum > 10
         |""".stripMargin

    val sql_suspect_low_score =
      s"""
         |select distinct a.dt,a.userid,a.blogname,a.allnum,a.type,a.num
         |from
         |  (select * from userScore where allNum between 0 and 5) a
         |  join
         |  (select distinct dt,userid from searchNoResult where num>=3 ) b
         |on a.dt=b.dt and a.userid=b.userid
         |""".stripMargin

    val sql_suspect_medium_score =
      s"""
         |select distinct a.dt,a.userid,a.blogname,a.allnum,a.type,a.num
         |from
         |  (select * from userScore where allNum between 6 and 10) a
         |  join
         |  (select distinct dt,userid from searchNoResult where num>=1 ) b
         |on a.dt=b.dt and a.userid=b.userid
         |""".stripMargin

    spark.sql(sql_suspect_high_score).createOrReplaceTempView("highScore")
    spark.sql(sql_suspect_low_score).createOrReplaceTempView("lowScore")
    spark.sql(sql_suspect_medium_score).createOrReplaceTempView("mediumScore")

    val sql_suspect_total =
      s"""
         |select * from highScore
         |union all
         |select * from lowScore
         |union all
         |select * from mediumScore
         |""".stripMargin

    spark.sql(sql_suspect_total).cache().createOrReplaceTempView("suspectTotal")

    val sql_suspect_content =
      s"""
         |select distinct a.dt,a.userId,b.blogName,b.allNum,a.target,a.type,a.num
         |from (
         |    select userid,dt,
         |        case when uri like '%/dwr/call/plaincall/TagBean.search.dwr%' or  uri like '%/dwr/mobile/call/plaincall/MobileTagBean.search.dwr%'
         |                  or  uri like '%/oldapi/tagPosts.api%' or  uri like '%/tag/post.json%'  then '标签相详情页翻页'
         |             when uri like '%/blogindex.do%' or  uri like '%/mobile/blogindex.do%' or  uri like '%/v2.0/blogHomePage.api%'
         |                  or uri like '%/dwr/call/plaincall/ArchiveBean.getArchivePostByTime.dwr% ' then '个人主页翻页'
         |             when uri like '%/blog.json%' then '搜索博客翻页'
         |             else '搜索文章翻页' end as type,
         |             target,max(offset) as num
         |
         |    from suspectTarget
         |    where (uri like '%/dwr/call/plaincall/TagBean.search.dwr%' or  uri like '%/dwr/mobile/call/plaincall/MobileTagBean.search.dwr%'
         |         or uri like '%/oldapi/tagPosts.api%' or  uri like '%/tag/post.json%' or  uri like '%/blogindex.do%'
         |         or uri like '%/mobile/blogindex.do%' or  uri like '%/v2.0/blogHomePage.api%'  or  uri like '%/blog.json%'
         |         or uri like '%/post.json%'
         | )
         |and userid rlike '^[1-9][0-9]+$$'
         |
         |group by 1,2,3,4
         |) a
         |
         |join
         |(select dt,userid,type,num,blogname,allnum from suspectTotal ) b
         |on a.userid=b.userid and a.dt=b.dt and a.type=b.type
         |""".stripMargin

    val sql_suspect_article =
      s"""
         |select distinct a.dt,a.userId,b.blogName,b.allNum,a.post as target,b.type,b.num
         |from
         |      (select  distinct dt,userid,conv(regexp_extract(urlpath,'/post/(\\w+)_(\\w+)',2),16,10) as post
         |      from lofter.ods_mda_web_di
         |      where dt='$date' and eventid='da_screen' and urlpath like '%/post/%'
         |             and userid rlike '^[1-9][0-9]+$$'
         |
         |      union all
         |      select  distinct dt,userid, params['tid'] as post  from lofter.ods_mda_app_partition_di where dt='$date' and eventid='g1-8' and actionType = 'page_view'
         |      ) a
         |join
         |
         |(select dt,userid,type,num,blogname,allnum from suspectTotal where type='文章浏览数') b
         |on a.userid=b.userid and a.dt=b.dt
         |""".stripMargin

    val sql_suspect_copy_link =
      s"""
         |select distinct a.dt,a.userId,b.blogName,b.allNum,a.post as target,b.type,b.num
         |from
         |    (select  dt, userid, itemid as post
         |     from lofter.ods_mda_app_partition_di where dt='$date' and actionType = 'other'
         |     and eventid='z4-10' and  itemid is not null
         |     group by  dt, userid, itemid
         |     ) a
         |   join
         |
         |    (select dt,userid,type,num,blogname,allnum from suspectTotal where type='复制链接') b
         |    on a.userid=b.userid and a.dt=b.dt
         |""".stripMargin

    val sql_suspect_search_no_result =
      s"""
         |select distinct a.dt,a.userId,b.blogName,b.allNum,a.queryName as target,b.type,b.num
         |from (
         |    select distinct a.userid,a.dt, a.queryname
         |    from (
         |        select dt, userid, search_query as queryname
         |        from lofter.dwd_search_action_di
         |        where dt = '$date' and eventId in ('b5-13','b5-12')
         |        group by 1,2,3
         |    ) a
         |    join (
         |        select dt, userid, search_query as queryname
         |        from lofter.dwd_search_action_di
         |        where dt='$date' and eventId in ('b5-20') and tab = 'all'
         |        group by 1,2,3
         |    ) b on a.dt=b.dt and a.userid=b.userid and a.queryname=b.queryname
         |) a
         |join (
         |    select dt,userid,type,num,blogname,allnum from suspectTotal where type='搜索按钮点击无结果'
         |) b on a.userid=b.userid and a.dt=b.dt
         |""".stripMargin

    spark.sql(sql_suspect_content).createOrReplaceTempView("suspectContent")
    spark.sql(sql_suspect_article).createOrReplaceTempView("suspectArticle")
    spark.sql(sql_suspect_copy_link).createOrReplaceTempView("suspectCopyLink")
    spark.sql(sql_suspect_search_no_result).createOrReplaceTempView("suspectSearchNoResult")

    val sql_suspect_summary =
      s"""
         |select * from suspectContent
         |union all
         |select * from suspectArticle
         |union all
         |select * from suspectCopyLink
         |union all
         |select * from suspectSearchNoResult
         |""".stripMargin

    val sql_suspect_ip =
      s"""
         |select distinct a.dt,a.userId,b.blogName,a.ip,a.country,a.province,a.city,b.allNum,b.type,b.num
         |from
         |(select distinct a.userid,a.dt,a.ip,a.country, a.province, a.city
         |from
         |      (select a.userid,a.dt,a.ip ,inline(Array(resolve_ip(a.ip))) as (country, province, city)
         |       from
         |          (select distinct userid,dt,userip as ip from suspectTarget
         |           union all
         |           select distinct userid,dt ,ip  from lofter.ods_mda_app_partition_di where dt='$date'
         |           ) a
         |      ) a
         |) a
         |
         |join
         |(select dt,userid,type,num,blogname,allnum from suspectTotal) b
         |on a.userid=b.userid and a.dt=b.dt
         |""".stripMargin

    spark.sql(sql_suspect_total)
      .repartition(1)
      .select("userId","blogName","allNum","type","num","dt")
      .write
      .mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_abnormal_behavior_user_overall_stats_di")

    spark.sql(sql_suspect_summary)
      .repartition(1)
      .select("userId","blogName","allNum","target","type","num","dt")
      .write
      .mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_abnormal_behavior_user_detail_stats_di")

    spark.sql(sql_suspect_ip)
      .repartition(1)
      .select("userId","blogName","ip","country","province","city","allNum","type","num","dt")
      .write
      .mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_abnormal_behavior_user_ip_stats_di")

    spark.close()

  }

}
