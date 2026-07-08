package com.netease.lofter.etl.ads

import com.github.nscala_time.time.Imports
import com.github.nscala_time.time.Imports.DateTime
import com.netease.lofter.etl.common.spark.CommonUtil.versionCompare
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}

object SearchPasswordDeviceStats {

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Search Query Device Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(Imports.DateTime.yesterday.toString("yyyy-MM-dd"))

    spark.udf.register("version_compare", versionCompare _)

    val retainDays = Seq(1,6,14,29)
    deviceActiveStats(spark,date)

    // stat the retain ratio for 1DaysAgo, 3DaysAgo, 7DaysAgo
    for (daysAgo <- retainDays){
      updateActiveDeviceRetain(spark, date, daysAgo)
    }

    def  searchPasswordQueryDetail(spark: SparkSession, date: String): Unit = {
      val oneMonthAgo = DateTime.parse(date).minusDays(29).toString("yyyy-MM-dd")

      val sql_intervene_word =
        s"""
           |select distinct dt, deviceUdid,occur_time as occurTime, userId, search_query as queryname
           |from lofter.dwd_search_action_di
           |where dt = '$date' and
           |      eventId in ('b5-12','b5-13','b5-24','b5-27','b5-28') and
           |      version_compare(app_version, '6.25.0') >= 0
           |""".stripMargin

      val sql_search_content =
        s"""
           |select dt, deviceudid, search_query as queryname,
           |       itemid, itemType, eventId, occur_time as occurTime, userId
           |from lofter.dwd_search_action_di
           |where dt = '$date' and
           |      eventId in ('b5-27','b5-28') and
           |      search_query is not null
           |""".stripMargin

      spark.sql(sql_intervene_word).createOrReplaceTempView("interveneWord")
      spark.sql(sql_search_content).createOrReplaceTempView("searchContent")

      val sql_result =
        s"""
           |select a.deviceudid,a.dt,a.new_flag,a.return_flag,a.queryname,a.occurtime,'搜索设备' as type,link,onelevelchannel as level1_channel
           |from (
           |    select a.deviceudid,a.dt,case when b.deviceudid is null then 0 else 1 end as new_flag,
           |         case when c.deviceudid is null then 0 else 1 end as return_flag,
           |         queryname,occurtime,d.link,d.onelevelchannel,
           |         rank()over(partition by a.deviceudid,a.dt order by occurtime asc) as rk
           |    from (
           |      select deviceudid,dt,queryname ,occurtime,userid from interveneWord
           |    ) a join (
           |      select distinct actpwd,link,channel as oneLevelChannel, actpwd_start_time as startTime, actpwd_end_time as endTime
           |      from lofter.dwd_growth_actpwd_access_di
           |      where dt between '$oneMonthAgo' and '$date' and actpwd_type = 2
           |    ) d on a.queryName = d.actpwd and  a.occurTime between  d.startTime and d.endTime
           |    left join (
           |       select deviceudid,dt from lofter.device_new  where dt='$date'  group by deviceudid,dt
           |    ) b on a.deviceudid = b.deviceudid and a.dt=b.dt
           |    left join (
           |      select deviceudid,dt from lofter.device_return  where dt='$date' and period=30  group by deviceudid,dt
           |    ) c on a.deviceudid=c.deviceudid and a.dt=c.dt
           |) a
           |where a.rk=1
           |union all
           |select a.deviceudid,a.dt,a.new_flag,a.return_flag,a.queryname,a.occurtime,'点击内容' as type,link,onelevelchannel as level1_channel
           |from (
           |    select a.deviceudid,a.dt,case when b.deviceudid is null then 0 else 1 end as new_flag,
           |         case when c.deviceudid is null then 0 else 1 end as return_flag,
           |         queryname,occurtime,d.link,d.onelevelchannel,
           |         rank()over(partition by a.deviceudid,a.dt order by occurtime asc) as rk
           |    from (
           |      select deviceudid,dt,queryname ,occurtime,userid , eventid from searchContent where eventid='b5-28'
           |    ) a join (
           |      select distinct actpwd,link, channel as oneLevelChannel, actpwd_start_time as startTime, actpwd_end_time as endTime
           |      from lofter.dwd_growth_actpwd_access_di
           |      where dt between '$oneMonthAgo' and '$date' and actpwd_type = 2
           |    ) d on a.queryName = d.actpwd and a.occurTime between  d.startTime and d.endTime
           |    left join (
           |      select deviceudid,dt from lofter.device_new  where dt='$date'  group by deviceudid,dt
           |    ) b on a.deviceudid=b.deviceudid and a.dt=b.dt
           |    left join (
           |      select deviceudid,dt from lofter.device_return  where dt='$date' and period=30  group by deviceudid,dt
           |    ) c on a.deviceudid=c.deviceudid and a.dt = c.dt
           |) a
           |where a.rk=1
           |""".stripMargin

      spark.sql(sql_result).createOrReplaceTempView("searchPasswordQueryDetail")
    }

    def deviceActiveStats(spark:SparkSession, date: String): Unit = {
      searchPasswordQueryDetail(spark, date)
      val sql_result =
        s"""
           |select type,new_flag as new_flag,return_flag,queryname as query_name,link,level1_channel,
           |    count(distinct deviceudid) as uv
           |from  searchPasswordQueryDetail
           |group by 1,2,3,4,5,6
           |""".stripMargin

      spark.sql(sql_result).withColumn("dt", lit(date))
        .write.mode(SaveMode.Overwrite)
        .insertInto("lofter_dm.ads_search_password_device_di")
    }

    def updateActiveDeviceRetain(spark: SparkSession, date: String, daysAgo: Int): Unit = {
      val newDay = DateTime.parse(date).minusDays(daysAgo).toString("yyyy-MM-dd")
      searchPasswordQueryDetail(spark, newDay)

      val sql_retain =
        s"""
           |select '$newDay' as baseDate,a.type,a.new_flag,a.return_flag,a.queryname as query_name,a.link,a.level1_channel,
           |    count(distinct b.deviceudid) as retain_uv
           |from searchPasswordQueryDetail a
           |inner join
           |(select deviceudid,dt from lofter.device_active where dt='$date' group by deviceudid,dt) b
           |on a.deviceudid=b.deviceudid 
           |group by 1,2,3,4,5,6,7
           |""".stripMargin

      spark.sql(sql_retain)
        .withColumn("dt", lit(date))
        .withColumn("period", lit(daysAgo))
        .write.mode(SaveMode.Overwrite)
        .insertInto("lofter_dm.ads_search_password_device_retain_di")
    }

    spark.close()

  }
}
