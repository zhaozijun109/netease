package com.netease.easyml.uds.examples

import com.alibaba.fastjson.JSON
import com.linkedin.spark.datasources.tfrecordv2.TFRecordSerializer
import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.{ConvertUtil, IOUtil, SparkUtil, StringUtil}
import com.netease.easyml.uds.examples.RankTFRecordEIEUDS.load
import com.netease.easyml.uds.util.LoadFeatureDump.{fillNa, parseFromJson, parseFromMusicJson}
import com.netease.easyml.uds.util.RankUtil._
import org.apache.commons.lang3.StringUtils
import org.apache.spark.sql.catalyst.expressions.GenericRow
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.sql.{Row, SparkSession}

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

/**
 * Created by linjiuning on 2021/6/5.
 * parse dump feature.
 */
case class RankFeatureDumpEIEArgs(config: String, labelInput: String, feature: String,
                                  output: String, contextInput: String = "", day: String = "", fillNa: Boolean = true,
                                  env: String = "", gzip: Boolean = true, numPartitions: Int = RESULT_PARTITION,
                                  broadcastLabel: Boolean = false, multiType: Boolean = false, position: String = POSITION,
                                  music: Boolean = false, partitionByHour: Boolean = false)

object RankFeatureDumpEIEUDS extends UDS[RankFeatureDumpEIEArgs] {

