package com.netease.easyml.uds.examples

import com.netease.easyml.common.uds.UDS
import com.netease.easyml.ml.util.MLUtils
import org.apache.spark.sql.SparkSession
import org.apache.spark.storage.StorageLevel;

/**
 * Created by linjiuning on 2021/2/23.
 * Write gensim WordVec format to hive table
 * <p>
 * data schema:
 * [output] word: String, vector: Seq[Float]
 * <p>
 * params:
 * input: input hdfs path
 * output: output table
 * vectorSize: embedding size
 */
case class WordVecToHiveArgs(input: String, output: String, vectorSize: Int = 0)

object WordVecToHiveUDS extends UDS[WordVecToHiveArgs] {

  def run(spark: SparkSession, args: WordVecToHiveArgs): Unit = {
    val rdd = spark.sparkContext.textFile(args.input)
      .map(line => line.split(" "))
      .filter(_.length > 1)

    val size = if (args.vectorSize < 1) {
      rdd.persist(StorageLevel.MEMORY_AND_DISK)
      rdd.map(_.length - 1).map((_, 1L))
        .reduceByKey(_ + _)
        .collect()
        .maxBy(_._2)
        ._1
    } else {
      args.vectorSize
    }

    println(s"Vector size = $size")

    val w2v = rdd.filter(_.length > size)
      .map(it => {
        val word = it.slice(0, it.length - size).mkString(" ")
        val vector = it.slice(it.length - size, it.length).map(_.toFloat)
        (word, vector)
      })

    MLUtils.saveWordVecToHive(spark, args.output, w2v)
  }
}
