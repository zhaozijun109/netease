package com.netease.easyml.ml.feature

import com.holdenkarau.spark.testing.SharedSparkContext
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2020/10/29.
 */
class VectorizerSuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  def makeDf(): Array[DataFrame] = {
    import spark.implicits._
    val dfNumeric = sc.makeRDD(Seq(
      -2.0, 1.0, -4.0, -1.0
    )).toDF()
    val dfString = sc.makeRDD(Seq(
      "-2.0", "1.0", "-4.0", "-1.0"
    )).toDF()
    val dfArray = sc.makeRDD(Seq(
      Seq(-2.0, 1.0, -4.0, -1.0)
    )).toDF()

    val dfMap = spark.createDataFrame(sc.makeRDD(Seq(Map("a" -> 2, "d" -> 4), Map("c" -> 4)))
      .map(it => Row.fromSeq(Seq(it))), StructType(Seq(StructField("map", MapType(StringType, IntegerType, true)))))

    Array(dfNumeric, dfString, dfArray, dfMap)
  }

  def makeJsonDf(): DataFrame = {
    import spark.implicits._
    val dfJson = sc.makeRDD(Seq(
      "{\"上海市\":\"0.9038461538461539\",\"台州市\":\"0.057692307692307696\",\"宁波市\":\"0.038461538461538464\"}",
      "{\"保定市\":\"0.006944444444444444\",\"天津市\":\"0.2569444444444444\",\"邢台市\":\"0.7361111111111112\"}",
      "{\"吉林市\":\"0.21739130434782608\",\"长春市\":\"0.782608695652174\"}",
      ""
    )).toDF()
    dfJson
  }

  test("transform") {
    val dfs = makeDf()

    for (df <- dfs) {
      val model = new Vectorizer()
        .setInputCol(df.columns(0))
        .setOutputCol("vector")
      //        .setSparse(true)

      val newDf = model.fit(df).transform(df)
      newDf.show(false)
    }

    val jsonDf = makeJsonDf()
    val model = new Vectorizer()
      .setInputCol(jsonDf.columns(0))
      .setJson(true)
      .setOutputCol("vector")
    //        .setSparse(true)

    val newDf = model.fit(jsonDf).transform(jsonDf)
    newDf.show(false)

    new VectorizerModel(Array("上海市", "长春市"))
      .setInputCol(jsonDf.columns(0))
      .setOutputCol("vector")
      .setJson(true)
      .transform(jsonDf).show(false)
  }
}