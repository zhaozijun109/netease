package com.netease.vc.data.ads

import com.github.nscala_time.time.Imports
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{DataFrame, SparkSession}

object VcCustomRankList {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)

    val dt = pargs.optional("date").getOrElse(Imports.DateTime.now().toString("yyyy-MM-dd"))
    val hour = pargs.optional("hour").getOrElse(Imports.DateTime.now().getHourOfDay)

    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val vc_rank_config = spark.read
      .format("jdbc")
      .option("url", "jdbc:mysql://vcharacter-mysql-rw-online.db.gy.ntes:4331/vcharacter?characterEncoding=utf-8&useSSL=false&autoReconnect=true&useAffectedRows=true&useLegacyDatetimeCode=false&useTimezone=true&serverTimezone=Asia/Shanghai")
      .option("dbtable", "vc_rank_config")
      .option("user", "online_algorithm_r_user")
      .option("password", "_FN68@jDm")
      .option("driver", "com.mysql.jdbc.Driver")
      .load()
      .filter(
        s"""
           |id >= 1000
           |and status = 1
           |and type = 5
           |""".stripMargin)
      .selectExpr("id", "sql_clause")

    println(s"自定义榜单数：${vc_rank_config.count()}")

    var resultDFs: List[DataFrame] = List()

    vc_rank_config.collect.foreach(row => {

      println(s"mysql内数据：$row ~")
      val rankId = Option(row.getAs[Long]("id")).getOrElse(0L)
      val sqlClause = Option(row.getAs[String]("sql_clause")).getOrElse("")

      println(s"======================处理：${rankId}========================")

      val sqlStr = sqlClause

      println(s"执行sql：$sqlStr")

      val df = try {
        val df1 = spark.sql(sqlStr).withColumn("rank_id", lit(rankId))
        println("===============start=============")
        df1.show(10)
        println("===============end=============")
        df1
      } catch {
        case ex: Exception =>
          println(s"错误sql:${sqlStr}")
          spark.emptyDataFrame
      }

      resultDFs = df :: resultDFs
    })

    val finalResult = if (resultDFs.nonEmpty) {
      resultDFs.reduce(_ union _)
    } else {
      spark.emptyDataFrame
    }

    if (!finalResult.isEmpty) {
      finalResult
        .selectExpr("rank_id", "target_id", "targetType", "rank_value", "rank_order")
        .withColumn("dt", lit(dt))
        .withColumn("hour", lit(hour))
        .write
        .mode("overwrite")
        .insertInto("vc_dm.ads_vc_custom_rank_dd")
    }
    spark.stop()
  }

}
