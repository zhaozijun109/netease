package com.netease.easyml.ml.metric.classification

import com.netease.easyml.annotation.Register
import com.netease.easyml.ml.metric.{Metric, toDoubleArrayUdf}
import com.netease.easyml.ml.param.HasNormalize
import com.netease.easyml.ml.util.SchemaUtils
import org.apache.spark.ml.param.shared.{HasLabelCol, HasPredictionCol, HasWeightCol}
import org.apache.spark.ml.util.{DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.types.DoubleType
import org.apache.spark.sql.{Dataset, Row}

/**
 * Created by linjiuning on 2020/7/6.
 * Support binary, multiclass, multilabel
 */
@Register(alias = Array("accuracy", "acc"))
class AccuracyScore(override val uid: String) extends Metric
  with HasPredictionCol with HasLabelCol with HasWeightCol with HasNormalize
  with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("accuracy"))

  override lazy val shortName: String = "accuracy"

  def setPredictionCol(value: String): this.type = set(predictionCol, value)

  def setLabelCol(value: String): this.type = set(labelCol, value)

  def setWeightCol(value: String): this.type = set(weightCol, value)

  def setNormalize(value: Boolean): this.type = set(normalize, value)

  def checkSchema(dataset: Dataset[_]): Unit = {
    val schema = dataset.schema
    SchemaUtils.checkNumericOrArrayOrVectorType(schema, $(predictionCol))
    SchemaUtils.checkNumericOrArrayOrVectorType(schema, $(labelCol))
    if (isDefined(weightCol))
      SchemaUtils.checkNumericType(schema, $(weightCol))
  }

  override def evaluate(dataset: Dataset[_]): Double = {
    checkSchema(dataset)

    var dataset_ = dataset.withColumn($(predictionCol), toDoubleArrayUdf(col($(predictionCol))))
    dataset_ = dataset_.withColumn($(labelCol), toDoubleArrayUdf(col($(labelCol))))

    if (isDefined(weightCol)) {
      dataset_ = dataset_.withColumn($(weightCol), col($(weightCol)).cast(DoubleType))
      val numDocs = if ($(normalize)) {
        dataset_.select($(weightCol)).rdd.map(row => row.getDouble(0)).sum()
      } else {
        1.0
      }
      dataset_.select($(predictionCol), $(labelCol), $(weightCol)).rdd.map {
        case Row(predictions: Seq[Double], labels: Seq[Double], weight: Double) =>
          if (predictions.zip(labels).exists(it => it._1 != it._2)) 0.0 else weight
        case Row(prediction: Double, label: Double, weight: Double) =>
          if (prediction == label) weight else 0.0
      }.sum() / numDocs
    } else {
      val numDocs = if ($(normalize)) {
        dataset_.count().toDouble
      } else {
        1.0
      }
      dataset_.select($(predictionCol), $(labelCol)).rdd.filter {
        case Row(predictions: Seq[Double], labels: Seq[Double]) =>
          !predictions.zip(labels).exists(it => it._1 != it._2)
        case Row(prediction: Double, label: Double) =>
          prediction == label
      }.count().toDouble / numDocs
    }
  }

  override def isLargerBetter: Boolean = true
}
