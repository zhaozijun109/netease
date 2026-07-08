package com.netease.easyml.ml.metric.regression

import com.netease.easyml.annotation.Register
import com.netease.easyml.ml.metric.Metric
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.param.shared.{HasLabelCol, HasPredictionCol}
import org.apache.spark.ml.util.{DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.Dataset

/**
 * Created by linjiuning on 2020/7/16.
 * Mean squared error regression loss
 */
@Register(alias = Array("r2_score", "r2"))
class R2Score(override val uid: String) extends Metric
  with HasPredictionCol with HasLabelCol
  with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("r2_score"))

  override lazy val shortName: String = "r2_score"

  def setPredictionCol(value: String): this.type = set(predictionCol, value)

  def setLabelCol(value: String): this.type = set(labelCol, value)


  override def evaluate(dataset: Dataset[_]): Double = {
    new RegressionEvaluator()
      .setPredictionCol($(predictionCol))
      .setLabelCol($(labelCol))
      .setMetricName("r2")
      .evaluate(dataset)
  }

  override def isLargerBetter: Boolean = true
}

