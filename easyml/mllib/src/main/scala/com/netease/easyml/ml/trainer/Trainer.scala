package com.netease.easyml.ml.trainer

import com.netease.easyml.common.collection.{Params => JParams}
import com.netease.easyml.ml.metric.Metric
import org.apache.spark.ml.param.Params
import org.apache.spark.ml.{Estimator, PipelineStage, Transformer}
import org.apache.spark.sql.DataFrame
import org.apache.spark.storage.StorageLevel

/**
 * Created by linjiuning on 2020/9/1.
 */
abstract class Trainer extends Params {
  protected var estimator: Estimator[_] = _
  protected var models: Array[Transformer] = Array.empty
  protected var metrics: Array[Metric] = Array()

  def setComponent(stage: PipelineStage): this.type = {
    if (classOf[Estimator[_]].isAssignableFrom(stage.getClass))
      setEstimator(stage.asInstanceOf[Estimator[_]])
    else
      setModels(stage.asInstanceOf[Transformer])
  }

  def setEstimator(estimator: Estimator[_]): this.type = {
    this.estimator = estimator
    this
  }

  def getEstimator: Estimator[_] = estimator

  def setModels(models: Transformer*): this.type = {
    this.models = models.toArray
    this
  }

  def getMetrics: Array[Metric] = metrics

  def setMetrics(metrics: Array[Metric]): this.type = {
    this.metrics = metrics
    this
  }

  def fit(trainDf: DataFrame): Unit

  def transform(testDf: DataFrame): DataFrame

  def fitTransform(trainDf: DataFrame): DataFrame = {
    fit(trainDf)
    transform(trainDf)
  }

  def evaluate(evalDf: DataFrame): JParams

  def metric(predDf: DataFrame): JParams = {
    val params = new JParams()
    val handelPersistent = predDf.storageLevel == StorageLevel.NONE
    if (handelPersistent) predDf.persist(StorageLevel.MEMORY_AND_DISK)
    for (elem <- metrics if elem != null) {
      val p_ = elem.evaluateJson(predDf)
      params.putAll(p_)
    }
    if (handelPersistent) predDf.unpersist()
    params
  }

  def load(path: String): Unit

  def save(path: String): Unit
}