package com.netease.easyml.uds

import com.holdenkarau.spark.testing.SharedSparkContext
import com.netease.easyml.common.util.IOUtil
import com.netease.easyml.uds.examples.{RoBertTFRecordDataArgs, RoBertTFRecordDataUDS}
import org.apache.spark.sql.catalyst.expressions.GenericRow
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.scalatest.FunSuite

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

/**
 * Created by linjiuning on 2021/5/24.
 */
class RoBertTFRecordDataSuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  val input = "input"
  val output = "output"

  val inputPath = "/Users/linjiuning/workspace/git/netease/nlp_zoo/tutorial/toy_dataset/estimator/bert/sample.txt"
  val inputCol = "text"
  val dupeFactor = 10
  val lowerCase = true
  val wholeWordMask = true
  val sentenceSep = "\n"
  val maxSeqLength = 512

  def dataset(): DataFrame = {
    val docs = ArrayBuffer.empty[String]
    val doc = ArrayBuffer.empty[String]
    IOUtil.readLines(inputPath, false).asScala.foreach(text => {
      if (text.isEmpty) {
        if (doc.nonEmpty) {
          docs.append(doc.mkString(sentenceSep))
        }
        doc.clear()
      } else {
        doc.append(text)
      }
    })

    if (doc.nonEmpty) {
      docs.append(doc.mkString(sentenceSep))
    }

    val schema = StructType(List(StructField(inputCol, StringType)))

    val testRows: Array[Row] = docs.toArray.map(it => new GenericRow(Array[Any](it)))
    val rdd = spark.sparkContext.parallelize(testRows)

    spark.createDataFrame(rdd, schema)
  }

  test("transform") {
    val training = dataset()
    training.show(false)
    training.createOrReplaceTempView(input)
    val args = RoBertTFRecordDataArgs(input = input, output = output, inputCol = inputCol, dupeFactor = dupeFactor,
      lowerCase = lowerCase, wholeWordMask = wholeWordMask, sentenceSep = sentenceSep, maxSeqLength = maxSeqLength)
    RoBertTFRecordDataUDS.run(spark, args)
  }
}
