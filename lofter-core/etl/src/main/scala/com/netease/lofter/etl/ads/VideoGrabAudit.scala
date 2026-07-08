package com.netease.lofter.etl.ads

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}

object VideoGrabAudit {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))
    val videoAuditDay = "2021-01-01"

    val dateNum = date.replaceAll("-", "").toInt

    val sql_video_common =
      s"""
         |select aa.* from
         |(select a.uuid,a.blogid,a.postid,a.level,a.version,a.createtime
         |from
         |  (select uuid,blogid,bussinessid  as postid,priority as level,createtime,version ,rank()over(partition by blogid,bussinessid  order by createtime desc ) as rk
         |   from   lofter.ods_log_anti_spam_copy_video_di
         |   where  bizInfo='genPostLog'  and dt>='$videoAuditDay' and   from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd')>='$videoAuditDay') a
         |where a.rk=1) aa
         |join
         |(select postid from lofter_db_dump.ods_db_media_video_fetch_nd ) b
         |on aa.postid=b.postid
         |""".stripMargin

    val sql_day =
      s"""
         |select b.postid,b.blogid,b.version,b.status,b.uuid,b.createtime,b.operator from
         |(select postid,blogid,version,status, get_json_object(get_json_object(content,"$$.callback"),"$$.uuId") as uuid ,createtime ,
         |       operator,rank()over(partition by get_json_object(get_json_object(content,"$$.callback"),"$$.uuId") order by createtime desc) as rk
         |from lofter_db_dump.ods_db_risk_antispam_callback_record_nd
         |where type=0 and  version>0 and status in (0,2,3)  and from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd')='$date' ) b
         |where b.rk=1
         |""".stripMargin

    val sql_his =
      s"""
         |select b.postid,b.blogid,b.version,b.status,b.uuid,b.createtime,b.operator from
         |(select postid,blogid,version,status, get_json_object(get_json_object(content,"$$.callback"),"$$.uuId") as uuid ,createtime ,
         |       operator,rank()over(partition by get_json_object(get_json_object(content,"$$.callback"),"$$.uuId") order by createtime desc) as rk
         |from lofter_db_dump.ods_db_risk_antispam_callback_record_nd
         |where type=0 and  version>0 and status in (0,2,3)  and from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') >='$videoAuditDay' ) b
         |where b.rk=1
         |""".stripMargin

    spark.sql(sql_video_common).createOrReplaceTempView("video_table")
    spark.sql(sql_day).createOrReplaceTempView("dayAudit")
    spark.sql(sql_his).createOrReplaceTempView("hisAudit")

    val sql_video_audit_day =
      s"""
         |select '$date' as dt,
         |       count(distinct case when c.status in (0,3) then a.postid else null end) as dayPassPostNum
         |from
         |video_table a
         |left  join
         |dayAudit c
         |on a.uuid=c.uuid
         |group by dt
         |""".stripMargin

    val sql_video_audit_his =
      s"""
         |select '$date' as dt,
         |       count(distinct case when c.status in (0,3) then a.postid else null end) as hisPassPostNum,
         |       count(distinct case when c.status is null then a.postid else null end) as notAuditPostNum
         |from
         |video_table a
         |left  join
         |hisAudit c
         |on a.uuid=c.uuid
         |group by dt
         |""".stripMargin

    spark.sql(sql_video_audit_day).createOrReplaceTempView("t1")
    spark.sql(sql_video_audit_his).createOrReplaceTempView("t2")

    val sql_video_audit_result =
      s"""
         |select t1.dayPassPostNum,hisPassPostNum,notAuditPostNum
         |from t1 join t2
         |on t1.dt=t2.dt
         |""".stripMargin

    spark.sql(sql_video_audit_result)
      .withColumn("dt", lit(date))
      .repartition(1)
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_video_audit_di")

    spark.stop()
  }
}
