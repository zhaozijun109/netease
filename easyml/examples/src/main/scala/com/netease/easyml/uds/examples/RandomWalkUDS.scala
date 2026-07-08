package com.netease.easyml.uds.examples

import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.{IOUtil, SparkUtil}
import com.netease.easyml.graphx.RandomWalk
import com.netease.easyml.uds.util.Constant.PARQUET
import org.apache.spark.sql.SparkSession

/**
 * Created by linjiuning on 2020/10/15.
 * Random walk of node2vec
 * <p>
 * data schema:
 * [input] src: String, dst: String, weight: Double (optional)
 * [output] path: Seq[String]
 * <p>
 * params:
 * input: input table
 * output: output table/path
 * walkLength: walk length
 * numWalks: num walk epochs
 * p: return parameter p
 * q: in-out parameter q
 * format: hive table stored format
 */
case class RandomWalkArgs(input: String, output: String, walkLength: Int = 80, numWalks: Int = 10,
                          p: Double = 1.0, q: Double = 1.0, format: String = PARQUET)

object RandomWalkUDS extends UDS[RandomWalkArgs] {

  def run(spark: SparkSession, args: Args): Unit = {
    val df = spark.sql(s"select * from ${args.input}")

    val conf = spark.sparkContext.getConf
    val numPartitions = SparkUtil.getDefaultParallelism(conf)
    logInfo(s"Set numPartitions = $numPartitions")

    val randomWalk = new RandomWalk()
      .setWalkLength(args.walkLength)
      .setNumWalks(args.numWalks)
      .setP(args.p)
      .setQ(args.q)
      .setNumPartition(numPartitions)

    val pathDf = randomWalk.transform(df)

    if (SparkUtil.isLocalMaster(conf) || IOUtil.isHdfs(args.output)) {
      val rdd = pathDf.select(randomWalk.getOutputCol)
        .rdd.map(row => {
        row.getSeq[String](0).mkString(" ")
      })
      rdd.saveAsTextFile(args.output)
    } else {
      SparkUtil.saveAsTable(pathDf, args.output, args.format)
    }
  }
}
