package com.netease.lofter.etl.ads

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}

object UserDataCenterWeeklyStats {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter User Data Center Weekly Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val yesterday = DateTime.yesterday.toString("yyyy-MM-dd")
    val date = pargs.optional("date").getOrElse(yesterday)
    val isFriday = DateTime.parse(date).getDayOfWeek == 5

    if (isFriday) {
      val weekStart = DateTime.parse(date).minusDays(6).toString("yyyy-MM-dd")
      val weekEnd = date

      val sql_user_post_behavior =
        s"""
           |select a.*,
           |    c.favoriteTag,favoriteTagPostNum,favoriteTagUv,
           |    d.hotNum,responseNum,favoriteNum,reblogNum,shareNum,subscribeNum,
           |    e.giftMoney,rewardMoney
           |from
           |(select blogId,
           |    count(if(publishDate between '$weekStart' and '$date' and contentType<>'问答',a.id,null)) as postNum,
           |    sum(if(publishDate between '$weekStart' and '$date',words_count,0)) as wordNum,
           |    sum(if(publishDate between '$weekStart' and '$date',photo_count,0)) as photoNum,
           |    count(if(publishDate between '$weekStart' and '$date' and contentType='视频',a.id,null)) as videoNum
           |from
           |    (select id,blogid,contenttype,publishdate
           |    from lofter.dim_post
           |    where publishdate<='$date' and isPublished=true
           |    )a
           |    left join
           |    (select * from lofter.dwd_post_length_dd where dt='$date') b
           |    on a.id=b.postId
           |group by blogId
           |) a
           |
           |left join
           |(select c.*,d.favoriteTagUv from
           |(select userId,tag as favoriteTag, post_count_7d as favoriteTagPostNum from
           |    (select userId,tag,post_count_7d, row_number() over(partition by userId order by post_count_7d desc) as rk
           |      from lofter.dws_par_user_tag_create_dd where dt='$date' and post_count_7d>0) aa
           | where rk=1) c
           |left join
           |(select tag,count(distinct userId) as favoriteTagUv from lofter.dws_par_user_tag_create_dd where dt='$date' and post_count_7d>0 group by tag having count(distinct userId) >= 10) d
           |on c.favoriteTag = d.tag
           |)c
           |on a.blogId=c.userId
           |
           |left join
           |(----上周收获热度数,上周收获喜欢数，上周收获转载数，上周收获推荐数
           |select  userid,sum( poshotpv) hotNum,sum(poscommentpv) responseNum,sum(pospraisepv) favoriteNum,
           |    sum(posreproducepv) reblogNum,sum( posrecpv) shareNum,sum(possubscribepv) subscribeNum
           |from  lofter.dws_post_base_stats_di
           |where dt>='$weekStart' and  dt<='$weekEnd'
           |group by userid
           |) d
           |on a.blogid=d.userid
           |left join
           |(select userId,receive_gift_amount_deduct_7d as giftMoney,receive_reward_amount_deduct_7d as rewardMoney
           | from lofter.dws_par_user_revenue_dd
           | where dt='$date' and (receive_gift_amount_deduct_7d>0 or receive_reward_amount_deduct_7d>0)
           |) e
           |on a.blogid=e.userId
           |""".stripMargin

      val sql_user_dun =
        s"""
           |select blogId,count(distinct dunUserId) as dunUv
           |from lofter_db_dump.ods_db_emote_dun_nd
           |where from_unixtime(cast(createTime / 1000 AS BIGINT), 'yyyy-MM-dd') between  '$weekStart' and '$weekEnd'
           |group by blogId
           |""".stripMargin

      val sql_top_postId =
        s"""
           |select blogId,
           |    count(distinct a.postId) as qualityPostNum,
           |    sum(bg_pv_7d) as qualityPostPv
           |from (
           |  select blogId, postId
           |  from lofter.ods_log_traffic_sensing_push_attribution_di
           |  where dt between '$weekStart' and '$weekEnd'
           |  group by blogId, postId
           |) a
           |left join (
           |  select postId, count(userid) bg_pv_7d
           |  from lofter.dwd_post_expose_di
           |  where dt between '$weekStart' and '$weekEnd' and reaction is null
           |  group by postId
           |) b on a.postId = b.postId
           |group by blogId
           |""".stripMargin

      val sql_user_gain_grain =
        s"""
           |select blogId,sum(count) as grainNum
           |from  lofter_db_dump.ods_db_trade_gift_present_record_nd
           |where  giftType=0 and  from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') between '$weekStart' and '$weekEnd'
           |group by blogId
           |""".stripMargin

      val sql_support =
        s"""
           |select blogId,collect_set(named_struct('supporterBlogId',supporterBlogId,'score',score)) as supportBlogInfos
           |from
           |(
           |select blogId,supporterBlogId, sum(score) as score,
           |    row_number() OVER (partition by blogId  ORDER BY  sum(score) desc) rank
           |from lofter_db_dump.ods_db_trade_support_record_nd
           |where from_unixtime(cast(time/1000 as bigint),'yyyy-MM-dd') between '$weekStart' and '$weekEnd' and
           |      score > 0
           |group by blogId,supporterBlogId
           |having score > 0
           |)
           |where rank<=3
           |group by blogId
           |""".stripMargin

      spark.sql(sql_user_post_behavior).createOrReplaceTempView("userPostBehavior")
      spark.sql(sql_user_dun).createOrReplaceTempView("userDun")
      spark.sql(sql_top_postId).createOrReplaceTempView("topPostId")
      spark.sql(sql_user_gain_grain).createOrReplaceTempView("gainGrain")
      spark.sql(sql_support).createOrReplaceTempView("blogSupport")

      val sql_result =
        s"""
           |select a.*,
           |      c.dunUv,
           |      qualityPostNum, qualityPostPv,
           |      e.grainNum,f.supportBlogInfos
           |from userPostBehavior a
           |left join userDun c
           |on a.blogId = c.blogId
           |left join topPostId d
           |on a.blogId=d.blogId
           |left join gainGrain e
           |on a.blogId=e.blogId
           |left join blogSupport f
           |on a.blogId=f.blogId
           |""".stripMargin

      spark.sql(sql_result)
        .withColumn("dt", lit(date))
        .repartition(10)
        .write.mode(SaveMode.Overwrite)
        .insertInto("lofter_dm.ads_user_data_center_wd")

    }

    spark.stop()
  }
}
