package com.netease.easyml.ml.transform

import org.apache.spark.ml.Transformer
import org.apache.spark.ml.param.ParamMap
import org.apache.spark.ml.param.shared.HasInputCols
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, Dataset}

/**
 * Created by linjiuning on 2020/11/4.
 */
class Select(override val uid: String) extends Transformer
  with HasInputCols with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("select"))

  def setInputCols(value: Array[String]): this.type = set(inputCols, value)

  override def transform(dataset: Dataset[_]): DataFrame = {
    dataset.select($(inputCols).map(col): _*).toDF()
  }

  override def copy(extra: ParamMap): Select = defaultCopy(extra)

  override def transformSchema(schema: StructType): StructType = {
    val fields = schema.fields.filter(it => $(inputCols).contains(it.name))
    StructType(fields)
  }
}

object Select extends DefaultParamsReadable[Select] {
  override def load(path: String): Select = super.load(path)
}
