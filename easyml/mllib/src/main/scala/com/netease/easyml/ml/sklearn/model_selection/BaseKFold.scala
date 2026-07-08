package com.netease.easyml.ml.sklearn.model_selection

import com.netease.easyml.ml.param.HasRandomState
import org.apache.spark.ml.param.{BooleanParam, IntParam, ParamValidators, Params}

/**
 * Created by linjiuning on 2020/8/19.
 */
trait BaseKFoldParams extends Params with HasRandomState {
  val nSplits: IntParam = new IntParam(this, "nSplits",
    "Number of folds. Must be at least 2.", ParamValidators.gtEq(2))

  val shuffle: BooleanParam = new BooleanParam(this, "shuffle",
    "Whether to shuffle each class’s samples before splitting into batches. Note that the samples within each split will not be shuffled.")

  def getShuffle: Boolean = $(shuffle)
}

abstract class BaseKFold extends BaseCrossValidator with BaseKFoldParams {

  def setNSplits(value: Int): this.type = set(nSplits, value)

  override def getNSplits: Int = $(nSplits)

  def setShuffle(value: Boolean): this.type = set(shuffle, value)

  def setRandomState(value: Long): this.type = set(randomState, value)
}