package com.netease.lofter.etl.dwd

import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}

object AntispamCallbackStatJob {
  def main(args: Array[String]): Unit = {

    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("extract the anti spam call back info")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.required("date")

    val sql_res =
      s"""
         |select 'comment' as opType,a.userId,bussinessId,bizInfo,uuid,status,version,machine,
         |    case when b.responseId is null then 'normal' else 'mask' end as flag
         |from
         |(select publishuserid as userid,bussinessId ,bizInfo,uuid
         |from lofter.ods_log_anti_spam_copy_di
         |where   hist=0 and dt='$date' and bizInfo in ('genPostResponseLog')
         |group by publishuserid,bussinessId,bizInfo,uuid ) a
         |left join
         |
         |(select distinct regexp_replace(get_json_object(content,"$$.responseIds"),'\\[|\\]','') as responseId,status,version,machine
         |from lofter_db_dump.ods_db_risk_antispam_callback_record_nd where  type=5 and  status=2
         |) b
         |on a.bussinessId=b.responseId
         |
         |union all
         |select 'message' as opType,a.userId,bussinessId,bizInfo,uuid,status,version,machine,
         |    case when b.messageId is null then 'normal' else 'mask' end as flag
         |from
         |(select publishuserid as userid,bussinessId,bizInfo,uuid
         |from lofter.ods_log_anti_spam_copy_di
         |where   hist=0 and dt='$date' and bizInfo in ('genMessageLog')
         |group by publishuserid,bussinessId,bizInfo,uuid ) a
         |left join
         |
         |(select distinct regexp_replace(get_json_object(content,"$$.messageIds"),'\\[|\\]','') as messageId,status,version,machine
         |from lofter_db_dump.ods_db_risk_antispam_callback_record_nd where  type=6 and  status=2
         |) b
         |on a.bussinessId=b.messageId
         |
         |union all
         |select 'post_version' as opType,a.userId,bussinessId,bizInfo,uuid,status,a.version,machine,
         |    case when b.status=2 then 'mask' else 'normal' end as flag
         |from
         |(select blogId as userId,bussinessId ,bizInfo,uuid,version
         |from lofter.ods_log_anti_spam_copy_di
         |where dt='$date' and hist=0 and bizInfo in ('genPostLog') and category='post'
         |group by blogId,bussinessId,bizInfo,uuid,version ) a
         |join
         |
         |(select postId,blogId,status,version,machine
         |from 
         |    (select postId,blogId,status,version,createTime,machine,
         |        row_number() over(partition by postId,blogId,version order by createTime desc ) as rk
         |    from lofter_db_dump.ods_db_risk_antispam_callback_record_nd 
         |    where type=0 and version>0 and status in (0,2,3) and 
         |        from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') ='$date'  
         |    ) a 
         |where a.rk=1 
         |) b 
         |on a.bussinessId=b.postId and a.userId=b.blogId and a.version=b.version
         |""".stripMargin

    spark.sql(sql_res)
      .withColumn("dt", lit(date))
      .repartition(1)
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter.dwd_antispam_copy_and_callback_di")

    spark.close()
  }

}
