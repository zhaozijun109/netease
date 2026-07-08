package com.netease.easyml.uds.examples;

import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.SparkUtil
import com.netease.easyml.uds.util.Constant._
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import org.apache.spark.storage.StorageLevel

/**
 * Created by linjiuning on 2021/10/25.
 * Swing algorithm use join version.
 * with restrict u->i->i, for session-based swing
 * <p>
 * data schema:
 * [input]
 * action table: user: String, item: String
 * target item table: item: String
 * restrict table: user: String, item_src: String, item_dst: String
 * [output] main_item: String, simi_item: String, score: Double
 * <p>
 * params:
 * actionTable: distinct action table
 * targetTable: distinct target table [optional]
 * restrictTable: distinct restrict table [optional]
 * output: output table
 * userCol: col name of user, default = user
 * itemCol: col name of item, default = item
 * alpha: hyper-parameter, default = 1.0
 * power: hyper-parameter, default = -0.35
 * eps: hyper-parameter, default = 5
 * k: top k
 * mode: index, swing or all
 */

case class SwingArgs(actionTable: String, output: String,
                     targetTable: String = NULL, restrictTable: String = NULL,
                     mode: String = "all", userCol: String = USER, itemCol: String = ITEM,
                     alpha: Double = 1.0, power: Double = -0.35, eps: Double = 5, k: Int = 0,
                     persist: Boolean = true)

object SwingUDS extends UDS[SwingArgs] {
  val columns: Array[String] = Array("main_item", "simi_item", "score")

  def runIndexer(spark: SparkSession, args: Args): Unit = {
    var uiDf = spark.sql(s"select ${args.userCol}, ${args.itemCol} from ${args.actionTable}")

    val window = Window.partitionBy(TYPE).orderBy(WORD)

    val vocabDf = uiDf.select(lit("u").alias(TYPE), col(args.userCol).alias(WORD))
      .union(uiDf.select(lit("i").alias(TYPE), col(args.itemCol).alias(WORD)))
      .distinct()
      .select(col("*"), (row_number().over(window) - lit(1)).alias(ID))

    val uVocabDf = vocabDf.filter(col(TYPE) === lit("u"))
    var iVocabDf = vocabDf.filter(col(TYPE) === lit("i"))
    uiDf = uiDf.alias("t1")
      .join(uVocabDf.alias("t2"), uiDf(args.userCol) === vocabDf(WORD))
      .select(col(s"t2.$ID").alias("u"), col(s"t1.${args.itemCol}")).alias("t1")
      .join(iVocabDf.alias("t2"), uiDf(args.itemCol) === vocabDf(WORD))
      .select(col("t1.u"), col(s"t2.$ID").alias("i"))

    SparkUtil.saveAsTable(uiDf, s"${args.output}_action")

    if (!args.restrictTable.equals(NULL)) {
      var restrictDf = spark.sql(s"select ${args.userCol}, ${args.itemCol}_src, ${args.itemCol}_dst from ${args.restrictTable}")
      restrictDf = restrictDf.alias("t1")
        .join(uVocabDf.alias("t2"), restrictDf(args.userCol) === vocabDf(WORD))
        .select(col(s"t2.$ID").alias("u"), col(s"t1.${args.itemCol}_src"), col(s"t1.${args.itemCol}_dst"))
        .alias("t1")
        .join(iVocabDf.alias("t2"), col(s"t1.${args.itemCol}_src") === iVocabDf(WORD))
        .select(col("t1.*"), col(s"t2.$ID").alias("i1"))
        .alias("t1")
        .join(iVocabDf.alias("t2"), col(s"t1.${args.itemCol}_dst") === iVocabDf(WORD))
        .select(col("t1.u"), col("t1.i1"), col(s"t2.$ID").alias("i2"))
      SparkUtil.saveAsTable(restrictDf, s"${args.output}_restrict")
    }

    // restrict simi item in target table
    if (!args.targetTable.equals(NULL)) {
      var targetDf = spark.sql(s"select ${args.itemCol} from ${args.targetTable}")
      targetDf = targetDf.alias("t1")
        .join(iVocabDf.alias("t2"), targetDf(args.itemCol) === iVocabDf(WORD))
        .select(col(s"t2.$ID").alias("i"))
      SparkUtil.saveAsTable(targetDf, s"${args.output}_target")
    }

    iVocabDf = iVocabDf.select(col(WORD).alias("item_id"), col(ID).alias("i"))
    SparkUtil.saveAsTable(iVocabDf, s"${args.output}_vocab")
  }

