package com.netease.lofter.etl.youdata

import com.github.nscala_time.time.Imports
import com.netease.wm.util.Args
import org.apache.spark.sql.{SaveMode, SparkSession}

object AntiSpamCallbackTimeStats {

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Anti Spam Callback Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(Imports.DateTime.yesterday.toString("yyyy-MM-dd"))
    val ONE_DAY_TIMESTAMP = 24 * 3600 * 1000L

    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")
    // regist a udf function to spark sql
    spark.sqlContext.udf.register("callBackTimeClassification",
      (time1: Long, time2: Long) => callBackTimeClassification(time1, time2))

    val sql_machine =
      s"""
         |select
         |   from_unixtime(cast(createtime/1000 as bigint),'yyyy-MM-dd') day,createtime,get_json_object(get_json_object(content,'$$.callback'),'$$.uuId')  uuid,
         |   postid,operator,machine,version,status,blogid,get_json_object(get_json_object(content,'$$.callback'),'$$.reviewLater') as reviewLater
         |from
         |   lofter_db_dump.ods_db_risk_antispam_callback_record_nd
         |where type=0 and version>0 and machine in (1,-1) and  from_unixtime(cast(createtime/1000 as bigint),'yyyy-MM-dd') between '$date' and date_add('$date', 1)
         |""".stripMargin

    val sql_person =
      s"""
         |select
         |   from_unixtime(cast(createtime/1000 as bigint),'yyyy-MM-dd') day,createtime,get_json_object(get_json_object(content,'$$.callback'),'$$.uuId')  uuid,
         |   postid,operator,machine,version,status,blogid
         |from
         |   lofter_db_dump.ods_db_risk_antispam_callback_record_nd
         |where type=0 and version>0 and machine=0 and  from_unixtime(cast(createtime/1000 as bigint),'yyyy-MM-dd') between '$date' and date_add('$date', 1)
         |""".stripMargin

    val sql_all =
      s"""
         |select
         |   from_unixtime(cast(createtime/1000 as bigint),'yyyy-MM-dd') day,createtime,get_json_object(get_json_object(content,'$$.callback'),'$$.uuId')  uuid,
         |   postid,operator,machine,version,status,blogid,get_json_object(get_json_object(content,'$$.callback'),'$$.reviewLater') as reviewLater
         |from
         |   lofter_db_dump.ods_db_risk_antispam_callback_record_nd
         |where type=0 and version>0 and  from_unixtime(cast(createtime/1000 as bigint),'yyyy-MM-dd') between '$date' and date_add('$date', 1)
         |""".stripMargin

    spark.sql(sql_machine).createOrReplaceTempView("machine_audit")
    spark.sql(sql_person).createOrReplaceTempView("person_audit")
    spark.sql(sql_all).createOrReplaceTempView("all_audit")

    val sql_first_machine =
      s"""
         |select
         |    day,createtime,uuid,postid,operator,machine,version,status,blogid
         | from
         |    (select
         |        from_unixtime(cast(createtime/1000 as bigint),'yyyy-MM-dd') day,createtime,get_json_object(get_json_object(content,'$$.callback'),'$$.uuId')  uuid,
         |        rank()over(partition by get_json_object(get_json_object(content,'$$.callback'),'$$.uuId') order by createtime asc) rk,
         |        postid,operator,machine,version,status,blogid
         |     from
         |        lofter_db_dump.ods_db_risk_antispam_callback_record_nd
         |     where type=0 and version>0 and machine in (1,-1) and  from_unixtime(cast(createtime/1000 as bigint),'yyyy-MM-dd') between '$date' and date_add('$date', 1) /**第一次机审的时间**/
         |     ) b
         | where rk=1
         |""".stripMargin

    val sql_first_person =
      s"""
         |select
         |    day,createtime,uuid,postid,operator,machine,version,status,blogid
         | from
         |    (select
         |        from_unixtime(cast(createtime/1000 as bigint),'yyyy-MM-dd') day,createtime,get_json_object(get_json_object(content,'$$.callback'),'$$.uuId')  uuid,
         |        rank()over(partition by get_json_object(get_json_object(content,'$$.callback'),'$$.uuId') order by createtime asc) rk,
         |        postid,operator,machine,version,status,blogid
         |     from
         |        lofter_db_dump.ods_db_risk_antispam_callback_record_nd
         |     where type=0 and version>0 and machine=0 and  from_unixtime(cast(createtime/1000 as bigint),'yyyy-MM-dd') between '$date' and date_add('$date', 1) /**第一次人审的时间**/
         |     )b
         |where rk=1
         |""".stripMargin

