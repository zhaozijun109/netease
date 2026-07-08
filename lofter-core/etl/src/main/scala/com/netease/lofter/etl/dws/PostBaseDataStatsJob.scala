package com.netease.lofter.etl.dws

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.{SaveMode, SparkSession}

object PostBaseDataStatsJob {
  val COSTTIME_MAX: Long = 18000 * 1000L
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Post Base Data Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .config("spark.sql.autoBroadcastJoinThreshold","10485760")
      .getOrCreate()

    val yesterday = DateTime.yesterday.toString("yyyy-MM-dd")
    val date = pargs.optional("date").getOrElse(yesterday)
    val oneWeekAgo = DateTime.parse(date).minusDays(6).toString("yyyy-MM-dd")

    spark.sql("create temporary function bitmap_count as 'com.netease.wm.udf.bitmap.BitmapCountUDF'")
    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")

    val sql_post_info_common =
      s"""
         |select a.id as postId,a.contentType,a.userId,concat(a.blogName,'.lofter.com/post/',conv(a.blogId, 10, 16),'_',conv(a.id, 10, 16)) as url,
         |       from_unixtime(cast(publishTime / 1000 AS BIGINT), 'yyyy-MM-dd HH') as publishDate,a.tags,
         |       nvl(b.firstTag,'') as firstTag,nvl(secondTag,'') as secondTag,nvl(thirdTag,'') as thirdTag,
         |       nvl(recStatus,'else') as recStatus,nvl(securityLevel,-1) as securityLevel,
         |       nvl(enterAuditDate,'1970-01-01 08') as enterAuditDate,nvl(enterRecDate,'1970-01-01 08') as enterRecDate,
         |       case when d.userLevel=2 then '官号' when d.userLevel=1 then '达人' else '普通用户' end as userLevel,
         |       0 as wordsNum, 0 as photoNum,
         |       nvl(a.ImportPlatformType,'站内') as platformType,
         |       0L as duration, 0L as imgHeight, 0L as imgWidth
         |from (
         |  select * from lofter.dim_post where contentType in('长文章','图片','文字','视频')
         |) a
         |left join (
         | select  postId,blogId,
         |         regexp_replace(get_json_object(customTags, '$$[*].firstTag'),'"','') as firstTag,
         |         regexp_replace(get_json_object(customTags, '$$[*].secondTag'),'"','') as secondTag,
         |         regexp_replace(get_json_object(customTags, '$$[*].thirdTag'),'"','') as thirdTag,
         |         from_unixtime(cast(createTime / 1000 AS BIGINT), 'yyyy-MM-dd HH') as enterAuditDate,
         |         grade as securityLevel,
         |         case when recomStatus=1 then from_unixtime(cast(updateTime / 1000 AS BIGINT), 'yyyy-MM-dd HH')
         |              else null end as enterRecDate,
         |         case when recomStatus=0 then '初始'
         |              when recomStatus=1 then '推荐'
         |              when recomStatus=-1 then '不推荐'
         |              else '重新编辑' end as recStatus
         | from lofter_db_dump.ods_db_recommend_review_post_nd
         |  where length(customTags)>0
         |) b on a.id=b.postId
         |left join (
         |  select b.blogId,max(userLevel) as userLevel
         |  from (
         |    select blogId, 1 as userLevel
         |    from lofter_db_dump.ods_db_authenticate_blog_nd
         |    group by blogId
         |
         |    union all
         |
         |    select blogId, 2 as userLevel
         |    from lofter_db_dump.ods_db_verify_blog_nd
         |    group by blogId
         |  ) b
         |  group by blogId
         |) d on a.blogId = d.blogId
         |""".stripMargin

    spark.sql(sql_post_info_common).createOrReplaceTempView("postInfo")

