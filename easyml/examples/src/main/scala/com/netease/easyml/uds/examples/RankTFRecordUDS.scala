package com.netease.easyml.uds.examples

import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.{IOUtil, SparkUtil}
import com.netease.easyml.local.mllib.tokenizer.transformers.BertTokenizer
import com.netease.easyml.uds.util.Constant.NULL
import com.netease.easyml.uds.util.RankUtil
import com.netease.easyml.uds.util.RankUtil._
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{Column, DataFrame, SparkSession}

import scala.collection.mutable

/**
 * Created by linjiuning on 2021/3/9.
 * write tf record.
 */
case class RankTFRecordArgs(input: String, sql: String = NULL, output: String, config: String,
                            startDay: String = "", endDay: String = "", env: String = "", recordType: String = EXAMPLE,
                            splitTrainEval: Boolean = true, procFeaOnly: Boolean = true, fillNa: Boolean = true, asInt: Boolean = false,
                            maxLength: Int = 0, gzip: Boolean = true, numPartitions: Int = RESULT_PARTITION,
                            multiType: Boolean = false)

object RankTFRecordUDS extends UDS[RankTFRecordArgs] {
  var BERT_TOKENIZER_BC: Broadcast[BertTokenizer] = _

  def run(spark: SparkSession, args: RankTFRecordArgs): Unit = {
    var configs = if (args.config.endsWith(".yaml")) {
      featureConfigFromServerConfig(readServerConfig(args.config))
    } else readConfig(args.config)
    var names = configs.map(_.name)

    var df = if (IOUtil.isDirectory(args.input)) {
      loadTfRecords(spark, args.input, startDay = args.startDay, endDay = args.endDay, recordType = args.recordType)
    } else {
      SparkUtil.loadFromTable(spark, args.input, startDay = args.startDay, endDay = args.endDay)
    }

    if (!args.sql.equals(NULL)) {
      df.createOrReplaceTempView("a")
      df = RankUtil.sql(spark, args.sql, args.env)
    }

    var columns = df.columns

    if (args.splitTrainEval && columns.contains(IS_TRAIN)) {
      df = df.withColumn(IS_TRAIN, when(col(IS_TRAIN).equalTo(1).or(col(DAY) < args.endDay), 1).otherwise(0))
    }

    if (columns.contains(IS_TRAIN) && !names.contains(IS_TRAIN)) {
      names = IS_TRAIN +: names
    }

    columns.foreach(col => {
      if (!col.toLowerCase.equals(col)) {
        df = df.withColumnRenamed(col, col.toLowerCase)
      }
    })

    columns = df.columns

    names = names.filter(columns.contains)
    configs = configs.filter(it => names.contains(it.name))
    df = df.select(names.map(col): _*)

    df = process(df, args.recordType, configs, args.procFeaOnly, args.maxLength, args.fillNa, args.asInt)
    saveTfRecord(df, args.output, recordType = args.recordType, splitTrainEval = args.splitTrainEval,
      gzip = args.gzip, numPartitions = args.numPartitions, multiType = args.multiType)
  }

  def aggDForSequenceExample(df: DataFrame, configs: Array[FeatureConfig], procFeaOnly: Boolean): DataFrame = {
    val aggColumns = mutable.ArrayBuffer[Column]()
    val nConfigs = if (procFeaOnly) {
      configs.filter(_.isFea)
    } else {
      configs
    }
    nConfigs.foreach(config => {
      val name = config.name
      val format = config.format
      val share = config.share
      format match {
        case "int" | "float" | "string" | "list_float" | "list_string" =>
          val column = if (share) {
            collect_list(name).getItem(0).alias(name)
          } else {
            collect_list(name).alias(name)
          }
          aggColumns.append(column)
        case "kv" =>
          val idName = name + KV_FEA_SUFFIX_IDS
          val weightName = name + KV_FEA_SUFFIX_VALUES
          val (columnId, columnWt) = if (share) {
            val columnId = collect_list(idName).getItem(0).alias(idName)
            val columnWt = collect_list(weightName).getItem(0).alias(weightName)
            (columnId, columnWt)
          } else {
            val columnId = collect_list(idName).alias(idName)
            val columnWt = collect_list(weightName).alias(weightName)
            (columnId, columnWt)
          }
          aggColumns.append(columnId)
          aggColumns.append(columnWt)
      }
    })
    val aggCols = aggColumns.result().toArray
    if (aggCols.isEmpty) {
      df
    } else {
      df.groupBy(SESSION_ID).agg(aggCols.head, aggCols.slice(1, aggCols.length): _*)
    }
  }

  def process(oldDf: DataFrame, recordType: String, configs: Array[FeatureConfig],
              procFeaOnly: Boolean, maxLength: Int = 0, fillNa: Boolean = true, asInt: Boolean = false): DataFrame = {
    var df = oldDf
    df = featureProcess(df, configs, procFeaOnly, maxLength = maxLength, fillNa = fillNa, asInt = asInt)

    if (recordType.equals("sequence_example")) {
      df = aggDForSequenceExample(df, configs, procFeaOnly)
    }
    df
  }
}