    val sql_anti_spam_copy =
      s"""
         |select
         |    distinct dt, bussinesstype contenttype, createtime,uuid,policyid postid,priority,version,photocheckcount,blogid,
         |    hour(from_unixtime(cast(createtime/1000 as bigint),'yyyy-MM-dd HH:mm:ss')) as hh
         |  from
         |    lofter.ods_log_anti_spam_copy_di
         |  where bizinfo='genPostLog' and dt='$date' and from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd')='$date'
         |""".stripMargin

    spark.sql(sql_anti_spam_copy).createOrReplaceTempView("anti_spam_copy")
    spark.sql(sql_first_machine).createOrReplaceTempView("first_machine_audit")
    spark.sql(sql_first_person).createOrReplaceTempView("first_person_audit")

    val sql_zq_1 =
      s"""
         |select
         |    a.dt,a.hh,a.contenttype,a.priority, count(distinct a.postid,a.version,a.uuid) as uuid_num1, count(distinct b.operator) as operator_uv
         |from
         |    anti_spam_copy a
         |join
         |    person_audit b
         |on a.uuid = b.uuid
         |group by
         |    a.dt,a.hh,a.contenttype,a.priority
         |grouping sets((a.dt,a.contenttype,a.priority),(a.dt,a.hh,a.contenttype,a.priority))
         |""".stripMargin

    val sql_zq_2 =
      s"""
         |select
         |    a.dt,a.hh,a.contenttype,a.priority,
         |    count(distinct case when b.status=2 then concat_ws('_',a.postid,a.version,a.uuid) else null end) as fail_num,
         |    count(distinct case when b.status=3 then concat_ws('_',a.postid,a.version,a.uuid) else null end) as pass_num
         |from
         |    anti_spam_copy a
         |join
         |    first_person_audit b
         |on a.uuid = b.uuid and a.blogid=b.blogid and a.postid=b.postid and a.version=b.version
         |group by
         |    a.dt,a.hh,a.contenttype,a.priority
         |grouping sets((a.dt,a.contenttype,a.priority),(a.dt,a.hh,a.contenttype,a.priority))
         |""".stripMargin

    val sql_zq_3 =
      s"""
         |select
         |    a.dt,a.hh,a.contenttype,a.priority, count(distinct a.postid,a.version,a.uuid) as uuid_num3,sum(b.createtime-a.createtime) as times3,count(distinct b.operator) as uv
         |from
         |    anti_spam_copy a
         |join
         |    (select * from
         |        (select *,rank() over(partition by uuid order by createtime asc) as rk from all_audit where status in (0,2,3) ) a1
         |      where rk=1
         |    ) b
         |on a.uuid = b.uuid and a.blogid=b.blogid and a.postid=b.postid and a.version=b.version
         |group by
         |    a.dt,a.hh,a.contenttype,a.priority
         |grouping sets((a.dt,a.contenttype,a.priority),(a.dt,a.hh,a.contenttype,a.priority))
         |""".stripMargin

    val sql_zq_4 =
      s"""
         |select
         |    a.dt,a.hh,a.contenttype,a.priority, count(distinct a.postid,a.version,a.uuid) as uuid_num4,sum(c.createtime-b.createtime) as times4,count(distinct c.operator) as uv
         |from
         |    anti_spam_copy a
         |join
         |    (select * from
         |        (select *,rank() over(partition by uuid order by createtime asc) as rk from all_audit where (status=1 or (status=0 and reviewLater='true')) and machine in (1,-1) ) a1
         |      where rk=1
         |    ) b
         |on a.uuid = b.uuid and a.blogid=b.blogid and a.postid=b.postid and a.version=b.version
         |join
         |    (select * from
         |        (select *,rank() over(partition by uuid order by createtime asc) as rk from all_audit where status in (2,3) and machine=0 ) c1
         |      where rk=1
         |    ) c
         |on b.uuid = c.uuid and b.blogid=c.blogid and b.postid=c.postid and b.version=c.version and b.createtime<=c.createtime
         |group by
         |    a.dt,a.hh,a.contenttype,a.priority
         |grouping sets((a.dt,a.contenttype,a.priority),(a.dt,a.hh,a.contenttype,a.priority))
         |""".stripMargin

