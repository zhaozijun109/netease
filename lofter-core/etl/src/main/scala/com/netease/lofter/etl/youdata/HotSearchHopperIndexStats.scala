package com.netease.lofter.etl.youdata

import com.github.nscala_time.time.Imports
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}

object HotSearchHopperIndexStats {
  val batchSize = 100

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Search Index Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(Imports.DateTime.yesterday.toString("yyyy-MM-dd"))
    val yesterday = Imports.DateTime.yesterday.toString("yyyy-MM-dd")

    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")
    hotSearchHopperIndexStats(spark, date)

    spark.close()

  }

  def hotSearchHopperIndexStats(spark: SparkSession, date: String): Unit = {
    val oneMonthAgo = Imports.DateTime.parse(date).minusDays(29).toString("yyyy-MM-dd")

    val sql_exclude_device =
      s"""
         |select deviceUdid
         |from lofter.dwd_growth_actpwd_access_di
         |where dt between $oneMonthAgo and $date
         |group by deviceUdid
         |""".stripMargin

    val sql_mda =
      s"""
         |select deviceos,deviceudid,eventid,occurtime,itemtype,itemid,customudid,params
         |from lofter.ods_mda_app_partition_di
         |where dt='$date' and (eventId in('b5-10','b5-11','b5-12','b5-13','b5-15','b5-26','z1-1','z1-2','z1-3','z1-4','z1-5','g1-8','a2-61')
         |      or eventid like  ('%k1-%') or eventid like  ('%b1-4%'))
         |""".stripMargin

    spark.sql(sql_exclude_device).cache().createOrReplaceTempView("hotSearchExclude")
    spark.sql(sql_mda).createOrReplaceTempView("mda")

    val sql_hot_search =
      s"""
         |select a.* from
         |(select
         |    deviceos, deviceudid,occurtime,customudid,itemtype
         |from
         |    mda
         |where eventid='b5-11' and params['refer']='hotsearch'
         |) a
         |left join
         |hotSearchExclude b
         |on a.deviceudid=b.deviceUdid
         |where b.deviceUdid is null
         |""".stripMargin

    val sql_hot_search_path_1 =
      s"""
         |select a.* from
         |(select
         |    deviceudid,eventid,occurtime,itemtype,deviceos,itemid,
         |    case when params['text'] is not null then params['text'] else params['queryname'] end as text1,
         |    params['ruletype'] as ruletype
         |from
         |    mda
         |where
         |eventid in ('b5-12','b5-13','z1-1','z1-2','z1-3','z1-4','z1-5') or
         |eventid like  ('%k1-%') or
         |eventid like  ('%b1-4%') or (eventid in('b5-15') and params['type']=2 )
         |) a
         |left join
         |hotSearchExclude b
         |on a.deviceudid=b.deviceUdid
         |where b.deviceUdid is null
         |""".stripMargin

    val sql_common =
      s"""
         |select * from
         |   (select
         |        a.deviceos,a.customudid, b.eventid,a.deviceudid,a.occurtime as time1,b.occurtime as time2,b.itemtype,b.itemid,b.text1,ruletype,
         |        row_number() OVER(partition by  a.customudid,a.deviceudid,a.occurtime order by b.occurtime  asc) as rk
         |    from
         |        t1 a
         |    join
         |        t2 b
         |    on a.deviceudid=b.deviceudid and b.occurtime >= a.occurtime and b.occurtime<= (a.occurtime+30000)
         |     ) c
         |where rk=1
         |""".stripMargin

    spark.sql(sql_hot_search).createOrReplaceTempView("t1")
    spark.sql(sql_hot_search_path_1).createOrReplaceTempView("t2")
    spark.sql(sql_common).createOrReplaceTempView("t3")

    val sql_res1 =
      s"""
         |select
         |    '$date' as dt,deviceos,'主动搜索' as statType, count(distinct deviceudid) as uv, count(deviceudid) as pv
         |from
         |    t1
         |group by deviceos
         |""".stripMargin

    val sql_res2 =
      s"""
         |select
         |    '$date' as dt,deviceos,
         |    case when eventid in ('b5-12') then '搜索联想词'
         |         when eventid in ('b5-13') then '点击搜索按钮'
         |         when eventid like  ('%k1-%') then '删除搜索词'
         |         when eventid like  ('%b1-4%') or  eventid in('b5-15')  then '点击取消' end as statType,
         |    count(distinct deviceudid) as uv, count(deviceudid) as pv
         |from
         |    t3
         |group by deviceos,
         |     case when eventid in ('b5-12') then '搜索联想词'
         |          when eventid in ('b5-13') then '点击搜索按钮'
         |          when eventid like  ('%k1-%') then '删除搜索词'
         |          when eventid like  ('%b1-4%') or  eventid in('b5-15')  then '点击取消' end
         |""".stripMargin

    val sql_res3 =
      s"""
         |select
         |    '$date' as dt,deviceos,
         |    case when itemtype is null and  eventid='b5-12' and ruletype is null then '点击文本联想词'
         |          else '点击用户联想词' end as statType,
         |    count(distinct deviceudid) as uv, count(deviceudid) as pv
         |from
         |    t3
         |where eventid='b5-12'
         |group by deviceos,
         |    case when itemtype is null and  eventid='b5-12'  and ruletype is null  then '点击文本联想词'
         |         else '点击用户联想词' end
         |""".stripMargin

    val sql_res4_return =
      s"""
         |SELECT
         |    '$date' as dt,deviceos,'点击返回' as statType,count(DISTINCT a.deviceudid) as uv,count(a.deviceudid) as pv
         |FROM (
         |	SELECT
         |		 a.deviceos,a.deviceudid,a.time1,b.occurtime AS time2,
         |		 row_number() OVER (
         |		 PARTITION BY a.deviceudid,a.time1 ORDER BY b.occurtime ASC ) AS rk
         |	FROM
         |      (select distinct time2 as time1,itemid,deviceudid,deviceos
         |       from t3 where eventid='b5-12' and itemtype='BLOG') a
         |  JOIN
         |      (select aa.* from
         |        (SELECT
         |            distinct deviceudid,eventid,occurtime,itemtype,itemid
         |	      FROM mda
         |	      WHERE  eventid = 'b5-11'
         |	      		AND params ['refer'] IS NULL
         |         ) aa
         |        left join
         |        hotSearchExclude bb
         |        on aa.deviceudid=bb.deviceUdid
         |        where bb.deviceUdid is null
         |	      ) b
         |    ON a.deviceudid = b.deviceudid
         |		AND b.occurtime >= a.time1
         |		AND b.occurtime <= (a.time1 + 10000)
         |) a
         |where rk=1
         |group by deviceos
         |""".stripMargin

    val sql_res5_article =
      s"""
         |SELECT
         |    '$date' as dt,deviceos,'点击文章' as statType,count(DISTINCT d.deviceudid) as uv,count(d.deviceudid) as pv
         |FROM (
         |	SELECT
         |		 a.deviceos,a.deviceudid,a.time1,c.time2,
         |		 row_number() OVER (
         |		 PARTITION BY a.deviceudid,a.time1 ORDER BY c.time2 ASC ) AS rk
         |	FROM
         |      (select distinct time2 as time1,itemid, deviceudid, deviceos
         |       from t3 where eventid='b5-12' and itemtype='BLOG') a
         |  JOIN
         |      (	SELECT
         |            a.deviceudid,a.eventid,a.occurtime as time2,a.postid,b.blogid
         |	      FROM
         |            (select aa.* from
         |              (SELECT distinct deviceudid,eventid,occurtime,
         |                     CASE WHEN eventid='g1-8' THEN params['tid'] ELSE itemid end as postid
         |              FROM mda
         |	            WHERE  eventid IN ('g1-8','a2-61') ) aa
         |             left join
         |              hotSearchExclude bb
         |              on aa.deviceudid=bb.deviceUdid
         |              where bb.deviceUdid is null
         |	           ) a
         |       JOIN
         |           (SELECT id,blogid from lofter.dim_post) b
         |       ON a.postid = b.id
         |  ) c
         |  ON a.deviceudid=c.deviceudid
         |	   AND c.time2 >= a.time1
         |	   AND c.blogid=a.itemid
         |) d
         |where rk=1
         |group by deviceos
         |""".stripMargin

    // click the text or click the search button to search the result
    val sql_res6_click_text =
      s"""
         |SELECT
         |    '$date' as dt,deviceos,
         |    case when eventid='b5-12'  then '点击联想词搜索结果'
         |         when eventid='b5-13' then '点击搜索按钮搜索结果' end as statType,
         |    count(DISTINCT deviceudid) as uv,count(deviceudid) as pv
         |FROM (
         |	SELECT
         |		 a.text1,a.deviceos,a.deviceudid,a.time1,b.occurtime AS time2,a.eventid,
         |		 row_number() OVER (
         |		 PARTITION BY a.text1,a.deviceudid,a.eventid,a.time1 ORDER BY b.occurtime ASC ) AS rk
         |	FROM
         |      (select distinct time2 as time1,itemid,text1,deviceos,deviceudid,eventid,ruletype
         |       from t3 where (eventid='b5-12' and itemtype is null and ruletype is null) or eventid='b5-13') a
         |  JOIN
         |      (select aa.* from
         |        (SELECT
         |            distinct deviceudid,eventid,occurtime,itemtype,itemid,
         |            case when params['QueryName'] is not null then params['QueryName']
         |                 else params['queryname']  end as tag
         |	      FROM mda
         |	      WHERE  (eventid = 'b5-10' and itemtype is not null) or (eventid = 'b5-26')
         |        ) aa
         |       left join
         |        hotSearchExclude bb
         |        on aa.deviceudid=bb.deviceUdid
         |        where bb.deviceUdid is null
         |	      ) b
         |  ON b.deviceudid = a.deviceudid AND b.occurtime >= a.time1 AND b.tag = a.text1
         |) c
         |where rk=1
         |group by deviceos,
         |    case when eventid='b5-12' then '点击联想词搜索结果'
         |         when eventid='b5-13' then '点击搜索按钮搜索结果' end
         |""".stripMargin

    spark.sql(sql_res1).createOrReplaceTempView("res1")
    spark.sql(sql_res2).createOrReplaceTempView("res2")
    spark.sql(sql_res3).createOrReplaceTempView("res3")
    spark.sql(sql_res4_return).createOrReplaceTempView("res4")
    spark.sql(sql_res5_article).createOrReplaceTempView("res5")
    spark.sql(sql_res6_click_text).createOrReplaceTempView("res6")

    val sql_res_all =
      s"""
         |select * from res1 where statType is not null
         |union all
         |select * from res2 where statType is not null
         |union all
         |select * from res3 where statType is not null
         |union all
         |select * from res4 where statType is not null
         |union all
         |select * from res5 where statType is not null
         |union all
         |select * from res6 where statType is not null
         |""".stripMargin

    spark.sql(sql_res_all)
      .drop("dt")
      .withColumn("dt", lit(date))
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_search_hopper_di")
  }

}
