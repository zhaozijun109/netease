package com.netease.lofter.data.jobs.youdata

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.lit

object GrainNovelHotListsStats {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Grain Novel Hot List Stats")
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

    val sql_hot_novel =
      s"""
         |select a.*
         |from (
         |    select id, publishdate,blogid,tag,contenttype,blogname
         |    from lofter.dim_post
         |        lateral view explode(tags) myTable as tag
         |    where contenttype!='问答' and contenttype!='视频' and ispublished=true and
         |          valid=0 and allowview=0 and iscitedpost=false and tag in ('古言','现言','gb','悬疑')
         |) a
         |join (
         |    select postid
         |    from lofter_db_dump.ods_db_recommend_review_post_nd
         |    where recomstatus=1 group by postid
         |) b on a.id=b.postid
         |""".stripMargin
    
    spark.sql(sql_hot_novel).createOrReplaceTempView("hotNovel")

    val sql_hot_content_list =
      s"""
         |select 4 as listSource,2 as listType,a.tag as listName,
         |    a.id as postId,a.blogId,a.publishDate,a.blogName,a.pv as indexValue,a.rk1 as listRank
         |from (
         |    select a.id, publishdate,blogid,tag,contenttype,blogname,c.pv,row_number() over(partition by  a.tag order by c.pv desc) as rk1
         |    from hotNovel a
         |    join (
         |        select postid, sum(money * 10) as pv
         |        from lofter.dwd_gift_post_unlock_dd
         |        where dt='$date' and money > 0 and
         |              from_unixtime(cast(unlock_time/1000 as bigint), 'yyyy-MM-dd') between '$oneWeekAgo' and '$date'
         |        group by postid
         |        having pv>0
         |    ) c on a.id=c.postid
         |) a
         |where rk1 <= 100
         |""".stripMargin

    val sql_hot_growth_list =
      s"""
         |select 4 as listSource,3 as listType,a.tag as listName,
         |    a.id as postId,a.blogId,a.publishDate,a.blogName,a.pv as indexValue,a.rk1 as listRank
         |from (
         |    select a.id, publishdate,blogid,tag,contenttype,blogname,c.pv,row_number() over(partition by  a.tag order by c.pv desc) as rk1
         |    from hotNovel a
         |    join (
         |        select postid,sum(money * 10) as pv
         |        from lofter.dwd_gift_post_unlock_dd
         |        where dt='$date' and money > 0 and
         |              from_unixtime(cast(unlock_time/1000 as bigint), 'yyyy-MM-dd') between '$last2Day' and '$date'
         |        group by postid
         |        having pv > 0
         |    ) c on a.id=c.postid
         |) a
         |where rk1 <= 100
         |""".stripMargin

    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")
    spark.sql(sql_hot_content_list).createOrReplaceTempView("t2")
    spark.sql(sql_hot_growth_list).createOrReplaceTempView("t3")

    val sql_result =
      s"""
         |select *, md5(concat(listType,listName)) as distinctId,
         |    md5(concat(listType,listName,listSource,'$date')) as uniqId
         |from t2
         |union all
         |select *, md5(concat(listType,listName)) as distinctId,
         |    md5(concat(listType,listName,listSource,'$date')) as uniqId
         |from t3
         |""".stripMargin

    spark.sql(sql_result)
      .select("listType","listName","postId","blogId","publishDate","blogName","indexValue","listRank","distinctId","uniqId")
      .withColumn("dt", lit(date))
      .withColumn("listSource", lit(4))
      .write
      .mode("overwrite")
      .insertInto("lofter_dm.ads_grain_hot_list_di")

    spark.close()
  }

}
