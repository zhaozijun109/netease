package com.netease.easyml.ml.feature

import com.github.fommil.netlib.BLAS.{getInstance => blas}
import com.netease.easyml.common.util.IOUtil
import com.netease.easyml.ml.util.{SchemaUtils => EasyMLSchemaUtils, _}
import com.tencent.angel.ml.core.optimizer.decayer.StandardDecay
import org.apache.hadoop.fs.Path
import org.apache.spark.SparkContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.ml.Model
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.ml.param._
import org.apache.spark.ml.util._
import org.apache.spark.mllib.feature_.{Word2VecModel => OldWord2VecModel}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.types.{ArrayType, StringType, StructType}
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.util.DoubleAccumulator

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
 * Created by linjiuning on 2020/11/15.
 */

trait GESParams extends Params {
  val sideInfoCols: StringArrayParam = new StringArrayParam(this, "sideInfoCols", "side information column name")

  def getSideInfoCols: Array[String] = $(sideInfoCols)

  val sideInfoItemCol = new Param[String](
    this, "sideInfoItemCol", "the item column of side information")

  def getSideInfoItemCol: String = $(sideInfoItemCol)

  val sideInfo = new Param[String](
    this, "sideInfo", "the path or table of side information")

  def getSideInfo: String = $(sideInfo)

  val sideInfoMinCount = new IntParam(this, "sideInfoMinCount", "Ignores all side information words with total frequency lower than this.", ParamValidators.gtEq(0))

  def getSideInfoMinCount: Int = $(sideInfoMinCount)

  val bcItemVector = new BooleanParam(this, "bcItemVector", "Whether broadcast item vector during transform.")

  def getBcItemVector: Boolean = $(bcItemVector)

  def validateSideInfoSchema(schema: StructType): Unit = {
    val typeCandidates = List(StringType, new ArrayType(StringType, true), new ArrayType(StringType, false))
    EasyMLSchemaUtils.checkColumnType(schema, $(sideInfoItemCol), StringType)
    $(sideInfoCols).foreach(col => {
      EasyMLSchemaUtils.checkColumnTypes(schema, col, typeCandidates)
    })
  }

  setDefault(sideInfoMinCount -> 1, bcItemVector -> true)
}

class GES(override val uid: String) extends NegativeSample[GESModel] with GESParams {

  def this() = this(Identifiable.randomUID("ges"))

  def setSideInfoCols(value: Array[String]): this.type = set(sideInfoCols, value)

  def setSideInfoItemCol(value: String): this.type = set(sideInfoItemCol, value)

  def setSideInfo(value: String): this.type = set(sideInfo, value)

  def setSideInfoMinCount(value: Int): this.type = set(sideInfoMinCount, value)

  def setBcItemVector(value: Boolean): this.type = set(bcItemVector, value)

  @transient protected var sideInfoVocab: Map[String, Array[String]] = _

  @transient protected var bcVocabHash: Broadcast[mutable.HashMap[String, Int]] = _
  @transient protected var bcSideInfo: Broadcast[Array[Row]] = _

