package com.netease.lofter.etl.sparksqltest

import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.sql.types._
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate

trait SparkSqlJobTestBase extends AnyFunSuite with Matchers with BeforeAndAfterAll with BeforeAndAfterEach {

  lazy val spark: SparkSession = SparkSqlTestRunner.spark

  val jobsBaseDir: String = {
    val projectDir = sys.props.getOrElse("user.dir",
      sys.env.getOrElse("PROJECT_DIR", "."))
    val candidate = new java.io.File(s"$projectDir/etl/jobs")
    if (candidate.exists()) candidate.getAbsolutePath
    else {
      val alt = new java.io.File(s"$projectDir/jobs")
      if (alt.exists()) alt.getAbsolutePath
      else candidate.getAbsolutePath
    }
  }

  val defaultBaseDate: LocalDate = LocalDate.of(2024, 1, 2)

  override def afterEach(): Unit = {
    SparkSqlTestRunner.cleanup()
    super.afterEach()
  }

  override def afterAll(): Unit = {
    super.afterAll()
  }

  def jobPath(relativePath: String): String = s"$jobsBaseDir/$relativePath"

  def testJob(
      jobFile: String,
      mockInputData: Seq[MockData] = Seq.empty,
      schemaOverrides: Map[String, MockTableDef] = Map.empty,
      baseDate: LocalDate = defaultBaseDate,
      expected: ExpectedOutput = ExpectedOutput()
  ): JobTestResult = {
    val result = SparkSqlTestRunner.runJobTest(
      jobPath(jobFile), baseDate, mockInputData, schemaOverrides, expected
    )
    result
  }

  def dryRunJob(jobFile: String, baseDate: LocalDate = defaultBaseDate): (SparkSqlJob, SqlAnalysis, String) = {
    SparkSqlTestRunner.dryRun(jobPath(jobFile), baseDate)
  }

  def mockTable(
      database: String,
      table: String,
      columns: Seq[(String, DataType)],
      data: Seq[Seq[Any]],
      partitionColumns: Seq[String] = Seq.empty
  ): MockData = {
    val schema = StructType(columns.map { case (name, dt) => StructField(name, dt, nullable = true) })
    val rows = data.map(values => Row(values: _*))
    MockData(database, table, rows, schema, partitionColumns.map(c => c -> "").toMap)
  }

  def withOutputTable[T](result: JobTestResult)(f: DataFrame => T): T = {
    assert(result.success, s"Job ${result.jobName} failed: ${result.error.getOrElse("unknown")}")
    assert(result.outputTable.isDefined, s"Job ${result.jobName} has no output table")
    val df = spark.table(result.outputTable.get)
    f(df)
  }

  def assertJobSuccess(result: JobTestResult): Unit = {
    assert(result.success, s"Job ${result.jobName} failed: ${result.error.getOrElse("unknown")}")
  }

  def assertJobProducesRows(result: JobTestResult, minRows: Int = 1): Unit = {
    assertJobSuccess(result)
    assert(result.outputRowCount >= minRows,
      s"Job ${result.jobName} produced ${result.outputRowCount} rows, expected at least $minRows")
  }

  def assertJobHasColumns(result: JobTestResult, columns: Seq[String]): Unit = {
    assertJobSuccess(result)
    columns.foreach { col =>
      assert(result.outputColumns.contains(col),
        s"Missing column '$col' in output. Available: ${result.outputColumns.mkString(", ")}")
    }
  }

  def printJobReport(result: JobTestResult): Unit = {
    println(s"\n=== Job Test Report: ${result.jobName} ===")
    println(s"  Status:      ${if (result.success) "PASS" else "FAIL"}")
    println(s"  Output:      ${result.outputTable.getOrElse("(none)")}")
    println(s"  Row count:   ${result.outputRowCount}")
    println(s"  Columns:     ${result.outputColumns.mkString(", ")}")
    result.error.foreach(e => println(s"  Error:       $e"))
    if (result.outputSample.nonEmpty) {
      println(s"  Sample rows:")
      result.outputSample.take(5).foreach(r => println(s"    $r"))
    }
    println("=" * 50)
  }
}
