package com.netease.lofter.etl.youdata

import java.sql.Connection
import com.github.nscala_time.time.Imports.DateTime
import com.netease.lofter.etl.common.databases
import com.netease.lofter.etl.common.spark.SparkSqlImplicits.rowParam
import com.netease.wm.util.Args
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.sql.functions.lit

object BenefitMainPageConvStats {
  def main(args: Array[String]): Unit = {

    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val yesterday = DateTime.yesterday.toString("yyyy-MM-dd")
    val date = pargs.optional("date").getOrElse(yesterday)

    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")

    mainPageConv(spark, date)
    spark.close()
  }

  def mainPageConv(spark: SparkSession, day: String): Unit = {

    // stat the main page visit user and pv ( eventId should contain z1-4 and w1-1 both)
    val sql_main_page =
      s"""
         |select b.userid,b.deviceos
         |from (
         |    select distinct userid, deviceOs
         |    from lofter.ods_mda_app_partition_di
         |    where dt = '$day' and actionType = 'other' and eventId='z1-4'
         |) a
         |join (
         |    select userid,deviceos  from lofter.dwd_evt_benefit_page_view_di where dt = '$day' and  eventid='w1-1'
         |) b on a.userid=b.userid and a.deviceos=b.deviceos
       """.stripMargin

    spark.sql(sql_main_page).cache().createOrReplaceTempView("mda_main_page")

    val sql_shelf =
      s"""
         |select b.userid,b.deviceos from
         |(select distinct userid,deviceos from mda_main_page) a
         |join
         | (select userid,deviceos from lofter.dwd_evt_benefit_page_view_di where dt = '$day' and eventid='w2-2')  b
         |on a.userid=b.userid and a.deviceos=b.deviceos
       """.stripMargin

    spark.sql(sql_shelf).cache().createOrReplaceTempView("mda_shelf")

    val sql_product =
      s"""
         |select b.userid,b.deviceos,b.productid from
         |(select distinct userid,deviceos from mda_main_page) a
         |join
         | (select userid,deviceos,productid  from lofter.dwd_evt_benefit_page_view_di where dt = '$day' and eventid='w3-2')  b
         |on a.userid=b.userid and a.deviceos=b.deviceos
       """.stripMargin

    val sql_shelf_product =
      s"""
         |select b.userid,b.deviceos,b.productid  from
         |(select distinct userid,deviceos from mda_shelf) a
         |join
         | (select userid,deviceos,productid from lofter.dwd_evt_benefit_page_view_di where dt = '$day' and eventid='w3-2')  b
         |on a.userid=b.userid and a.deviceos=b.deviceos
       """.stripMargin

    spark.sql(sql_product).cache().createOrReplaceTempView("mda_product")
    spark.sql(sql_shelf_product).cache().createOrReplaceTempView("mda_shelf_product")

    val sql_msp_pay =
      s"""
         |select distinct a.deviceos,d.userid,d.id,d.amount  from
         |mda_shelf_product a
         |join
         |(select id,cast(buyerId as bigint) as userid, productid ,orderId,(storePrice*productnum - newCouponPreferential) as amount from lofter_db_dump.ods_db_benefit_order_product_nd where status in (1,3,4)
         |and from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') = '$day'
         |) d
         |on  a.userid=d.userid and a.productid=d.productid
       """.stripMargin

    val sql_mp_pay =
      s"""
         |select distinct a.deviceos,d.userid,d.id,d.amount  from
         |mda_product a
         |join
         |(select id,cast(buyerId as bigint) as userid, productid ,orderId,(storePrice*productnum - newCouponPreferential) as amount from  lofter_db_dump.ods_db_benefit_order_product_nd where status in (1,3,4)
         |and from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') = '$day'
         |) d
         |on  a.userid=d.userid and a.productid=d.productid
       """.stripMargin

    val sql_m_pay =
      s"""
         |select distinct a.deviceos,d.userid,d.id,d.amount  from
         |mda_main_page a
         |join
         |(select id,cast(buyerId as bigint) as userid, productid ,orderId,(storePrice*productnum - newCouponPreferential) as amount from lofter_db_dump.ods_db_benefit_order_product_nd where status in (1,3,4)
         |and from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') = '$day'
         |) d
         |on  a.userid=d.userid
       """.stripMargin

    spark.sql(sql_msp_pay).createOrReplaceTempView("msp_pay")
    spark.sql(sql_mp_pay).createOrReplaceTempView("mp_pay")
    spark.sql(sql_m_pay).createOrReplaceTempView("m_pay")

    val sql_res =
      s"""
         |select a.deviceos,m_pv,m_uv,m_s_pv,m_s_uv,m_p_pv,m_p_uv,m_s_p_pv,m_s_p_uv,
         |msp_pay_uv,msp_pay_money,msp_pay_num,mp_pay_uv,mp_pay_money,mp_pay_num,m_pay_uv,m_pay_money,m_pay_num from
         |(select deviceos,count(distinct userid) as m_uv,count(1) as m_pv from mda_main_page group by deviceos) a
         |left join
         |(select deviceos,count(distinct userid) as m_s_uv,count(1) as m_s_pv from mda_shelf group by deviceos) b on a.deviceos=b.deviceos
         |left join
         |(select deviceos,count(distinct userid) as m_p_uv,count(1) as m_p_pv from mda_product group by deviceos) c on a.deviceos=c.deviceos
         |left join
         |(select deviceos,count(distinct userid) as m_s_p_uv,count(1) as m_s_p_pv from mda_shelf_product group by deviceos) d on a.deviceos=d.deviceos
         |left join
         |(select deviceos,count(distinct userid) as msp_pay_uv,count(id) as msp_pay_num,sum(amount) as msp_pay_money from msp_pay group by deviceos) e on a.deviceos=e.deviceos
         |left join
         |(select deviceos,count(distinct userid) as mp_pay_uv,count(id) as mp_pay_num,sum(amount) as mp_pay_money from mp_pay group by deviceos) f on a.deviceos=f.deviceos
         |left join
         |(select deviceos,count(distinct userid) as m_pay_uv,count(id) as m_pay_num,sum(amount) as m_pay_money from m_pay group by deviceos) g on a.deviceos=g.deviceos
       """.stripMargin

    spark.sql(sql_res)
      .withColumn("dt", lit(day))
      .repartition(1)
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_ec_market_page_cvr_pt_di")
  }

}
