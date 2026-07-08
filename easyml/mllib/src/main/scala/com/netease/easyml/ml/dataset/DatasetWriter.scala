package com.netease.easyml.ml.dataset

import com.netease.easyml.common.collection.{Params => JParams}
import org.apache.spark.ml.param.{ParamMap, Params}
import org.apache.spark.sql.DataFrame

/**
 * Created by linjiuning on 2020/7/7.
 */

abstract class DatasetWriter extends Params {

  def write(dataFrame: DataFrame, params: JParams)

  override def copy(extra: ParamMap): Params = {
    defaultCopy(extra)
  }
}