  def sideInformation(spark: SparkSession): Unit = {
    val cols = $(sideInfoCols)
    val path = $(sideInfo)
    val df = if (IOUtil.isDirectory(path) || IOUtil.isFile(path)) {
      spark.read.option("header", "true").csv(path)
    } else {
      spark.sql(s"select * from $path")
    }
    validateSideInfoSchema(df.schema)

    df.persist(StorageLevel.MEMORY_AND_DISK)

    val sideInfoMinCount = getSideInfoMinCount

    sideInfoVocab = df.select(cols.map(col): _*).toDF.rdd.flatMap(row => {
      cols.indices.flatMap(i => {
        val col = cols(i)
        row.get(i) match {
          case str: String =>
            Seq((col, str))
          case seq: Seq[String] =>
            seq.map((col, _))
          case null =>
            Seq()
        }
      })
    }).map((_, 1L))
      .reduceByKey(_ + _)
      .filter(_._2 >= sideInfoMinCount)
      .map(it => (it._1._1, (it._1._2, it._2)))
      .groupByKey
      .map(it => {
        val vocabs = it._2.toArray.sortBy(-_._2).map(_._1)
        (it._1, vocabs)
      })
      .collectAsMap()
      .toMap

    val siVocabHash = cols.map(col => {
      val siVocabs = sideInfoVocab(col)
      var vocabHash = mutable.HashMap.empty[String, Int]
      var a = 0
      while (a < siVocabs.length) {
        vocabHash += siVocabs(a) -> a
        a += 1
      }
      (col, vocabHash.toMap)
    }).toMap

    val bcSideInfoVocabHash = spark.sparkContext.broadcast(siVocabHash)

    sideInfoVocab.foreach {
      case (col, vocab) =>
        println(s"side info = $col, vocabSize = ${vocab.length}")
    }

    val sideInfo_ = new Array[Row](vocabSize)

    val bcVocabHash_ = bcVocabHash
    val item2Row = df.select(($(sideInfoItemCol) +: cols).map(col): _*)
      .toDF.rdd.filter(row => row.get(0) != null)
      .filter(row => bcVocabHash_.value.contains(row.getString(0)))
      .map(row => {
        val item = bcVocabHash_.value(row.getString(0))
        val values = cols.indices.map(i => {
          val col = cols(i)
          val vocab = bcSideInfoVocabHash.value(col)
          row.get(i + 1) match {
            case str: String =>
              vocab.getOrElse(str, null)
            case seq: Seq[String] =>
              val ids = seq.filter(vocab.contains).map(it => vocab(it))
              if (ids.isEmpty) {
                null
              } else {
                ids
              }
            case null =>
              null
          }
        })
        (item, Row.fromSeq(values))
      }).collect()
    println(s"Item with side information ratio: ${item2Row.length} / ${vocab.length} = ${item2Row.length * 1.0 / vocab.length}")
    item2Row.foreach {
      case (i, row) =>
        sideInfo_(i) = row
    }

    bcSideInfo = spark.sparkContext.broadcast(sideInfo_)

    bcSideInfoVocabHash.destroy()
    df.unpersist()
  }

