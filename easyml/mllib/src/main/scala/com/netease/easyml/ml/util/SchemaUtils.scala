
package com.netease.easyml.ml.util

import com.netease.easyml.common.util.SparkUtil
import org.apache.spark.sql.types._


// This originates from apache-spark utils for handling schemas.
object SchemaUtils {

  // TODO: Move the utility methods to SQL.

  /**
   * Check whether the given schema contains a column of the required data type.
   *
   * @param colName  column name
   * @param dataType required column data type
   */
  def checkColumnType(schema: StructType,
                      colName: String,
                      dataType: DataType,
                      msg: String = ""): Unit = {
    val actualDataType = schema(colName).dataType
    val message = if (msg != null && msg.trim.length > 0) " " + msg else ""
    require(actualDataType.equals(dataType),
      s"Column $colName must be of type $dataType but was actually $actualDataType.$message")
  }

  def isColumnType(schema: StructType,
                   colName: String,
                   dataType: DataType): Boolean = {
    val actualDataType = schema(colName).dataType
    actualDataType.equals(dataType)
  }

  /**
   * Check whether the given schema contains a column of one of the require data types.
   *
   * @param colName   column name
   * @param dataTypes required column data types
   */
  def checkColumnTypes(schema: StructType,
                       colName: String,
                       dataTypes: Seq[DataType],
                       msg: String = ""): Unit = {
    val actualDataType = schema(colName).dataType
    val message = if (msg != null && msg.trim.length > 0) " " + msg else ""
    require(dataTypes.exists(actualDataType.equals),
      s"Column $colName must be of type equal to one of the following types: " +
        s"${dataTypes.mkString("[", ", ", "]")} but was actually of type $actualDataType.$message")
  }

  def isColumnTypes(schema: StructType,
                    colName: String,
                    dataTypes: Seq[DataType]): Boolean = {
    val actualDataType = schema(colName).dataType
    dataTypes.exists(actualDataType.equals)
  }

  /**
   * Check whether the given schema contains a column of the string data type.
   *
   * @param colName column name
   */
  def checkStringType(schema: StructType,
                      colName: String,
                      msg: String = ""): Unit = {
    val actualDataType = schema(colName).dataType
    val message = if (msg != null && msg.trim.length > 0) " " + msg else ""
    require(actualDataType.isInstanceOf[StringType], s"Column $colName must be of type " +
      s"StringType but was actually of type $actualDataType.$message")
  }

  def isStringType(schema: StructType,
                   colName: String): Boolean = {
    val actualDataType = schema(colName).dataType
    actualDataType.isInstanceOf[StringType]
  }

  /**
   * Check whether the given schema contains a column of the numeric data type.
   *
   * @param colName column name
   */
  def checkNumericType(schema: StructType,
                       colName: String,
                       msg: String = ""): Unit = {
    val actualDataType = schema(colName).dataType
    val message = if (msg != null && msg.trim.length > 0) " " + msg else ""
    require(actualDataType.isInstanceOf[NumericType], s"Column $colName must be of type " +
      s"NumericType but was actually of type $actualDataType.$message")
  }

  def isNumericType(schema: StructType,
                    colName: String): Boolean = {
    val actualDataType = schema(colName).dataType
    actualDataType.isInstanceOf[NumericType]
  }

  def checkArrayType(schema: StructType,
                     colName: String,
                     msg: String = ""): Unit = {
    val actualDataType = schema(colName).dataType
    val message = if (msg != null && msg.trim.length > 0) " " + msg else ""
    require(actualDataType.isInstanceOf[ArrayType], s"Column $colName must be of type " +
      s"ArrayType but was actually of type $actualDataType.$message")
  }

  def isArrayType(schema: StructType,
                  colName: String): Boolean = {
    val actualDataType = schema(colName).dataType
    actualDataType.isInstanceOf[ArrayType]
  }

