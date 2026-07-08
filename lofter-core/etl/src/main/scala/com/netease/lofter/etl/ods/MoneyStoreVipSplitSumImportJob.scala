package com.netease.lofter.etl.ods

import com.netease.wm.util.Args
import org.apache.spark.sql.{SaveMode, SparkSession}

object MoneyStoreVipSplitSumImportJob {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val dt = pargs.required("date")

    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val outPath = s"/user/da_lofter/db_dump/Money_StoreVipSplitSum/$dt"
    spark.sql(s"alter table lofter_db_dump.ods_db_money_store_vip_split_sum_nd set location '$outPath' ")

    val pointsDf = spark.read
      .format("jdbc")
      .option("url", "jdbc:mysql://10.59.186.164:6000/lofter-mirror-gz?useUnicode=true&characterEncoding=UTF-8")
      .option("dbtable", "Money_StoreVipSplitSum")
      .option("user", "lofter_bi_gy")
      .option("password", "Q8@BJ5wh_")
      .option("driver","com.mysql.jdbc.Driver")
      .load()

    pointsDf.write.mode(SaveMode.Overwrite)
      .insertInto("lofter_db_dump.ods_db_money_store_vip_split_sum_nd")

    spark.close()
  }
}