  def run(spark: SparkSession, args: RankFeatureDumpEIEArgs): Unit = {
    val mConfig = readServerConfig(args.config)
    val feaConfigs = featureConfigFromServerConfig(mConfig)
    val kvs = feaConfigs.filter(it => it.format.equals("kv")).map(_.name).toSet

    val (contextType, itemType) = schemaFromServerConfig(mConfig)
    val (eContextType, eItemType) = schemaFromServerConfig(mConfig, expandKv = true)
    var featureDf = if (IOUtil.isDirectory(args.feature)) {
      spark.read.text(IOUtil.join(args.feature, s"${args.day}"))
    } else {
      SparkUtil.loadFromTable(spark, args.feature, startDay = args.day, endDay = args.day)
    }
    var labelDf = load(spark, args.labelInput, startDay = args.day, endDay = args.day, env = args.env)

    val keepColumns = Array(IS_TRAIN, SESSION_ID, USER_ID, ITEM_ID)
    val other = labelDf.columns.filterNot(keepColumns.contains)
    if (!other.isEmpty) {
      labelDf = SparkUtil.withSerializeExampleColumn(labelDf, other, SERIALIZED_EXAMPLES, Array(args.position))
    }

    if (other.contains(args.position)) {
      val sortUdf = udf(sort _)
      labelDf = labelDf.groupBy(SESSION_ID, USER_ID).agg(max(IS_TRAIN).alias(IS_TRAIN), collect_list(struct(ITEM_ID, SERIALIZED_EXAMPLES, args.position)).alias(SERIALIZED_EXAMPLES))
        .select(col(SESSION_ID), col(USER_ID), col(IS_TRAIN), sortUdf(col(SERIALIZED_EXAMPLES)).alias(SERIALIZED_EXAMPLES))
        .select(col(SESSION_ID), col(USER_ID), col(IS_TRAIN), col(SERIALIZED_EXAMPLES).getField("v0").alias(ITEM_ID), col(SERIALIZED_EXAMPLES).getField("v1").alias(SERIALIZED_EXAMPLES))
    } else {
      labelDf = labelDf.groupBy(SESSION_ID, USER_ID).agg(max(IS_TRAIN).alias(IS_TRAIN), collect_list(ITEM_ID).alias(ITEM_ID), collect_list(SERIALIZED_EXAMPLES).alias(SERIALIZED_EXAMPLES))
    }

    if (StringUtils.isNoneBlank(args.contextInput)) {
      var contextDf = load(spark, args.contextInput, startDay = args.day, endDay = args.day, env = args.env)
      val keepColumns = Array(SESSION_ID, USER_ID)
      val other = contextDf.columns.filterNot(keepColumns.contains)
      assert(other.nonEmpty)
      contextDf = SparkUtil.withSerializeExampleColumn(contextDf, other, SERIALIZED_CONTEXT)
      labelDf = labelDf.alias("t1").join(contextDf.alias("t2"), Seq(SESSION_ID, USER_ID)).select("t1.*", SERIALIZED_CONTEXT)
    } else {
      labelDf = labelDf.withColumn(SERIALIZED_CONTEXT, lit(null))
    }

    import spark.implicits._
    featureDf = featureDf.rdd.map(it => {
      try {
        val text = it.getString(0)
        val json = JSON.parseObject(text)
        val sId = json.getString("ri")
        val uid = json.getString("ui")
        val ufm = json.getString("ufm")
        val ifm = json.getString("ifm")
        (sId, uid, ufm, ifm)
      } catch {
        case _: Exception =>
          null
      }
    }).filter(it => it != null && !StringUtil.isEmpty(it._1))
      .toDF(SESSION_ID, USER_ID, "ufm", "ifm")

    val contextSerializer = new TFRecordSerializer(StructType(StructField(SESSION_ID, StringType) +: eContextType.fields))
    val exampleSerializer = new TFRecordSerializer(eItemType)

    if (args.broadcastLabel) {
      labelDf = broadcast(labelDf)
    }

    var result = featureDf.join(labelDf, Seq(SESSION_ID, USER_ID))
      .select(IS_TRAIN, SESSION_ID, "ufm", "ifm", ITEM_ID, SERIALIZED_EXAMPLES, SERIALIZED_CONTEXT)
      .rdd.map(it => {
      val isTrain = ConvertUtil.toString(it.get(0))
      val rid = it.getString(1)
      val itemIds = it.getAs[Seq[String]](ITEM_ID)
      val examples = it.getAs[Seq[Array[Byte]]](SERIALIZED_EXAMPLES)
      val context = it.getAs[Array[Byte]](SERIALIZED_CONTEXT)
      val indices = itemIds.zipWithIndex.toMap
      try {
        val userObj = JSON.parseObject(it.getAs[String]("ufm"))
        var hour = "unk"
        if (userObj == null) {
          null
        } else {
          val user = ArrayBuffer.empty[Any]
          contextType.foreach(it => {
            var value = if (args.music) parseFromMusicJson(it.dataType, userObj, it.name, kvs.contains(it.name)) else parseFromJson(it.dataType, userObj, it.name)
            if (kvs.contains(it.name)) {
              val pair = parseKV(value.asInstanceOf[Seq[String]])
              user.append(pair.keys)
              user.append(pair.values)
            } else {
              if (value != null) {
                if (it.name.equals("u_follow_blogs")) {
                  val seq = value.asInstanceOf[Seq[String]]
                  if (seq.length > 50) {
                    value = seq.slice(0, 50)
                  }
                } else if (it.name.equals("u_tags")) {
                  val seq = value.asInstanceOf[Seq[String]]
                  if (seq.length > 15) {
                    value = seq.slice(0, 15)
                  }
                }
              }
              if (args.fillNa) {
                value = fillNa(it.dataType, value)
              }
              if (args.partitionByHour && it.name.equals("o_hour")) {
                hour = ConvertUtil.toInt(value).toString
                if (hour.length < 2) {
                  hour = "0" + hour
                }
              }
              user.append(value)
            }
          })
          val nRow = Row.fromSeq(rid +: user)
          val contextExample = contextSerializer.serializeExample(nRow)
          val uExample = if (context != null) {
            contextExample.toBuilder.mergeFrom(context).build().toByteArray
          } else {
            contextExample.toByteArray
          }
          val itemsObj = JSON.parseObject(it.getAs[String]("ifm"))
          if (itemsObj == null) {
            null
          } else {
            val iExamples = itemsObj.keySet().asScala.toArray
              .filter(it => indices.contains(it.split(":").last))
              .map(it => {
                val itemId = it.split(":").last
                val example = examples(indices(itemId))
                val itemObj = itemsObj.getJSONObject(it)
                val item = ArrayBuffer.empty[Any]
                itemType.foreach(it => {
                  var value = if (args.music) parseFromMusicJson(it.dataType, itemObj, it.name, kvs.contains(it.name)) else parseFromJson(it.dataType, itemObj, it.name)
                  if (kvs.contains(it.name)) {
                    val pair = parseKV(value.asInstanceOf[Seq[String]])
                    item.append(pair.keys)
                    item.append(pair.values)
                  } else {
                    if (args.fillNa) {
                      value = fillNa(it.dataType, value)
                    }
                    item.append(value)
                  }
                })
                val nRow = Row.fromSeq(item)
                val iExample = exampleSerializer.serializeExample(nRow).toBuilder.mergeFrom(example).build().toByteArray
                iExample
              })
            (isTrain, hour, uExample, iExamples)
          }
        }
      } catch {
        case e: Exception =>
          logWarning(e.getMessage)
          null
      }
    }).filter(it => it != null)
      .toDF(IS_TRAIN, HOUR, SERIALIZED_CONTEXT, SERIALIZED_EXAMPLES)

    if (!args.partitionByHour) {
      result = result.drop(HOUR)
    }

    if (SparkUtil.isLocalMaster(spark.sparkContext.getConf)) {
      result.show(false)
    } else {
      saveTfRecord(result, args.output, gzip = args.gzip, numPartitions = args.numPartitions, multiType = args.multiType, partitionBy = if (args.partitionByHour) Some(Seq(HOUR)) else None)
    }

  }

  case class Pair(v0: String, v1: Array[Byte])

  def sort(pairs: Seq[GenericRow]): Seq[Pair] = {
    pairs.map(pair => (pair.getString(0), pair.getAs[Array[Byte]](1), ConvertUtil.toFloat(pair.get(2))))
      .sortBy(_._3)
      .map(it => Pair(it._1, it._2))
  }
}
