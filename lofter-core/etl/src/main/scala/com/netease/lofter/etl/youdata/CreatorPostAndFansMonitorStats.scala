package com.netease.lofter.etl.youdata

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}

object CreatorPostAndFansMonitorStats {

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Creator Post And Fans Monitor Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val yesterday = DateTime.yesterday.toString("yyyy-MM-dd")
    val date = pargs.optional("date").getOrElse(yesterday)

    val period = Seq(1,3,7)

    spark.sql("create temporary function bitmap_count as 'com.netease.wm.udf.bitmap.BitmapCountUDF'")
    // stat the period for 1DaysAgo, 7DaysAgo, 7DaysAgo
    for (daysAgo <- period) {
      creatorPostAndFansStats(spark, date, daysAgo)
    }

    def creatorPostAndFansStats(spark: SparkSession, date: String, period: Int): Unit = {
      val baseDay = DateTime.parse(date).minusDays(period-1).toString("yyyy-MM-dd")

      val sql_post_info =
        s"""
           |select distinct a.postid,a.blogid,a.publishdate,
           |    case when b.blogid is not null then b.level
           |         when b.blogid is null and c.blogid is not null then 'official'
           |         when b.blogid is null and c.blogid is null and d.userid is not null then 'mcn'
           |         else 'else' end as blog_type
           |from
           |   (select blogid,publishdate,id postid
           |    from lofter.dim_post
           |    where  ispublished=true and isforbidden=false and iscitedpost=false
           |        and allowview=0
           |        and ((movefrom not in("blog","lofternetease","blog163like","loftmove","BLOGPOST","bbs","photo-pp","163_news","instagram_mirror","weibo_sync","news","pp","netease_photo" )
           |        and  movefrom not like "%blog%" and  movefrom not like "%move%") or movefrom is null) /**剔除各种导入来源**/
           |        and contenttype in ("图片","文字","视频")
           |        and publishdate = '$baseDay' and isImported=0 and isActivityAutoPost=0 ) a
           |    left join
           |    (
           |    select userId as blogid, level
           |    from lofter.dws_par_creator_dd
           |    where dt='$yesterday' and level is not null
           |    ) b
           |    on a.blogid=b.blogid
           |    left join
           |    (
           |     select id blogid
           |     from lofter.dim_blog
           |     where isofficial=1
           |    ) c
           |    on a.blogid=c.blogid
           |    left join
           |    (
           |      select userid  from lofter_db_dump.ods_db_media_account_import_nd  where platformtype in('1','2','3','4','5','6','7')
           |    ) d
           |    on a.blogid=d.userid
           |""".stripMargin

      val sql_post_ebc =
        s"""
           |select postId, post_userId as blogId,
           |    sum(expose_pv) as exposure_pv,
           |    sum(expose_uv) as exposure_uv,
           |    sum(click_pv) as click_pv,
           |    sum(click_uv) as click_uv,
           |    sum(real_browse_pv) as real_browse_pv,
           |    sum(real_browse_uv) as real_browse_uv,
           |    sum(fans_expose_pv) as fans_exposure_pv,
           |    sum(fans_expose_uv) as fans_exposure_uv,
           |    sum(fans_click_pv) as fans_click_pv,
           |    sum(fans_click_uv) as fans_click_uv,
           |    sum(fans_real_browse_pv) as fans_real_browse_pv,
           |    sum(fans_real_browse_uv) as fans_real_browse_uv
           |from lofter.dws_post_traffic_di
           |where dt between '$baseDay' and '$date'
           |group by postId, post_userId
           |""".stripMargin

      val sql_post_hd =
        s"""
           |select postId,
           |    sum(response_pv) as response_pv,
           |    sum(bitmap_count(response_device_bitmap)) as response_uv,
           |    sum(hot_pv) as hot_pv,
           |    sum(bitmap_count(hot_device_bitmap)) as hot_uv,
           |    sum(fans_response_pv) as fans_response_pv,
           |    sum(bitmap_count(fans_response_user_bitmap)) as fans_response_uv,
           |    sum(fans_hot_pv) as fans_hot_pv,
           |    sum(bitmap_count(fans_hot_user_bitmap)) as fans_hot_uv
           |from lofter.dws_post_interaction_di
           |where dt between '$baseDay' and '$date'
           |group by postId
           |""".stripMargin

      val sql_blog_fans_active =
        s"""
           |select blogId,count(distinct t1.userid) as active_fans
           |from
           |    (
           |    select userid,blogid
           |    from  lofter_db_dump.ods_db_user_following_nd
           |    where from_unixtime(cast(followtime/1000 as bigint),'yyyy-MM-dd') <= '$baseDay'
           |    group by userid,blogid,followtime
           |    ) t1
           |    inner join
           |    (
           |    select dt,userid
           |    from lofter.device_active
           |    lateral view explode(userids) t1 as userid
           |    where dt between '$baseDay' and '$date'
           |    group by dt,userid
           |    ) t2
           |    on t1.userid=t2.userid
           |group by blogId
           |""".stripMargin

      spark.sql(sql_post_info).createOrReplaceTempView("creatorPostInfo")
      spark.sql(sql_post_ebc).createOrReplaceTempView("creatorPostEbc")
      spark.sql(sql_post_hd).createOrReplaceTempView("creatorPostHd")
      spark.sql(sql_blog_fans_active).createOrReplaceTempView("blogFansActive")

      val sql_result =
        s"""
           |select publishDate,blog_type,
           |     count(a.postId) as post_cnt, count(distinct a.blogId) as blog_cnt,
           |     avg(exposure_pv) as exposure_pv, avg(exposure_uv) as exposure_uv, avg(click_pv) as click_pv, avg(click_uv) as click_uv,
           |     avg(real_browse_pv) as real_browse_pv,avg(real_browse_uv) as real_browse_uv,
           |     avg(hot_pv) as hot_pv,avg(hot_uv) as hot_uv,avg(response_pv) as response_pv,avg(response_uv) as response_uv,
           |     avg(fans_exposure_pv) as fans_exposure_pv, avg(fans_exposure_uv) as fans_exposure_uv, avg(fans_click_pv) as fans_click_pv, avg(fans_click_uv) as fans_click_uv,
           |     avg(fans_real_browse_pv) as fans_real_browse_pv,avg(fans_real_browse_uv) as fans_real_browse_uv,
           |     avg(fans_hot_pv) as fans_hot_pv,avg(fans_hot_uv) as fans_hot_uv,avg(fans_response_pv) as fans_response_pv,avg(fans_response_uv) as fans_response_uv,
           |     avg(active_fans) as active_fans_uv
           |from creatorPostInfo a
           |left join creatorPostEbc b
           |on a.blogId=b.blogId and a.postId=b.postId
           |left join creatorPostHd c
           |on a.postId=c.postId
           |left join blogFansActive d
           |on a.blogId = d.blogId
           |group by publishDate,blog_type
           |""".stripMargin

      spark.sql(sql_result)
        .repartition(1)
        .withColumn("dt", lit(date))
        .withColumn("period", lit(period))
        .write.mode(SaveMode.Overwrite)
        .insertInto("lofter_dm.ads_creator_type_ebc_hd_monitor_di")
    }

    spark.close()
  }

}
