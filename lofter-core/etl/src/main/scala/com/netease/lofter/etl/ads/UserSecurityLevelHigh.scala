package com.netease.lofter.etl.ads

import com.github.nscala_time.time.Imports._
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}

object UserSecurityLevelHigh {
  val ANTI_RISK_COPY_LOG_START = "2020-11-15"

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter User Security Level Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))

    val sql_grade_update_blog =
      s"""
         |select  a.blogid
         |from
         |
         |(select blogid   from lofter_db_dump.ods_db_authenticate_blog_nd group by blogid
         |union all
         |select blogid from   lofter_db_dump.ods_db_verify_blog_nd group by blogid
         |union
         |select blogid
         |from lofter_db_dump.ods_db_user_following_nd
         |group by blogid having count(distinct userid)>=1000
         |union
         |select a.blogid
         |from
         |(select a.blogid,sum(b.hot) as hot
         |from
         |    (select id as postid ,blogid
         |    from lofter_db_dump.ods_db_post_nd
         |    where valid=0 and allowview=0) a
         |    join
         |    (select blogid,postid,count(1) as hot
         |     from lofter_db_dump.ods_db_post_hot_nd
         |     where from_unixtime(cast(optime / 1000 AS BIGINT), 'yyyy-MM-dd')>='2020-01-01'
         |     and type between 1 and 4
         |     group by blogid,postid) b
         | on a.postid=b.postid and a.blogid=b.blogid
         |group by a.blogid)a
         |where a.hot>=1000)a
         |
         |join
         |(select dt, blogid  from lofter.dws_user_security_level_di where dt='$date' and grade=0 group by dt, blogid ) b
         |on a.blogid=b.blogid
         |join
         |
         |(select a.blogid
         |from
         |(select a.blogid,a.num,case when b.num is null then 0 else b.num end as unpassnum
         |from
         |(select a.blogid,count(distinct a.bussinessid) as num,a.bizinfo as type
         |from
         |    (select blogid,bussinessid ,bizinfo
         |       from lofter.ods_log_anti_spam_copy_di
         |       where  category='post' and hist=0 and version=1  and
         |       from_unixtime(cast(createtime/1000 as bigint),'yyyy-MM-dd') between '$ANTI_RISK_COPY_LOG_START' and  '$date'
         |        and bizinfo='genPostLog' and dt between '$ANTI_RISK_COPY_LOG_START' and  '$date'
         |       group by blogid,bussinessid,bizinfo  ) a
         |join
         |(select b.postid,b.blogid,b.status
         |from
         |(select postid,blogid,status,version  ,createtime,rank()over(partition by postid,blogid order by version desc,  createtime desc ) as rk
         |from lofter_db_dump.ods_db_risk_antispam_callback_record_nd where type=0 and   version=1 and status in (0,2,3)  ) b
         |where b.rk=1) b
         |on a.bussinessid=b.postid and a.blogid=b.blogid
         |group by a.blogid,a.bizinfo) a
         |left join
         |(select a.blogid,count(distinct a.bussinessid) as num,a.bizinfo as type
         |from
         |      (select  blogid,bussinessid ,bizinfo
         |       from lofter.ods_log_anti_spam_copy_di
         |       where  category='post' and hist=0 and version=1  and
         |       from_unixtime(cast(createtime/1000 as bigint),'yyyy-MM-dd') between '$ANTI_RISK_COPY_LOG_START' and  '$date'
         |       and bizinfo='genPostLog' and dt between '$ANTI_RISK_COPY_LOG_START' and  '$date'
         |       group by blogid,bussinessid,bizinfo  ) a
         |join
         |(select b.postid,b.blogid,b.status
         |from
         |(select postid,blogid,status,version ,createtime,rank()over(partition by postid,blogid order by version desc,  createtime desc ) as rk
         |from lofter_db_dump.ods_db_risk_antispam_callback_record_nd
         |where type=0 and   version=1 and status in (0,2,3)  ) b
         |where b.rk=1 and  b.status=2) b
         |on a.bussinessid=b.postid and a.blogid=b.blogid
         |group by a.blogid,a.bizinfo) b
         |on a.blogid=b.blogid and a.type=b.type) a
         |where a.unpassnum/a.num <=0.01
         |group by  a.blogid) c
         |on c.blogid=b.blogid
         |
         |join
         |(select e.blogid from
         |(select blogid, count(id) as num
         | from lofter_db_dump.ods_db_post_nd
         | where ispublished=1 and valid=0 and allowview=0 and citerootpostid=0  and type!=5  group by blogid  ) e
         |where e.num>=30) e
         |on e.blogid=b.blogid
         |group by a.blogid
         |""".stripMargin

    spark.sql(sql_grade_update_blog)
      .withColumn("dt", lit(date))
      .repartition(1)
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_high_security_user_di")

    spark.close()
  }
}
