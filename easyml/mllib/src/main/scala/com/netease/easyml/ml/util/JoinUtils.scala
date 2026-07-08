package com.netease.easyml.ml.util

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.{lit, sum, when}

/**
 * Created by linjiuning on 2021/7/9.
 */
object JoinUtils {

  def multiply(a: DataFrame, b: DataFrame, transposeA: Boolean = false, transposeB: Boolean = false): DataFrame = {
    var Array(ai, aj, av) = a.columns.slice(0, 3)
    var Array(bj, bk, bv) = b.columns.slice(0, 3)
    if (transposeA) {
      val t = aj
      aj = ai
      ai = t
    }
    if (transposeB) {
      val t = bk
      bk = bj
      bj = t
    }
    a.alias("a").select(ai, aj, av)
      .join(b.alias("b").select(bj, bk, bv), a(aj) === b(bj))
      .selectExpr(s"a.$ai as $ai", s"b.$bk as $aj", s"a.$av * b.$bv as $av")
      .groupBy(ai, aj)
      .agg(sum(av).alias(av))
  }

  def multiply(a: DataFrame, b: Double): DataFrame = {
    val Array(ai, aj, av) = a.columns.slice(0, 3)
    a.select(ai, aj, av)
      .withColumn(av, a(av) * lit(b))
  }

  def diagMultiply(a: DataFrame, b: Double): DataFrame = {
    val Array(ai, aj, av) = a.columns.slice(0, 3)
    a.select(ai, aj, av)
      .withColumn(av, when(a(ai) === a(aj), a(av) * lit(b)).otherwise(a(av)))
  }

  def add(a: DataFrame, b: DataFrame): DataFrame = {
    val Array(ai, aj, av) = a.columns.slice(0, 3)
    val Array(bi, bj, bv) = b.columns.slice(0, 3)
    a.select(ai, aj, av)
      .join(b.select(bi, bj, bv), a(ai) === b(bi) && a(aj) === b(bj), "outer")
      .select(a(ai), a(aj), (a(av) + b(bv)).alias(av))
  }

  def add(a: DataFrame, b: Double): DataFrame = {
    val Array(ai, aj, av) = a.columns.slice(0, 3)
    a.select(ai, aj, av)
      .withColumn(av, a(av) + lit(b))
  }

  def diagAdd(a: DataFrame, b: Double): DataFrame = {
    val Array(ai, aj, av) = a.columns.slice(0, 3)
    a.select(ai, aj, av)
      .withColumn(av, when(a(ai) === a(aj), a(av) + lit(b)).otherwise(a(av)))
  }
}
