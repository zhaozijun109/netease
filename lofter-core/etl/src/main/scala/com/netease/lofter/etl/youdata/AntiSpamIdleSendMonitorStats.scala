package com.netease.lofter.etl.youdata

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.{SaveMode, SparkSession}

object AntiSpamIdleSendMonitorStats {

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Anti Spam IdleSend Monitor Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val yesterday = DateTime.yesterday.toString("yyyy-MM-dd")
    val date = pargs.optional("date").getOrElse(yesterday)
    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")

    val sql_idle_callback =
      s"""
         |SELECT b.day,b.idleareatype,b.taskid,b.name,b.tasktype
         |	,get_json_object(get_json_object(get_json_object(callbackhandleconf, "$$.defaultBlog"), "$$.machine"), "$$.forbid") AS machine_forbid_action
         |	,get_json_object(get_json_object(get_json_object(callbackhandleconf, "$$.defaultBlog"), "$$.human"), "$$.forbid") AS human_forbid_action
         |	,get_json_object(get_json_object(get_json_object(callbackhandleconf, "$$.defaultBlog"), "$$.machine"), "$$.forbidAgain") AS machine_forbidAgain_action
         |	,get_json_object(get_json_object(get_json_object(callbackhandleconf, "$$.defaultBlog"), "$$.human"), "$$.forbidAgain") AS human_forbidAgain_action
         |	,get_json_object(get_json_object(get_json_object(callbackhandleconf, "$$.defaultBlog"), "$$.machine"), "$$.normal") AS machine_normal_action
         |	,get_json_object(get_json_object(get_json_object(callbackhandleconf, "$$.defaultBlog"), "$$.human"), "$$.normal") AS human_normal_action
         |	,get_json_object(get_json_object(get_json_object(callbackhandleconf, "$$.defaultBlog"), "$$.machine"), "$$.suspect") AS machine_suspect_action
         |	,get_json_object(get_json_object(get_json_object(callbackhandleconf, "$$.defaultBlog"), "$$.human"), "$$.suspect") AS human_suspect_action
         |	,get_json_object(get_json_object(get_json_object(callbackhandleconf, "$$.defaultBlog"), "$$.machine"), "$$.unforbid") AS machine_unforbid_action
         |	,get_json_object(get_json_object(get_json_object(callbackhandleconf, "$$.defaultBlog"), "$$.human"), "$$.unforbid") AS human_unforbid_action
         |	,a.label,a.level,a.tag,a.status,a.machine
         |	,from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') AS callbackday
         |	,a.operator,count(DISTINCT a.postid) AS num
         |FROM (
         |	SELECT id AS taskid,name,starttime,callbackhandleconf
         |		,from_unixtime(cast(starttime / 1000 AS BIGINT), 'yyyy-MM-dd') AS day
         |		,tasktype,idleareatype
         |	FROM lofter_db_dump.ods_db_risk_antispam_hist_task_nd
         |	) b
         |JOIN (
         |	SELECT distinct a.machine,a.postid,a.blogid,a.taskid,a.label
         |		  ,a.level,a.tag,a.status,a.createtime,operator
         |	FROM (
         |		SELECT machine,postid,blogid,taskid,machine,label
         |			    ,level,tag,status,createtime,operator
         |			    ,rank() OVER (PARTITION BY machine,postid,blogid,taskid ORDER BY createtime DESC) AS rk
         |		FROM lofter_db_dump.ods_db_risk_antispam_post_tmp_nd
         |		WHERE taskid > 0
         |		) a
         |	WHERE a.rk = 1
         |	) a ON a.taskid = b.taskid
         |GROUP BY b.day
         |	,b.idleareatype
         |	,b.taskid
         |	,b.name
         |	,b.tasktype
         |	,get_json_object(get_json_object(get_json_object(callbackhandleconf, "$$.defaultBlog"), "$$.machine"), "$$.forbid")
         |	,get_json_object(get_json_object(get_json_object(callbackhandleconf, "$$.defaultBlog"), "$$.human"), "$$.forbid")
         |	,get_json_object(get_json_object(get_json_object(callbackhandleconf, "$$.defaultBlog"), "$$.machine"), "$$.forbidAgain")
         |	,get_json_object(get_json_object(get_json_object(callbackhandleconf, "$$.defaultBlog"), "$$.human"), "$$.forbidAgain")
         |	,get_json_object(get_json_object(get_json_object(callbackhandleconf, "$$.defaultBlog"), "$$.machine"), "$$.normal")
         |	,get_json_object(get_json_object(get_json_object(callbackhandleconf, "$$.defaultBlog"), "$$.human"), "$$.normal")
         |	,get_json_object(get_json_object(get_json_object(callbackhandleconf, "$$.defaultBlog"), "$$.machine"), "$$.suspect")
         |	,get_json_object(get_json_object(get_json_object(callbackhandleconf, "$$.defaultBlog"), "$$.human"), "$$.suspect")
         |	,get_json_object(get_json_object(get_json_object(callbackhandleconf, "$$.defaultBlog"), "$$.machine"), "$$.unforbid")
         |	,get_json_object(get_json_object(get_json_object(callbackhandleconf, "$$.defaultBlog"), "$$.human"), "$$.unforbid")
         |	,a.label
         |	,a.level
         |	,a.tag
         |	,a.status
         |	,a.machine
         |	,from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd')
         |	,a.operator
         |""".stripMargin

