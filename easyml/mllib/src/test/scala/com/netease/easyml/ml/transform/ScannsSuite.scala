package com.netease.easyml.ml.transform

import com.holdenkarau.spark.testing.SharedSparkContext
import com.linkedin.nn.algorithm.CosineSignRandomProjectionNNS
import com.netease.easyml.ml.util.Utils
import org.apache.spark.ml.linalg.{Vector, Vectors}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2020/10/30.
 */
class ScannsSuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  test("scanns") {
    val numFeatures = 100
    val numCandidates = 10

    val rng = Utils.random
    val items: RDD[(Long, Vector)] = spark.sparkContext.parallelize((0 until 100).map(i => {
      (i.toLong, Vectors.dense((0 until numFeatures).toArray.map(_ => rng.nextDouble())))
    }))


    val model = new CosineSignRandomProjectionNNS()
      .setNumHashes(30)
      .setSignatureLength(15)
      .setJoinParallelism(5)
      .setBucketLimit(1000)
      .setShouldSampleBuckets(true)
      .setNumOutputPartitions(10)
      .createModel(numFeatures)

    val nbrs: RDD[(Long, Long, Double)] = model.getSelfAllNearestNeighbors(items, numCandidates)
    nbrs.foreach(println)
  }
}
