package com.netease.easyml.ml.sklearn.model_selection

import java.util.Random

import com.netease.easyml.annotation.Register
import com.netease.easyml.ml.util.{Utils => MLUtils}
import org.apache.spark.ml.param.{ParamMap, Params}
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.{DataFrame, Dataset}
import org.apache.spark.storage.StorageLevel

/**
 * Created by linjiuning on 2020/8/10.
 */
@Register(alias = Array("shuffle"))
class ShuffleSplit(override val uid: String) extends BaseShuffleSplit with DefaultParamsWritable {
  def this() = {
    this(Identifiable.randomUID("shuffleSplit"))
  }

  setDefault(nSplits, 10)

  override def split(dataset: Dataset[_]): Iterator[(DataFrame, DataFrame)] = {
    val handlePersistence = dataset.storageLevel == StorageLevel.NONE

    if (handlePersistence) dataset.persist(StorageLevel.MEMORY_AND_DISK)

    val nSamples = dataset.count()
    val (nTrain, nTest) = Utils.validateShuffleSplit(nSamples, if (isDefined(testSize)) Some($(testSize)) else None,
      if (isDefined(trainSize)) Some($(trainSize)) else None, Some(0.1))
    val weight = Array(nTrain * 1.0 / nSamples, nTest * 1.0 / nSamples)

    val rng = if (isDefined(randomState)) {
      new Random($(randomState))
    } else {
      MLUtils.random
    }
    (0 until $(nSplits)).toIterator.map(i => {
      val Array(train, test) = dataset.toDF().randomSplit(weight, rng.nextLong)
      if (i == $(nSplits) - 1 && handlePersistence) dataset.unpersist()
      (train, test)
    })
  }

  override def copy(extra: ParamMap): Params = defaultCopy(extra)
}

object ShuffleSplit extends DefaultParamsReadable[ShuffleSplit] {
  override def load(path: String): ShuffleSplit = super.load(path)
}