package com.netease.easyml.uds.examples

import com.netease.easyml.common.resource.ResourceManager
import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.{IOUtil, SparkUtil}
import com.netease.easyml.local.mllib.tokenizer.transformers.{BertTokenizer, TransformersUtil}
import com.netease.easyml.ml.transform.{TensorFlowPredictor, TorchPredictor}
import com.netease.easyml.ml.util.SchemaUtils
import com.netease.easyml.uds.util.Constant._
import org.apache.spark.sql.types.{ArrayType, IntegerType}
import org.apache.spark.sql.{Row, SparkSession}

/**
 * Created by linjiuning on 2021/7/26.
 * TF/TH bert embedding predictor, exported by nlp_zoo's run_bert_whitening script.
 * <p>
 * data schema:
 * [input] textA: String, textB[Optional]: String
 * [output] vector: Array[Float]
 * <p>
 * params:
 * input: input table
 * output: output table
 * vocabPath: vocab path of bert[Optional]
 * exportDir: saved model dir
 * textACol: col name
 * textBCol: col name[Optional]
 * maxLength: max text length <= 512
 * lowercase: do lowercase
 * batchSize: batch size
 * numThreads: num threads[Optional]
 * numPartitions: num partitions[Optional]
 * format: hive storage format
 */
case class BertEmbeddingArgs(input: String, output: String, vocabPath: String,
                             exportDir: String, textACol: String, textBCol: String = NULL,
                             maxLength: Int = 128, lowercase: Boolean = true,
                             batchSize: Int = 32, numThreads: Int = 0, numPartitions: Int = 0,
                             format: String = "parquet")

object BertEmbeddingUDS extends UDS[BertEmbeddingArgs] {
  val INPUT_COLS: Array[String] = Array("Input-Token", "Input-Segment")
  val OUTPUT_COL = "vector"

  def run(spark: SparkSession, args: Args): Unit = {
    val conf = spark.sparkContext.getConf

    val nThreads = if (args.numThreads > 0) {
      args.numThreads
    } else {
      SparkUtil.getNumTaskCpus(conf)
    }

    val nPartitions = if (args.numPartitions > 0) {
      args.numPartitions
    } else {
      4 * SparkUtil.getDefaultParallelism(conf)
    }

    var df = spark.sql(s"select * from ${args.input} where ${args.textACol} is not null")

    val tfBackend = IOUtil.isDirectory(args.exportDir)

    val predictor = if (tfBackend) {
      new TensorFlowPredictor()
        .setBatchSize(args.batchSize)
        .setInputCols(INPUT_COLS)
        .setDropInputs(true)
        .setNumThreads(nThreads)
        .setNumPartitions(nPartitions)
        .setPath(args.exportDir)
    } else {
      new TorchPredictor()
        .setInputCols(INPUT_COLS)
        .setOutputCols(Array(OUTPUT_COL))
        .setBatchSize(args.batchSize)
        .setDropInputs(true)
        .setNumThreads(nThreads)
        .setNumPartitions(nPartitions)
        .setPath(args.exportDir)
    }

    val rdd = df.rdd.repartition(nPartitions).mapPartitions(iter => {
      val tokenizer = getOrCreate(args.vocabPath, args.maxLength, args.lowercase)

      iter.map(row => {
        val textA = row.getAs[String](args.textACol)
        val textB = if (!args.textBCol.equals(NULL)) {
          row.getAs[String](args.textBCol)
        } else {
          null
        }
        val feature = tokenizer.encodePlus(textA, textB)

        val inputIds = feature.get(TransformersUtil.IndexKeys.INPUT_IDS)
        val tokenTypeIds = feature.get(TransformersUtil.IndexKeys.TOKEN_TYPE_IDS)
        Row.fromSeq(row.toSeq ++ Seq(inputIds, tokenTypeIds))
      })
    })

    var schema = df.schema
    INPUT_COLS.foreach(name => {
      schema = SchemaUtils.appendColumn(schema, name, ArrayType(IntegerType))
    })

    df = spark.createDataFrame(rdd, schema)

    val cols = df.columns

    df = predictor.transform(df)

    if (tfBackend) {
      val outputCol = df.columns.filterNot(cols.contains)(0)
      df = df.withColumnRenamed(outputCol, OUTPUT_COL)
    }

    if (SparkUtil.isLocalMaster(conf)) {
      df.show(false)
    } else {
      SparkUtil.saveAsTable(df, args.output, args.format)
    }

  }

  def getOrCreate(vocabPath: String, maxLength: Int, lowercase: Boolean): BertTokenizer = {
    ResourceManager.getOrCreate(vocabPath, () => {
      val builder = BertTokenizer.builder
      if (!vocabPath.equals(NULL)) {
        builder.setVocabFile(vocabPath)
      }
      builder.setMaxLength(maxLength)
      builder.setDoLowerCase(lowercase)
      builder.build
    })
  }
}
