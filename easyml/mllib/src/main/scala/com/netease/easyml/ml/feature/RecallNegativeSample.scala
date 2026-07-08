package com.netease.easyml.ml.feature

import com.netease.easyml.common.util.CollectionUtil
import com.netease.easyml.ml.util.{SchemaUtils, Utils}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.ml.Transformer
import org.apache.spark.ml.param._
import org.apache.spark.ml.param.shared.{HasInputCol, HasMaxIter, HasSeed}
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types.{ArrayType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, Dataset, Row}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 * Created by linjiuning on 2020/9/27.
 */
trait RecallNegativeSampleBase extends Params
  with HasInputCol with HasMaxIter with HasSeed {

  final val negative = new IntParam(this, "negative", "the int for negative specifies how many \"noise words\" " +
    "should be drawn (usually between 5-20).", ParamValidators.gt(0))

  def getNegative: Int = $(negative)

  final val nsExponent = new DoubleParam(this, "nsExponent",
    "The exponent used to shape the negative sampling distribution. A value of 1.0 samples exactly in proportion " +
      "to the frequencies, 0.0 samples all words equally, while a negative value samples low-frequency words more " +
      "than high-frequency words. The popular default value of 0.75 was chosen by the original Word2Vec paper. " +
      "More recently, in https://arxiv.org/abs/1804.04212, Caselles-Dupré, Lesaint, & Royo-Letelier suggest that " +
      "other values may perform better for recommendation applications.")

  def getNSExponent: Double = $(nsExponent)

  final val sample = new DoubleParam(this, "sample",
    "The threshold for configuring which higher-frequency words are randomly downsampled, " +
      "useful range is (0, 1e-5).")

  def getSample: Double = $(sample)

  final val randomSample = new BooleanParam(this, "randomSample",
    "Whether do negative sample randomly or based on word frequency, default false.")

  def getRandomSample: Boolean = $(randomSample)

  setDefault(sample -> 1e-3, seed -> 1, negative -> 5, nsExponent -> 0.75, maxIter -> 1, randomSample -> false)

  /**
   * Validate and transform the input schema.
   */
  protected def validateAndTransformSchema(schema: StructType): StructType = {
    SchemaUtils.checkArrayType(schema, $(inputCol))
    val rawFields = schema.fields.filterNot(_.name.equals($(inputCol)))

    val elementType = schema($(inputCol)).dataType.asInstanceOf[ArrayType].elementType
    val positive = StructField("positive", ArrayType(elementType), nullable = false)
    val negative = StructField("negative", ArrayType(ArrayType(elementType)), nullable = false)
    StructType(rawFields ++ Array(positive, negative))
  }
}

