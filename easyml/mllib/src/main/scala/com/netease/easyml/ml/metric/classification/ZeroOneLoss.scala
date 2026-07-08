package com.netease.easyml.ml.metric.classification

import com.netease.easyml.annotation.Register
import com.netease.easyml.ml.metric.Metric
import com.netease.easyml.ml.param.HasNormalize
import com.netease.easyml.ml.util.SchemaUtils
import org.apache.spark.ml.param.shared.{HasLabelCol, HasPredictionCol, HasWeightCol}
import org.apache.spark.ml.util.{DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.types.DoubleType

/**
 * Created by linjiuning on 2020/7/16.
 * Zero-one classification loss.
 */
@Register(alias = Array("zero_one_loss", "zero-one-loss", "zerooneloss"))
class ZeroOneLoss(override val uid: String) extends Metric
  with HasPredictionCol with HasLabelCol with HasWeightCol with HasNormalize
  with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("zero_one_loss"))

  override lazy val shortName: String = "zero_one_loss"

  def setPredictionCol(value: String): this.type = set(predictionCol, value)

  def setLabelCol(value: String): this.type = set(labelCol, value)

  def setWeightCol(value: String): this.type = set(weightCol, value)

  def setNormalize(value: Boolean): this.type = set(normalize, value)

  def checkSchema(dataset: Dataset[_]): Unit = {
    val schema = dataset.schema
    SchemaUtils.checkNumericOrArrayOrVectorType(schema, $(labelCol))
    SchemaUtils.checkNumericOrArrayOrVectorType(schema, $(predictionCol))
    if (isDefined(weightCol))
      SchemaUtils.checkNumericType(schema, $(weightCol))
  }

  override def evaluate(dataset: Dataset[_]): Double = {
    val accuracyScore = new AccuracyScore()
      .setPredictionCol($(predictionCol))
      .setLabelCol($(labelCol))
      .setNormalize($(normalize))
    if (isDefined(weightCol))
      accuracyScore.setWeightCol($(weightCol))

    val score = accuracyScore.evaluate(dataset)
    if ($(normalize))
      1 - score
    else {
      val numDocs = if (isDefined(weightCol)) {
        val dataset_ = dataset.withColumn($(weightCol), col($(weightCol)).cast(DoubleType))
        dataset_.select($(weightCol)).rdd.map(row => row.getDouble(0)).sum()
      } else
        dataset.count().toDouble

      numDocs - score
    }
  }

  override def isLargerBetter: Boolean = false
}

