package com.netease.lofter.etl.ods

import com.netease.wm.util.Args
import org.apache.spark.sql.{SaveMode, SparkSession}

object StatisPointImportJob {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val dt = pargs.required("date")

    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val pointsDf = spark.read
      .format("jdbc")
      .option("url", "jdbc:mysql://lofter-rds-yaolu-stat-point-34895.rds.cn-gz-p1.internal.:3306/yaolu_stat_point?useUnicode=true&characterEncoding=UTF-8")
      .option("dbtable", "statis_point")
      .option("user", "lofter_bi_gy")
      .option("password", "@C0wW8Ue_")
      .option("driver","com.mysql.jdbc.Driver")
      .load()

    val outPath = "/user/da_lofter/db_dump/statis_point"
    pointsDf.write.mode(SaveMode.Overwrite)
      .parquet(outPath)

    spark.sql(s"alter table lofter_db_dump.ods_db_statis_point_nd set location '$outPath' ")
    spark.close()
  }
}
