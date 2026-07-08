package com.netease.lofter.etl.ads

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.lit

object BatchGrainHotListsStats {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Batch Grain Hot List Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))
    val dateNum = DateTime.parse(date).getDayOfMonth
    val dateNum2 = date.substring(5)
    if (dateNum2.equalsIgnoreCase("01-31")){
      annualBatchGrainStats(spark,date)
    } else if (dateNum == 27 || dateNum == 1) {
      monthBatchGrainStats(spark,date)
    } else {
      println(s"there is no need to cal on date $date ")
    }

    if (DateTime.parse(date).dayOfWeek().get()==5 ) {
      weekBatchGrainStats(spark, date)
    }

    def annualBatchGrainStats(spark: SparkSession, date: String): Unit = {
      val year = DateTime.parse(date).getYear -1
      val yearStart = s"$year-01-01"
      val yearEnd = s"$year-12-31"

      val sql_post_info =
        s"""
           |select id as postId,blogid,tag,publishDate,contentType
           |    from lofter.dim_post
           |    lateral view explode(tags) myTable as tag
           |    where contenttype in ('长文章','文字','图片','视频')  and ispublished=true and
           |    valid=0 and allowview=0 and iscitedpost=false and publishDate between '$yearStart' and '$yearEnd'
           |    group by id,blogid,tag,publishDate,contentType
           |""".stripMargin

      val sql_tag =
        s"""
           |select a.tag,a.uv
           |from
           |(select a.tag,count(distinct a.blogid) as uv
           |from
           |    postInfo a
           |group by a.tag) a
           |
           |left join
           |(select tagName FROM lofter_db_dump.ods_db_recommend_tag_new_nd WHERE blackTag > 0
           |union
           |select tag as tagName from lofter.zq_lofter_liangdan_black_tag group by tag
           |union
           |select name as tagName
           |from lofter_db_dump.ods_db_cmb_tag_nd
           |where status=0 and cpflag=1  and otherprops='真人cp' group by name
           |) b
           |on a.tag=b.tagname
           |where b.tagname is null and a.uv>=3000
           |""".stripMargin

      spark.sql(sql_post_info).createOrReplaceTempView("postInfo")
      spark.sql(sql_tag).createOrReplaceTempView("tagInfo")

      val sql_result =
        s"""
           |select '$year' as period,a.tag,a.postid,a.blogid,a.hot,rk,
           |    md5(concat(tag,'$year')) as distinctId,contentType
           |from
           |(select a.*,b.hot,row_number() over(partition by a.tag order by b.hot desc,publishDate desc) as rk
           |from
           |(select a.tag,b.postId,b.blogId,b.publishDate,contentType
           |from
           |tagInfo a
           |join
           |postInfo b
           |on a.tag=b.tag
           |group by a.tag,b.postId,b.blogId,b.publishDate,contentType) a
           |join
           |(select postid, (favoritecount+reblogcount+sharecount+subscribecount) as hot from lofter_db_dump.ods_db_post_count_nd) b
           |on a.postid=b.postid) a
           |where a.rk<=500
           |""".stripMargin

      spark.sql(sql_result)
        .repartition(5)
        .withColumn("dt", lit(date))
        .withColumn("statType",lit("year"))
        .write
        .mode("overwrite")
        .insertInto("lofter_dm.ads_grain_batch_hot_list_di")

    }

    def monthBatchGrainStats(spark: SparkSession, date: String): Unit = {
      val year = DateTime.parse(date).getYear
      val lastYear = year - 1
      val month = DateTime.parse(date).getMonthOfYear
      val lastMonth = if(month == 1) 12 else month-1
      val monthStart = if(month == 1) s"$lastYear-$lastMonth-01" else if(lastMonth<10) s"$year-0$lastMonth-01" else s"$year-$lastMonth-01"
      val monthEnd = DateTime.parse(s"$year-$month-01").minusDays(1).toString("yyyy-MM-dd")
      val monthNum = monthStart.substring(0,7)

      val sql_post_info =
        s"""
           |select id as postId,blogid,tag,publishDate,contentType
           |    from lofter.dim_post
           |    lateral view explode(tags) myTable as tag
           |    where contenttype in ('长文章','文字','图片','视频')  and ispublished=true and
           |    valid=0 and allowview=0 and iscitedpost=false and publishDate between '$monthStart' and '$monthEnd'
           |    group by id,blogid,tag,publishDate,contentType
           |""".stripMargin

      val sql_tag =
        s"""
           |select a.tag,a.uv
           |from
           |(select a.tag,count(distinct a.blogid) as uv
           |from
           |    postInfo a
           |group by a.tag) a
           |
           |left join
           |(select tagName FROM lofter_db_dump.ods_db_recommend_tag_new_nd WHERE blackTag > 0
           |union
           |select tag as tagName from lofter.zq_lofter_liangdan_black_tag group by tag
           |union
           |select name as tagName
           |from lofter_db_dump.ods_db_cmb_tag_nd
           |where status=0 and cpflag=1  and otherprops='真人cp' group by name
           |) b
           |on a.tag=b.tagname
           |where b.tagname is null and a.uv>=500
           |""".stripMargin

      spark.sql(sql_post_info).createOrReplaceTempView("postInfo")
      spark.sql(sql_tag).createOrReplaceTempView("tagInfo")

      val sql_result =
        s"""
           |select '$monthNum' as period,a.tag,a.postid,a.blogid,a.hot,rk,
           |    md5(concat(tag,'$monthNum')) as distinctId,contentType
           |from
           |(select a.*,b.hot,row_number()over(partition by a.tag order by b.hot desc,publishDate desc ) as rk
           |from
           |(select a.tag,b.postId,b.blogId,b.publishDate,contentType
           |from
           |tagInfo a
           |join
           |postInfo b
           |on a.tag=b.tag
           |group by a.tag,b.postId,b.blogId,b.publishDate,contentType) a
           |join
           |(select postid, (favoritecount+reblogcount+sharecount+subscribecount) as hot from lofter_db_dump.ods_db_post_count_nd) b
           |on a.postid=b.postid) a
           |where a.rk<=100
           |""".stripMargin

      spark.sql(sql_result)
        .repartition(5)
        .withColumn("dt", lit(date))
        .withColumn("statType",lit("month"))
        .write
        .mode("overwrite")
        .insertInto("lofter_dm.ads_grain_batch_hot_list_di")

    }

    def weekBatchGrainStats(spark: SparkSession, date: String): Unit = {
      val weekStart = DateTime.parse(date).minusDays(6).toString("yyyy-MM-dd")
      val weekOfYear = DateTime.parse(date).getWeekOfWeekyear
      val weekNum = date.substring(0,7) + s"-$weekOfYear"

      val sql_post_info =
        s"""
           |select id as postId,blogid,tag,publishDate,contentType
           |    from lofter.dim_post
           |    lateral view explode(tags) myTable as tag
           |    where contenttype in ('长文章','文字','图片','视频')  and ispublished=true and
           |    valid=0 and allowview=0 and iscitedpost=false and publishDate between '$weekStart' and '$date'
           |    group by id,blogid,tag,publishDate,contentType
           |""".stripMargin

      val sql_tag =
        s"""
           |select a.tag,a.uv
           |from
           |(select a.tag,count(distinct a.blogid) as uv
           |from
           |    postInfo a
           |group by a.tag) a
           |
           |left join
           |(select tagName FROM lofter_db_dump.ods_db_recommend_tag_new_nd WHERE blackTag > 0
           |union
           |select tag as tagName from lofter.zq_lofter_liangdan_black_tag group by tag
           |union
           |select name as tagName
           |from lofter_db_dump.ods_db_cmb_tag_nd
           |where status=0 and cpflag=1  and otherprops='真人cp' group by name
           |) b
           |on a.tag=b.tagname
           |where b.tagname is null and a.uv>=300
           |""".stripMargin

      spark.sql(sql_post_info).createOrReplaceTempView("postInfo")
      spark.sql(sql_tag).createOrReplaceTempView("tagInfo")

      val sql_result =
        s"""
           |select '$weekNum' as period,a.tag,a.postid,a.blogid,a.hot,rk,
           |    md5(concat(tag,'$weekNum')) as distinctId,contentType
           |from
           |(select a.*,b.hot,row_number()over(partition by a.tag order by b.hot desc,publishDate desc ) as rk
           |from
           |(select a.tag,b.postId,b.blogId,b.publishDate,contentType
           |from
           |tagInfo a
           |join
           |postInfo b
           |on a.tag=b.tag
           |group by a.tag,b.postId,b.blogId,b.publishDate,contentType) a
           |join
           |(select postid, (favoritecount+reblogcount+sharecount+subscribecount) as hot from lofter_db_dump.ods_db_post_count_nd) b
           |on a.postid=b.postid) a
           |where a.rk<=50
           |""".stripMargin

      spark.sql(sql_result)
        .repartition(5)
        .withColumn("dt", lit(date))
        .withColumn("statType",lit("week"))
        .write
        .mode("overwrite")
        .insertInto("lofter_dm.ads_grain_batch_hot_list_di")
    }

    spark.close()
  }

}
