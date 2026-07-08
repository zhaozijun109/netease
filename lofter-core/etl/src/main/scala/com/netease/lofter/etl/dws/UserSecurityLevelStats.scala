package com.netease.lofter.etl.dws

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.lit

object UserSecurityLevelStats {

  def gradeClassification(flag: Int, score: Float): Int = {
    if (flag == 1) 1
    else if (flag == 2) 2
    else if (flag == 0 && score >= 0 && score < 4) 0
    else if (flag == 0 && score >= 4 && score < 15) 1
    else if (flag == 0 && score >= 15) 2
    else -1
  }

  def main(args: Array[String]): Unit = {

    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter User Security Level Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))

    val riskStartDate = DateTime.parse(date).minusYears(1).toString("yyyy-MM-dd")

    // register a udf function to spark sql
    spark.sqlContext.udf.register("gradeClassification", (flag: Int, score: Float) => gradeClassification(flag, score))

    val sql_anti_spam_copy =
      s"""
         |select if(bizInfo = 'genPostLog', blogid, publishUserId) as  blogid,
         |       bussinessId as businessId, bizInfo, version
         |from lofter.ods_log_anti_spam_copy_di
         |where hist = 0 and dt between '$riskStartDate' and  '$date' and
         |      from_unixtime(cast(createTime/1000 as bigint),'yyyy-MM-dd') between '$riskStartDate' and '$date' and
         |      bizInfo in('genPostLog','genPostResponseLog','genMessageLog')
         |""".stripMargin

    val sql_callback_record_post =
      s"""
         |select b.postid,b.blogid,b.status
         |from (
         |    select postid, blogid, status, version, createTime,
         |           rank() over(partition by postid,blogid order by version desc, createTime desc) as rk
         |    from (
         |        select postid,blogid,status,
         |               max(version) as version,
         |               max(createTime) as createTime
         |        from lofter_db_dump.ods_db_risk_antispam_callback_record_nd
         |        where type=0 and   version>0 and status in (0,2,3)
         |        group by postid,blogid,status
         |    ) a
         |) b
         |where rk=1
         |""".stripMargin

    val sql_callback_record_response =
      s"""
         |select distinct regexp_replace(get_json_object(content,"$$.responseIds"),'\\\\[|\\\\]','') as responseId,status
         |from lofter_db_dump.ods_db_risk_antispam_callback_record_nd
         |where  type=5 and  status=2
         |""".stripMargin

    val sql_callback_record_message =
      s"""
         |select distinct regexp_replace(get_json_object(content,"$$.messageIds"),'\\\\[|\\\\]','') as messageId,status
         |from lofter_db_dump.ods_db_risk_antispam_callback_record_nd
         |where  type=6 and  status=2
         |""".stripMargin

    spark.sql(sql_anti_spam_copy).cache().createOrReplaceTempView("antiSpamCopy")
    spark.sql(sql_callback_record_post).createOrReplaceTempView("callbackPost")
    spark.sql(sql_callback_record_response).createOrReplaceTempView("callbackResponse")
    spark.sql(sql_callback_record_message).createOrReplaceTempView("callbackMessage")

    val sql_not_pass_user =
      s"""
         |select a.blogid, a.bizInfo as type,
         |       count(distinct if(b.status=2, a.businessId, null)) / count(distinct a.businessId) as unpass_rate
         |from (
         | select distinct blogid,businessId,bizInfo from antiSpamCopy where bizInfo='genPostLog' and version>0
         |) a
         |join callbackPost b on a.businessId = b.postid and a.blogid = b.blogid
         |group by a.blogid, a.bizInfo
         |having count(distinct a.businessId) >= 3
         |
         |union all
         |
         |select a.blogid, a.bizInfo as type,
         |   count(distinct if(b.status is not null, a.businessId, null)) / count(distinct a.businessId) as unpass_rate
         |from (
         |    select blogid, businessId, bizInfo
         |    from antiSpamCopy
         |    where bizInfo='genPostResponseLog'
         |) a
         |left join callbackResponse b on a.businessId = b.responseId
         |group by a.blogid,a.bizInfo
         |having count(distinct a.businessId) >= 10
         |
         |union all
         |
         |select a.blogid,a.bizInfo as type,
         |       count(distinct if(b.status is not null, a.businessId, null)) / count(distinct a.businessId) as unpass_rate
         |from (
         |    select blogid,businessId,bizInfo from antiSpamCopy where bizInfo='genMessageLog'
         |) a
         |left join callbackMessage b on a.businessId = b.messageId
         |group by a.blogid,a.bizInfo
         |having count(distinct a.businessId) >= 3
         |
         |union all
         |
         |select a.blogid,'live' as type,
         |       count(distinct if(b.blogid is not null and b.recordId is not null, a.recordId, null)) / count(distinct a.recordId) as unpass_rate
         |from (
         |    select distinct anchor as blogid ,id as recordId
         |    from lofter_db_dump.ods_db_live_record_nd
         |) a
         |left join (
         |    select distinct anchor as blogid, recordId from lofter_db_dump.ods_db_live_forbidden_nd
         |) b on a.blogid=b.blogid and a.recordId = b.recordId
         |group by a.blogid
         |having count(distinct a.recordId)>=5
         |
         |union all
         |
         |select a.blogid,'focus' as type,
         |       count(distinct if(b.blogid is not null, a.followedBlogId, null))/ count(distinct a.followedBlogId) as unpass_rate
         |from (
         |    select distinct userid as blogid,blogid as followedblogid from lofter_db_dump.ods_db_user_following_nd
         |) a
         |left join (
         |    select distinct blogid,status from lofter_db_dump.ods_db_forbid_nd
         |) b on a.followedBlogid = b.blogid
         |group by a.blogid
         |having count(distinct a.followedBlogId)>=8
         |
         |union all
         |
         |select a.blogid,'fans' as type,
         |       count(distinct case when b.blogid is not null then a.fansId else null end)/count(distinct a.fansId) as unpass_rate
         |from
         |(select distinct blogid ,userid as fansId from lofter_db_dump.ods_db_user_following_nd) a
         |left join
         |(select distinct blogid,status from lofter_db_dump.ods_db_forbid_nd) b
         |on a.fansId=b.blogid
         |group by a.blogid
         |having count(distinct a.fansId)>=5
         |""".stripMargin

    spark.sql(sql_not_pass_user).cache().createOrReplaceTempView("unPassUser")

    val sql_user_result =
      s"""
         |select *
         |from unPassUser
         |where blogid in (
         |        select blogid
         |        from unPassUser
         |        group by blogid
         |        having array_contains(collect_set(type),'genPostLog') or
         |               array_contains(collect_set(type),'genPostResponseLog') or
         |               array_contains(collect_set(type),'genMessageLog')
         |)
         |""".stripMargin

    spark.sql(sql_user_result).repartition(100).cache().createOrReplaceTempView("userResult")

    val sql_user_security_level =
      s"""
         |select a.blogId,a.rate as score,a.flag as forbiddenType,gradeClassification(a.flag,a.rate) as grade,b.details
         |from
         |(select a.blogid,a.rate,CASE WHEN b.flag is null then 0 else b.flag end as flag
         |from
         |
         |(select a.blogid,round(sum(a.weight*a.unpass_rate),2) as rate
         |from
         |    (select  case when type='genPostResponseLog' then 28
         |                  when type='genMessageLog' then 17
         |                  when type='genPostLog' then 34
         |                  when type='fans' then 12
         |                  when type='focus' then 8
         |                  when type='live' then 0 else 0 end as weight,
         |              blogid,unpass_rate
         |    from  userResult
         |    ) a
         |group by a.blogid) a
         |
         |left join
         |
         |(select distinct a.blogid,
         |        case when a.status=1 and a.days>=180 then 0
         |             when a.status=1 and a.days<180 then 1
         |             else  2 end as flag
         |from
         |      (select  blogid,status,
         |                datediff('$date',from_unixtime(cast(ForbidTime / 1000 AS BIGINT), 'yyyy-MM-dd'))  as days
         |      from lofter_db_dump.ods_db_forbid_nd )  a
         |) b
         |
         |on a.blogid=b.blogid) a
         |join
         |(select blogid,collect_list(named_struct('type',type,'unpass_rate',unpass_rate)) as details
         | from userResult group by blogid) b
         |on a.blogid=b.blogid
         |""".stripMargin

    spark.sql(sql_user_security_level)
      .withColumn("dt", lit(date))
      .repartition(5)
      .write.mode("overwrite")
      .insertInto("lofter.dws_user_security_level_di")

    spark.stop()
  }
}
