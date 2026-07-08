package com.netease.music.da.transfer.common.handler

import com.netease.music.da.transfer.common.Pluggable
import com.netease.music.da.transfer.common.conf.CommonConstants
import com.netease.music.da.transfer.common.log.LogTrait
import org.apache.spark.sql.{DataFrame, SparkSession}

trait DataHandler extends Pluggable {
  def handler(dataFrame: DataFrame): DataFrame
}

object DataHandler extends LogTrait {
  def apply(spark: SparkSession): DataHandler = {
    val sparkConf = spark.sparkContext.getConf
    val className = sparkConf.get(CommonConstants.HANDLER_TYPE)
    LOG.info(s"Construct data handler $className")
    val clazz = Class.forName(className)
    val constructor = clazz.getConstructor(classOf[SparkSession])
    constructor.newInstance(spark).asInstanceOf[DataHandler]
  }
}