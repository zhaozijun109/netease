package com.netease.easyml.ml.transform

import org.apache.spark.ml.Transformer
import org.apache.spark.ml.param.shared.HasSeed
import org.apache.spark.ml.param.{BooleanParam, DoubleParam, ParamMap, ParamValidators}
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, Dataset}

/**
 * Created by linjiuning on 2020/7/22.
 */
class Sample(override val uid: String) extends Transformer
  with HasSeed with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("sample"))

  val fraction: DoubleParam = new DoubleParam(this, "fraction", "Fraction of rows to generate, range [0.0, 1.0]", ParamValidators.inRange(0.0, 1.0, true, true))

  def setFraction(value: Double): this.type = set(fraction, value)

  def getFraction: Double = $(fraction)

  val withReplacement: BooleanParam = new BooleanParam(this, "withReplacement", "Sample with replacement or not.")

  def setWithReplacement(value: Boolean): this.type = set(withReplacement, value)

  def getWithReplacement: Boolean = $(withReplacement)

  setDefault(withReplacement, false)

  def setSeed(value: Long): this.type = set(seed, value)

  override def transform(dataset: Dataset[_]): DataFrame = {
    if (isDefined(seed))
      dataset.sample($(withReplacement), $(fraction), $(seed)).toDF()
    else
      dataset.sample($(withReplacement), $(fraction)).toDF()
  }

  override def copy(extra: ParamMap): Transformer = defaultCopy(extra)

  override def transformSchema(schema: StructType): StructType = {
    schema
  }
}

object Sample extends DefaultParamsReadable[Sample] {
  override def load(path: String): Sample = super.load(path)
}