class RecallNegativeSample(override val uid: String) extends Transformer with RecallNegativeSampleBase with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("rank_ns"))

  def setInputCol(value: String): this.type = set(inputCol, value)

  def setMaxIter(value: Int): this.type = set(maxIter, value)

  def setSeed(value: Long): this.type = set(seed, value)

  def setNegative(value: Int): this.type = set(negative, value)

  def setNSExponent(value: Double): this.type = set(nsExponent, value)

  def setSample(value: Double): this.type = set(sample, value)

  def setRandomSample(value: Boolean): this.type = set(randomSample, value)

  protected var vocabSize = 0
  @transient protected var vocab: Array[Any] = _
  @transient protected var vocabCnt: Array[Long] = _
  @transient protected var vocabHash: mutable.HashMap[Any, Int] = _
  @transient protected var sampleInt: Array[Int] = _
  @transient protected var cumTable: Array[Int] = _

  protected def learnVocab[S <: Iterable[Any]](dataset: RDD[S]): Unit = {
    val words = dataset.flatMap(x => x)

    val vocabCnt_ = words.map(w => (w, 1L))
      .reduceByKey(_ + _)
      .collect()

    val trainWordsCount = vocabCnt_.map(_._2).sum

    vocab = vocabCnt_.map(_._1)
    vocabCnt = vocabCnt_.map(_._2)

    vocabSize = vocab.length
    require(vocabSize > 0, "The vocabulary size should be > 0. You may need to check " +
      "the setting of minCount, which could be large enough to remove all your words in sentences.")

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

      sampleInt = new Array[Int](vocabSize)
      Array.tabulate(vocabSize)(i => {
        val v = vocabCnt(i)
        val wordProbability = Math.min(1.0, (Math.sqrt(v / thresholdCount) + 1) * (thresholdCount / v))
        sampleInt(i) = Math.round(wordProbability * (Int.MaxValue - 1)).toInt
      })
    }

    vocabHash = mutable.HashMap.empty[Any, Int]
    var a = 0
    while (a < vocabSize) {
      vocabHash += vocab(a) -> a
      a += 1
    }
    println(s"vocabSize = $vocabSize, trainWordsCount = $trainWordsCount")
  }


  protected def makeCumTable(domain: Int = Int.MaxValue - 1): Unit = {
    cumTable = new Array[Int](vocabSize)
    var trainWordsPow = 0.0
    Array.tabulate(vocabSize)(i => {
      trainWordsPow += Math.pow(vocabCnt(i), $(nsExponent))
    })

    var cumulative = 0.0
    Array.tabulate(vocabSize)(i => {
      cumulative += Math.pow(vocabCnt(i), $(nsExponent))
      cumTable(i) = Math.round(cumulative / trainWordsPow * domain).toInt
    })

    if (cumTable.nonEmpty) {
      require(cumTable.last == domain)
    }
  }

  protected def batch(k: Int,
                      dataset: Dataset[_],
                      bcCumTable: Broadcast[Array[Int]],
                      bcSampleInt: Broadcast[Array[Int]],
                      bcVocabHash: Broadcast[mutable.HashMap[Any, Int]],
                      bcVocab: Broadcast[Array[Any]]): RDD[Row] = {
    // $() will slow down the speed
    val seed = getSeed
    val sample = getSample
    val negative = getNegative
    val randomSample = getRandomSample
    val inputCol = getInputCol

    dataset.toDF.rdd.mapPartitionsWithIndex { case (idx, iter) =>
      val random = Utils.newXORShiftRandom(seed ^ ((idx + 1) << 16) ^ ((-k - 1) << 8))

      iter.map(row => {
        var pos = ArrayBuffer.empty[Any]
        var neg = ArrayBuffer.empty[Array[Any]]

        val sentence = row.getAs[Seq[Any]](inputCol)
        val spSentence = if (sample > 0) {
          sentence.filter(wd => {
            val i = bcVocabHash.value(wd)
            bcSampleInt.value(i) >= random.nextInt(Int.MaxValue)
          })
        } else {
          sentence
        }

        var i = 0
        while (i < spSentence.length) {
          val p = spSentence(i)
          val nsSamples = Array.tabulate(negative)(_ => {
            if (randomSample) {
              random.nextInt(vocabSize)
            } else {
              val ni = random.nextInt(Int.MaxValue)
              val i = CollectionUtil.searchsorted(bcCumTable.value, ni)
              bcVocab.value(i)
            }
          })

          pos += p
          neg += nsSamples

          i += 1
        }
        val idx = row.fieldIndex(inputCol)
        var seq = row.toSeq
        seq = seq.slice(0, idx) ++ seq.slice(idx + 1, seq.length) ++ Seq(pos.toArray, neg.toArray)
        Row.fromSeq(seq)
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

    if (!$(randomSample)) {
      makeCumTable()
    }

    val spark = dataset.sparkSession
    val sc = spark.sparkContext

    val bcVocab = sc.broadcast(vocab)
    val bcVocabHash = sc.broadcast(vocabHash)
    val bcCumTable = if (cumTable != null) {
      sc.broadcast(cumTable)
    } else {
      null
    }
    val bcSampleInt = if (sampleInt != null) {
      sc.broadcast(sampleInt)
    } else {
      null
    }

    try {
      val rdds = (1 to getMaxIter).map(k => {
        batch(k, dataset, bcCumTable, bcSampleInt, bcVocabHash, bcVocab)
      })

      val rdd = spark.sparkContext.union(rdds)
      spark.createDataFrame(rdd, structType)
    } finally {
      //      bcVocab.destroy()
      //      bcVocabHash.destroy()
      //      if (bcCumTable != null) {
      //        bcCumTable.destroy()
      //      }
      //      if (bcSampleInt != null) {
      //        bcSampleInt.destroy()
      //      }
    }
  }

  override def copy(extra: ParamMap): Transformer = defaultCopy(extra)
}

object RecallNegativeSample extends DefaultParamsReadable[RecallNegativeSample] {
  override def load(path: String): RecallNegativeSample = super.load(path)
}