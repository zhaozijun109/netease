package com.netease.lofter.etl.youdata

import com.github.nscala_time.time.Imports
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

object mdaLogRecover {

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("MDA Result Recover")
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(Imports.DateTime.yesterday.toString("yyyy-MM-dd"))
    val last1_day = Imports.DateTime.parse(date).minusDays(1).toString("yyyy-MM-dd")
    //val last2_day = Imports.DateTime.parse(date).minusDays(2).toString("yyyy-MM-dd")

    val input = pargs.required("input")
    val output = pargs.required("output")


    spark.read.json(input).createOrReplaceTempView("tb1")

    val sql_recover_today =
      s"""
         |select * from tb1 where from_unixtime(cast(kafkaTime/1000 as bigint),'yyyy-MM-dd')="$date"
       """.stripMargin

    val sql_recover_last1Day =
      s"""
         |select * from tb1 where from_unixtime(cast(kafkaTime/1000 as bigint),'yyyy-MM-dd')="$last1_day"
       """.stripMargin


    spark.sql(sql_recover_today).repartition(10).write.option("compression","gzip").mode("overwrite").json(s"$output/$date")
    spark.sql(sql_recover_last1Day).repartition(10).write.option("compression","gzip").mode("overwrite").json(s"$output/$last1_day")

    spark.close()

  }

}
