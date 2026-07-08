package com.netease.lofter.etl.ads

import com.github.nscala_time.time.Imports
import com.netease.lofter.etl.common.spark.CommonUtil.versionCompare
import com.netease.wm.util.Args
import org.apache.spark.sql.{SaveMode, SparkSession}

object SearchWordStatsByUserType {

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Search Word Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(Imports.DateTime.yesterday.toString("yyyy-MM-dd"))
    spark.udf.register("version_compare", versionCompare _)

    val sql_mda_search =
      s"""
         |select dt,deviceudid,deviceOs,occur_time as occurTime,eventId,itemType, search_query as queryName, tab
         |from lofter.dwd_search_action_di
         |where dt = '$date' and eventId in ('b5-10','b5-12','b5-13','b5-20') and
         |      version_compare(app_version, '6.16.0') >= 0
         |""".stripMargin

    spark.sql(sql_mda_search).cache().createOrReplaceTempView("searchMda")

    val sql_search_no_result =
      s"""
         |select a.dt, a.deviceudid, a.deviceOs, a.queryname
         |from (
         |    select distinct dt, deviceudid, deviceOs, queryName
         |    from searchMda
         |    where eventId in ('b5-13','b5-12')
         |) a
         |join (
         |    select distinct dt, deviceudid, queryName
         |    from searchMda
         |    where eventId in ('b5-20') and tab = 'all'
         |) b on a.dt=b.dt and a.deviceudid = b.deviceudid and a.queryname = b.queryname
         |""".stripMargin
    
    val sql_search_has_result =
      s"""
         |select a.dt,a.deviceos,a.deviceudid as search_deviceudid,a.queryName, b.deviceudid as click_deviceudid ,b.occurtime
         |from (
         |    select distinct dt, deviceOs, deviceudid, queryName from searchMda
         |) a
         |left join (
         |    select b.dt,b.deviceudid,b.queryname,b.occurTime
         |    from (
         |        select dt,deviceudid, occurTime, queryName
         |        from searchMda
         |        where eventId in ('b5-10','b5-12')
         |     ) b
         |     left join (
         |         select distinct dt, deviceudid, queryName
         |         from searchMda
         |         where eventId in ('b5-20') and tab = 'all'
         |     ) c on b.dt=c.dt and b.queryName=c.queryName and b.deviceudid=c.deviceudid
         |     where c.queryname is null
         |) b on a.dt=b.dt and a.deviceudid=b.deviceudid and a.queryname=b.queryname
         |""".stripMargin

    val sql_user_type =
      s"""
         |select distinct a.dt,a.deviceudid,
         |       case when allflg in ('单账号匿名用户') then '匿名'
         |            when allflg in ('单账号注册新用户','双账号前匿名后注册新用户','双账号双注册前新后老用户') then '新注册'
         |            else '老注册' end as userType
         |from
         |(
         |select  dt,deviceudid,
         |              case when flg1=0 and flg2=1 and flg4=1  then '单账号注册新用户'
         |              when flg1=0 and flg2=1 and flg4=0  then '单账号注册老用户'
         |              when flg1=0 and flg2=0   then '单账号匿名用户'
         |              when flg1=1 and flg2=0 and flg3=1 and flg5=1 then '双账号前匿名后注册新用户'
         |              when flg1=1 and flg2=0 and flg3=1 and flg5=0 then '双账号前匿名后注册老用户'
         |              when flg1=1 and flg2=1 and flg3=1 and flg4=0 and flg5=0 then '双账号双注册前老后老用户'
         |              when flg1=1 and flg2=1 and flg3=1 and flg4=1 and flg5=0 then '双账号双注册前新后老用户'
         |              else '其他' end allflg
         |from
         |
         |      (select  dt, case when (a.userid=a.finaluserid or (a.userid is null and a.finaluserid is null)  )then 0 else 1 end as flg1,
         |                  case when b.isanonymous =0 then 1 else 0 end as flg2,
         |                  case when c.isanonymous =0 then 1 else 0 end as flg3,
         |                  case when (b.createdate=a.dt or b.createdate is null) then 1 else 0 end as flg4,
         |                  case when (c.createdate=a.dt or c.createdate is null) then 1 else 0 end as flg5,
         |              a.deviceudid,a.userid,a.finaluserid
         |      from
         |            (select dt, deviceudid,userid,finaluserid from lofter.device_new where dt = '$date'  ) a
         |            left join
         |            (select id,createdate,isanonymous from  lofter.dim_user) b
         |            on a.userid=b.id
         |
         |            left join
         |            (select id,createdate,isanonymous from  lofter.dim_user ) c
         |            on a.finaluserid=c.id ) a
         |) a
         |where a.allflg in ('单账号注册新用户','单账号注册老用户','单账号匿名用户','双账号前匿名后注册新用户','双账号前匿名后注册老用户','双账号双注册前老后老用户','双账号双注册前新后老用户')
         |""".stripMargin

    spark.sql(sql_search_no_result).createOrReplaceTempView("searchNoResult")
    spark.sql(sql_search_has_result).createOrReplaceTempView("searchResult")
    spark.sql(sql_user_type).createOrReplaceTempView("userTypeResult")

    val sql_of_search_no_result =
      s"""
         |select a.dt,a.deviceOs, b.userType, a.queryName, count(distinct a.deviceudid) as deviceNum
         |from
         |searchNoResult a
         |left join
         |userTypeResult b
         |on a.dt=b.dt and a.deviceudid=b.deviceudid
         |group by 1,2,3,4
         |""".stripMargin

    val sql_of_search_result =
      s"""
         |select a.dt,a.deviceos,b.userType,a.queryName,
         |        count(distinct a.search_deviceudid) as searchDeviceNum,
         |        count(distinct a.click_deviceudid) as clickDeviceNum,
         |        count(distinct concat(a.click_deviceudid,a.occurtime)) as clickNum
         |from
         |searchResult a
         |left join
         |userTypeResult b
         |on a.dt=b.dt and a.search_deviceudid=b.deviceudid
         |group by 1,2,3,4
         |""".stripMargin

    spark.sql(sql_of_search_no_result)
      .repartition(5)
      .select("deviceOs","userType","queryName","deviceNum","dt")
      .write
      .mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_search_no_result_stats_di")

    spark.sql(sql_of_search_result)
      .repartition(5)
      .select("deviceOs","userType","queryName","searchDeviceNum","clickDeviceNum","clickNum","dt")
      .write
      .mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_search_click_result_stats_di")

    spark.close()

  }

}
