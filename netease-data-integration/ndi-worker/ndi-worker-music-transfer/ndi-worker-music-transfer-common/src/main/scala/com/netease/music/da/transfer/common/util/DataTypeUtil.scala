package com.netease.music.da.transfer.common.util

import java.sql.{Date, Timestamp}

import org.apache.spark.sql.types.DataType
import org.apache.spark.sql.types.DataTypes._

object DataTypeUtil {
  val dataTypes: Map[String, DataType] = Map(
    "string" -> StringType,
    "boolean" -> BooleanType,
    "date" -> DateType,
    "timestamp" -> TimestampType,
    "double" -> DoubleType,
    "float" -> FloatType,
    "integer" -> IntegerType,
    "long" -> LongType,
    "short" -> ShortType
  )

  def legalTransfer: Map[DataType, Set[DataType]] = Map[DataType, Set[DataType]](
    StringType -> Set[DataType](StringType, BooleanType, DoubleType,
      FloatType, IntegerType, LongType, ShortType, DateType, TimestampType),

    BooleanType -> Set[DataType](StringType, BooleanType),

    DateType -> Set[DataType](StringType, DateType, TimestampType, LongType),

    TimestampType -> Set[DataType](StringType, DateType, TimestampType, LongType),

    DoubleType -> Set[DataType](StringType, DoubleType),

    FloatType -> Set[DataType](StringType, DoubleType, FloatType),

    IntegerType -> Set[DataType](StringType, DoubleType, FloatType, IntegerType, LongType),

    LongType -> Set[DataType](StringType, DateType, TimestampType, DoubleType, FloatType, LongType),

    ShortType -> Set[DataType](StringType, DoubleType, FloatType, LongType, IntegerType, ShortType)
  )

  def stringToDataType(name: String): DataType = {
    dataTypes(name.toLowerCase())
  }

  /**
    * BooleanType -> java.lang.Boolean
    * ByteType -> java.lang.Byte
    * ShortType -> java.lang.Short
    * IntegerType -> java.lang.Integer
    * FloatType -> java.lang.Float
    * DoubleType -> java.lang.Double
    * StringType -> String
    * DecimalType -> java.math.BigDecimal
    * DateType -> java.sql.Date
    * TimestampType -> java.sql.Timestamp
    * BinaryType -> byte array
    * ArrayType -> scala.collection.Seq (use getList for java.util.List)
    * MapType -> scala.collection.Map (use getJavaMap for java.util.Map)
    * StructType -> org.apache.spark.sql.Row
    *
    * You can see it in [[org.apache.spark.sql.Row.get(0)]]
    *
    */
  def transferValue(inputValue: Any, inputType: DataType, outputType: DataType): Any = {
    if (!legalTransfer(inputType).contains(outputType)) {
      throw new IllegalArgumentException(s"Cannot transfer datatype from $inputType into $outputType.")
    }
    if (inputValue == null) {
      throw new IllegalArgumentException(s"Cannot transfer from a null value.")
    }
    inputType match {
      case StringType =>
        val value = inputValue.asInstanceOf[String]
        outputType match {
          case StringType =>
            value
          case BooleanType =>
            "true".equalsIgnoreCase(value)
          case DoubleType =>
            value.toDouble
          case FloatType =>
            value.toFloat
          case IntegerType =>
            value.toInt
          case LongType =>
            value.toLong
          case ShortType =>
            value.toShort
          case DateType =>
            Date.valueOf(value)
          case TimestampType =>
            Timestamp.valueOf(value)
        }
      case BooleanType =>
        val value = inputValue.asInstanceOf[java.lang.Boolean]
        outputType match {
          case StringType =>
            value.toString
          case BooleanType =>
            value
        }
      case DateType =>
        val value = inputValue.asInstanceOf[Date]
        outputType match {
          case DateType =>
            value
          case TimestampType =>
            new Timestamp(value.getTime)
          case LongType =>
            value.getTime
        }
      case TimestampType =>
        val value = inputValue.asInstanceOf[Timestamp]
        outputType match {
          case StringType =>
            value.toString
          case DateType =>
            new Date(value.getTime)
          case TimestampType =>
            value
          case LongType =>
            value.getTime
        }
      case DoubleType =>
        val value = inputValue.asInstanceOf[java.lang.Double]
        outputType match {
          case StringType =>
            value.toString
          case DoubleType =>
            value
        }

      case FloatType =>
        val value = inputValue.asInstanceOf[java.lang.Float]
        outputType match {
          case StringType =>
            value.toString
          case DoubleType =>
            value.toDouble
          case FloatType =>
            value
        }

      case IntegerType =>
        val value = inputValue.asInstanceOf[java.lang.Integer]
        outputType match {
          case StringType =>
            value.toString
          case DoubleType =>
            value.toDouble
          case FloatType =>
            value.toFloat
          case LongType =>
            value.toLong
          case IntegerType =>
            value
        }

      case LongType =>
        val value = inputValue.asInstanceOf[java.lang.Long]
        outputType match {
          case StringType =>
            value.toString
          case DateType =>
            new Date(value)
          case TimestampType =>
            new Timestamp(value)
          case DoubleType =>
            value.toDouble
          case FloatType =>
            value.toFloat
          case LongType =>
            value
        }

      case ShortType =>
        val value = inputValue.asInstanceOf[java.lang.Short]
        outputType match {
          case StringType =>
            value.toString
          case DoubleType =>
            value.toDouble
          case FloatType =>
            value.toFloat
          case LongType =>
            value.toLong
          case IntegerType =>
            value.toShort
          case ShortType =>
            value
        }
    }
  }
}
