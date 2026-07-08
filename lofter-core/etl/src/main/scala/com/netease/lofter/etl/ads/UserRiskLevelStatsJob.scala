package com.netease.lofter.etl.ads

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}

object UserRiskLevelStatsJob {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Risk Level Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))
    val halfYearAgo = DateTime.parse(date).minusMonths(6).toString("yyyy-MM-dd")

    val sql_risk_user =
      s"""
         |select a.dt,a.userid,
         |       case when a.shihou_flag=1 and a.shizhong_flag=1  then '双模型命中'
         |       when a.shihou_flag=1 and a.shizhong_flag=0  then '事后模型单命中'
         |       else  '事中模型单命中' end as type
         |from
         |
         |(select a.dt,a.userid,
         |       count(if(a.source='shihou', 1, null)) as shihou_flag,
         |       count(if(a.source='shizhong', 1, null)) as shizhong_flag
         |from
         |(
         |select b.dt,b.publisheruserid as userid ,'shihou' as source  ---事后模型输出用户
         |from
         |       (select distinct dt,publisheruserid,postid
         |       from
         |       lofter.dwd_suspect_shuare_model_di
         |       where dt between '$halfYearAgo' and '$date' ) a
         |join
         |       (select distinct postid,  publisheruserid ,from_unixtime(cast(optime / 1000 AS BIGINT), 'yyyy-MM-dd') as dt
         |       from  lofter_db_dump.ods_db_post_hot_nd
         |       where    type in (1,2,3,4)
         |       and from_unixtime(cast(optime / 1000 AS BIGINT), 'yyyy-MM-dd') between '$halfYearAgo' and '$date'
         |       ) b
         |on a.postid=b.postid and a.publisheruserid=b.publisheruserid and a.dt between date_sub(b.dt,6) and b.dt
         |group by 1,2,3
         |
         |union
         |select dt, userid ,'shizhong' as source  ---事中模型输出用户
         |from
         |lofter.ods_log_anti_risk_shuare_di
         |where dt between '$halfYearAgo' and '$date' and rgrisk='reject' and actiontype='like' and name='事中嫌疑刷热'
         |group by 1,2,3
         |) a
         |group by  a.dt,a.userid
         |) a
         |group by 1,2,3
      """.stripMargin

    spark.sql(sql_risk_user).createOrReplaceTempView("riskUsers")

    val sql_risk_user_level =
      s"""
         |select  a.userId,a.type as userType,a.source
         |from
         |(select a.userid,a.source,a.type,rank()over(partition by a.userid order by a.sourceflg asc) as rk
         |from
         |(select case when source='robot' then 1 when source='human' then 2 else 3 end as sourceflg,a.userid,a.type,a.source
         |from
         |      (
         |      select a.userid,
         |      case when a.flg=0 and days>=20 then '白'
         |      when a.flg=1 and days>=30 then '白'
         |      when a.flg=0 and days<20 then '灰'
         |      when a.flg=1 and days<30 then '黑'
         |      end as type,'model' as source                       ----模型用户
         |      from
         |      (select a.userid, a.flg,datediff('$date',a.dt) as days
         |      from
         |          (select a.userid,max(a.flg) as flg,max(a.dt) as dt
         |          from
         |              (select dt,userid,case when type='双模型命中' then 1 else 0 end as flg
         |              from riskUsers
         |              group by 1,2,3 ) a
         |          group by a.userid
         |          ) a
         |      ) a
         |
         |      union
         |      select a.userid ,'白' as type,'robot' as source       ----机器人用户
         |      from
         |      (select userId from lofter_db_dump.ods_db_media_account_import_nd where platformType in (0,2) group by userid
         |      union
         |      select blogid as userId from lofter_db_dump.ods_db_robot_blog_info_nd group by blogid) a
         |      group by 1,2,3
         |
         |      union
         |      select userid,type,'human' as source               -----人工导入名单管理用户
         |      from lofter.zq_lofter_shuare_user_black_white_zeppelin
         |      group by 1,2,3
         |     )  a
         |  ) a
         |) a
         |where a.rk=1
         |group by a.userid,a.type,a.source
         |""".stripMargin

    spark.sql(sql_risk_user_level)
      .withColumn("dt", lit(date))
      .repartition(1)
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter.dwd_risk_user_level_dd")

    spark.stop()
  }
}
