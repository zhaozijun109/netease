package com.netease.lofter.etl.ads

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{SaveMode, SparkSession}

object FansVipInvitationStats {

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Fans Vip Invitation Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .enableHiveSupport()
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))
    val oneMonthAgo = DateTime.parse(date).minusDays(29).toString("yyyy-MM-dd")

    val sql_1 =
      s"""
         |select a.blogid,a.hot,a.coin,b.postnum as post_num from
         |(select a.blogid,sum(c.hot) as hot,sum(d.coin) as coin
         |from
         |    (select id,blogid,ip
         |    from lofter.dim_post
         |    LATERAL VIEW explode(ips) t2 AS ip
         |    group by id,blogid,ip) a
         |    join
         |    (select ip from lofter.zq_lofter_fans_vip_target_ip) b
         |    on a.ip=b.ip
         |    left join
         |    (select postid,blogid,
         |    (favoritecount+reblogcount+sharecount+subscribecount) hot
         |    from lofter_db_dump.ods_db_post_count_nd) c
         |    on a.id=c.postid
         |    left join
         |    (select postid,blogid,sum(coin) as coin
         |    from lofter.dwd_paid_post_detail_dd
         |    where dt='$date' group by postid,blogid  ) d
         |    on a.id=d.postid
         |    group by a.blogid
         |) a
         |join
         |(select blogid,count(distinct postid) as postnum
         |from lofter.dim_gift_post_return_dd
         |where dt='$date' and status=1
         |group by blogid) b
         |on a.blogid=b.blogid
         |left join
         |(select userid from lofter_db_dump.ods_db_trade_fans_vip_account_nd  group by userid) c
         |on a.blogid=c.userid
         |where c.userid is null
         |""".stripMargin

    val sql_2 =
      s"""
         |select a.blogId,a.postnum  as post_num,b.publisheruserid,b.hot,c.sender,c.coin
         |from
         |(select blogId,count(distinct postid) as postnum from
         |lofter.dim_gift_post_return_dd
         |where dt='$date' and status=1
         |group by blogId) a
         |left join
         |(select blogid,publisheruserid,count(1) as hot
         |from lofter_db_dump.ods_db_post_hot_nd
         |where from_unixtime(cast(optime / 1000 AS BIGINT), 'yyyy-MM-dd') between '$oneMonthAgo' and '$date'
         |group by blogid,publisheruserid) b
         |on a.blogid=b.blogid
         |left join
         |(select sender,blogid,sum(coin) as coin
         |from lofter.dwd_paid_post_detail_dd
         |where dt='$date' and from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') between '$oneMonthAgo' and '$date'
         |group by blogid,sender ) c
         |on a.blogid=c.blogid
         |left join
         |(select userid from lofter_db_dump.ods_db_trade_fans_vip_account_nd  group by userid) d
         |on a.blogid=d.userid
         |where d.userid is null
         |""".stripMargin

    spark.sql(sql_1).createOrReplaceTempView("rule1")
    spark.sql(sql_2).createOrReplaceTempView("rule2")

    val sql_result =
      s"""
         |select blogId
         |from rule1
         |where post_num >= 5 and (hot >= 50000 or coin >= 10000)
         |
         |union
         |select blogId
         |from
         |(select blogId,count(distinct publisheruserid) as hot_uv
         | from rule2
         | where post_num >= 5 and hot >= 10
         | group by blogId
         | having hot_uv >= 20) a
         |
         |union
         |select blogId
         |from
         |(select blogId,count(distinct sender) as sender_uv
         | from rule2
         | where post_num >= 5 and coin >= 30
         | group by blogId
         | having sender_uv >= 20) a
         |""".stripMargin

    spark.sql(sql_result)
      .withColumn("dt", lit(date))
      .repartition(1)
      .write
      .mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_fans_vip_invitation_di")

    spark.stop()

  }

}
