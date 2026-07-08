package com.netease.easyml.ml.metric.classification

import com.netease.easyml.annotation.Register
import com.netease.easyml.ml.metric.{Metric, toDoubleArrayUdf}
import com.netease.easyml.ml.util.SchemaUtils
import org.apache.spark.ml.param.shared.{HasLabelCol, HasRawPredictionCol, HasWeightCol}
import org.apache.spark.ml.util.{DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.types.DoubleType
import org.apache.spark.sql.{Dataset, Row}

/**
 * Created by linjiuning on 2020/7/16.
 * Compute the Brier score.
 */
@Register(alias = Array("brier_score_loss", "brier-score-loss", "brierscoreloss"))
class BrierScoreLoss(override val uid: String) extends Metric
  with HasRawPredictionCol with HasLabelCol with HasWeightCol
  with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("brier_score_loss"))

  override lazy val shortName: String = "brier_score_loss"

  // Probabilities of the positive class.
  def setRawPredictionCol(value: String): this.type = set(rawPredictionCol, value)

  def setLabelCol(value: String): this.type = set(labelCol, value)

  def setWeightCol(value: String): this.type = set(weightCol, value)

  def checkSchema(dataset: Dataset[_]): Unit = {
    val schema = dataset.schema
    SchemaUtils.checkNumericType(schema, $(labelCol))
    SchemaUtils.checkNumericOrArrayOrVectorType(schema, $(rawPredictionCol))
    if (isDefined(weightCol))
      SchemaUtils.checkNumericType(schema, $(weightCol))
  }

  private def brierLoss(probability: Seq[Double], label: Double, weight: Double = 1.0): Double = {
    val truePos = probability.last
    val loss = if (label > 0)
      1 - truePos
    else
      -truePos
    Math.pow(loss, 2) * weight
  }

  override def evaluate(dataset: Dataset[_]): Double = {
    checkSchema(dataset)

    var dataset_ = dataset.withColumn($(rawPredictionCol), toDoubleArrayUdf(col($(rawPredictionCol))))
      .withColumn($(labelCol), col($(labelCol)).cast(DoubleType))

    if (isDefined(weightCol)) {
      dataset_ = dataset_.withColumn($(weightCol), col($(weightCol)).cast(DoubleType))
      val numDocs = dataset_.select($(weightCol)).rdd.map(row => row.getDouble(0)).sum()

      dataset_ = dataset_.withColumn($(weightCol), col($(weightCol)).cast(DoubleType))
      dataset_.select($(rawPredictionCol), $(labelCol), $(weightCol)).rdd.map {
        case Row(probabilities: Seq[Double], label: Double, weight: Double) =>
          brierLoss(probabilities, label, weight)
      }.sum() / numDocs
    } else {
      val numDocs = dataset_.count().toDouble
      dataset_.select($(rawPredictionCol), $(labelCol)).rdd.map {
        case Row(probabilities: Seq[Double], label: Double) =>
          brierLoss(probabilities, label)
      }.sum() / numDocs
    }
  }

  override def isLargerBetter: Boolean = false
}

