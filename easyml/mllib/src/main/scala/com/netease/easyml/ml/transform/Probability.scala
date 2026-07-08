package com.netease.easyml.ml.transform

import com.netease.easyml.common.util.ConvertUtil
import com.netease.easyml.ml.util.SchemaUtils
import org.apache.spark.ml.Transformer
import org.apache.spark.ml.linalg.Vector
import org.apache.spark.ml.param.ParamMap
import org.apache.spark.ml.param.shared.{HasOutputCol, HasPredictionCol, HasProbabilityCol}
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.types.{DoubleType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, Dataset, Row}

/**
 * Created by linjiuning on 2020/11/4.
 */
class Probability(override val uid: String) extends Transformer
  with HasPredictionCol with HasProbabilityCol with HasOutputCol with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("probability"))

  def setPredictionCol(value: String): this.type = set(predictionCol, value)

  def setProbabilityCol(value: String): this.type = set(probabilityCol, value)

  def setOutputCol(value: String): this.type = set(outputCol, value)

  override def transform(dataset: Dataset[_]): DataFrame = {
    val structType = transformSchema(dataset.schema, logging = true)
    val predictionCol = getPredictionCol
    val probabilityCol = getProbabilityCol
    val rdd = dataset.toDF.rdd.map(row => {
      val prediction = row.getAs[Any](predictionCol)
      val probability = row.getAs[Any](probabilityCol)
      val value = if (prediction != null && probability != null) {
        val i = ConvertUtil.toInt(prediction)
        probability match {
          case vector: Vector =>
            vector(i)
          case array: Seq[Double] =>
            array(i)
        }
      } else {
        null.asInstanceOf[Double]
      }
      Row.fromSeq(row.toSeq :+ value)
    })
    dataset.sparkSession.createDataFrame(rdd, structType)
  }

  override def copy(extra: ParamMap): Probability = defaultCopy(extra)

  override def transformSchema(schema: StructType): StructType = {
    SchemaUtils.checkNumericType(schema, $(predictionCol))
    SchemaUtils.checkArrayOrVectorType(schema, $(probabilityCol))
    SchemaUtils.appendColumn(schema, StructField($(outputCol), DoubleType, true))
  }
}

object Probability extends DefaultParamsReadable[Probability] {
  override def load(path: String): Probability = super.load(path)
}

