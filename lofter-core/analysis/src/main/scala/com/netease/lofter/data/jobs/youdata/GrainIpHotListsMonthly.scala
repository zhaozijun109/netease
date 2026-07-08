package com.netease.lofter.data.jobs.youdata

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.lit

object GrainIpHotListsMonthly {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Hot List Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))
    val last1Day = DateTime.parse(date).minusDays(1).toString("yyyy-MM-dd")
    val last2Day = DateTime.parse(date).minusDays(2).toString("yyyy-MM-dd")
    val last3Day = DateTime.parse(date).minusDays(3).toString("yyyy-MM-dd")
    val oneWeekAgo = DateTime.parse(date).minusDays(6).toString("yyyy-MM-dd")
    val lastWeekEnd = DateTime.parse(date).minusDays(7).toString("yyyy-MM-dd")
    val lastWeekStart = DateTime.parse(date).minusDays(14).toString("yyyy-MM-dd")
    val oneMonthAgo = DateTime.parse(date).minusDays(29).toString("yyyy-MM-dd")

    val sql_tag_ip =
      s"""
         |select b.ipname as ipName, c.id as id, c.blogid as blogId,
         |       c.publishDate as publishDate,
         |       c.contentType as contentType, c.blogName as blogName
         |from (
         |    select name as tagname, ipid
         |    from lofter_db_dump.ods_db_cmb_tag_nd
         |    LATERAL VIEW explode(split(ipids, ';')) t2  as ipid
         |    where status=0
         |) a
         |join (
         |    select id,name as ipname
         |    from lofter_db_dump.ods_db_cmb_ip_nd
         |) b on a.ipid=b.id
         |join (
         |    select id, publishdate,blogid,blogname,tag,contenttype
         |    from lofter.dim_post
         |    lateral view explode(tags) myTable as tag
         |    where	contenttype!='问答' and contenttype!='视频' and ispublished=true and
         |          valid=0 and allowview=0 and iscitedpost=false
         |) c on a.tagname=c.tag
         |left join (
         |    SELECT tagName FROM lofter_db_dump.ods_db_recommend_tag_new_nd WHERE blackTag > 0
         |) d on a.tagname=d.tagName
         |where d.tagName is null
         |group by b.ipname, c.id, c.blogid, c.publishdate, c.contenttype, c.blogname
         |
         |union
         |
         |select a.*
         |from (
         |    select level3 as ipName, postId as id, blogId, publish_date as publishDate,
         |           content_type as contentType, blog_name as blogName
         |    from lofter_dm.ads_creator_double_perspective_post_di
         |         lateral view explode(split(double_perspective_type,'\\\\|')) t as level3
         |    where dt = "$last1Day"
         |) a
         |join (
         |   select level3 from lofter_dm.ads_specialty_categories_level group by level3
         |) b on a.ipName = b.level3
         |
         |""".stripMargin

    val sql_hot_ip =
      s"""
         |select a.ipname as ipName
         |from (
         |    select a.ipname,row_number() over(order by a.num desc) as rk
         |    from (
         |        select ipname, count(distinct id) as num
         |        from  tagIp
         |        where publishdate between '$oneMonthAgo' and '$date'
         |        group by ipname
         |    ) a
         |) a
         |where a.rk<=100
         |
         |union
         |
         |select a.ipname as ipName
         |from (
         |    select a.ipname,row_number() over(order by a.rate desc ,a.num1 desc) as rk
         |    from (
         |        select a.ipname,a.num1, case when a.num2=0 then 999999.99 else num1/num2  end as rate
         |        from (
         |            select a.ipname,a.num as num1,case when b.num is null then 0 else b.num end as num2
         |            from (
         |                select ipname, count(distinct id) as num
         |                from tagIp
         |                where publishdate between  '$oneWeekAgo' and '$date'
         |                group by ipname
         |            ) a
         |            left join (
         |               select ipname,count(distinct id) as num
         |               from  tagIp
         |               where publishdate between  '$lastWeekStart' and '$lastWeekEnd'
         |               group by ipname
         |            ) b on a.ipname=b.ipname
         |            where a.num>=100
         |        ) a
         |    ) a
         |) a
         |where a.rk<=30
         |
         |union
         |
         |select a.*
         |from (
         |    select level3 as ipName
         |    from lofter_dm.ads_creator_double_perspective_post_di
         |         lateral view explode(split(double_perspective_type,'\\\\|')) t as level3
         |    where dt = "$last1Day"
         |    group by level3
         |) a
         |join (
         |   select level3 from lofter_dm.ads_specialty_categories_level group by level3
         |) b on a.ipName = b.level3
         |where a.ipName not in ('嗑cp运营补充', '嗑cp精品池')
         |""".stripMargin

    val sql_hot_post =
      s"""
         |select a.*
         |from (
         |    select distinct ipname,id,blogid,publishdate,blogname from tagIp
         |) a
         |join (
         |    select postid
         |    from lofter_db_dump.ods_db_recommend_review_post_nd
         |    where recomstatus=1 group by postid
         |) b on a.id=b.postid
         |""".stripMargin
    
    spark.sql(sql_tag_ip).createOrReplaceTempView("tagIp")
    spark.sql(sql_hot_ip).createOrReplaceTempView("hotIps")
    spark.sql(sql_hot_post).createOrReplaceTempView("hotPosts")

    val sql_hot_comment_list =
      s"""
         |select 1 as listSource,4 as listType,a.ipname as listName,
         |    a.id as postId,a.blogId,a.publishDate,a.blogName,a.pv as indexValue,a.rk1 as listRank
         |from (
         |    select a.ipname,b.id,b.blogid,b.publishdate,b.blogname,c.pv,
         |         row_number() over (partition by  a.ipname order by c.pv desc, b.id) as rk1
         |    from hotIps a
         |    join hotPosts b on a.ipname=b.ipname
         |    join (
         |        select postid, sum(response_pv) as pv
         |        from lofter.dws_post_interaction_di
         |        where dt between '$oneMonthAgo' and '$date'
         |        group by postid
         |        having pv >= 10
         |    ) c on b.id=c.postid
         |) a
         |where rk1 <= 100
         |""".stripMargin

    val sql_hot_share_list =
      s"""
         |select 1 as listSource, 6 as listType,a.ipname as listName,
         |    a.id as postId,a.blogId,a.publishDate,a.blogName,a.pv as indexValue,a.rk1 as listRank
         |from (
         |    select a.ipname,b.id,b.blogid,b.publishdate,b.blogname,c.pv,
         |           row_number() over (partition by  a.ipname order by c.pv desc, b.id) as rk1
         |    from hotIps a
         |    join hotPosts b on a.ipname=b.ipname
         |    join (
         |        select postid,sum(share_pv) as pv
         |        from lofter.dws_post_interaction_di
         |        where dt between '$oneMonthAgo' and '$date'
         |        group by postid
         |        having pv >= 10
         |    ) c on b.id=c.postid
         |) a
         |where rk1<=100
         |""".stripMargin

    val sql_hot_hot_list =
      s"""
         |select 1 as listSource, 5 as listType,a.ipname as listName,
         |    a.id as postId,a.blogId,a.publishDate,a.blogName,a.pv as indexValue,a.rk1 as listRank
         |from (
         |    select a.ipname,b.id,b.blogid,b.publishdate,b.blogname,c.pv,
         |           row_number() over (partition by  a.ipname order by c.pv desc, b.id) as rk1
         |    from hotIps a
         |    join hotPosts b on a.ipname=b.ipname
         |    join (
         |        select postid,sum(hot_pv) as pv
         |        from lofter.dws_post_interaction_di
         |        where dt between '$oneMonthAgo' and '$date'
         |        group by postid
         |        having pv >= 10
         |    ) c on b.id=c.postid
         |) a
         |where rk1 <= 100
         |""".stripMargin

    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")

    spark.sql(sql_hot_comment_list).createOrReplaceTempView("t4")
    spark.sql(sql_hot_share_list).createOrReplaceTempView("t5")
    spark.sql(sql_hot_hot_list).createOrReplaceTempView("t6")

    val sql_result =
      s"""
         |select *, md5(concat(listType,listName, 'month')) as distinctId,
         |    md5(concat(listType,listName,listSource,'$date')) as uniqId
         |from t4
         |union all
         |select *, md5(concat(listType,listName, 'month')) as distinctId,
         |      md5(concat(listType,listName,listSource,'$date')) as uniqId
         |from t5
         |union all
         |select *, md5(concat(listType,listName, 'month')) as distinctId,
         |      md5(concat(listType,listName,listSource,'$date')) as uniqId
         |from t6
         |""".stripMargin

    spark.sql(sql_result)
      .select("listType","listName","postId","blogId","publishDate","blogName","indexValue","listRank","distinctId","uniqId")
      .withColumn("dt", lit(date))
      .withColumn("listSource", lit(1))
      .write
      .mode("overwrite")
      .insertInto("lofter_dm.ads_grain_hot_list_monthly_di")

    spark.close()
  }

}