    val sql_zq_5 =
      s"""
         |select
         |    a.dt,a.hh,a.contenttype,a.priority, count(distinct a.postid,a.version,a.uuid) as uuid_num5,
         |    sum(case when c.createtime is null then $ONE_DAY_TIMESTAMP else c.createtime-b.createtime end) as times5,
         |    count(distinct c.operator) as uv
         |from
         |    anti_spam_copy a
         |join
         |    (select * from
         |        (select *,rank() over(partition by uuid order by createtime asc) as rk from all_audit where status=0 and machine in (1,-1) ) a1
         |      where rk=1
         |    ) b
         |on a.uuid = b.uuid and a.blogid=b.blogid and a.postid=b.postid and a.version=b.version
         |left join
         |    (select * from
         |        (select *,rank() over(partition by uuid order by createtime asc) as rk from all_audit where status in (2,3) and machine=0 ) c1
         |      where rk=1
         |    ) c
         |on b.uuid = c.uuid and b.blogid=c.blogid and b.postid=c.postid and b.version=c.version and b.createtime<=c.createtime
         |group by
         |    a.dt,a.hh,a.contenttype,a.priority
         |grouping sets((a.dt,a.contenttype,a.priority),(a.dt,a.hh,a.contenttype,a.priority))
         |""".stripMargin

    spark.sql(sql_zq_1).createOrReplaceTempView("zq_1")
    spark.sql(sql_zq_2).createOrReplaceTempView("zq_2")
    spark.sql(sql_zq_3).createOrReplaceTempView("zq_3")
    spark.sql(sql_zq_4).createOrReplaceTempView("zq_4")
    spark.sql(sql_zq_5).createOrReplaceTempView("zq_5")

    val sql_zq_res_dd =
      s"""
         |select
         |    a.dt,a.contenttype,a.priority,a.uuid_num1 as uuid_num,a.operator_uv,
         |    b.pass_num as person_audit_pass_num,
         |    b.fail_num as person_audit_fail_num,
         |    (c.times3/c.uuid_num3/(1000L *60)) as overall_callback_time,
         |    (d.times4/d.uuid_num4/(1000L *60)) as normal_callback_time,
         |    (e.times5/e.uuid_num5/(1000L *60)) as force_callback_time,
         |    c.times3/1000 as overall_total_time,
         |    c.uuid_num3 as overall_total_uuid_number,
         |    c.uv as overall_total_operator,
         |    d.times4/1000 as normal_total_time,
         |    d.uuid_num4 as normal_total_uuid_number,
         |    d.uv as normal_total_operator,
         |    e.times5/1000 as force_total_time,
         |    e.uuid_num5 as force_total_uuid_number,
         |    e.uv as force_total_operator
         |from
         |    (select * from zq_1 where hh is null) a
         |left join (select * from zq_2 where hh is null) b
         |on a.dt=b.dt and a.contenttype=b.contenttype and a.priority=b.priority
         |left join (select * from zq_3 where hh is null) c
         |on a.dt=c.dt and a.contenttype=c.contenttype and a.priority=c.priority
         |left join (select * from zq_4 where hh is null) d
         |on a.dt=d.dt and a.contenttype=d.contenttype and a.priority=d.priority
         |left join (select * from zq_5 where hh is null) e
         |on a.dt=e.dt and a.contenttype=e.contenttype and a.priority=e.priority
         |""".stripMargin

