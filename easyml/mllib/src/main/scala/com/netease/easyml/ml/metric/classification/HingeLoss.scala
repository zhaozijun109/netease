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
 * Average hinge loss (non-regularized)
 */
@Register(alias = Array("hinge_loss", "hinge-loss", "hingeloss"))
class HingeLoss(override val uid: String) extends Metric
  with HasRawPredictionCol with HasLabelCol with HasWeightCol
  with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("hinge_loss"))

  override lazy val shortName: String = "hinge_loss"

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

  private def hingeLoss(probability: Seq[Double], label: Double, weight: Double = 1.0): Double = {
    val margin = if (probability.size == 1) {
      val y = if (label > 0) 1 else -1
      y * probability.last
    } else {
      val array = probability.toArray
      val idx = label.toInt
      val neg = array.zipWithIndex.filter(_._2 != idx).map(_._1).max
      array(idx) - neg
    }
    Math.max(0, 1 - margin) * weight
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
        case Row(predictions: Seq[Double], label: Double, weight: Double) =>
          hingeLoss(predictions, label, weight)
      }.sum() / numDocs
    } else {
      val numDocs = dataset_.count().toDouble
      dataset_.select($(rawPredictionCol), $(labelCol)).rdd.map {
        case Row(predictions: Seq[Double], label: Double) =>
          hingeLoss(predictions, label)
      }.sum() / numDocs
    }
  }

  override def isLargerBetter: Boolean = false
}

