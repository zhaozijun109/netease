package com.netease.music.da.transfer.common.writer

import com.netease.music.da.transfer.common.Pluggable
import com.netease.music.da.transfer.common.conf.CommonConstants
import com.netease.music.da.transfer.common.log.LogTrait
import org.apache.spark.sql.{DataFrame, SparkSession}

trait DataWriter extends Pluggable {
  def write(data: DataFrame)
}

object DataWriter extends LogTrait {
  def apply(spark: SparkSession): DataWriter = {
    val sparkConf = spark.sparkContext.getConf
    val className = sparkConf.get(CommonConstants.WRITER_TYPE)
    LOG.info(s"Construct data writer $className")
    val clazz = Class.forName(className)
    val constructor = clazz.getConstructor(classOf[SparkSession])
    constructor.newInstance(spark).asInstanceOf[DataWriter]
  }
}
