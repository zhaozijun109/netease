package com.netease.easyml.ml.sklearn.model_selection

import java.util.Random

import com.netease.easyml.annotation.Register
import com.netease.easyml.ml.util.{Utils => MLUtils}
import org.apache.spark.ml.param.{ParamMap, Params}
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.{DataFrame, Dataset, Row}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.util.random.BernoulliCellSampler

/**
 * Created by linjiuning on 2020/8/19.
 */
@Register(alias = Array("kfold", "k_fold", "kFold"))
class KFold(override val uid: String) extends BaseKFold with DefaultParamsWritable {

  def this() = {
    this(Identifiable.randomUID("kFold"))
  }

  setDefault(nSplits -> 5, shuffle -> false)

  override def copy(extra: ParamMap): Params = defaultCopy(extra)

  override def split(dataset: Dataset[_]): Iterator[(DataFrame, DataFrame)] = {
    val handlePersistence = dataset.storageLevel == StorageLevel.NONE

    if (handlePersistence) dataset.persist(StorageLevel.MEMORY_AND_DISK)

    val rng = if (isDefined(randomState)) {
      new Random($(randomState))
    } else {
      MLUtils.random
    }
    val nSplitsF = $(nSplits).toFloat
    val rdd = dataset.toDF().rdd
    val seed = rng.nextLong()
    val sparkSession = dataset.sparkSession
    val schema = dataset.schema
    (1 to $(nSplits)).toIterator.map(fold => {
      val sampler = new BernoulliCellSampler[Row]((fold - 1) / nSplitsF, fold / nSplitsF, complement = false)
      val validation = MLUtils.newPartitionwiseSampledRDD(rdd, sampler, true, seed)
      val training = MLUtils.newPartitionwiseSampledRDD(rdd, sampler.cloneComplement(), true, seed)
      if (fold == $(nSplits) && handlePersistence) dataset.unpersist()
      (sparkSession.createDataFrame(training, schema), sparkSession.createDataFrame(validation, schema))
    })
  }
}

object KFold extends DefaultParamsReadable[KFold] {
  override def load(path: String): KFold = super.load(path)
}