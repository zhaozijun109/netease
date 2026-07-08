package com.netease.easyudf.cmd

import com.criteo.rsvd._
import com.netease.easyml.common.cmds.UserDefinedCmd
import com.netease.easyml.common.util.ConvertUtil
import org.apache.spark.mllib.linalg.distributed.MatrixEntry
import org.apache.spark.sql.{DataFrame, SparkSession}

import java.util

class Rsvd extends UserDefinedCmd[RSVDConfig] {
  var input: String = ""

  override def apply(spark: SparkSession, params: util.Map[String, String]): DataFrame = {
    input = params.remove("input")
    val args = parseArgs(params)
    println(s"Args: ${ConvertUtil.toJson(args)}")
    apply(spark, args = args)
  }

  override def apply(spark: SparkSession, args: RSVDConfig): DataFrame = {
    val matrix = spark.table(input).rdd.map(row => {
      val r = ConvertUtil.toLong(row.get(0))
      val c = ConvertUtil.toLong(row.get(1))
      val v = ConvertUtil.toDouble(row.get(2))
      MatrixEntry(r, c, v)
    })

    val matHeight = matrix.map(_.i).max() + 1
    val matWidth = matrix.map(_.j).max() + 1

    val matrixToDecompose = BlockMatrix.fromMatrixEntries(matrix,
      matHeight = matHeight,
      matWidth = matWidth,
      args.blockSize,
      args.partitionHeightInBlocks,
      args.partitionWidthInBlocks)

    val RsvdResults(leftSingularVectors, _, rightSingularVectors) =
      RSVD.run(matrixToDecompose, args, spark.sparkContext)

    var df: DataFrame = null
    if (args.computeLeftSingularVectors) {
      df = toDataFrame(spark, leftSingularVectors.get)
    }

    if (args.computeRightSingularVectors) {
      val right = toDataFrame(spark, rightSingularVectors.get)
      if (df == null) {
        df = right
      } else {
        df = df.alias("t1").join(right.alias("t2"), "i")
          .selectExpr("t1.i", "t1.vector as left", "t2.vector as right")
      }
    }
    df
  }

  def toDataFrame(spark: SparkSession, matrix: SkinnyBlockMatrix): DataFrame = {
    import spark.implicits._
    matrix.toIndexedEmbeddings.map(it => (it._1, it._2.toArray)).toDF("i", "vector")
  }
}



