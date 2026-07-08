package com.netease.lofter.etl.ods

import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

object RateLimitEtlJob {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val dt = pargs.required("date")

    val esUrl = "lofter-risk-es1.gy.ntes:7000,lofter-risk-es2.gy.ntes:7000,lofter-risk-es3.gy.ntes:7000"
    val esHosts = esUrl.split(",").map(_.split(":")(0))
    val esPort = 7000

    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .config("es.nodes", esHosts.mkString(","))
      .config("es.port", esPort.toString)
      .config("es.net.http.auth.user", "lofter_risk_online")
      .config("es.net.http.auth.pass", "67pg4UmF7F")
      .getOrCreate()

    val df = spark.read.format("es")
      .load("risk_limit/_doc")

    df.createOrReplaceTempView("es_risk_limit")

    val sql =
      s"""
         |insert overwrite table lofter.ods_risk_limit_dd partition(dt = '$dt')
         |select blogid,postid,status,type,opTime,operator
         |from es_risk_limit
         |""".stripMargin

    spark.sql(sql)

    spark.sql(s"create or replace view lofter.ods_risk_limit as select * from lofter.ods_risk_limit_dd where dt = '$dt' ")

    spark.close()
  }
}
