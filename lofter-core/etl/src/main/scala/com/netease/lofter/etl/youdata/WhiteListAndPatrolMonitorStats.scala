package com.netease.lofter.etl.youdata

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

object WhiteListAndPatrolMonitorStats {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Anti Risk WhiteList and Patrol Monitor Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))

    val sql_white_list_all =
      s"""
         |select '$date' as dt,status,count(distinct a.postid) as postNum,
         |       count(distinct a.blogid) as blogNum,count(distinct a.postid,a.version) as  versionNum
         |from
         |(select operator,postid,blogid,version,status
         |from lofter_db_dump.ods_db_risk_antispam_callback_record_nd
         |where get_json_object(get_json_object(content,"$$.callback"),"$$.headUserFlag")='true'
         |      and machine=0  and  type=0 and   version>0 and status in (2,3)  and
         |      from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd')='$date'
         |) a
         |group by status
         |""".stripMargin

    val sql_white_list_detail =
      s"""
         |select distinct '$date' as dt,a.postId,a.blogId,c.blogName,a.operator,
         |       concat(c.blogname,'.lofter.com/post/',conv(b.blogid, 10, 16),'_',conv(a.postid, 10, 16)) as url
         |from
         |(select distinct operator,postid,blogid,version,createtime
         |from lofter_db_dump.ods_db_risk_antispam_callback_record_nd
         |where get_json_object(get_json_object(content,"$$.callback"),"$$.headUserFlag")='true' and machine=0  and
         |      type=0 and   version>0 and status =2  and
         |      from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd')  ='$date'
         |) a
         |join
         |(select distinct operator,postid,blogid,createtime
         |from lofter_db_dump.ods_db_risk_antispam_callback_record_nd
         |where get_json_object(content,"$$.method")='batchAuditPosts' and machine=0  and  type=0 and   status =2  and
         |      from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') ='$date'
         |) b
         |on a.postid=b.postid and a.blogid=b.blogid and a.operator=b.operator and abs(a.createtime-b.createtime)<5000
         |join
         |(select blogid,blogname from lofter_db_dump.ods_db_blog_info_nd) c
         |on b.blogid=c.blogid
         |""".stripMargin

    val sql_patrol_all =
      s"""
         |SELECT '$date' as dt,operator,status,
         |    count(DISTINCT postid,version) AS versionNum, count(DISTINCT postid) AS postNum
         |FROM
         |    lofter_db_dump.ods_db_risk_antispam_callback_record_nd
         |WHERE content LIKE '%inspection_backend%' AND machine = 0
         |	   AND from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') = '$date'
         |GROUP BY operator,status,'$date'
         |grouping sets(
         |    (operator,status,'$date'),
         |    (status,'$date')
         |	)
         |""".stripMargin

    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")

    spark.sql(sql_white_list_all)
      .select("status","postNum","blogNum","versionNum","dt")
      .write
      .mode("overwrite")
      .insertInto("lofter_dm.ads_anti_spam_whitelist_all_di")

    spark.sql(sql_white_list_detail)
      .select("postId","blogId","blogName","operator","url","dt")
      .write
      .mode("overwrite")
      .insertInto("lofter_dm.ads_anti_spam_whitelist_detail_di")

    spark.sql(sql_patrol_all)
      .select("operator","status","postNum","versionNum","dt")
      .write
      .mode("overwrite")
      .insertInto("lofter_dm.ads_anti_spam_patrol_all_di")

    spark.close()
  }

}
