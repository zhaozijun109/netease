package com.netease.lofter.etl.sparksqltest

import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.sql.types._

import java.time.LocalDate

case class MockData(database: String, table: String, data: Seq[Row], schema: StructType, partitionValues: Map[String, String] = Map.empty)

case class ExpectedOutput(
    minRows: Option[Int] = None,
    maxRows: Option[Int] = None,
    exactRowCount: Option[Int] = None,
    requiredColumns: Seq[String] = Seq.empty,
    assertions: Seq[DataFrame => Unit] = Seq.empty
)

case class JobTestResult(
    jobName: String,
    success: Boolean,
    outputTable: Option[String],
    outputRowCount: Long,
    outputColumns: Seq[String],
    outputSample: Seq[Row],
    error: Option[String]
)

object SparkSqlTestRunner {

  lazy val spark: SparkSession = {
    val session = SparkSession.builder()
      .master("local[*]")
      .appName("SparkSqlJobTest")
      .config("spark.sql.warehouse.dir", System.getProperty("java.io.tmpdir") + "/spark-warehouse-test")
      .config("javax.jdo.option.ConnectionURL",
        s"jdbc:derby:;databaseName=${System.getProperty("java.io.tmpdir")}/metastore_db_test_${System.currentTimeMillis()};create=true")
      .config("spark.sql.shuffle.partitions", "2")
      .config("spark.ui.enabled", "false")
      .config("spark.sql.adaptive.enabled", "false")
      .config("spark.sql.storeAssignmentPolicy", "LEGACY")
      .config("spark.sql.mapKeyDedupPolicy", "LAST_WIN")
      .config("spark.driver.bindAddress", "127.0.0.1")
      .enableHiveSupport()
      .getOrCreate()

    session.sparkContext.setLogLevel("WARN")
    session
  }

  def runJobTest(
      jobFilePath: String,
      baseDate: LocalDate = LocalDate.of(2024, 1, 2),
      mockInputData: Seq[MockData] = Seq.empty,
      schemaOverrides: Map[String, MockTableDef] = Map.empty,
      expected: ExpectedOutput = ExpectedOutput()
  ): JobTestResult = {
    val job = JobFileParser.parseJobFile(jobFilePath)
      .getOrElse(return JobTestResult("unknown", success = false, None, 0, Seq.empty, Seq.empty, Some(s"Failed to parse job file: $jobFilePath")))

    runJob(job, baseDate, mockInputData, schemaOverrides, expected)
  }

  def runJob(
      job: SparkSqlJob,
      baseDate: LocalDate = LocalDate.of(2024, 1, 2),
      mockInputData: Seq[MockData] = Seq.empty,
      schemaOverrides: Map[String, MockTableDef] = Map.empty,
      expected: ExpectedOutput = ExpectedOutput()
  ): JobTestResult = {
    try {
      val analysis = MockTableSetup.autoSetupTablesForJob(spark, job, schemaOverrides)

      loadMockData(mockInputData)

      val vars = VariableSubstitution.buildDefaultVars(baseDate)

      val skippedUdfs = scala.collection.mutable.ListBuffer[String]()
      for (query <- job.queries) {
        val substituted = VariableSubstitution.substituteWithVars(query, vars)
        if (isUdfRegistration(substituted)) {
          skippedUdfs += substituted.trim.take(80)
        } else {
          // 对SQL进行自引用重写
          val rewritten = SqlRewriter.rewriteForSelfReference(substituted, analysis.selfReferences)
          println(s"  [DEBUG] 处理SQL查询:")
          println(s"  [DEBUG] 自引用表数量: ${analysis.selfReferences.size}")
          println(s"  [DEBUG] SQL是否发生变化: ${rewritten != substituted}")
          if (rewritten != substituted) {
            println(s"  [INFO] SQL重写前:\n$substituted")
            println(s"  [INFO] SQL重写后:\n$rewritten")
          } else if (analysis.selfReferences.nonEmpty) {
            println(s"  [WARN] 存在自引用但SQL未发生重写")
            println(s"  [WARN] 原始SQL (前200字符): ${substituted.take(200)}")
            println(s"  [DEBUG] 完整SQL:\n$substituted")
          }
          spark.sql(rewritten)
        }
      }
      if (skippedUdfs.nonEmpty) {
        System.err.println(s"  [WARN] Skipped ${skippedUdfs.size} UDF registration query(s) for job ${job.name}")
      }

      val outputResult = analysis.outputTable.map { out =>
        val df = spark.table(out.fullName)
        val count = df.count()
        val columns = df.columns.toSeq
        val sample = df.limit(20).collect().toSeq

        validateOutput(df, count, columns, expected)

        (out.fullName, count, columns, sample)
      }

      outputResult match {
        case Some((tableName, count, columns, sample)) =>
          JobTestResult(job.name, success = true, Some(tableName), count, columns, sample, None)
        case None =>
          JobTestResult(job.name, success = true, None, 0, Seq.empty, Seq.empty, None)
      }
    } catch {
      case e: Exception =>
        JobTestResult(job.name, success = false, None, 0, Seq.empty, Seq.empty, Some(s"${e.getClass.getSimpleName}: ${e.getMessage}"))
    }
  }

