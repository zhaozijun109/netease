package com.netease.vc.data.ads

import org.apache.spark.sql.{DataFrame, SparkSession}

object VirtualRankingListOM5280CT {
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
           |and order_type in (1,2) and order_field in (1,2,3)
           |and type = 1 and target_type = 1
           |""".stripMargin)
      .selectExpr("id", "type", "target_type", "simulator_id", "pack_id", "trim(start_time) as start_time", "trim(end_time) as end_time", "order_field", "order_type", "sql_clause", "version")

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
      val version = Option(row.getAs[Long]("version")).getOrElse(0)

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

      val sqlStr =
        s"""
           |select $targetType as target_type,$rankId as rank_id,pack_id, character_id as target_id, nums, rk, $version + 1 as version,
           |count(*) over (partition by pack_id) as _count
           |from (select pack_id, character_id, nums, row_number() over (partition by pack_id order by nums desc) as rk
           |      from (select x2.pack_id, x1.character_id, ${orderType}(${orderField}) as nums
           |            from vc.dws_vc_uc_daily_action_i_d x1
           |                     inner join
           |                 (select character_id, package_id as pack_id
           |                  from ((select package_id,
           |                                explode(characters) as character_id,
           |                                character_type,
           |                                character_nums,
           |                                version,
           |                                dt,
           |                                h
           |                         from vc.dwd_vc_character_package_i_d
           |                         where dt = (select max(dt) from vc.dwd_vc_character_package_i_d)
           |                           and h = (select max(h)
           |                                    from vc.dwd_vc_character_package_i_d
           |                                    where dt = (select max(dt) from vc.dwd_vc_character_package_i_d))
           |                           and package_id = ${packId}))) x2 on x1.character_id = x2.character_id
           |            where dt between '${startTime}' and '${endTime}'
           |            group by x2.pack_id, x1.character_id))
           |where rk <= 100
           |""".stripMargin


      println(sqlStr)

      val df = spark.sql(sqlStr)

      println("===============start=============")
      df.show(10)
      println("===============end=============")

      resultDFs = df :: resultDFs
    })

    val finalResult = if (resultDFs.nonEmpty) {
      resultDFs.reduce(_ union _)
    } else {
      spark.emptyDataFrame
    }

    finalResult.write.mode("overwrite").saveAsTable("vc.ads_vc_character_package_rank_ct_a_d")

    spark.stop()
  }

}
