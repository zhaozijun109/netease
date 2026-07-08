package com.netease.music.da.transfer.common.handler

import com.netease.music.da.transfer.common.conf.Properties._
import com.netease.music.da.transfer.common.conf.Property
import com.netease.music.da.transfer.common.handler.ColumnHandler._
import com.netease.music.da.transfer.common.util.DataTypeUtil
import org.apache.spark.sql.types.{DataTypes, StructField, StructType}
import org.apache.spark.sql.{DataFrame, SparkSession}

trait InputType

case class AddColumn() extends InputType

case class ModifyColumn() extends InputType

class ColumnHandler(spark: SparkSession) extends AbstractHandler(spark) {
  override def confPrefix: String = "spark.transmit.handler.column"

  override def handler(dataFrame: DataFrame): DataFrame = {
    val outputColumnsProperties = this.properties.getProperties(OUTPUT_COLUMNS)
    val transferCols = outputColumnsProperties.map { properties =>
        val col = properties.getProperty(MAP_ID).get
        val inputCol = properties.getProperty(INPUT_COLUMN).get
        (inputCol, col)
      }.toMap

    spark.newSession()

    val originSchema: StructType = dataFrame.schema
    val schema: StructType = {
      val newSchema = originSchema.map { field =>
        val name = transferCols.getOrElse(field.name, field.name)
        if (field.name != name) {
          StructField(name, field.dataType)
        } else {
          field
        }
      }
      StructType(newSchema)
    }
    spark.createDataFrame(dataFrame.rdd, schema)
  }

}

object ColumnHandler {
  val OUTPUT_COLUMNS = Property("outputColumns", Option.apply(List[String]()), toListFunc)
  val DATATYPE = Property("datatype", Option.apply(DataTypes.StringType), DataTypeUtil.stringToDataType)
  val INPUT_TYPE = Property("inputType", Option.empty, {
    case "addColumn" =>
      AddColumn()
    case "modifyColumn" =>
      ModifyColumn()
    case name =>
      throw new IllegalArgumentException(s"Unknown input type $name.")
  })
  val INPUT_VALUE = Property("inputValue", Option.empty, toStringFunc)
  val INPUT_COLUMN = Property("inputColumn", Option.empty, toStringFunc)
}