    val sql_forbidden_blog =
      s"""
         |select a.*,b.blogname,
         |    c.phones,last_phone,last_login_ip,createdate,from_platform,account_type,emails,
         |    d.createip
         |from
         |(select  blogid,from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') as day,forbidtype,hint,postids,operator
         | from  lofter_db_dump.ods_db_forbid_record_nd where status=2) a
         | join
         | (select blogid,blogname from lofter_db_dump.ods_db_blog_info_nd) b
         | on a.blogid=b.blogid
         |left join
         |(select userid,concat_ws(',',phones) as phones,last_phone,last_login_ip,
         |    from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd')  as createdate,
         |    from_platform,accounttype as account_type,concat_ws(',',emails) as emails
         |from lofter.dws_par_user_base_dd  where dt='$date') c
         |on a.blogid = c.userid
         |left join
         |(select userid , ip as createip from  lofter_db_dump.ods_db_audit_log_nd where opType=6 and ip is not null) d
         |on a.blogid=d.userid
         |""".stripMargin

    val sql_machine_audit =
      s"""
         |select b.day,
         |    b.idleareatype,b.taskid,b.name,b.tasktype,
         |    a.label,a.level as machinelevel,a.tag,
         |    from_unixtime(cast(a.createtime / 1000 AS BIGINT), 'yyyy-MM-dd')  as callbackday,
         |    a.operator,c.level as humanlevel,
         |    count(distinct a.postid) as num
         |from
         |    (select id as taskid,name,starttime,callbackhandleconf, from_unixtime(cast(starttime / 1000 AS BIGINT), 'yyyy-MM-dd') as day,
         |    tasktype,idleareatype
         |     from  lofter_db_dump.ods_db_risk_antispam_hist_task_nd ) b
         |join
         |(select a.machine,a.postid,a.blogid,a.taskid,a.label,a.level,a.tag,a.status,a.createtime,operator
         |from
         |     (select machine,postid,blogid,taskid,machine,label,level,tag,status,createtime,operator
         |             ,rank() over(partition by machine, postid,blogid,taskid order by createtime desc) as rk
         |     from lofter_db_dump.ods_db_risk_antispam_post_tmp_nd where taskid>0 and machine=1 ) a
         |where a.rk=1
         |group by a.postid,a.blogid,a.taskid,a.label,a.level,a.tag,a.status,a.createtime,a.machine,a.operator) a
         |on a.taskid=b.taskid
         |left join
         |(select a.machine,a.postid,a.blogid,a.taskid,a.label,a.level,a.tag,a.status,a.createtime,operator
         |from
         |     (select machine,postid,blogid,taskid,machine,label,level,tag,status,createtime,operator
         |    ,rank()over(partition by machine, postid,blogid,taskid order by createtime desc  ) as rk
         |    from lofter_db_dump.ods_db_risk_antispam_post_tmp_nd where taskid>0 and machine=0) a
         |where a.rk=1
         |group by a.postid,a.blogid,a.taskid,a.label,a.level,a.tag,a.status,a.createtime,a.machine,a.operator) c
         |on a.postid=c.postid and a.blogid=c.blogid and a.taskid=c.taskid
         |group by b.day,
         |b.idleareatype,b.taskid,b.name,b.tasktype,
         | a.label,a.level ,a.tag,
         | from_unixtime(cast(a.createtime / 1000 AS BIGINT), 'yyyy-MM-dd'),
         | a.operator,c.level
         |""".stripMargin

