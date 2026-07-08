package com.netease.lofter.etl.ods

import com.netease.wm.util.Args
import org.apache.spark.sql.{SaveMode, SparkSession}

object AdChannelConfigImportJob {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val dt = pargs.required("date")

    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val adConfigDf = spark.read
      .format("jdbc")
      .option("url", "jdbc:mysql://lofter-rds-statis-jd-34893.rds.cn-gz-p1.internal.:3306/comic_statis?useUnicode=true&characterEncoding=UTF-8")
      .option("dbtable", "ad_channel_config")
      .option("user", "lofter_bi")
      .option("password", "qKsbCbRpM")
      .option("driver","com.mysql.jdbc.Driver")
      .load()

    val outPath = s"/user/da_lofter/db_dump/ad_channel_config/$dt"
    adConfigDf.write.mode(SaveMode.Overwrite)
      .parquet(outPath)

    spark.sql(s"alter table lofter_db_dump.ods_db_ad_channel_config_nd set location '$outPath' ")

    spark.stop()
  }
}
