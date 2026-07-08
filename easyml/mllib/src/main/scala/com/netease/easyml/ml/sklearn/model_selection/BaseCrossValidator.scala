package com.netease.easyml.ml.sklearn.model_selection

import org.apache.spark.ml.param.Params
import org.apache.spark.sql.{DataFrame, Dataset}

/**
 * Created by linjiuning on 2020/8/19.
 */
abstract class BaseCrossValidator extends Params {
  def getNSplits: Int

  def split(dataset: Dataset[_]): Iterator[(DataFrame, DataFrame)]
}
