package com.netease.wm.ad

import com.github.nscala_time.time.Imports._
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{ArrayType, StringType}

object AdxInventories {

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Adx Inventories")
      .config("spark.sql.parquet.binaryAsString", true)
      .getOrCreate()

    val yesterday = DateTime.yesterday.toString("yyyy-MM-dd")
    val day = pargs.optional("date").getOrElse(yesterday)
    val previousDay = LocalDate.parse(day).minusDays(1).toString("yyyy-MM-dd")

    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")

    val mdaActions = spark.sql(
      s"""
         |select
         |  lower(deviceOs) os,
         |  appVersion,
         |  case
           |	when eventId = 'a1-27' then 'FEEDS'
           |	when eventId = 'b1-45' then 'EXPLOREFEEDNATIVE'
           |	when eventId = 'b8-2' then 'VIDEOINTERVAL'
           |	when eventId = 'g1-40' then 'SINGLEPOSTPAGE'
           |  when eventId = 'g1-109' then 'REWARDVIDEO'
           |  else COALESCE(params ['adCategory'],params ['category']) ----客户端g3-24埋点错误，将adCategory埋到了category上
         |  end as category,
         |  case
         |    when eventId = 'g1-109' then 10001 ----g1-109埋点未添加adCategory和location，等客户端添加
         |    else params ['location']
         |  end as location,
         |  case
           |	when eventId in ('a1-27','b1-45','b8-2','g1-40') then params ['pos']
           |	else null
         |  end as pos,
         |  deviceudid,userid,itemid
         |from
         |  lofter.ods_mda_app_partition_di
         |where
         |  dt = '$day'
         |  and eventId in ('a1-27','b1-45','b8-2','g3-24','ad-14','g1-40','g1-109')
         |  and length(deviceOs) > 0
         |""".stripMargin)

    val adPostionConfig = spark.sql(
      s"""
         |select
         |  a.positionName,
         |  a.positionId,
         |  a.category,
         |  a.location,
         |  a.putLocation as pos,
         |  a.extJson,
         |  get_json_object(a.extjson, '$$.shieldCrowds') as shieldCrowds,
         |  get_json_object(a.extjson, '$$.deliveryCrowds') as deliveryCrowds
         |from
         |  lofter_db_dump.ods_db_act_adspace_snapshot_nd a
         |  join lofter_db_dump.ods_db_ad_position_nd b on a.positionId = b.id
         |where b.appId = '6ED29071' and
         |  a.date = from_unixtime(unix_timestamp('$day', 'yyyy-MM-dd'), 'yyyyMMdd')
      """.stripMargin)
      .cache()

    val stockMda = mdaActions.filter("pos is null").drop("pos")
    val actionMda = mdaActions.filter("pos is not null").drop("location")
    val adStock = stockMda.join(broadcast(adPostionConfig),Seq("category", "location"))
      .unionAll(
        actionMda.join(broadcast(adPostionConfig),Seq("category", "pos"))
      )

    val shieldCrowds = adPostionConfig
      .filter("shieldCrowds is not null")
      .withColumn("shieldCrowds",from_json(col("shieldCrowds"),ArrayType(StringType)))
      .withColumn("job_id",explode(col("shieldCrowds")))
    val deliveryCrowds = adPostionConfig
      .filter("deliveryCrowds is not null")
      .withColumn("deliveryCrowds",from_json(col("deliveryCrowds"),ArrayType(StringType)))
      .withColumn("job_id",explode(col("deliveryCrowds")))

    val groupUsers = spark.sql(
      s"""
         |select userid,job_id from lofter.dwd_user_group_user_list_di
         |where dt = '$previousDay'
      """.stripMargin)
      .cache()

    val shieldUsers = groupUsers.join(broadcast(shieldCrowds),"job_id")
      .groupBy(col("positionId"),col("userid"))
      .agg(count(col("job_id")).as("shield"))

    val deliveryUsers = groupUsers.join(broadcast(deliveryCrowds),"job_id")
      .groupBy(col("positionId"),col("userid"))
      .agg(count(col("job_id")).as("delivery"))

    val users = shieldUsers.join(deliveryUsers,Seq("positionId","userid"),"full_outer")

    val validStock = adStock.join(users,Seq("userid","positionId"),"left_outer")
      .filter("shieldCrowds is null or shieldCrowds = '' or shield is null or shield <= 0")
      .filter("deliveryCrowds is null or deliveryCrowds = '' or delivery > 0")
      .groupBy("positionId","positionName","location","category","pos","os","appVersion")
      .agg(
        countDistinct("itemid","deviceudid").as("distinctNum"),
        count("*").as("num")
      ).withColumn("stock",
      when(
        col("category").isin("EXPLOREFEEDNATIVE","VIDEOINTERVAL","SINGLEPOSTPAGE"),col("distinctNum")).otherwise(col("num"))
      ).drop("num","distinctNum")

    validStock.createOrReplaceTempView("stocks")

    spark.sql(
      s"""
        |insert overwrite table lofter_dm.dwb_ad_inventories_di partition(dt = '$day')
        |select os, positionName, stock, appVersion,positionid
        |from stocks
        |""".stripMargin)

    val adActions = spark.sql(
      s"""
         |select
         |    lower(os) as os,version as appVersion,
         |    positionid,
         |    dspid,
         |    sum(bgpv) as exposePv,
         |    count(distinct if(bgpv > 0, deviceUdid, null)) exposeUv,
         |    sum(clickpv) clickPv,
         |    count(distinct if(clickpv > 0, deviceUdid, null)) clickUv,
         |    sum(if(bgpv > 0 and length(deviceUdid) > 0,1,0)) validExposePv
         |from lofter.dwd_ad_actions_di
         |where dt = '$day'
         |group by os,positionid,dspid,version
         |""".stripMargin)

//    val distinctCategory = Lists.newArrayList("EXPLOREFEEDNATIVE","VIDEOINTERVAL","SINGLEPOSTPAGE")
    validStock.join(adActions,Seq("os","appVersion","positionid"),"left_outer")
      .repartition(1)
      .cache()
      .createOrReplaceTempView("statistical")

    spark.sql(
      s"""
         |insert overwrite table lofter_dm.dwb_ad_inventories_effect_new_di partition(dt = '$day')
         |select os,
         |positionName,
         |dspid,
         |stock,
         |exposepv,
         |exposeuv,
         |clickpv,
         |clickuv,
         |validexposepv,
         |appVersion,
         |positionid,
         |category,
         |pos
         |from statistical
         |""".stripMargin
    )

    spark.close()
  }
}
