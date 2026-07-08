package com.netease.easyml.ml.nlp

import java.util
import java.util.function.Supplier
import java.util.regex.Pattern

import com.hankcs.hanlp.collection.trie.DoubleArrayTrie
import com.netease.easyml.common.resource.ResourceManager
import com.netease.easyml.common.util.StringUtil
import com.netease.easyml.ml.param.{HasLowercase, HasNumPartitions}
import com.netease.easyml.ml.sklearn.feature_extraction.TextUtil
import com.netease.easyml.ml.util.SchemaUtils
import org.apache.spark.ml.Model
import org.apache.spark.ml.param._
import org.apache.spark.ml.param.shared.HasInputCol
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}
import org.apache.spark.storage.StorageLevel

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

/**
 * Created by linjiuning on 2020/8/21.
 * 基于凝固度的新词发现算法
 * https://spaces.ac.cn/archives/6920
 * https://github.com/bojone/word-discovery
 */

trait WordDiscoveryParams extends Params with HasInputCol with HasLowercase with HasNumPartitions {

  val ngram: IntParam = new IntParam(this, "ngram", "ngram", ParamValidators.gt(1))

  def getNgram: Int = $(ngram)

  val minCount: IntParam = new IntParam(this, "minCount", "min frequency count", ParamValidators.gtEq(0))

  def getMinCount: Int = $(minCount)

  val minPMI: DoubleArrayParam = new DoubleArrayParam(this, "minPMI", "min pmi for each ngram")

  def getMinPMI: Array[Double] = $(minPMI)

  val minLength: IntParam = new IntParam(this, "minLength", "min length of vocab")

  def getMinLength: Int = $(minLength)

  setDefault(lowercase -> true, ngram -> 4, minPMI -> Array(0, 2, 4, 6), minCount -> 32, minLength -> 1)

  protected def validateAndTransformSchema(schema: StructType): StructType = {
    SchemaUtils.checkColumnTypes(schema, $(inputCol), Array(StringType, ArrayType(StringType)))
    StructType(Seq(StructField("word", StringType),
      StructField("count", LongType)))
  }
}

class WordDiscovery(override val uid: String) extends Model[WordDiscovery] with WordDiscoveryParams with DefaultParamsWritable {

  import WordDiscovery._

  def this() = this(Identifiable.randomUID("wordDiscovery"))

  def setInputCol(value: String): this.type = set(inputCol, value)

  def setLowercase(value: Boolean): this.type = set(lowercase, value)

  def setNumPartitions(value: Int): this.type = set(numPartitions, value)

  def setNgram(value: Int): this.type = set(ngram, value)

  def setMinCount(value: Int): this.type = set(minCount, value)

  def setMinPMI(value: Array[Double]): this.type = set(minPMI, value)

  def setMinLength(value: Int): this.type = set(minLength, value)

  def countNgram(dataset: Dataset[_], charLevel: Boolean): RDD[(String, Long)] = {
    val ngram = getNgram
    val lowercase = getLowercase
    val minCount = getMinCount

    val regexCl = Pattern.compile("[^\\u4e00-\\u9fa50-9a-zA-Z ]+")

    dataset.select($(inputCol)).rdd.flatMap(row => {
      var tokens = if (charLevel) {
        var text = row.getString(0)
        text = regexCl.matcher(text).replaceAll("\n")
        TextUtil.charNgram(text, 1, ngram)
      } else {
        var words = row.getAs[Seq[String]](0)
        words = words.map(it => regexCl.matcher(it).replaceAll("\n"))
        TextUtil.wordNgram(words, 1, ngram)
      }
      tokens = tokens.map(StringUtil.strip)
        .filterNot(_.isEmpty)
      if (lowercase)
        tokens.map(it => it.toLowerCase())
      else
        tokens
    }).map((_, 1L))
      .reduceByKey(_ + _)
      .filter(_._2 >= minCount)
  }

  def filterNgram(spark: SparkSession, counter: RDD[(String, Long)], charLevel: Boolean, sep: String): Array[String] = {
    counter.persist(StorageLevel.MEMORY_AND_DISK)
    val count = counter.filter(it => it._1.split(sep).length == 1).map(_._2).sum().toLong
    import spark.implicits._

    val splitRdd = counter.map {
      case (word, cnt) =>
        (word.split(sep), cnt)
    }

    val ngram = getNgram
    val minPMI = getMinPMI

    val singleDf = splitRdd.filter(_._1.length < ngram)
      .map(it => (it._1.mkString(sep), it._2))
      .toDF("s_wd", "s_cnt")

    singleDf.createOrReplaceTempView("single")

    var pairDf = splitRdd.filter(_._1.length > 1)
      .flatMap {
        case (tokens, cnt) =>
          (1 until tokens.length).map(i => {
            (tokens.slice(0, i).mkString(sep), tokens.slice(i, tokens.length).mkString(sep), cnt)
          })
      }
      .toDF("p_wd1", "p_wd2", "p_cnt")

    if (isSet(numPartitions)) {
      pairDf = pairDf.repartition($(numPartitions))
    }

    pairDf.createOrReplaceTempView("pair")

    spark.sql(s"select pair.*, coalesce(single.s_cnt, $count) as s_cnt1 from pair left join single on pair.p_wd1=single.s_wd")
      .createOrReplaceTempView("pair")

    val joinDf = spark.sql(s"select pair.*, coalesce(single.s_cnt, $count) as s_cnt2 from pair left join single on pair.p_wd2=single.s_wd")

    val ngrams = joinDf.rdd
      .map {
        case Row(wd1: String, wd2: String, cnt: Long, cnt1: Long, cnt2: Long) =>
          val pmi = Math.log(count * cnt * 1.0 / (cnt1 * cnt2))
          (wd1 + sep + wd2, pmi)
      }.reduceByKey((it1, it2) => Math.min(it1, it2))
      .filter(it => {
        val word = it._1
        val pmi = it._2
        val ngram = if (charLevel)
          word.length
        else
          word.split(sep).length
        pmi >= minPMI(ngram - 1)
      }).map(_._1).collect()
    counter.unpersist()
    ngrams
  }

