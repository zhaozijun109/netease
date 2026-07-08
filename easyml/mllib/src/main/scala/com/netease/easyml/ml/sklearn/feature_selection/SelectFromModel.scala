package com.netease.easyml.ml.sklearn.feature_selection

import com.microsoft.azure.synapse.ml.lightgbm.{LightGBMClassificationModel, LightGBMRankerModel, LightGBMRegressionModel}
import ml.dmlc.xgboost4j.scala.Booster
import ml.dmlc.xgboost4j.scala.spark.{XGBoostClassificationModel, XGBoostRegressionModel}
import org.apache.spark.SparkException
import org.apache.spark.ml.classification.LogisticRegressionModel
import org.apache.spark.ml.param._
import org.apache.spark.ml.param.shared._
import org.apache.spark.ml.util.{DefaultParamsReadable, Identifiable}
import org.apache.spark.ml.{Estimator, Transformer}
import org.apache.spark.sql.Dataset

import scala.collection.mutable

/**
 * Created by linjiuning on 2020/11/3.
 */

trait SelectFromModelParams extends Params with HasLabelCol with HasMaxIter {

  val estimator: Param[Estimator[_]] = new Param(this, "estimator", "estimator for selection")

  def getEstimator: Estimator[_] = $(estimator)

  final val importanceType: Param[String] = new Param[String](this, "importanceType", "Get importance of each feature based on information gain or cover", ParamValidators.inArray(Array("gain", "cover", "total_gain", "total_cover", "split")))

  final def getImportanceType: String = $(importanceType)

  setDefault(importanceType -> "gain", maxIter -> 1)
}

class SelectFromModel(override val uid: String) extends FeatureSelector(uid) with SelectFromModelParams {

  import SelectFromModel._

  def this() = this(Identifiable.randomUID("selFromModel"))

  def setLabelCol(value: String): this.type = set(labelCol, value)

  def setMaxIter(value: Int): this.type = set(maxIter, value)

  def setEstimator(value: Estimator[_]): this.type = set(estimator, value)

  override def computeImportance(dataset: Dataset[_]): Array[Double] = {
    val estimator = getEstimator
    estimator.set(estimator.getParam("featuresCol"), $(featuresCol))
      .set(estimator.getParam("labelCol"), $(labelCol))

    var featureImportance: Array[Double] = null
    val iter = getMaxIter
    val importanceType = getImportanceType
    for (_ <- 0 until iter) {
      val model = estimator.fit(dataset).asInstanceOf[Transformer]
      val scores = modelFeatureImportance(model, importanceType)
      if (featureImportance == null) {
        featureImportance = scores
      } else {
        featureImportance = featureImportance.zip(scores).map(it => it._1 + it._2)
      }
    }
    featureImportance.map(_ / iter)
  }
}


object SelectFromModel extends DefaultParamsReadable[SelectFromModel] {

  def modelFeatureImportance(model: Transformer, importanceType: String = "gain"): Array[Double] = {
    model match {
      case lr: LogisticRegressionModel =>
        lrFeatureImportance(lr)
      case xgb: XGBoostClassificationModel =>
        xgboostFeatureImportance(xgb.nativeBooster, importanceType)
      case xgb: XGBoostRegressionModel =>
        xgboostFeatureImportance(xgb.nativeBooster, importanceType)
      case lgb: LightGBMClassificationModel =>
        lgb.getFeatureImportances(importanceType)
      case lgb: LightGBMRegressionModel =>
        lgb.getFeatureImportances(importanceType)
      case lgb: LightGBMRankerModel =>
        lgb.getFeatureImportances(importanceType)
      case _ =>
        throw new SparkException(s"Model ${model.getClass} is not supported.")
    }
  }

  def xgboostFeatureImportance(booster: Booster, importanceType: String): Array[Double] = {
    val scores = try {
      // xgboost 0.82
      val method = booster.getClass.getDeclaredMethod("getScore", classOf[String], classOf[String])
      method.invoke(booster, "", importanceType).asInstanceOf[Map[String, Double]]
    } catch {
      // xgboost 0.81
      case _: NoSuchMethodException =>
        val method = booster.getClass.getDeclaredMethod("getFeatureScore", classOf[String])
        method.invoke(booster, "").asInstanceOf[mutable.Map[String, Integer]]
          .mapValues(_.toDouble)
    }
    scores.map {
      case (name, score) =>
        val id = name.slice(1, name.length).toInt
        (id, score)
    }.toArray.sortBy(_._1).map(_._2.toDouble)
  }

  def lrFeatureImportance(lr: LogisticRegressionModel): Array[Double] = {
    val coef = lr.coefficientMatrix

    val normCoef = new Array[Double](coef.numCols)
    for (row <- coef.rowIter) {
      val array = row.toArray
      for (i <- normCoef.indices) {
        normCoef(i) += Math.abs(array(i))
      }
    }
    normCoef
  }

  override def load(path: String): SelectFromModel = super.load(path)
}