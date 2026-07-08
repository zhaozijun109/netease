package com.netease.lofter.data.jobs.backend

import com.github.nscala_time.time.Imports.DateTime
import com.netease.lofter.data.common.databases.getRecDDBConn
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

import java.sql.Connection

object TrafficSensingIpPercentileExport {
  val BATCH_SIZE = 500

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val yesterday = DateTime.yesterday.toString("yyyy-MM-dd")
    val date = pargs.optional("date").getOrElse(yesterday)

    val pushThresholdDataset =
      s"""
         |select nvl(level,'NEW') as blog_level, ip as main_ip,
         |       cast(percentile['1'] as string) as action_type, 'percentile:share' as push_type, cast(percentile['0'] as bigint) as thresholds
         |from lofter_dm.ads_ip_traffic_sensing_percentile_di
         |     lateral view explode(arrays_zip(split(share_percentile,','), array(0.5,0.55,0.6,0.65,0.7,0.75,0.8,0.85,0.9,0.95,1))) t as percentile
         |where dt = '$date' and post_count > 10
         |
         |union all
         |
         |select nvl(level,'NEW') as blog_level, ip as main_ip,
         |       cast(percentile['1'] as string) as action_type, 'percentile:recommend' as push_type, cast(percentile['0'] as bigint) as thresholds
         |from lofter_dm.ads_ip_traffic_sensing_percentile_di
         |     lateral view explode(arrays_zip(split(rec_percentile,','), array(0.5,0.55,0.6,0.65,0.7,0.75,0.8,0.85,0.9,0.95,1))) t as percentile
         |where dt = '$date' and post_count > 10
         |
         |union all
         |
         |select nvl(level,'NEW') as blog_level, ip as main_ip,
         |       cast(percentile['1'] as string) as action_type, 'percentile:hot' as push_type, cast(percentile['0'] as bigint) as thresholds
         |from lofter_dm.ads_ip_traffic_sensing_percentile_di
         |     lateral view explode(arrays_zip(split(hot_percentile,','), array(0.5,0.55,0.6,0.65,0.7,0.75,0.8,0.85,0.9,0.95,1))) t as percentile
         |where dt = '$date' and post_count > 10
         |""".stripMargin

    import com.netease.lofter.data.common.spark.SparkSqlImplicits._
    import com.netease.wm.util.Sql._
    implicit val conn: Connection = getRecDDBConn

    sql"""delete from rec_data_author_push_thresholds where push_type = 'percentile:share' """.execute()
    sql"""delete from rec_data_author_push_thresholds where push_type = 'percentile:recommend' """.execute()
    sql"""delete from rec_data_author_push_thresholds where push_type = 'percentile:hot' """.execute()

    val results = spark.sql(pushThresholdDataset).collect()

    results.toSeq.grouped(BATCH_SIZE).foreach { rows =>
      val batch = rows.map(rowParam _)
      sql"insert into rec_data_author_push_thresholds(blog_level, main_ip, action_type, push_type, thresholds) values(${"blog_level"}, ${"main_ip"}, ${"action_type"}, ${"push_type"}, ${"thresholds"})".batchUpdate(batch)
    }

    spark.close()
  }
}
