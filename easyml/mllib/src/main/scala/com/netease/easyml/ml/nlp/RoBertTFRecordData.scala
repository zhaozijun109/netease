package com.netease.easyml.ml.nlp

import java.util.Random
import java.util.function.Supplier

import com.netease.easyml.common.resource.ResourceManager
import com.netease.easyml.common.util.{MathUtil, StringUtil}
import com.netease.easyml.local.mllib.tokenizer.JiebaTokenizer
import com.netease.easyml.local.mllib.tokenizer.transformers.BertTokenizer
import com.netease.easyml.ml.param.{HasLowercase, HasNumPartitions}
import com.netease.easyml.ml.util.SchemaUtils
import org.apache.spark.ml.Model
import org.apache.spark.ml.param._
import org.apache.spark.ml.param.shared.HasInputCol
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Dataset, Row}
import org.apache.spark.storage.StorageLevel

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer
import scala.util.Try

/**
 * Created by linjiuning on 2021/5/24.
 * RoBERT 训练数据准备
 */

trait RoBERTTFRecordDataParams extends Params with HasInputCol with HasLowercase with HasNumPartitions {

  val vocabFile: Param[String] = new Param[String](this, "vocabFile", "The vocabulary file that the BERT model was trained on.")

  def getVocabFile: String = $(vocabFile)

  val sentenceSep: Param[String] = new Param[String](this, "sentenceSep", "The sentence separator.")

  def getSentenceSep: String = $(sentenceSep)

  val wholeWordMask: BooleanParam = new BooleanParam(this, "wholeWordMask",
    "Whether to use whole word masking rather than per-WordPiece masking.")

  def getWholeWordMask: Boolean = $(wholeWordMask)

  val maxSeqLength: IntParam = new IntParam(this, "maxSeqLength", "Maximum sequence length.", ParamValidators.gt(1))

  def getMaxSeqLength: Int = $(maxSeqLength)

  val maxPredictionsPerSeq: IntParam = new IntParam(this, "maxPredictionsPerSeq", "Maximum number of masked LM predictions per sequence.", ParamValidators.gt(1))

  def getMaxPredictionsPerSeq: Int = $(maxPredictionsPerSeq)

  val dupeFactor: IntParam = new IntParam(this, "dupeFactor", "Number of times to duplicate the input data (with different masks).")

  def getDupeFactor: Int = $(dupeFactor)

  val maskedLmProb: DoubleParam = new DoubleParam(this, "maskedLmProb", "Masked LM probability.")

  def getMaskedLmProb: Double = $(maskedLmProb)

  val shortSeqProb: DoubleParam = new DoubleParam(this, "shortSeqProb", "Probability of creating sequences which are shorter than the maximum length.")

  def getShortSeqProb: Double = $(shortSeqProb)

  setDefault(vocabFile -> "", sentenceSep -> "\n", lowercase -> true, wholeWordMask -> true, maxSeqLength -> 512,
    maxPredictionsPerSeq -> 80, dupeFactor -> 10, maskedLmProb -> 0.15, shortSeqProb -> 0.1)

  protected def validateAndTransformSchema(schema: StructType): StructType = {
    SchemaUtils.checkStringType(schema, $(inputCol))
    StructType(Seq(StructField("tokens", ArrayType(StringType)),
      StructField("input_ids", ArrayType(IntegerType)),
      StructField("segment_ids", ArrayType(IntegerType)),
      StructField("masked_lm_positions", ArrayType(IntegerType)),
      StructField("masked_lm_ids", ArrayType(IntegerType))
    ))
  }
}

case class Instance(tokens: Array[String],
                    segmentIds: Array[Int],
                    maskedLmPositions: Array[Int],
                    maskedLmLabels: Array[String])

case class MaskedLmInstance(index: Int, label: String)

class RoBERTTFRecordData(override val uid: String) extends Model[RoBERTTFRecordData] with RoBERTTFRecordDataParams with DefaultParamsWritable {

  import RoBERTTFRecordData._

  def this() = this(Identifiable.randomUID("wordDiscovery"))

  def setInputCol(value: String): this.type = set(inputCol, value)

  def setSentenceSep(value: String): this.type = set(sentenceSep, value)

  def setLowercase(value: Boolean): this.type = set(lowercase, value)

  def setWholeWordMask(value: Boolean): this.type = set(wholeWordMask, value)

  def setNumPartitions(value: Int): this.type = set(numPartitions, value)

  def setMaxSeqLength(value: Int): this.type = set(maxSeqLength, value)

  def setMaxPredictionsPerSeq(value: Int): this.type = set(maxPredictionsPerSeq, value)

  def setDupeFactor(value: Int): this.type = set(dupeFactor, value)

  def setMaskedLmProb(value: Double): this.type = set(maskedLmProb, value)

  def setShortSeqProb(value: Double): this.type = set(shortSeqProb, value)

