package com.netease.easyml.ml.feature

import com.holdenkarau.spark.testing.SharedSparkContext
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2020/9/16.
 */
class ArrayAssemblerSuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  def makeDf(): Array[DataFrame] = {
    import spark.implicits._
    val df1 = sc.makeRDD(Seq(
      (-2.0, 1.0, -4.0, -1.0),
      (-3.3, 2.0, -3.0, -0.5)
    )).toDF()
    val df2 = sc.makeRDD(Seq(
      ("a", "b", "c"),
      ("d", "e", "f")
    )).toDF()
    Array(df1, df2)
  }

  test("transform") {
    val dfs = makeDf()

    for (df <- dfs) {
      val model = new ArrayAssembler()
        .setInputCols(df.columns)
        .setOutputCol("assembly")

      val newDf = model.transform(df)
      newDf.show(false)
    }
  }
}