  def checkNumericArrayType(schema: StructType,
                            colName: String,
                            msg: String = ""): Unit = {
    val actualDataType = schema(colName).dataType
    val message = if (msg != null && msg.trim.length > 0) " " + msg else ""
    require(isNumericArrayType(schema, colName), s"Column $colName must be of type " +
      s"Numeric ArrayType but was actually of type $actualDataType.$message")
  }

  def isNumericArrayType(schema: StructType,
                         colName: String): Boolean = {
    val actualDataType = schema(colName).dataType
    actualDataType.isInstanceOf[ArrayType] && actualDataType.asInstanceOf[ArrayType].elementType.isInstanceOf[NumericType]
  }

  def checkStringArrayType(schema: StructType,
                           colName: String,
                           msg: String = ""): Unit = {
    val actualDataType = schema(colName).dataType
    val message = if (msg != null && msg.trim.length > 0) " " + msg else ""
    require(isStringArrayType(schema, colName), s"Column $colName must be of type " +
      s"String ArrayType but was actually of type $actualDataType.$message")
  }

  def isStringArrayType(schema: StructType,
                        colName: String): Boolean = {
    val actualDataType = schema(colName).dataType
    actualDataType.isInstanceOf[ArrayType] && actualDataType.asInstanceOf[ArrayType].elementType.isInstanceOf[StringType]
  }

  def checkVectorType(schema: StructType,
                      colName: String,
                      msg: String = ""): Unit = {
    val actualDataType = schema(colName).dataType
    val message = if (msg != null && msg.trim.length > 0) " " + msg else ""
    require(actualDataType.getClass.getSimpleName.equals("VectorUDT"), s"Column $colName must be of type " +
      s"VectorUDT but was actually of type $actualDataType.$message")
  }

  def isVectorType(schema: StructType,
                   colName: String): Boolean = {
    val actualDataType = schema(colName).dataType
    actualDataType.getClass.getSimpleName.equals("VectorUDT")
  }

  def checkArrayOrVectorType(schema: StructType,
                             colName: String,
                             msg: String = ""): Unit = {
    val actualDataType = schema(colName).dataType
    val message = if (msg != null && msg.trim.length > 0) " " + msg else ""
    require(actualDataType.isInstanceOf[ArrayType] || actualDataType.getClass.getSimpleName.equals("VectorUDT"), s"Column $colName must be of type " +
      s"ArrayType or VectorUDT but was actually of type $actualDataType.$message")
  }

  def checkNumericOrArrayOrVectorType(schema: StructType,
                                      colName: String,
                                      msg: String = ""): Unit = {
    val actualDataType = schema(colName).dataType
    val message = if (msg != null && msg.trim.length > 0) " " + msg else ""
    require(actualDataType.isInstanceOf[NumericType] || actualDataType.isInstanceOf[ArrayType] || actualDataType.getClass.getSimpleName.equals("VectorUDT"), s"Column $colName must be of type " +
      s"NumericType, ArrayType or VectorUDT but was actually of type $actualDataType.$message")
  }

  /**
   * Appends a new column to the input schema. This fails if the given output column already exists.
   *
   * @param schema   input schema
   * @param colName  new column name. If this column name is an empty string "", this method returns
   *                 the input schema unchanged. This allows users to disable output columns.
   * @param dataType new column data type
   * @return new schema with the input column appended
   */
  def appendColumn(
                    schema: StructType,
                    colName: String,
                    dataType: DataType,
                    nullable: Boolean = false): StructType = {
    if (colName.isEmpty) return schema
    appendColumn(schema, StructField(colName, dataType, nullable))
  }

  /**
   * Appends a new column to the input schema. This fails if the given output column already exists.
   *
   * @param schema input schema
   * @param col    New column schema
   * @return new schema with the input column appended
   */
  def appendColumn(schema: StructType, col: StructField): StructType = {
    require(!schema.fieldNames.contains(col.name), s"Column ${col.name} already exists.")
    StructType(schema.fields :+ col)
  }

  def vectorUDT: DataType = {
    val clazz = SparkUtil.classForName("org.apache.spark.ml.linalg.VectorUDT")
    clazz.newInstance().asInstanceOf[DataType]
  }
}
