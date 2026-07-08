package com.netease.lofter.data.jobs.flowcontrol

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}

object FlowControlEffectByPost {

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))
    val threeMonthAgo = DateTime.parse(date).minusMonths(3).toString("yyyy-MM-dd")

    val sql_project_post =
      s"""
         |select *
         |from lofter.dwd_dstr_flow_task_post_dd
         |where dt = '$date'
         |""".stripMargin

    spark.sql(sql_project_post).createOrReplaceTempView("projectPost")

    val sql_project_postId_first_enter_rec =
      s"""
         |select a.postId
         |from (
         |   select postId,count(1) as pv from projectPost group by postId having pv=1
         |) a
         |  join (
         |    select postId from projectPost
         |    where from_unixtime(cast(createTime/1000 as bigint),'yyyy-MM-dd')='$date'
         |  ) b on a.postId=b.postId
         |""".stripMargin

    spark.sql(sql_project_postId_first_enter_rec).cache().createOrReplaceTempView("firstEnterRecPostId")

    val sql_post_info =
      s"""
         |select a.id as postId,a.contentType,a.userId,
         |       from_unixtime(cast(publishTime / 1000 AS BIGINT), 'yyyy-MM-dd HH') as publishDate,
         |       nvl(customTags,'') as customTags,
         |       nvl(b.firstTag,'') as firstTag,nvl(secondTag,'') as secondTag,nvl(thirdTag,'') as thirdTag,
         |       nvl(enterAuditDate,'1970-01-01 08') as enterAuditDate,nvl(enterRecDate,'1970-01-01 08') as enterRecDate
         |from (select * from lofter.dim_post where contentType in('长文章','图片','文字','视频')) a
         |
         |left join
         |(select  postId,blogId,customTags,
         |         get_json_object(customtags, '$$[*].firstTag') as firstTag,
         |         get_json_object(customtags, '$$[*].secondTag') as secondTag,
         |         get_json_object(customtags, '$$[*].thirdTag') as thirdTag,
         |         from_unixtime(cast(createTime / 1000 AS BIGINT), 'yyyy-MM-dd HH') as enterAuditDate,
         |         case when recomstatus=1 then from_unixtime(cast(reviewTime / 1000 AS BIGINT), 'yyyy-MM-dd HH') else null end as enterRecDate
         |from lofter_db_dump.ods_db_recommend_review_post_nd
         |where length(customtags)>0 ) b
         |on a.id=b.postId
         |""".stripMargin

    spark.sql(sql_post_info).createOrReplaceTempView("postInfo")

    val post_daily =
      s"""
         |select a.flowTaskId, a.flowTaskType,
         |      a.postId,status,startTime,endTime,creator,
         |      b.userId,b.contentType,b.firstTag,b.secondTag,b.thirdTag,b.publishDate,b.enterRecDate,
         |      b.exposurePv,exposureUv,clickPv,clickUv,
         |      round(clickPv/exposurePv,4) as clickPvRatio, round(clickUv/exposureUv,4) as clickUvRatio,
         |      posHotPv, round(posHotPv * 1000 / exposurePv, 2) as hotFor1kPv,
         |      hdUv,browseUv,round(hdUv/browseUv,4) as hdRio,
         |      if(contentType = '视频', realBrowsePv, 0) as realPlayPv,
         |      if(playPv=0,0,round(finishPlayPv/playPv,4)) as finishPlayRatio,
         |      0 as realFinishPlayRatio,
         |      case when realBrowseUv>0 then round(realBrowseNewUv/realBrowseUv,4)
         |           else 0 end as newUvConsumeRatio
         |from projectPost a
         |    join (
         |      select * from lofter.dws_post_base_stats_di where dt='$date'
         |    ) b on a.postId = b.postId
         |""".stripMargin

    // stat the postId detail index for the postId has been entered the rec
    val post_acc =
      s"""
         |select a.flowTaskId, a.flowTaskType,
         |      a.postId, b.userId,b.contentType,b.customTags,b.firstTag,b.secondTag,b.thirdTag,b.publishDate,b.enterRecDate,
         |      c.exposurePv,exposureUv,clickPv,clickUv,
         |      posHotPv,hdUv,browseUv,
         |      if(b.contentType = '视频', realBrowsePv,0) as realPlayPv,finishPlayPv,playPv,realFinishPlayPv,
         |      if(b.contentType = '视频', realBrowseUv,0) as realPlayUv,realBrowseUv,realBrowseNewUv,
         |      nvl(d.level,-1) as level,
         |      if(e.postId is not null, 1, 0) as isNewRecPost
         |from projectPost a
         |  left join postInfo b on a.postId = b.postId
         |  join (
         |    select postId,exposurePv,exposureUv,clickPv,clickUv,posHotPv,hdUv,browseUv,
         |        playPv,realPlayPv,finishPlayPv,realFinishPlayPv,realPlayUv,realBrowsePv,realBrowseUv,realBrowseNewUv
         |    from lofter.dws_post_base_stats_dd where dt = '$date'
         |  ) c on a.postId = c.postId
         |  left join (
         |     select post_id as postId,max(level) as level from rec.rec_lofter_boost_article_adapt_v2 where day between '$threeMonthAgo' and '$date' group by post_id
         |  ) d on a.postId = d.postId
         |  left join firstEnterRecPostId e on a.postId = e.postId
         |""".stripMargin

    spark.sql(post_acc)
      .withColumn("dt", lit(date))
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_rec_dis_post_acc_di")

    spark.sql(post_daily)
      .withColumn("dt", lit(date))
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_rec_dis_post_di")

    spark.stop()
  }
}
