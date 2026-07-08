package com.netease.lofter.data.jobs.backend

import com.netease.wm.util.Args
import org.apache.spark.sql.{SaveMode, SparkSession}

object AcademicScoreSeg {

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val dt = pargs.required("date")

    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val academicScore = spark.read
      .format("jdbc")
      .option("url", "jdbc:mysql://lofter-rds-activity-online-jd-34731.rds.cn-gz-p1.internal.:3306/lofter_activity?useUnicode=true&characterEncoding=UTF-8")
      .option("dbtable", "Academic_Score")
      .option("user", "lofter_bi_gy")
      .option("password", "NO@b7Q_a9")
      .option("driver","com.mysql.jdbc.Driver")
      .load()

    val academicActivity = spark.read
      .format("jdbc")
      .option("url", "jdbc:mysql://lofter-rds-activity-online-jd-34731.rds.cn-gz-p1.internal.:3306/lofter_activity?useUnicode=true&characterEncoding=UTF-8")
      .option("dbtable", "Academic_Activity")
      .option("user", "lofter_bi_gy")
      .option("password", "NO@b7Q_a9")
      .option("driver","com.mysql.jdbc.Driver")
      .load()

    academicScore.createOrReplaceTempView("academic_score")
    academicActivity.createOrReplaceTempView("academic_activity")

    val segSql =
     s"""
        |insert overwrite table lofter_dm.ads_act_kecp_academic_score_seg_di partition( dt = '$dt')
        |select a.activityName, b.score_seg, b.uv
        |from academic_activity a
        |join (
        |  select actId,
        |          case when score < 250 then '0-250'
        |              when score < 350 then '251-350'
        |              when score < 400 then '351-400'
        |              when score < 550 then '401-550'
        |              when score < 2000 then '551-2000'
        |              else '大于等于2000' end score_seg,
        |        count(distinct userId) as uv
        |  from academic_score
        |  where status = 0
        |  group by 1, 2
        |) b on a.id = b.actId
        |""".stripMargin

    spark.sql(segSql)

    spark.close()
  }
}
