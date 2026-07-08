package com.netease.lofter.etl.youdata

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.lit

object GrainCategoryHotListsStats {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Grain Category Hot List Stats")
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

    val sql_hot_category =
      s"""
         |select distinct postid,blogid,category as hotCategory
         |from lofter.dim_post_category_dd
         |where dt='$date' and
         |   category in ('设计','绘画','娱乐','冷知识','游戏','校园学习','时尚','美食','萌宠','萌宠','生活')
         |
         |union
         |select distinct postid,blogid,category2 as hotCategory
         |from lofter.dim_post_category_dd
         |where dt='$date' and
         |   category2 in ('手作','日本动漫','国产动漫','欧美动漫','虚拟偶像','手办模玩','cosplay','电影','电视剧','广播剧')
         |""".stripMargin

    val sql_hot_post =
      s"""
         |select a.*,publishdate,blogname
         |from (
         |    select distinct postid,blogid,hotCategory from hotCategories
         |) a
         |join (
         |    select id, publishdate,blogid,blogname
         |    from lofter.dim_post
         |    where	contenttype!='问答' and contenttype!='视频'   and ispublished=true and
         |          valid=0 and allowview=0 and iscitedpost=false
         |    group by id, publishdate,blogid,blogname
         |) b on a.postid=b.id
         |join (
         |    select postid
         |    from lofter_db_dump.ods_db_recommend_review_post_nd
         |    where recomstatus=1
         |    group by postid
         |) c on a.postid=c.postid
         |""".stripMargin
    
    spark.sql(sql_hot_category).createOrReplaceTempView("hotCategories")
    spark.sql(sql_hot_post).createOrReplaceTempView("hotPosts")

    val sql_hot_watch_list =
      s"""
         |select 3 as listSource,1 as listType, a.hotCategory as listName,
         |    a.postId,a.blogId,a.publishDate,a.blogName,a.pv as indexValue,a.rk1 as listRank
         |from (
         |    select b.hotCategory,b.postId,b.blogid,b.publishdate,b.blogname,c.pv,
         |           row_number() over(partition by  b.hotCategory order by c.pv desc) as rk1
         |    from hotPosts b
         |    join (
         |        select postid, count(1) as pv
         |        from lofter.dwd_post_browse_di
         |        where dt between '$oneWeekAgo' and '$date' and
         |              post_content_type != '视频' and is_real > 0
         |        group by postid
         |    ) c on b.postid=c.postid
         |) a
         |where rk1 <= 100
         |""".stripMargin

    val sql_hot_content_list =
      s"""
         |select 3 as listSource,2 as listType,a.hotCategory as listName,
         |    a.postId,a.blogId,a.publishDate,a.blogName,a.pv as indexValue,a.rk1 as listRank
         |from (
         |    select b.hotCategory,b.postId,b.blogid,b.publishdate,b.blogname,c.pv,
         |           row_number() over(partition by  b.hotCategory order by c.pv desc) as rk1
         |    from hotPosts b
         |    join (
         |        select postid,(favoritecount+reblogcount+sharecount+subscribecount) as pv
         |        from lofter_db_dump.ods_db_post_count_nd
         |    ) c on b.postid=c.postid
         |) a
         |where rk1 <= 100
         |""".stripMargin

    val sql_hot_growth_list =
      s"""
         |select 3 as listSource,3 as listType,a.hotCategory as listName,
         |    postId,a.blogId,a.publishDate,a.blogName,a.pv1 as indexValue,a.rk1 as listRank
         |from (
         |    select a.hotCategory,postId,blogid,publishdate,blogname,pv1,pv2,row_number() over(partition by hotCategory  order by a.rate desc ,a.pv1 desc) as rk1
         |    from (
         |        select a.hotCategory,a.postId,a.blogid,a.publishdate,a.blogname,a.pv1, pv2,case when a.pv2=0 then 999999.99 else pv1/pv2  end as rate
         |        from (
         |            select b.hotCategory,b.postId,b.blogid,b.publishdate,b.blogname,c.pv1,nvl(c.pv2,0) as pv2
         |            from hotPosts b
         |            join (
         |                select postid,
         |                     sum(if(dt between '$last1Day' and '$date', hd_pv, 0)) as pv1,
         |                     sum(if(dt between '$last3Day' and '$last2Day', hd_pv, 0)) as pv2
         |                from lofter.dws_post_interaction_di
         |                where dt between '$last3Day' and '$date'
         |                group by postid
         |                having pv1 >= 50
         |            )  c on b.postid=c.postid
         |        ) a
         |    ) a
         |) a
         |where a.rk1 <= 100
         |""".stripMargin

    val sql_hot_comment_list =
      s"""
         |select 3 as listSource,4 as listType,a.hotCategory as listName,
         |    a.postId,a.blogId,a.publishDate,a.blogName,a.pv as indexValue,a.rk1 as listRank
         |from (
         |    select b.hotCategory,b.postId,b.blogid,b.publishdate,b.blogname,c.pv,
         |           row_number() over (partition by  b.hotCategory order by c.pv desc) as rk1
         |    from hotPosts b
         |    join (
         |        select postid,sum(response_pv) as pv
         |        from lofter.dws_post_interaction_di
         |        where dt between '$oneWeekAgo' and '$date'
         |        group by postid
         |        having pv >= 10
         |    ) c on b.postid=c.postid
         |) a
         |where rk1 <= 100
         |""".stripMargin

    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")

    spark.sql(sql_hot_watch_list).createOrReplaceTempView("t1")
    spark.sql(sql_hot_content_list).createOrReplaceTempView("t2")
    spark.sql(sql_hot_growth_list).createOrReplaceTempView("t3")
    spark.sql(sql_hot_comment_list).createOrReplaceTempView("t4")

    val sql_result =
      s"""
         |select *, md5(concat(listType,listName)) as distinctId,
         |    md5(concat(listType,listName,listSource,'$date')) as uniqId
         |from t1
         |union all
         |select *, md5(concat(listType,listName)) as distinctId,
         |    md5(concat(listType,listName,listSource,'$date')) as uniqId
         |from t2
         |union all
         |select *, md5(concat(listType,listName)) as distinctId,
         |    md5(concat(listType,listName,listSource,'$date')) as uniqId
         |from t3
         |union all
         |select *, md5(concat(listType,listName)) as distinctId,
         |    md5(concat(listType,listName,listSource,'$date')) as uniqId
         |from t4
         |""".stripMargin

    spark.sql(sql_result)
      .select("listType","listName","postId","blogId","publishDate","blogName","indexValue","listRank","distinctId","uniqId")
      .withColumn("dt", lit(date))
      .withColumn("listSource", lit(3))
      .write
      .mode("overwrite")
      .insertInto("lofter_dm.ads_grain_hot_list_di")

    spark.close()
  }

}
