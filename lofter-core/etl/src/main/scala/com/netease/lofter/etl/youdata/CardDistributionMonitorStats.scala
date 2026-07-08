package com.netease.lofter.etl.youdata

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.lit

object CardDistributionMonitorStats {
  def main(args: Array[String]): Unit = {
    val params = Args(args)
    val spark = SparkSession.builder()
      .appName("lofter Card Distribution stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val yesterday = DateTime.yesterday().toString("yyyy-MM-dd")
    val date = params.optional("date").getOrElse(yesterday)
    val cardStartDate = "2022-04-29"

    val sql_card_name =
      s"""
         |select distinct a.activitycode,
         |                b.name,
         |                case when b.slotType=4 then '一番赏' else '抽赏' end as activity_code_type
         |from  lofter_db_dump.ods_db_benefit_user_card_bag_nd a
         |left join
         |lofter_db_dump.ods_db_benefit_card_activity_nd b
         |on a.activitycode=b.code and a.createTime between b.startTime and b.endTime
         |""".stripMargin

    val sql_user_card =
      s"""
         |select userid,
         |       activitycode,
         |       case when b.slotType=4 then '一番赏' else '抽赏' end as activity_code_type,
         |       count(id) as card_cnt,
         |       count(distinct attributeid) as card_dis_cnt
         |from (
         |  select userid,activitycode,id,attributeid,createtime from lofter_db_dump.ods_db_benefit_user_card_bag_nd where status in(0,1,2)
         |) a
         |left join (
         |  select code, slotType, startTime, endTime
         |  from lofter_db_dump.ods_db_benefit_card_activity_nd
         |) b
         |on a.activitycode=b.code and a.createTime between b.startTime and b.endTime
         |group by userid,
         |         activitycode,
         |         case when b.slotType=4 then '一番赏' else '抽赏' end
         |""".stripMargin

    val sql_card_money =
      s"""
         |select thirdpartynum as activitycode,
         |       case when b.slotType=4 then '一番赏' else '抽赏' end as activity_code_type,
         |       buyerid as userid,
         |       from_unixtime(cast(a.createTime/1000 as bigint),'yyyy-MM-dd') as createDate,
         |       cast(round(sum(amount),0) as bigint) as money
         |from (
         |  select thirdpartynum,buyerid,createTime,amount
         |  from lofter_db_dump.ods_db_benefit_trade_nd
         |  where from_unixtime(cast(createtime/1000 as bigint),'yyyy-MM-dd') between '$cardStartDate' and '$date'
         |  and productType =5 and status in (1) and length(thirdpartynum)>0
         |) a
         |left join (
         |  select code, slotType, startTime, endTime
         |  from lofter_db_dump.ods_db_benefit_card_activity_nd
         |) b
         |on a.thirdpartynum=b.code and a.createTime between b.startTime and b.endTime
         |group by thirdpartynum,
         |         case when b.slotType=4 then '一番赏' else '抽赏' end,
         |         buyerid,
         |         from_unixtime(cast(a.createTime/1000 as bigint),'yyyy-MM-dd')
         |""".stripMargin

    spark.sql(sql_card_name).createOrReplaceTempView("cardName")
    spark.sql(sql_user_card).createOrReplaceTempView("userCard")
    spark.sql(sql_card_money).createOrReplaceTempView("cardMoney")

    val sql_result =
      s"""
         |select a.activitycode as activity_code,b.name,stat_type,num,uv,a.activity_code_type
         |from
         |(
         |  select activitycode,activity_code_type,'card_num' as stat_type,card_cnt as num,count(distinct userid) as uv
         |  from userCard
         |  group by 1,2,3,4
         |
         |  union all
         |  select activitycode,activity_code_type,'card_dis_num' as stat_type,card_dis_cnt as num,count(distinct userid) as uv
         |  from userCard
         |  group by 1,2,3,4
         |
         |  union all
         |  select activitycode,activity_code_type,'money' as stat_type,money as num,count(distinct userid) as uv
         |  from (select activitycode,activity_code_type,userid,sum(money) as money from cardMoney group by 1,2,3) a
         |  group by 1,2,3,4
         |
         |  union all
         |  select  activitycode,activity_code_type,'re_buy' as stat_type,
         |    count(case when buy_num>=2 then userid else null end) as num,
         |    count(distinct userid) as uv
         |  from (select activitycode,activity_code_type,userid,count(distinct createDate) as buy_num from cardMoney group by 1,2,3) a
         |  group by 1,2,3
         |
         |    union all
         |  select  activitycode,activity_code_type,'real_buy' as stat_type,
         |    sum(money) as num,
         |    count(distinct userid) as uv
         |  from  cardMoney
         |  where money>0
         |  group by 1,2,3
         |) a
         |left join
         |cardName  b
         |on a.activitycode=b.activitycode
         |""".stripMargin

    spark.sql(sql_result)
      .withColumn("dt", lit(date))
      .repartition(1)
      .write
      .mode("overwrite")
      .insertInto("lofter_dm.ads_card_distribution_monitor_dd")

    spark.close()
  }
}
