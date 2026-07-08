package com.netease.lofter.etl.youdata

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

object TradeCoinChargeStats {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Trade Coin Charge Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))

    val sql_trade_coin_charge =
      s"""
         |select aa.dt,aa.paytype,
         |       sum(chargeUv) as chargeUv,sum(chargeMoney) as chargeMoney,
         |       sum(newChargeUv) as newChargeUv, sum(newChargeMoney) as newChargeMoney
         |from
         |   (select '$date' as dt,paytype,count(distinct userid) as chargeUv,sum(amount) as chargeMoney
         |    from
         |    lofter_db_dump.ods_db_trade_buy_coin_order_nd
         |    where status=1
         |    and from_unixtime(cast(createtime/1000 as bigint),'yyyy-MM-dd')='$date'
         |    group by paytype
         |    ) aa
         |
         |left join
         |  (
         |     select '$date' as dt,b.paytype,count(distinct a.userid) as newChargeUv,sum(b.amount) as newChargeMoney
         |     from
         |      (
         |	      select userid,min(createtime) as first_pay_time
         |	      from
         |	      lofter_db_dump.ods_db_trade_buy_coin_order_nd
         |	      where status=1
         |	      group by userid
         |       having from_unixtime(cast(first_pay_time/1000 as bigint),'yyyy-MM-dd')='$date'
         |       ) a
         |
         |      inner join
         |      ( select userid,paytype,amount
         |        from
         |        lofter_db_dump.ods_db_trade_buy_coin_order_nd
         |        where status=1
         |        and from_unixtime(cast(createtime/1000 as bigint),'yyyy-MM-dd')='$date'
         |        ) b
         |      on a.userid=b.userid
         |      group by b.paytype
         |  ) bb
         |on aa.dt=bb.dt and aa.paytype=bb.paytype
         |group by aa.dt,aa.paytype
         |grouping sets((aa.dt,aa.paytype),(aa.dt))
         |""".stripMargin

    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")

    spark.sql(sql_trade_coin_charge)
      .select("paytype","chargeUv", "chargeMoney", "newChargeUv", "newChargeMoney", "dt")
      .write
      .mode("overwrite")
      .insertInto("lofter_dm.ads_live_charge_stats_di")

    spark.close()
  }

}
