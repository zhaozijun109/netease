package com.netease.easyml.ml.feature

import com.holdenkarau.spark.testing.SharedSparkContext
import org.apache.spark.ml.feature_.{IndexToString, StringIndexer, StringIndexerModel}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2020/12/22.
 */
class StringIndexerSuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  def makeDf(): DataFrame = {
    import spark.implicits._
    sc.makeRDD(Seq(
      ("a", Array("aa", "bb")),
      ("a", Array("b", "cc")),
      (null, Array("ab", "cc")),
      ("d", null)
    )).toDF("str", "array")
  }

  test("stringIndexer") {
    val df = makeDf()

    val indexer = new StringIndexer()
      .setInputCols(df.columns)
      .setOutputCols(df.columns.map("map_" + _))
      .setOutputType("int")
      .setStartOffset(1)
      .setHandleInvalid("skip")
      .setMinCount(2)

    val model = indexer.fit(df)
    var newDf = model.transform(df)

    newDf = new IndexToString()
      .setLabels(model.labelsArray(0))
      .setInputCol("map_str")
      .setOutputCol("rev_map_str")
      .setStartOffset(model.getStartOffset)
      .transform(newDf)

    newDf = new IndexToString()
      .setLabels(model.labelsArray(1))
      .setInputCol("map_array")
      .setOutputCol("rev_map_array")
      .setStartOffset(model.getStartOffset)
      .transform(newDf)
    newDf.show(false)

    model.write.overwrite().save("target/tmp/strIdx")

    StringIndexerModel.load("target/tmp/strIdx").transform(df).show(false)
  }
}