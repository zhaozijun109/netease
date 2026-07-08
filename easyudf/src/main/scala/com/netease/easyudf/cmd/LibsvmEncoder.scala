package com.netease.easyudf.cmd

import com.netease.easyml.common.cmds.UserDefinedCmd
import com.netease.easyml.ml.util.SchemaUtils
import com.netease.easyudf.cmd.LibsvmEncoder.encode
import org.apache.spark.ml.feature_.VectorAssembler
import org.apache.spark.ml.linalg.Vector
import org.apache.spark.mllib.linalg.{Vector => OldVector}
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions.{col, udf}
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.collection.mutable.ArrayBuffer

case class LibsvmEncoderArgs(input: String, featuresCol: String = "features",
                             include: String = null, exclude: String = null)

class LibsvmEncoder extends UserDefinedCmd[LibsvmEncoderArgs] {

  override def apply(spark: SparkSession, args: LibsvmEncoderArgs): DataFrame = {
    val df = spark.table(args.input)
    val columns = Select.filterColumns(df.columns, args.include, args.exclude)
    encode(df, columns, args.featuresCol)
  }

}

object LibsvmEncoder {

  val encoderUdf: UserDefinedFunction = udf { vec: Any =>
    val writer = ArrayBuffer.empty[String]
    vec match {
      case v: Vector =>
        v.foreachActive { case (i, v) =>
          writer.append(s"${i + 1}:$v")
        }
      case v: OldVector =>
        v.foreachActive { case (i, v) =>
          writer.append(s"${i + 1}:$v")
        }
      case v => throw new IllegalArgumentException(
        "function vector_to_array requires a non-null input argument and input type must be " +
          "`org.apache.spark.ml.linalg.Vector` or `org.apache.spark.mllib.linalg.Vector`, " +
          s"but got ${if (v == null) "null" else v.getClass.getName}.")
    }
    writer.mkString(" ")
  }

  def encode(df: DataFrame, columns: Array[String], featuresCol: String): DataFrame = {
    var newDf = new VectorAssembler()
      .setInputCols(columns)
      .setOutputCol(featuresCol)
      .setKeepInputCol(false)
      .setHandleInvalid("keep_zero")
      .transform(df)
    val size = if (columns.exists(col => SchemaUtils.isVectorType(df.schema, col))) {
      newDf.first().getAs[Vector](featuresCol).size
    } else {
      columns.length
    }
    newDf = newDf.withColumn(s"${featuresCol}_$size", encoderUdf(col(featuresCol))).drop(featuresCol)
    newDf
  }
}
