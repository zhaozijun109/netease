package com.netease.easyml.ml.feature

import com.netease.easyml.common.util.{CollectionUtil, MathUtil}
import com.netease.easyml.ml.param.HasNumPartitions
import com.netease.easyml.ml.util.{FastSigmoid, SchemaUtils, Utils}
import com.tencent.angel.ml.core.optimizer.decayer.StepSizeScheduler
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.ml.param._
import org.apache.spark.ml.param.shared.{HasInputCol, HasMaxIter, HasOutputCol, HasSeed}
import org.apache.spark.ml.util.DefaultParamsWritable
import org.apache.spark.ml.{Estimator, Model}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types.{ArrayType, StringType, StructType}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 * Created by linjiuning on 2020/9/27.
 */
trait NegativeSampleBase extends Params
  with HasInputCol with HasOutputCol with HasMaxIter with HasSeed with HasNumPartitions {

  final val vectorSize = new IntParam(
    this, "vectorSize", "Dimensionality of the word vectors.",
    ParamValidators.gt(0))

  def getVectorSize: Int = $(vectorSize)

  final val window = new IntParam(
    this, "window", "Maximum distance between the current and predicted word within a sentence.",
    ParamValidators.gt(0))

  def getWindow: Int = $(window)

  final val minCount = new IntParam(this, "minCount", "Ignores all words with total frequency lower than this.", ParamValidators.gtEq(0))

  def getMinCount: Int = $(minCount)

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

  final val alpha = new DoubleParam(this, "alpha", "The initial learning rate.", ParamValidators.gt(0))

  def getAlpha: Double = $(alpha)

  final val minAlpha = new DoubleParam(this, "minAlpha", "Learning rate will linearly drop to `min_alpha` as training progresses.")

  def getMinAlpha: Double = $(minAlpha)

  final val maxVocabSize = new IntParam(this, "maxVocabSize",
    "Limits the RAM during vocabulary building; if there are more unique " +
      "words than this, then prune the infrequent ones. Every 10 million word types need about 1GB of RAM. " +
      "Set to `None` for no limit.")

  def getMaxVocabSize: Int = $(maxVocabSize)

  //  final val maxFinalSize = new IntParam(this, "maxFinalSize",
  //    "Limits the vocab to a target vocab size by automatically picking a matching min_count. If the specified " +
  //      "min_count is more than the calculated min_count, the specified min_count will be used. " +
  //      "Set to `None` if not required.")
  //
  //  def getMaxFinalSize: Int = $(maxFinalSize)

  final val sample = new DoubleParam(this, "sample",
    "The threshold for configuring which higher-frequency words are randomly downsampled, " +
      "useful range is (0, 1e-5).")

  def getSample: Double = $(sample)

  final val sortedVocab = new BooleanParam(this, "sortedVocab",
    "If 1, sort the vocabulary by descending frequency before assigning word indexes.")

  def getSortedVocab: Boolean = $(sortedVocab)

  final val batchWords = new IntParam(this, "batchWords",
    "Target size (in words) for batches of examples.", ParamValidators.gt(0))

  def getBatchWords: Int = $(batchWords)

  final val computeLoss = new BooleanParam(this, "computeLoss",
    "If True, computes and stores loss value.")

  def getComputeLoss: Boolean = $(computeLoss)

  final val maxSentenceLength = new IntParam(this, "maxSentenceLength", "Maximum length " +
    "(in words) of each sentence in the input data. Any sentence longer than this threshold will " +
    "be divided into chunks up to the size (> 0)", ParamValidators.gt(0))

  def getMaxSentenceLength: Int = $(maxSentenceLength)

  final val shuffle = new BooleanParam(this, "shuffle",
    "If True, shuffle sentence every epoch.")

  def getShuffle: Boolean = $(shuffle)

  final val batchSize = new IntParam(this, "batchSize",
    "Target size (in sentences) for batches of examples.", ParamValidators.gt(0))

  def getBatchSize: Int = $(batchSize)

  final val decayRate = new DoubleParam(this, "decayRate", "The decay rate of initial learning rate.", ParamValidators.gt(0))

  def getDecayRate: Double = $(decayRate)

  final val randomSample = new BooleanParam(this, "randomSample",
    "Whether do negative sample randomly or based on word frequency, default false.")

  def getRandomSample: Boolean = $(randomSample)

  val sliceSave = new BooleanParam(this, "sliceSave", "Whether do slice save rather than native WordVecModel save.")

  def getSliceSave: Boolean = $(sliceSave)

  setDefault(vectorSize -> 100, window -> 5, minCount -> 5, sample -> 1e-3, seed -> 1, alpha -> 0.025, minAlpha -> 0.0001, negative -> 5,
    nsExponent -> 0.75, sortedVocab -> true, batchWords -> 10000, maxIter -> 5, computeLoss -> true, numPartitions -> 1, shuffle -> false,
    batchSize -> Int.MaxValue, randomSample -> false, sliceSave -> true)

  /**
   * Validate and transform the input schema.
   */
  protected def validateAndTransformSchema(schema: StructType): StructType = {
    val typeCandidates = List(new ArrayType(StringType, true), new ArrayType(StringType, false))
    SchemaUtils.checkColumnTypes(schema, $(inputCol), typeCandidates)
    SchemaUtils.appendColumn(schema, $(outputCol), SchemaUtils.vectorUDT)
  }
}

