package com.netease.easyml.ml.sklearn.model_selection

import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.{DataFrame, Dataset}

/**
 * Created by linjiuning on 2020/8/20.
 */
class RandomUnderSampler(override val uid: String) extends BaseSampler with DefaultParamsWritable {
  def this() = {
    this(Identifiable.randomUID("randomUnderSampler"))
  }

  override val samplingType: String = BaseSampler.SAMPLING_TYPE_UNDER_SAMPLING

  override def sample(dataset: Dataset[_], labels: Array[String], ratio: Map[String, Double]): DataFrame = {
    val newRatio = labels.map(label => (label, ratio.getOrElse(label, 1.0))).toMap
    sample(dataset, newRatio)
  }
}

object RandomUnderSampler extends DefaultParamsReadable[RandomUnderSampler] {
  override def load(path: String): RandomUnderSampler = super.load(path)
}