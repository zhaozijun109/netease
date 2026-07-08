package com.netease.easyml.ml.feature

import org.apache.spark.ml.Transformer
import org.apache.spark.ml.param.ParamMap
import org.apache.spark.ml.param.shared._
import org.apache.spark.ml.util._
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Dataset, Row}

/**
 * Created by linjiuning on 2020/9/16.
 * A feature transformer that merges multiple columns into a array column.
 */
class ArrayAssembler(override val uid: String)
  extends Transformer with HasInputCols with HasOutputCol with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("arrAssembler"))

  def setInputCols(value: Array[String]): this.type = set(inputCols, value)

  def setOutputCol(value: String): this.type = set(outputCol, value)

  override def transform(dataset: Dataset[_]): DataFrame = {
    val outputSchema = transformSchema(dataset.schema, logging = true)
    val newRdd = dataset.toDF().rdd
      .map(row => {
        val output = $(inputCols).map(col => row.getAs[Any](col))
        Row.fromSeq(row.toSeq :+ output)
      })

    dataset.sparkSession.createDataFrame(newRdd, outputSchema)
  }

  override def transformSchema(schema: StructType): StructType = {
    val inputColNames = $(inputCols)
    val outputColName = $(outputCol)
    val dataTypes = inputColNames.map { name =>
      schema(name).dataType
    }.distinct
    if (dataTypes.length > 1) {
      throw new IllegalArgumentException("Data type of inputCols must be the same.")
    }
    val incorrectColumns = inputColNames.flatMap { name =>
      schema(name).dataType match {
        case _: NumericType | BooleanType | StringType => None
        case other => Some(s"Data type $other of column $name is not supported.")
      }
    }
    if (incorrectColumns.nonEmpty) {
      throw new IllegalArgumentException(incorrectColumns.mkString("\n"))
    }
    if (schema.fieldNames.contains(outputColName)) {
      throw new IllegalArgumentException(s"Output column $outputColName already exists.")
    }
    StructType(schema.fields :+ StructField(outputColName, new ArrayType(dataTypes.last, true), true))
  }

  override def copy(extra: ParamMap): ArrayAssembler = defaultCopy(extra)
}

object ArrayAssembler extends DefaultParamsReadable[ArrayAssembler] {

  override def load(path: String): ArrayAssembler = super.load(path)
}
