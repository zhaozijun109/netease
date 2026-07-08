package com.netease.lofter.etl.ads

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.lit

object BookstoreHotListsStats {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Bookstore Hot List Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))
    val last2Day = DateTime.parse(date).minusDays(2).toString("yyyy-MM-dd")

    val sql_hot_post =
      s"""
         |select postId, blogId, all_devices as value, rk
         |from (
         |    select a.postId,a.blogId,all_devices,
         |           row_number() over(order by all_devices desc,actpwd_trade_devices desc) as rk
         |   from (
         |       select postId,blogId,
         |              sum(all_devices) as all_devices,
         |              sum(actpwd_trade_devices) as actpwd_trade_devices
         |       from lofter_dm.ads_ec_paid_subscribe_actpwd_funnel_di
         |       where  dt between '$last2Day' and '$date'
         |       group by postId,blogId
         |       having actpwd_trade_devices/all_devices>=0.1
         |    ) a
         |    join (
         |      select distinct postId,blogId from lofter.dim_bookstore_post_dd where dt='$date' and bookstore_content_status=0
         |    ) b on a.postId=b.postId
         |    left join (
         |        select postId
         |        from lofter_db_dump.ods_db_store_rank_list_nd
         |        where status = -1
         |        group by postId
         |    ) c on a.postId = c.postId
         |    where c.postId is null
         |) t
         |where rk <= 100
         |""".stripMargin

    val sql_hot_increase =
      s"""
         |select a.*
         |from (
         |    select a.postId,blogId,
         |         (b.posHotPv-c.posHotPv) as value,
         |         row_number() over(order by b.posHotPv-c.posHotPv desc,publishdate desc) as rk
         |    from (
         |        select distinct postId,blogId
         |        from lofter.dim_bookstore_post_dd
         |        where dt='$date' and bookstore_content_status=0
         |    ) a
         |    join (
         |        select postId,publishdate,posHotPv from lofter.dws_post_base_stats_di where dt='$date'
         |    ) b on a.postId=b.postId
         |    left join (
         |        select postId,posHotPv
         |        from lofter.dws_post_base_stats_di
         |        where dt=date_sub('$date',1)
         |    ) c on a.postId=c.postId
         |    left join (
         |        select postId
         |        from lofter_db_dump.ods_db_store_rank_list_nd
         |        where status = -1
         |        group by postId
         |    ) d on a.postId = d.postId
         |    where d.postId is null
         |) a
         |where rk <= 100
         |""".stripMargin
    
    val sql_buy_vip =
      s"""
         |select a2.*
         |from (
         |    select 0 as postId,blogId,pv as value, row_number() over(order by pv desc) as rk
         |    from (
         |        select a.blogId,count(distinct a.postId,a.userId) as pv
         |        from (
         |            select a.userId,b.postId,b.blogId
         |            from (
         |                select userId,vipblogId,from_unixtime(cast(finishTime / 1000 AS bigint), 'yyyy-MM-dd') as startDay,
         |                       date_add(from_unixtime(cast(finishTime / 1000 AS bigint), 'yyyy-MM-dd') ,cast(vipdays-1 as int)) as endDay
         |                from lofter_db_dump.ods_db_trade_fans_vip_order_nd
         |                where status=1 and
         |                      '$date' between from_unixtime(cast(finishTime / 1000 AS bigint), 'yyyy-MM-dd') and date_add(from_unixtime(cast(finishTime / 1000 AS bigint), 'yyyy-MM-dd') ,cast(vipdays-1 as int))
         |            ) a
         |            join (
         |                select distinct postId,blogId from  lofter.dim_bookstore_post_dd where dt='$date' and bookstore_content_status = 0
         |            ) b on a.vipblogId=b.blogId
         |            join (
         |                select userId,itemId,dt
         |                from lofter.ods_mda_app_partition_di
         |                where dt='$date' and actionType = 'page_view' and eventId='g1-8'  group by userId,itemId,dt
         |            ) c on a.userId=c.userId and b.postId=c.itemId
         |            group by a.userId,b.postId,b.blogId
         |          union all
         |            select a.userId,c.postId,c.blogId
         |            from (
         |                select userId,from_unixtime(cast(finishTime / 1000 AS BIGINT), 'yyyy-MM-dd') as startDay,
         |                       date_add(from_unixtime(cast(finishTime / 1000 AS BIGINT), 'yyyy-MM-dd') ,cast(vipdays-1 as int)) as endDay
         |                from lofter_db_dump.ods_db_trade_store_vip_order_nd
         |                where status=1 and
         |                      '$date' between from_unixtime(cast(finishTime / 1000 AS bigint), 'yyyy-MM-dd') and date_add(from_unixtime(cast(finishTime / 1000 AS bigint), 'yyyy-MM-dd') ,cast(vipdays-1 as int))
         |            ) a
         |            join (
         |                select userId,itemId,dt from lofter.ods_mda_app_partition_di
         |                where dt='$date' and actionType = 'page_view' and eventId='g1-8'  group by userId,itemId,dt
         |            ) b on a.userId = b.userId
         |            join (
         |                select distinct postId,blogId from lofter.dim_bookstore_post_dd where dt='$date' and bookstore_content_status = 0
         |            ) c on b.itemId = c.postId
         |            group by a.userId,c.postId,c.blogId
         |        ) a
         |        left join (
         |            select blogId
         |            from lofter_db_dump.ods_db_store_rank_list_nd
         |            where status = -1 and rankType = 3
         |            group by blogId
         |        ) b on a.blogId = b.blogId
         |        group by a.blogId
         |    ) a1
         |) a2
         |where rk <= 100
         |""".stripMargin

    spark.sql(sql_hot_post)
      .withColumn("dt", lit(date))
      .withColumn("stat_type", lit(1))
      .write
      .mode("overwrite")
      .insertInto("lofter_dm.ads_bookstore_hot_list_di")

    spark.sql(sql_hot_increase)
      .withColumn("dt", lit(date))
      .withColumn("stat_type", lit(2))
      .write
      .mode("overwrite")
      .insertInto("lofter_dm.ads_bookstore_hot_list_di")

    spark.sql(sql_buy_vip)
      .withColumn("dt", lit(date))
      .withColumn("stat_type", lit(3))
      .write
      .mode("overwrite")
      .insertInto("lofter_dm.ads_bookstore_hot_list_di")

    spark.close()
  }

}
