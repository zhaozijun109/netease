package com.netease.lofter.etl.youdata

import com.github.nscala_time.time.Imports
import com.netease.wm.util.Args
import org.apache.spark.sql.{SaveMode, SparkSession}

object PostChangeReasonStats {
  def main(args: Array[String]): Unit = {

    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Post Change Reason Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(Imports.DateTime.yesterday.toString("yyyy-MM-dd"))

    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")

    postChangeReasonStats(spark, date)
    spark.close()

  }

  def postChangeReasonStats(spark: SparkSession, date: String) : Unit = {

    val sql_1 =
      s"""
         |select t1.dt,t1.opname,
         |   case when t2.postid is null then '未知'
         |        when t2.modifier='antispam' then '反垃圾'
         |        when t1.opname='删文' and t4.userid is not null and t4.optime<=t2.modifytime then '删除博客'
         |        when t1.opname='删文' and t5.userid is not null and t5.optime<=t2.modifytime then '删除博客'
         |        when t1.opname='删文' and t2.modifier='0' then '删除博客'
         |        when t2.modifier='0' then '未知'
         |        when t3.userid is not null then '用户自己操作'
         |        when t3.userid is null then '他人操作' end modifier_type,
         |   count(distinct t1.id) op_postnum,
         |   count(distinct t1.blogid) op_uv
         |from
         |(
         |select dt,id,modifytime,blogid,publisheruserid,
         |   case when valid=32 and `_bin_old`['valid']=0 then '删文'
         |     when allowview=100 and `_bin_old`['allowview']=0 then '变更为仅自己可见'
         |     when valid=25 and `_bin_old`['valid']=0 then '封禁文章' end opname
         |from lofter.ods_binlog_post_di a
         |where dt='$date' and `_bin_op`=2
         |and ((valid=32 and `_bin_old`['valid']=0) or (allowview=100 and `_bin_old`['allowview']=0) or (valid=25 and `_bin_old`['valid']=0))
         |)t1
         |
         |left outer join
         |(
         |select postid,modifier,modifytime,blogid
         |from lofter.ods_log_post_change_di
         |where dt='$date' and action in('delPost','updatePost')
         |)t2
         |on t1.id=t2.postid and t1.modifytime=t2.modifytime
         |
         |left outer join
         |(
         |---blogid的关系userid列表
         |select distinct blogid,userid
         |from
         |(
         |select blogid,userid from lofter.dim_post
         |union all
         |select blogid,userid from lofter_db_dump.ods_db_user_blog_account_nd
         |)a
         |)t3 on t2.blogid=t3.blogid and t2.modifier=t3.userid
         |
         |left outer join
         |(
         |----删除账号
         |select userid,optime
         |from lofter_db_dump.ods_db_audit_log_nd
         |where optype='2'
         |)t4
         |on t1.publisheruserid=t4.userid
         |
         |left outer join
         |(
         |----删除博客
         |select userid,optime
         |from lofter_db_dump.ods_db_audit_log_nd
         |where optype='1'
         |)t5
         |on t1.blogid=t5.userid
         |group by t1.dt,t1.opname,
         |   case when t2.postid is null then '未知'
         |        when t2.modifier='antispam' then '反垃圾'
         |        when t1.opname='删文' and t4.userid is not null and t4.optime<=t2.modifytime then '删除博客'
         |        when t1.opname='删文' and t5.userid is not null and t5.optime<=t2.modifytime then '删除博客'
         |        when t1.opname='删文' and t2.modifier='0' then '删除博客'
         |        when t2.modifier='0' then '未知'
         |        when t3.userid is not null then '用户自己操作'
         |        when t3.userid is null then '他人操作' end
         |""".stripMargin

    spark.sql(sql_1)
      .repartition(1)
      .select("opname","modifier_type", "op_postnum","op_uv","dt")
      .write
      .mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_post_change_reason_stat_di")
  }

}
