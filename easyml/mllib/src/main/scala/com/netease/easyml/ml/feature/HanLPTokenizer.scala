package com.netease.easyml.ml.feature

import com.hankcs.hanlp.HanLP
import com.hankcs.hanlp.seg.Segment
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

trait HanLPTokenizerParams extends Params with HasInputCol with HasOutputCol with HasLowercase {
  def setInputCol(value: String): this.type = set(inputCol, value)

  def setOutputCol(value: String): this.type = set(outputCol, value)

  def setLowercase(value: Boolean): this.type = set(lowercase, value)

  val skipEmpty: BooleanParam = new BooleanParam(this, "skipEmpty",
    "whether to skip empty text.")

  def setSkipEmpty(value: Boolean): this.type = set(skipEmpty, value)

  def getSkipEmpty: Boolean = $(skipEmpty)

  val withPos: BooleanParam = new BooleanParam(this, "withPos",
    "whether to output token with pos.")

  def setWithPos(value: Boolean): this.type = set(skipEmpty, value)

  def getWithPos: Boolean = $(withPos)

  val customDictionary: Param[String] = new Param[String](this, "customDictionary",
    "custom dictionary path")

  def setCustomDictionary(value: String): this.type = set(customDictionary, value)

  def getCustomDictionary: String = $(customDictionary)

  val delimiter: Param[String] = new Param[String](this, "delimiter",
    "delimiter of custom dictionary")

  def setDelimiter(value: String): this.type = set(delimiter, value)

  def getDelimiter: String = $(delimiter)

  val overwrite: BooleanParam = new BooleanParam(this, "overwrite",
    "whether to overwrite hanlp default dictionary.")

  def setOverwrite(value: Boolean): this.type = set(overwrite, value)

  def getOverwrite: Boolean = $(overwrite)

  val algorithm: Param[String] = new Param[String](this, "algorithm",
    "hanlp segment algorithm", ParamValidators.inArray(Array("viterbi", "dat", "crf", "perceptron", "nshort")))

  def setAlgorithm(value: String): this.type = set(algorithm, value)

  def getAlgorithm: String = $(algorithm)

  setDefault(lowercase -> true, skipEmpty -> false, withPos -> false, overwrite -> false,
    algorithm -> "viterbi", customDictionary -> "", delimiter -> " ")

  protected def validateAndTransformSchema(schema: StructType): StructType = {
    SchemaUtils.checkColumnType(schema, $(inputCol), StringType)
    SchemaUtils.appendColumn(schema, $(outputCol), ArrayType(StringType, true))
  }
}

class HanLPTokenizer(override val uid: String)
  extends Model[HanLPTokenizer] with HanLPTokenizerParams with DefaultParamsWritable {

  import HanLPTokenizer._

  def this() = this(Identifiable.randomUID("hanlpTok"))

  override def copy(extra: ParamMap): HanLPTokenizer = defaultCopy(extra)

  override def transform(dataset: Dataset[_]): DataFrame = {
    val outputSchema = transformSchema(dataset.schema, logging = true)
    val inputCol = getInputCol
    val lowercase = getLowercase
    val withPos = getWithPos
    val skipEmpty = getSkipEmpty
    val customDictionary = getCustomDictionary
    val overwrite = getOverwrite
    val algorithm = getAlgorithm
    val delimiter = getDelimiter
    val newRdd = dataset.toDF.rdd.mapPartitions(iter => {
      val segment = getOrCreate(customDictionary, overwrite, algorithm, delimiter)
      var newIter = iter.map(row => {
        val text = Option(row.getAs[String](inputCol)).getOrElse("")
        val tokens = if (text.isEmpty) {
          Seq()
        } else {
          var newText = text
          if (lowercase) {
            newText = newText.toLowerCase()
          }
          segment.seg(newText).asScala.map(term => if (withPos) term.toString else term.word)
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

object HanLPTokenizer extends DefaultParamsReadable[HanLPTokenizer] {

  def getOrCreate(path: String, overwrite: Boolean, algorithm: String, delimiter: String): Segment = {
    ResourceManager.getOrCreate(path, () => {
      val segment = if (algorithm.nonEmpty)
        HanLP.newSegment(algorithm)
      else
        HanLP.newSegment()
      if (!StringUtil.isEmpty(path)) {
        SegmentUtil.addVocab(path, delimiter, overwrite)
      }
      segment
    })
  }

  override def load(path: String): HanLPTokenizer = super.load(path)
}