    val sql_person_audit =
      s"""
         |select b.day,
         |       b.idleareatype,b.taskid,b.name,b.tasktype,
         |       a.label,a.level as humanlevel1,a.tag,
         |       from_unixtime(cast(a.createtime / 1000 AS BIGINT), 'yyyy-MM-dd')  as callbackday,
         |       a.operator,c.level as humanlevel2,
         |       count(distinct a.postid) as num
         |from
         |(select id as taskid,name,starttime,callbackhandleconf, from_unixtime(cast(starttime / 1000 AS BIGINT), 'yyyy-MM-dd') as day,
         |tasktype,idleareatype
         | from  lofter_db_dump.ods_db_risk_antispam_hist_task_nd ) b
         |   join
         |(select a.machine,a.postid,a.blogid,a.taskid,a.label,a.level,a.tag,a.status,a.createtime,operator
         |from
         |    (select machine,postid,blogid,taskid,machine,label,level,tag,status,createtime,operator
         |    ,rank()over(partition by machine, postid,blogid,taskid order by createtime asc ) as rk
         |    from lofter_db_dump.ods_db_risk_antispam_post_tmp_nd where taskid>0 and machine=0 ) a
         |where a.rk=1
         |group by a.postid,a.blogid,a.taskid,a.label,a.level,a.tag,a.status,a.createtime,a.machine,a.operator) a
         |on a.taskid=b.taskid
         |left join
         |(select a.machine,a.postid,a.blogid,a.taskid,a.label,a.level,a.tag,a.status,a.createtime,operator
         |from
         |    (select machine,postid,blogid,taskid,machine,label,level,tag,status,createtime,operator
         |    ,rank()over(partition by machine, postid,blogid,taskid order by createtime desc ) as rk
         |    from lofter_db_dump.ods_db_risk_antispam_post_tmp_nd where taskid>0 and machine=0) a
         |where a.rk=1
         |group by a.postid,a.blogid,a.taskid,a.label,a.level,a.tag,a.status,a.createtime,a.machine,a.operator) c
         |on a.postid=c.postid and a.blogid=c.blogid and a.taskid=c.taskid
         |group by b.day,
         |b.idleareatype,b.taskid,b.name,b.tasktype,
         | a.label,a.level ,a.tag,
         | from_unixtime(cast(a.createtime / 1000 AS BIGINT), 'yyyy-MM-dd'),
         | a.operator,c.level
         |""".stripMargin

    spark.sql(sql_idle_callback)
      .repartition(1)
      .select("day","callbackday","name","idleareatype","taskid","tasktype",
        "machine_forbid_action","human_forbid_action","machine_forbidAgain_action","human_forbidAgain_action",
        "machine_normal_action","human_normal_action","machine_suspect_action","human_suspect_action",
        "machine_unforbid_action","human_unforbid_action","label","level","tag","status","machine","operator","num")
      .write
      .mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_anti_spam_idle_send_detail_nd")

    spark.sql(sql_forbidden_blog)
      .repartition(1)
      .select("day","blogid","forbidtype","hint","postids","operator","blogname","phones","last_phone","last_login_ip","createip","createdate","from_platform","account_type","emails")
      .write
      .mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_anti_spam_idle_send_forbidden_blog_nd")

    spark.sql(sql_machine_audit)
      .repartition(1)
      .select("day","callbackday","name","idleareatype","taskid","tasktype",
        "machinelevel","label","tag","operator","humanlevel","num")
      .write
      .mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_anti_spam_idle_send_machine_audit_nd")

    spark.sql(sql_person_audit)
      .repartition(1)
      .select("day","callbackday","name","idleareatype","taskid","tasktype",
        "humanlevel1","label","tag","operator","humanlevel2","num")
      .write
      .mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_anti_spam_idle_send_human_audit_nd")

    spark.close()

  }

}
