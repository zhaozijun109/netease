package com.netease.lofter.etl.ads

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.lit

object ReturnGiftHotListsStats {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Return Gift Hot List Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))

    if (DateTime.parse(date).dayOfWeek().get() == 5) {
      weekGiftReturnStats(spark, date)
    }

    def weekGiftReturnStats(spark: SparkSession, date: String): Unit = {
      val weekStart = DateTime.parse(date).minusDays(13).toString("yyyy-MM-dd")
      val weekOfYear = DateTime.parse(date).getWeekOfWeekyear
      val weekNum = date.substring(0,7) + s"-$weekOfYear"

      val sql_post_info =
        s"""
           |select a.*,b.blogId,b.tag
           |from
           |(select postid,platform_type,content_type,count(1) as num
           |from lofter.dwd_paid_post_detail_dd
           |where from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') between '$weekStart' and '$date'
           |   and dt='$date' and content_type in ('文字','图片') and return_gift_id>0 and platform_type in ('博客引入','图文UGC')
           |group by postid,platform_type,content_type) a
           |join
           |(select id,blogid,tag
           |from lofter.dim_post
           |lateral view explode(tags) myTable as tag
           |where ispublished=true and valid=0 and allowview=0 ) b
           |on a.postid=b.id
           |""".stripMargin

      val sql_exclude_tag =
        s"""
           |select tagName FROM lofter_db_dump.ods_db_recommend_tag_new_nd WHERE blackTag > 0
           |union
           |select tag as tagName from lofter.zq_lofter_liangdan_black_tag group by tag
           |union
           |select name as tagName
           |from lofter_db_dump.ods_db_cmb_tag_nd
           |where status=0 and cpflag=1  and otherprops='真人cp' group by name
           |""".stripMargin

      spark.sql(sql_post_info).createOrReplaceTempView("postInfo")
      spark.sql(sql_exclude_tag).createOrReplaceTempView("excludeTags")

      val sql_result_all =
        s"""
           |select '$weekNum' as period, 'overall' as tag,a.postid,a.blogid,a.platform_type,a.content_type,a.num,rk,
           |    md5(concat('overall','$weekNum',platform_type,content_type)) as distinctId
           |from
           |   (
           |   select  a.postid,a.platform_type,a.content_type,a.num,a.blogId,
           |        row_number() over (partition by a.platform_type,a.content_type order by a.num desc) as rk
           |   from postInfo a
           |   left join excludeTags b
           |   on a.tag=b.tagName
           |   where b.tagName is null and a.num>=500
           |   group by a.postid,a.platform_type,a.content_type,a.num,a.blogId
           |   ) a
           |where a.rk<=1000
           |""".stripMargin

      val sql_result_tag =
        s"""
           |select '$weekNum' as period, a.tag,a.postid,a.blogid,a.platform_type,a.content_type,a.num,rk,
           |    md5(concat(tag,'$weekNum',platform_type,content_type)) as distinctId
           |from
           |   (
           |   select  a.tag,a.postid,a.platform_type,a.content_type,a.num,a.blogId,
           |        row_number() over (partition by a.tag,a.platform_type,a.content_type order by a.num desc) as rk
           |   from postInfo a
           |   join lofter.zq_lofter_liangdan_white_tag c
           |   on a.tag=c.tag
           |   left join excludeTags b
           |   on a.tag=b.tagName
           |   where b.tagName is null and a.num>=500
           |   group by a.tag,a.postid,a.platform_type,a.content_type,a.num,a.blogId
           |   ) a
           |where a.rk<=100
           |""".stripMargin

      spark.sql(sql_result_all).union(spark.sql(sql_result_tag))
        .repartition(1)
        .withColumn("dt", lit(date))
        .withColumn("statType",lit("week"))
        .write
        .mode("overwrite")
        .insertInto("lofter_dm.ads_gift_return_hot_list_di")
    }

    spark.close()
  }

}
