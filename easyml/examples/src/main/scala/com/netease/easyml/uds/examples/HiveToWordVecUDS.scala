package com.netease.easyml.uds.examples

import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.IOUtil
import org.apache.spark.ml.linalg.Vector
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.size
import org.apache.spark.storage.StorageLevel

import scala.collection.JavaConverters._

/**
 * Created by linjiuning on 2021/6/23.
 * Write hive to gensim WordVec format
 * <p>
 * data schema:
 * [input] word: String, vector: Seq[Numeric]
 * [output] gensim format
 * <p>
 * params:
 * input: input table
 * output: output hdfs path
 * wordCol: word column name
 * vectorCol: vector column name
 * vectorSize: embedding size
 * numPartitions: output partitions
 */
case class HiveToWordVecArgs(input: String, output: String, wordCol: String, vectorCol: String,
                             vectorSize: Int = 0, numPartitions: Int = -1)

object HiveToWordVecUDS extends UDS[HiveToWordVecArgs] {

  def run(spark: SparkSession, args: Args): Unit = {
    val df = spark.sql(s"select * from ${args.input}")

    val vSize = if (args.vectorSize < 1) {
      df.persist(StorageLevel.MEMORY_AND_DISK)
      df.withColumn("size", size(df(args.vectorCol)))
        .toDF().rdd.map(it => (it.getAs[Int]("size"), 1L))
        .reduceByKey(_ + _)
        .collect()
        .maxBy(_._2)
        ._1
    } else {
      args.vectorSize
    }

    println(s"Vector size = $vSize")

    val newDf = df.filter(size(df(args.vectorCol)).equalTo(vSize)).select(args.wordCol, args.vectorCol)

    val sep = " "
    val vecs = newDf.rdd
      .map(row => {
        val word = row.getString(0)
        val vector = row.get(1) match {
          case vector: Vector => vector.toArray.mkString(sep)
          case seq: Any => seq.asInstanceOf[Seq[_]].mkString(sep)
        }
        word + sep + vector
      })

    if (!IOUtil.exists(IOUtil.parentName(args.output))) {
      IOUtil.mkdirs(IOUtil.parentName(args.output))
    } else {
      IOUtil.delete(args.output)
    }
    if (args.numPartitions < 0) {
      vecs.saveAsTextFile(args.output)
    } else if (args.numPartitions > 1) {
      vecs.coalesce(args.numPartitions).saveAsTextFile(args.output)
    } else {
      var lines = vecs.collect()
      lines = Array(s"${lines.length} $vSize") ++ lines
      IOUtil.writeLines(args.output, lines.toList.asJava)
    }
  }
}
