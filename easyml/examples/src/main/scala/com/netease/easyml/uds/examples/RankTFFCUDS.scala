package com.netease.easyml.uds.examples

import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.{ConvertUtil, IOUtil, SparkUtil}
import com.netease.easyml.ml.transform.TensorFlowPredictor
import com.netease.easyml.uds.util.RankUtil._
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col, udf}
import org.apache.spark.storage.StorageLevel

/**
 * Created by linjiuning on 2021/6/5.
 * tf feature column preprocess.
 * <p>
 * params:
 * input: input table
 * output: output table
 * config: feature config file
 * mode: saved model dir
 * dropInputs: whether to drop feature columns
 * day: table partition
 */
case class RankTFFCArgs(input: String, output: String, config: String, model: String,
                        startDay: String = "", endDay: String = "", gzip: Boolean = true)

object RankTFFCUDS extends UDS[RankTFFCArgs] {
  val PADDING: Int = -100
  val BATCH_SIZE = 256

  def trimArray(array: Seq[Any]): Seq[Long] = {
    array.map(ConvertUtil.toLong).filterNot(_ == PADDING)
  }

  def firstArray(array: Seq[Long]): Long = {
    array.head
  }

  def run(spark: SparkSession, args: Args): Unit = {
    var df = if (IOUtil.isDirectory(args.input)) {
      SparkUtil.loadFromTfRecordOfDays(spark, args.input, startDay = args.startDay, endDay = args.endDay)
    } else {
      SparkUtil.loadFromTable(spark, args.input, startDay = args.startDay, endDay = args.endDay)
    }

    var configs = readConfig(args.config)
    configs = configs.filter(it => it.isFea)
      .filter(it => it.featureColumn.contains(BUCKET) || it.featureColumn.contains(EMBED))

    val tfPredictor = new TensorFlowPredictor()
      .setExampleName(EXAMPLES)
      .setDropInputs(false)
      .setPath(args.model)
      .setBatchSize(BATCH_SIZE)
      .setNumPartitions(0)

    df = tfPredictor.transform(df)

    val trimUdf = udf(trimArray _)
    //    val firstUdf = udf(firstArray _)
    configs.foreach(config => {
      val name = if (config.format.equals("kv")) {
        config.name + KV_FEA_SUFFIX_IDS
      } else if (config.featureColumn.contains(BUCKET)) {
        config.name + FLOAT_FEA_BUCKETIZED_SUFFIX
      } else {
        config.name
      }
      df = df.withColumn(name, trimUdf(col(name)))
    })

    if (SparkUtil.isLocalMaster(spark.sparkContext.getConf)) {
      df.persist(StorageLevel.MEMORY_AND_DISK)
      df.show(false)
    }
    SparkUtil.saveAsTfRecord(df, args.output, gzip = args.gzip)
  }
}
