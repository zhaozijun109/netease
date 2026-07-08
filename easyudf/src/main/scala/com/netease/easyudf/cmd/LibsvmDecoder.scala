package com.netease.easyudf.cmd

import com.netease.easyml.common.cmds.UserDefinedCmd
import com.netease.easyudf.cmd.LibsvmDecoder.decode
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.functions.{col, udf}
import org.apache.spark.sql.{DataFrame, SparkSession}

case class LibsvmDecoderArgs(input: String, featuresCol: String = "features")

class LibsvmDecoder extends UserDefinedCmd[LibsvmDecoderArgs] {

  override def apply(spark: SparkSession, args: LibsvmDecoderArgs): DataFrame = {
    val df = spark.table(args.input)
    decode(df, args.featuresCol)
  }

}

object LibsvmDecoder {

  def decode(df: DataFrame, featuresCol: String): DataFrame = {
    val columns = df.columns.filter(col => col.startsWith(featuresCol + "_"))
    assert(columns.length == 1)
    val features = columns.last
    val size = features.split("_").last.toInt
    val decoderUdf = udf { vec: String =>
      val (indices, values) = vec.split(" ").filter(_.nonEmpty).map { item =>
        val indexAndValue = item.split(':')
        val index = indexAndValue(0).toInt - 1 // Convert 1-based indices to 0-based.
        val value = indexAndValue(1).toDouble
        (index, value)
      }.unzip
      Vectors.sparse(size, indices, values).compressed
    }
    df.withColumn(featuresCol, decoderUdf(col(features))).drop(features)
  }
}