  def getCandidates(dataset: Dataset[_], ngrams: Array[String], charLevel: Boolean, sep: String): RDD[(String, Long)] = {
    val minCount = getMinCount
    val minLength = getMinLength
    val ngramsBc = dataset.sparkSession.sparkContext.broadcast(ngrams)
    dataset.select($(inputCol)).rdd.mapPartitions(iter => {
      val trie = getOrCreateTrie(uid, ngramsBc.value)
      iter.flatMap(row => {
        val text = if (charLevel) {
          row.getString(0)
        } else {
          row.getAs[Seq[String]](0).mkString(sep)
        }
        var start = 0
        var end = 1
        val result = new ArrayBuffer[String]
        for (i <- text.indices) {
          if (i == end) {
            result += text.substring(start, end)
            start = i
            end = i + 1
          }
          val endIds = trie.commonPrefixSearch(text, i, 0, 0).asScala
          if (endIds.nonEmpty) {
            val len = endIds.map(i => trie.getValueAt(i).length).max
            end = Math.max(end, len + i)
          }
        }
        result.toIterator
      })
    }).filter(it => if (charLevel) it.length >= minLength else it.split(sep).length >= minLength)
      .map((_, 1L))
      .reduceByKey(_ + _)
      .filter(_._2 >= minCount)
  }

  def filterVocab(ngrams: Array[String], candidates: RDD[(String, Long)], charLevel: Boolean, sep: String): RDD[(String, Long)] = {
    val ngram = getNgram
    val ngramsSet = ngrams.toSet
    candidates.filter {
      case (word, _) =>
        if (charLevel) {
          if (word.length < 3) true
          else if (word.length <= ngram && ngramsSet.contains(word)) true
          else word.sliding(ngram).forall(ngramsSet.contains)
        } else {
          val units = word.split(sep)
          if (units.length < 3) true
          else if (units.length <= ngram && ngramsSet.contains(word)) true
          else units.sliding(ngram).forall(it => ngramsSet.contains(it.mkString(sep)))
        }
    }
  }

  override def transform(dataset: Dataset[_]): DataFrame = {
    val outputSchema = transformSchema(dataset.schema, logging = true)

    val handlePersistence = dataset.storageLevel == StorageLevel.NONE

    if (handlePersistence) dataset.persist(StorageLevel.MEMORY_AND_DISK)

    val charLevel = SchemaUtils.isStringType(dataset.schema, $(inputCol))
    val sep = if (charLevel) "" else " "

    // step1: ngram counter
    val counter = countNgram(dataset, charLevel)
    // step2: filter "weak" ngrams by pmi
    val ngrams = filterNgram(dataset.sparkSession, counter, charLevel, sep)
    // step3: get candidate based on tokenize
    var candidates = getCandidates(dataset, ngrams, charLevel, sep)
    // step4: backtrack to filter
    candidates = filterVocab(ngrams, candidates, charLevel, sep)
    if (!charLevel) {
      candidates = candidates.map {
        case (word, cnt) =>
          (word.replaceAll(sep, ""), cnt)
      }
    }
    val rdd = candidates.map(it => Row.fromTuple(it))
    val newDs = dataset.sparkSession.createDataFrame(rdd, outputSchema)

    if (handlePersistence) dataset.unpersist()
    newDs
  }

  override def copy(extra: ParamMap): WordDiscovery = defaultCopy(extra)

  override def transformSchema(schema: StructType): StructType = validateAndTransformSchema(schema)
}

object WordDiscovery extends DefaultParamsReadable[WordDiscovery] {
  def getOrCreateTrie(uid: String, words: Array[String]): DoubleArrayTrie[String] = {
    ResourceManager.getOrCreate(uid, new Supplier[DoubleArrayTrie[String]] {
      override def get(): DoubleArrayTrie[String] = {
        val trie = new DoubleArrayTrie[String]()
        val map = new util.TreeMap[String, String]
        for (key <- words) {
          map.put(key, key)
        }
        trie.build(map)
        trie
      }
    })
  }

  override def load(path: String): WordDiscovery = super.load(path)
}