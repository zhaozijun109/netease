package com.netease.easyml.uds.util

import com.netease.easyml.common.util.{IOUtil, SparkUtil}
import com.netease.easyml.local.mllib.tfserving.config.ModelBaseConfig
import com.netease.easyml.local.mllib.tokenizer.transformers.{BertTokenizer, TransformersUtil}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.internal.Logging
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, SparkSession}

import java.util.regex.Pattern
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.Try

/**
 * Created by linjiuning on 2021/9/4.
 */
object RankUtil extends Logging {
  val DAY_PATTERN = Pattern.compile("[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]")
  var BERT_TOKENIZER_BC: Broadcast[BertTokenizer] = _

  val CONFIG_LINE_SEP = "\t"
  val KEY_VAL_SEP = ":"

  val STRING_MASK = ""
  val KV_MASK = "-1:0.0"

  val USER_ID = "user_id"
  val ITEM_ID = "item_id"
  val LABEL = "label"
  val SESSION_ID = "rid"
  val POSITION = "position"
  val IS_TRAIN = "is_train"
  val DAY = "day"
  val TYPE = "type"
  val TRAIN = "train"
  val EVAL = "eval"
  val HOUR = "hour"

  val EXAMPLE = "Example"
  val EXAMPLES = "examples"
  val SERIALIZED_CONTEXT = "serialized_context"
  val SERIALIZED_EXAMPLES = "serialized_examples"

  val KV_FEA_SUFFIX_IDS = "_ids"
  val KV_FEA_SUFFIX_VALUES = "_values"

  val BERT_FEA_SUFFIX_TOKEN_IDS = "_token_ids"
  val BERT_FEA_SUFFIX_TOKEN_TYPE_IDS = "_token_type_ids"

  val FLOAT_FEA_BUCKETIZED_SUFFIX = "_bucketized"

  val MIN_COUNT = 50
  val NUM_BUCKETS = 10

  val BUCKET = "bucket"
  val EMBED = "embed"
  val BERT = "bert"
  val BERT_PAIR_SEP = "_!_"

  val RESULT_PARTITION = 200

  case class FeatureConfig(name: String, key: String, group: String, format: String, featureColumn: String, embedName: String,
                           normalizer: String, dim: Int, minCount: Int, numBucket: Int, fillna: Any, share: Boolean, isFea: Boolean,
                           isContextFea: Boolean)


  def readConfig(path: String, minCount: Int = MIN_COUNT,
                 numBucket: Int = NUM_BUCKETS,
                 nameAsEmbedName: Boolean = false): Array[FeatureConfig] = {
    val rlines = if (IOUtil.exists(path)) {
      IOUtil.readLines(path)
    } else {
      IOUtil.getResource(path)
    }
    val lines = rlines.asScala.toArray.filter(_.nonEmpty)
    parseConfig(lines, minCount = minCount, numBucket = numBucket, nameAsEmbedName = nameAsEmbedName)
  }

  def parseConfig(lines: Array[String], minCount: Int = MIN_COUNT,
                  numBucket: Int = NUM_BUCKETS,
                  nameAsEmbedName: Boolean = false): Array[FeatureConfig] = {
    val head = lines.head.split(CONFIG_LINE_SEP, -1)
    println(head.mkString("@"))
    lines.slice(1, lines.length)
      .map(line => {
        val values = line.split(CONFIG_LINE_SEP, -1)
        println(values.mkString("@"))
        val map = head.indices.map(i => (head(i), if (i < values.length) values(i) else "")).toMap
        val name = map("name")
        val group = map.getOrElse("group", map.getOrElse("user_or_item", ""))
        val format = map("format")
        val featureColumn = map("feature_column")
        val dim = Try(map("embed_dim").trim.toInt).getOrElse(0)
        val share = group.equals("user")
        val isFea = map("is_fea").equals("1")
        var key = map.getOrElse("key", "")
        if (key.isEmpty) {
          key = name
        }
        val normalizer = map.getOrElse("normalizer", "")
        var embedName = map.getOrElse("embed_name", "")
        if (nameAsEmbedName && embedName.isEmpty) {
          embedName = name
        }
        val mCnt = map.getOrElse("min_count", "")
        val mCount = if (mCnt.isEmpty) {
          if (embedName.equals(USER_ID)) {
            1
          } else {
            minCount
          }
        } else {
          mCnt.toInt
        }
        val sBucket = map.getOrElse("num_bucket", "")
        val nBucket = if (sBucket.isEmpty) numBucket else sBucket.toInt
        val fillna = getFillna(name, format, dim)
        val isContext = map.getOrElse("is_context_fea", "0").equals("1")
        val config = FeatureConfig(
          name = name,
          key = key,
          group = group,
          format = format,
          featureColumn = featureColumn,
          embedName = embedName,
          normalizer = normalizer,
          dim = dim,
          minCount = mCount,
          numBucket = nBucket,
          fillna = fillna,
          share = share,
          isFea = isFea,
          isContextFea = isContext
        )
        config
      }).filter(it => it.name.equals(it.key))
  }

