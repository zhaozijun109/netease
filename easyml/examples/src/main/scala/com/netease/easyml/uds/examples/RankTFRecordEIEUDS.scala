package com.netease.easyml.uds.examples

import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.Udfs.mergeExampleUdf
import com.netease.easyml.common.util.{ConvertUtil, SparkUtil}
import com.netease.easyml.uds.util.Constant._
import com.netease.easyml.uds.util.RankUtil._
import org.apache.commons.lang3.StringUtils
import org.apache.spark.sql.catalyst.expressions.GenericRow
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.storage.StorageLevel

/**
 * Created by linjiuning on 2021/10/22.
 * write tfr EIE format tf record.
 */
case class RankTFRecordEIEArgs(labelInput: String, contextInput: String = "", exampleInput: String, output: String, config: String,
                               day: String = SparkUtil.NULL, fillNa: Boolean = true,
                               splitTrainEval: Boolean = true, procFeaOnly: Boolean = true, maxLength: Int = 0,
                               gzip: Boolean = true, env: String = "", numPartitions: Int = RESULT_PARTITION,
                               multiType: Boolean = false, position: String = POSITION)

object RankTFRecordEIEUDS extends UDS[RankTFRecordEIEArgs] {
  val POSITIVE_SIZE = "positive_size"
  val SIZE = "size"

  def run(spark: SparkSession, args: RankTFRecordEIEArgs): Unit = {
    val configs = if (args.config.endsWith(".yaml")) {
      featureConfigFromServerConfig(readServerConfig(args.config))
    } else readConfig(args.config)

    val labelDf = load(spark, args.labelInput, startDay = args.day, endDay = args.day, env = args.env)

    var exampleDf = load(spark, args.exampleInput, startDay = args.day, endDay = args.day, env = args.env)
    val contextDf = if (StringUtils.isBlank(args.contextInput)) {
      val userColumns = (configs.filter(_.isContextFea).map(_.name).filter(exampleDf.columns.contains) :+ SESSION_ID).distinct
      val itemColumns = (exampleDf.columns.filterNot(userColumns.contains) :+ SESSION_ID).distinct
      val exprs = userColumns.filterNot(it => it.equals(SESSION_ID) || it.equals(USER_ID)).map(it => first(col(it)).alias(it))
      val contextDf = exampleDf.select(userColumns.map(col): _*).groupBy(SESSION_ID, USER_ID).agg(col(SESSION_ID), col(USER_ID) +: exprs: _*)
      exampleDf = exampleDf.select(itemColumns.map(col): _*)
      contextDf
    } else {
      load(spark, args.contextInput, startDay = args.day, endDay = args.day, env = args.env)
    }

    var joinDf = runJoin(args, labelDf, contextDf, exampleDf, configs)
    if (args.numPartitions > 0) {
      joinDf = joinDf.repartition(args.numPartitions)
    }
    if (SparkUtil.isLocalMaster(spark.sparkContext.getConf)) {
      joinDf.show(false)
    } else {
      saveTfRecord(joinDf, args.output, splitTrainEval = args.splitTrainEval, gzip = args.gzip, multiType = args.multiType)
    }
  }

