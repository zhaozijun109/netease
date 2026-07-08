package com.netease.lofter.etl.youdata

import com.github.nscala_time.time.Imports._
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}

/**
  * export various data into youdata rds
  */
object CoreActionStatsJob {

  case class tagDomain(domainName: String, id: Long)

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Core Action Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val yesterday = DateTime.yesterday.toString("yyyy-MM-dd")
    val date = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))
    val statDays = 30
    val weekAgo = DateTime.parse(date).minusDays(7).toString("yyyy-MM-dd")

    import org.apache.spark.sql.types._

    val sql_tagDomain =
      s"""
         |select id, domainName
         |from lofter.dim_domain
       """.stripMargin.replaceAll(raw"[\n\r\s]+", " ")

    spark.sql(sql_tagDomain).createOrReplaceTempView("tagDomain")


    val postSql =
      s"""
         |select *,concat('qatest5.lofter.com/post/', blogid, '_', postid) as url,
         |         concat('https://qatest5.lofter.com/post/', blogid, '_', postid) as view_url
         |from (
         |    select a.postId as pid, lower(hex(a.postId)) as postId, lower(hex(a.blogId)) as blogId, a.blogId as bid, a.blogName, a.blogNickName,
         |       a.contentType, concat_ws(',', a.tags) tags, if(a.citedParentPostId > 0, 1, 0) cited,
         |       a.hot, a.commendUserCount, a.commendCount, a.praiseCount, a.praiseUserCount, a.recommendCount, a.reproduceUserCount, a.reproduceCount, a.subscribeUserCount, a.subscribeCount,
         |       a.newHot, a.postPublishDate, b.avgHot, if(b.avgHot > 0, (a.hot - b.avgHot) / b.avgHot, 0) as hotChangeRatio,
         |       a.domains
         |    from (
         |        select * from lofter_dm.ads_post_general_di where dt = '$date'
         |    ) a
         |    left join (
         |        select * from lofter_dm.ads_post_general_7d where dt = '$date'
         |     ) b on a.postId = b.postId
         |) aa
        """.stripMargin

    spark.sql(postSql).createOrReplaceTempView("post_stats")

    spark.sql("select * from post_stats order by hot desc limit 500")
      .drop("domains")
      .withColumn("dt", lit(date))
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_post_hot_top_di")

    spark.sql(s"select * from post_stats where postPublishDate = '$date' order by hot desc limit 500")
      .drop("domains")
      .withColumn("dt", lit(date))
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_post_hot_top_of_new_di")

    spark.sql(s"select * from post_stats order by hotChangeRatio desc limit 500")
      .drop("domains")
      .withColumn("dt", lit(date))
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_post_potential_top_di")

    // domain hot post
    val domainHotPostTop100 =
      """
        |select *
        |from (
        |  select t.*, d.domainName, row_number() over (partition by t.domainId order by t.hot desc) as rnk
        |  from (
        |    select p.*, explode(p.domains) as domainId, concat('qatest5.lofter.com/post/', blogid, '_', postid) as url
        |    from post_stats p
        |  ) t join lofter.dim_domain d on t.domainId = d.id
        |) f
        |where rnk <= 100
      """.stripMargin

    spark.sql(domainHotPostTop100)
      .drop("domains")
      .withColumn("dt", lit(date))
      .write
      .mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_post_hot_top_of_domain_di")

    // tag
    val tagSql =
      s"""
         |select a.tag, a.domainName, a.isActivityTag,
         |       a.hot, a.browsingUserCount, a.browsingCount, a.subscribeCount, a.postCount,
         |       a.commendCount, a.praiseCount, a.recommendCount, a.reproduceCount, a.newPostHot, a.newUserHot,
         |       a.newBrowsingCount, a.newBrowsingUserCount, a.newSubscribeCount, a.newCommendCount,
         |       b.avgHot, b.avgBrowsingCount, if(b.avgHot > 0, (a.hot - b.avgHot) / b.avgHot, 0) as hotChangeRatio
         |from (
         |    select * from lofter_dm.ads_tag_general_di where dt = '$date'
         |) a
         |left join (
         |    select * from lofter_dm.ads_tag_general_7d where dt = '$date'
         |) b on a.tag = b.tag and a.domainName = b.domainName
        """.stripMargin

    spark.sql(tagSql).createOrReplaceTempView("tag_stats")

    spark.sql("select * from tag_stats order by hot desc limit 500")
      .withColumn("dt", lit(date))
      .write
      .mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_tag_hot_top_di")

    spark.sql("select * from tag_stats order by hotChangeRatio desc limit 500")
      .withColumn("dt", lit(date))
      .write
      .mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_tag_potential_top_di")

    val tag3dSql =
      s"""
         |select a.tag, a.domainName, a.isActivityTag,
         |       a.hot, a.browsingUserCount, a.browsingCount, a.subscribeCount, a.postCount,
         |       a.commendCount, a.praiseCount, a.recommendCount, a.reproduceCount, a.newPostHot, a.newUserHot,
         |       a.newBrowsingCount, a.newBrowsingUserCount, a.newSubscribeCount, a.newCommendCount,
         |       b.avgHot, b.avgBrowsingCount, if(b.avgHot > 0, (a.avgHot - b.avgHot) / b.avgHot, 0) as hotChangeRatio
         |from (
         |    select * from lofter_dm.ads_tag_general_3d where dt = '$date'
         |) a
         |left join (
         |    select tag, domainName, avg(avgHot) as avgHot, avg(browsingCount) as avgBrowsingCount
         |    from lofter_dm.ads_tag_general_3d
         |    where dt <= '$date' and dt > '$weekAgo'
         |    group by tag, domainName
         |) b on a.tag = b.tag and a.domainName = b.domainName
         |where a.dt = '$date'
        """.stripMargin
    spark.sql(tag3dSql).createOrReplaceTempView("tag_stats_3d")

    spark.sql("select * from tag_stats_3d order by hot desc limit 500")
      .withColumn("dt", lit(date))
      .write
      .mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_tag_hot_top_3d")

    spark.sql("select * from tag_stats_3d order by hotChangeRatio desc limit 500")
      .withColumn("dt", lit(date))
      .write
      .mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_tag_potential_top_3d")

    val tag7dSql =
      s"""
         |select a.tag, a.domainName, a.isActivityTag,
         |       a.hot, a.browsingUserCount, a.browsingCount, a.subscribeCount, a.postCount,
         |       a.commendCount, a.praiseCount, a.recommendCount, a.reproduceCount, a.newPostHot, a.newUserHot,
         |       a.newBrowsingCount, a.newBrowsingUserCount, a.newSubscribeCount, a.newCommendCount,
         |       b.avgHot, b.avgBrowsingCount, if(b.avgHot > 0, (a.avgHot - b.avgHot) / b.avgHot, 0) as hotChangeRatio
         |from (
         |    select * from lofter_dm.ads_tag_general_7d where dt = '$date'
         |) a
         |left join (
         |    select tag, domainName, avg(avgHot) as avgHot, avg(browsingCount) as avgBrowsingCount
         |    from lofter_dm.ads_tag_general_7d
         |    where dt <= '$date' and dt > '$weekAgo'
         |    group by tag, domainName
         |) b on a.tag = b.tag and a.domainName = b.domainName
         |where a.dt = '$date'
        """.stripMargin
    spark.sql(tag7dSql).createOrReplaceTempView("tag_stats_7d")

    spark.sql("select * from tag_stats_7d order by hot desc limit 500")
      .withColumn("dt", lit(date))
      .write
      .mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_tag_hot_top_7d")

    spark.sql("select * from tag_stats_7d order by hotChangeRatio desc limit 500")
      .withColumn("dt", lit(date))
      .write
      .mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_tag_potential_top_7d")

    val tag15dSql =
      s"""
         |select a.tag, a.domainName, a.isActivityTag,
         |       a.hot, a.browsingUserCount, a.browsingCount, a.subscribeCount, a.postCount,
         |       a.commendCount, a.praiseCount, a.recommendCount, a.reproduceCount, a.newPostHot, a.newUserHot,
         |       a.newBrowsingCount, a.newBrowsingUserCount, a.newSubscribeCount, a.newCommendCount,
         |       b.avgHot, b.avgBrowsingCount, if(b.avgHot > 0, (a.avgHot - b.avgHot) / b.avgHot, 0) as hotChangeRatio
         |from (select * from lofter_dm.ads_tag_general_15d where dt = '$date') a
         |left join (
         |    select tag, domainName, avg(avgHot) as avgHot, avg(browsingCount) as avgBrowsingCount
         |    from lofter_dm.ads_tag_general_15d
         |    where dt <= '$date' and dt > '$weekAgo'
         |    group by tag, domainName
         |) b on a.tag = b.tag and a.domainName = b.domainName
         |where a.dt = '$date'
        """.stripMargin
    spark.sql(tag15dSql).createOrReplaceTempView("tag_stats_15d")

    spark.sql("select * from tag_stats_15d order by hot desc limit 500")
      .withColumn("dt", lit(date))
      .write
      .mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_tag_hot_top_15d")

    spark.sql("select * from tag_stats_15d order by hotChangeRatio desc limit 500")
      .withColumn("dt", lit(date))
      .write
      .mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_tag_potential_top_15d")

    // stat for the message
    val message_sql  =
      s"""
         |select clienttype,
         |    count(distinct accountid) push_uv,
         |    count(distinct case when accountid<>uidid then uidid else null end) receive_uv,
         |    count(distinct case when accountid<>uidid then messageid else null end) push_num,
         |    count(distinct case when accountid=uidid then messageid else null end) receive_num
         |from lofter.ods_log_message_di
         |where dt = '$date'
         |group by clienttype
       """.stripMargin

    spark.sql(message_sql)
      .withColumn("dt",lit(date))
      .write
      .mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_message_stat_of_client_di")

    // stat the post according to the contentType
    val sql_post_content =
      s"""
         |select t1.clienttype,t1.contenttype,t2.iscitedpost,
         |   count(distinct t1.postid) postnum,
         |   count(distinct t1.userid) postuv
         |from (
         |    select '$date' as dt, platform as clientType,
         |           post_content_type as contentType, postid, userid
         |    from lofter.dwd_post_publish_di
         |    where dt = '$date'
         |) t1
         |left join (
         |    select id,iscitedpost
         |    from lofter.dim_post
         |) t2 on t1.postid=t2.id
         |join lofter.dim_user u on t1.userId = u.id
         |where u.isTest = 0
         |group by t1.dt, t1.clientType, t1.contentType, t2.iscitedpost
         |grouping sets((t1.dt,t1.clienttype,t1.contenttype,t2.iscitedpost),
         |              (t1.dt,t1.clienttype,t1.contenttype),
         |              (t1.dt,t1.contenttype),(t1.dt))
       """.stripMargin

    spark.sql(sql_post_content)
      .withColumn("dt",lit(date))
      .write
      .mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_post_publish_by_content_type_di")

    spark.close()
  }
}
