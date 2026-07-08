package com.netease.lofter.etl.youdata

import com.github.nscala_time.time.Imports
import com.netease.wm.util.Args
import org.apache.spark.sql.{SaveMode, SparkSession}

object NegativeFeedbackStats {
  val dateBoundary = "2020-06-25"

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Nagative Feedback Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(Imports.DateTime.yesterday.toString("yyyy-MM-dd"))

    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")

    if ( date < dateBoundary) {
      userNegativeFeedback(spark, date)
    } else {
      postArticleNegativeFeedback(spark, date)
      userNegativeFeedback(spark, date)
    }

    spark.close()

  }

  def postArticleNegativeFeedback(spark: SparkSession, date: String): Unit = {

    val sql_1 =
      s"""
         |select '$date' as dt,
         |count(distinct case when valid=32 and `_bin_old`['valid']=0 then id else null end) delnum,
         |count(distinct case when valid=32 and `_bin_old`['valid']=0 then blogid else null end) deluv,
         |count(distinct case when allowview=100 and `_bin_old`['allowview']=0 then id else null end) privatenum,
         |count(distinct case when allowview=100 and `_bin_old`['allowview']=0 then blogid else null end) privateuv,
         |count(distinct case when valid=25 and `_bin_old`['valid']=0 then id else null end) forbiddennum,
         |count(distinct case when valid=25 and `_bin_old`['valid']=0 then blogid else null end) forbiddenuv
         |from lofter.ods_binlog_post_di
         |where dt='$date' and `_bin_op`=2
         |group by dt
       """.stripMargin

    spark.sql(sql_1)
      .repartition(1)
      .select("delnum","deluv", "privatenum","privateuv","forbiddennum","forbiddenuv","dt")
      .write
      .mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.dwb_post_nega_feed_stat_di")

    }

  def userNegativeFeedback(spark: SparkSession, date: String): Unit  = {
    val sql_user_close =
      s"""
         |select '$date' as dt,
         |      count(distinct case when from_unixtime(cast(createTime/1000 as bigint),'yyyy-MM-dd')='$date' then userid else null end) cancel_uv,
         |      count(distinct case when from_unixtime(cast(db_update_time as bigint),'yyyy-MM-dd')='$date' and status=1 then userid else null end) giveup_canceluv,
         |      count(distinct case when from_unixtime(cast(db_update_time as bigint),'yyyy-MM-dd')='$date' and status=2 then userid else null end) success_canceluv
         |from lofter_db_dump.ods_db_user_close_account_log_nd
         |where status != 3 and
         |    ( from_unixtime(cast(createTime/1000 as bigint),'yyyy-MM-dd')='$date' or
         |      from_unixtime(cast(db_update_time as bigint),'yyyy-MM-dd')='$date' )
         |""".stripMargin

    val sql_user_black =
      s"""
         |select '$date' as dt,
         |count(distinct userid) op_uv,
         |count(distinct blacklistblogid) blacklistblogid_uv
         |from lofter_db_dump.ods_db_blacklist_user_nd
         |where from_unixtime(cast(createTime/1000 as bigint),'yyyy-MM-dd')='$date'
         |""".stripMargin

    spark.sql(sql_user_close).createOrReplaceTempView("t1")
    spark.sql(sql_user_black).createOrReplaceTempView("t2")

    val sql_res =
      s"""
         |select t1.cancel_uv,giveup_canceluv,success_canceluv,op_uv,blacklistblogid_uv,'$date' as dt
         |from t1 left outer join t2 on t1.dt=t2.dt
         |""".stripMargin

    spark.sql(sql_res)
      .repartition(1)
      .write
      .mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.dwb_user_nega_feed_stat_di")

  }

}