    val sql_result =
      s"""
         |select a.*,
         |        nvl(click_pv,0) as clickPv, nvl(click_uv,0) as clickUv, nvl(browse_pv,0) as browsePv,nvl(browse_uv,0) as browseUv,
         |        nvl(expose_pv,0) as exposurePv, nvl(expose_uv,0) as exposureUv, nvl(real_browse_pv,0) as realBrowsePv,
         |        nvl(real_browse_uv,0) as realBrowseUv,nvl(browse_duration,0) as browseTime, nvl(real_browse_duration,0) as realBrowseTime,
         |        nvl(new_user_real_browse_uv,0) as realBrowseNewUv,
         |
         |        0L as playPv, 0L as playUv, cast(null as double) as playTime, cast(null as double) as playProgress,
         |        0L as realPlayPv, 0L as realPlayUv, cast(null as double) as realPlayTime, cast(null as double) as realPlayProgress,
         |        0L as finishPlayPv, 0L as finishPlayUv, 0L as realFinishPlayPv, 0L as realFinishPlayUv,
         |
         |        nvl(dislike_pv,0) as negFeedbackPv,nvl(dislike_uv,0) as negFeedbackUv,
         |
         |        nvl(share_pv,0L) as sharePv, nvl(bitmap_count(share_device_bitmap),0L) as shareUv,
         |
         |        nvl(praise_pv,0) as posPraisePv, nvl(bitmap_count(praise_device_bitmap),0L) as posPraiseUv, 0L as negPraisePv, 0L as negPraiseUv,
         |        nvl(recommend_pv,0) as posRecPv,nvl(bitmap_count(recommend_device_bitmap),0) as posRecUv, 0L as negRecPv, 0L as negRecUv,
         |        nvl(reproduce_pv,0) as posReproducePv, nvl(bitmap_count(reproduce_device_bitmap), 0) as posReproduceUv, 0L as negReproducePv, 0L as negReproduceUv,
         |        nvl(subscribe_pv,0) as posSubscribePv, nvl(bitmap_count(subscribe_device_bitmap), 0) as posSubscribeUv, 0L as negSubscribePv, 0L as negSubscribeUv,
         |        nvl(hot_pv,0) as posHotPv, nvl(bitmap_count(hot_device_bitmap),0) as posHotUv, 0L as negHotPv, 0L as negHotUv,
         |
         |        nvl(response_pv,0) as posCommentPv, nvl(bitmap_count(response_device_bitmap),0) as posCommentUv, 0L as negCommentPv, 0L as negCommentUv,
         |        nvl(hd_pv,0) as hdPv, nvl(bitmap_count(hd_device_bitmap),0) as hdUv,
         |
         |        nvl(rewardUv,0) as rewardUv, nvl(rewardAmount,0) as rewardAmount,
         |        nvl(freeGiftUv,0) as freeGiftUv, nvl(freeGiftAmount,0) as freeGiftAmount,
         |        nvl(chargeGiftUv,0) as chargeGiftUv, nvl(chargeGiftAmount,0) as chargeGiftAmount,
         |        nvl(centralized_expose_pv,0) as centralizedExposurePv, nvl(centralized_expose_uv,0) as centralizedExposureUv,
         |        nvl(non_centralized_expose_pv,0) as nonCentralizedExposurePv, nvl(non_centralized_expose_uv,0) as nonCentralizedExposureUv,
         |        nvl(valid_response_pv,0) as valid_response,
         |        nvl(j.support_exposure_pv,0) as support_exposure_pv, nvl(j.support_induced_pv,0) as support_induced_pv
         |from postInfo a
         |left join (select * from lofter.dws_post_traffic_di where dt='$date') b
         |on a.postId=b.postId
         |left join (select * from lofter.dws_post_interaction_di where dt='$date') f
         |on a.postId=f.postId
         |left join (
         |    select postid,
         |           cast(0 as bigint) as rewarduv,
         |           cast(0.0 as double) as rewardamount,
         |           count(distinct if(grain_tickets > 0, userId, null)) as freegiftuv,
         |           sum(grain_tickets) as freegiftamount,
         |           count(distinct if(money > 0, userId, null) ) as chargegiftuv,
         |           cast(sum(money * 10) as bigint) as chargegiftamount
         |    from lofter.dwd_gift_post_unlock_dd
         |    where dt='$date' and from_unixtime(cast(unlock_time/1000 as bigint), 'yyyy-MM-dd') = '$date'
         |    group by 1
         |) h
         |on a.postId=h.postId
         |left join (select * from lofter.dws_post_support_di where dt='$date') j
         |on a.postId = j.postId
         |""".stripMargin

    spark.sql(sql_result).filter("clickPv > 0 or exposurePv > 0 or playPv > 0 or posHotPv > 0 or posCommentPv > 0").write.mode(SaveMode.Overwrite)
      .parquet(s"/user/da_lofter/hive_db/lofter.db/dws_post_base_stats_di/dt=$date")
    spark.sql(s"alter table lofter.dws_post_base_stats_di add if not exists partition(dt='$date')")

    spark.close()
  }

}
