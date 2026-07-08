package com.netease.lofter.etl.ods

import com.netease.lofter.common.HdfsUtil
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.lit

object AntiSpamCallBackEtlJob {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Antispam CallBack Etl")
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.required("date")

    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")
    val path = s"/user/da_lofter/datastream/antispam/online/$date"

    if (HdfsUtil.isHdfsPathEmpty(path)) {
      spark.close()
      return
    }

    val df1 = spark.read.json(path)

    val type0 = df1.filter("type=0").count()
    val type5 = df1.filter("type=5").count()
    val type6 = df1.filter("type=6").count()
    val type7 = df1.filter("type=7").count()

    // type: 0 article, 5 comment, 6 message, 7 blog, 8 report
    if (type0 > 0) {
      df1.filter("type=0")
        .withColumn("dt", lit(date))
        .select("blogId", "postId", "postType", "label", "level", "createTime", "operateType", "operator", "allowView", "hist", "dt")
        .write
        .mode("overwrite")
        .insertInto("lofter.ods_log_antispam_callback_article_di")
    }

    if (type5 > 0) {
      df1.filter("type=5")
        .withColumn("dt", lit(date))
        .select("responseId", "blogId", "postId", "label", "level", "createTime", "operateType", "hist", "dt")
        .write
        .mode("overwrite")
        .insertInto("lofter.ods_log_antispam_callback_comment_di")
    }

    if (type6 > 0) {
      df1.filter("type=6")
        .withColumn("dt", lit(date))
        .select("messageId", "blogId", "label", "level", "createTime", "operateType", "hist", "dt")
        .write
        .mode("overwrite")
        .insertInto("lofter.ods_log_antispam_callback_message_di")
    }

    if (type7 > 0) {
      df1.filter("type=7")
        .withColumn("dt", lit(date))
        .select("blogId", "label", "level", "createTime", "operateType", "hint", "dt")
        .write
        .mode("overwrite")
        .insertInto("lofter.ods_log_antispam_callback_blog_di")
    }

  }

}