  def readServerConfig(path: String): ModelBaseConfig = {
    val rlines = if (IOUtil.exists(path)) {
      IOUtil.readLines(path)
    } else {
      IOUtil.getResource(path)
    }
    ModelBaseConfig.load(rlines.asScala.mkString("\n"))
  }

  def getFillna(name: String, format: String, dim: Int): Any = {
    format match {
      case "int" => 0
      case "float" => 0.0
      case "string" => STRING_MASK
      case "list_string" => Array(STRING_MASK)
      case "nested_list_string" => Array(STRING_MASK)
      case "list_int" | "list_float" | "nested_list_int" | "kv_int" => null
      case "kv" => Array(KV_MASK)
      case _ =>
        throw new IllegalArgumentException(s"error format=$format, name=$name")
    }
  }

  def featureProcess(oldDf: DataFrame, configs: Array[FeatureConfig],
                     procFeaOnly: Boolean, maxLength: Int = 0,
                     fillNa: Boolean = true, asInt: Boolean = false): DataFrame = {
    if (BERT_TOKENIZER_BC == null && configs.exists(_.featureColumn.contains(BERT))) {
      val builder = BertTokenizer.builder
      if (maxLength > 0) {
        builder.setMaxLength(maxLength)
      }
      builder.setDoLowerCase(true)
      val tokenizer = builder.build
      BERT_TOKENIZER_BC = oldDf.sparkSession.sparkContext.broadcast(tokenizer)
    }

    var df = oldDf
    val parseKVUdf = udf(parseKV _)
    val parseKVIntUdf = udf(parseKVInt _)
    val bertUdf = udf(bertTokenize _)
    val nConfigs = if (procFeaOnly) {
      configs.filter(_.isFea)
    } else {
      configs
    }
    nConfigs.foreach(config => {
      val name = config.name
      var format = config.format
      var na = config.fillna
      if (asInt) {
        if (format.equals("kv")) {
          format = "kv_int"
        } else {
          format = format.replace("string", "int")
        }
        na = getFillna(name, format, config.dim)
      }
      val featureColumn = config.featureColumn

      val col = df(name)
      format match {
        case "int" | "float" | "string" =>
          if (fillNa)
            df = df.withColumn(name, when(col.isNull, na).otherwise(col))
          else
            df = df.withColumn(name, when(col.equalTo(na), null).otherwise(col))
        case "list_int" | "list_float" | "list_string" | "nested_list_string" | "nested_list_int" =>
          if (fillNa)
            df = df.withColumn(name, when(col.isNull.or(size(col).lt(1)), na).otherwise(col))
          else
            df = df.withColumn(name, when(col.equalTo(na).or(size(col).lt(1)), null).otherwise(col))
        case "kv" | "kv_int" =>
          val nameKv = name + "_kv"
          val nameIds = name + KV_FEA_SUFFIX_IDS
          val nameValues = name + KV_FEA_SUFFIX_VALUES

          if (format.equals("kv"))
            df = df.withColumn(nameKv, parseKVUdf(col))
          else
            df = df.withColumn(nameKv, parseKVIntUdf(col))
          df = df.withColumn(nameIds, df(nameKv).getField("keys"))
            .withColumn(nameValues, df(nameKv).getField("values"))
            .drop(nameKv).drop(name)
        case _ =>
      }
      featureColumn match {
        case BERT =>
          val nameBert = name + "_bert"
          val nameTokenIds = name + BERT_FEA_SUFFIX_TOKEN_IDS
          val nameTokenTypeIds = name + BERT_FEA_SUFFIX_TOKEN_TYPE_IDS

          df = df.withColumn(nameBert, bertUdf(df(name)))
          df = df.withColumn(nameTokenIds, df(nameBert).getField("tokenIds"))
            .withColumn(nameTokenTypeIds, df(nameBert).getField("tokenTypeIds"))
            .drop(nameBert).drop(name)
        case _ =>
      }
    })
    df
  }

