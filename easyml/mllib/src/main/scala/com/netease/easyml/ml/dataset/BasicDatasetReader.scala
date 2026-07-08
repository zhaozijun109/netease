package com.netease.easyml.ml.dataset

import java.util

import com.netease.easyml.annotation.Register
import com.netease.easyml.common.collection
import org.apache.spark.ml.param.{Param, ParamValidators, Params, StringArrayParam}
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
 * Created by linjiuning on 2020/7/6.
 */

trait BasicDatasetReaderParams extends Params {
  val format: Param[String] = new Param[String](this, "format", "Specifies the input data source format.",
    ParamValidators.inArray(Array("csv", "json", "parquet", "jdbc", "libsvm")))

  def getFormat: String = $(format)

  val schema: Param[String] = new Param[String](this, "schema", "Specifies the schema by using the input DDL-formatted string.")

  def getSchema: String = $(schema)

  setDefault(schema, "")

  val options: Param[Map[String, String]] = new Param[Map[String, String]](this, "options", "Adds input options for the underlying data source.")

  def getOptions: Map[String, String] = $(options)

  setDefault(options, Map.empty[String, String])

  val columns: StringArrayParam = new StringArrayParam(this, "columns", "Specifies the select columns")

  def getColumns: Array[String] = $(columns)

  setDefault(columns, Array.empty[String])
}

@Register(name = "basic", isDefault = true)
class BasicDatasetReader(override val uid: String) extends DatasetReader with BasicDatasetReaderParams with DefaultParamsWritable {
  def this() = this(Identifiable.randomUID("basic_reader"))

  def setFormat(value: String): this.type = set(format, value)

  def setSchema(value: String): this.type = set(schema, value)

  def setOptions(value: Map[String, String]): this.type = set(options, value)

  def setColumns(value: Array[String]): this.type = set(columns, value)

  override def read(spark: SparkSession, params: collection.Params): DataFrame = {
    val path = params.get("path", classOf[String])
    var format = getFormat
    if (params.containsKey("format")) {
      format = params.get("format", classOf[String])
    }

    val mOptions = params.get[java.util.Map[String, String]]("options", new util.HashMap[String, String]())

    for ((key, value) <- $(options)) {
      if (!mOptions.containsKey(key)) {
        mOptions.put(key, value)
      }
    }

    var reader = spark.read.format(format)
      .options(mOptions)

    if ($(schema).nonEmpty)
      reader = reader.schema($(schema))

    val df = reader.load(path)

    if ($(columns).nonEmpty) {
      df.select($(columns).map(col): _*)
    } else
      df
  }
}

object BasicDatasetReader extends DefaultParamsReadable[BasicDatasetReader] {

  override def load(path: String): BasicDatasetReader = super.load(path)
}
