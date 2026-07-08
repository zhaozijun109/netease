package com.netease.easyml.uds.examples

import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.SparkUtil
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import org.apache.spark.storage.StorageLevel

/**
 * Created by linjiuning on 2021/7/26.
 */
case class RecallAtKArgs(prediction: String, truth: String, output: String, k1: String, k2: Int = 0)

object RecallAtKUDS extends UDS[RecallAtKArgs] {
  val RID = "rid"
  val USER_ID = "user_id"
  val SCORE = "score"
  val ITEM_ID = "item_id"
  val RANK = "rank"
  val K = "k"
  val SIZE = "size"
  val INTER = "inter"

  def run(spark: SparkSession, args: Args): Unit = {
    var pDf = SparkUtil.loadFromTable(spark, args.prediction, s"$RID,$USER_ID,$ITEM_ID,$SCORE")
    var tDf = SparkUtil.loadFromTable(spark, args.truth, s"$RID,$USER_ID,$ITEM_ID,$SCORE")

    val partition = Window.partitionBy(RID, USER_ID)
    val order = partition.orderBy(col(SCORE).desc)
    pDf = pDf.select(col("*"), row_number().over(order).alias(RANK))
    tDf = tDf.select(col("*"), row_number().over(order).alias(RANK), sum(lit(1)).over(partition).alias(SIZE))

    pDf.persist(StorageLevel.MEMORY_AND_DISK)
    tDf.persist(StorageLevel.MEMORY_AND_DISK)

    pDf = args.k1.split(":").map(_.toInt).map(i => pDf.filter(col(RANK) <= i).withColumn(K, lit(i))).reduce(_ union _)
    var keys = Array(RID, USER_ID, ITEM_ID)
    if (args.k2 > 0) {
      tDf = tDf.filter(col(RANK) <= args.k2)
    } else {
      tDf = args.k1.split(":").map(_.toInt).map(i => tDf.filter(col(RANK) <= i).withColumn(K, lit(i))).reduce(_ union _)
      keys = keys :+ K
    }

    val result = pDf.alias("t1").join(tDf.alias("t2"), keys, "left")
      .select(col(s"t1.$K"), when(col(s"t2.$RID").isNotNull, 1).otherwise(0).alias(INTER), col(s"t2.$SIZE"))
      .groupBy(keys.filterNot(_.equals(ITEM_ID)).map(col): _*)
      .agg(sum(INTER).alias(INTER), first(SIZE).alias(SIZE))
      .select(col(K), (col(INTER) / col(SIZE)).alias(SCORE))
      .groupBy(K)
      .avg(SCORE)

    if (SparkUtil.isLocalMaster(spark.sparkContext.getConf)) {
      result.show(false)
    } else {
      SparkUtil.saveAsTable(result, args.output)
    }
  }

}