  override def transform(dataset: Dataset[_]): DataFrame = {
    val outputSchema = transformSchema(dataset.schema, logging = true)

    val inputCol = getInputCol
    val vocabFile = getVocabFile
    val sentenceSep = getSentenceSep
    val wholeWordMask = getWholeWordMask
    val lowercase = getLowercase
    val shortSeqProb = getShortSeqProb
    val maxSeqLength = getMaxSeqLength
    val maxNumTokens = maxSeqLength - 3
    val maskedLmProb = getMaskedLmProb
    val maxPredictionsPerSeq = getMaxPredictionsPerSeq
    val dupeFactor = getDupeFactor

    val allDocuments = dataset.toDF.rdd.mapPartitions(iter => {
      val jieba = new JiebaTokenizer
      val tokenizer = getOrCreateTokenizer(uid, vocabFile, lowercase)
      iter.map(row => {
        Try(row.getAs[String](inputCol)).getOrElse("")
      }).filter(_.nonEmpty)
        .map(doc => {
          val sentences = doc.split(sentenceSep)
          sentences.map(sentence => {
            if (wholeWordMask) {
              val tokens = ArrayBuffer.empty[String]
              jieba.tokenize(sentence).asScala.foreach(word => {
                tokenizer.tokenize(word.text).asScala.zipWithIndex.map {
                  case (word, i) =>
                    if (i == 0) {
                      WHOLE_WORLD_MASK_SEP + word
                    } else {
                      word
                    }
                }.foreach(token => tokens.append(token))
              })
              tokens.toArray
            } else {
              tokenizer.tokenize(sentence).asScala.toArray
            }
          }).filter(_.nonEmpty)
        })
    }).filter(it => it.nonEmpty && it(0).nonEmpty)

    allDocuments.persist(StorageLevel.MEMORY_AND_DISK)

    val newRdds = (1 to dupeFactor).map(i => {
      logInfo(s"Dupe $i of $dupeFactor...")

      val newRdd = allDocuments.mapPartitions(iter => {
        val rng = new Random()
        val tokenizer = getOrCreateTokenizer(uid, vocabFile, lowercase)
        val vocabWords = tokenizer.vocab().keySet().asScala.toArray
        iter.flatMap(document => {
          val targetSeqLength = if (rng.nextDouble() < shortSeqProb) {
            rng.nextInt(maxNumTokens - 1) + 2
          } else {
            maxNumTokens
          }
          var i = 0
          val currentChunk = ArrayBuffer.empty[Array[String]]
          var currentLength = 0
          val instances = ArrayBuffer.empty[Instance]
          while (i < document.length) {
            currentChunk.append(document(i))
            currentLength += document(i).length
            if (i == document.length - 1 || currentLength >= targetSeqLength) {
              if (currentChunk.nonEmpty) {
                val aEnd = if (currentChunk.length > 2) {
                  rng.nextInt(currentChunk.length - 1) + 1
                } else {
                  1
                }
                val tokensA = ArrayBuffer.empty[String]
                for (j <- 0 until aEnd) {
                  tokensA.appendAll(currentChunk(j))
                }

                val tokensB = ArrayBuffer.empty[String]
                for (j <- aEnd until currentChunk.length) {
                  tokensB.appendAll(currentChunk(j))
                }
                truncateSeqPair(tokensA, tokensB, maxNumTokens, rng)

                val tokens = ArrayBuffer.empty[String]
                val segmentIds = ArrayBuffer.empty[Int]
                tokens.append("[CLS]")
                segmentIds.append(0)
                for (token <- tokensA) {
                  tokens.append(token)
                  segmentIds.append(0)
                }
                tokens.append("[SEP]")
                segmentIds.append(0)

                for (token <- tokensB) {
                  tokens.append(token)
                  segmentIds.append(1)
                }

                if (tokensB.nonEmpty) {
                  tokens.append("[SEP]")
                  segmentIds.append(1)
                }

                val (newTokens, maskedLmPositions, maskedLmLabels) = createMaskedLmPredictions(tokens.toArray, maskedLmProb, maxPredictionsPerSeq, vocabWords, wholeWordMask, rng)
                val instance = Instance(
                  tokens = newTokens,
                  segmentIds = segmentIds.toArray,
                  maskedLmPositions = maskedLmPositions,
                  maskedLmLabels = maskedLmLabels
                )
                instances.append(instance)
              }
              currentChunk.clear()
              currentLength = 0
            }
            i += 1
          }
          instances
        }).map(it => instanceToExample(it, tokenizer, maxSeqLength, maxPredictionsPerSeq))
          .map(it => Row.fromSeq(it))
      })
      newRdd.persist(StorageLevel.MEMORY_AND_DISK)
      newRdd.foreachPartition(_ => Unit)
      newRdd
    })

    val newRdd = dataset.sparkSession.sparkContext.union(newRdds)
    dataset.sparkSession.createDataFrame(newRdd, outputSchema)
  }

  override def copy(extra: ParamMap): RoBERTTFRecordData = defaultCopy(extra)

  override def transformSchema(schema: StructType): StructType = validateAndTransformSchema(schema)
}

