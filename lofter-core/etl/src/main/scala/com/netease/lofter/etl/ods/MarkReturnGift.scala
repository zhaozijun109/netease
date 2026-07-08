package com.netease.lofter.etl.ods

import com.netease.wm.util.Args
import org.apache.spark.sql.{SaveMode, SparkSession}

object MarkReturnGift {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val dt = pargs.required("date")

    val esUrl = "lofter-risk-es1.gy.ntes:7000,lofter-risk-es2.gy.ntes:7000,lofter-risk-es3.gy.ntes:7000"
    val esHosts = esUrl.split(",").map(_.split(":")(0))
    val esPort = 7000

    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .config("es.nodes", esHosts.mkString(","))
      .config("es.port", esPort.toString)
      .config("es.net.http.auth.user", "lofter_risk_online")
      .config("es.net.http.auth.pass", "67pg4UmF7F")
      .getOrCreate()

    val df = spark.read.format("es")
      .option("es.field.read.empty.as.null", "no")
      .option("es.read.field.exclude", "ocrImages,ocrTexts")
      .option("es.read.field.include", "id,personTags,reviewStatus,postId,blogId,machineTags")
      .option("es.read.field.as.array.include", "machineTags,personTags")
      .load("mark_return_gift/_doc")

    df.write.mode(SaveMode.Overwrite).saveAsTable("lofter.ods_risk_mark_return_gift_nd")

    spark.close()
  }
}
