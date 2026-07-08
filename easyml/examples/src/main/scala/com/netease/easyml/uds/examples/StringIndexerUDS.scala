package com.netease.easyml.uds.examples

import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.SparkUtil
import com.netease.easyml.uds.util.Constant._
import org.apache.spark.ml.feature_.StringIndexer
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.types.StringType;

/**
 * Created by linjiuning on 2020/12/21.
 * Convert String to index
 * <p>
 * data schema:
 * [input] : inputCol: String or Array[String]
 * [output] inputCol: _, inputCol_2id: Double or Array[Double]
 * <p>
 * params:
 * input: input table
 * output: output table
 * outputVocab: output vocab table
 * path: model save hdfs path
 * inputCols: col name of input
 * castCols: cols of inputCols which type is Numeric so that need to be cast to StringType.
 * outputType: index dataType, should be int, double or long.
 * startOffset: index start offset, default 0.
 * minCount: min count, default 1.
 * keepInput: whether to keep input Cols, default true
 */
case class StringIndexerArgs(input: String, output: String, outputVocab: String, path: String = NULL, inputCols: String, castCols: String = NULL,
                             outputType: String = "int", startOffset: Int = 0, minCount: Int = 1, keepInput: Boolean = true, format: String = PARQUET)

object StringIndexerUDS extends UDS[StringIndexerArgs] {
  def run(spark: SparkSession, args: Args): Unit = {
    var df = spark.sql(s"select * from ${args.input}")
    if (!args.castCols.equals(NULL)) {
      args.castCols.split(";").filter(_.nonEmpty).foreach(c => {
        df = df.withColumn(c, col(c).cast(StringType))
      })
    }
    val inputCols = args.inputCols.split(";")
    val outputCols = inputCols.map(_ + "_2id")
    val minCount = args.minCount
    val handleInvalid = if (minCount > 1) {
      "skip"
    } else {
      "error"
    }
    val model = new StringIndexer()
      .setInputCols(inputCols)
      .setOutputCols(outputCols)
      .setOutputType(args.outputType)
      .setHandleInvalid(handleInvalid)
      .setStartOffset(args.startOffset)
      .setMinCount(minCount)
      .fit(df)

    var newDf = model.transform(df)

    if (!args.keepInput) {
      newDf = newDf.drop(inputCols: _*)
    }

    SparkUtil.saveAsTable(newDf, args.output, format = args.format)

    if (!args.path.equals(NULL)) {
      model.write.overwrite().save(args.path)
    }
    val offset = model.getStartOffset
    val vocabs = model.getInOutCols()._1.zip(model.labelsArray).flatMap {
      case (inputCol, vocabs) =>
        vocabs.zipWithIndex.map {
          case (word, idx) =>
            (inputCol, word, idx + offset)
        }
    }

    import spark.implicits._
    val vocabDf = spark.sparkContext.parallelize(vocabs).toDF("col", "word", "id")
    SparkUtil.saveAsTable(vocabDf, args.outputVocab, "textfile")
  }
}
