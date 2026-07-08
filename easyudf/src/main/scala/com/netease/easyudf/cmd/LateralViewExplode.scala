package com.netease.easyudf.cmd

import com.netease.easyml.common.cmds.UserDefinedCmd
import com.netease.easyml.common.util.Cmds
import org.apache.spark.sql.{DataFrame, SparkSession}

case class LateralViewExplodeArgs(input: String, keys: String)

class LateralViewExplode extends UserDefinedCmd[LateralViewExplodeArgs] {

  override def apply(spark: SparkSession, args: LateralViewExplodeArgs): DataFrame = {
    val columns = spark.table(args.input).columns
    var sql = ""
    args.keys.split(",").foreach(key => {
      val expr = columns.map(it => if (it.equals(key)) s"tmp.$it as $it" else it).mkString(",")
      val from = if (sql.isEmpty) args.input else s"($sql)t"
      sql = s"select $expr from $from lateral view explode($key)tmp as $key"
    })
    Cmds.run(spark, sql)
  }

}
