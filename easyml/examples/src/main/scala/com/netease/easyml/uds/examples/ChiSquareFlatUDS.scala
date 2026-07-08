package com.netease.easyml.uds.examples

import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.{ConvertUtil, SparkUtil}
import com.netease.easyml.ml.util.SchemaUtils
import com.netease.easyml.uds.examples.{ChiSquareUDS => ChiSquare}
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.types.{DoubleType, StructField, StructType}
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.storage.StorageLevel

import scala.collection.mutable.ArrayBuffer;

/**
 * Created by linjiuning on 2020/12/29.
 */

object ChiSquareFlatUDS extends UDS[ChiSquare.Args] {

  def groupLabel(labels: Array[String]): Array[String] = {
    val comb = ArrayBuffer.empty[String]
    labels.sorted.foreach(label => {
      for (i <- comb.indices) {
        comb.append(comb(i) + "," + label)
      }
      comb.append(label)
    })
    comb.toArray
  }

  def run(spark: SparkSession, args: ChiSquare.Args): Unit = {
    val dataset = spark.sql(s"select ${args.labelCol}, ${args.inputCol}, ${args.countCol} from ${args.input}")
    val conf = spark.sparkContext.getConf
    val numPartitions = SparkUtil.getDefaultParallelism(conf)
    logInfo(s"Set numPartitions = $numPartitions")

    val schema = dataset.schema
    SchemaUtils.checkStringType(schema, args.labelCol)
    SchemaUtils.checkStringType(schema, args.inputCol)
    SchemaUtils.checkNumericType(schema, args.countCol)
    val structType = StructType(Seq(schema(args.labelCol), schema(args.inputCol), StructField(args.countCol, DoubleType), StructField("score", DoubleType)))

    val rdd = dataset
      .filter(col(args.labelCol).isNotNull &&
        col(args.inputCol).isNotNull &&
        col(args.countCol).isNotNull)
      .select(args.labelCol, args.inputCol, args.countCol).rdd

    val counter = rdd
      .flatMap(row => {
        val count = ConvertUtil.toDouble(row.get(2))
        val labels = groupLabel(row.getString(0).split(","))
        labels.flatMap(label => Array(
          ((label, null), count),
          ((label, row.getString(1)), count)
        )) ++
          Array(
            ((null, row.getString(1)), count)
          )
      }).reduceByKey(_ + _)

    import spark.implicits._

    val rowSumDf = counter.filter(it => it._1._2 == null)
      .map(it => (it._1._1, it._2))
      .toDF("i", "count")

    val colSumDf = counter.filter(it => it._1._1 == null)
      .map(it => (it._1._2, it._2))
      .toDF("j", "count")

    val pairDf = counter.filter(it => it._1._1 != null && it._1._2 != null)
      .map(it => (it._1._1, it._1._2, it._2))
      .toDF("i", "j", "count")

    rowSumDf.persist(StorageLevel.MEMORY_AND_DISK)
    colSumDf.persist(StorageLevel.MEMORY_AND_DISK)
    pairDf.persist(StorageLevel.MEMORY_AND_DISK)

    val total = colSumDf.rdd.map(_.getDouble(1)).sum()

    pairDf.createOrReplaceTempView("pair")
    rowSumDf.createOrReplaceTempView("row")
    colSumDf.createOrReplaceTempView("col")

    spark.sql("select pair.*, row.count as r_c from pair join row on pair.i = row.i")
      .createOrReplaceTempView("pair")

    val joinDf = spark.sql("select pair.*, col.count as c_c from pair join col on pair.j = col.j")

    val resultRdd = joinDf.rdd
      .map {
        case Row(label: String, feature: String, cnt: Double, row: Double, col: Double) =>
          val rc = row * col / total
          var significant = Math.pow(cnt - rc, 2) / rc
          if (args.sign && cnt < rc) {
            significant *= -1
          }
          (label, feature, cnt, significant)
      }.map(it => Row.fromTuple(it))
    val df = spark.createDataFrame(resultRdd, structType)

    SparkUtil.saveAsTable(df, args.output)
  }

}
