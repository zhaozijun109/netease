package com.netease.easyml.ml.metric.classification

import com.netease.easyml.annotation.Register
import com.netease.easyml.common.collection.{Params => JParams}
import com.netease.easyml.ml.metric.{Metric, toDoubleArrayUdf}
import com.netease.easyml.ml.util.SchemaUtils
import org.apache.spark.ml.param.shared.{HasLabelCol, HasPredictionCol, HasWeightCol}
import org.apache.spark.ml.util.{DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.functions.col

/**
 * Created by linjiuning on 2020/7/16.
 * Compute a confusion matrix for each class.
 */
@Register(alias = Array("multilabel_confusion_matrix"))
class MultiLabelConfusionMatrix(override val uid: String) extends Metric
  with HasPredictionCol with HasLabelCol with HasWeightCol
  with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("multilabel_confusion_matrix"))

  override lazy val shortName: String = "multilabel_confusion_matrix"

  def setPredictionCol(value: String): this.type = set(predictionCol, value)

  def setLabelCol(value: String): this.type = set(labelCol, value)

  def setWeightCol(value: String): this.type = set(weightCol, value)

  def checkSchema(dataset: Dataset[_]): Unit = {
    val schema = dataset.schema
    SchemaUtils.checkNumericOrArrayOrVectorType(schema, $(predictionCol))
    SchemaUtils.checkNumericOrArrayOrVectorType(schema, $(labelCol))
    if (isDefined(weightCol))
      SchemaUtils.checkNumericType(schema, $(weightCol))
  }

  def confusionMatrix(dataset: Dataset[_]): Array[Array[Array[Double]]] = {
    checkSchema(dataset)

    var dataset_ = dataset.withColumn($(predictionCol), toDoubleArrayUdf(col($(predictionCol))))
      .withColumn($(labelCol), toDoubleArrayUdf(col($(labelCol))))

    dataset_ = if (isDefined(weightCol)) {
      dataset_.select($(predictionCol), $(labelCol), $(weightCol))
    } else {
      dataset_.select($(predictionCol), $(labelCol))
    }

    val labels = dataset_.select($(predictionCol), $(labelCol))
      .rdd.flatMap(row => {
      row.getSeq[Double](0) ++ row.getSeq[Double](1)
    }).distinct().collect().sorted

    val confusions = dataset_.rdd.flatMap(row => {
      val yPred = row.getSeq[Double](0)
      val yTrue = row.getSeq[Double](1)
      val w = if (row.size > 2) row.getDouble(2) else 1.0
      if (yPred.length == 1) {
        // multiclass
        labels.map(it => {
          val y1 = if (it == yTrue.last) 1.0 else 0.0
          val y2 = if (it == yPred.last) 1.0 else 0.0
          ((it, y1, y2), 1.0)
        })
      } else {
        // multilabel
        yTrue.zipWithIndex.map {
          case (elem, i) =>
            ((i.toDouble, elem, yPred(i)), w)
        }
      }
    }).reduceByKey(_ + _)
      .collectAsMap()

    val n = confusions.map(_._1._1).max.toInt + 1

    val values = Array.fill(n)(Array.fill(2)(Array(0.0, 0.0)))
    confusions.foreach {
      case ((i, yTrue, yPred), w) =>
        values(i.toInt)(yTrue.toInt)(yPred.toInt) = w
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

