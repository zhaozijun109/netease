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
@Register(alias = Array("mean_squared_error", "mse"))
class MeanSquaredError(override val uid: String) extends Metric
  with HasPredictionCol with HasLabelCol
  with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("mean_squared_error"))

  override lazy val shortName: String = "mean_squared_error"

  def setPredictionCol(value: String): this.type = set(predictionCol, value)

  def setLabelCol(value: String): this.type = set(labelCol, value)


  override def evaluate(dataset: Dataset[_]): Double = {
    new RegressionEvaluator()
      .setPredictionCol($(predictionCol))
      .setLabelCol($(labelCol))
      .setMetricName("mse")
      .evaluate(dataset)
  }

  override def isLargerBetter: Boolean = false
}

