package com.netease.easyml.ml.metric.classification

import com.netease.easyml.annotation.Register
import com.netease.easyml.ml.metric.Metric
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator
import org.apache.spark.ml.param.shared.{HasLabelCol, HasRawPredictionCol}
import org.apache.spark.ml.util.{DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.Dataset

/**
 * Created by linjiuning on 2020/7/15.
 */
@Register(alias = Array("areaUnderPR"))
class AreaUnderPR(override val uid: String) extends Metric with HasRawPredictionCol with HasLabelCol with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("areaUnderPR"))

  override lazy val shortName: String = "areaUnderPR"

  def setRawPredictionCol(value: String): this.type = set(rawPredictionCol, value)

  def setLabelCol(value: String): this.type = set(labelCol, value)

  override def evaluate(dataset: Dataset[_]): Double = {
    new BinaryClassificationEvaluator()
      .setMetricName("areaUnderPR")
      .setRawPredictionCol($(rawPredictionCol))
      .setLabelCol($(labelCol))
      .evaluate(dataset)
  }
}
