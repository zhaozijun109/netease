package com.netease.lofter.etl.sparksqltest.dimensions

import com.netease.lofter.etl.sparksqltest._
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._

import java.time.LocalDate

class DimPostArticleJobTest extends SparkSqlJobTestBase {

  test("dim_post_article filters out questions from dim_post") {
    val dimPostSchema = StructType(Seq(
      StructField("id", LongType),
      StructField("userId", LongType),
      StructField("blogId", LongType),
      StructField("blogName", StringType),
      StructField("title", StringType),
      StructField("publishTime", LongType),
      StructField("publishDate", StringType),
      StructField("tags", ArrayType(StringType)),
      StructField("domains", ArrayType(StringType)),
      StructField("isPublished", IntegerType),
      StructField("isForbidden", IntegerType),
      StructField("isBlogAuthenticated", IntegerType),
      StructField("userPostIndex", IntegerType),
      StructField("contentType", StringType),
      StructField("citedParentPostid", LongType),
      StructField("isCitedPost", IntegerType),
      StructField("moveFrom", StringType),
      StructField("userCreateFrom", StringType),
      StructField("userCreateDate", StringType),
      StructField("blogNickname", StringType),
      StructField("valid", IntegerType),
      StructField("allowView", IntegerType),
      StructField("isChat", IntegerType),
      StructField("isMoved", IntegerType),
      StructField("isImported", IntegerType),
      StructField("isActivityAutopost", IntegerType),
      StructField("ips", ArrayType(StringType)),
      StructField("url", StringType),
      StructField("is_book_store", IntegerType),
      StructField("recomStatus", IntegerType),
      StructField("importPlatformType", StringType)
    ))

    val dimPostData = Seq(
      Row(1L, 100L, 200L, "blog1", "Photo Post", 1704067200000L, "2024-01-01", Seq("art"), Seq("domain1"), 1, 0, 0, 1, "图片", null, 0, null, null, null, "nick1", 1, 1, 0, 0, 0, 0, Seq("ip1"), "url1", 0, 1, null),
      Row(2L, 101L, 201L, "blog2", "Video Post", 1704067200000L, "2024-01-01", Seq("video"), Seq("domain2"), 1, 0, 0, 2, "视频", null, 0, null, null, null, "nick2", 1, 1, 0, 0, 0, 0, Seq("ip2"), "url2", 0, 1, null),
      Row(3L, 102L, 202L, "blog3", "Question Post", 1704067200000L, "2024-01-01", Seq("qa"), Seq("domain3"), 1, 0, 0, 3, "问答", null, 0, null, null, null, "nick3", 1, 1, 0, 0, 0, 0, Seq("ip3"), "url3", 0, 1, null)
    )

    val dimPostArticleSchema = dimPostSchema

    val result = testJob(
      "dimensions/dim_post_article.job",
      mockInputData = Seq(
        MockData("lofter", "dim_post", dimPostData, dimPostSchema),
      ),
      schemaOverrides = Map(
        "lofter.dim_post" -> MockTableDef("lofter", "dim_post",
          dimPostSchema.fields.map(f => MockColumn(f.name, f.dataType)).toSeq),
        "lofter.dim_post_article" -> MockTableDef("lofter", "dim_post_article",
          dimPostArticleSchema.fields.map(f => MockColumn(f.name, f.dataType)).toSeq)
      )
    )

    printJobReport(result)
    assertJobSuccess(result)
    assertJobProducesRows(result, minRows = 2)

    withOutputTable(result) { df =>
      val contentTypes = df.select("contentType").collect().map(_.getString(0)).toSet
      contentTypes should not contain "问答"
      contentTypes should contain("图片")
      contentTypes should contain("视频")
      df.count() shouldBe 2
    }
  }
}
