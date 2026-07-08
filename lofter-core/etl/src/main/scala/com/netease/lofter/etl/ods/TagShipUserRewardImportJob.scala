package com.netease.lofter.etl.ods

import com.netease.wm.util.Args
import org.apache.spark.sql.{SaveMode, SparkSession}

object TagShipUserRewardImportJob {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val dt = pargs.required("date")

    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val outPath = s"/user/da_lofter/db_dump/act_TagShip_UserReward/$dt"
    spark.sql(s"alter table lofter_db_dump.ods_db_act_tag_ship_user_reward_nd set location '$outPath' ")

    val sourceDf = spark.read
      .format("jdbc")
      .option("url", "jdbc:mysql://lofter-rds-activity-online-jd-34731.rds.cn-gz-p1.internal.:3306/lofter_activity?useUnicode=true&characterEncoding=UTF-8")
      .option("dbtable", "TagShip_UserReward")
      .option("user", "lofter_bi_gy")
      .option("password", "NO@b7Q_a9")
      .option("driver","com.mysql.jdbc.Driver")
      .load()

    sourceDf.write.mode(SaveMode.Overwrite)
      .insertInto("lofter_db_dump.ods_db_act_tag_ship_user_reward_nd")

    spark.stop()
  }
}
