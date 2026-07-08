package com.netease.lofter.etl.dwd

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}

/**
 * compute user retention for period 1, 2, 6, 14, 29
 */
object UserRetentionEtlJob {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)

    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.required("date")
    val endDate = DateTime.parse(date)

    def userRetentionForPeriod(period: Int): Unit = {
      val periodDate = endDate.minusDays(period).toString("yyyy-MM-dd")
      val returnDeviceSql =
        s"""
           |select '$periodDate' as baseDate, a.userId
           |from (select accountId as userId from lofter.dwd_evt_user_login_di where dt='$periodDate' group by accountId) a
           |    join (select accountId as userId from lofter.dwd_evt_user_login_di where dt='$date' group by accountId) b on a.userId = b.userId
           |""".stripMargin

      spark.sql(returnDeviceSql)
        .repartition(1)
        .withColumn("dt", lit(date))
        .withColumn("period", lit(period))
        .write.mode(SaveMode.Overwrite)
        .insertInto("lofter.dwd_user_retention_di")
    }

    Seq(1, 2, 6, 14, 29).foreach(userRetentionForPeriod _)

    spark.close()
  }
}
