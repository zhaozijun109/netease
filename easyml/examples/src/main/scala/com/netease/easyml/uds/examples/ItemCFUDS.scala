package com.netease.easyml.uds.examples

import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.SparkUtil
import com.netease.easyml.uds.util.Constant._
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.storage.StorageLevel;

/**
 * Created by linjiuning on 2020/12/4.
 * ItemCF algorithm.
 * optimize runIndexer with large id's size
 * <p>
 * data schema:
 * [input]
 * action table: user: String, item: String
 * target item table: item: String
 * [output] main_item: String, simi_item: String, score: Double
 * <p>
 * params:
 * actionTable: distinct action table
 * targetTable: distinct target table [optional]
 * output: output table
 * userCol: col name of user, default = user
 * itemCol: col name of item, default = item
 * alpha: hyper-parameter, default = 0.65
 * minSameUser: hyper-parameter, default = 5
 * k: top k
 * mode: index, itemcf or all
 */
case class ItemCFArgs(actionTable: String, targetTable: String = NULL, output: String, userCol: String = USER, itemCol: String = ITEM,
                      alpha: Double = 0.65, minSameUser: Int = 5, k: Int = 0, mode: String = "all", persist: Boolean = true)

object ItemCFUDS extends UDS[ItemCFArgs] {
  val columns: Array[String] = Array("main_item", "simi_item", "score")
  val TYPE: String = "type"
  val WORD: String = "word"
  val ID: String = "id"
  val FLAG: String = "flag"

  def runIndexer(spark: SparkSession, args: Args): Unit = {
    var uiDf = spark.sql(s"select ${args.userCol}, ${args.itemCol}, 0 as $FLAG from ${args.actionTable}")

    // restrict simi item in target table
    // ensure target item_id's id is bigger
    var targetDf: DataFrame = null
    if (!args.targetTable.equals(NULL)) {
      targetDf = spark.sql(s"select ${args.itemCol} from ${args.targetTable}")

      uiDf = uiDf.alias("t1").join(targetDf.alias("t2"), col(s"t1.${args.itemCol}") === col(s"t2.${args.itemCol}"), "left")
        .select(col(s"t1.${args.userCol}"), col(s"t1.${args.itemCol}"),
          when(col(s"t2.${args.itemCol}").isNull, 0).otherwise(1).alias(FLAG))
    }

    val window = Window.partitionBy(TYPE).orderBy(FLAG)

    val vocabDf = uiDf.select(lit("u").alias(TYPE), col(args.userCol).alias(WORD), col(FLAG))
      .union(uiDf.select(lit("i").alias(TYPE), col(args.itemCol).alias(WORD), col(FLAG)))
      .groupBy(TYPE, WORD)
      .agg(max(FLAG).alias(FLAG))
      .select(col(TYPE), col(WORD), (row_number().over(window) - lit(1)).alias(ID))

    val uVocabDf = vocabDf.filter(col(TYPE) === lit("u"))
    var iVocabDf = vocabDf.filter(col(TYPE) === lit("i"))
    uiDf = uiDf.alias("t1")
      .join(uVocabDf.alias("t2"), uiDf(args.userCol) === vocabDf(WORD))
      .select(col(s"t2.$ID").alias("u"), col(s"t1.${args.itemCol}")).alias("t1")
      .join(iVocabDf.alias("t2"), uiDf(args.itemCol) === vocabDf(WORD))
      .select(col("t1.u"), col(s"t2.$ID").alias("i"))

    SparkUtil.saveAsTable(uiDf, s"${args.output}_action")

    // restrict simi item in target table
    if (!args.targetTable.equals(NULL)) {
      targetDf = targetDf.alias("t1")
        .join(iVocabDf.alias("t2"), targetDf(args.itemCol) === iVocabDf(WORD))
        .select(col(s"t2.$ID").alias("i"))
      SparkUtil.saveAsTable(targetDf, s"${args.output}_target")
    }

    iVocabDf = iVocabDf.select(col(WORD).alias("item_id"), col(ID).alias("i"))
    SparkUtil.saveAsTable(iVocabDf, s"${args.output}_vocab")
  }

