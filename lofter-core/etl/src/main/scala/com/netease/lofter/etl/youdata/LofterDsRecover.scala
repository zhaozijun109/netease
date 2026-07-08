package com.netease.lofter.etl.youdata

import com.github.nscala_time.time.Imports
import com.netease.wm.util.Args
import org.apache.spark.sql.{Row, SparkSession}

object LofterDsRecover {

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Hubble Result Stats")
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(Imports.DateTime.yesterday.toString("yyyy-MM-dd"))
    val last1_day = Imports.DateTime.parse(date).minusDays(1).toString("yyyy-MM-dd")
    val last2_day = Imports.DateTime.parse(date).minusDays(2).toString("yyyy-MM-dd")

    val recoverType = Seq("BrowsePage","praise","recommend","reproduce","subscribe","publishpost","follow","commend","message")

    spark.read.json(s"/user/da_lofter/datastream/tomcat/online/$date").createOrReplaceTempView("tomcat_online")

    val sql_json =
      s"""
         |select * from (select a.body as body,b.* from tomcat_online a lateral view json_tuple(a.body,'time','type') b as time,type) c where time is not null and type in("BrowsePage","praise","recommend","reproduce","subscribe","publishpost","follow","commend","message")
       """.stripMargin

/*    val sql_json_test =
      s"""
         |select type,count(1) from (select a.body as body,b.* from tomcat_online a lateral view json_tuple(a.body,'time','action','type') b as time,action,type) c where time is not null group by type
       """.stripMargin*/

    spark.sql(sql_json).foreachPartition{ x: Iterator[Row] =>
      if (x.hasNext){
        val recType = x.next().fieldIndex("type")

      }
    }
    spark.sql(sql_json).createOrReplaceTempView("tb1")

    for (rcType <- recoverType) {

      val sql_recover_today =
        s"""
           |select body from tb1 where from_unixtime(cast(time/1000 as bigint),'yyyy-MM-dd')="$date" and type="$rcType"
       """.stripMargin

      val sql_recover_last1Day =
        s"""
           |select body from tb1 where from_unixtime(cast(time/1000 as bigint),'yyyy-MM-dd')="$last1_day" and type="$rcType"
       """.stripMargin

      val sql_recover_last2Day =
        s"""
           |select body from tb1 where from_unixtime(cast(time/1000 as bigint),'yyyy-MM-dd')="$last2_day" and type="$rcType"
       """.stripMargin

      spark.sql(sql_recover_today).repartition(1).write.mode("overwrite").text(s"/user/da_lofter/wj/lofterDs/$rcType/$date")
      spark.sql(sql_recover_last1Day).repartition(1).write.mode("overwrite").text(s"/user/da_lofter/wj/lofterDs/$rcType/$last1_day")
      spark.sql(sql_recover_last2Day).repartition(1).write.mode("overwrite").text(s"/user/da_lofter/wj/lofterDs/$rcType/$last2_day")
    }

    spark.close()

  }

}
