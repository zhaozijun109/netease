package com.netease.vc.data.ads

import org.apache.spark.sql.{DataFrame, SparkSession}

object VirtualRankingListOM5280VAll {
  def main(args: Array[String]): Unit = {
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
           |and type = 2
           |and trim(end_time) >= date_sub(CURRENT_DATE(),3)
           |""".stripMargin)
      .selectExpr("id", "type", "target_type", "simulator_id", "pack_id", "trim(start_time) as start_time", "trim(end_time) as end_time", "order_field", "order_type", "sql_clause", "version")

    println(s"榜单数：${vc_rank_config.count()}")

    var resultDFs: List[DataFrame] = List()

    vc_rank_config.collect.foreach(row => {

      println(s"mysql内数据：$row ~")

      val orderTypeValue = Option(row.getAs[Int]("order_type")).getOrElse(0)
      val orderFieldValue = Option(row.getAs[Int]("order_field")).getOrElse(0)
      val startTime = Option(row.getAs[String]("start_time")).getOrElse("")
      val endTime = Option(row.getAs[String]("end_time")).getOrElse("")
      val packId = Option(row.getAs[Long]("pack_id")).getOrElse(0L)
      val rankId = Option(row.getAs[Long]("id")).getOrElse(0L)
      val targetType = Option(row.getAs[Int]("target_type")).getOrElse(0)
      val version = Math.addExact(Math.toIntExact(row.getAs[Long]("version")), 1)
      val sqlClause = Option(row.getAs[String]("sql_clause")).getOrElse("")
      val simulatorId = Option(row.getAs[String]("simulator_id")).getOrElse("")

      println(s"======================处理：${rankId}========================")

      val orderType = orderTypeValue match {
        case 1 => "sum"
        case 2 => "max"
        case _ => "sum" // 默认值
      }

      val orderField = orderFieldValue match {
        case 1 => "gmv"
        case 2 => "talk_cnt"
        case 3 => "energy_consume"
        case _ => "talk_cnt" // 默认值
      }

      val sqlStr = sqlClause.replace("pack_id, target_id, nums, rk, _count",s"$targetType as target_type,$rankId as rank_id,pack_id,target_id,cast(nums as int) as nums,rk,$version as version,_count")
        .replace("${{start_date}}",startTime)
        .replace("${{end_date}}",endTime)
        .replace("${{pack_id}}",packId.toString)
        .replace("${{simulator_id}}",simulatorId)

      println(s"执行sql：$sqlStr")

      val df = try {
        val df1 = spark.sql(sqlStr)
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
      finalResult.write.mode("overwrite").saveAsTable("vc.ads_vc_character_package_rank_virall_a_d")
    }
    spark.stop()
  }

}
