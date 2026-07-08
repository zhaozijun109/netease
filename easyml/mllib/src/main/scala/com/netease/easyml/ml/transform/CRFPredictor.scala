package com.netease.easyml.ml.transform

import com.alibaba.fastjson.JSON
import com.github.zhifac.crf4j.TaggerImpl.Mode
import com.github.zhifac.crf4j.{Example, TaggerImpl}
import com.netease.easyml.common.util.{IOUtil, SequenceLabeling}
import com.netease.easyml.ml.param.{HasPath, HasSeparator}
import org.apache.spark.ml.Transformer
import org.apache.spark.ml.param.shared.{HasFeaturesCol, HasLabelCol, HasRawPredictionCol}
import org.apache.spark.ml.param.{Param, ParamMap, Params}
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.types.{ArrayType, StringType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, Dataset, Row}

import scala.collection.JavaConversions._

/**
 * Created by linjiuning on 2020/7/2.
 */
trait CRFParams extends Params with HasPath with HasFeaturesCol with HasRawPredictionCol with HasLabelCol with HasSeparator {
  // default char level
  setDefault(separator, "")

  val labelMapPath: Param[String] = new Param[String](this, "labelMapPath", "label map path")

  def getLabelMapPath: String = $(labelMapPath)

  setDefault(labelMapPath, "")
}

class CRFPredictor(override val uid: String) extends Transformer with CRFParams with DefaultParamsWritable {
  def this() = this(Identifiable.randomUID("crf"))

  def setFeaturesCol(value: String): this.type = set(featuresCol, value)

  def setRawPredictionCol(value: String): this.type = set(rawPredictionCol, value)

  def setLabelCol(value: String): this.type = set(labelCol, value)

  def setSeparator(value: String): this.type = set(separator, value)

  def setPath(value: String): this.type = set(path, value)

  def setLabelMapPath(value: String): this.type = set(labelMapPath, value)

  def newTagger(): TaggerImpl = {
    val tagger = new TaggerImpl(Mode.TEST)
    val stream = IOUtil.getInputStream($(path))
    tagger.open(stream, 0, 0, 1.0D)
    tagger
  }

  private lazy val labelMap = {
    if ($(labelMapPath).nonEmpty) {
      JSON.parseObject(IOUtil.readLines($(labelMapPath)).mkString(""))
        .entrySet().map(it => (it.getKey, it.getValue.toString))
        .toMap
    } else {
      Map.empty[String, String]
    }
  }

  override def transform(dataset: Dataset[_]): DataFrame = {
    val rdd = dataset.toDF().rdd.mapPartitions(iter => {
      val tagger = newTagger()
      iter.map(row => {
        val ex: Example = new Example
        val features = Option(row.getAs[String]($(featuresCol))).getOrElse("")
        features.split($(separator)).foreach(it => tagger.add(ex, it))
        tagger.parse(ex)
        val tokenWithTag = (0 until tagger.size(ex))
          .map(it => (tagger.x(ex, it, 0), tagger.y2(ex, it)))
          .map(it => (it._1, labelMap.getOrElse(it._2, it._2)))
          .toList

        val tokens = tokenWithTag.map(_._1)
        val rawPred = tokenWithTag.map(_._2)
        val pairs = SequenceLabeling.getEntities(rawPred, false)
          .map(pos => {
            val tag = pos.getValue0
            val b = pos.getValue1.getValue0
            val e = pos.getValue1.getValue1
            val text = tokens.slice(b, e + 1).mkString("")
            Array(text, tag)
          })

        Row.fromSeq(row.toSeq ++ Array(rawPred, pairs))
      })
    })
    dataset.sparkSession.createDataFrame(rdd, transformSchema(dataset.schema))
  }

  override def copy(extra: ParamMap): CRFPredictor = defaultCopy(extra)

  override def transformSchema(schema: StructType): StructType = {
    val rawPredCol = StructField($(rawPredictionCol), ArrayType(StringType, false), false)
    val labelCol_ = StructField($(labelCol), ArrayType(ArrayType(StringType, false), false), false)
    StructType(schema.fields ++ Array(rawPredCol, labelCol_))
  }

}

object CRFPredictor extends DefaultParamsReadable[CRFPredictor] {
  override def load(path: String): CRFPredictor = super.load(path)
}
