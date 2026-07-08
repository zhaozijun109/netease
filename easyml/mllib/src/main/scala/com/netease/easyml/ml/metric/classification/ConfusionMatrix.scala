package com.netease.easyml.ml.metric.classification

import com.netease.easyml.annotation.Register
import com.netease.easyml.common.collection.{Params => JParams}
import com.netease.easyml.ml.metric.Metric
import com.netease.easyml.ml.util.SchemaUtils
import org.apache.spark.ml.param.shared.{HasLabelCol, HasPredictionCol, HasWeightCol}
import org.apache.spark.ml.param.{Param, ParamValidators, Params}
import org.apache.spark.ml.util.{DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.types.DoubleType

/**
 * Created by linjiuning on 2020/7/16.
 * Compute confusion matrix to evaluate the accuracy of a classification.
 */

trait HasNormalizeType extends Params {

  /**
   * Normalizes confusion matrix over the true (rows), predicted (columns)
   * conditions or all the population. If None, confusion matrix will not be
   * normalized.
   *
   * @group param
   */
  final val normalize = new Param[String](
    this, "normalize", "the way to normalize confusion matrix",
    ParamValidators.inArray(Array("true", "pred", "all", "none")))
  setDefault(normalize, "none")

  /** @group getParam */
  def getNormalize: String = $(normalize)
}

@Register(alias = Array("confusion_matrix"))
class ConfusionMatrix(override val uid: String) extends Metric
  with HasPredictionCol with HasLabelCol with HasWeightCol with HasNormalizeType
  with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("confusion_matrix"))

  override lazy val shortName: String = "confusion_matrix"

  def setPredictionCol(value: String): this.type = set(predictionCol, value)

  def setLabelCol(value: String): this.type = set(labelCol, value)

  def setWeightCol(value: String): this.type = set(weightCol, value)

  def setNormalize(value: String): this.type = set(normalize, value)

  def checkSchema(dataset: Dataset[_]): Unit = {
    val schema = dataset.schema
    SchemaUtils.checkNumericType(schema, $(predictionCol))
    SchemaUtils.checkNumericType(schema, $(labelCol))
    if (isDefined(weightCol))
      SchemaUtils.checkNumericType(schema, $(weightCol))
  }

  def confusionMatrix(dataset: Dataset[_]): Array[Array[Double]] = {
    checkSchema(dataset)

    var dataset_ = dataset.withColumn($(predictionCol), col($(predictionCol)).cast(DoubleType))
      .withColumn($(labelCol), col($(labelCol)).cast(DoubleType))

    dataset_ = if (isDefined(weightCol)) {
      dataset_.select($(predictionCol), $(labelCol), $(weightCol))
    } else {
      dataset_.select($(predictionCol), $(labelCol))
    }

    val confusions = dataset_.rdd.map(row => {
      val yPred = row.getDouble(0)
      val yTrue = row.getDouble(1)
      val w = if (row.size > 2) row.getDouble(2) else 1.0
      ((yTrue, yPred), w)
    }).reduceByKey(_ + _)
      .collectAsMap()

    val labels = confusions.keys.flatMap(it => Array(it._1, it._2))
      .toSet

    val n = labels.size

    val values = Array.fill(n)(Array.fill(n)(0.0))
    confusions.foreach {
      case ((yTrue, yPred), w) =>
        values(yTrue.toInt)(yPred.toInt) = w
    }
    $(normalize) match {
      case "true" =>
        val sum = values.map(it => it.sum)
        var i = 0
        while (i < n) {
          if (sum(i) > 0) {
            var j = 0
            while (j < n) {
              values(i)(j) /= sum(i)
              j += 1
            }
          }
          i += 1
        }
      case "pred" =>
        var j = 0
        while (j < n) {
          var i = 0
          var sum = 0.0
          while (i < n) {
            sum += values(i)(j)
            i += 1
          }
          if (sum > 0) {
            i = 0
            while (i < n) {
              values(i)(j) /= sum
              i += 1
            }
          }
          j += 1
        }
      case "all" =>
        val sum = values.flatten.sum
        if (sum > 0) {
          var i = 0
          while (i < n) {
            var j = 0
            while (j < n) {
              values(i)(j) /= sum
              j += 1
            }
            i += 1
          }
        }
      case _ =>
    }
    values
  }

  override def evaluateJson(dataset: Dataset[_]): JParams = {
    val metric = confusionMatrix(dataset)
    val jParams = new JParams()
    jParams.put(shortName, metric)
    jParams
  }

  override def isLargerBetter: Boolean = {
    throw new UnsupportedOperationException("Confusion matrix is not comparable.")
  }

  override def evaluate(dataset: Dataset[_]): Double = {
    throw new UnsupportedOperationException("Confusion matrix is not a double value.")
  }
}

