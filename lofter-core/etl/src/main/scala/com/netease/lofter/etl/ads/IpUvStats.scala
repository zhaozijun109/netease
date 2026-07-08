package com.netease.lofter.etl.ads

import org.apache.spark.sql.{SaveMode, SparkSession}

object IpUvStats {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Lofter Risk Ip Uv Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val sql_machine_users =
      s"""
         |select userId from lofter_db_dump.ods_db_media_account_import_nd where platformType in (0,2) group by userId
         |union
         |select blogId as userId from lofter_db_dump.ods_db_robot_blog_info_nd group by blogId
         |""".stripMargin
    spark.sql(sql_machine_users).cache().createOrReplaceTempView("machineUsers")

    val sql_ip_users =
      s"""
         |select a.ip,a.userId
         |from
         |(select distinct ip,userId
         |from lofter_db_dump.ods_db_audit_log_nd
         |where opType=6 and ip is not null and ip not in ('127.0.0.1','127.0.1')
         |) a
         |join
         |(select id as userId from lofter.dim_user where isanonymous=0 and istest=0) b
         |on a.userId=b.userId
         |left join machineUsers c
         |on a.userId=c.userId
         |where c.userId is null
      """.stripMargin

    spark.sql(sql_ip_users).createOrReplaceTempView("IpUsers")

    val sql_result =
      s"""
         |select a.ip,ipUv,ip3,ip3Uv
         |from
         |(select ip,count(distinct userId) as ipUv from IpUsers group by 1) a
         |left join
         |(select substring_index(ip,'.',3) as ip3,count(distinct userId) as ip3Uv from IpUsers group by 1) b
         |on substring_index(a.ip,'.',3)=b.ip3
         |where a.ip rlike '^(\\\\d{1,2}|1\\\\d\\\\d|2[0-4]\\\\d|25[0-5])\\\\.(\\\\d{1,2}|1\\\\d\\\\d|2[0-4]\\\\d|25[0-5])\\\\.(\\\\d{1,2}|1\\\\d\\\\d|2[0-4]\\\\d|25[0-5])\\\\.(\\\\d{1,2}|1\\\\d\\\\d|2[0-4]\\\\d|25[0-5])$$'
         |""".stripMargin

    spark.sql(sql_result).repartition(5).write.mode(SaveMode.Overwrite).insertInto("lofter.dws_ip_uv_nd")

    spark.stop()
  }
}
