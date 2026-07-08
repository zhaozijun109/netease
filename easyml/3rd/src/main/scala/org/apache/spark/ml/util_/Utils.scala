package org.apache.spark.ml.util_

import java.util.regex.Pattern

import org.apache.spark.mllib.feature_.{Word2VecModel => OldWord2VecModel}
import org.apache.spark.network.util.JavaUtils
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
 * Created by linjiuning on 2020/12/10.
 */
object Utils {
  val MAX_SIZE: Long = JavaUtils.byteStringAsBytes("512m")

  def wordVecToRDD(spark: SparkSession, wordVectors: OldWord2VecModel): RDD[(String, Array[Float])] = {
    val numWords = wordVectors.wordIndex.size
    val vectorSize = wordVectors.wordVectors.length / numWords

    val steps = getVectorParallelizeSteps(spark, vectorSize, numWords)
    val numPartitions = getVectorNumPartitions(spark, vectorSize, numWords)
    println(s"NumSteps: $steps, numPartitions: $numPartitions")
    val stepSize = numWords / steps + 1

    var rdd: RDD[(String, Array[Float])] = null
    wordVectors.wordIndex.toSeq.grouped(stepSize)
      .foreach(iter => {
        val seq = iter.map {
          case (word, i) =>
            val vector = wordVectors.wordVectors.slice(i * vectorSize, (i + 1) * vectorSize)
            (word, vector)
        }
        val subRdd = spark.sparkContext.parallelize(seq, numPartitions)

        if (rdd == null) {
          rdd = subRdd
        } else {
          rdd = rdd.union(subRdd)
        }
      })
    rdd.coalesce(numPartitions)
  }

  def wordVecToDF(spark: SparkSession, wordVectors: OldWord2VecModel): DataFrame = {
    import spark.implicits._
    wordVecToRDD(spark, wordVectors)
      .toDF("word", "vector")
  }

  def getVectorNumPartitions(spark: SparkSession, vectorSize: Int, numWords: Int): Int = {
    val bufferSize = JavaUtils.byteStringAsBytes(
      spark.conf.get("spark.kryoserializer.buffer.max", "64m"))
    val approxSize = (4L * vectorSize + 15) * numWords
    ((approxSize / bufferSize) + 1).toInt
  }

  def getVectorParallelizeSteps(spark: SparkSession, vectorSize: Int, numWords: Int): Int = {
    var maxSize = spark.conf.get("spark.rpc.message.maxSize", "128")
    val m = Pattern.compile("[0-9]$").matcher(maxSize)
    if (m.find()) {
      maxSize += "m"
    }
    val bufferSize = Math.min(JavaUtils.byteStringAsBytes(maxSize), MAX_SIZE)
    val approxSize = (4L * vectorSize + 15) * numWords
    ((approxSize / bufferSize) + 1).toInt
  }
}
