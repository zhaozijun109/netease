package com.netease.easyml.ml.metric.classification

import com.netease.easyml.annotation.Register
import com.netease.easyml.common.collection.{Params => JParams}
import com.netease.easyml.ml.metric.{HasAverageType, Metric}
import org.apache.spark.ml.param.shared.{HasLabelCol, HasPredictionCol}
import org.apache.spark.ml.param.{DoubleParam, ParamValidators, Params}
import org.apache.spark.ml.util.{DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.Dataset

/**
 * Created by linjiuning on 2020/7/16.
 */

trait FScoreBaseParams extends Params with HasPredictionCol with HasLabelCol with HasAverageType {

  val zeroDivision = new DoubleParam(this, "beta",
    "Sets the value to return when there is a zero division.",
    ParamValidators.inArray(Array(0.0, 1.0)))

  def getZeroDivision: Double = $(zeroDivision)

  setDefault(zeroDivision, 0.0)
}

@Register(alias = Array("fscore", "f-score", "f_score"))
class FBetaScore(override val uid: String) extends Metric
  with FScoreBaseParams with DefaultParamsWritable {
  def this() = this(Identifiable.randomUID("fscore"))

  override lazy val shortName: String = "fscore"

  val beta = new DoubleParam(this, "beta",
    "The strength of recall versus precision in the F-score.",
    ParamValidators.gt(0.0))

  def getBeta: Double = $(beta)

  setDefault(beta, 1.0)

  def setPredictionCol(value: String): this.type = set(predictionCol, value)

  def setLabelCol(value: String): this.type = set(labelCol, value)

  def setAverage(value: String): this.type = set(average, value)

  def setBeta(value: Double): this.type = set(beta, value)

  override def evaluate(dataset: Dataset[_]): Double = {
    if ($(average).equals("none")) {
      throw new IllegalArgumentException("the result of average = none is not a numeric.")
    }
    evaluateJson(dataset).get(shortName, 0.0)
  }

  override def evaluateJson(dataset: Dataset[_]): JParams = {
    val confusion = new MultiLabelConfusionMatrix()
      .setPredictionCol($(predictionCol))
      .setLabelCol($(labelCol))
      .confusionMatrix(dataset)
    val n = confusion.length
    var tpSum = Array.fill[Double](n)(0)
    var predSum = Array.fill[Double](n)(0)
    var trueSum = Array.fill[Double](n)(0)

    for ((elem, i) <- confusion.zipWithIndex) {
      tpSum(i) += elem(1)(1)
      predSum(i) += tpSum(i) + elem(0)(1)
      trueSum(i) += tpSum(i) + elem(1)(0)
    }

    if ($(average).equals("micro")) {
      tpSum = Array(tpSum.sum)
      predSum = Array(predSum.sum)
      trueSum = Array(trueSum.sum)
    }

    val beta2 = $(beta) * $(beta)

    val precisions = tpSum.zip(predSum).map {
      case (tp, pred) =>
        if (pred == 0) 0.0 else tp / pred
    }

    val recalls = tpSum.zip(trueSum).map {
      case (tp, true_) =>
        if (true_ == 0) 0.0 else tp / true_
    }

    val fscores = precisions.zip(recalls).map {
      case (precision, recall) =>
        var denom = beta2 * precision + recall
        if (denom == 0)
          denom = 1.0
        (1 + beta2) * precision * recall / denom
    }

    val jParams = new JParams()
    if ($(average).equals("none")) {
      jParams.put(shortName, fscores)
      jParams.put("precision", precisions)
      jParams.put("recall", recalls)
    } else {
      val weights = $(average) match {
        case "weighted" =>
          trueSum
        case _ =>
          Array.fill[Double](fscores.length)(1.0)
      }
      val weightSum = weights.sum
      val (precision, recall, fscore) = if (weightSum == 0) {
        (0, 0, 0) //TODO
      } else {
        val precision = precisions.zip(weights).map {
          case (p, weight) =>
            p * weight / weightSum
        }.sum

        val recall = recalls.zip(weights).map {
          case (recall, weight) =>
            recall * weight / weightSum
        }.sum

        val fscore = fscores.zip(weights).map {
          case (f, weight) =>
            f * weight / weightSum
        }.sum
        (precision, recall, fscore)
      }

      jParams.put(shortName, fscore)
      jParams.put("precision", precision)
      jParams.put("recall", recall)
    }
    jParams
  }
}


