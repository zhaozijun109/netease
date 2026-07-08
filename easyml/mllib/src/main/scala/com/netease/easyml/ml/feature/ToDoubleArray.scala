package com.netease.easyml.ml.feature

import com.netease.easyml.ml.metric.toDoubleArrayUdf
import com.netease.easyml.ml.util.SchemaUtils
import org.apache.spark.ml.Transformer
import org.apache.spark.ml.param.ParamMap
import org.apache.spark.ml.param.shared.{HasInputCol, HasOutputCol}
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.types.{ArrayType, DoubleType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, Dataset}

/**
 * Created by linjiuning on 2020/7/20.
 */
class ToDoubleArray(override val uid: String) extends Transformer
  with HasInputCol with HasOutputCol with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("to_double_array"))

  def setInputCol(value: String): this.type = set(inputCol, value)

  def setOutputCol(value: String): this.type = set(outputCol, value)

  override def transform(dataset: Dataset[_]): DataFrame = {
    val schema = dataset.schema
    SchemaUtils.checkNumericOrArrayOrVectorType(schema, $(inputCol))

    dataset.withColumn($(outputCol), toDoubleArrayUdf(col($(inputCol))))
  }

  override def copy(extra: ParamMap): Transformer = defaultCopy(extra)

  override def transformSchema(schema: StructType): StructType = {
    val outDataType = ArrayType(DoubleType, false)
    val field = StructField($(outputCol), outDataType, false)
    if ($(inputCol).equals($(outputCol))) {
      val idx = schema.fieldIndex($(inputCol))
      schema.fields(idx) = field
      StructType(schema.fields)
    } else {
      StructType(schema.fields ++ Array(field))
    }
  }
}

object ToDoubleArray extends DefaultParamsReadable[ToDoubleArray] {
  override def load(path: String): ToDoubleArray = super.load(path)
}
