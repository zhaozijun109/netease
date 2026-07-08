package com.netease.easyml.ml.transform

import org.apache.spark.ml.Transformer
import org.apache.spark.ml.param.shared.{HasInputCol, HasOutputCol}
import org.apache.spark.ml.param.{Param, ParamMap}
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.catalyst.parser.CatalystSqlParser
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.types.{StructField, StructType}
import org.apache.spark.sql.{DataFrame, Dataset}

/**
 * Created by linjiuning on 2020/9/16.
 */
class Cast(override val uid: String) extends Transformer
  with HasInputCol with HasOutputCol with DefaultParamsWritable {
  val ALL_DATA_TYPES: Array[String] = Array("string", "boolean", "byte", "short", "int", "long",
    "float", "double", "decimal", "date", "timestamp")

  def this() = this(Identifiable.randomUID("cast"))

  val to: Param[String] = new Param[String](this, "to", "casts the column to a different data type.", (value: String) => ALL_DATA_TYPES.contains(value.toLowerCase))

  def setTo(value: String): this.type = set(to, value)

  def getTo: String = $(to)

  def setInputCol(value: String): this.type = set(inputCol, value)

  def setOutputCol(value: String): this.type = set(outputCol, value)

  override def transform(dataset: Dataset[_]): DataFrame = {
    dataset.withColumn($(outputCol), col($(inputCol)).cast($(to)))
  }

  override def copy(extra: ParamMap): Transformer = defaultCopy(extra)

  override def transformSchema(schema: StructType): StructType = {
    val outputColName = $(outputCol)
    if (schema.fieldNames.contains(outputColName)) {
      throw new IllegalArgumentException(s"Output column $outputColName already exists.")
    }
    val inputField = schema($(inputCol))
    val dataType = CatalystSqlParser.parseDataType($(to))

    StructType(schema.fields :+ StructField(outputColName, dataType, inputField.nullable))
  }
}

object Cast extends DefaultParamsReadable[Cast] {
  override def load(path: String): Cast = super.load(path)
}