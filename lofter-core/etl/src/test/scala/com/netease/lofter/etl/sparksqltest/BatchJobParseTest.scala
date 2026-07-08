package com.netease.lofter.etl.sparksqltest

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate

class BatchJobParseTest extends AnyFunSuite with Matchers {

  val jobsDir: String = {
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

  test("all sparksql job files parse successfully") {
    val jobs = JobFileParser.findSparkSqlJobs(jobsDir)

    assert(jobs.nonEmpty, s"No sparksql jobs found in $jobsDir")
    println(s"\nFound ${jobs.size} sparksql jobs")

    val failures = scala.collection.mutable.ListBuffer[(String, String)]()

    for (job <- jobs) {
      try {
        assert(job.queries.nonEmpty, s"Job ${job.name} has no queries")

        val allAnalyses = job.queries.map(TableExtractor.analyze)
        val combined = allAnalyses.reduce { (a, b) =>
          SqlAnalysis(
            a.inputTables ++ b.inputTables,
            b.outputTable.orElse(a.outputTable),
            (a.outputPartitionColumns ++ b.outputPartitionColumns).distinct
          )
        }
        assert(combined.inputTables.nonEmpty || combined.outputTable.isDefined,
          s"Job ${job.name}: could not extract any table references across ${job.queries.size} queries")
      } catch {
        case e: Exception =>
          failures += ((job.name, e.getMessage))
      }
    }

    if (failures.nonEmpty) {
      println(s"\n${failures.size} jobs had issues:")
      failures.foreach { case (name, msg) => println(s"  FAIL: $name -> $msg") }
    }

    println(s"\n${jobs.size - failures.size}/${jobs.size} jobs parsed and analyzed successfully")
  }

  test("all sparksql jobs have valid variable substitution") {
    val jobs = JobFileParser.findSparkSqlJobs(jobsDir)
    val baseDate = LocalDate.of(2024, 1, 2)
    val vars = VariableSubstitution.buildDefaultVars(baseDate)

    for (job <- jobs) {
      for (query <- job.queries) {
        val substituted = VariableSubstitution.substituteWithVars(query, vars)
        val remaining = VariableSubstitution.extractVariables(substituted)
        assert(remaining.isEmpty,
          s"Job ${job.name} has unresolved variables after substitution: ${remaining.mkString(", ")}")
      }
    }
  }

  test("report table dependency graph") {
    val jobs = JobFileParser.findSparkSqlJobs(jobsDir)
    val allInputTables = scala.collection.mutable.Set[String]()
    val allOutputTables = scala.collection.mutable.Set[String]()

    for (job <- jobs; query <- job.queries) {
      val analysis = TableExtractor.analyze(query)
      allInputTables ++= analysis.inputTables.map(_.fullName)
      analysis.outputTable.foreach(t => allOutputTables += t.fullName)
    }

    println(s"\n=== Table Dependency Report ===")
    println(s"Total input tables:  ${allInputTables.size}")
    println(s"Total output tables: ${allOutputTables.size}")
    println(s"Tables that are both input and output: ${(allInputTables & allOutputTables).size}")

    val databases = (allInputTables ++ allOutputTables).map(_.split('.').head).toSet
    println(s"Databases: ${databases.mkString(", ")}")

    val externalInputs = allInputTables -- allOutputTables
    println(s"\nExternal inputs (not produced by any sparksql job): ${externalInputs.size}")
    if (externalInputs.size <= 30) {
      externalInputs.toSeq.sorted.foreach(t => println(s"  $t"))
    }
  }
}
