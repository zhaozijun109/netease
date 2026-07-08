package com.netease.easyml.ml.dataset

import com.netease.easyml.common.collection.{Params => JParams}
import org.apache.spark.ml.param.{ParamMap, Params}
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
 * Created by linjiuning on 2020/7/6.
 */

abstract class DatasetReader extends Params {

  def read(spark: SparkSession, params: JParams): DataFrame

  override def copy(extra: ParamMap): Params = {
    defaultCopy(extra)
  }
}
