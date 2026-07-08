package com.netease.easyml.uds.examples

import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.{IOUtil, SparkUtil}
import com.netease.easyml.ml.feature.{SIFDocVec, SimpleDocVecModel, WordVecModel, WordWeightModel}
import com.netease.easyml.ml.util.{MLUtils, VectorUtils}
import org.apache.spark.ml.feature.StandardScaler
import org.apache.spark.ml.linalg.Vector
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.udf

/**
 * Created by linjiuning on 2020/10/22.
 * Simple compute DocVec based on WordVec
 * <p>
 * data schema:
 * [input] id: Any, tokens: Seq[String]
 * [output] id: Any, vector: Seq[Float]
 * <p>
 * params:
 * input: input table
 * output: output table/path
 * idCol: id col name
 * inputCol: tokens col name
 * mode: [mean, idf, sif]
 * vector: word vector table/path
 * weight: word weight table/path
 * useSVD: whether use svd to compute sif pc
 * filterEmpty: whether fill empty vector
 */
case class DocVecArgs(input: String, output: String, idCol: String, inputCol: String,
                      mode: String = "mean", vector: String, weight: String, useSVD: Boolean = false, filterEmpty: Boolean = true)

object DocVecUDS extends UDS[DocVecArgs] {

  def run(spark: SparkSession, args: Args): Unit = {

    val df = spark.sql(s"select ${args.idCol}, ${args.inputCol} from ${args.input} where ${args.idCol} is not null and ${args.inputCol} is not null")

    val wordVecModel = WordVecModel.load(args.vector)

    val mode_ = args.mode.toLowerCase
    val wordWeightModel = if (args.weight.nonEmpty && !mode_.equals("mean")) {
      WordWeightModel.load(args.weight)
    } else {
      null
    }

    val model = new SimpleDocVecModel(wordWeightModel, wordVecModel)
      .setInputCol(args.inputCol)
      .setOutputCol("vector")
      .setDivBySize(mode_.equals("sif"))

    var newDf = model.transform(df)
      .drop(args.inputCol)

    val outputCol = model.getOutputCol

    if (mode_.equals("sif")) {
      val tmp = outputCol + "_tmp"
      if (!args.useSVD) {
        newDf = newDf.withColumnRenamed(outputCol, tmp)
        newDf = new StandardScaler()
          .setInputCol(tmp)
          .setOutputCol(outputCol)
          .setWithMean(true)
          .setWithStd(false)
          .fit(newDf)
          .transform(newDf)
          .drop(tmp)
      }
      newDf = newDf.withColumnRenamed(outputCol, tmp)
      newDf = new SIFDocVec()
        .setInputCol(tmp)
        .setOutputCol(outputCol)
        .setUseSVD(args.useSVD)
        .fit(newDf)
        .transform(newDf)
        .drop(tmp)
    }

    def nonZero(v: Vector): Boolean = {
      !VectorUtils.isEmptyVector(v)
    }

    val nonZeroUdf = udf(nonZero _)

    if (args.filterEmpty) {
      newDf = newDf.filter(nonZeroUdf(newDf("vector")))
    }

    if (SparkUtil.isLocalMaster(spark.sparkContext.getConf)) {
      if (IOUtil.isHdfs(args.output)) {
        MLUtils.saveWordVecToText(newDf, args.output, numPartitions = Some(1))
      } else {
        newDf.show(false)
      }
    } else {
      MLUtils.saveWordVecToHive(newDf, args.output, wordCol = args.idCol)
    }
  }
}
