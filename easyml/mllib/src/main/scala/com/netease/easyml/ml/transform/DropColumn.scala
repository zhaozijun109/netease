package com.netease.easyml.ml.transform

import org.apache.spark.ml.Transformer
import org.apache.spark.ml.param.ParamMap
import org.apache.spark.ml.param.shared.HasInputCols
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, Dataset}

/**
 * Created by linjiuning on 2020/7/20.
 */
class DropColumn(override val uid: String) extends Transformer
  with HasInputCols with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("drop_column"))

  def setInputCols(value: Array[String]): this.type = set(inputCols, value)

  override def transform(dataset: Dataset[_]): DataFrame = {
    dataset.drop($(inputCols): _*).toDF()
  }

  override def copy(extra: ParamMap): DropColumn = defaultCopy(extra)

  override def transformSchema(schema: StructType): StructType = {
    val fields = schema.fields.filter(it => !$(inputCols).contains(it.name))
    StructType(fields)
  }
}

object DropColumn extends DefaultParamsReadable[DropColumn] {
  override def load(path: String): DropColumn = super.load(path)
}