object RoBERTTFRecordData extends DefaultParamsReadable[RoBERTTFRecordData] {
  val WHOLE_WORLD_MASK_SEP: String = "_!_"

  def getOrCreateTokenizer(uid: String, vocabPath: String, lowercase: Boolean): BertTokenizer = {
    ResourceManager.getOrCreate(uid, new Supplier[BertTokenizer] {
      override def get(): BertTokenizer = {
        val builder = BertTokenizer.builder.setDoLowerCase(lowercase)
        if (!StringUtil.isEmpty(vocabPath)) {
          builder.setVocabFile(vocabPath)
        }
        builder.build()
      }
    })
  }

  def truncateSeqPair(tokenA: ArrayBuffer[String], tokenB: ArrayBuffer[String], maxNumTokens: Int, rng: java.util.Random): Unit = {
    while (tokenA.length + tokenB.length > maxNumTokens) {
      val truncTokens = if (tokenA.length > tokenB.length) tokenA else tokenB
      assert(truncTokens.nonEmpty)
      if (rng.nextDouble() < 0.5) {
        truncTokens.remove(0)
      } else {
        truncTokens.remove(truncTokens.length - 1)
      }
    }
  }

  def createMaskedLmPredictions(tokens: Array[String], maskLmProb: Double, maxPredictionsPerSeq: Int,
                                vocabWords: Array[String], wholeWordMask: Boolean, rng: java.util.Random): (Array[String], Array[Int], Array[String]) = {
    val candIndexesBuf = ArrayBuffer.empty[ArrayBuffer[Int]]
    val outTokens = Array.fill[String](tokens.length)("")
    tokens.zipWithIndex.foreach {
      case (token, i) =>
        if (!token.equals("[CLS]") && !token.equals("[SEP]")) {
          if (wholeWordMask && candIndexesBuf.nonEmpty && token.startsWith(WHOLE_WORLD_MASK_SEP)) {
            candIndexesBuf.last.append(i)
          } else {
            val buf = ArrayBuffer.empty[Int]
            buf.append(i)
            candIndexesBuf.append(buf)
          }
          val newToken = if (wholeWordMask && token.startsWith(WHOLE_WORLD_MASK_SEP)) {
            token.slice(WHOLE_WORLD_MASK_SEP.length, token.length)
          } else {
            token
          }
          outTokens(i) = newToken
        } else {
          outTokens(i) = token
        }
    }

    val candIndexes = candIndexesBuf.toArray
    MathUtil.shuffle(rng, candIndexes)

    val numToPredict = Math.min(maxPredictionsPerSeq, Math.max(1, Math.round(tokens.length * maskLmProb).toInt))

    val maskLms = ArrayBuffer.empty[MaskedLmInstance]
    var i = 0
    while (i < candIndexes.length && maskLms.length < numToPredict) {
      val indexSet = candIndexes(i)
      if (indexSet.length + maskLms.length <= numToPredict) {
        indexSet.foreach(index => {
          val token = outTokens(index)
          val maskToken = if (rng.nextDouble() < 0.8) {
            "[MASK]"
          } else {
            if (rng.nextDouble() < 0.5) {
              outTokens(index)
            } else {
              vocabWords(rng.nextInt(vocabWords.length))
            }
          }
          outTokens(index) = maskToken
          maskLms.append(MaskedLmInstance(index = index, label = token))
        })
      }
      i += 1
    }
    assert(maskLms.length <= numToPredict)

    val maskedLmPositions = ArrayBuffer.empty[Int]
    val maskedLmLabels = ArrayBuffer.empty[String]
    maskLms.sortBy(_.index).foreach(it => {
      maskedLmPositions.append(it.index)
      maskedLmLabels.append(it.label)
    })
    (outTokens, maskedLmPositions.toArray, maskedLmLabels.toArray)
  }

  def instanceToExample(instance: Instance, tokenizer: BertTokenizer, maxSeqLength: Int, maxPredictionsPerSeq: Int): Seq[Any] = {
    val inputIds = Array.fill[Int](maxSeqLength)(0)
    val segmentIds = Array.fill[Int](maxSeqLength)(0)
    val maskedLmPositions = Array.fill[Int](maxPredictionsPerSeq)(0)
    val maskedLmIds = Array.fill[Int](maxPredictionsPerSeq)(0)

    instance.tokens.zipWithIndex.foreach {
      case (token, i) =>
        val id = tokenizer.convertTokenToId(token)
        inputIds(i) = id
    }

    instance.segmentIds.zipWithIndex.foreach {
      case (id, i) =>
        segmentIds(i) = id
    }

    instance.maskedLmPositions.zipWithIndex.foreach {
      case (id, i) =>
        maskedLmPositions(i) = id
    }

    instance.maskedLmLabels.zipWithIndex.foreach {
      case (token, i) =>
        val id = tokenizer.convertTokenToId(token)
        maskedLmIds(i) = id
    }

    Seq(instance.tokens, inputIds, segmentIds, maskedLmPositions, maskedLmIds)
  }

  override def load(path: String): RoBERTTFRecordData = super.load(path)
}