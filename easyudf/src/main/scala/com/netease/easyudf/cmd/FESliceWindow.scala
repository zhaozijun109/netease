package com.netease.easyudf.cmd

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.serializer.SerializeFilter
import com.netease.easyml.common.cmds.UserDefinedCmd
import com.netease.easyml.common.util.{IOUtil, SparkUtil}
import com.netease.easyudf.pojo.FESliceWindowConfig
import org.apache.commons.lang3.StringUtils
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.collection.JavaConverters._

case class FESliceWindowArgs(input: String, config: String, output: String = null, day: String,
                             format: String = null, debug: Boolean = false, compact: Boolean = true,
                             cubeFilter: String = null, partition: Int = 0)

class FESliceWindow extends UserDefinedCmd[FESliceWindowArgs] {

  override def apply(spark: SparkSession, args: FESliceWindowArgs): DataFrame = {
    val engineConfig = IOUtil.smartReadConfig(args.config, classOf[FESliceWindowConfig])
    engineConfig.setCompact(args.compact)
    engineConfig.setDay(args.day)
    if (StringUtils.isNoneBlank(args.output)) {
      SparkUtil.setDynamicPartition(spark)
      engineConfig.setOutput(args.output)
    }
    if (StringUtils.isNoneBlank(args.format)) {
      engineConfig.setFormat(args.format)
    }
    val entity = engineConfig.getEntity.asScala.flatMap(_.getExpr.split(",").map(_.trim).filter(_.nonEmpty)).toSet
    val info = SparkUtil.getHiveTableInfo(spark, args.input)
    val useCube = info.fields.exists(it => entity.contains(it.name) && it.dtype.startsWith("array<"))
    engineConfig.setUseCube(useCube)
    engineConfig.setCubeFilter(args.cubeFilter)
    val inc = (info.fields ++ info.partitionFields).exists(it => it.name.equals(FESliceWindowConfig.ENTITY))
    engineConfig.setInc(inc)
    engineConfig.setPartition(args.partition)
    println(s"CONFIG: ${JSON.toJSONString(engineConfig, new Array[SerializeFilter](0))}")
    val sql = engineConfig.sql(args.input)
    println(s"SQL: $sql")
    if (args.debug) {
      null
    } else {
      SparkUtil.sqlText(spark, sql)
    }
  }

}
