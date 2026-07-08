package com.netease.easyml.ml.feature

import com.netease.easyml.ml.util.{SchemaUtils, Utils}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.ml.Transformer
import org.apache.spark.ml.param._
import org.apache.spark.ml.param.shared.{HasInputCol, HasSeed}
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types.{ArrayType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, Dataset, Row}
import org.apache.spark.util.collection_.OpenHashMap

import scala.collection.mutable.ArrayBuffer

/**
 * Created by linjiuning on 2020/12/28.
 */
trait SkipGramBase extends Params with HasInputCol with HasSeed {

  final val window = new IntParam(
    this, "window", "Maximum distance between the current and predicted word within a sentence.",
    ParamValidators.gt(0))

  def getWindow: Int = $(window)

  final val sample = new DoubleParam(this, "sample",
    "The threshold for configuring which higher-frequency words are randomly downsampled, " +
      "useful range is (0, 1e-5).")

  def getSample: Double = $(sample)

  final val maxSentenceLength = new IntParam(this, "maxSentenceLength", "Maximum length " +
    "(in words) of each sentence in the input data. Any sentence longer than this threshold will " +
    "be divided into chunks up to the size (> 0)", ParamValidators.gt(0))

  def getMaxSentenceLength: Int = $(maxSentenceLength)

  setDefault(window -> 5, sample -> 1e-3, seed -> 1)

  /**
   * Validate and transform the input schema.
   */
  protected def validateAndTransformSchema(schema: StructType): StructType = {
    //    val typeCandidates = List(new ArrayType(IntegerType, true), new ArrayType(IntegerType, false))
    //    SchemaUtils.checkColumnTypes(schema, $(inputCol), typeCandidates)
    SchemaUtils.checkArrayType(schema, $(inputCol))

    val elementType = schema($(inputCol)).dataType.asInstanceOf[ArrayType].elementType
    val fields = Array("src", "dst").map(col => StructField(col, elementType, nullable = false))
    StructType(fields)
  }
}

class SkipGram(override val uid: String) extends Transformer with SkipGramBase with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("skipGram"))

  def setInputCol(value: String): this.type = set(inputCol, value)

  def setWindow(value: Int): this.type = set(window, value)

  def setSeed(value: Long): this.type = set(seed, value)

  def setSample(value: Double): this.type = set(sample, value)

  def setMaxSentenceLength(value: Int): this.type = set(maxSentenceLength, value)

  protected var trainWordsCount = 0L
  @transient protected var sampleInt: OpenHashMap[Any, Int] = _

  protected def learnVocab[S <: Iterable[Any]](dataset: RDD[S]): Unit = {
    val words = dataset.flatMap(x => x)

    val vocabCnt_ = words.map(w => (w, 1L))
      .reduceByKey(_ + _)
      .collect()

    trainWordsCount = vocabCnt_.map(_._2).sum

    if ($(sample) > 0.0) {
      // Precalculate each vocabulary item's threshold for sampling
      val thresholdCount = if ($(sample) == 0.0) {
        // no words downsampled
        trainWordsCount.toDouble
      } else if ($(sample) < 1.0) {
        // traditional meaning: set parameter as proportion of total
        trainWordsCount * $(sample)
      } else {
        // new shorthand: sample >= 1 means downsample all words with higher count than sample
        $(sample) * (3 + Math.sqrt(5)) / 2
      }

      sampleInt = new OpenHashMap[Any, Int]()

      vocabCnt_.foreach {
        case (w, v) =>
          val wordProbability = Math.min(1.0, (Math.sqrt(v / thresholdCount) + 1) * (thresholdCount / v))
          val c = Math.round(wordProbability * (Int.MaxValue - 1)).toInt
          sampleInt.update(w, c)
      }
    }

    println(s"trainWordsCount = $trainWordsCount")
  }

  protected def skipGram(newSentences: RDD[Seq[Any]],
                         bcSampleInt: Broadcast[OpenHashMap[Any, Int]]): RDD[(Any, Any)] = {
    val seed = getSeed
    val sample = getSample
    val window = getWindow

    val newSentences_ = if (isSet(maxSentenceLength)) {
      val length = getMaxSentenceLength
      newSentences.flatMap(arr => arr.grouped(length))
    } else {
      newSentences
    }

    newSentences_.mapPartitionsWithIndex { case (idx, iter) =>
      val random = Utils.newXORShiftRandom(seed ^ ((idx + 1) << 16) ^ ((-1) << 8))

      iter.flatMap(sentence => {
        val srcAndDst = ArrayBuffer.empty[(Any, Any)]

        val spSentence = if (sample > 0) {
          sentence.filter(i => bcSampleInt.value(i) >= random.nextInt(Int.MaxValue))
        } else {
          sentence
        }

        var i = 0
        while (i < spSentence.length) {
          val predict = spSentence(i)
          val reduceWindow = random.nextInt(window)

          val j = Math.max(0, i - window + reduceWindow)
          val k = Math.min(spSentence.length, i + window + 1 - reduceWindow)
          for (t <- j until k) {
            if (t != i) {
              val context = spSentence(t)
              srcAndDst.append((context, predict))
            }
          }
          i += 1
        }
        srcAndDst.result()
      })
    }
  }

  override def transformSchema(schema: StructType): StructType = {
    validateAndTransformSchema(schema)
  }

  override def transform(dataset: Dataset[_]): DataFrame = {
    val structType = transformSchema(dataset.schema, logging = true)
    val input = dataset.select($(inputCol)).rdd.map(_.getAs[Seq[Any]](0))
    learnVocab(input)

    val spark = dataset.sparkSession
    val bcSampleInt = if (sampleInt != null) {
      spark.sparkContext.broadcast(sampleInt)
    } else {
      null
    }
    val rdd = skipGram(input, bcSampleInt).map(Row.fromTuple)

    spark.createDataFrame(rdd, structType)
  }

  override def copy(extra: ParamMap): Transformer = defaultCopy(extra)
}

object SkipGram extends DefaultParamsReadable[SkipGram] {
  override def load(path: String): SkipGram = super.load(path)
}