abstract class NegativeSample[M <: Model[M]] extends Estimator[M] with NegativeSampleBase with DefaultParamsWritable {

  def setInputCol(value: String): this.type = set(inputCol, value)

  def setOutputCol(value: String): this.type = set(outputCol, value)

  def setVectorSize(value: Int): this.type = set(vectorSize, value)

  def setWindow(value: Int): this.type = set(window, value)

  def setAlpha(value: Double): this.type = set(alpha, value)

  def setMinAlpha(value: Double): this.type = set(minAlpha, value)

  def setNumPartitions(value: Int): this.type = set(numPartitions, value)

  def setMaxIter(value: Int): this.type = set(maxIter, value)

  def setSeed(value: Long): this.type = set(seed, value)

  def setMinCount(value: Int): this.type = set(minCount, value)

  def setNegative(value: Int): this.type = set(negative, value)

  def setNSExponent(value: Double): this.type = set(nsExponent, value)

  def setMaxVocabSize(value: Int): this.type = set(maxVocabSize, value)

  //  def setMaxFinalSize(value: Int): this.type = set(maxFinalSize, value)

  def setSample(value: Double): this.type = set(sample, value)

  def setSortedVocab(value: Boolean): this.type = set(sortedVocab, value)

  def setComputeLoss(value: Boolean): this.type = set(computeLoss, value)

  def setBatchWords(value: Int): this.type = set(batchWords, value)

  def setMaxSentenceLength(value: Int): this.type = set(maxSentenceLength, value)

  def setShuffle(value: Boolean): this.type = set(shuffle, value)

  def setBatchSize(value: Int): this.type = set(batchSize, value)

  def setDecayRate(value: Double): this.type = set(decayRate, value)

  def setRandomSample(value: Boolean): this.type = set(randomSample, value)

  def setSliceSave(value: Boolean): this.type = set(sliceSave, value)

  protected val EXP_TABLE_SIZE = 1000
  protected val MAX_EXP = 6

  protected var trainWordsCount = 0L
  protected var vocabSize = 0
  @transient protected var vocab: Array[String] = _
  @transient protected var vocabCnt: Array[Long] = _
  @transient protected var vocabHash: mutable.HashMap[String, Int] = _
  @transient protected var sampleInt: Array[Int] = _
  @transient protected var cumTable: Array[Int] = _
  @transient protected var expTable: FastSigmoid = _

  @transient protected var scheduler: StepSizeScheduler = _

  protected def learnVocab[S <: Iterable[String]](dataset: RDD[S]): Unit = {
    val words = dataset.flatMap(x => x)

    var vocabCnt_ = words.map(w => (w, 1L))
      .reduceByKey(_ + _)
      .filter(_._2 >= $(minCount))
      .collect()

    trainWordsCount = vocabCnt_.map(_._2).sum

    if ($(sortedVocab) || isSet(maxVocabSize)) {
      vocabCnt_ = vocabCnt_.sortBy(_._2)(Ordering[Long].reverse)
    }

    if (isSet(maxVocabSize) && vocabCnt_.length > $(maxVocabSize)) {
      vocabCnt_ = vocabCnt_.slice(0, $(maxVocabSize))
    }

    vocab = vocabCnt_.map(_._1)
    vocabCnt = vocabCnt_.map(_._2)

    vocabSize = vocab.length
    require(vocabSize > 0, "The vocabulary size should be > 0. You may need to check " +
      "the setting of minCount, which could be large enough to remove all your words in sentences.")
    if (vocabSize.toLong * $(vectorSize) >= Int.MaxValue) {
      throw new RuntimeException("Please increase minCount or decrease vectorSize in Word2Vec" +
        " to avoid an OOM. You are highly recommended to make your vocabSize*vectorSize, " +
        "which is " + vocabSize + "*" + $(vectorSize) + " for now, less than `Int.MaxValue`.")
    }

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

    vocabHash = mutable.HashMap.empty[String, Int]
    var a = 0
    while (a < vocabSize) {
      vocabHash += vocab(a) -> a
      a += 1
    }
    println(s"vocabSize = $vocabSize, trainWordsCount = $trainWordsCount")
  }

