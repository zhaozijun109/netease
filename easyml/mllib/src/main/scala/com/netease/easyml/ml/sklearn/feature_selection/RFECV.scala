package com.netease.easyml.ml.sklearn.feature_selection

import com.netease.easyml.ml.param.HasRandomState
import com.netease.easyml.ml.sklearn.model_selection.KFold
import org.apache.spark.ml.Estimator
import org.apache.spark.ml.evaluation.Evaluator
import org.apache.spark.ml.param._
import org.apache.spark.ml.param.shared._
import org.apache.spark.ml.util.{DefaultParamsReadable, Identifiable}
import org.apache.spark.sql.Dataset

/**
 * Created by linjiuning on 2020/11/3.
 */

trait RFECVParams extends Params with HasLabelCol with HasRandomState {

  val estimator: Param[Estimator[_]] = new Param(this, "estimator", "estimator for selection")

  def getEstimator: Estimator[_] = $(estimator)

  val evaluator: Param[Evaluator] = new Param(this, "evaluator",
    "evaluator used to select hyper-parameters that maximize the validated metric")

  def getEvaluator: Evaluator = $(evaluator)

  val cv: IntParam = new IntParam(this, "cv",
    "number of folds for cross validation (>= 2)", ParamValidators.gtEq(2))

  def getCv: Int = $(cv)

  val minFeaturesToSelect: IntParam = new IntParam(this, "minFeaturesToSelect",
    "The minimum number of features to be selected. This number of features will always be scored, even if the difference between the original feature count and ``min_features_to_select`` isn't divisible by ``step``.", ParamValidators.gt(0))

  def getMinFeaturesToSelect: Int = $(minFeaturesToSelect)

  val step: DoubleParam = new DoubleParam(this, "step",
    "If greater than or equal to 1, then ``step`` corresponds to the " +
      "(integer) number of features to remove at each iteration. " +
      "If within (0.0, 1.0), then ``step`` corresponds to the percentage " +
      "(rounded down) of features to remove at each iteration.", ParamValidators.gtEq(0))

  def getStep: Double = $(step)

  setDefault(cv -> 3, step -> 1, minFeaturesToSelect -> 1, randomState -> 90)
}

class RFECV(override val uid: String) extends FeatureSelector(uid) with RFECVParams {

  def this() = this(Identifiable.randomUID("rfeCv"))

  setDefault(selectorType -> FeatureSelector.Threshold)

  def setLabelCol(value: String): this.type = set(labelCol, value)

  def setEstimator(value: Estimator[_]): this.type = set(estimator, value)

  def setEvaluator(value: Evaluator): this.type = set(evaluator, value)

  def setCv(value: Int): this.type = set(cv, value)

  def setMinFeaturesToSelect(value: Int): this.type = set(minFeaturesToSelect, value)

  def setStep(value: Double): this.type = set(step, value)

  override def computeImportance(dataset: Dataset[_]): Array[Double] = {
    val validator = new KFold()
      .setNSplits($(cv))
      .setRandomState($(randomState))

    val scores = validator.split(dataset).map {
      case (train, test) =>
        val rfe = new RFE()
          .setLabelCol($(labelCol))
          .setNFeaturesToSelect($(minFeaturesToSelect))
          .setEstimator($(estimator))
          .setEvaluator($(evaluator))
          .setStep($(step))
        rfe.numFeatures = numFeatures
        rfe.rfe(train, test)
        rfe.scores
    }.toArray

    val sumScores = Array.fill(scores(0).length)(0.0)

    scores.foreach(sc => sc.indices.foreach(i => sumScores(i) += sc(i)))

    val step_ = if (getStep > 1) {
      getStep.toInt
    } else {
      (getStep * numFeatures).toInt
    }

    val argmaxIdx = sumScores.length - sumScores.reverse.zipWithIndex.maxBy(_._1)._2 - 1

    val nFeaturesToSelect = Math.max(numFeatures - argmaxIdx * step_, $(minFeaturesToSelect))
    val rfe = new RFE()
      .setLabelCol($(labelCol))
      .setNFeaturesToSelect(nFeaturesToSelect)
      .setEstimator($(estimator))
      .setEvaluator($(evaluator))
      .setStep($(step))
      .setTrainRatio(1.0)
    rfe.computeImportance(dataset)
  }
}

object RFECV extends DefaultParamsReadable[RFECV] {
  override def load(path: String): RFECV = super.load(path)
}