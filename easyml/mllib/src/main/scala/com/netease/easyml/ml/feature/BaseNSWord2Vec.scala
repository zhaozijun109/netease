package com.netease.easyml.ml.feature

import com.netease.easyml.ml.util.FastSigmoid
import com.tencent.angel.ml.core.optimizer.decayer.StandardDecay
import org.apache.spark.SparkContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.ml.feature_.Word2VecModel
import org.apache.spark.mllib.feature_.{Word2VecModel => OldWord2VecModel}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Dataset

import scala.collection.mutable

/**
 * Created by linjiuning on 2020/9/27.
 */

abstract class BaseNSWord2Vec extends NegativeSample[Word2VecModel] {

  def fit[S <: Iterable[String]](dataset: RDD[S]): OldWord2VecModel = {
    if (isDefined(decayRate)) {
      scheduler = new StandardDecay(getAlpha, getDecayRate)
    }
    createExpTable()
    learnVocab(dataset)
    if (!$(randomSample)) {
      makeCumTable()
    }

    val sc = dataset.context

    val bcExpTable = sc.broadcast(expTable)
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
      doFit(dataset, sc, bcExpTable, bcCumTable, bcSampleInt, bcVocabHash)
    } finally {
      bcExpTable.destroy()
      bcVocabHash.destroy()
      if (bcCumTable != null) {
        bcCumTable.destroy()
      }
      if (bcSampleInt != null) {
        bcSampleInt.destroy()
      }
    }
  }

  protected def doFit[S <: Iterable[String]](dataset: RDD[S], sc: SparkContext,
                                             bcExpTable: Broadcast[FastSigmoid],
                                             bcCumTable: Broadcast[Array[Int]],
                                             bcSampleInt: Broadcast[Array[Int]],
                                             bcVocabHash: Broadcast[mutable.HashMap[String, Int]]): OldWord2VecModel

  override def fit(dataset: Dataset[_]): Word2VecModel = {
    transformSchema(dataset.schema, logging = true)
    val input = dataset.select($(inputCol)).rdd.map(_.getAs[Seq[String]](0))
    val wordVectors = fit(input)
    copyValues(new Word2VecModel(uid, wordVectors).setParent(this))
  }
}

