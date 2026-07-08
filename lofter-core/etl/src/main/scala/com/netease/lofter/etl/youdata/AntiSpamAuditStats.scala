package com.netease.lofter.etl.youdata

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}

object AntiSpamAuditStats {
  def main(args: Array[String]): Unit = {
    val params = Args(args)
    val spark = SparkSession.builder()
      .appName("lofter Anti Spam Audit stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val yesterday = DateTime.yesterday().toString("yyyy-MM-dd")
    val date = params.optional("date").getOrElse(yesterday)
    val day15Ago = DateTime.parse(date).minusDays(15).toString("yyyy-MM-dd")

    val sql_callback_his =
      s"""
         |select postid,blogid,version,status, get_json_object(get_json_object(content,"$$.callback"),"$$.uuId") as uuid ,
         |       createtime,operator,forbidtype,hint,machine
         |from lofter_db_dump.ods_db_risk_antispam_callback_record_nd
         |where type=0 and  version>0 and status in (0,1,2,3)  and from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') between '$day15Ago' and '$date'
         |""".stripMargin

    spark.sql(sql_callback_his).cache().createOrReplaceTempView("callBackHis")

    val sql_audit_person =
      s"""
         |select a.copyDate,'person' as auditType,b.status as firstStatus,c.status as secondStatus,b.operator,
         |       b.forbidType,b.hint,count(distinct b.postid,b.blogid,b.version) as num
         |from
         |    (select distinct uuid, bussinessType as posttype,blogid,bussinessid  as postid,priority as level,createtime,version,
         |            from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') as copyDate
         |    from   lofter.ods_log_anti_spam_copy_di
         |    where  bizInfo='genPostLog'  and  dt between '$day15Ago' and '$date'
         |    and from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') between '$day15Ago' and '$date'
         |    ) a
         |    join
         |    (select a.postid,a.blogid,a.version,a.status,a.uuid,a.createtime,a.operator,a.forbidtype,a.hint
         |    from
         |    (select postid,blogid,version,status,uuid,
         |            createtime ,operator,rank()over(partition by uuid order by  createtime asc) as rk,
         |            forbidtype,hint
         |    from callBackHis
         |    where status in (2,3) and machine=0 ) a
         |    where a.rk=1
         |    group by a.postid,a.blogid,a.version,a.status,a.uuid,a.createtime,a.operator,a.forbidtype,a.hint) b
         |    on  a.blogid=b.blogid and a.postid=b.postid and a.version=b.version and a.uuid=b.uuid and a.createtime<b.createtime
         |    left join
         |    (select a.postid,a.blogid,a.version,a.status,a.uuid,a.createtime,a.operator
         |    from
         |    (select postid,blogid,version,status,uuid ,createtime ,
         |            operator,rank()over(partition by uuid order by createtime desc) as rk
         |    from callBackHis
         |    where status in (2,3) and machine=0) a
         |    where a.rk=1
         |    group by a.postid,a.blogid,a.version,a.status,a.uuid,a.createtime,a.operator) c
         |    on  b.blogid=c.blogid and b.postid=c.postid and b.version=c.version and b.uuid=c.uuid and b.createtime<c.createtime
         |group by a.copyDate,b.status,c.status ,b.operator,b.forbidType,b.hint
         |""".stripMargin

    val sql_audit_machine =
      s"""
         |select a.copyDate,'machine' as auditType,b.status as firstStatus,c.status as secondStatus,b.operator,
         |       b.forbidType,b.hint,count(distinct b.postid,b.blogid,b.version) as num
         |from
         |    (select distinct uuid, bussinessType as posttype,blogid,bussinessid  as postid,priority as level,createtime,version,
         |            from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') as copyDate
         |    from   lofter.ods_log_anti_spam_copy_di
         |    where  bizInfo='genPostLog'  and  dt between '$day15Ago' and '$date'
         |    and from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') between '$day15Ago' and '$date'
         |    ) a
         |    join
         |    (select a.postid,a.blogid,a.version,a.status,a.uuid,a.createtime,a.operator,a.forbidtype,a.hint
         |    from
         |    (select postid,blogid,version,status,uuid,
         |            createtime ,operator,rank()over(partition by uuid order by status desc, createtime asc) as rk,
         |            forbidtype,hint
         |    from callBackHis
         |    where status in(0,1,2) and machine in(-1,1) ) a
         |    where a.rk=1
         |    group by a.postid,a.blogid,a.version,a.status,a.uuid,a.createtime,a.operator,a.forbidtype,a.hint) b
         |    on  a.blogid=b.blogid and a.postid=b.postid and a.version=b.version and a.uuid=b.uuid and a.createtime<b.createtime
         |    left join
         |    (select a.postid,a.blogid,a.version,a.status,a.uuid,a.createtime,a.operator
         |    from
         |    (select postid,blogid,version,status,uuid ,createtime ,
         |            operator,rank()over(partition by uuid order by createtime desc) as rk
         |    from callBackHis
         |    where status in (2,3) and machine=0) a
         |    where a.rk=1
         |    group by a.postid,a.blogid,a.version,a.status,a.uuid,a.createtime,a.operator) c
         |    on  b.blogid=c.blogid and b.postid=c.postid and b.version=c.version and b.uuid=c.uuid and b.createtime<c.createtime
         |group by a.copyDate,b.status,c.status ,b.operator,b.forbidType,b.hint
         |""".stripMargin

    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")
    spark.sql(sql_audit_machine).union(spark.sql(sql_audit_person))
      .repartition(5)
      .select("copyDate","auditType","firstStatus","secondStatus","operator","forbidType","hint","num")
      .withColumn("dt",lit(date))
      .write
      .mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_anti_spam_audit_stats_di")

    spark.close()
  }
}