    val sql_zq_res_hh =
      s"""
         |select
         |    a.dt,a.hh,a.contenttype,a.priority,a.uuid_num1 as uuid_num,a.operator_uv,
         |    b.pass_num as person_audit_pass_num,
         |    b.fail_num as person_audit_fail_num,
         |    (c.times3/c.uuid_num3/(1000L *60)) as overall_callback_time,
         |    (d.times4/d.uuid_num4/(1000L *60)) as normal_callback_time,
         |    (e.times5/e.uuid_num5/(1000L *60)) as force_callback_time,
         |    c.times3/1000 as overall_total_time,
         |    c.uuid_num3 as overall_total_uuid_number,
         |    c.uv as overall_total_operator,
         |    d.times4/1000 as normal_total_time,
         |    d.uuid_num4 as normal_total_uuid_number,
         |    d.uv as normal_total_operator,
         |    e.times5/1000 as force_total_time,
         |    e.uuid_num5 as force_total_uuid_number,
         |    e.uv as force_total_operator
         |from
         |    (select * from zq_1 where hh is not null) a
         |left join (select * from zq_2 where hh is not null) b
         |on a.dt=b.dt and a.hh=b.hh and a.contenttype=b.contenttype and a.priority=b.priority
         |left join (select * from zq_3 where hh is not null) c
         |on a.dt=c.dt and a.hh=c.hh and a.contenttype=c.contenttype and a.priority=c.priority
         |left join (select * from zq_4 where hh is not null) d
         |on a.dt=d.dt and a.hh=d.hh and a.contenttype=d.contenttype and a.priority=d.priority
         |left join (select * from zq_5 where hh is not null) e
         |on a.dt=e.dt and a.hh=e.hh and a.contenttype=e.contenttype and a.priority=e.priority
         |""".stripMargin


    val sql_1 =
      s"""
         |select
         |     t.dt,
         |     case when t.contenttype=1 then "文本"
         |     when t.contenttype=2 then "图片"
         |     when t.contenttype=3 then "音乐"
         |     when t.contenttype=4 then "视频"
         |     when t.contenttype=5 then "问答"
         |     when t.contenttype=6 then "长文章" end contentType,
         |     t.priority,
         |     callBackTimeClassification(a.createtime,b.createtime) as timeInterval,
         |     count(distinct t.uuid) as auditNum,
         |     count(distinct t.postid) postNum,
         |     count(distinct concat(t.postid,t.version)) postVersionNum,
         |     count(distinct b.operator) operators,
         |     count(distinct case when b.status=3 then t.uuid else null end) as passNum,
         |     count(distinct  case when b.status=2 then t.uuid else null end) as blockNum
         |from
         |    anti_spam_copy t
         |     join first_machine_audit a
         |          on t.uuid=a.uuid and t.dt=a.day
         |     join first_person_audit b
         |          on  a.uuid=b.uuid and a.day=b.day
         |group by
         |      t.dt,
         |      case when t.contenttype=1 then "文本"
         |          when t.contenttype=2 then "图片"
         |          when t.contenttype=3 then "音乐"
         |          when t.contenttype=4 then "视频"
         |          when t.contenttype=5 then "问答"
         |          when t.contenttype=6 then "长文章" end,
         |       t.priority,
         |       callBackTimeClassification(a.createtime,b.createtime)
         |""".stripMargin

    // personal audit time stat
    val sql_2_all =
      s"""
         |select
         |    t.dt,'all' as statType,b.operator,
         |    case when t.contenttype=1 then "文本"
         |     when t.contenttype=2 then "图片"
         |     when t.contenttype=3 then "音乐"
         |     when t.contenttype=4 then "视频"
         |     when t.contenttype=5 then "问答"
         |     when t.contenttype=6 then "长文章" end contentType,
         |     t.priority,
         |     count(distinct t.uuid) as auditNum,
         |     count(distinct t.postid) postNum,
         |     count(distinct case when b.status=3 then t.uuid else null end) as passNum,
         |     count(distinct  case when b.status=2 then t.uuid else null end) as blockNum,
         |     avg((b.createtime-a.createtime)/(1000*60)) avgTime
         |from
         |    anti_spam_copy t
         |join first_machine_audit a
         |     on t.uuid=a.uuid and t.dt=a.day
         |join first_person_audit b
         |     on  a.uuid=b.uuid and a.day=b.day
         |group by
         |    t.dt,b.operator,
         |    case when t.contenttype=1 then "文本"
         |         when t.contenttype=2 then "图片"
         |         when t.contenttype=3 then "音乐"
         |         when t.contenttype=4 then "视频"
         |         when t.contenttype=5 then "问答"
         |         when t.contenttype=6 then "长文章" end,
         |    t.priority
         |""".stripMargin

