package com.netease.easyml.uds.examples

import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.{IOUtil, SparkUtil}
import com.netease.easyml.ml.feature.{ToDenseVector, ToDoubleArray}
import com.netease.easyml.uds.util.Constant.NULL
import org.apache.spark.ml.feature.{PCA, PCAModel}
import org.apache.spark.sql.SparkSession;

/**
 * Created by linjiuning on 2021/6/25.
 * Doing pca dimension reduction.
 * <p>
 * data schema:
 * [input] vector: Seq[Numeric]
 * [output] gensim format
 * <p>
 * params:
 * input: input table
 * output: output table
 * path: output model path
 * vectorCol: vector column name
 * k: the number of principal components (> 0)
 * mode: fit, transform or all
 * format: save hive format
 */
case class PCAArgs(input: String, output: String, vectorCol: String, k: Int, path: String = NULL,
                   mode: String = "all", format: String = "parquet")

object PCAUDS extends UDS[PCAArgs] {

  def run(spark: SparkSession, args: Args): Unit = {
    var df = spark.sql(s"select * from ${args.input}")

    val tmpCol = "tmp_" + args.vectorCol
    df = new ToDenseVector()
      .setInputCol(args.vectorCol)
      .setOutputCol(tmpCol)
      .transform(df)
      .drop(args.vectorCol)

    val pcaModel = if (args.mode.equals("transform")) {
      PCAModel.load(args.path)
    } else {
      val model = new PCA()
        .setK(args.k)
        .setInputCol(tmpCol)
        .setOutputCol(args.vectorCol)
        .fit(df)
      if (!args.path.equals(NULL)) {
        if (IOUtil.exists(args.path)) {
          IOUtil.delete(args.path)
        }
        IOUtil.mkParentDirs(args.path)
        model.save(args.path)
      }
      model
    }

    if (!args.mode.equals("fit")) {
      df = pcaModel.transform(df)

      df = new ToDoubleArray()
        .setInputCol(args.vectorCol)
        .setOutputCol(tmpCol)
        .transform(df)
        .drop(args.vectorCol)
        .withColumnRenamed(tmpCol, args.vectorCol)

      if (SparkUtil.isLocalMaster(spark.sparkContext.getConf)) {
        df.show(false)
      } else {
        SparkUtil.saveAsTable(df, args.output, format = args.format)
      }
    }
  }
}
