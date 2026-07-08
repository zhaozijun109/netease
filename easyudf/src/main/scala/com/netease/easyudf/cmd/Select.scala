package com.netease.easyudf.cmd

import com.netease.easyml.common.cmds.UserDefinedCmd
import com.netease.easyml.common.util.SparkUtil
import com.netease.easyudf.cmd.Select.filterColumns
import org.apache.commons.lang3.StringUtils
import org.apache.spark.sql.{DataFrame, SparkSession}

import java.util.regex.Pattern

case class SelectArgs(input: String, expr: String = "?", include: String = null, exclude: String = null, sql: String = "", filter: Boolean = false)

class Select extends UserDefinedCmd[SelectArgs] {

  override def apply(spark: SparkSession, args: SelectArgs): DataFrame = {
    val df = spark.table(args.input)
    val columns = filterColumns(df.columns, args.include, args.exclude)

    val expr = df.columns.filter(col => !args.filter || columns.contains(col))
      .map(col => if (columns.contains(col)) args.expr.replaceAll("\\?", col) + " as " + col else col).mkString(",")

    val sql = if (StringUtils.isBlank(args.sql)) {
      s"select $expr from ${args.input}"
    } else {
      args.sql.replaceAll("\\?", expr)
    }
    SparkUtil.sqlText(spark, sql)
  }

}


object Select {
  def filterColumns(columns: Array[String], include: String = null, exclude: String = null): Array[String] = {
    var newColumns = columns
    if (StringUtils.isNoneBlank(include)) {
      val pattern = Pattern.compile(include)
      newColumns = newColumns.filter(it => pattern.matcher(it).matches())
    }
    if (StringUtils.isNoneBlank(exclude)) {
      val pattern = Pattern.compile(exclude)
      newColumns = newColumns.filterNot(it => pattern.matcher(it).matches())
    }
    newColumns
  }

  def selectExpr(expr: String = "?", columns: Array[String]): Array[String] = {
    columns.map(col => expr.replaceAll("\\?", col) + " as " + col)
  }
}

class ApplyColumn extends Select