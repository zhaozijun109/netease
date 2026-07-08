package com.netease.easyml.ml.feature

import java.util.regex.Pattern

import com.hankcs.hanlp.HanLP
import com.netease.easyml.common.util.StringUtil
import com.netease.easyml.ml.param.HasLowercase
import com.netease.easyml.ml.util.SchemaUtils
import org.apache.spark.ml.Model
import org.apache.spark.ml.param._
import org.apache.spark.ml.param.shared.{HasInputCol, HasOutputCol}
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.types.{StringType, StructType}
import org.apache.spark.sql.{DataFrame, Dataset, Row}

/**
 * Created by linjiuning on 2020/8/20.
 */

trait TextNormalizeParams extends Params with HasInputCol with HasOutputCol with HasLowercase {
  def setInputCol(value: String): this.type = set(inputCol, value)

  def setOutputCol(value: String): this.type = set(outputCol, value)

  def setLowercase(value: Boolean): this.type = set(lowercase, value)

  val fullToHalf: BooleanParam = new BooleanParam(this, "fullToHalf",
    "whether to convert full characters to half characters before tokenizing.")

  def setFullToHalf(value: Boolean): this.type = set(simplified, value)

  def getFullToHalf: Boolean = $(simplified)

  val simplified: BooleanParam = new BooleanParam(this, "simplified",
    "whether to convert all characters to simplified chinese characters before tokenizing.")

  def setSimplified(value: Boolean): this.type = set(simplified, value)

  def getSimplified: Boolean = $(simplified)

  val removeEmoji: BooleanParam = new BooleanParam(this, "removeEmoji",
    "whether to remove emoji characters.")

  def setRemoveEmoji(value: Boolean): this.type = set(removeEmoji, value)

  def getRemoveEmoji: Boolean = $(removeEmoji)

  val removePunctuation: BooleanParam = new BooleanParam(this, "removePunctuation",
    "whether to remove punctuations.")

  def setRemovePunctuation(value: Boolean): this.type = set(removePunctuation, value)

  def getRemovePunctuation: Boolean = $(removePunctuation)

  val skipEmpty: BooleanParam = new BooleanParam(this, "skipEmpty",
    "whether to skip empty text.")

  def setSkipEmpty(value: Boolean): this.type = set(skipEmpty, value)

  def getSkipEmpty: Boolean = $(skipEmpty)

  val regex: Param[Map[String, String]] = new Param[Map[String, String]](this, "regex",
    "regex replacement.")

  def setRegex(value: Map[String, String]): this.type = set(regex, value)

  def getRegex: Map[String, String] = $(regex)

  setDefault(fullToHalf -> true, simplified -> true, lowercase -> true, skipEmpty -> true,
    removeEmoji -> false, removePunctuation -> false)

  protected def validateAndTransformSchema(schema: StructType): StructType = {
    SchemaUtils.checkColumnType(schema, $(inputCol), StringType)
    SchemaUtils.appendColumn(schema, $(outputCol), StringType)
  }
}

class TextNormalize(override val uid: String)
  extends Model[TextNormalize] with TextNormalizeParams with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("textNorm"))

  override def copy(extra: ParamMap): TextNormalize = defaultCopy(extra)

  override def transform(dataset: Dataset[_]): DataFrame = {
    val outputSchema = transformSchema(dataset.schema, logging = true)
    val newRdd = dataset.toDF.rdd.mapPartitions(iter => {
      val map = if (isSet(regex)) {
        $(regex).map {
          case (pt, rep) =>
            (Pattern.compile(pt), rep)
        }
      } else {
        Map.empty[Pattern, String]
      }
      var newIter = iter.map(row => {
        var text = Option(row.getAs[String]($(inputCol))).getOrElse("")
        text = StringUtil.strip(text)
        if (text.nonEmpty) {
          if ($(fullToHalf)) {
            text = StringUtil.fullToHalf(text)
          }
          if ($(lowercase)) {
            text = text.toLowerCase()
          }
          if ($(simplified)) {
            text = HanLP.convertToSimplifiedChinese(text)
          }
        }
        if ($(removeEmoji) && text.nonEmpty) {
          text = StringUtil.removeAllEmojis(text)
        }
        if ($(removePunctuation) && text.nonEmpty) {
          text = StringUtil.removePunctuation(text)
        }
        if (map.nonEmpty && text.nonEmpty) {
          map.foreach {
            case (pattern, str) =>
              if (text.nonEmpty)
                text = pattern.matcher(text).replaceAll(str)
              else
                text
          }
        }
        (row, text)
      })
      if ($(skipEmpty)) {
        newIter = newIter.filter(_._2.nonEmpty)
      }
      newIter.map {
        case (row, text) =>
          Row.fromSeq(row.toSeq :+ text)
      }
    })
    dataset.sparkSession.createDataFrame(newRdd, outputSchema)
  }

  override def transformSchema(schema: StructType): StructType = {
    validateAndTransformSchema(schema)
  }
}

object TextNormalize extends DefaultParamsReadable[TextNormalize] {

  override def load(path: String): TextNormalize = super.load(path)
}