  def dryRun(
      jobFilePath: String,
      baseDate: LocalDate = LocalDate.of(2024, 1, 2)
  ): (SparkSqlJob, SqlAnalysis, String) = {
    val job = JobFileParser.parseJobFile(jobFilePath)
      .getOrElse(throw new RuntimeException(s"Failed to parse: $jobFilePath"))

    val analysis = job.queries.map(TableExtractor.analyze).reduce { (a, b) =>
      SqlAnalysis(
        a.inputTables ++ b.inputTables,
        b.outputTable.orElse(a.outputTable),
        (a.outputPartitionColumns ++ b.outputPartitionColumns).distinct,
        a.selfReferences ++ b.selfReferences
      )
    }

    val vars = VariableSubstitution.buildDefaultVars(baseDate)
    val substitutedSql = job.queries.map { q =>
      val substituted = VariableSubstitution.substituteWithVars(q, vars)
      SqlRewriter.rewriteForSelfReference(substituted, analysis.selfReferences)
    }.mkString(";\n")

    (job, analysis, substitutedSql)
  }

  private def loadMockData(mockData: Seq[MockData]): Unit = {
    for (md <- mockData) {
      spark.sql(s"CREATE DATABASE IF NOT EXISTS ${md.database}")

      val df = spark.createDataFrame(
        spark.sparkContext.parallelize(md.data),
        md.schema
      )

      if (md.partitionValues.nonEmpty) {
        df.write.mode("overwrite").insertInto(s"${md.database}.${md.table}")
      } else {
        val fullTableName = s"${md.database}.${md.table}"

        try { spark.sql(s"DROP TABLE IF EXISTS $fullTableName") } catch { case _: Exception => }

        val nonPartCols = md.schema.fields.map(f => s"${f.name} ${sparkTypeToHive(f.dataType)}").mkString(", ")
        spark.sql(s"CREATE TABLE IF NOT EXISTS $fullTableName ($nonPartCols) STORED AS PARQUET")

        df.write.mode("overwrite").insertInto(fullTableName)
      }
    }
  }

  private val UdfPattern = """(?i)^\s*create\s+temporary\s+function\b""".r

  private def isUdfRegistration(sql: String): Boolean =
    UdfPattern.findFirstIn(sql).isDefined

  private def sparkTypeToHive(dt: DataType): String = dt match {
    case StringType => "STRING"
    case IntegerType => "INT"
    case LongType => "BIGINT"
    case DoubleType => "DOUBLE"
    case FloatType => "FLOAT"
    case BooleanType => "BOOLEAN"
    case _: ArrayType => "ARRAY<STRING>"
    case _: MapType => "MAP<STRING,STRING>"
    case _ => "STRING"
  }

  private def validateOutput(df: DataFrame, count: Long, columns: Seq[String], expected: ExpectedOutput): Unit = {
    expected.exactRowCount.foreach { exact =>
      assert(count == exact, s"Expected exactly $exact rows, got $count")
    }
    expected.minRows.foreach { min =>
      assert(count >= min, s"Expected at least $min rows, got $count")
    }
    expected.maxRows.foreach { max =>
      assert(count <= max, s"Expected at most $max rows, got $count")
    }
    expected.requiredColumns.foreach { col =>
      assert(columns.contains(col), s"Missing required column: $col. Available: ${columns.mkString(", ")}")
    }
    expected.assertions.foreach(_(df))
  }

  def cleanup(): Unit = {
    for (db <- Seq("lofter", "lofter_db_dump", "lofter_dm")) {
      try { spark.sql(s"DROP DATABASE IF EXISTS $db CASCADE") } catch { case _: Exception => }
    }
  }
}
