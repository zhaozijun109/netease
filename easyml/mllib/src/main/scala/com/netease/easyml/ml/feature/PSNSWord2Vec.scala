package com.netease.easyml.ml.feature

import java.util.concurrent.TimeUnit

import com.github.fommil.netlib.BLAS.{getInstance => blas}
import com.netease.easyml.common.util.IOUtil
import com.netease.easyml.ml.util.{FastSigmoid, MLUtils}
import com.tencent.angel.graph.embedding.line._
import com.tencent.angel.ml.matrix.{MatrixContext, RowType}
import com.tencent.angel.model.{MatrixLoadContext, MatrixSaveContext, ModelLoadContext, ModelSaveContext}
import com.tencent.angel.spark.context.PSContext
import com.tencent.angel.spark.models.PSMatrix
import it.unimi.dsi.fastutil.ints.{Int2IntOpenHashMap, Int2ObjectOpenHashMap}
import org.apache.hadoop.fs.Path
import org.apache.spark.SparkContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.ml.param._
import org.apache.spark.ml.util.{DefaultParamsReadable, Identifiable}
import org.apache.spark.mllib.feature_.{Word2VecModel => OldWord2VecModel}
import org.apache.spark.rdd.RDD
import org.apache.spark.util.DoubleAccumulator

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
 * Created by linjiuning on 2020/9/27.
 */
trait PSNSWord2VecBase extends BaseNSWord2Vec {

  final val numPsPart = new IntParam(
    this, "numPsPart", "Num of parameter server part.",
    ParamValidators.gt(0))

  def getNumPsPart: Int = $(numPsPart)

  final val preTrainedModelPath = new Param[String](
    this, "preTrainedModelPath", "Pretrained model path of ps model.")

  def getPreTrainedModelPath: String = $(preTrainedModelPath)

  final val modelPath = new Param[String](
    this, "modelPath", "Save path of ps model.")

  def getModelPath: String = $(modelPath)

  final val checkpointInterval = new IntParam(
    this, "checkpointInterval", "Every N epoch to checkpoint ps model.",
    ParamValidators.gt(0))

  def getCheckpointInterval: Int = $(checkpointInterval)

  final val saveModelInterval = new IntParam(
    this, "saveModelInterval", "Every N epoch to save ps model.",
    ParamValidators.gt(0))

  def getSaveModelInterval: Int = $(saveModelInterval)

  final val returnNull = new BooleanParam(
    this, "returnNull", "Every N epoch to save ps model.")

  def getReturnNull: Boolean = $(returnNull)

  setDefault(decayRate -> 0.1, numPsPart -> 1, sortedVocab -> false, checkpointInterval -> Int.MaxValue,
    saveModelInterval -> Int.MaxValue, returnNull -> false, batchSize -> 50, batchWords -> 5000, alpha -> 0.1)
}

case class Batch(srcIdx: Array[Int], dstIdx: Array[Int], negIdx: Array[Array[Int]], alpha: Double)

class PSNSWord2Vec(override val uid: String) extends PSNSWord2VecBase {

  def this() = this(Identifiable.randomUID("psNsW2v"))

  def setNumPsPart(value: Int): this.type = set(numPsPart, value)

  def setPreTrainedModelPath(value: String): this.type = set(preTrainedModelPath, value)

  def setModelPath(value: String): this.type = set(modelPath, value)

  def setCheckpointInterval(value: Int): this.type = set(checkpointInterval, value)

  def setSaveModelInterval(value: Int): this.type = set(saveModelInterval, value)

  def setReturnNull(value: Boolean): this.type = set(returnNull, value)

  val matrixName = "embedding"
  // Create one ps matrix to hold the input vectors and the output vectors for all node
  var mc: MatrixContext = _
  var psMatrix: PSMatrix = _
  var matrixId: Int = _

  private def randomInitialize(): Unit = {
    val beforeRandomize = System.currentTimeMillis()
    psMatrix.psfUpdate(new LINEModelRandomize(new RandomizeUpdateParam(matrixId, $(vectorSize), 2, $(seed).toInt))).get()
    logInfo(s"Model successfully Randomized, cost ${(System.currentTimeMillis() - beforeRandomize) / 1000.0}s")
  }