  case class KVPair(keys: Seq[String], values: Seq[Double])

  case class KVIntPair(keys: Seq[Long], values: Seq[Double])

  def parseKV(kvs: Seq[String]): KVPair = {
    if (kvs == null || (kvs.length == 1 && kvs.head.isEmpty)) {
      KVPair(null, null)
    } else {
      val keys = mutable.ArrayBuffer[String]()
      val values = mutable.ArrayBuffer[Double]()
      kvs.foreach(kv => {
        try {
          val j = kv.lastIndexOf(":")
          if (j >= 0) {
            val key = kv.slice(0, j)
            val value = kv.slice(j + 1, kv.length).toDouble
            keys.append(key)
            values.append(value)
          }
        } catch {
          case _: Exception =>
            logWarning(s"error kv=$kv")
        }
      })
      KVPair(keys, values)
    }
  }

  def parseKVInt(kvs: Seq[String]): KVIntPair = {
    val pair = parseKV(kvs)
    KVIntPair(if (pair.keys == null) null else pair.keys.map(_.toLong), pair.values)
  }

  case class BertPair(tokenIds: Seq[Int], tokenTypeIds: Seq[Int])

  def bertTokenize(text: String): BertPair = {
    if (text == null) {
      BertPair(Seq(), Seq())
    } else {
      val pairs = text.split(BERT_PAIR_SEP, 2)
      val (textA, textB) = if (pairs.length == 2) {
        (pairs(0), pairs(1))
      } else {
        (pairs(0), null)
      }

      val feature = BERT_TOKENIZER_BC.value.encodePlus(textA, textB)

      val inputIds = feature.get(TransformersUtil.IndexKeys.INPUT_IDS).asInstanceOf[Array[Int]]
      val tokenTypeIds = feature.get(TransformersUtil.IndexKeys.TOKEN_TYPE_IDS).asInstanceOf[Array[Int]]

      BertPair(inputIds, tokenTypeIds)
    }
  }

  def featureConfigFromServerConfig(config: ModelBaseConfig): Array[FeatureConfig] = {
    val strConfigs = mutable.ArrayBuffer.empty[String]
    strConfigs.append(Array("name", "is_fea", "group", "format", "feature_column", "embed_dim", "is_context_fea").mkString(CONFIG_LINE_SEP))

    def append(f: ModelBaseConfig.FeatureConfig, format: String = "string", featureColumn: String = "embed"): Unit = {
      val group = if (f.getIsShared) "user" else "item"
      strConfigs.append(Array(f.getName, 1, group, format, featureColumn, f.getDim, if (f.getIsShared) 1 else 0).map(_.toString).mkString(CONFIG_LINE_SEP))
    }

    var features = config.getIntFeatures
    if (features != null) {
      features.asScala.foreach(f => append(f, "int", "numeric"))
    }
    features = config.getFloatFeatures
    if (features != null) {
      features.asScala.foreach(f => append(f, "float", "numeric"))
    }
    features = config.getStringFeatures
    if (features != null) {
      features.asScala.foreach(f => append(f))
    }
    features = config.getListFloatFeatures
    if (features != null) {
      features.asScala.foreach(f => append(f, "list_float", "numeric"))
    }
    features = config.getListStringFeatures
    if (features != null) {
      features.asScala.foreach(f => append(f, "list_string"))
    }
    features = config.getKvFeatures
    if (features != null) {
      features.asScala.foreach(f => append(f, "kv"))
    }
    features = config.getNestedListStringFeatures
    if (features != null) {
      features.asScala.foreach(f => append(f, "list_string"))
    }
    parseConfig(strConfigs.toArray)
  }

  def schemaFromConfig(configs: Array[FeatureConfig]): StructType = {
    val fields = configs.filter(_.isFea).map(config => {
      val name = config.name
      val dataType = config.format match {
        case "string" => StringType
        case "int" => IntegerType
        case "float" => DoubleType
        case "list_float" => ArrayType(DoubleType)
        case _ => ArrayType(StringType)
      }
      StructField(name, dataType)
    })
    StructType(fields)
  }


