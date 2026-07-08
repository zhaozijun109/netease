package com.netease.lofter.etl.dwd

import com.netease.wm.util.Args
import org.apache.spark.sql.{SaveMode, SparkSession}

object FactUserLoginJob {
  def main(args: Array[String]): Unit = {

    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.required("date")
    val output = pargs.required("output")

    spark.sql("set spark.sql.caseSensitive=true")
    spark.read.json(s"/user/da_lofter/hive/lofter_ds_tomcat/dt=$date").createOrReplaceTempView("t1")

    spark.sql("create temporary function resolve_ip as 'com.netease.wm.udf.ResolveIp'")

    val sql_res =
      s"""
         |select  accountId,deviceUdid,clientType,actionType,ip,loginType,appVersion,
         |        userRemotePort,actionTime,uri, login_country, login_province, login_city
         |from (
         |    select coalesce(accountId,userId) as accountId,
         |           coalesce(deviceUdid,deviceudid) as deviceUdid ,
         |           clientType,type as actionType,IP as ip,cast(loginType as string) as loginType,appVersion,
         |           userRemotePort,time as actionTime, uri,
         |           inline(Array(resolve_ip(IP))) AS (login_country, login_province, login_city)
         |    from t1 where (accountId is not null) and time is not null
         |) t
         |""".stripMargin
    val outPath = s"$output/dt=$date"

    spark.sql(sql_res)
      .repartition(10)
      .write.mode(SaveMode.Overwrite)
      .parquet(outPath)

    spark.sql(s"alter table lofter.dwd_evt_user_login_di add if not exists partition(dt='$date') location '$outPath'")
    spark.close()
  }

}
