package com.netease.lofter.etl.sparksqltest

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object VariableSubstitution {

  private val VariablePattern = """\$\{([^}]+)\}""".r
  private val DaysAgoPattern = """azkaban\.flow\.(\d+)\.days\.ago""".r
  private val DateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  def substitute(sql: String, baseDate: LocalDate = LocalDate.now().minusDays(1)): String = {
    VariableSubstitution.substituteWithVars(sql, buildDefaultVars(baseDate))
  }

  def substituteWithVars(sql: String, vars: Map[String, String]): String = {
    VariablePattern.replaceAllIn(sql, m => {
      val varName = m.group(1)
      java.util.regex.Matcher.quoteReplacement(
        vars.getOrElse(varName, m.matched)
      )
    })
  }

  def buildDefaultVars(baseDate: LocalDate): Map[String, String] = {
    val vars = scala.collection.mutable.Map[String, String]()

    vars("azkaban.flow.current.date") = baseDate.format(DateFormat)

    for (days <- 1 to 365) {
      vars(s"azkaban.flow.$days.days.ago") = baseDate.minusDays(days).format(DateFormat)
    }

    vars.toMap
  }

  def extractVariables(sql: String): Set[String] = {
    VariablePattern.findAllMatchIn(sql).map(_.group(1)).toSet
  }
}
