package com.netease.easyml.ml.transform

import java.util.function.Supplier
import com.netease.easyml.common.resource.ResourceManager
import com.netease.easyml.local.mllib.FastText
import com.netease.easyml.ml.param.HasPath
import org.apache.spark.ml.Transformer
import org.apache.spark.ml.param.shared.{HasFeaturesCol, HasPredictionCol, HasProbabilityCol}
import org.apache.spark.ml.param.{Param, ParamMap}
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.types.{DoubleType, FloatType, StringType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, Dataset, Row}

/**
 * Created by linjiuning on 2020/8/3.
 */

class FastTextPredictor(override val uid: String) extends Transformer
  with HasFeaturesCol with HasPredictionCol with HasProbabilityCol
  with HasPath with DefaultParamsWritable {

  import FastTextPredictor._

  def this() = this(Identifiable.randomUID("fasttext"))

  def setFeaturesCol(value: String): this.type = set(featuresCol, value)

  def setPredictionCol(value: String): this.type = set(predictionCol, value)

  def setProbabilityCol(value: String): this.type = set(probabilityCol, value)

  val index = new Param[Boolean](
    this, "index", "whether index the origin token")

  def getIndex: Boolean = $(index)

  def setIndex(value: Boolean): this.type = set(index, value)

  setDefault(index, false)

  def setPath(value: String): this.type = set(path, value)

  override def transform(dataset: Dataset[_]): DataFrame = {
    val path = getPath
    val index = getIndex
    val featuresCol = getFeaturesCol
    val rdd = dataset.toDF().rdd.mapPartitions(iter => {
      val model = getOrCreate(path, index)
      iter.map(row => {
        val text = Option(row.getAs[String](featuresCol)).getOrElse("")

        val probLabel = if (text.nonEmpty) {
          val pred = model.predictProba(text)
          Option(pred)
        } else {
          None
        }
        (row, probLabel)
      }).filter(_._2.isDefined)
        .map(it => {
          val row = it._1
          val label = it._2.get.label
          val prob = Math.exp(it._2.get.logProb).toFloat
          Row.fromSeq(row.toSeq ++ Array(label, prob))
        })
    })
    dataset.sparkSession.createDataFrame(rdd, transformSchema(dataset.schema))
  }

  override def copy(extra: ParamMap): Transformer = defaultCopy(extra)

  override def transformSchema(schema: StructType): StructType = {
    val predField = StructField($(predictionCol), StringType)
    val probField = StructField($(probabilityCol), FloatType)
    StructType(schema.fields ++ Array(predField, probField))
  }
}

object FastTextPredictor extends DefaultParamsReadable[FastTextPredictor] {
  def getOrCreate(path: String, index: Boolean): FastText = synchronized {
    ResourceManager.getOrCreate(path, () => {
      FastText.loadModel(path, index)
    })
  }

  override def load(path: String): FastTextPredictor = super.load(path)
}
