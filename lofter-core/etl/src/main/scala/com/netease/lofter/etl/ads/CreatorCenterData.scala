package com.netease.lofter.etl.ads

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}

object CreatorCenterData {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Creator Center Data")
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val yesterday = DateTime.yesterday.toString("yyyy-MM-dd")
    val dt = pargs.optional("date").getOrElse(yesterday)

    val creatorGeneral =
      s"""
         |select userId,
         |       favoriteCount,
         |       row_number() over (order by favoriteCount desc) / (sum(if(favoriteCount > 0, 1, 0)) over ()) as favoriteRank,
         |       recommendCount,
         |       row_number() over (order by recommendCount desc) / (sum(if(recommendCount > 0, 1, 0)) over ()) as recommendRank,
         |       reblogCount,
         |       row_number() over (order by reblogCount desc)  / (sum(if(reblogCount > 0, 1, 0)) over ()) as reblogRank,
         |       subscribeCount,
         |       row_number() over (order by subscribeCount desc)  / (sum(if(subscribeCount > 0, 1, 0)) over ()) as subscribeRank,
         |       commentCount,
         |       row_number() over (order by commentCount desc)  / (sum(if(commentCount > 0, 1, 0)) over ()) as commentRank,
         |       postCount,
         |       row_number() over (order by postCount desc)  / (sum(if(postCount > 0, 1, 0)) over ()) as postRank,
         |       hot,
         |       newPostHot,
         |       followCount
         |from (
         |    select coalesce(h.userId, f.userId, p.userId) as userId,
         |           h.favoriteCount, h.recommendCount, h.reblogCount, h.subscribeCount, h.hot,
         |           h.newPostHot, f.followCount, p.postCount, c.commentCount
         |    from (
         |        select post_userid as userId,
         |               sum(if(opType = 'praise', 1, 0)) as favoriteCount,
         |               sum(if(opType = 'recommend', 1, 0)) as recommendCount,
         |               sum(if(opType = 'reproduce', 1, 0)) as reblogCount,
         |               sum(if(opType = 'subscribe', 1, 0)) as subscribeCount,
         |               count(1) as hot,
         |               sum(if(post_publish_date = '$dt', 1, 0)) newPostHot
         |        from lofter.dwd_post_hot_di
         |        where dt = '$dt'
         |        group by post_userid
         |    ) h
         |     full join (
         |    -- 粉丝数只考虑主博客
         |        select blogId as userId, count(1) followCount
         |        from lofter.dwd_blog_follow_di
         |        where dt='$dt'
         |        group by blogId
         |    ) f on h.userId = f.userId
         |     full join (
         |        select userId, count(1) postCount
         |        from lofter.dwd_post_publish_di
         |        where dt='$dt'
         |        group by userId
         |    ) p on p.userId = coalesce(f.userId, h.userId)
         |    full join (
         |      select post_userid as userId, count(1) commentCount
         |      from lofter.dwd_post_response_di
         |      where dt='$dt'
         |      group by post_userid
         |    ) c on c.userId = coalesce(f.userId, h.userId, p.userId)
         |) t
         |""".stripMargin

    val creatorHottestTag =
      s"""
         |select userId, tag, hot, tagHotRankRatio as hotTagRank
         |from (
         |    select  userId, tag, hot, tagHotRank, tagHotTotal, tagHotRank / tagHotTotal as tagHotRankRatio,
         |            row_number() over (partition by userId order by tagHotRank / tagHotTotal desc) ratioRank
         |    from (
         |        select userId, tag, hot,
         |               row_number() over (partition by tag order by hot desc) as tagHotRank,
         |               sum(if(hot > 0, 1, 0)) over (partition by tag) as tagHotTotal
         |        from (
         |            select post_userid as userId, tag, count(1) hot
         |            from lofter.dwd_post_hot_di h lateral view explode(post_tags) pt as tag
         |            where dt='$dt'
         |            group by post_userid, tag
         |        ) t
         |        where hot > 10
         |    ) tt
         |    where tagHotRank * 2 < tagHotTotal
         |) ttt
         |where ratioRank = 1
         |""".stripMargin

    spark.sql(creatorGeneral).createOrReplaceTempView("creator_general")
    spark.sql(creatorHottestTag).createOrReplaceTempView("creator_tag_hot")

    val creatorSql =
      s"""
         |select a.userId as id,
         |       a.userId,
         |       a.favoriteCount, if(a.favoriteRank > 1.0, null, a.favoriteRank) as favoriteRank,
         |       a.recommendCount, if(a.recommendRank > 1.0, null, a.recommendRank) as recommendRank,
         |       a.reblogCount, if(a.reblogRank > 1.0, null, a.reblogRank) as reblogRank,
         |       a.subscribeCount, if(a.subscribeRank > 1.0, null, a.subscribeRank) as subscribeRank,
         |       a.commentCount, if(a.commentRank > 1.0, null, a.commentRank) as commentRank,
         |       a.postCount, if(a.postRank > 1.0, null, a.postRank) as postRank,
         |       a.hot, a.newPostHot, a.followCount as fansCount,
         |       b.tag as hotTag, if(b.hotTagRank > 1.0, null, b.hotTagRank) as hotTagRank
         |from creator_general a
         |     left join creator_tag_hot b on a.userId = b.userId
         |where a.userId is not null
         |""".stripMargin

    spark.sql(creatorSql)
      .withColumn("dt", lit(dt))
      .repartition(10)
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_par_creator_rank_di")

    spark.stop()
  }
}