  def doFit[S <: Iterable[String]](dataset: RDD[S], sc: SparkContext,
                                   bcExpTable: Broadcast[FastSigmoid],
                                   bcCumTable: Broadcast[Array[Int]],
                                   bcSampleInt: Broadcast[Array[Int]],
                                   bcVocabHash: Broadcast[mutable.HashMap[String, Int]]): GESModel = {
    val seed = getSeed
    val computeLoss = getComputeLoss
    val vectorSize = getVectorSize
    val maxIter = getMaxIter
    val bcSideInfo_ = bcSideInfo
    val sideInfoCols = getSideInfoCols

    val newSentences = convertToId(dataset, bcVocabHash).cache()

    val initRandom = Utils.newXORShiftRandom(seed)

    val lossAccum: DoubleAccumulator = if (computeLoss)
      sc.doubleAccumulator("loss")
    else
      null

    val syn0Global = Array.fill[Float](vocabSize * vectorSize)((initRandom.nextFloat() - 0.5f) / vectorSize)
    val syn1Global = new Array[Float](vocabSize * vectorSize)

    val siVocabSize = sideInfoVocab.map(_._2.length).sum
    val si0Global = Array.fill[Float](siVocabSize * vectorSize)((initRandom.nextFloat() - 0.5f) / vectorSize)

    var sum = 0
    val offsets = sideInfoCols.map(col => {
      val offset = sum
      sum += sideInfoVocab(col).length
      (col, offset)
    }).toMap

    for (k <- 1 to maxIter) {
      val bcSyn0Global = sc.broadcast(syn0Global)
      val bcSyn1Global = sc.broadcast(syn1Global)
      val bcSi0Global = sc.broadcast(si0Global)
      val msEpochStartTime = System.currentTimeMillis()

      val batches = batch(k, newSentences, bcCumTable, bcSampleInt)

      val partial = batches.mapPartitions(iter => {
        val epochStartTime = System.currentTimeMillis()
        val syn0Modify = new Array[Int](vocabSize)
        val syn1Modify = new Array[Int](vocabSize)
        val si0Modify = new Array[Int](siVocabSize)
        val syn0 = bcSyn0Global.value
        val syn1 = bcSyn1Global.value
        val si0 = bcSi0Global.value
        val sideInfo = bcSideInfo_.value

        var loss = 0.0
        iter.foreach(batch => {
          val alpha = batch.alpha
          val srcIdx = batch.srcIdx
          val dstIdx = batch.dstIdx
          val negIdx = batch.negIdx

          var i = 0
          while (i < srcIdx.length) {
            val src = srcIdx(i)
            val dst = dstIdx(i)
            val neg = negIdx(i)

            val row1 = src * vectorSize

            val input = new Array[Float](vectorSize)
            blas.saxpy(vectorSize, 1.0f, syn0, row1, 1, input, 0, 1)
            val siRow = sideInfo(src)
            var n = 1
            val siAlpha = if (siRow != null) {
              sideInfoCols.zipWithIndex.flatMap {
                case (col, i) =>
                  val offset = offsets(col)
                  val siIdx = siRow.get(i) match {
                    case id: Int =>
                      Seq(id)
                    case seq: Seq[Int] =>
                      seq
                    case null =>
                      Seq()
                  }
                  if (siIdx.nonEmpty) {
                    n += 1
                    val sa = 1.0f / siIdx.length
                    siIdx.map(j => {
                      val j_ = offset + j
                      blas.saxpy(vectorSize, sa, si0, j_ * vectorSize, 1, input, 0, 1)
                      (j_, sa)
                    })
                  } else {
                    Seq()
                  }
              }
            } else {
              null
            }

            if (n > 1) {
              blas.sscal(vectorSize, 1.0f / n, input, 0, 1)
            }

            val neu1e = new Array[Float](vectorSize)

            val nsSamples = Array((dst, 1)) ++ neg.map((_, 0))
            nsSamples.foreach { case (target, label) =>
              val row2 = target * vectorSize
              val dot = blas.sdot(vectorSize, input, 0, 1, syn1, row2, 1)
              if (computeLoss) {
                val fDot = if (label == 1) dot else -dot
                bcExpTable.value.log(fDot).foreach(l => loss -= l)
              }
              bcExpTable.value(dot).map(f => {
                val g = ((label - f) * alpha).toFloat
                blas.saxpy(vectorSize, g, syn1, row2, 1, neu1e, 0, 1)
                blas.saxpy(vectorSize, g, input, 0, 1, syn1, row2, 1)
                syn1Modify(target) += 1
              })
            }
            blas.saxpy(vectorSize, 1.0f / n, neu1e, 0, 1, syn0, row1, 1)
            //            blas.saxpy(vectorSize, 1.0f, neu1e, 0, 1, syn0, row1, 1)
            syn0Modify(src) += 1
            if (siAlpha != null) {
              siAlpha.foreach {
                case (src, sa) =>
                  si0Modify(src) += 1
                  blas.saxpy(vectorSize, sa / n, neu1e, 0, 1, si0, src * vectorSize, 1)
                //                  blas.saxpy(vectorSize, sa, neu1e, 0, 1, si0, src * vectorSize, 1)
              }
            }
            i += 1
          }
        })
        if (computeLoss) {
          lossAccum.add(loss)
          val epochTime = System.currentTimeMillis() - epochStartTime
          println(s"Epoch=$k loss=$loss time=${epochTime.toFloat / 1000}s")
        }
        // Only output modified vectors.
        Iterator.tabulate(vocabSize) { index =>
          if (syn0Modify(index) > 0) {
            Some((index, syn0.slice(index * vectorSize, (index + 1) * vectorSize)))
          } else {
            None
          }
        }.flatten ++ Iterator.tabulate(vocabSize) { index =>
          if (syn1Modify(index) > 0) {
            Some((index + vocabSize, syn1.slice(index * vectorSize, (index + 1) * vectorSize)))
          } else {
            None
          }
        }.flatten ++ Iterator.tabulate(siVocabSize) { index =>
          if (si0Modify(index) > 0) {
            Some((index + 2 * vocabSize, si0.slice(index * vectorSize, (index + 1) * vectorSize)))
          } else {
            None
          }
        }.flatten
      })

      // SPARK-24666: do normalization for aggregating weights from partitions.
      // Original Word2Vec either single-thread or multi-thread which do Hogwild-style aggregation.
      // Our approach needs to do extra normalization, otherwise adding weights continuously may
      // cause overflow on float and lead to infinity/-infinity weights.
      val synAgg = partial.mapPartitions { iter =>
        iter.map { case (id, vec) =>
          (id, (vec, 1))
        }
      }.reduceByKey { (vc1, vc2) =>
        blas.saxpy(vectorSize, 1.0f, vc2._1, 1, vc1._1, 1)
        (vc1._1, vc1._2 + vc2._2)
      }.map { case (id, (vec, count)) =>
        blas.sscal(vectorSize, 1.0f / count, vec, 1)
        (id, vec)
      }.collect()
      var i = 0
      while (i < synAgg.length) {
        val index = synAgg(i)._1
        if (index < vocabSize) {
          Array.copy(synAgg(i)._2, 0, syn0Global, index * vectorSize, vectorSize)
        } else if (index < 2 * vocabSize) {
          Array.copy(synAgg(i)._2, 0, syn1Global, (index - vocabSize) * vectorSize, vectorSize)
        } else {
          Array.copy(synAgg(i)._2, 0, si0Global, (index - 2 * vocabSize) * vectorSize, vectorSize)
        }
        i += 1
      }

      val epochTime = System.currentTimeMillis() - msEpochStartTime
      if (computeLoss) {
        println(s"[Master] Epoch=$k loss=${lossAccum.value} time=${epochTime.toFloat / 1000}s")
        lossAccum.reset()
      } else {
        println(s"[Master] Epoch=$k time=${epochTime.toFloat / 1000}s")
      }

      bcSyn0Global.destroy()
      bcSyn1Global.destroy()
      bcSi0Global.destroy()
    }
    newSentences.unpersist()

    var offset = 0
    val models = sideInfoCols.map(col => {
      val vocab = sideInfoVocab(col)
      val end = offset + vocab.length * vectorSize
      val vector = si0Global.slice(offset, end)
      offset = end
      (col, new OldWord2VecModel(vocab.zipWithIndex.toMap, vector))
    }).toMap

    val wordVector = new OldWord2VecModel(vocab.zipWithIndex.toMap, syn0Global)
    new GESModel(uid, wordVector, models)
  }