  override def doFit[S <: Iterable[String]](dataset: RDD[S], sc: SparkContext,
                                            bcExpTable: Broadcast[FastSigmoid],
                                            bcCumTable: Broadcast[Array[Int]],
                                            bcSampleInt: Broadcast[Array[Int]],
                                            bcVocabHash: Broadcast[mutable.HashMap[String, Int]]): OldWord2VecModel = {
    val vectorSize = getVectorSize
    val numPsPart = getNumPsPart
    val computeLoss = getComputeLoss
    val maxIter = getMaxIter
    val negative = getNegative

    mc = new MatrixContext(matrixName, 1, vocabSize)
    mc.setMaxRowNumInBlock(1)
    mc.setMaxColNumInBlock((vocabSize + numPsPart - 1) / numPsPart)
    mc.setRowType(RowType.T_ANY_INTKEY_DENSE)
    mc.setValueType(classOf[LINENode])
    mc.setInitFunc(new LINEInitFunc(2, vectorSize))
    psMatrix = PSMatrix.matrix(mc)
    matrixId = psMatrix.id

    if (isSet(preTrainedModelPath)) {
      loadModel($(preTrainedModelPath))
    } else {
      randomInitialize()
    }

    val lossAccum: DoubleAccumulator = if (computeLoss)
      sc.doubleAccumulator("loss")
    else
      null

    val newSentences = convertToId(dataset, bcVocabHash).cache()
    for (k <- 1 to maxIter) {
      val msEpochStartTime = System.currentTimeMillis()
      val batches = batch(k, newSentences, bcCumTable, bcSampleInt)

      batches.foreachPartition(iter => {
        val epochStartTime = System.currentTimeMillis()
        var loss = 0.0
        iter.foreach(batch => {
          val alpha = batch.alpha
          val srcIdx = batch.srcIdx
          val dstIdx = batch.dstIdx
          val negIdx = batch.negIdx

          val result = psMatrix.asyncPsfGet(new LINEGetEmbedding(new LINEGetEmbeddingParam(matrixId, srcIdx, dstIdx,
            negIdx, 2, negative))).get(1800000, TimeUnit.MILLISECONDS).asInstanceOf[LINEGetEmbeddingResult].getResult
          val srcFeats: Int2ObjectOpenHashMap[Array[Float]] = result._1
          val dstFeats: Int2ObjectOpenHashMap[Array[Float]] = result._2

          val inputUpdateCounter = new Int2IntOpenHashMap(srcFeats.size())
          val inputUpdates = new Int2ObjectOpenHashMap[Array[Float]](srcFeats.size())

          val outputUpdateCounter = new Int2IntOpenHashMap(dstFeats.size())
          val outputUpdates = new Int2ObjectOpenHashMap[Array[Float]](dstFeats.size())

          var i = 0
          while (i < srcIdx.length) {
            val src = srcIdx(i)
            val dst = dstIdx(i)
            val neg = negIdx(i)
            val syn0 = srcFeats.get(src)
            val neu1e = new Array[Float](vectorSize)
            val nsSamples = Array((dst, 1)) ++ neg.map((_, 0))
            nsSamples.foreach { case (target, label) =>
              val syn1 = dstFeats.get(target)
              val dot = blas.sdot(vectorSize, syn0, 0, 1, syn1, 0, 1)
              if (computeLoss) {
                val fDot = if (label == 1) dot else -dot
                bcExpTable.value.log(fDot).foreach(l => loss -= l)
              }
              bcExpTable.value(dot).map(f => {
                val g = ((label - f) * alpha).toFloat
                blas.saxpy(vectorSize, g, syn1, 0, 1, neu1e, 0, 1)
                val gSyn1 = if (!outputUpdates.containsKey(target)) {
                  val gSyn1 = new Array[Float](vectorSize)
                  outputUpdates.put(target, gSyn1)
                  gSyn1
                } else {
                  outputUpdates.get(target)
                }
                blas.saxpy(vectorSize, g, syn0, 0, 1, gSyn1, 0, 1)
                outputUpdateCounter.putIfAbsent(target, 0)
                outputUpdateCounter.addTo(target, 1)
              })
            }
            val gSyn0 = if (!inputUpdates.containsKey(src)) {
              val gSyn0 = new Array[Float](vectorSize)
              inputUpdates.put(src, gSyn0)
              gSyn0
            } else {
              inputUpdates.get(src)
            }
            blas.saxpy(vectorSize, 1.0f, neu1e, 0, 1, gSyn0, 0, 1)
            inputUpdateCounter.putIfAbsent(src, 0)
            inputUpdateCounter.addTo(src, 1)
            i += 1
          }
          inputUpdateCounter.asScala.foreach {
            case (key, count) =>
              val syn0 = inputUpdates.get(key)
              blas.sscal(vectorSize, 1.0f / count, syn0, 1)
          }
          outputUpdateCounter.asScala.foreach {
            case (key, count) =>
              val syn1 = outputUpdates.get(key)
              blas.sscal(vectorSize, 1.0f / count, syn1, 1)
          }
          psMatrix.psfUpdate(new LINEAdjust(new LINEAdjustParam(matrixId, inputUpdates, outputUpdates, 2)))
        })
        if (computeLoss) {
          lossAccum.add(loss)
          val epochTime = System.currentTimeMillis() - epochStartTime
          println(s"Epoch=$k loss=$loss time=${epochTime.toFloat / 1000}s")
        }
      })
      val epochTime = System.currentTimeMillis() - msEpochStartTime
      if (computeLoss) {
        println(s"Epoch=$k loss=${lossAccum.value} time=${epochTime.toFloat / 1000}s")
        lossAccum.reset()
      } else {
        println(s"Epoch=$k time=${epochTime.toFloat / 1000}s")
      }
    }

    saveModel($(modelPath))
    sc.parallelize(vocab.zipWithIndex.map(f => s"${f._2}:${f._1}"), 1)
      .saveAsTextFile(IOUtil.join($(modelPath), "mapping"))

    newSentences.unpersist()

    if (!$(returnNull)) {
      val (vocab, wordVectors) = MLUtils.loadAngelWordVec(sc, $(modelPath))
      new OldWord2VecModel(vocab.zipWithIndex.toMap, wordVectors)
        .setSliceSave(getSliceSave)
    } else {
      null.asInstanceOf[OldWord2VecModel]
    }
  }