    // personal audit time stat
    val sql_2_machine_suspect =
      s"""
         |select
         |    t.dt,'suspect' as statType,b.operator,
         |    case when t.contenttype=1 then "文本"
         |     when t.contenttype=2 then "图片"
         |     when t.contenttype=3 then "音乐"
         |     when t.contenttype=4 then "视频"
         |     when t.contenttype=5 then "问答"
         |     when t.contenttype=6 then "长文章" end contentType,
         |     t.priority,
         |     count(distinct t.uuid) as auditNum,
         |     count(distinct t.postid) postNum,
         |     count(distinct case when b.status=3 then t.uuid else null end) as passNum,
         |     count(distinct  case when b.status=2 then t.uuid else null end) as blockNum,
         |     avg((b.createtime-a.createtime)/(1000*60)) avgTime
         |from
         |    anti_spam_copy t
         |join (
         |    select * from
         |        (select *,rank() over(partition by uuid order by createtime asc) rk from machine_audit where status=1 or (status=0 and reviewLater='true') ) a1 where rk=1
         |    ) a
         |     on t.uuid=a.uuid and t.dt=a.day
         |join first_person_audit b
         |     on  a.uuid=b.uuid and a.day=b.day
         |group by
         |    t.dt,b.operator,
         |    case when t.contenttype=1 then "文本"
         |         when t.contenttype=2 then "图片"
         |         when t.contenttype=3 then "音乐"
         |         when t.contenttype=4 then "视频"
         |         when t.contenttype=5 then "问答"
         |         when t.contenttype=6 then "长文章" end,
         |    t.priority
         |""".stripMargin

    // personal audit time stat
    val sql_2_machine_normal =
      s"""
         |select
         |    t.dt,'normal' as statType,b.operator,
         |    case when t.contenttype=1 then "文本"
         |     when t.contenttype=2 then "图片"
         |     when t.contenttype=3 then "音乐"
         |     when t.contenttype=4 then "视频"
         |     when t.contenttype=5 then "问答"
         |     when t.contenttype=6 then "长文章" end contentType,
         |     t.priority,
         |     count(distinct t.uuid) as auditNum,
         |     count(distinct t.postid) postNum,
         |     count(distinct case when b.status=3 then t.uuid else null end) as passNum,
         |     count(distinct  case when b.status=2 then t.uuid else null end) as blockNum,
         |     avg((b.createtime-a.createtime)/(1000*60)) avgTime
         |from
         |    anti_spam_copy t
         |join (
         |    select * from
         |        (select *,rank() over(partition by uuid order by createtime asc) rk from machine_audit where status=0) a1 where rk=1
         |    ) a
         |     on t.uuid=a.uuid and t.dt=a.day
         |join first_person_audit b
         |     on  a.uuid=b.uuid and a.day=b.day
         |group by
         |    t.dt,b.operator,
         |    case when t.contenttype=1 then "文本"
         |         when t.contenttype=2 then "图片"
         |         when t.contenttype=3 then "音乐"
         |         when t.contenttype=4 then "视频"
         |         when t.contenttype=5 then "问答"
         |         when t.contenttype=6 then "长文章" end,
         |    t.priority
         |""".stripMargin

    // personal audit time interval stats
    val sql_3_all =
      s"""
         |select
         |    t.dt,'all' as statType,b.operator,
         |    case when t.contenttype=1 then "文本"
         |     when t.contenttype=2 then "图片"
         |     when t.contenttype=3 then "音乐"
         |     when t.contenttype=4 then "视频"
         |     when t.contenttype=5 then "问答"
         |     when t.contenttype=6 then "长文章" end contentType,
         |     t.priority,
         |     callBackTimeClassification(a.createtime,b.createtime) as timeInterval,
         |     count(distinct t.uuid) as auditNum,
         |     count(distinct t.postid) postNum,
         |     count(distinct case when b.status=3 then t.uuid else null end) as passNum,
         |     count(distinct  case when b.status=2 then t.uuid else null end) as blockNum
         |from
         |    anti_spam_copy t
         |join first_machine_audit a
         |     on t.uuid=a.uuid and t.dt=a.day
         |join first_person_audit b
         |     on  a.uuid=b.uuid and a.day=b.day
         |group by
         |    t.dt,b.operator,
         |    case when t.contenttype=1 then "文本"
         |         when t.contenttype=2 then "图片"
         |         when t.contenttype=3 then "音乐"
         |         when t.contenttype=4 then "视频"
         |         when t.contenttype=5 then "问答"
         |         when t.contenttype=6 then "长文章" end,
         |    t.priority,
         |    callBackTimeClassification(a.createtime,b.createtime)
         |""".stripMargin

