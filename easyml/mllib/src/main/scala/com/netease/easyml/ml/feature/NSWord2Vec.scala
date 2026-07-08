package com.netease.easyml.ml.feature

import com.github.fommil.netlib.BLAS.{getInstance => blas}
import com.netease.easyml.ml.util.{FastSigmoid, Utils}
import org.apache.spark.SparkContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.ml.param._
import org.apache.spark.ml.util.{DefaultParamsReadable, Identifiable}
import org.apache.spark.mllib.feature_.{Word2VecModel => OldWord2VecModel}
import org.apache.spark.rdd.RDD
import org.apache.spark.util.DoubleAccumulator

import scala.collection.mutable

/**
 * Created by linjiuning on 2020/9/27.
 */
class NSWord2Vec(override val uid: String) extends BaseNSWord2Vec {

  def this() = this(Identifiable.randomUID("nsW2v"))

  override def doFit[S <: Iterable[String]](dataset: RDD[S], sc: SparkContext,
                                            bcExpTable: Broadcast[FastSigmoid],
                                            bcCumTable: Broadcast[Array[Int]],
                                            bcSampleInt: Broadcast[Array[Int]],
                                            bcVocabHash: Broadcast[mutable.HashMap[String, Int]]): OldWord2VecModel = {
    val seed = getSeed
    val computeLoss = getComputeLoss
    val vectorSize = getVectorSize
    val maxIter = getMaxIter

    val newSentences = convertToId(dataset, bcVocabHash).cache()

    val initRandom = Utils.newXORShiftRandom(seed)

    val lossAccum: DoubleAccumulator = if (computeLoss)
      sc.doubleAccumulator("loss")
    else
      null

    val syn0Global = Array.fill[Float](vocabSize * vectorSize)((initRandom.nextFloat() - 0.5f) / vectorSize)
    val syn1Global = new Array[Float](vocabSize * vectorSize)

    for (k <- 1 to maxIter) {
      val bcSyn0Global = sc.broadcast(syn0Global)
      val bcSyn1Global = sc.broadcast(syn1Global)
      val msEpochStartTime = System.currentTimeMillis()

      val batches = batch(k, newSentences, bcCumTable, bcSampleInt)

      val partial = batches.mapPartitions(iter => {
        val epochStartTime = System.currentTimeMillis()
        val syn0Modify = new Array[Int](vocabSize)
        val syn1Modify = new Array[Int](vocabSize)
        val syn0 = bcSyn0Global.value
        val syn1 = bcSyn1Global.value
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

            val neu1e = new Array[Float](vectorSize)

            val row1 = src * vectorSize

            val nsSamples = Array((dst, 1)) ++ neg.map((_, 0))
            nsSamples.foreach { case (target, label) =>
              val row2 = target * vectorSize
              val dot = blas.sdot(vectorSize, syn0, row1, 1, syn1, row2, 1)
              if (computeLoss) {
                val fDot = if (label == 1) dot else -dot
                bcExpTable.value.log(fDot).foreach(l => loss -= l)
              }
              bcExpTable.value(dot).map(f => {
                val g = ((label - f) * alpha).toFloat
                blas.saxpy(vectorSize, g, syn1, row2, 1, neu1e, 0, 1)
                blas.saxpy(vectorSize, g, syn0, row1, 1, syn1, row2, 1)
                syn1Modify(target) += 1
              })
            }
            blas.saxpy(vectorSize, 1.0f, neu1e, 0, 1, syn0, row1, 1)
            syn0Modify(src) += 1
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
        } else {
          Array.copy(synAgg(i)._2, 0, syn1Global, (index - vocabSize) * vectorSize, vectorSize)
        }
        i += 1
      }

      val epochTime = System.currentTimeMillis() - msEpochStartTime
      if (computeLoss) {
        println(s"Master: Epoch=$k loss=${lossAccum.value} time=${epochTime.toFloat / 1000}s")
        lossAccum.reset()
      } else {
        println(s"Master: Epoch=$k time=${epochTime.toFloat / 1000}s")
      }

      bcSyn0Global.destroy()
      bcSyn1Global.destroy()
    }
    newSentences.unpersist()

    new OldWord2VecModel(vocab.zipWithIndex.toMap, syn0Global)
  }

  override def copy(extra: ParamMap): NSWord2Vec = defaultCopy(extra)
}

object NSWord2Vec extends DefaultParamsReadable[NSWord2Vec] {

  override def load(path: String): NSWord2Vec = super.load(path)
}
