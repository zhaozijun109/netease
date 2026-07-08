package com.netease.music.da.transfer.common.reader

import com.netease.music.da.transfer.common.Pluggable
import com.netease.music.da.transfer.common.conf.CommonConstants
import com.netease.music.da.transfer.common.log.LogTrait
import org.apache.spark.sql.{DataFrame, SparkSession}

trait DataReader extends Pluggable {
  def read(): DataFrame
}

object DataReader extends LogTrait {
  def apply(spark: SparkSession): DataReader = {
    val sparkConf = spark.sparkContext.getConf
    val className = sparkConf.get(CommonConstants.READER_TYPE)
    LOG.info(s"Construct data reader $className")
    val clazz = Class.forName(className)
    val constructor = clazz.getConstructor(classOf[SparkSession])
    constructor.newInstance(spark).asInstanceOf[DataReader]
  }
}