  def checkpointAndSaveIfNeed(epoch: Int): Unit = {
    var startTs = 0L
    if (epoch % $(checkpointInterval) == 0 && epoch < $(maxIter)) {
      logInfo(s"Epoch=$epoch, checkpoint the model")
      startTs = System.currentTimeMillis()
      psMatrix.checkpoint(epoch)
      logInfo(s"checkpoint use time=${System.currentTimeMillis() - startTs}")
    }

    if (epoch % $(saveModelInterval) == 0 && epoch < $(maxIter)) {
      logInfo(s"Epoch=$epoch, save the model")
      startTs = System.currentTimeMillis()
      saveModel($(modelPath), epoch)
      logInfo(s"save use time=${System.currentTimeMillis() - startTs}")
    }
  }

  def saveModel(modelPathRoot: String, epoch: Int): Unit = {
    saveModel(new Path(modelPathRoot, s"CP_$epoch").toString)
  }

  def saveModel(modelPath: String): Unit = {
    logInfo(s"saving model to $modelPath")
    if (IOUtil.exists(modelPath)) {
      IOUtil.delete(modelPath)
    } else {
      IOUtil.mkdirs(modelPath)
    }

    val saveContext = new ModelSaveContext(modelPath)
    saveContext.addMatrix(new MatrixSaveContext(matrixName, classOf[TextLINEModelOutputFormat].getTypeName))
    PSContext.instance().save(saveContext)
  }

  def loadModel(modelPath: String): Unit = {
    val startTime = System.currentTimeMillis()
    logInfo(s"load model from $modelPath")

    val loadContext = new ModelLoadContext(modelPath)
    loadContext.addMatrix(new MatrixLoadContext(psMatrix.name))
    PSContext.getOrCreate(SparkContext.getOrCreate()).load(loadContext)
    logInfo(s"model load time=${System.currentTimeMillis() - startTime} ms")
  }

  override def copy(extra: ParamMap): PSNSWord2Vec = defaultCopy(extra)
}

object PSNSWord2Vec extends DefaultParamsReadable[PSNSWord2Vec] {

  override def load(path: String): PSNSWord2Vec = super.load(path)
}