    // personal audit time interval stats
    val sql_3_machine_suspect =
      s"""
         |select
         |    t.dt,'suspect' as statType,b.operator,
         |    case when t.contenttype=1 then "文本"
         |     when t.contenttype=2 then "图片"
         |     when t.contenttype=3 then "音乐"
         |     when t.contenttype=4 then "视频"
         |     when t.contenttype=5 then "问答"
         |     when t.contenttype=6 then "长文章" end contentType,
         |     t.priority,
         |     callBackTimeClassification(a.createtime,b.createtime) as timeInterval,
         |     count(distinct t.uuid) as auditNum,
         |     count(distinct t.postid) postNum,
         |     count(distinct case when b.status=3 then t.uuid else null end) as passNum,
         |     count(distinct  case when b.status=2 then t.uuid else null end) as blockNum
         |from
         |    anti_spam_copy t
         |join (
         |    select * from
         |        (select *,rank() over(partition by uuid order by createtime asc) rk from machine_audit where status=1 or (status=0 and reviewLater='true') ) a1 where rk=1
         |    ) a
         |     on t.uuid=a.uuid and t.dt=a.day
         |join first_person_audit b
         |     on  a.uuid=b.uuid and a.day=b.day
         |group by
         |    t.dt,b.operator,
         |    case when t.contenttype=1 then "文本"
         |         when t.contenttype=2 then "图片"
         |         when t.contenttype=3 then "音乐"
         |         when t.contenttype=4 then "视频"
         |         when t.contenttype=5 then "问答"
         |         when t.contenttype=6 then "长文章" end,
         |    t.priority,
         |    callBackTimeClassification(a.createtime,b.createtime)
         |""".stripMargin

    val sql_3_machine_suspect_add_hour =
      s"""
         |select
         |    t.dt,t.hh,'suspect' as statType,b.operator,
         |    case when t.contenttype=1 then "文本"
         |     when t.contenttype=2 then "图片"
         |     when t.contenttype=3 then "音乐"
         |     when t.contenttype=4 then "视频"
         |     when t.contenttype=5 then "问答"
         |     when t.contenttype=6 then "长文章" end contentType,
         |     t.priority,
         |     callBackTimeClassification(a.createtime,b.createtime) as timeInterval,
         |     count(distinct t.uuid) as auditNum,
         |     count(distinct t.postid) postNum,
         |     count(distinct case when b.status=3 then t.uuid else null end) as passNum,
         |     count(distinct  case when b.status=2 then t.uuid else null end) as blockNum
         |from
         |    anti_spam_copy t
         |join (
         |    select * from
         |        (select *,rank() over(partition by uuid order by createtime asc) rk from machine_audit where status=1 or (status=0 and reviewLater='true') ) a1 where rk=1
         |    ) a
         |     on t.uuid=a.uuid and t.dt=a.day
         |join first_person_audit b
         |     on  a.uuid=b.uuid and a.day=b.day
         |group by
         |    t.dt,t.hh,b.operator,
         |    case when t.contenttype=1 then "文本"
         |         when t.contenttype=2 then "图片"
         |         when t.contenttype=3 then "音乐"
         |         when t.contenttype=4 then "视频"
         |         when t.contenttype=5 then "问答"
         |         when t.contenttype=6 then "长文章" end,
         |    t.priority,
         |    callBackTimeClassification(a.createtime,b.createtime)
         |""".stripMargin

    // personal audit time interval stats
    val sql_3_machine_normal =
      s"""
         |select
         |    t.dt,'normal' as statType,b.operator,
         |    case when t.contenttype=1 then "文本"
         |     when t.contenttype=2 then "图片"
         |     when t.contenttype=3 then "音乐"
         |     when t.contenttype=4 then "视频"
         |     when t.contenttype=5 then "问答"
         |     when t.contenttype=6 then "长文章" end contentType,
         |     t.priority,
         |     callBackTimeClassification(a.createtime,b.createtime) as timeInterval,
         |     count(distinct t.uuid) as auditNum,
         |     count(distinct t.postid) postNum,
         |     count(distinct case when b.status=3 then t.uuid else null end) as passNum,
         |     count(distinct  case when b.status=2 then t.uuid else null end) as blockNum
         |from
         |    anti_spam_copy t
         |join (
         |    select * from
         |        (select *,rank() over(partition by uuid order by createtime asc) rk from machine_audit where status=0) a1 where rk=1
         |    ) a
         |     on t.uuid=a.uuid and t.dt=a.day
         |join first_person_audit b
         |     on  a.uuid=b.uuid and a.day=b.day
         |group by
         |    t.dt,b.operator,
         |    case when t.contenttype=1 then "文本"
         |         when t.contenttype=2 then "图片"
         |         when t.contenttype=3 then "音乐"
         |         when t.contenttype=4 then "视频"
         |         when t.contenttype=5 then "问答"
         |         when t.contenttype=6 then "长文章" end,
         |    t.priority,
         |    callBackTimeClassification(a.createtime,b.createtime)
         |""".stripMargin

