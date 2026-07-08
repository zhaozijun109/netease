package com.netease.easyml.ml.dataset

import java.util

import com.netease.easyml.annotation.Register
import com.netease.easyml.common.collection
import org.apache.spark.ml.param.{Param, ParamValidators, Params, StringArrayParam}
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SaveMode}

/**
 * Created by linjiuning on 2020/7/6.
 */

trait BasicDatasetWriterParams extends Params {
  val format: Param[String] = new Param[String](this, "format", "Specifies the input data source format.",
    ParamValidators.inArray(Array("csv", "json", "parquet", "jdbc", "text")))

  def getFormat: String = $(format)

  val options: Param[Map[String, String]] = new Param[Map[String, String]](this, "options", "Adds input options for the underlying data source.")

  def getOptions: Map[String, String] = $(options)

  setDefault(options, Map.empty[String, String])

  val columns: StringArrayParam = new StringArrayParam(this, "columns", "Specifies the select columns")

  def getColumns: Array[String] = $(columns)

  setDefault(columns, Array.empty[String])

  val mode: Param[String] = new Param[String](this, "mode", "Specifies the behavior when data or table already exists. Options include.",
    ParamValidators.inArray(Array("overwrite", "append", "ignore", "error", "errorifexists")))

  def getMode: String = $(mode)

  setDefault(mode, SaveMode.ErrorIfExists.toString.toLowerCase())
}

@Register(name = "basic", isDefault = true)
class BasicDatasetWriter(override val uid: String) extends DatasetWriter with BasicDatasetWriterParams with DefaultParamsWritable {
  def this() = this(Identifiable.randomUID("basic_writer"))

  def setFormat(value: String): this.type = set(format, value)

  def setOptions(value: Map[String, String]): this.type = set(options, value)

  def setColumns(value: Array[String]): this.type = set(columns, value)

  def setMode(value: String): this.type = set(mode, value)

  override def write(dataFrame: DataFrame, params: collection.Params): Unit = {
    val path = params.get("path", classOf[String])
    var format = getFormat
    if (params.containsKey("format")) {
      format = params.get("format", classOf[String])
    }
    val options = params.get[java.util.Map[String, String]]("options", new util.HashMap[String, String]())
    for ((key, value) <- getOptions) {
      if (!options.containsKey(key)) {
        options.put(key, value)
      }
    }

    val mode = params.get[String]("mode", getMode)

    val df = if ($(columns).nonEmpty) {
      dataFrame.select($(columns).map(col): _*)
    } else
      dataFrame

    val writer = df.write
      .format(format)
      .options(options)
      .mode(mode)

    if (path.contains("/"))
      writer.save(path)
    else
      writer.saveAsTable(path)
  }
}

object BasicDatasetWriter extends DefaultParamsReadable[BasicDatasetWriter] {

  override def load(path: String): BasicDatasetWriter = super.load(path)
}