  override def copy(extra: ParamMap): GES = defaultCopy(extra)

  override def fit(dataset: Dataset[_]): GESModel = {
    transformSchema(dataset.schema, logging = true)
    val sc = dataset.sparkSession.sparkContext

    val input = dataset.select($(inputCol)).rdd.map(_.getAs[Seq[String]](0))
    if (isDefined(decayRate)) {
      scheduler = new StandardDecay(getAlpha, getDecayRate)
    }
    createExpTable()
    learnVocab(input)
    bcVocabHash = sc.broadcast(vocabHash)
    if (!$(randomSample)) {
      makeCumTable()
    }
    sideInformation(dataset.sparkSession)

    val bcExpTable = sc.broadcast(expTable)
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
      val model = doFit(input, sc, bcExpTable, bcCumTable, bcSampleInt, bcVocabHash)
      copyValues(model.setParent(this))
    } finally {
      bcExpTable.destroy()
      if (bcCumTable != null) {
        bcCumTable.destroy()
      }
      if (bcSampleInt != null) {
        bcSampleInt.destroy()
      }
      bcVocabHash.destroy()
      bcSideInfo.destroy()
    }
  }
}


class GESModel(override val uid: String,
               @transient private val wordVectors: OldWord2VecModel,
               @transient private val sideInfoWordVectors: Map[String, OldWord2VecModel])
  extends Model[GESModel] with GESParams with NegativeSampleBase with MLWritable {

  import GESModel._

  def setSideInfoItemCol(value: String): this.type = set(sideInfoItemCol, value)

  def setSideInfoCols(value: Array[String]): this.type = set(sideInfoCols, value)

  def setOutputCol(value: String): this.type = set(outputCol, value)

  def setBcItemVector(value: Boolean): this.type = set(bcItemVector, value)

  def setSliceSave(value: Boolean): this.type = set(sliceSave, value)

  private def getVectors(spark: SparkSession, model: OldWord2VecModel): DataFrame = {
    import spark.implicits._
    MLUtils.wordVecToRDD(spark, model).toDF(WORD_COL, VECTOR_COL)
  }

  def getItemVectors(spark: SparkSession): DataFrame = {
    getVectors(spark, wordVectors)
  }

  def getSideInfoVectors(spark: SparkSession, name: String): DataFrame = {
    val wordVectors = sideInfoWordVectors(name)

    getVectors(spark, wordVectors)
  }

  def getVectors(spark: SparkSession): DataFrame = {
    import spark.implicits._
    var df = MLUtils.wordVecToRDD(spark, wordVectors)
      .map(it => ("item", it._1, it._2))
      .toDF(TYPE_COL, WORD_COL, VECTOR_COL)

    sideInfoWordVectors.foreach {
      case (type_, wordVectors) =>
        val siDf = MLUtils.wordVecToRDD(spark, wordVectors).map(it => (type_, it._1, it._2))
          .toDF(TYPE_COL, WORD_COL, VECTOR_COL)
        df = df.union(siDf)
    }
    df
  }

  /**
   * Transform a sentence column to a vector column to represent the whole sentence. The transform
   * is performed by averaging all word vectors it contains.
   */
  override def transform(dataset: Dataset[_]): DataFrame = {
    val outputSchema = transformSchema(dataset.schema, logging = true)

    val isBcItemVec = getBcItemVector
    val columns = dataset.columns
    val sideInfoCols = getSideInfoCols.filter(columns.contains)
    val itemCol = $(sideInfoItemCol)
    val containsItem = columns.contains(itemCol)
    val siVectors = sideInfoCols.map(col => (col, sideInfoWordVectors(col).getVectors)).toMap

    val bcSiVectors = dataset.sparkSession.sparkContext.broadcast(siVectors)

    val d = $(vectorSize)
    val emptyVec = Vectors.sparse(d, Array.emptyIntArray, Array.emptyDoubleArray)

    val newDs = if (containsItem && !isBcItemVec) {
      val itemVectorDf = getVectors(dataset.sparkSession, wordVectors)
        .withColumnRenamed(WORD_COL, s"__${WORD_COL}__")
      dataset.join(itemVectorDf, dataset(itemCol) === itemVectorDf(s"__${WORD_COL}__"), "left_outer")
        .drop(s"__${WORD_COL}__")
    } else {
      dataset
    }

    val bcWordVectors = if (containsItem && isBcItemVec) {
      dataset.sparkSession.sparkContext.broadcast(wordVectors.getVectors)
    } else {
      null
    }

    val newRdd = newDs.toDF.rdd.map(row => {
      val sum = new Array[Float](d)
      var n = 0

      if (bcWordVectors != null) {
        val item = row.getString(row.fieldIndex(itemCol))
        if (item != null && bcWordVectors.value.contains(item)) {
          val vector = bcWordVectors.value(item)
          n += 1
          blas.saxpy(d, 1.0f, vector, 0, 1, sum, 0, 1)
        }
      } else if (containsItem) {
        val vector = row.getSeq[Float](row.fieldIndex(VECTOR_COL))
        if (vector != null) {
          n += 1
          blas.saxpy(d, 1.0f, vector.toArray, 0, 1, sum, 0, 1)
        }
      }

      sideInfoCols.foreach(col => {
        val si = row.get(row.fieldIndex(col)) match {
          case str: String =>
            Seq(str)
          case seq: Seq[String] =>
            seq
          case null =>
            Seq()
        }
        val siVectors = bcSiVectors.value(col)
        val vectors = si.filter(siVectors.contains).map(wd => siVectors(wd))
        if (vectors.nonEmpty) {
          n += 1
          val sa = 1.0f / vectors.length
          vectors.foreach(vector => {
            blas.saxpy(d, sa, vector, 0, 1, sum, 0, 1)
          })
        }
      })
      val vector = if (n == 0) {
        emptyVec
      } else {
        if (n > 1) {
          blas.sscal(d, 1.0f / n, sum, 1)
        }
        Vectors.dense(sum.map(_.toDouble))
      }

      val seq = if (containsItem && !isBcItemVec) {
        val i = row.fieldIndex(VECTOR_COL)
        val seq = row.toSeq
        seq.slice(0, i) ++ seq.slice(i + 1, seq.length)
      } else {
        row.toSeq
      }

      Row.fromSeq(seq :+ vector)
    })

    dataset.sparkSession.createDataFrame(newRdd, outputSchema)
  }

  override def validateAndTransformSchema(schema: StructType): StructType = {
    val typeCandidates = List(StringType, new ArrayType(StringType, true), new ArrayType(StringType, false))
    EasyMLSchemaUtils.checkColumnType(schema, $(sideInfoItemCol), StringType)
    $(sideInfoCols).foreach(col => {
      EasyMLSchemaUtils.checkColumnTypes(schema, col, typeCandidates)
    })
    EasyMLSchemaUtils.appendColumn(schema, $(outputCol), EasyMLSchemaUtils.vectorUDT)
  }

  override def copy(extra: ParamMap): GESModel = {
    val copied = new GESModel(uid, wordVectors, sideInfoWordVectors)
    copyValues(copied, extra).setParent(parent)
  }

  override def write: MLWriter = new GESModelWriter(this)

  override def transformSchema(schema: StructType): StructType = {
    validateAndTransformSchema(schema)
  }
}

