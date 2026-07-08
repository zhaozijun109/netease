package com.netease.easyml.ml.feature

import com.huaban.analysis.jieba.JiebaSegmenter
import com.netease.easyml.common.resource.ResourceManager
import com.netease.easyml.common.util.{SegmentUtil, StringUtil}
import com.netease.easyml.ml.param.HasLowercase
import com.netease.easyml.ml.util.SchemaUtils
import org.apache.spark.ml.Model
import org.apache.spark.ml.param._
import org.apache.spark.ml.param.shared.{HasInputCol, HasOutputCol}
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.types.{ArrayType, StringType, StructType}
import org.apache.spark.sql.{DataFrame, Dataset, Row}

import scala.collection.JavaConverters._

/**
 * Created by linjiuning on 2020/8/20.
 */

trait JiebaTokenizerParams extends Params with HasInputCol with HasOutputCol with HasLowercase {
  def setInputCol(value: String): this.type = set(inputCol, value)

  def setOutputCol(value: String): this.type = set(outputCol, value)

  def setLowercase(value: Boolean): this.type = set(lowercase, value)

  val skipEmpty: BooleanParam = new BooleanParam(this, "skipEmpty",
    "whether to skip empty text.")

  def setSkipEmpty(value: Boolean): this.type = set(skipEmpty, value)

  def getSkipEmpty: Boolean = $(skipEmpty)

  val customDictionary: Param[String] = new Param[String](this, "customDictionary",
    "custom dictionary path")

  def setCustomDictionary(value: String): this.type = set(customDictionary, value)

  def getCustomDictionary: String = $(customDictionary)

  val delimiter: Param[String] = new Param[String](this, "delimiter",
    "delimiter of custom dictionary")

  def setDelimiter(value: String): this.type = set(delimiter, value)

  def getDelimiter: String = $(delimiter)

  val mode: Param[String] = new Param[String](this, "mode",
    "jieba segment algorithm", ParamValidators.inArray(Array("search", "index")))

  def setMode(value: String): this.type = set(mode, value)

  def getMode: String = $(mode)

  setDefault(lowercase -> true, skipEmpty -> false, mode -> "search", customDictionary -> "", delimiter -> " ")

  protected def validateAndTransformSchema(schema: StructType): StructType = {
    SchemaUtils.checkColumnType(schema, $(inputCol), StringType)
    SchemaUtils.appendColumn(schema, $(outputCol), ArrayType(StringType, true))
  }
}

class JiebaTokenizer(override val uid: String)
  extends Model[JiebaTokenizer] with JiebaTokenizerParams with DefaultParamsWritable {

  import JiebaTokenizer._

  def this() = this(Identifiable.randomUID("jiebaTok"))

  override def copy(extra: ParamMap): JiebaTokenizer = defaultCopy(extra)

  override def transform(dataset: Dataset[_]): DataFrame = {
    val outputSchema = transformSchema(dataset.schema, logging = true)
    val inputCol = getInputCol
    val lowercase = getLowercase
    val skipEmpty = getSkipEmpty
    val customDictionary = getCustomDictionary
    val delimiter = getDelimiter
    val segMode = JiebaSegmenter.SegMode.valueOf($(mode).toUpperCase)
    val newRdd = dataset.toDF.rdd.mapPartitions(iter => {
      val segment = getOrCreate(customDictionary, delimiter)
      var newIter = iter.map(row => {
        val text = Option(row.getAs[String](inputCol)).getOrElse("")
        val tokens = if (text.isEmpty) {
          Seq()
        } else {
          var newText = text
          if (lowercase) {
            newText = newText.toLowerCase()
          }
          segment.process(newText, segMode).asScala.map(_.word)
        }
        (row, tokens)
      })
      if (skipEmpty) {
        newIter = newIter.filter(_._2.nonEmpty)
      }
      newIter.map {
        case (row, tokens) =>
          Row.fromSeq(row.toSeq :+ tokens)
      }
    })
    dataset.sparkSession.createDataFrame(newRdd, outputSchema)
  }

  override def transformSchema(schema: StructType): StructType = {
    validateAndTransformSchema(schema)
  }
}

object JiebaTokenizer extends DefaultParamsReadable[JiebaTokenizer] {

  def getOrCreate(path: String, delimiter: String): JiebaSegmenter = {
    ResourceManager.getOrCreate(path, () => {
      val segment = new JiebaSegmenter

      if (!StringUtil.isEmpty(path)) {
        SegmentUtil.loadCustomDictionary(path, delimiter)
      }
      segment
    })
  }

  override def load(path: String): JiebaTokenizer = super.load(path)
}
