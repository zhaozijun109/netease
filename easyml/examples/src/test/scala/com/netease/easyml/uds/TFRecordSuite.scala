package com.netease.easyml.uds

import com.holdenkarau.spark.testing.SharedSparkContext
import com.netease.easyml.common.util.SparkUtil
import com.netease.easyml.uds.examples.{TFRecordArgs, TFRecordUDS}
import org.apache.spark.sql.catalyst.expressions.GenericRow
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Row, SaveMode, SparkSession}
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2020/12/25.
 */
class TFRecordSuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  val input = "input"
  val outputPath = "target/tmp/tfrecord_gzip"
  val output = "output"

  val inputCols = "null"
  val recordType = "Example"
  val gzip = true

  def dataset(): DataFrame = {
    val testRows: Array[Row] = Array(
      new GenericRow(Array[Any](11, 1, 23L, 10.0F, 14.0, List(1.0, 2.0), "r1")),
      new GenericRow(Array[Any](21, 2, 24L, 12.0F, 15.0, List(2.0, 2.0), "r2")))
    val schema = StructType(List(StructField("id", IntegerType),
      StructField("IntegerCol", IntegerType),
      StructField("LongCol", LongType),
      StructField("FloatCol", FloatType),
      StructField("DoubleCol", DoubleType),
      StructField("ArrayCol", ArrayType(DoubleType, true)),
      StructField("StringCol", StringType)))

    val rdd = spark.sparkContext.parallelize(testRows)

    spark.createDataFrame(rdd, schema)
  }

  test("write and read") {
    val training = dataset()
    training.show(false)
    training.createOrReplaceTempView(input)
    var args = TFRecordArgs(input = input, output = outputPath, inputCols = inputCols, recordType = recordType, gzip = gzip, partitionBy = "id,StringCol")
    TFRecordUDS.run(spark, args)

    args = TFRecordArgs(input = outputPath, output = output, inputCols = inputCols, recordType = recordType, gzip = gzip)
    TFRecordUDS.run(spark, args)
  }

  test("convert"){
//    val df = SparkUtil.loadFromTfRecord(spark, "/Users/linjiuning/workspace/git/netease/fastrec/tutorial/toy_dataset/estimator/rank/toy/tfrecords/train")
//    df.write.option("compression","uncompressed").mode(SaveMode.Overwrite).parquet("/Users/linjiuning/workspace/git/netease/fastrec/tutorial/toy_dataset/estimator/rank/toy/parquet/train")
    val df = spark.read.text("/Users/linjiuning/workspace/git/netease/fastrec/tutorial/toy_dataset/estimator/rank/toy/res_dir/user_id.txt").show()
  }
}