  def runSwing(spark: SparkSession, args: Args): Unit = {
    val output = SparkUtil.tableName(args.output)
    val uiDf = spark.sql(s"select u, i from ${output}_action")

    val wU = uiDf.groupBy("u")
      .agg(pow(count("i") + lit(args.eps), args.power).alias("score"))

    val iuuDf = uiDf.alias("t1").join(uiDf.alias("t2"), "i")
      .where("t1.u < t2.u")
      .select(col("i"), col("t1.u").alias("u1"), col("t2.u").alias("u2"))

    val uuCommonDf = iuuDf.groupBy("u1", "u2")
      .agg(count("i").alias("inter"))

    // restrict simi item in target table
    var iuuTDf = iuuDf
    if (!args.targetTable.equals(NULL)) {
      val targetDf = spark.sql(s"select i from ${output}_target")

      iuuTDf = iuuDf.alias("t1").join(targetDf.alias("t2"), "i")
        .select(col("t1.*"))
    }

    var uuiiDf = iuuDf.alias("t1").join(iuuTDf.alias("t2"), Array("u1", "u2"))
      .where("t1.i != t2.i")
      .select(col("u1"), col("u2"), col("t1.i").alias("i1"), col("t2.i").alias("i2"))

    if (!args.restrictTable.equals(NULL)) {
      var restrictDf = spark.sql(s"select * from ${args.output}_restrict")
      restrictDf = restrictDf.alias("t1").join(restrictDf.alias("t2"), Array("i1", "i2"))
        .where("t1.u < t2.u")
        .select(col("t1.u").alias("u1"), col("t2.u").alias("u2"), col("i1"), col("i2"))

      uuiiDf = uuiiDf.alias("t1")
        .join(restrictDf.alias("t2"), Array("u1", "u2", "i1", "i2"))
        .select(col("t1.*"))
    }

    val uuDf = uuCommonDf.alias("t1")
      .join(wU.alias("t2"), col("t1.u1") === col("t2.u"))
      .select("t1.*", "t2.score")
      .alias("t1")
      .join(wU.alias("t2"), col("t1.u2") === col("t2.u"))
      .select(col("t1.u1"), col("t1.u2"),
        (col("t2.score") * col("t1.score") / (lit(args.alpha) + col("t1.inter"))).alias("score"))

    val iiDf = uuiiDf.alias("t1").join(uuDf.alias("t2"), Array("u1", "u2"))
      .select("t1.i1", "t1.i2", "t2.score")
      .groupBy("i1", "i2")
      .agg(sum("score").alias("score"))

    if (args.persist) {
      iiDf.persist(StorageLevel.MEMORY_AND_DISK)
      iiDf.count()
    }

    val topKDf = if (args.k > 0) {
      val window = Window.partitionBy("i1").orderBy(col("score").desc)
      iiDf.select(col("*"), row_number().over(window).alias("rank"))
        .filter(col("rank") <= args.k)
        .drop("rank")
    }
    else {
      iiDf
    }

    val vocabDf = spark.sql(s"select * from ${output}_vocab")

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
    } else if (args.mode.equals("swing")) {
      runSwing(spark, args)
    } else {
      runIndexer(spark, args)
      runSwing(spark, args)
    }
  }
}
