package com.netease.easyml.uds.examples

import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.{IOUtil, SparkUtil}
import com.netease.easyml.uds.util.RankUtil._
import org.apache.spark.ml.feature.QuantileDiscretizer
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{FloatType, StructField, StructType}
import org.apache.spark.storage.StorageLevel

import scala.collection.JavaConverters._

/**
 * Created by linjiuning on 2021/3/9.
 * Count String to index Vocabulary
 *
 * params:
 * input: input table
 * output: output dir
 * config: feature config file
 */
case class RankDiscretizeArgs(input: String, output: String, config: String,
                              numBucket: Int = NUM_BUCKETS, startDay: String = "", endDay: String = "",
                              saveAsFile: Boolean = false, isFea: Boolean = true, recordType: String = EXAMPLE)

object RankDiscretizeUDS extends UDS[RankDiscretizeArgs] {
  val TOLERANCE = 1e-10
  val MAX_VALUE = 1000000

  def run(spark: SparkSession, args: RankDiscretizeArgs): Unit = {
    var configs = readConfig(args.config, numBucket = args.numBucket, nameAsEmbedName = true)
      .filter(it => it.featureColumn.contains(BUCKET))

    if (args.isFea) {
      configs = configs.filter(_.isFea)
    }

    val keys = configs.map(_.name).mkString(",")
    val isTfrecord = IOUtil.isDirectory(args.input)
    val df = if (isTfrecord) {
      loadTfRecords(spark, args.input, keys = keys, startDay = args.startDay, endDay = args.endDay, recordType = args.recordType, source = "tfrecordv2")
    } else {
      SparkUtil.loadFromTable(spark, args.input, keys, startDay = args.startDay, endDay = args.endDay)
    }

    val groups = configs.groupBy(_.embedName)
    var counter = 0
    val counts = groups.mapValues(it => if (it.count(f => f.format.equals("list_float")) > 0) {
      counter -= 1
      counter
    } else it.length)
      .toArray.map(it => (it._2, it._1)).groupBy(_._1).mapValues(_.map(_._2))

    if (counts.size > 1) {
      df.persist(StorageLevel.MEMORY_AND_DISK)
    }

    val bounds = counts.toArray.flatMap { case (colCnt, embedNames) =>
      val newDf = if (colCnt > 0) {
        val names = embedNames.map(bdName => groups(bdName).map(_.name))
        val newRdd = spark.sparkContext.union(
          (0 until colCnt).map(j => names.indices.map(i => col(names(i)(j))))
            .map(cols => df.select(cols: _*).rdd)
        )
        val schema = StructType(embedNames.map(df.schema(_)))
        spark.createDataFrame(newRdd, schema)
      } else {
        val embedName = embedNames(0)
        val configs = groups(embedName)
        val newRdd = spark.sparkContext.union(
          configs.map(it => if (it.format.equals("list_float")) explode(col(it.name)) else col(it.name))
            .map(col => df.select(col))
            .map(df => df.withColumn(df.columns(0), col(df.columns(0)).cast("float")).rdd)
        )
        val schema = StructType(Array(StructField(embedName, FloatType)))
        spark.createDataFrame(newRdd, schema)
      }

      val configs = embedNames.map(bdName => groups(bdName).head)
      val buckets = configs.map(_.numBucket)

      if (buckets.distinct.length > 1) {
        newDf.persist(StorageLevel.MEMORY_AND_DISK)
      }

      buckets.zip(configs).groupBy(_._1).flatMap(it => {
        val bucket = it._1
        val configs = it._2.map(_._2)
        val names = configs.map(_.embedName)

        val outputCols = names.map(_ + "_buckets")

        val bucketizer = new QuantileDiscretizer()
          .setNumBuckets(bucket)
          .setInputCols(names)
          .setOutputCols(outputCols)
          .setHandleInvalid("skip")
          .fit(newDf)

        names.zip(bucketizer.getSplitsArray).map {
          case (key, splits) =>
            val newSplits = if (splits.isEmpty) {
              Array(TOLERANCE, MAX_VALUE)
            } else if (splits(0) < TOLERANCE) {
              TOLERANCE +: splits.slice(1, splits.length)
            } else {
              TOLERANCE +: splits
            }

            if (newSplits(newSplits.length - 2) < MAX_VALUE) {
              newSplits(newSplits.length - 1) = MAX_VALUE
            }
            (key, newSplits.sorted)
        }
      })
    }

    if (IOUtil.exists(args.output)) {
      IOUtil.delete(args.output)
    }

    val results = bounds.map(it => it._1 + "=" + it._2.mkString(","))

    results.foreach(println)
    if (args.saveAsFile) {
      IOUtil.writeLines(args.output, results.toList.asJava)
    } else {
      spark.sparkContext.parallelize(results, 1).saveAsTextFile(args.output)
    }
  }
}
