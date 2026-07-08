package com.netease.easyml.uds.util

import com.alibaba.fastjson.{JSON, JSONObject}
import com.netease.easyml.common.cmds.UserDefinedCmd
import com.netease.easyml.common.util.{ConvertUtil, IOUtil, SparkUtil}
import com.netease.easyml.local.mllib.tfserving.config.ModelBaseConfig
import com.netease.easyml.uds.util.LoadFeatureDump.loadAndParseFeatureDf
import com.netease.easyml.uds.util.RankUtil._
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Row, SparkSession}

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

case class LoadFeatureDumpArgs(input: String, config: String, startDay: String = "", endDay: String = "", keys: String = "", music: Boolean = false)

class LoadFeatureDump extends UserDefinedCmd[LoadFeatureDumpArgs] {

  override def apply(spark: SparkSession, args: LoadFeatureDumpArgs): DataFrame = {
    val keys = args.keys.split("[,;]").filterNot(_.isEmpty)
    val mConfig = readServerConfig(args.config)
    loadAndParseFeatureDf(spark, args.input, args.startDay, args.endDay, mConfig, if (keys.isEmpty) None else Some(keys), args.music)
  }

}

object LoadFeatureDump {

  def loadAndParseFeatureDf(spark: SparkSession, feature: String, startDay: String, endDay: String, mConfig: ModelBaseConfig, keys: Option[Array[String]] = None, music: Boolean = false): DataFrame = {
    var (contextType, itemType) = schemaFromServerConfig(mConfig)
    val kvFeas = mConfig.getKvFeatures.asScala.map(_.getName).toSet
    if (keys.isDefined) {
      contextType = StructType(contextType.fields.filter(it => keys.get.contains(it.name)))
      itemType = StructType(itemType.fields.filter(it => keys.get.contains(it.name)))
    }
    var featureDf = if (IOUtil.isDirectory(feature)) {
      var paths = IOUtil.listDirectory(feature).asScala.toArray.map(IOUtil.baseName)
      if (startDay.nonEmpty) {
        paths = paths.filter(it => it >= startDay)
      }
      if (endDay.nonEmpty) {
        paths = paths.filter(it => it <= endDay)
      }
      paths = paths.map(it => IOUtil.join(feature, it))
      spark.read.text(paths: _*)
    } else {
      SparkUtil.loadFromTable(spark, feature, startDay = startDay, endDay = endDay)
    }

    val featureRdd = featureDf.rdd.flatMap(it => {
      try {
        val text = it.getString(0)
        val json = JSON.parseObject(text)
        val sId = json.getString("ri")
        val userObj = json.getJSONObject("ufm")
        if (userObj == null) {
          Array[Row]()
        } else {
          val user = ArrayBuffer.empty[Any]
          contextType.foreach(it => {
            val value = if (music) parseFromMusicJson(it.dataType, userObj, it.name, kvFeas.contains(it.name)) else parseFromJson(it.dataType, userObj, it.name)
            user.append(value)
          })
          val itemsObj = json.getJSONObject("ifm")
          if (itemsObj == null) {
            Array[Row]()
          } else {
            itemsObj.keySet().asScala.toSeq.map(it => {
              val itemObj = itemsObj.getJSONObject(it)
              val item = ArrayBuffer.empty[Any]
              itemType.foreach(it => {
                val value = if (music) parseFromMusicJson(it.dataType, itemObj, it.name, kvFeas.contains(it.name)) else parseFromJson(it.dataType, itemObj, it.name)
                item.append(value)
              })
              Row.fromSeq(Seq(sId) ++ user ++ item)
            })
          }
        }
      } catch {
        case _: Exception =>
          Array[Row]()
      }
    })

    featureDf = spark.createDataFrame(featureRdd, StructType(Seq(StructField(SESSION_ID, StringType)) ++ contextType.fields ++ itemType.fields))
      .filter(col(SESSION_ID).isNotNull.and(col(SESSION_ID).notEqual("")))
    featureDf
  }

  def parseFromJson(dataType: DataType, obj: JSONObject, key: String): Any = {
    if (!obj.containsKey(key)) {
      null
    } else {
      dataType match {
        case FloatType => obj.getFloatValue(key)
        case IntegerType => obj.getIntValue(key)
        case StringType => obj.getString(key)
        case ArrayType(FloatType, true) =>
          val array = obj.getJSONArray(key)
          if (array.isEmpty) null else (0 until array.size()).map(array.getFloatValue)
        case ArrayType(StringType, true) =>
          obj.get(key) match {
            case map: java.util.Map[_, _] =>
              if (map.isEmpty) null else {
                val array = ArrayBuffer.empty[String]
                map.keySet().asScala.foreach(k => {
                  array.append(s"$k:${map.get(k)}")
                })
                array
              }
            case col: java.util.Collection[_] =>
              if (col.isEmpty) null else col.asScala.map(ConvertUtil.toString)
          }
      }
    }
  }

  def parseFromMusicJson(dataType: DataType, obj: JSONObject, key: String, kv: Boolean): Any = {
    if (!obj.containsKey(if (kv) key + KV_FEA_SUFFIX_IDS else key)) {
      null
    } else {
      if (kv) {
        val keys = obj.getJSONArray(key + KV_FEA_SUFFIX_IDS)
        val values = obj.getJSONArray(key + KV_FEA_SUFFIX_VALUES)
        if (keys.isEmpty) null else {
          if (keys.size() == 1 && ConvertUtil.toString(keys.get(0)).isEmpty && ConvertUtil.toDouble(values.get(0)) == 0) {
            null
          } else {
            (0 until keys.size()).map(i => {
              val k = ConvertUtil.toString(keys.get(i))
              val v = ConvertUtil.toDouble(values.get(i))
              s"$k:$v"
            })
          }
        }
      } else {
        val array = obj.getJSONArray(key)
        dataType match {
          case FloatType => array.getFloat(0)
          case IntegerType => array.getInteger(0)
          case StringType =>
            var str = array.getString(0)
            if (key.equals(ITEM_ID)) {
              val unit = str.split(":")
              str = unit.slice(1, unit.length).mkString(":")
            }
            str
          case ArrayType(FloatType, true) =>
            if (array.isEmpty) null else (0 until array.size()).map(array.getFloatValue)
          case ArrayType(StringType, true) =>
            if (array.isEmpty) null else {
              var values = (0 until array.size()).map(i => ConvertUtil.toString(array.get(i)))
              if (values.length == 1 && values(0).isEmpty) {
                values = null
              }
              values
            }
        }
      }
    }
  }

  def fillNa(dataType: DataType, obj: Any): Any = {
    dataType match {
      case FloatType => if (obj == null) 0.0f else obj
      case IntegerType => if (obj == null) 0 else obj
      case StringType => if (obj == null) STRING_MASK else obj
      case ArrayType(FloatType, true) =>
        if (obj == null || obj.asInstanceOf[Seq[String]].isEmpty) Array(0.0f) else obj
      case ArrayType(StringType, true) =>
        if (obj == null || obj.asInstanceOf[Seq[String]].isEmpty) Array(STRING_MASK) else obj
    }
  }

  def fillNa(df: DataFrame, columns: Array[String]): DataFrame = {
    val dataTypes = df.schema.fields.map(_.dataType)
    val indices = columns.map(df.columns.indexOf)
    val rdd = df.rdd.map(row => {
      Row.fromSeq(row.toSeq.zipWithIndex.map(it => if (indices.contains(it._2)) fillNa(dataTypes(it._2), it._1) else it._1))
    })
    df.sparkSession.createDataFrame(rdd, df.schema)
  }
}


