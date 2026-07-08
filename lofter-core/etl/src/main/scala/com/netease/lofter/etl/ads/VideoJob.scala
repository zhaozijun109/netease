package com.netease.lofter.etl.ads

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}

object VideoJob {
  val videoStartDay = "2018-10-01"
  val videoIndexStartDay = "2021-01-01"

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .config("spark.sql.autoBroadcastJoinThreshold", "-1")
      .config("spark.sql.adaptive.autoBroadcastJoinThreshold", "-1")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))

    val sql_video_post_info =
      s"""
         |select a.postId,a.blogId,publishTime,nvl(b.platformType,0) as platformType
         |from (
         |  select id as postId,blogId, publishTime
         |  from lofter_db_dump.ods_db_post_nd where type=4 and citeRootPostid=0 and valid<>32 and isPublished = 1 and
         |       from_unixtime(cast(publishTime/1000 as bigint),'yyyy-MM-dd') between '$videoStartDay' and '$date'
         |) a
         |left join (
         |  select platformType,postId,blogId
         |  from lofter_db_dump.ods_db_media_post_import_nd
         |  where from_unixtime(cast(createTime/1000 as bigint),'yyyy-MM-dd') between '$videoStartDay' and '$date'
         |) b on a.postId=b.postId and a.blogId=b.blogId
         |""".stripMargin

    spark.sql(sql_video_post_info).repartition(1).cache().createOrReplaceTempView("postInfo")

    val sql_post_hd =
      s"""
         |select postId,
         |       sum(case when type=1 then 1 else 0 end) praisePv,
         |       sum(case when type=2 then 1 else 0 end) reproducePv,
         |       sum(case when type=3 then 1 else 0 end) recommendPv,
         |       sum(case when type=4 then 1 else 0 end) subscribePv,
         |       sum(case when type=10 then 1 else 0 end) commentPv,
         |       sum(case when type in(1,2,3,4) then 1 else 0 end) hotPv,
         |       count(distinct publisherUserId) as hdUv
         |from (
         |  select a.postId, a.publisherUserId, 10 as type
         |  from lofter_db_dump.ods_db_post_response_nd a join postInfo b on a.postId = b.postId
         |  where from_unixtime(cast(a.publishTime/1000 as bigint), 'yyyy-MM-dd') between '$videoIndexStartDay' and '$date'
         |
         |  union all
         |
         |  select a.postId, a.publisherUserId, a.type
         |  from lofter_db_dump.ods_db_post_hot_nd a join postInfo b on a.postId = b.postId
         |  where from_unixtime(cast(a.opTime/1000 as bigint), 'yyyy-MM-dd') between '$videoIndexStartDay' and '$date' and
         |        a.type in(1,2,3,4)
         |) t
         |group by postId
         |""".stripMargin

    spark.sql(sql_post_hd).createOrReplaceTempView("postHd")

    val sql_result =
      s"""
         |select a.postId as id,a.postId,blogId,publishTime,platformType,
         |       nvl(hotPv,0) as hotPv,nvl(praisePv,0) as praisePv, nvl(reproducePv,0) as reproducePv,
         |       nvl(recommendPv,0) as recommendPv, nvl(subscribePv,0) as subscribePv,
         |       nvl(view_pv,0) as viewPv,nvl(view_uv,0) as viewUv, nvl(play_pv,0) as playPv,nvl(play_uv,0) as playUv,
         |       nvl(real_play_pv,0) as realPlayPv,nvl(real_play_uv,0) as realPlayUv,nvl(finish_play_pv,0) as finishPlayPv,
         |       nvl(finish_play_uv,0) as finishPlayUv,nvl(real_play_time,0) as realPlayTime,nvl(play_time,0) as playTime,
         |       nvl(commentPv,0) as commentPv,nvl(hdUv,0) as hdUv,
         |       if(play_uv == 0 or play_uv is null or hdUv is null,0,hdUv/play_uv) as hdRatio
         |from postInfo a
         |left join postHd b on a.postId = b.postId
         |left join (
         |  select * from lofter_dm.ads_video_post_index_dd where dt = '$date'
         |) c on a.postId = c.postId
         |""".stripMargin

    spark.sql(sql_result)
      .withColumn("dt", lit(date))
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_video_post_dd")

    spark.stop()
  }

}