object GESModel extends MLReadable[GESModel] {

  val TYPE_COL = "type"
  val WORD_COL = "word"
  val VECTOR_COL = "vector"

  class GESModelWriter(instance: GESModel) extends MLWriter {

    override protected def saveImpl(path: String): Unit = {
      DefaultEasyMLParamsWriter.saveMetadata(instance, path, sc)
      val sliceSave = instance.getSliceSave

      val dataPath = new Path(path, "data").toString
      val itemPath = new Path(dataPath, "item").toString
      instance.wordVectors.setSliceSave(sliceSave).save(sc, itemPath)
      instance.sideInfoWordVectors.foreach {
        case (col, wordVectors) =>
          val path = new Path(dataPath, col).toString
          wordVectors.setSliceSave(sliceSave).save(sc, path)
      }
    }
  }

  private class GESModelReader extends MLReader[GESModel] {

    private val className = classOf[GESModel].getName

    override def load(path: String): GESModel = {
      val metadata = DefaultEasyMLParamsReader.loadMetadata(path, sc, className)

      val dataPath = new Path(path, "data").toString

      val wordVectors = IOUtil.listDirectory(dataPath).asScala.map(path => {
        val name = IOUtil.baseName(path)
        val wordVectors = OldWord2VecModel.load(sc, path)
        (name, wordVectors)
      }).toMap

      val itemVectors = wordVectors("item")

      val sideInfo = wordVectors - "item"

      val model = new GESModel(metadata.uid, itemVectors, sideInfo)
      DefaultEasyMLParamsReader.getAndSetParams(model, metadata)
      model
    }
  }

  override def read: MLReader[GESModel] = new GESModelReader

  override def load(path: String): GESModel = super.load(path)
}