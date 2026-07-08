package com.netease.easyml.ml.sklearn.feature_selection

import com.netease.easyml.ml.param.HasRandomState
import com.netease.easyml.ml.sklearn.model_selection.ShuffleSplit
import org.apache.spark.ml.evaluation.Evaluator
import org.apache.spark.ml.linalg.{DenseVector, SparseVector, Vector, Vectors}
import org.apache.spark.ml.param._
import org.apache.spark.ml.param.shared._
import org.apache.spark.ml.util.{DefaultParamsReadable, Identifiable}
import org.apache.spark.ml.{Estimator, Transformer}
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.functions.{col, udf}

import scala.collection.mutable

/**
 * Created by linjiuning on 2020/11/3.
 */

trait RFEParams extends Params with HasLabelCol with HasRandomState {

  val estimator: Param[Estimator[_]] = new Param(this, "estimator", "estimator for selection")

  def getEstimator: Estimator[_] = $(estimator)

  val evaluator: Param[Evaluator] = new Param(this, "evaluator",
    "evaluator used to select hyper-parameters that maximize the validated metric")

  def getEvaluator: Evaluator = $(evaluator)

  val trainRatio: DoubleParam = new DoubleParam(this, "trainRatio",
    "ratio between training set and validation set (>= 0 && <= 1)", ParamValidators.inRange(0, 1))

  def getTrainRatio: Double = $(trainRatio)

  val nFeaturesToSelect: IntParam = new IntParam(this, "nFeaturesToSelect",
    "The number of features to select. If `None`, half of the features are selected.", ParamValidators.gt(0))

  def getNFeaturesToSelect: Int = $(nFeaturesToSelect)

  val step: DoubleParam = new DoubleParam(this, "step",
    "If greater than or equal to 1, then ``step`` corresponds to the " +
      "(integer) number of features to remove at each iteration. " +
      "If within (0.0, 1.0), then ``step`` corresponds to the percentage " +
      "(rounded down) of features to remove at each iteration.", ParamValidators.gtEq(0))

  def getStep: Double = $(step)

  setDefault(trainRatio -> 0.75, step -> 1)
}

class RFE(override val uid: String) extends FeatureSelector(uid) with RFEParams {

  import RFE._

  def this() = this(Identifiable.randomUID("rfe"))

  setDefault(selectorType -> FeatureSelector.Threshold)

  def setLabelCol(value: String): this.type = set(labelCol, value)

  def setEstimator(value: Estimator[_]): this.type = set(estimator, value)

  def setEvaluator(value: Evaluator): this.type = set(evaluator, value)

  def setTrainRatio(value: Double): this.type = set(trainRatio, value)

  def setNFeaturesToSelect(value: Int): this.type = set(nFeaturesToSelect, value)

  def setStep(value: Double): this.type = set(step, value)

  var scores: Array[Double] = _
  var ranking: Array[Int] = _

  def rfe(train: Dataset[_], test: Dataset[_]): Array[Double] = {
    val estimator = getEstimator
    estimator.set(estimator.getParam("featuresCol"), $(featuresCol))
      .set(estimator.getParam("labelCol"), $(labelCol))

    val evaluator = getEvaluator

    val nFeaturesToSelect_ = if (isSet(nFeaturesToSelect)) {
      $(nFeaturesToSelect)
    } else {
      numFeatures / 2
    }

    val step_ = if (getStep >= 1) {
      getStep.toInt
    } else {
      (getStep * numFeatures).toInt
    }

    val scores_ = mutable.ArrayBuilder.make[Double]
    var featureImportance = Array.fill(numFeatures)(0.0)
    val support = Array.fill(numFeatures)(1)
    val ranking_ = Array.fill(numFeatures)(1)

    if (nFeaturesToSelect_ >= numFeatures) {
      val model = estimator.fit(train).asInstanceOf[Transformer]
      featureImportance = SelectFromModel.modelFeatureImportance(model)
    } else {
      while (support.sum > nFeaturesToSelect_) {
        val indices = support.zipWithIndex.filter(it => it._1 == 1).map(_._2)
        val selectFunc = udf(select(indices) _)
        val trainDf = train.withColumn($(featuresCol), selectFunc(col($(featuresCol))))

        val model = estimator.fit(trainDf).asInstanceOf[Transformer]

        val importance = SelectFromModel.modelFeatureImportance(model)

        val threshold = Math.min(step_, support.sum - nFeaturesToSelect_)

        val sorted = indices.zip(importance).sortBy(_._2)
        sorted.slice(0, threshold).foreach {
          case (i, d) =>
            support(i) = 0
        }

        sorted.slice(threshold, sorted.length).foreach {
          case (i, d) =>
            featureImportance(i) = d
        }

        support.zipWithIndex.foreach {
          case (i, flag) =>
            if (flag == 0) {
              ranking_(i) += 1
            }
        }

        if (evaluator != null && test != null) {
          val testDf = test.withColumn($(featuresCol), selectFunc(col($(featuresCol))))
          val resultDf = model.transform(testDf)
          var score = evaluator.evaluate(resultDf)
          if (!evaluator.isLargerBetter) {
            score = -score
          }
          scores_ += score
        }
      }
    }

    support.zipWithIndex.foreach {
      case (i, flag) =>
        if (flag == 0) {
          featureImportance(i) = 0
        }
    }
    scores = scores_.result()
    ranking = ranking_
    featureImportance
  }

  override def computeImportance(dataset: Dataset[_]): Array[Double] = {
    val (train, test) = if (isSet(trainRatio) && $(trainRatio) < 1) {
      val validator = new ShuffleSplit()
        .setNSplits(1)
        .setTrainSize($(trainRatio))
        .setRandomState($(randomState))

      validator.split(dataset).next()
    } else {
      (dataset, null)
    }
    rfe(train, test)
  }
}


object RFE extends DefaultParamsReadable[RFE] {
  override def load(path: String): RFE = super.load(path)

  def select(indices: Array[Int])(vector: Vector): Vector = {
    vector match {
      case DenseVector(array) =>
        val newArr = indices.map(i => array(i))
        Vectors.dense(newArr)
      case SparseVector(size, idx, values) =>
        val newIdx = mutable.ArrayBuilder.make[Int]
        val newValues = mutable.ArrayBuilder.make[Double]

        var i = 0
        var j = 0

        while (i < indices.length && j < idx.length) {
          val ti = indices(i)
          val tj = idx(j)
          if (ti == tj) {
            i += 1
            j += 1
            newIdx += tj
            newValues += values(tj)
          } else if (ti < tj) {
            i += 1
          } else {
            j += 1
          }
        }
        Vectors.sparse(indices.length, newIdx.result(), newValues.result())
    }
  }
}