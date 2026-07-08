package com.netease.lofter.data.jobs.backend

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col, lit, struct, to_json}

object PostGroupDataSend {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val jobId = pargs.required("jobId")
    val dt = pargs.optional("date").getOrElse(DateTime.yesterday().toString("yyyy-MM-dd"))

    val jobPublishSql =
      s"""
         |select cast(_c0 as bigint) as postId,
         |       $jobId as packId,
         |       3 as expireDays,
         |       0 as type
         |from `csv`.`/user/da_lofter/warehouse/content_group/dt=$dt/job_id=$jobId`
         |
         |""".stripMargin

    spark.sql(jobPublishSql)
      .toJSON
      .write
      .format("kafka")
      .option("kafka.bootstrap.servers", "10.59.187.60:9092,10.59.187.61:9092,10.59.187.62:9092")
      .option("topic", "AD.RESOURCE_MAISUI.POSTPACKAGEID")
      .save()
  }
}
