package com.netease.lofter.etl.ads

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}

object PushableUserListJob {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val dt = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))
    val twoMonthAgo = DateTime.parse(dt).minusMonths(1).toString("yyyy-MM-dd")

    val sql =
      s"""
         |select a.userId, c.life_cycle_type as lifeCircle
         |from (
         |    select userId
         |    from lofter.device_active lateral view explode(userIds) t1 as userId
         |    where dt > '$twoMonthAgo' and dt <= '$dt'
         |    group by userId
         |) a join (
         |    select userId
         |    from lofter.dws_par_user_push_dd
         |    where dt ='$dt' and from_unixtime(cast(update_time/1000 as bigint), 'yyyy-MM-dd') >= '$twoMonthAgo' and
         |          is_push_on > 0
         |) b on a.userId = b.userId
         |join (
         |   select userId, life_cycle_type
         |   from lofter.dws_user_life_circle_judge_dd
         |   where dt = '$dt'
         |) c on a.userId = c.userId
         |""".stripMargin

    spark.sql(sql)
      .withColumn("dt", lit(dt))
      .repartition(1)
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_rec_pushable_user_di")

    spark.close()
  }
}
