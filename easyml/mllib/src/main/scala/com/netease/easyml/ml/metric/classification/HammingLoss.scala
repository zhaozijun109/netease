package com.netease.easyml.ml.metric.classification

import com.netease.easyml.annotation.Register
import com.netease.easyml.ml.metric.{Metric, toDoubleArrayUdf}
import com.netease.easyml.ml.util.SchemaUtils
import org.apache.spark.ml.param.shared.{HasLabelCol, HasPredictionCol, HasWeightCol}
import org.apache.spark.ml.util.{DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.types.DoubleType
import org.apache.spark.sql.{Dataset, Row}

/**
 * Created by linjiuning on 2020/7/16.
 * Compute the average Hamming loss.
 */
@Register(alias = Array("hamming_loss", "hamming-loss", "hammingloss"))
class HammingLoss(override val uid: String) extends Metric
  with HasPredictionCol with HasLabelCol with HasWeightCol
  with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("hamming_loss"))

  override lazy val shortName: String = "hamming_loss"

  def setPredictionCol(value: String): this.type = set(predictionCol, value)

  def setLabelCol(value: String): this.type = set(labelCol, value)

  def setWeightCol(value: String): this.type = set(weightCol, value)

  def checkSchema(dataset: Dataset[_]): Unit = {
    val schema = dataset.schema
    SchemaUtils.checkNumericOrArrayOrVectorType(schema, $(labelCol))
    SchemaUtils.checkNumericOrArrayOrVectorType(schema, $(predictionCol))
    if (isDefined(weightCol))
      SchemaUtils.checkNumericType(schema, $(weightCol))
  }

  private def hammingLoss(prediction: Seq[Double], label: Seq[Double], weight: Double = 1.0): Double = {
    val loss = if (label.length == 1 && prediction.length == 1) {
      // binary/multiclass
      if (label.last == prediction.last) 0.0 else 1.0
    } else {
      // multilabel
      prediction.zip(label).count(it => it._1 != it._2).toDouble / prediction.length
    }
    loss * weight
  }

  override def evaluate(dataset: Dataset[_]): Double = {
    checkSchema(dataset)

    var dataset_ = dataset.withColumn($(predictionCol), toDoubleArrayUdf(col($(predictionCol))))
      .withColumn($(labelCol), toDoubleArrayUdf(col($(labelCol))))

    if (isDefined(weightCol)) {
      dataset_ = dataset_.withColumn($(weightCol), col($(weightCol)).cast(DoubleType))
      val numDocs = dataset_.select($(weightCol)).rdd.map(row => row.getDouble(0)).sum()

      dataset_ = dataset_.withColumn($(weightCol), col($(weightCol)).cast(DoubleType))
      dataset_.select($(predictionCol), $(labelCol), $(weightCol)).rdd.map {
        case Row(predictions: Seq[Double], labels: Seq[Double], weight: Double) =>
          hammingLoss(predictions, labels, weight)
      }.sum() / numDocs
    } else {
      val numDocs = dataset_.count().toDouble
      dataset_.select($(predictionCol), $(labelCol)).rdd.map {
        case Row(predictions: Seq[Double], labels: Seq[Double]) =>
          hammingLoss(predictions, labels)
      }.sum() / numDocs
    }
  }

  override def isLargerBetter: Boolean = false
}

