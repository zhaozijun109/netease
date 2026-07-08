package com.netease.easyml.ml.transform

import java.util.Random

import com.holdenkarau.spark.testing.SharedSparkContext
import org.apache.spark.sql.SparkSession
import org.faiss.IndexFlat
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2020/11/9.
 */
class FaissSearcherSuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()
  val indexPath = "target/faiss.index"

  test("local") {
    val d = 64 // dimension
    val nb = 100000 // database size
    val nq = 10000 // nb of queries

    val xb = new Array[Float](d * nb)
    val xq = new Array[Float](d * nq)

    val rng = new Random(1024)

    for (i <- 0 until nb) {
      for (j <- 0 until d) {
        xb(d * i + j) = rng.nextFloat
      }
      xb(d * i) += i / 1000.0f
    }

    for (i <- 0 until nq) {
      for (j <- 0 until d) {
        xq(d * i + j) = rng.nextFloat
      }
      xq(d * i) += i / 1000.0f
    }

    val index = new IndexFlat(d)

    index.add(nb, xb)

    index.writeIndex(indexPath)
  }

  test("spark") {
    import spark.implicits._
    val d = 64 // dimension
    val nb = 100000 // database size
    val nq = 10000 // nb of queries

    val rng = new Random(1024)

    val xqs = (0 until nq).map(i => {
      val xq = new Array[Double](d)
      for (j <- 0 until d) {
        xq(j) = rng.nextFloat
      }
      xq(0) += i / 1000.0f
      xq
    })

    val df = spark.sparkContext.parallelize(xqs).toDF("features")

    val dummyVocab = (0 until nb).map(it => "v:" + it.toString).toArray

    var newDf = new FaissSearcher()
      .setPath(indexPath)
      .setVocabs(dummyVocab)
      .setK(5)
      .transform(df)
    newDf.drop("features").show(false)

    newDf = new FaissSearcher()
      .setPath(indexPath)
      .setVocabs(dummyVocab)
      .setK(5)
      .setThreshold(0.0f)
      .transform(df)
    newDf.drop("features").show(false)
  }
}
