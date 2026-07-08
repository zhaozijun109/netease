package com.netease.easyml.uds.examples

import com.linkedin.spark.datasources.tfrecordv2.TFRecordDeserializer
import com.linkedin.spark.shaded.org.tensorflow.example.Example
import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.SparkUtil
import com.netease.easyml.uds.examples.RankTFRecordEIEUDS.load
import com.netease.easyml.uds.util.LoadFeatureDump.fillNa
import com.netease.easyml.uds.util.RankUtil._
import org.apache.commons.lang3.StringUtils
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.sql.{Row, SparkSession}

/**
 * Created by linjiuning on 2021/6/5.
 * join eie feature.
 */
case class EIEJoinArgs(input: String, output: String, exampleInput: String = "", contextInput: String = "",
                       day: String = "", fillNa: Boolean = false,
                       env: String = "", gzip: Boolean = true, numPartitions: Int = RESULT_PARTITION,
                       multiType: Boolean = false)

object EIEJoinUDS extends UDS[EIEJoinArgs] {

  def run(spark: SparkSession, args: EIEJoinArgs): Unit = {
    var featureDf = SparkUtil.loadFromTfRecord(spark, args.input, source = "tfrecordv2", options = Some(Map("bytes_keys" -> s"$SERIALIZED_CONTEXT,$SERIALIZED_EXAMPLES")))
    val structType = StructType(Seq(StructField(SESSION_ID, StringType), StructField(USER_ID, StringType)))
    val deserializer = new TFRecordDeserializer(structType)
    val rdd = featureDf.rdd.map(row => {
      val bytes = row.getAs[Array[Byte]](SERIALIZED_CONTEXT)
      val example = Example.parseFrom(bytes)
      val internalRow = deserializer.deserializeExample(example)
      Row.fromSeq(Seq(internalRow.getString(0), internalRow.getString(1)) ++ row.toSeq)
    })
    val fields = structType.fields ++ featureDf.schema.fields
    featureDf = spark.createDataFrame(rdd, StructType(fields))

    if (StringUtils.isNoneBlank(args.exampleInput)) {
      var exampleDf = load(spark, args.exampleInput, startDay = args.day, endDay = args.day, env = args.env)
      val keepColumns = Array(SESSION_ID, USER_ID, ITEM_ID)
      val other = exampleDf.columns.filterNot(keepColumns.contains)
      assert(other.nonEmpty)
      if (args.fillNa) {
        exampleDf = fillNa(exampleDf, other)
      }
      exampleDf = SparkUtil.withSerializeExampleColumn(exampleDf, other, SERIALIZED_EXAMPLES)
      exampleDf = exampleDf.groupBy(SESSION_ID, USER_ID).agg(collect_list(ITEM_ID).alias(ITEM_ID), collect_list(SERIALIZED_EXAMPLES).alias(SERIALIZED_EXAMPLES))
      featureDf = featureDf.alias("t1").join(exampleDf.alias("t2"), Seq(SESSION_ID, USER_ID))
        .selectExpr("t1.*", s"t2.$ITEM_ID", s"t2.$SERIALIZED_EXAMPLES as merge_$SERIALIZED_EXAMPLES")
    }

    if (StringUtils.isNoneBlank(args.contextInput)) {
      var contextDf = load(spark, args.contextInput, startDay = args.day, endDay = args.day, env = args.env)
      val keepColumns = Array(SESSION_ID, USER_ID)
      val other = contextDf.columns.filterNot(keepColumns.contains)
      assert(other.nonEmpty)
      if (args.fillNa) {
        contextDf = fillNa(contextDf, other)
      }
      contextDf = SparkUtil.withSerializeExampleColumn(contextDf, other, SERIALIZED_CONTEXT)
      featureDf = featureDf.alias("t1").join(contextDf.alias("t2"), Seq(SESSION_ID, USER_ID))
        .selectExpr("t1.*", s"t2.$SERIALIZED_CONTEXT as merge_$SERIALIZED_CONTEXT")
    }

    val exampleDeserializer = new TFRecordDeserializer(StructType(Seq(StructField(ITEM_ID, StringType))))
    import spark.implicits._
    val result = featureDf.rdd.map(it => {
      val tp = it.getAs[String](TYPE)
      var context = it.getAs[Array[Byte]](SERIALIZED_CONTEXT)
      var examples = it.getAs[Seq[Array[Byte]]](SERIALIZED_EXAMPLES)
      if (StringUtils.isNoneBlank(args.contextInput)) {
        val mergeContext = if (StringUtils.isNoneBlank(args.contextInput)) it.getAs[Array[Byte]](s"merge_$SERIALIZED_CONTEXT") else null
        context = Example.parseFrom(context).toBuilder.mergeFrom(mergeContext).build().toByteArray
      }

      if (StringUtils.isNoneBlank(args.exampleInput)) {
        val itemIds = it.getAs[Seq[String]](ITEM_ID)
        val indices = itemIds.zipWithIndex.toMap
        val mergeExamples = if (StringUtils.isNoneBlank(args.exampleInput)) it.getAs[Seq[Array[Byte]]](s"merge_$SERIALIZED_EXAMPLES") else null
        examples = examples.map(example => {
          val itemId = exampleDeserializer.deserializeExample(Example.parseFrom(example)).getString(0)
          if (indices.contains(itemId)) {
            Example.parseFrom(example).toBuilder.mergeFrom(mergeExamples(indices(itemId))).build().toByteArray
          } else {
            example
          }
        })
      }
      (tp, context, examples)
    }).toDF(IS_TRAIN, SERIALIZED_CONTEXT, SERIALIZED_EXAMPLES)

    if (SparkUtil.isLocalMaster(spark.sparkContext.getConf)) {
      result.show(false)
    } else {
      saveTfRecord(result, args.output, gzip = args.gzip, numPartitions = args.numPartitions, multiType = args.multiType)
    }

  }

}
