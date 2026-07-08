package com.netease.easyml.ml.sklearn.model_selection

import com.netease.easyml.ml.param.HasRandomState
import org.apache.spark.ml.param.{DoubleParam, IntParam, ParamValidators, Params}

/**
 * Created by linjiuning on 2020/8/10.
 */
trait BaseShuffleSplitParams extends Params with HasRandomState {
  val nSplits: IntParam = new IntParam(this, "nSplits",
    "Number of re-shuffling & splitting iterations.", ParamValidators.gt(0))

  val testSize: DoubleParam = new DoubleParam(this, "testSize",
    "If float, should be between 0.0 and 1.0 and represent the proportion of the dataset to include in the test split. " +
      "If int, represents the absolute number of test samples.", ParamValidators.gt(0.0))

  def getTestSize: Double = $(testSize)

  val trainSize: DoubleParam = new DoubleParam(this, "trainSize", "If float, should be between 0.0 and 1.0 and represent the proportion of the dataset to include in the train split. " +
    "If int, represents the absolute number of train samples.", ParamValidators.gt(0.0))

  def getTrainSize: Double = $(trainSize)
}

abstract class BaseShuffleSplit extends BaseCrossValidator with BaseShuffleSplitParams {

  def setNSplits(value: Int): this.type = set(nSplits, value)

  override def getNSplits: Int = $(nSplits)

  def setTrainSize(value: Double): this.type = set(trainSize, value)

  def setTestSize(value: Double): this.type = set(testSize, value)

  def setRandomState(value: Long): this.type = set(randomState, value)
}