    spark.sql(sql_1)
      .repartition(1)
      .select("contentType","priority","timeInterval","auditNum","postNum","postVersionNum","operators","passNum","blockNum","dt")
      .write
      .mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_anti_spam_post_time_interval_stats_di")

    spark.sql(sql_2_all).union(spark.sql(sql_2_machine_suspect)).union(spark.sql(sql_2_machine_normal))
      .repartition(1)
      .select("statType","operator","contentType","priority","auditNum","postNum","passNum","blockNum","avgTime","dt")
      .write
      .mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_anti_spam_operator_time_stats_di")

    spark.sql(sql_3_all).union(spark.sql(sql_3_machine_suspect)).union(spark.sql(sql_3_machine_normal))
      .repartition(1)
      .select("statType","operator","contentType","priority","timeInterval","auditNum","postNum","passNum","blockNum","dt")
      .write
      .mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_anti_spam_operator_time_interval_stats_di")

    spark.sql(sql_3_machine_suspect_add_hour)
      .repartition(1)
      .select("statType","hh","operator","contentType","priority","timeInterval","auditNum","postNum","passNum","blockNum","dt")
      .write
      .mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_anti_spam_suspect_time_interval_stats_hour_di")

    spark.sql(sql_zq_res_dd)
      .repartition(1)
      .select("contenttype","priority","uuid_num","operator_uv","person_audit_pass_num","person_audit_fail_num","overall_callback_time","normal_callback_time","force_callback_time",
        "overall_total_time","overall_total_uuid_number","overall_total_operator","normal_total_time","normal_total_uuid_number","normal_total_operator",
        "force_total_time","force_total_uuid_number","force_total_operator","dt")
      .write
      .mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_anti_spam_summary_stats_di")

    spark.sql(sql_zq_res_hh)
      .repartition(1)
      .select("hh","contenttype","priority","uuid_num","operator_uv","person_audit_pass_num","person_audit_fail_num","overall_callback_time","normal_callback_time","force_callback_time",
        "overall_total_time","overall_total_uuid_number","overall_total_operator","normal_total_time","normal_total_uuid_number","normal_total_operator",
        "force_total_time","force_total_uuid_number","force_total_operator","dt")
      .write
      .mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_anti_spam_hour_summary_stats_di")

    spark.close()

  }

  def callBackTimeClassification(time1: Long, time2: Long): String = {
    val timeDiff = (time2 - time1)/1000
    if (timeDiff ==0)  "0秒"
    else if (timeDiff > 0 && timeDiff <= 10)  "(0,10]秒"
    else if (timeDiff > 10 && timeDiff <= 30)  "(10,30]秒"
    else if (timeDiff > 30 && timeDiff <= 60)  "(30,60]秒"
    else if (timeDiff > 60 && timeDiff <= 120)  "(1,2]分钟"
    else if (timeDiff > 120 && timeDiff <= 180)  "(2,3]分钟"
    else if (timeDiff > 180 && timeDiff <= 300)  "(3,5]分钟"
    else if (timeDiff > 300 && timeDiff <= 600)  "(5,10]分钟"
    else if (timeDiff > 600 && timeDiff <= 900)  "(10,15]分钟"
    else if (timeDiff > 900 && timeDiff <= 1800)  "(15,30]分钟"
    else if (timeDiff > 1800 && timeDiff <= 2700)  "(30,45]分钟"
    else if (timeDiff > 2700 && timeDiff <= 3600)  "(45,60]分钟"
    else if (timeDiff > 3600 && timeDiff <= 5400)  "(60,90]分钟"
    else if (timeDiff > 5400)  "大于90分钟"
    else  "else"
  }

}
