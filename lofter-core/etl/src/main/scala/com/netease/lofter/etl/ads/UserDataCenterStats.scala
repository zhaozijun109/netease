package com.netease.lofter.etl.ads

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}

object UserDataCenterStats {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter User Center Data Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val yesterday = DateTime.yesterday.toString("yyyy-MM-dd")
    val date = pargs.optional("date").getOrElse(yesterday)
    val yearAgo = DateTime.parse(date).minusYears(1).toString("yyyy-MM-dd")

    val sql_user_gift_reward =
      s"""
         |select a.*,giftTopPostId,giftTopMoney,rewardTopPostId,rewardTopMoney,grainNum
         |from (
         |   ---礼物收益，-打赏收入
         |   select userId as blogid ,receive_gift_amount_deduct_std as giftMoney,receive_reward_amount_deduct_std as rewardMoney
         |   from
         |   lofter.dws_par_user_revenue_dd
         |   where dt='$date'
         |) a
         |
         |left join (
         |    ---付费礼物相关 每日----礼物Top1文章ID----礼物Top1文章收入
         |    select  *
         |    from (
         |        select blogid,postid giftTopPostId ,sum(cast(giftMoney as decimal(10,2))) as giftTopMoney,
         |               row_number() OVER (partition by blogid ORDER BY sum(cast(giftMoney as decimal(10,2)))  desc) rank
         |        from (
         |            select a.*,
         |                   (case when from_unixtime(cast(createTime/1000 as bigint),'yyyy-MM-dd')<'2023-01-01' then 0.95
         |                         when from_unixtime(cast(createTime/1000 as bigint),'yyyy-MM-dd')<'2024-01-01' then 0.8
         |                         else 0.75 end) * 0.1 * coin as giftMoney
         |            from lofter_db_dump.ods_db_trade_gift_present_record_nd a
         |            join (
         |                select id from lofter.dim_post where valid = 0 and allowview in (0,100)
         |            ) b on a.postid=b.id
         |        ) t
         |        where giftType=1
         |        group by blogid, postid
         |    ) tt
         |    where  rank=1
         |) b on a.blogid=b.blogid
         |
         |left join (
         |    ---打赏----打赏Top1文章ID----打赏Top1文章收入
         |    select  *
         |    from (
         |        select authorblogid blogid,authorpostid rewardTopPostId,sum(rewardincomeamount) rewardTopMoney,
         |               row_number() OVER (partition by authorblogid ORDER BY  sum(rewardincomeamount) desc) rank
         |        from (
         |            select a.*
         |            from lofter_db_dump.ods_db_trade_reward_order_nd a
         |            join (
         |                select id from lofter.dim_post where  valid=0  and  allowview in (0,100)
         |            ) b on a.authorpostid = b.id
         |        ) t
         |        where status=10 and authorpostid<>0
         |        group by authorblogid,authorpostid
         |    ) tt
         |    where  rank=1
         |) d on a.blogid=d.blogid
         |
         |left join (
         |    -----用户收到的粮票总数量
         |    select aa.blogid,sum(count) as grainNum
         |    from (
         |        select blogid,createTime,count
         |        from lofter_db_dump.ods_db_trade_gift_present_record_nd
         |        where giftType=0
         |    ) aa
         |    join (
         |        select userid,agreetime from  lofter_db_dump.ods_db_trade_gift_account_nd
         |        where status=2
         |        group by  userid,agreetime
         |    ) bb on aa.blogid=bb.userid and aa.createtime>=bb.agreetime
         |    group by blogid
         |) e on a.blogid=e.blogid
         |""".stripMargin

    val sql_user_post_behavior =
      s"""
         |select a.*,
         |    b.tagNum,c.favoriteTag,favoriteTagPostNum,favoriteTagUv,
         |    e.commentNum
         |from
         |(select blogId,sum(words_count) as wordNum,sum(photo_count) as photoNum,
         |    count(distinct case when contentType='视频' then a.id end) as videoNum,
         |    count(case when publishDate between '$yearAgo' and '$date' and contentType<>'问答' then id end )/12 as postNumAvg
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
         |(select userId,count(tag) as tagNum from lofter.dws_par_user_tag_create_dd where dt='$date' group by userId) b
         |on a.blogId = b.userId
         |
         |left join
         |(select c.*,d.favoriteTagUv from
         |(select userId,tag as favoriteTag, post_count_acc as favoriteTagPostNum from
         |    (select userId,tag,post_count_acc, row_number() over(partition by userId order by post_count_acc desc) as rk
         |      from lofter.dws_par_user_tag_create_dd where dt='$date') aa
         | where rk=1) c
         |left join
         |(select tag,count(distinct userId) as favoriteTagUv from lofter.dws_par_user_tag_create_dd where dt='$date' group by tag) d
         |on c.favoriteTag = d.tag
         |)c
         |on a.blogId=c.userId
         |
         |left join
         |(select userId,receive_comment_cnt as commentNum from lofter.dws_par_creator_interaction_dd where dt='$date') e
         |on a.blogId = e.userId
         |""".stripMargin

    val sql_user_dun =
      s"""
         |select blogId,count(distinct dunUserId) as dunUv
         |from lofter_db_dump.ods_db_emote_dun_nd
         |group by blogId
         |""".stripMargin

    spark.sql(sql_user_gift_reward).createOrReplaceTempView("userReward")
    spark.sql(sql_user_post_behavior).createOrReplaceTempView("userPostBehavior")
    spark.sql(sql_user_dun).createOrReplaceTempView("userDun")

    val sql_result =
      s"""
         |select a.*,
         |      b.giftMoney,rewardMoney,giftTopPostId,giftTopMoney,rewardTopPostId,rewardTopMoney,grainNum,
         |      c.dunUv
         |from userPostBehavior a
         |left join userReward b
         |on a.blogId = b.blogId
         |left join userDun c
         |on a.blogId = c.blogId
         |""".stripMargin

    spark.sql(sql_result)
      .withColumn("dt", lit(date))
      .repartition(10)
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_user_data_center_dd")

    spark.stop()
  }
}
