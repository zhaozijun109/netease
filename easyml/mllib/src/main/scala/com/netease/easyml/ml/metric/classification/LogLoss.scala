package com.netease.easyml.ml.metric.classification

import com.netease.easyml.annotation.Register
import com.netease.easyml.ml.metric.{Metric, toDoubleArrayUdf}
import com.netease.easyml.ml.param.{HasEpsilon, HasNormalize}
import com.netease.easyml.ml.util.SchemaUtils
import org.apache.spark.ml.param.shared.{HasLabelCol, HasRawPredictionCol, HasWeightCol}
import org.apache.spark.ml.util.{DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.types.DoubleType
import org.apache.spark.sql.{Dataset, Row}

/**
 * Created by linjiuning on 2020/7/16.
 * Log loss, aka logistic loss or cross-entropy loss.
 */
@Register(alias = Array("log_loss", "log-loss", "logloss"))
class LogLoss(override val uid: String) extends Metric
  with HasRawPredictionCol with HasLabelCol with HasWeightCol with HasEpsilon with HasNormalize
  with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("log_loss"))

  override lazy val shortName: String = "log_loss"

  def setRawPredictionCol(value: String): this.type = set(rawPredictionCol, value)

  def setLabelCol(value: String): this.type = set(labelCol, value)

  def setWeightCol(value: String): this.type = set(weightCol, value)

  def setNormalize(value: Boolean): this.type = set(normalize, value)

  def setEpsilon(value: Double): this.type = set(epsilon, value)

  def checkSchema(dataset: Dataset[_]): Unit = {
    val schema = dataset.schema
    SchemaUtils.checkNumericType(schema, $(labelCol))
    SchemaUtils.checkNumericOrArrayOrVectorType(schema, $(rawPredictionCol))
    if (isDefined(weightCol))
      SchemaUtils.checkNumericType(schema, $(weightCol))
  }

  private def clip(value: Double): Double = {
    Math.min(1 - $(epsilon), Math.max($(epsilon), value))
  }

  private def logLoss(probability: Seq[Double], label: Double, weight: Double = 1.0): Double = {
    val clipped = probability.map(clip)
    val sum = clipped.sum
    val reNormalize = clipped.map(_ / sum).toArray
    if (reNormalize.length <= 2) {
      -(label * Math.log(reNormalize.last) + (1 - label) * Math.log(1 - reNormalize.last)) * weight
    }
    else {
      -Math.log(reNormalize(label.toInt)) * weight
    }
  }

  override def evaluate(dataset: Dataset[_]): Double = {
    checkSchema(dataset)

    var dataset_ = dataset.withColumn($(rawPredictionCol), toDoubleArrayUdf(col($(rawPredictionCol))))
      .withColumn($(labelCol), col($(labelCol)).cast(DoubleType))

    if (isDefined(weightCol)) {
      dataset_ = dataset_.withColumn($(weightCol), col($(weightCol)).cast(DoubleType))
      val numDocs = if ($(normalize)) {
        dataset_.select($(weightCol)).rdd.map(row => row.getDouble(0)).sum()
      } else {
        1.0
      }
      dataset_ = dataset_.withColumn($(weightCol), col($(weightCol)).cast(DoubleType))
      dataset_.select($(rawPredictionCol), $(labelCol), $(weightCol)).rdd.map {
        case Row(predictions: Seq[Double], label: Double, weight: Double) =>
          logLoss(predictions, label, weight)
      }.sum() / numDocs
    } else {
      val numDocs = if ($(normalize)) {
        dataset_.count().toDouble
      } else {
        1.0
      }
      dataset_.select($(rawPredictionCol), $(labelCol)).rdd.map {
        case Row(predictions: Seq[Double], label: Double) =>
          logLoss(predictions, label)
      }.sum() / numDocs
    }
  }

  override def isLargerBetter: Boolean = false
}