  def runJoin(args: Args, labelDf: DataFrame, contextDf: DataFrame, exampleDf: DataFrame, configs: Array[FeatureConfig]): DataFrame = {
    var vLabelDf = labelDf.drop(DAY)
    var vContextDf = contextDf.drop(DAY)
    var vExampleDf = exampleDf.drop(DAY)

    val keepColumns = Array(IS_TRAIN, SESSION_ID, USER_ID, ITEM_ID)
    val other = labelDf.columns.filterNot(keepColumns.contains)
    if (!other.isEmpty) {
      vLabelDf = SparkUtil.withSerializeExampleColumn(vLabelDf, other, SERIALIZED_EXAMPLES, Array(args.position))
    }

    vContextDf = processExample(args, vContextDf, configs, serializeSessionId = true, keepCols = Array(SESSION_ID, USER_ID))
    vExampleDf = processExample(args, vExampleDf, configs, serializeSessionId = false, keepCols = Array(SESSION_ID, ITEM_ID))

    vLabelDf.persist(StorageLevel.MEMORY_AND_DISK)

    var usingColumns = if (vExampleDf.columns.contains(SESSION_ID)) Seq(SESSION_ID, ITEM_ID) else Seq(ITEM_ID)
    vExampleDf = vLabelDf.drop(SERIALIZED_CONTEXT).alias("t1").join(vExampleDf.alias("t2"), usingColumns)
      .select("t1.*", s"t2.$EXAMPLES")
    if (vExampleDf.columns.contains(SERIALIZED_EXAMPLES)) {
      vExampleDf = vExampleDf.withColumn(SERIALIZED_EXAMPLES, mergeExampleUdf(col(SERIALIZED_EXAMPLES), col(EXAMPLES)))
        .drop(EXAMPLES)
    } else {
      vExampleDf = vExampleDf.withColumnRenamed(EXAMPLES, SERIALIZED_EXAMPLES)
    }
    vExampleDf.drop(ITEM_ID)

    vContextDf = vContextDf.withColumnRenamed(EXAMPLES, SERIALIZED_CONTEXT)

    vExampleDf = if (!vExampleDf.columns.contains(args.position)) {
      vExampleDf.groupBy(SESSION_ID, USER_ID)
        .agg(collect_list(SERIALIZED_EXAMPLES).alias(SERIALIZED_EXAMPLES), max(IS_TRAIN).alias(IS_TRAIN))
    } else {
      val sortUdf = udf(sort _)
      vExampleDf.groupBy(SESSION_ID, USER_ID)
        .agg(collect_list(struct(SERIALIZED_EXAMPLES, args.position)).alias(SERIALIZED_EXAMPLES), max(IS_TRAIN).alias(IS_TRAIN))
        .select(col(IS_TRAIN), col(SESSION_ID), col(USER_ID), sortUdf(col(SERIALIZED_EXAMPLES)).alias(SERIALIZED_EXAMPLES))
    }

    usingColumns = if (vContextDf.columns.contains(SESSION_ID)) Seq(SESSION_ID, USER_ID) else Seq(USER_ID)
    var joinDf = vContextDf.alias("t1").join(vExampleDf.alias("t2"), usingColumns)
    joinDf = if (joinDf.columns.contains(POSITIVE_SIZE)) {
      joinDf.select(col("t1.*"), col(s"t2.$IS_TRAIN"), col(s"t2.$SERIALIZED_EXAMPLES"), col(s"t2.$POSITIVE_SIZE"))
    } else {
      joinDf.select(col("t1.*"), col(s"t2.$IS_TRAIN"), col(s"t2.$SERIALIZED_EXAMPLES"))
    }
    val keep = Array(IS_TRAIN, SERIALIZED_CONTEXT, SERIALIZED_EXAMPLES, SIZE)
    joinDf.columns.filterNot(keep.contains).foreach(it => joinDf = joinDf.drop(it))
    joinDf
  }

  def processExample(args: Args, df: DataFrame, configs: Array[FeatureConfig], serializeSessionId: Boolean, keepCols: Array[String] = Array()): DataFrame = {
    var newDf = SparkUtil.columnsToLowerCase(df)
    val newConfigs = configs.filter(it => newDf.columns.contains(it.name))
    var names = newConfigs.map(_.name).distinct
    if (!serializeSessionId) {
      names = names.filterNot(it => it.equals(SESSION_ID))
    }

    newDf = featureProcess(newDf, newConfigs, args.procFeaOnly, maxLength = args.maxLength, fillNa = args.fillNa)

    SparkUtil.withSerializeExampleColumn(newDf, names, EXAMPLES, keepCols)
  }

  def load(spark: SparkSession, input: String, startDay: String = NULL, endDay: String = NULL, env: String = ""): DataFrame = {
    if (input.endsWith(SparkUtil.SQL_FILE)) {
      SparkUtil.sql(spark, input, SparkUtil.parseEnv(env))
    } else {
      SparkUtil.loadFromTable(spark, input, startDay = startDay, endDay = endDay)
    }
  }

  def sort(pairs: Seq[GenericRow]): Seq[Array[Byte]] = {
    pairs.map(pair => (pair.getAs[Array[Byte]](0), ConvertUtil.toFloat(pair.get(1))))
      .sortBy(_._2)
      .map(_._1)
  }
}
