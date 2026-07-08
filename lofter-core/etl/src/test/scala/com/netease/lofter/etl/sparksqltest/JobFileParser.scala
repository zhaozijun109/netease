package com.netease.lofter.etl.sparksqltest

import java.io.File
import java.nio.file.{Files, Paths}
import scala.collection.mutable
import scala.io.Source

case class SparkSqlJob(
    name: String,
    filePath: String,
    jobType: String,
    queries: Seq[String],
    dependencies: Seq[String],
    sparkConf: Map[String, String],
    variables: Set[String]
)

object JobFileParser {

  private val QueryKeyPattern = """^hive\.query\.(\d+)$""".r
  private val VariablePattern = """\$\{([^}]+)\}""".r
  private val ConfKeyPattern = """^conf\.(.+)$""".r

  def parseJobFile(filePath: String): Option[SparkSqlJob] = {
    val file = new File(filePath)
    if (!file.exists()) return None

    val rawLines = Source.fromFile(file, "UTF-8").getLines().toList
    val properties = parseProperties(rawLines)

    val jobType = properties.getOrElse("type", "")
    if (jobType != "sparksql") return None

    val queries = extractQueries(properties)
    val dependenciesStr = properties.getOrElse("dependencies", "")
    // 处理注释：去掉#后面的内容
    val cleanDependenciesStr = dependenciesStr.split("#").head.trim
    val dependencies = cleanDependenciesStr
      .split(",")
      .map(_.trim)
      .filter(_.nonEmpty)
      .toSeq

    val sparkConf = properties.collect {
      case (ConfKeyPattern(key), value) => key -> value
    }

    val variables = queries.flatMap(q => VariablePattern.findAllMatchIn(q).map(_.group(1))).toSet

    val jobName = file.getName.replace(".job", "")

    Some(SparkSqlJob(
      name = jobName,
      filePath = filePath,
      jobType = jobType,
      queries = queries,
      dependencies = dependencies,
      sparkConf = sparkConf,
      variables = variables
    ))
  }

  def findSparkSqlJobs(jobsDir: String): Seq[SparkSqlJob] = {
    val dir = new File(jobsDir)
    if (!dir.exists() || !dir.isDirectory) return Seq.empty

    findJobFiles(dir).flatMap(f => parseJobFile(f.getAbsolutePath))
  }

  private def findJobFiles(dir: File): Seq[File] = {
    val files = dir.listFiles() match {
      case null => Array.empty[File]
      case fs => fs
    }
    val jobFiles = files.filter(f => f.isFile && f.getName.endsWith(".job"))
    val subDirFiles = files.filter(_.isDirectory).flatMap(findJobFiles)
    (jobFiles ++ subDirFiles).toSeq
  }

  private def parseProperties(lines: List[String]): Map[String, String] = {
    val result = mutable.LinkedHashMap[String, String]()
    var currentKey: Option[String] = None
    var currentValue = new StringBuilder

    for (line <- lines) {
      val trimmed = line.trim
      if (trimmed.isEmpty || trimmed.startsWith("#")) {
        // 如果遇到空行并且当前正在解析续行，结束当前属性
        if (trimmed.isEmpty && currentKey.isDefined) {
          result(currentKey.get) = currentValue.toString().trim
          currentKey = None
          currentValue = new StringBuilder
        }
        // skip comments and blank lines
      } else if (currentKey.isDefined) {
        // continuation line
        if (trimmed.endsWith("\\")) {
          currentValue.append(trimmed.dropRight(1)).append(" ")
        } else {
          currentValue.append(" ").append(trimmed)
          result(currentKey.get) = currentValue.toString().trim
          currentKey = None
          currentValue = new StringBuilder
        }
      } else {
        val eqIdx = trimmed.indexOf('=')
        if (eqIdx > 0) {
          val key = trimmed.substring(0, eqIdx).trim
          val value = trimmed.substring(eqIdx + 1).trim
          if (value.endsWith("\\")) {
            currentKey = Some(key)
            currentValue = new StringBuilder(value.dropRight(1)).append(" ")
          } else {
            result(key) = value
          }
        }
      }
    }

    // flush any remaining continuation
    if (currentKey.isDefined) {
      result(currentKey.get) = currentValue.toString().trim
    }

    result.toMap
  }

  private def extractQueries(properties: Map[String, String]): Seq[String] = {
    properties.toSeq
      .collect { case (QueryKeyPattern(num), sql) => (num.toInt, sql) }
      .sortBy(_._1)
      .map(_._2)
  }
}
