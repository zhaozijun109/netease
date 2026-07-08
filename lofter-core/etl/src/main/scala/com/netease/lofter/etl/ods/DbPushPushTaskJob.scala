package com.netease.lofter.etl.ods

import com.netease.wm.util.Args
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.joda.time.DateTime

object DbPushPushTaskJob {
  def main(args: Array[String]): Unit = {

    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.required("date")
    val threeMonthAgo = DateTime.parse(date).minusMonths(3).toString("yyyy-MM-dd")
    spark.read.parquet(s"/user/da_lofter/hive_db/lofter_db_dump.db/ods_db_push_push_task_dd/dt=$date")
      .createOrReplaceTempView("pushTask")

    val sql_result =
      s"""
         |select * from pushTask
         |where from_unixtime(cast(createtime/1000 as bigint),'yyyy-MM-dd')='$date'
         |union
         |select * from lofter_db_dump.ods_db_push_push_task_nd
         |where from_unixtime(cast(createtime/1000 as bigint),'yyyy-MM-dd') >= '$threeMonthAgo'
         |""".stripMargin

    spark.sql(sql_result).write.mode(SaveMode.Overwrite)
      .parquet(s"/user/da_lofter/hive_db/lofter_db_dump.db/ods_db_push_push_task_temp/")

    spark.read.parquet(s"/user/da_lofter/hive_db/lofter_db_dump.db/ods_db_push_push_task_temp/")
      .write.mode(SaveMode.Overwrite).saveAsTable("lofter_db_dump.ods_db_push_push_task_nd")

    spark.close()
  }

}
