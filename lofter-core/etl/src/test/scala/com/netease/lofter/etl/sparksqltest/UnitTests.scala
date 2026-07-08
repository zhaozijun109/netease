package com.netease.lofter.etl.sparksqltest

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.io.{File, PrintWriter}
import java.time.LocalDate

class JobFileParserTest extends AnyFunSuite with Matchers {

  private def writeTempJob(content: String): String = {
    val file = File.createTempFile("test_job_", ".job")
    file.deleteOnExit()
    val pw = new PrintWriter(file)
    pw.write(content)
    pw.close()
    file.getAbsolutePath
  }

  test("parse simple sparksql job") {
    val content =
      """type=sparksql
        |hive.query.01=insert overwrite table lofter.dim_test select id from lofter.source_table
        |dependencies=source_job""".stripMargin

    val job = JobFileParser.parseJobFile(writeTempJob(content))
    job shouldBe defined
    job.get.jobType shouldBe "sparksql"
    job.get.queries should have size 1
    job.get.queries.head should include("insert overwrite table lofter.dim_test")
    job.get.dependencies shouldBe Seq("source_job")
  }

  test("parse multiline query with backslash continuation") {
    val content =
      """type=sparksql
        |hive.query.01=insert overwrite table lofter.output \
        |select a.id, \
        |       b.name \
        |from lofter.table_a a \
        |join lofter.table_b b on a.id = b.id""".stripMargin

    val job = JobFileParser.parseJobFile(writeTempJob(content))
    job shouldBe defined
    val sql = job.get.queries.head
    sql should include("select a.id,")
    sql should include("from lofter.table_a")
    sql should include("join lofter.table_b")
  }

  test("extract azkaban variables") {
    val content =
      """type=sparksql
        |hive.query.01=select * from t where dt = '${azkaban.flow.1.days.ago}' and dt2 >= '${azkaban.flow.7.days.ago}'""".stripMargin

    val job = JobFileParser.parseJobFile(writeTempJob(content))
    job shouldBe defined
    job.get.variables should contain("azkaban.flow.1.days.ago")
    job.get.variables should contain("azkaban.flow.7.days.ago")
  }

  test("parse spark conf properties") {
    val content =
      """type=sparksql
        |conf.spark.dynamicAllocation.maxExecutors=50
        |conf.spark.sql.shuffle.partitions=200
        |hive.query.01=select 1""".stripMargin

    val job = JobFileParser.parseJobFile(writeTempJob(content))
    job shouldBe defined
    job.get.sparkConf should contain("spark.dynamicAllocation.maxExecutors" -> "50")
    job.get.sparkConf should contain("spark.sql.shuffle.partitions" -> "200")
  }

  test("skip non-sparksql jobs") {
    val content =
      """execution-jar=${classpath}
        |class=com.netease.lofter.etl.ods.SomeJob
        |params=--date ${azkaban.flow.1.days.ago}""".stripMargin

    val job = JobFileParser.parseJobFile(writeTempJob(content))
    job shouldBe None
  }

  test("handle empty dependencies") {
    val content =
      """type=sparksql
        |hive.query.01=select 1""".stripMargin

    val job = JobFileParser.parseJobFile(writeTempJob(content))
    job shouldBe defined
    job.get.dependencies shouldBe empty
  }
}

class TableExtractorTest extends AnyFunSuite with Matchers {

  test("extract input and output tables") {
    val sql = """insert overwrite table lofter.dwd_post_browse_di partition(dt = '2024-01-01')
                |select a.userId from lofter.ods_mda_app_partition_di a
                |join lofter.dim_post_article b on a.postId = b.id
                |left join lofter_db_dump.ods_db_user_following_nd c on a.userId = c.userId""".stripMargin

    val analysis = TableExtractor.analyze(sql)
    analysis.outputTable shouldBe Some(TableRef("lofter", "dwd_post_browse_di"))
    analysis.inputTables should contain(TableRef("lofter", "ods_mda_app_partition_di"))
    analysis.inputTables should contain(TableRef("lofter", "dim_post_article"))
    analysis.inputTables should contain(TableRef("lofter_db_dump", "ods_db_user_following_nd"))
    analysis.inputTables should not contain TableRef("lofter", "dwd_post_browse_di")
  }

  test("extract partition columns") {
    val sql = "insert overwrite table lofter.output partition(dt = '2024-01-01') select 1"
    val analysis = TableExtractor.analyze(sql)
    analysis.outputPartitionColumns should contain("dt")
  }

  test("handle CTE queries") {
    val sql = """with cte as (select id from lofter.source_table)
                |insert overwrite table lofter_dm.output_table partition(dt = '2024-01-01')
                |select * from cte join lofter_db_dump.ods_db_post_response_nd b on cte.id = b.id""".stripMargin

    val analysis = TableExtractor.analyze(sql)
    analysis.outputTable shouldBe Some(TableRef("lofter_dm", "output_table"))
    analysis.inputTables should contain(TableRef("lofter", "source_table"))
    analysis.inputTables should contain(TableRef("lofter_db_dump", "ods_db_post_response_nd"))
  }

  test("handle multiple database prefixes") {
    val sql = """select a.id from lofter.table1 a
                |join lofter_db_dump.table2 b on a.id = b.id
                |join lofter_dm.table3 c on a.id = c.id""".stripMargin

    val analysis = TableExtractor.analyze(sql)
    analysis.inputTables.map(_.database) should contain allOf("lofter", "lofter_db_dump", "lofter_dm")
  }
}

class VariableSubstitutionTest extends AnyFunSuite with Matchers {

  test("substitute azkaban date variables") {
    val sql = "select * from t where dt = '${azkaban.flow.1.days.ago}'"
    val baseDate = LocalDate.of(2024, 1, 2)
    val result = VariableSubstitution.substitute(sql, baseDate)
    result shouldBe "select * from t where dt = '2024-01-01'"
  }

  test("substitute multiple date ranges") {
    val sql = "where dt >= '${azkaban.flow.7.days.ago}' and dt <= '${azkaban.flow.1.days.ago}'"
    val baseDate = LocalDate.of(2024, 1, 10)
    val result = VariableSubstitution.substitute(sql, baseDate)
    result should include("2024-01-09")
    result should include("2024-01-03")
  }

  test("substitute current date") {
    val sql = "partition(dt = '${azkaban.flow.current.date}')"
    val baseDate = LocalDate.of(2024, 6, 15)
    val result = VariableSubstitution.substitute(sql, baseDate)
    result shouldBe "partition(dt = '2024-06-15')"
  }

  test("leave unknown variables unchanged") {
    val sql = "select '${unknown.var}'"
    val result = VariableSubstitution.substitute(sql)
    result shouldBe "select '${unknown.var}'"
  }

  test("extract all variables from sql") {
    val sql = "where dt = '${azkaban.flow.1.days.ago}' and x = '${azkaban.flow.current.date}'"
    val vars = VariableSubstitution.extractVariables(sql)
    vars should have size 2
    vars should contain("azkaban.flow.1.days.ago")
    vars should contain("azkaban.flow.current.date")
  }
}
