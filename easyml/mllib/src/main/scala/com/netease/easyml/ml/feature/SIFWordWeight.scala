package com.netease.easyml.ml.feature

import com.netease.easyml.ml.util.SchemaUtils
import org.apache.spark.ml.Estimator
import org.apache.spark.ml.param._
import org.apache.spark.ml.param.shared.{HasInputCol, HasOutputCol}
import org.apache.spark.ml.util._
import org.apache.spark.sql.types.{ArrayType, DoubleType, StringType, StructType}
import org.apache.spark.sql.{Dataset, Row}
import org.apache.spark.storage.StorageLevel

/**
 * Created by linjiuning on 2020/8/20.
 * A Simple but Tough-to-Beat Baseline for Sentence Embeddings 
 */
trait SIFWordWeightParams extends Params with HasInputCol with HasOutputCol {
  val a: DoubleParam = new DoubleParam(this, "a", "the parameter in the SIF weighting scheme, usually in the range [3e-5, 3e-3]",
    ParamValidators.gt(0))

  def getA: Double = $(a)

  val docLevel: BooleanParam = new BooleanParam(this, "docLevel", "calculate word frequency by doc level or word level.")

  def getDocLevel: Boolean = $(docLevel)

  setDefault(a -> 1e-3, docLevel -> false)

  protected def validateAndTransformSchema(schema: StructType): StructType = {
    SchemaUtils.checkColumnType(schema, $(inputCol), ArrayType(StringType))
    SchemaUtils.appendColumn(schema, $(outputCol), DoubleType)
  }
}

class SIFWordWeight(override val uid: String) extends Estimator[WordWeightModel]
  with SIFWordWeightParams with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("sif_wt"))

  def setInputCol(value: String): this.type = set(inputCol, value)

  def setOutputCol(value: String): this.type = set(outputCol, value)

  def setA(value: Double): this.type = set(a, value)

  def setDocLevel(value: Boolean): this.type = set(docLevel, value)

  override def fit(dataset: Dataset[_]): WordWeightModel = {
    validateAndTransformSchema(dataset.schema)
    val a = getA
    val wordCounts = dataset.select($(inputCol)).rdd.flatMap {
      case Row(sentence: Seq[String]) =>
        if ($(docLevel)) {
          sentence.distinct.map((_, 1L))
        } else {
          sentence.map((_, 1L))
        }
    }.reduceByKey(_ + _)

    wordCounts.persist(StorageLevel.MEMORY_AND_DISK)

    val sum = wordCounts.map(_._2).reduce(_ + _)

    val wordWeights = wordCounts.map {
      case (word, cnt) =>
        val pw = cnt * 1.0 / sum
        val wt = a / (a + pw)
        (word, wt.toFloat)
    }.collectAsMap().toMap
    wordCounts.unpersist()
    new WordWeightModel(wordWeights).setParent(this)
  }

  override def copy(extra: ParamMap): SIFWordWeight = defaultCopy(extra)

  override def transformSchema(schema: StructType): StructType = {
    validateAndTransformSchema(schema)
  }
}

object SIFWordWeight extends DefaultParamsReadable[SIFWordWeight] {
  override def load(path: String): SIFWordWeight = super.load(path)
}