  protected def createExpTable(): Unit = {
    expTable = new FastSigmoid(EXP_TABLE_SIZE, MAX_EXP)
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

  protected def convertToId[S <: Iterable[String]](dataset: RDD[S], bcVocabHash: Broadcast[mutable.HashMap[String, Int]]): RDD[Array[Int]] = {
    // each partition is a collection of sentences,
    // will be translated into arrays of Index integer
    val sentences: RDD[Array[Int]] = dataset.mapPartitions { sentenceIter =>
      // Each sentence will map to 0 or more Array[Int]
      sentenceIter.flatMap { sentence =>
        // Sentence of words, some of which map to a word index
        val wordIndexes = sentence.flatMap(bcVocabHash.value.get)
        // break wordIndexes into trunks of maxSentenceLength when has more
        if (isSet(maxSentenceLength))
          wordIndexes.grouped($(maxSentenceLength)).map(_.toArray)
        else
          Iterator.single(wordIndexes.toArray)
      }
    }
    sentences.repartition($(numPartitions))
  }

  protected def batch(k: Int,
                      newSentences: RDD[Array[Int]],
                      bcCumTable: Broadcast[Array[Int]],
                      bcSampleInt: Broadcast[Array[Int]]): RDD[Batch] = {
    // $() will slow down the speed
    val maxIter = getMaxIter
    val seed = getSeed
    val batchWords = getBatchWords
    val numPartitions = getNumPartitions
    val minAlpha = getMinAlpha
    val lr = getAlpha
    val sample = getSample
    val window = getWindow
    val negative = getNegative
    val shuffle = getShuffle
    val batchSize = getBatchSize
    val randomSample = getRandomSample

    val scheduleAlpha = if (scheduler != null) {
      if (k == 1) Some(getAlpha) else Some(scheduler.next())
    } else {
      None
    }

    val totalWordsCounts = maxIter * trainWordsCount + 1
    val numWordsProcessedInPreviousIterations = (k - 1) * trainWordsCount
    newSentences.mapPartitionsWithIndex { case (idx, iter) =>
      val random = Utils.newXORShiftRandom(seed ^ ((idx + 1) << 16) ^ ((-k - 1) << 8))
      var wordCount = 0L
      var allWordCount = 0L
      var batch = ArrayBuffer.empty[Array[Int]]

      var alpha = if (scheduleAlpha.nonEmpty) {
        scheduleAlpha.get
      } else {
        lr - (lr - minAlpha) * numWordsProcessedInPreviousIterations / totalWordsCounts
      }
      if (alpha < minAlpha) alpha = minAlpha

      var batches = iter.flatMap(sentence => {
        if (shuffle) {
          MathUtil.shuffle(sentence)
        }
        allWordCount += sentence.length
        if (wordCount + sentence.length <= batchWords && batch.length < batchSize) {
          batch += sentence
          wordCount += sentence.length
          None
        } else {
          val job = Some((alpha, batch))
          alpha = if (scheduleAlpha.nonEmpty) {
            scheduleAlpha.get
          } else {
            lr - (lr - minAlpha) * (numPartitions * allWordCount.toDouble + numWordsProcessedInPreviousIterations) / totalWordsCounts
          }
          if (alpha < minAlpha) alpha = minAlpha
          logInfo(s"wordCount = ${allWordCount + numWordsProcessedInPreviousIterations}, " +
            s"epochWordCount = $allWordCount, " + s"alpha = $alpha")

          batch = ArrayBuffer.empty[Array[Int]]
          batch += sentence
          wordCount = sentence.length
          job
        }
      })

      if (batch.nonEmpty) {
        batches ++= Seq((alpha, batch))
      }

      batches.map { case (alpha, sentences) =>
        var srcIdx = ArrayBuffer.empty[Int]
        var dstIdx = ArrayBuffer.empty[Int]
        var negIdx = ArrayBuffer.empty[Array[Int]]
        sentences.foreach(sentence => {
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
                val nsSamples = Array.tabulate(negative)(_ => {
                  if (randomSample) {
                    random.nextInt(vocabSize)
                  } else {
                    val ni = random.nextInt(Int.MaxValue)
                    CollectionUtil.searchsorted(bcCumTable.value, ni)
                  }
                }).filter(index => index != predict)
                srcIdx += context
                dstIdx += predict
                negIdx += nsSamples
              }
            }
            i += 1
          }
        })
        Batch(srcIdx.toArray, dstIdx.toArray, negIdx.toArray, alpha)
      }
    }
  }

  override def transformSchema(schema: StructType): StructType = {
    validateAndTransformSchema(schema)
  }
}

