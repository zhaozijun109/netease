package com.netease.lofter.etl.ads

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}

object PostViewCountChange {

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Search Post Index Update")
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val dt = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))
    val dayAgo = DateTime.parse(dt).minusDays(1).toString("yyyy-MM-dd")

    spark.read.parquet(s"/user/da_lofter/db_dump/post_view_count/$dayAgo").createOrReplaceTempView("ods_db_post_view_count_yesterday")

    val updateViewSql =
      s"""
         |select m.postId, m.viewCount, n.viewCount as prevViewCount
         |from lofter_db_dump.ods_db_post_view_count_nd m
         |     left join ods_db_post_view_count_yesterday n on m.postId = n.postId
         |where n.postId is null or m.viewCount != n.viewCount
         |""".stripMargin

    spark.sql(updateViewSql).repartition(2)
      .withColumn("dt", lit(dt))
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_post_backend_view_count_change_di")

    spark.close()
  }
}