  def runItemCF(spark: SparkSession, args: Args): Unit = {
    def cooccurrence(numOfRatersForAAndB: Double, numOfRatersForA: Double, numOfRatersForB: Double): Double = {
      if (numOfRatersForA > numOfRatersForB)
        numOfRatersForAAndB / (math.pow(numOfRatersForA, args.alpha) * math.pow(numOfRatersForB, 1 - args.alpha))
      else
        numOfRatersForAAndB / (math.pow(numOfRatersForA, 1 - args.alpha) * math.pow(numOfRatersForB, args.alpha))
    }

    val cooccurrenceUdf = udf(cooccurrence _)

    val uiDf = spark.sql(s"select u, i from ${args.output}_action")

    val wI = uiDf.groupBy("i")
      .agg(count("u").alias("score"))

    // restrict simi item in target table
    var uiTDf = uiDf
    var targetDf: DataFrame = null
    // ensure target item_id's id is bigger
    if (!args.targetTable.equals(NULL)) {
      targetDf = spark.sql(s"select i from ${args.output}_target")

      uiTDf = uiTDf.alias("t1").join(targetDf.alias("t2"), "i")
        .select(col("t1.*"))
    }

    val uiiDf = uiDf.alias("t1").join(uiTDf.alias("t2"), "u")
      .where("t1.i < t2.i")
      .select(col("u"), col("t1.i").alias("i1"), col("t2.i").alias("i2"))

    var iiCommonDf = uiiDf.groupBy("i1", "i2")
      .agg(count("u").alias("inter"))

    if (args.minSameUser > 0) {
      iiCommonDf = iiCommonDf.filter(col("inter") >= lit(args.minSameUser))
    }

    var iiDf = iiCommonDf.alias("t1")
      .join(wI.alias("t2"), col("t1.i1") === col("t2.i"))
      .select("t1.*", "t2.score")
      .alias("t1")
      .join(wI.alias("t2"), col("t1.i2") === col("t2.i"))
      .select(col("t1.i1"), col("t1.i2"),
        cooccurrenceUdf(col("t1.inter"), col("t1.score"), col("t2.score")).alias("score"))

    if (args.persist) {
      iiDf.persist(StorageLevel.MEMORY_AND_DISK)
      iiDf.count()
    }

    val iiTDf = iiDf.select(col("i2").alias("i1"), col("i1").alias("i2"), col("score"))
    iiDf = iiDf.union(iiTDf)
    if (!args.targetTable.equals(NULL)) {
      iiDf = iiDf.alias("t1").join(targetDf.alias("t2"), col("t1.i2") === col("t2.i"))
        .select(col("t1.*"))
    }

    val topKDf = if (args.k > 0) {
      val window = Window.partitionBy("i1").orderBy(col("score").desc)
      iiDf.select(col("*"), row_number().over(window).alias("rank"))
        .filter(col("rank") <= args.k)
        .drop("rank")
    } else {
      iiDf
    }

    val vocabDf = spark.sql(s"select * from ${args.output}_vocab")

    val finalDf = topKDf.alias("t1").join(vocabDf.alias("t2"), col("t1.i1") === col("t2.i"))
      .join(vocabDf.alias("t3"), col("t1.i2") === col("t3.i"))
      .selectExpr(s"t2.item_id as ${columns(0)}",
        s"t3.item_id as ${columns(1)}",
        s"score as ${columns(2)}")

    if (SparkUtil.isLocalMaster(spark.sparkContext.getConf)) {
      finalDf.show(false)
    } else {
      SparkUtil.saveAsTable(finalDf, args.output)
    }
  }

  def run(spark: SparkSession, args: Args): Unit = {
    if (args.mode.equals("index")) {
      runIndexer(spark, args)
    } else if (args.mode.equals("itemcf")) {
      runItemCF(spark, args)
    } else {
      runIndexer(spark, args)
      runItemCF(spark, args)
    }
  }
}
