package com.netease.lofter.etl.dws

import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types._
import org.joda.time.{DateTime, Days}

object UserLastLoginJob {
  def main(args: Array[String]): Unit = {

    val pargs = Args(args)
    val day = pargs.required("date")
    val dayAgo = DateTime.parse(day).minusDays(1).toString("yyyy-MM-dd")

    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", value = true)
      .getOrCreate()

    val schema_tomcat= StructType(
      Array(StructField("accountId",LongType,false),
        StructField("time",LongType,true),
        StructField("clientType",StringType,true),
        StructField("deviceUdid",StringType,true),
        StructField("appVersion",StringType,true),
        StructField("type",StringType,true)
      )
    )

    spark.read.schema(schema_tomcat)
      .json(s"/user/da_lofter/hive/lofter_ds_tomcat/dt=$day")
      .createOrReplaceTempView("lofter_ds_tomcat_today")

    val schema_tomcat_all = StructType(
      Array(StructField("accountId",LongType,false),
        StructField("time",LongType,true),
        StructField("clientType",StringType,true),
        StructField("deviceUdid",StringType,true),
        StructField("appVersion",StringType,true),
        StructField("type",StringType,true),
        StructField("app_time",LongType,true),
        StructField("app_client_type",StringType,true)
      )
    )
    spark.read.schema(schema_tomcat_all).parquet(s"/user/da_lofter/hive/tomcatAll/$dayAgo")
      .createOrReplaceTempView("lofter_ds_tomcat_all_dayAgo")

    val sql_tomcat_merge=
      s"""
         |select accountId,
         |       min_by(time, rank) as time,
         |       min_by(clientType, rank) as clientType,
         |       min_by(deviceUdid, rank) as deviceUdid,
         |       min_by(appVersion, rank) as appVersion,
         |       if(datediff(from_unixtime(cast(min_by(time, rank)/1000 as bigint), 'yyyy-MM-dd'),
         |                   from_unixtime(cast(min(new_last_time)/1000 as bigint), 'yyyy-MM-dd')) > 0,
         |          min(new_last_time), null) as lastTime,
         |       min_by(app_time, app_rank) as app_time,
         |       min_by(app_client_type, app_rank) as app_client_type
         |from (
         |  select *,
         |        row_number() over (partition by accountId order by time desc) as rank,
         |        row_number() over (partition by accountId order by app_time desc) as app_rank
         |  from (
         |     select *,
         |            if(clientType in ('IPHONE', 'IPAD', 'ANDROID'), time, null) as app_time,
         |            if(clientType in ('IPHONE', 'IPAD', 'ANDROID'), clientType, null) as app_client_type,
         |            null as new_last_time
         |     from lofter_ds_tomcat_today where accountId > 0 and type='login'
         |
         |     union all
         |
         |     select *,
         |            time as new_last_time
         |     from lofter_ds_tomcat_all_dayAgo
         |     where accountId > 0
         |  ) a
         |) b
         |group by accountId
       """.stripMargin

    spark.sql(sql_tomcat_merge).createOrReplaceTempView("lofter_ds_tomcat_all")

    val outPath = s"/user/da_lofter/hive/tomcatAll/$day"

    spark.sql(sql_tomcat_merge)
      .repartition(2)
      .write.mode("overwrite")
      .parquet(outPath)

    spark.sql(s"alter table lofter.dws_evt_login_user_last_dd add if not exists partition(dt='$day') location '$outPath' ")
  }
}
