package com.netease.easyudf.cmd

import com.netease.easyml.common.cmds.UserDefinedCmd
import com.netease.easyml.common.util.SparkUtil.Field
import com.netease.easyml.common.util.{Cmds, SparkUtil}
import org.apache.commons.lang3.StringUtils
import org.apache.spark.sql.{DataFrame, SparkSession}

case class CreateTableArgs(input: String, output: String, format: String = "parquet", partitions: String = "", properties: String = "", compact: Boolean = false)

class CreateTable extends UserDefinedCmd[CreateTableArgs] {

  def render(field: Field, compact: Boolean): String = {
    var dtype = field.dtype
    if (compact) {
      dtype = dtype.replaceAll("double", "float")
        .replaceAll("bigint", "int")
        .replaceAll("decimal\\(\\d+,\\d+\\)", "float")
    }
    if (StringUtils.isNoneBlank(field.comment)) {
      s"${field.name} $dtype comment '${field.comment}'"
    } else {
      s"${field.name} $dtype"
    }
  }

  override def apply(spark: SparkSession, args: CreateTableArgs): DataFrame = {
    val tableInfo = SparkUtil.getHiveTableInfo(spark, args.input)
    var fields = tableInfo.fields
    var partitionFields = tableInfo.partitionFields

    var partitionsExpr = ""
    if (StringUtils.isNoneBlank(args.partitions)) {
      var pts = partitionFields.map(_.name).toSet
      partitionFields = partitionFields ++ args.partitions.split(",").filterNot(it => pts.contains(it)).map(it => Field(it, "string", ""))
      pts = partitionFields.map(_.name).toSet
      fields = fields.filterNot(it => pts.contains(it.name))
      partitionsExpr = partitionFields.map(it => render(it, args.compact)).mkString(", ")
      partitionsExpr = s"partitioned by($partitionsExpr)"
    }
    val expr = fields.map(it => render(it, args.compact)).mkString(", ")
    val sql = s"create table if not exists ${args.output}($expr) $partitionsExpr stored as ${args.format} ${args.properties}"
    Cmds.run(spark, sql)
  }

}
