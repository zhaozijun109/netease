package com.netease.easyml.ml.sklearn.feature_extraction

import com.netease.easyml.ml.param.{HasLowercase, HasNgramRange}
import com.netease.easyml.ml.util.SchemaUtils
import org.apache.spark.ml.param._
import org.apache.spark.ml.param.shared.{HasInputCol, HasOutputCol}
import org.apache.spark.sql.types.{StringType, StructType}

/**
 * Created by linjiuning on 2020/8/7.
 */
trait VectorizerParams extends Params with HasInputCol with HasOutputCol with HasLowercase with HasNgramRange {
  val binary: BooleanParam =
    new BooleanParam(this, "binary", "If True, all non zero counts are set to 1. This is useful for discrete probabilistic models that model binary events rather than integer counts.")

  def getBinary: Boolean = $(binary)

  val tokenPattern: Param[String] =
    new Param[String](this, "tokenPattern", "Regular expression denoting what constitutes a \"token\", only used " +
      "if ``analyzer == 'word'``. The default regexp select tokens of 2 " +
      "or more alphanumeric characters (punctuation is completely ignored " +
      "and always treated as a token separator).")

  def getTokenPattern: String = $(tokenPattern)

  val analyzer: Param[String] =
    new Param[String](this, "analyzer", "Whether the feature should be made of word n-gram or character n-grams. " +
      "Option 'char_wb' creates character n-grams only from text inside " +
      "word boundaries; n-grams at the edges of words are padded with space.",
      ParamValidators.inArray(Array("word", "char", "char_wb")))

  def getAnalyzer: String = $(analyzer)

  protected def validateAndTransformSchema(schema: StructType): StructType = {
    SchemaUtils.checkColumnType(schema, $(inputCol), StringType)
    SchemaUtils.appendColumn(schema, $(outputCol), SchemaUtils.vectorUDT)
  }

  setDefault(binary -> false, ngramRange -> Array(1, 1), lowercase -> true,
    tokenPattern -> "[^\\s]+", analyzer -> "word")

  setDefault(inputCol -> "text", outputCol -> "features")
}
