package com.netease.lofter.etl.youdata

import com.github.nscala_time.time.Imports
import com.netease.wm.util.Args
import org.apache.spark.sql.{SaveMode, SparkSession}

object NotePageSourceStats {

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Note Page Source Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .enableHiveSupport()
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(Imports.DateTime.yesterday.toString("yyyy-MM-dd"))

    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")

    val sql_event_list =
      s"""
         |select *
         |from
         |(
         |select dt,deviceudid,deviceos,appversion,occurtime,eventid,itemid,scene,rn,
         |  lead(eventid,1,null) over (partition by deviceudid order by rn) as next_eventid,
         |  lead(scene,1,null) over (partition by deviceudid order by rn) as next_scene,
         |  lead(occurtime,1,null) over (partition by deviceudid order by rn) as next_occurtime,
         |  lead(itemid,1,null) over (partition by deviceudid order by rn) as next_itemid
         |from
         |(
         |select t1.dt,t1.deviceudid,t1.deviceos,t1.appversion,t1.occurtime,t1.eventid,
         |   case when t1.eventid='g1-8' and itemid is not null then itemid else params['tid'] end itemid,
         |   case when t1.scene='rec_search' then 'search' ----修正错误数据
         |        when t1.eventid='g1-41' then 'related_item' ----相关文章入口
         |        when t1.scene is not null then t1.scene
         |        when t1.eventid='h1-7' then 'collection' ---修正历史数据，下同
         |        when t1.eventid in('k1-2','k1-4') then 'search'
         |        when t1.eventid like 'a3%' then 'subscribe'
         |        when t1.eventid in('e12-3','e12-4') then 'mycollect_colt'
         |        when t1.eventid in('e2-44','e2-45','e2-46','e2-9') then 'homepage'
         |        when t1.eventid='g1-8' then 'note'
         |        when t1.eventid='f1-33' then 'tag'
         |        when t1.scene is null and t1.category is not null then t1.category else null end scene,
         |    row_number()over(partition by t1.dt,t1.deviceudid order by occurtime) rn
         |from
         |(
         |select *
         |from lofter.ods_mda_app_partition_di
         |where dt='$date')t1
         |join
         |(
         |---埋点配置信息
         |select eventid,driveHref
         |from
         |(
         |select pointNo eventid,driveHref,row_number()over(partition by pointNo order by updateTime desc) updaterank
         |from lofter_db_dump.ods_db_statis_point_nd
         |where status=0
         |)a
         |where updaterank=1
         |)t2
         |on t1.eventid=t2.eventid
         |where t2.driveHref=1   ---所有可以驱动跳转的点
         |or t1.eventid in('k1-2','k1-4','b6-4') ---搜索
         |or t1.eventid in('a3-1','a3-2','a3-3','a3-4','a3-5','a3-27','a3-28','a3-29','a3-31','a3-32','a3-34')---订阅
         |or t1.eventid in('e2-9','h1-7') ---九宫格点击卡片
         |or t1.eventid in('e12-3','e12-4') ---我的收藏-合集
         |or t1.eventid in('b1-46','b1-49','b1-53','g1-41') ---发现页、领域页、相关文章
         |or t1.eventid in('e2-44','e2-45','e2-46','e2-9') ----个人主页，怎么区分是自己的？无法区分
         |or t1.eventid in('g1-8','f1-33')  ---单日志页、标签结果页
         |)t3
         |where eventid='g1-8' or (eventid<>'g1-8' and scene<>'note')
         |)t4
         |where (eventid='g1-8' and next_eventid='g1-8' and next_itemid is not null and itemid<>next_itemid)
         |or (eventid='g1-8' and next_eventid='g1-8' and next_itemid is null)
         |or (eventid='g1-8' and next_eventid<>'g1-8')
         |or (eventid='g1-8' and next_eventid is null)
         |or (eventid<>'g1-8' and next_scene is null)
         |or (eventid<>'g1-8' and next_scene is not null and scene<>next_scene)
         |""".stripMargin

    spark.sql(sql_event_list).createOrReplaceTempView("t1")

    val sql_res =
      s"""
         |select dt,deviceos,appversion,first_source,second_source,sum(1) notepv
         |from
         |(
         |select dt,deviceos,appversion,deviceudid,scene,occurtime,rn,
         |  case when first_source is null then 'other' else first_source end first_source,first_visitrank,
         |  case when first_source is null then second_source
         |       when first_source is not null and second_source is not null and second_visitrank>first_visitrank then second_source else null end second_source
         |from
         |(
         |select dt,deviceos,appversion,deviceudid,scene,occurtime,rn,
         |last_value(if(scene not in('collection','tag','homepage','note'),scene,null),true)over(partition by dt,deviceudid order by rn) first_source,
         |last_value(if(scene not in('collection','tag','homepage','note'),rn,null),true)over(partition by dt,deviceudid order by rn) first_visitrank,
         |last_value(if(scene in('collection','tag','homepage'),scene,null),true)over(partition by dt,deviceudid order by rn) second_source,
         |last_value(if(scene in('collection','tag','homepage'),rn,null),true)over(partition by dt,deviceudid order by rn) second_visitrank
         |from t1
         |)a
         |)b
         |where scene='note'
         |group by dt,deviceos,appversion,first_source,second_source
         |""".stripMargin

    spark.sql(sql_res)
      .repartition(1)
      .select("deviceos", "appversion", "first_source", "second_source", "notepv", "dt")
      .write
      .mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_note_page_source_stat_di")

    spark.stop()

  }

}
