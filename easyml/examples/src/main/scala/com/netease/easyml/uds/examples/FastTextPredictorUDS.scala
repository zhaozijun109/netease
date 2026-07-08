package com.netease.easyml.uds.examples

import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.SparkUtil
import com.netease.easyml.ml.feature.HanLPTokenizer
import com.netease.easyml.ml.transform.FastTextPredictor
import com.netease.easyml.ml.util.SchemaUtils
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.sql.{Row, SparkSession};

/**
 * Created by linjiuning on 2020/11/11.
 * Fasttext predictor
 * <p>
 * data schema:
 * [input] text: String or Array[String]
 * [output] prediction: Int, probability: Float
 * <p>
 * params:
 * input: input table
 * output: output table
 * inputCol: text or tokens col name
 * model: fasttext model path
 */
case class FastTextPredictorArgs(input: String, output: String, inputCol: String, model: String)

object FastTextPredictorUDS extends UDS[FastTextPredictorArgs] {

  def run(spark: SparkSession, args: Args): Unit = {
    var df = spark.sql(s"select * from ${args.input}")

    val oriCols = df.columns

    val tokensCol = if (SchemaUtils.isStringArrayType(df.schema, args.inputCol)) {
      args.inputCol
    }
    else {
      val colName = "tokens"

      val tokenizer = new HanLPTokenizer()
        .setSkipEmpty(true)
        .setInputCol(args.inputCol)
        .setOutputCol(colName)

      df = tokenizer.transform(df)
      colName
    }

    val featuresCol = "features"

    val schema = StructType(df.schema.fields :+ StructField(featuresCol, StringType))

    val newRdd = df.rdd.map(row => {
      val text = row.getAs[Seq[String]](tokensCol).mkString(" ")
      Row.fromSeq(row.toSeq :+ text)
    })

    df = df.sparkSession.createDataFrame(newRdd, schema)

    val predictor = new FastTextPredictor()
      .setPath(args.model)

    df = predictor.transform(df)

    val keepCols = oriCols ++ Array(predictor.getPredictionCol, predictor.getProbabilityCol)

    df.columns.filterNot(keepCols.contains).foreach(col => df = df.drop(col))

    if (SparkUtil.isLocalMaster(spark.sparkContext.getConf)) {
      df.show(false)
    } else {
      SparkUtil.saveAsTable(df, args.output)
    }
  }
}
