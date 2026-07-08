package com.netease.easyml.ml.metric.classification

import com.netease.easyml.annotation.Register
import com.netease.easyml.common.collection.Params
import com.netease.easyml.ml.metric.Metric
import org.apache.spark.ml.util.{DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.Dataset

/**
 * Created by linjiuning on 2020/7/17.
 */
@Register(alias = Array("recall", "recall_score", "recall-score"))
class RecallScore(override val uid: String) extends Metric
  with FScoreBaseParams with DefaultParamsWritable {
  def this() = this(Identifiable.randomUID("recall"))

  override lazy val shortName: String = "recall"

  def setPredictionCol(value: String): this.type = set(predictionCol, value)

  def setLabelCol(value: String): this.type = set(labelCol, value)

  def setAverage(value: String): this.type = set(average, value)

  override def evaluate(dataset: Dataset[_]): Double = {
    if ($(average).equals("none")) {
      throw new IllegalArgumentException("the result of average = none is not a numeric.")
    }
    evaluateJson(dataset).get(shortName, 0.0)
  }

  override def evaluateJson(dataset: Dataset[_]): Params = {
    val score = new FBetaScore()
      .setAverage($(average))
      .setBeta(1.0)
      .setPredictionCol($(predictionCol))
      .setLabelCol($(labelCol))
    val param = score.evaluateJson(dataset)

    val precision = param.get(shortName)

    val result = new Params();
    result.put(shortName, precision)
    result
  }
}
