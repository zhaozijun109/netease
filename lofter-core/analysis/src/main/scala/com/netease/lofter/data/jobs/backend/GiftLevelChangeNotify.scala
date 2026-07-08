package com.netease.lofter.data.jobs.backend

import com.github.nscala_time.time.Imports._
import com.netease.lofter.data.common.{KafkaPush, PopoMessage, kafkaConfig}
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.write

object GiftLevelChangeNotify {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    import spark.implicits._

    val date = pargs.optional("date").getOrElse(DateTime.now().toString("yyyy-MM-dd"))
    val twoWeeksAgo = DateTime.parse(date).minusDays(14).toString("yyyy-MM-dd")

    val notifySql =
      s"""
         |select count(distinct if(change_type = 'down' and grade = 2, blogId, null)) as down_count,
         |    count(distinct if(change_type = 'down' and grade = 2 and is_protected = 0, blogId, null)) as normal_down_count,
         |    count(distinct if(change_type = 'down' and grade = 2 and level in ('S', 'A', 'B'), blogId, null)) as down_sab_count,
         |    count(distinct if(change_type = 'down' and grade = 2 and is_protected = 0 and level in ('S', 'A', 'B'), blogId, null)) as normal_down_sab_count,
         |    count(distinct if(change_type = 'down' and grade = 2 and revenue_level >= 'level_4', blogId, null)) as down_revenue_level4_count,
         |    count(distinct if(change_type = 'down' and grade = 2 and is_protected = 0 and revenue_level >= 'level_4', blogId, null)) as normal_down_revenue_level4_count,
         |    count(distinct if(change_type = 'down' and grade = 2 and fans_std >= 1000, blogId, null)) as down_fans_1k_count,
         |    count(distinct if(change_type = 'down' and grade = 2 and is_protected = 0 and fans_std >= 1000, blogId, null)) as normal_down_fans_1k_count,
         |    count(distinct if(change_type = 'up' and grade = 4, blogId, null)) as up_count,
         |    count(distinct if(change_type = 'up' and grade = 4 and level in ('D'), blogId, null)) as up_d_level_count,
         |    count(distinct if(change_type = 'up' and grade = 4 and level in ('D*'), blogId, null)) as up_d_star_level_count,
         |    count(distinct if(change_type = 'up' and grade = 4 and revenue_money <= 100, blogId, null)) as up_revenue_100_count,
         |    count(distinct if(change_type = 'down' and grade = 3, blogId, null)) as middle_down_count,
         |    count(distinct if(change_type = 'down' and grade = 3 and level in ('S', 'A', 'B'), blogId, null)) as middle_down_sab_count,
         |    count(distinct if(change_type = 'down' and grade = 3 and revenue_level >= 'level_4', blogId, null)) as middle_down_revenue_level4_count,
         |    count(distinct if(change_type = 'down' and grade = 3 and fans_std >= 1000, blogId, null)) as middle_down_fans_1k_count
         |from (
         |    select *, if(grade > online_grade, 'up', 'down') as change_type,
         |           if(from_unixtime(cast(agree_time / 1000 AS BIGINT), 'yyyy-MM-dd') < '2024-07-05', 1, 0) as is_protected
         |    from lofter_dm.ads_creator_gift_level_change_di
         |    where dt = '$date' and
         |        grade != online_grade and
         |        weeks >= if(grade > online_grade, 3, 4)
         |) a
         | """.stripMargin

    val message = spark.sql(notifySql).collect().map { row =>
      val downCount = row.getAs[Long](0)
      val normalDownCount = row.getAs[Long](1)
      val downSabCount = row.getAs[Long](2)
      val normalDownSabCount = row.getAs[Long](3)
      val downRevenueLevel4Count = row.getAs[Long](4)
      val normalDwnRevenueLevel4Count = row.getAs[Long](5)
      val downFans1kCount = row.getAs[Long](6)
      val normalDownFans1kCount = row.getAs[Long](7)
      val upCount = row.getAs[Long](8)
      val upDLevelCount = row.getAs[Long](9)
      val upDStarLevelCount = row.getAs[Long](10)
      val upRevenue100Count = row.getAs[Long](11)
      val middleDownCount = row.getAs[Long](12)
      val middleDownSabCount = row.getAs[Long](13)
      val middleDownRevenueLevel4Count = row.getAs[Long](14)
      val middleDownFans1kCount = row.getAs[Long](15)

      Seq(
        s"1. 预计本周降至低级，人数${downCount}人",
        s"\tSAB作者${downSabCount}人",
        s"\t收益level4共${downRevenueLevel4Count}人",
        s"\t1k粉以上${downFans1kCount}人",

        s"2. 预计本周升至高级，人数${upCount}人",
        s"\tD，D*分别${upDLevelCount}、${upDStarLevelCount}人",
        s"\t收益<=100共${upRevenue100Count}人",

        s"3. 预计本周从高掉到中，人数${middleDownCount}人",
        s"\tSAB作者${middleDownSabCount}人",
        s"\t收益level4共${middleDownRevenueLevel4Count}人",
        s"\t1k粉以上${middleDownFans1kCount}人"
      ).mkString("\n")
    }.head

    val popoMessage = PopoMessage(message, targetType = 1, targets = Seq("5671809"))

    import spark.implicits._
    implicit val formats: org.json4s.Formats = DefaultFormats

    spark.createDataset(Seq(KafkaPush(key = 1.toString, write(popoMessage))))
        .selectExpr("key", """value""")
        .write
        .format("kafka")
        .option("kafka.bootstrap.servers", kafkaConfig.BOOTSTRAP_SERVERS_BACKEND)
        .option("topic", "LOFTER.CMBWEB.POPO")
        .save()

    spark.stop()
  }
}
