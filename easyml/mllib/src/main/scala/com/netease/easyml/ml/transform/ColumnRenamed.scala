package com.netease.easyml.ml.transform

import org.apache.spark.ml.Transformer
import org.apache.spark.ml.param.ParamMap
import org.apache.spark.ml.param.shared.{HasInputCols, HasOutputCols}
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.types.{StructField, StructType}
import org.apache.spark.sql.{DataFrame, Dataset}

/**
 * Created by linjiuning on 2020/7/20.
 */
class ColumnRenamed(override val uid: String) extends Transformer
  with HasInputCols with HasOutputCols with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("column_renamed"))

  def setInputCols(value: Array[String]): this.type = set(inputCols, value)

  def setOutputCols(value: Array[String]): this.type = set(outputCols, value)

  override def transform(dataset: Dataset[_]): DataFrame = {
    require($(inputCols).length == $(outputCols).length, "inputCols size must equal outputCols.")
    var dataset_ = dataset.toDF()
    for ((input, output) <- $(inputCols).zip($(outputCols))) {
      dataset_ = dataset_.withColumnRenamed(input, output)
    }
    dataset_
  }

  override def copy(extra: ParamMap): Transformer = defaultCopy(extra)

  override def transformSchema(schema: StructType): StructType = {
    val map = $(inputCols).zip($(outputCols)).toMap
    val fields = schema.fields
    val newFields = fields.map(it => {
      if (map.contains(it.name)) {
        StructField(map(it.name), it.dataType, it.nullable, it.metadata)
      } else
        it
    })
    StructType(newFields)
  }
}

object ColumnRenamed extends DefaultParamsReadable[ColumnRenamed] {
  override def load(path: String): ColumnRenamed = super.load(path)
}