package com.netease.easyml.uds.examples

import com.alibaba.fastjson.JSON
import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.SparkUtil
import org.apache.spark.internal.Logging
import org.apache.spark.sql.SparkSession

import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks

/**
 * Created by linjiuning on 2021/6/23.
 * Write nlp_zoo ann json result to hive
 *
 * data schema:
 * [input] json text, {"src": xx, "topn": [{"dst": xx, "score": xx}]}
 * [output]
 * if kv:
 * src: String, labels: Array[String]
 * else:
 * src: String, labels: Array[String], scores: Array[Float]
 *
 * params:
 * input: input hdfs
 * output: output table
 * k: top k
 * threshold: threshold of distance
 * kv: whether use kv format
 */
case class ANNJsonToHiveArgs(input: String, output: String, k: Int = Int.MaxValue, threshold: Float = 0, kv: Boolean = true)

object ANNJsonToHiveUDS extends UDS[ANNJsonToHiveArgs] with Logging {

  def run(spark: SparkSession, args: Args): Unit = {
    val rdd = spark.sparkContext.textFile(args.input)
      .filter(it => it.nonEmpty)
      .map(it => {
        val obj = JSON.parseObject(it)
        val src = obj.getString("src")
        val topn = obj.getJSONArray("topn")
        val labels = ArrayBuffer.empty[String]
        val distances = ArrayBuffer.empty[Float]
        val loop = new Breaks
        loop.breakable {
          for (i <- 0 until Math.min(args.k, topn.size)) {
            val obj = topn.getJSONObject(i)
            val score = obj.getFloat("score")
            if (score < args.threshold) {
              loop.break
            }
            labels.append(obj.getString("dst"))
            distances.append(score)
          }
        }
        (src, labels.toArray, distances.toArray)
      }).filter(it => it._1.nonEmpty && it._2.nonEmpty)

    import spark.implicits._
    val df = if (args.kv) {
      rdd.map(it => (it._1, it._2.zip(it._3).map(it => it._1 + ":" + it._2)))
        .toDF("src", "labels")
    } else {
      rdd.toDF("src", "labels", "scores")
    }

    SparkUtil.saveAsTable(df, args.output)
  }
}
