package com.netease.easyml.ml

import com.netease.easyml.common.util.{DateUtil, SparkUtil}
import org.apache.spark.sql.SparkSession


/**
 * Created by linjiuning on 2020/7/8.
 */
package object dataset {
  val PREFIX = "easyml."
  val VARIABLE = "\\$\\{(.*?)}".r

  val VAR_PLACEHOLDER: Array[String] = Array(
    "easyml.latest", // （最新的分区）
    "easyml.N.days.ago", // （程序运行时的前N天，格式：yyyy-MM-dd）
    "easyml.N.month.ago", // （程序运行时的前N个月的第一天，格式：yyyy-MM-01）
    "easyml.current.date", // （程序运行时的当前日期，格式：yyyy-MM-dd）
    "easyml.current.month", // （程序运行时的当前月的第一天，格式：yyyy-MM-01）
    "easyml.start.year", // （程序运行时间的年份，格式：yyyy）
    "easyml.start.month", // （程序运行时间的月份，格式：MM）
    "easyml.start.day", // （程序运行时间的日期，格式：dd）
    "easyml.start.hour", // （程序运行时间的小时数，格式：HH）
    "easyml.start.minute", // （程序运行时间的分钟数，格式：mm）
    "easyml.start.second", // （程序运行时间的秒数，格式：ss）
    "easyml.start.milliseconds" // （程序运行时间的毫秒数，格式：SSS）
  )

  def dynamicVariable(keys: Array[String]): Map[String, String] = {
    var entry = Map.empty[String, String]
    val pt1 = """([a-z]+).([a-z]+)""".r
    val pt2 = """([0-9]+).([a-z]+).ago""".r
    for (key <- keys if key.startsWith(PREFIX)) {
      val value = key.substring(PREFIX.length) match {
        case pt1(action, unit) =>
          action match {
            case "current" =>
              unit match {
                case "date" =>
                  DateUtil.getToday("yyyy-MM-dd")
                case "month" =>
                  DateUtil.getToday("yyyy-MM") + "-" + "01"
                case _ =>
                  ""
              }
            case "start" =>
              unit match {
                case "year" =>
                  DateUtil.getToday("yyyy")
                case "month" =>
                  DateUtil.getToday("MM")
                case "day" =>
                  DateUtil.getToday("dd")
                case "hour" =>
                  DateUtil.getToday("HH")
                case "minute" =>
                  DateUtil.getToday("mm")
                case "second" =>
                  DateUtil.getToday("ss")
                case "milliseconds" =>
                  DateUtil.getToday("SSS")
                case _ =>
                  ""
              }
            case _ =>
              ""
          }
        case pt2(nStr, unit) =>
          val n = -nStr.toInt
          unit match {
            case "day" | "days" =>
              DateUtil.dateOffset(DateUtil.getToday("yyyy-MM-dd"), n, "yyyy-MM-dd")
            case "month" | "months" =>
              DateUtil.monthOffset(DateUtil.getToday("yyyy-MM"), n, "yyyy-MM") + "-" + "01"
            case _ =>
              ""
          }
        case _ =>
          ""
      }
      if (value.nonEmpty) {
        entry += (key -> value)
      }
    }
    entry
  }

  private def variables(sql: String): Array[String] = {
    VARIABLE.findAllMatchIn(sql)
      .map(it => it.group(1))
      .toArray
      .distinct
  }

  private def render(sql: String, params: Map[String, String]): String = {
    var res = sql
    for ((key, value) <- params) {
      val nKey = key.replaceAll("\\.", "\\\\.")
      res = res.replaceAll("\\$\\{\\s*%s\\s*}".format(nKey), value)
    }
    res
  }

  def sqlRender(spark: Option[SparkSession], sql: String, params: Map[String, String]): String = {
    var entry = Map.empty[String, String]
    var vars = variables(sql)
    vars = vars.filter(it => !params.contains(it))
    val table = params.getOrElse("path", params.getOrElse("table", ""))
    entry += ("table" -> table)
    if (vars.contains("easyml.latest") && spark.nonEmpty) {
      if (!table.isEmpty) {
        SparkUtil.getLatestPart(spark.get, table).foreach(it => entry += ("easyml.latest" -> it._2))
      }
    }
    val map = dynamicVariable(vars)
    entry ++= map
    for ((key, value) <- params) {
      if (!entry.contains(key))
        entry += (key -> value)
    }
    render(sql, entry)
  }
}
