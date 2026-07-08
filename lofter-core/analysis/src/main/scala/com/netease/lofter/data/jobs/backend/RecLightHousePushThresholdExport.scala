package com.netease.lofter.data.jobs.backend

import com.netease.lofter.data.common.databases.getRecDDBConn
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

import java.sql.Connection

object RecLightHousePushThresholdExport {
  val BATCH_SIZE = 500

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val pushThresholdDataset =
      s"""
         |select blog_level, click_count_threshold
         |from rec.rec_data_ecology_light_lighthouse_v2_threshold
         |""".stripMargin

    import com.netease.lofter.data.common.spark.SparkSqlImplicits._
    import com.netease.wm.util.Sql._
    implicit val conn: Connection = getRecDDBConn

    sql"""delete from rec_data_lighthouse_push_thresholds """.execute()

    val results = spark.sql(pushThresholdDataset).collect()

    results.toSeq.grouped(BATCH_SIZE).foreach { rows =>
      val batch = rows.map(rowParam _)
      sql"insert into rec_data_lighthouse_push_thresholds(blog_level, click_count_threshold) values(${"blog_level"}, ${"click_count_threshold"})".batchUpdate(batch)
    }

    spark.close()
  }
}
