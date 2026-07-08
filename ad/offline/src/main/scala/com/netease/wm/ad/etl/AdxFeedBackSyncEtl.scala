package com.netease.wm.ad.etl

import com.netease.wm.ad.common.dbConfig
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}

import java.sql.{Connection, DriverManager}

object AdxFeedBackSyncEtl {
  case class FeedBack(id: Long, productId: Long, platform: Int, clientVersion: Option[String], adId: Option[String],
                      flightId: Option[String], materialId: Option[String], clickCount: Int, showCount: Int,
                      createTime: Long, updateTime: Long)

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)

    val date = pargs.required("date")
    val dateNum = date.replaceAll("-", "").toInt

    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    import com.netease.wm.util.Sql._
    import spark.implicits._

    def getDB: Connection = {
      Class.forName("com.mysql.jdbc.Driver")
      DriverManager.getConnection(dbConfig.yaoluOnlineJdbcUrl)
    }

    implicit val db: Connection = getDB

    val data = sql"select * from adfeedback_stat_daily where statTime = ${0}".query[FeedBack](param(dateNum))

    val jdbcDF = spark.createDataset(data)

    jdbcDF
      .withColumn("dt", lit(date))
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter.ods_db_ad_feedback_di")

    spark.stop()
  }
}
