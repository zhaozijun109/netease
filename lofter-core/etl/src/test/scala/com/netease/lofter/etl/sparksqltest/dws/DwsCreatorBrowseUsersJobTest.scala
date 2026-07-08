package com.netease.lofter.etl.sparksqltest.dws

import com.netease.lofter.etl.sparksqltest._
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._

import java.time.LocalDate

class DwsCreatorBrowseUsersJobTest extends SparkSqlJobTestBase {

  test("dws_creator_browse_users_di aggregates browse events per creator-user pair") {
    val browseSchema = StructType(Seq(
      StructField("post_userId", LongType),
      StructField("userId", LongType),
      StructField("occurTime", LongType),
      StructField("is_real", IntegerType),
      StructField("dt", StringType)
    ))

    val browseData = Seq(
      Row(200L, 100L, 1000L, 1, "2024-01-01"),
      Row(200L, 100L, 2000L, 1, "2024-01-01"),
      Row(200L, 100L, 3000L, 1, "2024-01-01"),
      Row(200L, 101L, 1500L, 1, "2024-01-01"),
      Row(201L, 100L, 5000L, 1, "2024-01-01"),
      Row(200L, 102L, 4000L, 0, "2024-01-01"),
      Row(200L, 103L, 6000L, 1, "2024-01-02")
    )

    val outputSchema = StructType(Seq(
      StructField("blogId", LongType),
      StructField("userId", LongType),
      StructField("first_time", LongType),
      StructField("last_time", LongType),
      StructField("dt", StringType)
    ))

    val result = testJob(
      "dws/dws_creator_browse_users_di.job",
      mockInputData = Seq(
        MockData("lofter", "dwd_post_browse_di", browseData, browseSchema)
      ),
      schemaOverrides = Map(
        "lofter.dwd_post_browse_di" -> MockTableDef("lofter", "dwd_post_browse_di",
          browseSchema.fields.map(f => MockColumn(f.name, f.dataType)).toSeq, Seq("dt")),
        "lofter.dws_creator_browse_users_di" -> MockTableDef("lofter", "dws_creator_browse_users_di",
          outputSchema.fields.map(f => MockColumn(f.name, f.dataType)).toSeq, Seq("dt"))
      )
    )

    printJobReport(result)
    assertJobSuccess(result)

    withOutputTable(result) { df =>
      df.count() shouldBe 3

      val rows = df.collect().map(r => (r.getLong(0), r.getLong(1), r.getLong(2), r.getLong(3)))

      val creator200user100 = rows.find(r => r._1 == 200L && r._2 == 100L).get
      creator200user100._3 shouldBe 1000L
      creator200user100._4 shouldBe 3000L

      val creator200user101 = rows.find(r => r._1 == 200L && r._2 == 101L).get
      creator200user101._3 shouldBe 1500L
      creator200user101._4 shouldBe 1500L

      val creator201user100 = rows.find(r => r._1 == 201L && r._2 == 100L).get
      creator201user100._3 shouldBe 5000L
      creator201user100._4 shouldBe 5000L
    }
  }
}
