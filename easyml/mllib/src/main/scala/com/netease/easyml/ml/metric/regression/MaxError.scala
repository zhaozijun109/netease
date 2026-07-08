package com.netease.easyml.ml.metric.regression

import com.netease.easyml.annotation.Register
import com.netease.easyml.ml.metric.Metric
import com.netease.easyml.ml.util.SchemaUtils
import org.apache.spark.ml.param.shared.{HasLabelCol, HasPredictionCol}
import org.apache.spark.ml.util.{DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.types.DoubleType
import org.apache.spark.sql.{Dataset, Row}

/**
 * Created by linjiuning on 2020/7/16.
 * max_error metric calculates the maximum residual error.
 */
@Register(alias = Array("max_error"))
class MaxError(override val uid: String) extends Metric
  with HasPredictionCol with HasLabelCol
  with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("max_error"))

  override lazy val shortName: String = "max_error"

  def setPredictionCol(value: String): this.type = set(predictionCol, value)

  def setLabelCol(value: String): this.type = set(labelCol, value)

  def checkSchema(dataset: Dataset[_]): Unit = {
    val schema = dataset.schema
    SchemaUtils.checkNumericType(schema, $(predictionCol))
    SchemaUtils.checkNumericType(schema, $(labelCol))
  }

  override def evaluate(dataset: Dataset[_]): Double = {
    checkSchema(dataset)

    var dataset_ = dataset.withColumn($(predictionCol), col($(predictionCol)).cast(DoubleType))
    dataset_ = dataset_.withColumn($(labelCol), col($(labelCol)).cast(DoubleType))

    dataset_.select($(predictionCol), $(labelCol)).rdd.map {
      case Row(prediction: Double, label: Double) =>
        Math.abs(prediction - label)
    }.max()
  }

  override def isLargerBetter: Boolean = false
}

