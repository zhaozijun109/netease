package com.netease.lofter.etl.sparksqltest.dwd

import com.netease.lofter.etl.sparksqltest._
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._

import java.time.LocalDate

class DwdCollectionDetailJobTest extends SparkSqlJobTestBase {

  test("dwd_collection_detail_di filters collection browse events") {
    val browseSchema = StructType(Seq(
      StructField("itemId", LongType),
      StructField("deviceUdid", StringType),
      StructField("userId", LongType),
      StructField("occurTime", LongType),
      StructField("deviceOs", StringType),
      StructField("appVersion", StringType),
      StructField("item_type", StringType),
      StructField("dt", StringType)
    ))

    val browseData = Seq(
      Row(1001L, "udid_a", 100L, 1704153600000L, "iOS", "7.0.0", "合集", "2024-01-01"),
      Row(1002L, "udid_b", 101L, 1704153700000L, "Android", "7.1.0", "合集", "2024-01-01"),
      Row(1003L, "udid_c", 102L, 1704153800000L, "iOS", "7.0.0", "帖子", "2024-01-01"),
      Row(0L, "udid_d", 103L, 1704153900000L, "iOS", "7.0.0", "合集", "2024-01-01"),
      Row(1004L, "udid_e", 104L, 1704154000000L, "Android", "7.2.0", "合集", "2024-01-02")
    )

    val outputSchema = StructType(Seq(
      StructField("collectionId", LongType),
      StructField("deviceUdid", StringType),
      StructField("userId", LongType),
      StructField("occurTime", LongType),
      StructField("deviceOs", StringType),
      StructField("appVersion", StringType),
      StructField("dt", StringType)
    ))

    val result = testJob(
      "dwd/dwd_collection_detail_di.job",
      mockInputData = Seq(
        MockData("lofter", "dwd_content_browse_di", browseData, browseSchema)
      ),
      schemaOverrides = Map(
        "lofter.dwd_content_browse_di" -> MockTableDef("lofter", "dwd_content_browse_di",
          browseSchema.fields.map(f => MockColumn(f.name, f.dataType)).toSeq, Seq("dt")),
        "lofter.dwd_collection_detail_di" -> MockTableDef("lofter", "dwd_collection_detail_di",
          outputSchema.fields.map(f => MockColumn(f.name, f.dataType)).toSeq, Seq("dt"))
      )
    )

    printJobReport(result)
    assertJobSuccess(result)

    withOutputTable(result) { df =>
      val ids = df.select("collectionId").collect().map(_.getLong(0)).toSet
      ids should contain(1001L)
      ids should contain(1002L)
      ids should not contain 1003L
      ids should not contain 0L
      ids should not contain 1004L
    }
  }
}
