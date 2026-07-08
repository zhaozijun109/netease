package com.netease.easyml.ml.metric

import com.netease.easyml.common.collection.{Params => JParams}
import org.apache.spark.ml.evaluation.Evaluator
import org.apache.spark.ml.param.ParamMap
import org.apache.spark.sql.Dataset

/**
 * Created by linjiuning on 2020/7/6.
 * Based on spark's evaluator api.
 */
abstract class Metric extends Evaluator {

  protected lazy val shortName: String = this.getClass.getSimpleName.toLowerCase

  /**
   * Evaluates model output and returns a json metric.
   *
   * @param dataset  a dataset that contains labels/observations and predictions.
   * @param paramMap parameter map that specifies the input columns and output metrics
   * @return metric
   */
  def evaluateJson(dataset: Dataset[_], paramMap: ParamMap): JParams = {
    this.copy(paramMap).evaluateJson(dataset)
  }

  /**
   * Evaluates model output and returns a json metric.
   *
   * @param dataset a dataset that contains labels/observations and predictions.
   * @return metric
   */
  def evaluateJson(dataset: Dataset[_]): JParams = {
    val metric = evaluate(dataset)
    val jParams = new JParams()
    jParams.put(shortName, metric)
    jParams
  }

  override def copy(extra: ParamMap): Metric = defaultCopy(extra)
}
