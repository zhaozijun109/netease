package com.netease.easyudf.cmd

import com.netease.easyml.common.cmds.UserDefinedCmd
import org.apache.spark.sql.{DataFrame, SparkSession}

case class ColumnRenamedArgs(input: String, prefix: String = "", suffix: String = "", regex: String = null,
                             replacement: String = null, include: String = null, exclude: String = null)

class ColumnRenamed extends UserDefinedCmd[ColumnRenamedArgs] {

  override def apply(spark: SparkSession, args: ColumnRenamedArgs): DataFrame = {
    val df = spark.table(args.input)
    val columns = Select.filterColumns(df.columns, args.include, args.exclude)
    var newDf = df
    columns.foreach(col => {
      var newCol = col
      if (args.regex != null && args.replacement != null) {
        newCol = newCol.replaceAll(args.regex, args.replacement)
      }
      newDf = newDf.withColumnRenamed(col, s"${args.prefix}$newCol${args.suffix}")
    })
    newDf
  }

}