  def schemaFromServerConfig(config: ModelBaseConfig, expandKv: Boolean = false): (StructType, StructType) = {
    val contextTypes = mutable.ArrayBuffer.empty[(String, DataType)]
    val itemTypes = mutable.ArrayBuffer.empty[(String, DataType)]

    def append(name: String, isShared: Boolean, dataType: DataType): Unit = {
      val pair = (name, dataType)
      if (isShared) {
        contextTypes.append(pair)
      } else {
        itemTypes.append(pair)
      }
    }

    var features = config.getIntFeatures
    if (features != null) {
      features.asScala.foreach(f => append(f.getName, f.getIsShared, IntegerType))
    }
    features = config.getFloatFeatures
    if (features != null) {
      features.asScala.foreach(f => append(f.getName, f.getIsShared, FloatType))
    }
    features = config.getStringFeatures
    if (features != null) {
      features.asScala.foreach(f => append(f.getName, f.getIsShared, StringType))
    }
    features = config.getListFloatFeatures
    if (features != null) {
      features.asScala.foreach(f => append(f.getName, f.getIsShared, ArrayType(FloatType)))
    }
    features = config.getListStringFeatures
    if (features != null) {
      features.asScala.foreach(f => append(f.getName, f.getIsShared, ArrayType(StringType)))
    }
    features = config.getKvFeatures
    if (features != null) {
      if (expandKv) {
        features.asScala.foreach(f => {
          append(f.getName + KV_FEA_SUFFIX_IDS, f.getIsShared, ArrayType(StringType))
          append(f.getName + KV_FEA_SUFFIX_VALUES, f.getIsShared, ArrayType(DoubleType))
        })
      } else {
        features.asScala.foreach(f => append(f.getName, f.getIsShared, ArrayType(StringType)))
      }
    }
    features = config.getNestedListStringFeatures
    if (features != null) {
      features.asScala.foreach(f => append(f.getName, f.getIsShared, ArrayType(StringType)))
    }
    val contextFields = contextTypes.sortBy(_._1).map(it => StructField(it._1, it._2))
    val itemFields = itemTypes.sortBy(_._1).map(it => StructField(it._1, it._2))
    (StructType(contextFields), StructType(itemFields))
  }

  def saveTfRecord(df: DataFrame, output: String, recordType: String = "Example",
                   splitTrainEval: Boolean = true, gzip: Boolean = false, numPartitions: Int = 0,
                   multiType: Boolean = false, partitionBy: Option[Seq[String]] = None): Unit = {
    var newDf = df
    if (numPartitions > 0) {
      newDf = newDf.repartition(numPartitions)
    }
    if (splitTrainEval && newDf.columns.contains(IS_TRAIN)) {
      if (multiType) {
        newDf = newDf.withColumn(TYPE, col(IS_TRAIN).cast(StringType))
      } else {
        newDf = newDf.withColumn(TYPE, when(newDf(IS_TRAIN).cast(IntegerType).equalTo(1), TRAIN).otherwise(EVAL)).drop(IS_TRAIN)
      }
      newDf = newDf.drop(IS_TRAIN)
      val pt = Seq(TYPE) ++ partitionBy.getOrElse(Seq())
      SparkUtil.saveAsTfRecord(newDf, output, recordType, gzip, partitionBy = Some(pt))
    } else {
      SparkUtil.saveAsTfRecord(newDf, output, recordType, gzip, partitionBy = partitionBy)
    }
  }

  def loadTfRecords(spark: SparkSession, path: String, startDay: String, endDay: String, keys: String = "*", recordType: String = "Example", source: String = "tfrecord"): DataFrame = {
    var df = SparkUtil.loadFromTfRecordOfDays(spark, path, startDay = startDay, endDay = endDay, recordType = recordType, keys = keys, source = source)
    if (!df.columns.contains(IS_TRAIN) && df.columns.contains(TYPE)) {
      df = df.withColumn(IS_TRAIN, when(col(TYPE).equalTo(TRAIN), 1).otherwise(0))
    }
    df
  }

  def sql(spark: SparkSession, sql: String, env: String): DataFrame = {
    val envs = SparkUtil.parseEnv(env)
    SparkUtil.sql(spark, sql, envs)
  }

  def resolvePath(path: String): String = {
    val baseName = IOUtil.baseName(path)
    if (baseName.equals("latest")) {
      val dirname = IOUtil.parentName(path)
      val day = IOUtil.listDirectory(dirname)
        .asScala.map(it => IOUtil.baseName(it))
        .filter(it => DAY_PATTERN.matcher(it).find()).max
      IOUtil.join(dirname, day)
    } else {
      path
    }
  }

}
