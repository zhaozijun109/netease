package com.netease.lofter.etl.sparksqltest.ads

import com.netease.lofter.etl.sparksqltest._
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._

import java.time.LocalDate

class AdsUserFavoriteTagTopJobTest extends SparkSqlJobTestBase {

  test("ads_par_user_favorite_tag_top_dd computes top-9 favorite tags per user") {
    val inputSchema = StructType(Seq(
      StructField("userId", LongType),
      StructField("tag", StringType),
      StructField("pv", LongType),
      StructField("is_favorite_tag", IntegerType),
      StructField("dt", StringType)
    ))

    // baseDate=2024-01-02 => azkaban.flow.1.days.ago = 2024-01-01
    // date_sub('2024-01-01', 180) = '2023-07-05'
    // dt range: [2023-07-05, 2024-01-01]

    // User 1: 11 distinct tags across multiple days => expect top 9 by total pv
    // User 2: 2 tags => expect both (< 10)
    // Rows with is_favorite_tag=0 or userId=0 should be excluded

    val inputData = Seq(
      // User 1, tag_01 through tag_11 with varying pv, spread across valid dt range
      Row(1L, "tag_01", 100L, 1, "2024-01-01"),
      Row(1L, "tag_01",  50L, 1, "2023-12-15"),
      Row(1L, "tag_02",  90L, 1, "2024-01-01"),
      Row(1L, "tag_03",  80L, 1, "2023-11-01"),
      Row(1L, "tag_04",  70L, 1, "2023-10-01"),
      Row(1L, "tag_05",  60L, 1, "2023-09-01"),
      Row(1L, "tag_06",  50L, 1, "2023-08-01"),
      Row(1L, "tag_07",  40L, 1, "2023-07-15"),
      Row(1L, "tag_08",  30L, 1, "2024-01-01"),
      Row(1L, "tag_09",  20L, 1, "2023-12-01"),
      Row(1L, "tag_10",  10L, 1, "2023-11-15"),
      Row(1L, "tag_11",   5L, 1, "2023-10-15"),
      // User 2, 2 tags
      Row(2L, "tag_a",  200L, 1, "2024-01-01"),
      Row(2L, "tag_b",  100L, 1, "2023-12-01"),
      // Excluded: is_favorite_tag = 0
      Row(1L, "tag_01", 999L, 0, "2024-01-01"),
      // Excluded: userId = 0
      Row(0L, "tag_x",  500L, 1, "2024-01-01"),
      // Excluded: dt out of range (before 2023-07-05)
      Row(1L, "tag_01", 999L, 1, "2023-06-01"),
      // Excluded: dt after 2024-01-01
      Row(1L, "tag_01", 999L, 1, "2024-01-02")
    )

    val outputSchema = StructType(Seq(
      StructField("userId", LongType),
      StructField("tag", StringType),
      StructField("pv", LongType),
      StructField("rnk", IntegerType),
      StructField("dt", StringType)
    ))

    val result = testJob(
      "ads/ads_par_user_favorite_tag_top_dd.job",
      mockInputData = Seq(
        MockData("lofter", "dws_tag_user_consume_di", inputData, inputSchema)
      ),
      schemaOverrides = Map(
        "lofter.dws_tag_user_consume_di" -> MockTableDef("lofter", "dws_tag_user_consume_di",
          inputSchema.fields.map(f => MockColumn(f.name, f.dataType)).toSeq, Seq("dt")),
        "lofter_dm.ads_par_user_favorite_tag_top_dd" -> MockTableDef("lofter_dm", "ads_par_user_favorite_tag_top_dd",
          outputSchema.fields.map(f => MockColumn(f.name, f.dataType)).toSeq, Seq("dt"))
      )
    )

    printJobReport(result)
    assertJobSuccess(result)

    withOutputTable(result) { df =>
      val allRows = df.collect()

      // User 1: 11 tags, but rnk < 10 means top 9 only
      val user1Rows = allRows.filter(_.getLong(0) == 1L)
      user1Rows.length shouldBe 9

      // tag_01 has pv=150 (100+50), should be rank 1 for user 1
      val tag01 = user1Rows.find(r => r.getString(1) == "tag_01").get
      tag01.getLong(2) shouldBe 150L
      tag01.getInt(3) shouldBe 1

      // tag_10 (pv=10) and tag_11 (pv=5) are rank 10 and 11 => excluded
      user1Rows.map(_.getString(1)).toSet should not contain "tag_10"
      user1Rows.map(_.getString(1)).toSet should not contain "tag_11"

      // User 2: only 2 tags, both should appear
      val user2Rows = allRows.filter(_.getLong(0) == 2L)
      user2Rows.length shouldBe 2
      val tagA = user2Rows.find(r => r.getString(1) == "tag_a").get
      tagA.getLong(2) shouldBe 200L
      tagA.getInt(3) shouldBe 1

      // Total rows: 9 (user1) + 2 (user2) = 11
      df.count() shouldBe 11
    }
  }
}
