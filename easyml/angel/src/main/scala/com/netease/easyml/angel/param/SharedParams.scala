package com.netease.easyml.angel.param

import org.apache.spark.ml.param._

/**
 * Created by linjiuning on 2020/7/1.
 */

trait HasNumEstimator extends Params {

  /**
   * Param for num estimator.
   *
   * @group param
   */
  final val numEstimator: IntParam = new IntParam(this, "numEstimator",
    "num estimator for ensemble model")

  /** @group getParam */
  final def getNumEstimator: Int = $(numEstimator)
}