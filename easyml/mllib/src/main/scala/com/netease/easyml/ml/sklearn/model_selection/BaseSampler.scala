package com.netease.easyml.ml.sklearn.model_selection

import java.util.Random

import com.netease.easyml.ml.param.HasRandomState
import com.netease.easyml.ml.util.{Utils => MLUtils}
import org.apache.spark.ml.Transformer
import org.apache.spark.ml.linalg.Vector
import org.apache.spark.ml.param._
import org.apache.spark.ml.param.shared.HasLabelCol
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, Dataset, Row}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.util.random.BernoulliCellSampler

import scala.collection.mutable.ArrayBuffer

/**
 * Created by linjiuning on 2020/8/20.
 */
trait BaseSamplerParams extends Params with HasLabelCol with HasRandomState {

  val samplingStrategy: Param[String] = new Param[String](this, "samplingStrategy",
    "Sampling information to sample the data set.", ParamValidators.inArray(BaseSampler.SAMPLING_STRATEGY))

  def getSamplingStrategy: String = $(samplingStrategy)

  val samplingNum: LongParam = new LongParam(this, "samplingNum",
    "Sampling number to sample the data set.", ParamValidators.gt(0))

  def getSamplingNum: Long = $(samplingNum)

  setDefault(samplingStrategy, "auto")
}

abstract class BaseSampler extends Transformer with BaseSamplerParams {

  import BaseSampler._

  def setLabelCol(value: String): this.type = set(labelCol, value)

  def setRandomState(value: Long): this.type = set(randomState, value)

  def setSamplingStrategy(value: String): this.type = set(samplingStrategy, value)

  def setSamplingNum(value: Long): this.type = set(samplingNum, value)

  val samplingType: String

  override def transform(dataset: Dataset[_]): DataFrame = {
    sample(dataset)
  }

  override def transformSchema(schema: StructType): StructType = schema

  override def copy(extra: ParamMap): Transformer = defaultCopy(extra)

  lazy val seed: Long = {
    val rng = if (isDefined(randomState)) {
      new Random($(randomState))
    } else {
      MLUtils.random
    }
    rng.nextLong()
  }

  def getLabelStats(dataset: Dataset[_]): Map[String, Long] = {
    dataset.select($(labelCol)).rdd.map(row => {
      val y = row.get(0)
      val yNorm = labelStr(y)
      (yNorm, 1L)
    }).reduceByKey(_ + _)
      .collectAsMap().toMap
  }

  def checkSamplingStrategy(dataset: Dataset[_]): (Array[String], Map[String, Double]) = {
    val labelStats = getLabelStats(dataset)
    val ratio = $(samplingStrategy) match {
      case SAMPLING_STRATEGY_MINORITY =>
        samplingStrategyMinority(labelStats, samplingType)
      case SAMPLING_STRATEGY_MAJORITY =>
        samplingStrategyMajority(labelStats, samplingType)
      case SAMPLING_STRATEGY_NOT_MINORITY =>
        samplingStrategyNotMinority(labelStats, samplingType)
      case SAMPLING_STRATEGY_NOT_MAJORITY =>
        samplingStrategyNotMajority(labelStats, samplingType)
      case SAMPLING_STRATEGY_ALL =>
        samplingStrategyAll(labelStats, samplingType)
      case SAMPLING_STRATEGY_AUTO =>
        if (isSet(samplingNum))
          samplingStrategyNumber(labelStats, samplingType, $(samplingNum))
        else
          samplingStrategyAuto(labelStats, samplingType)
    }
    (labelStats.keys.toArray, ratio)
  }

  def labelStr(y: Any): String = {
    y match {
      case vector: Vector =>
        vector.toArray.mkString(" ")
      case seq: Seq[_] =>
        seq.map(_.toString).sorted.mkString(" ")
      case any: Any =>
        any.toString
    }
  }

  def sample(dataset: Dataset[_], ratio: Map[String, Double]): DataFrame = {
    val copy = ratio.map {
      case (str, d) =>
        (str, Math.floor(d).toInt)
    }.filter(_._2 >= 1)

    val sampler = ratio.map {
      case (str, d) =>
        (str, d - Math.floor(d))
    }.filter(_._2 > 0).map {
      case (str, d) =>
        val sampler = new BernoulliCellSampler[Row](0, d, complement = false)
        sampler.setSeed(seed)
        (str, sampler)
    }

    val newRdd = dataset.toDF().rdd.mapPartitions(iter => {
      iter.flatMap(row => {
        val label = labelStr(row.getAs[String]($(labelCol)))
        val rows = new ArrayBuffer[Row]
        for (_ <- 0 until copy.getOrElse(label, 0)) {
          rows += row
        }
        if (sampler.contains(label) && sampler(label).sample() > 0) {
          rows += row
        }
        rows.toIterator
      })
    })

    dataset.sparkSession.createDataFrame(newRdd, dataset.schema)
  }

  def sample(dataset: Dataset[_], labels: Array[String], ratio: Map[String, Double]): DataFrame

  def sample(dataset: Dataset[_]): DataFrame = {
    val handelPersistent = dataset.storageLevel == StorageLevel.NONE
    if (handelPersistent) dataset.persist(StorageLevel.MEMORY_AND_DISK)

    val (labels, sampleRatio) = checkSamplingStrategy(dataset)
    val df = sample(dataset, labels, sampleRatio)

    if (handelPersistent) dataset.unpersist()
    df
  }
}

object BaseSampler {
  val SAMPLING_STRATEGY_MINORITY = "minority"
  val SAMPLING_STRATEGY_MAJORITY = "majority"
  val SAMPLING_STRATEGY_NOT_MINORITY = "not minority"
  val SAMPLING_STRATEGY_NOT_MAJORITY = "not majority"
  val SAMPLING_STRATEGY_ALL = "all"
  val SAMPLING_STRATEGY_AUTO = "auto"

  val SAMPLING_STRATEGY: Array[String] = Array(SAMPLING_STRATEGY_MAJORITY, SAMPLING_STRATEGY_NOT_MINORITY,
    SAMPLING_STRATEGY_NOT_MAJORITY, SAMPLING_STRATEGY_ALL, SAMPLING_STRATEGY_AUTO)

  val SAMPLING_TYPE_OVER_SAMPLING = "over-sampling"
  val SAMPLING_TYPE_UNDER_SAMPLING = "under-sampling"
  val SAMPLING_TYPE_CLEAN_SAMPLING = "clean-sampling"

  def samplingStrategyMinority(targetStats: Map[String, Long], samplingType: String): Map[String, Double] = {
    if (samplingType.equals(SAMPLING_TYPE_OVER_SAMPLING)) {
      val nSampleMajority = targetStats.values.max
      val classMinority = targetStats.minBy(_._2)._1

      targetStats.filter(_._1.equals(classMinority)).map {
        case (y, cnt) =>
          (y, (nSampleMajority - cnt) * 1.0 / cnt)
      }.filter(_._2 > 0)
    } else {
      throw new IllegalArgumentException("'sampling_strategy'='minority' cannot be used with under-sampler and clean-sampler.")
    }
  }

  def samplingStrategyMajority(targetStats: Map[String, Long], samplingType: String): Map[String, Double] = {
    if (samplingType.equals(SAMPLING_TYPE_UNDER_SAMPLING) || samplingType.equals(SAMPLING_TYPE_CLEAN_SAMPLING)) {
      val nSampleMinority = targetStats.values.min
      val classMajority = targetStats.maxBy(_._2)._1

      targetStats.filter(_._1.equals(classMajority)).map {
        case (y, cnt) =>
          (y, nSampleMinority * 1.0 / cnt)
      }.filter(_._2 > 0)
    } else {
      throw new IllegalArgumentException("'sampling_strategy'='majority' cannot be used with over-sampler.")
    }
  }

  def samplingStrategyNotMinority(targetStats: Map[String, Long], samplingType: String): Map[String, Double] = {
    if (samplingType.equals(SAMPLING_TYPE_OVER_SAMPLING)) {
      val nSampleMajority = targetStats.values.max
      val classMinority = targetStats.minBy(_._2)._1

      targetStats.filterNot(_._1.equals(classMinority)).map {
        case (y, cnt) =>
          (y, (nSampleMajority - cnt) * 1.0 / cnt)
      }.filter(_._2 > 0)
    } else {
      val nSampleMinority = targetStats.values.min
      val classMinority = targetStats.minBy(_._2)._1

      targetStats.filterNot(_._1.equals(classMinority)).map {
        case (y, cnt) =>
          (y, nSampleMinority * 1.0 / cnt)
      }.filter(_._2 > 0)
    }
  }

  def samplingStrategyNotMajority(targetStats: Map[String, Long], samplingType: String): Map[String, Double] = {
    if (samplingType.equals(SAMPLING_TYPE_OVER_SAMPLING)) {
      val nSampleMajority = targetStats.values.max
      val classMajority = targetStats.maxBy(_._2)._1

      targetStats.filterNot(_._1.equals(classMajority)).map {
        case (y, cnt) =>
          (y, (nSampleMajority - cnt) * 1.0 / cnt)
      }.filter(_._2 > 0)
    } else {
      val nSampleMinority = targetStats.values.min
      val classMajority = targetStats.maxBy(_._2)._1

      targetStats.filterNot(_._1.equals(classMajority)).map {
        case (y, cnt) =>
          (y, nSampleMinority * 1.0 / cnt)
      }.filter(_._2 > 0)
    }
  }

  def samplingStrategyAll(targetStats: Map[String, Long], samplingType: String): Map[String, Double] = {
    if (samplingType.equals(SAMPLING_TYPE_OVER_SAMPLING)) {
      val nSampleMajority = targetStats.values.max

      targetStats.map {
        case (y, cnt) =>
          (y, (nSampleMajority - cnt) * 1.0 / cnt)
      }.filter(_._2 > 0)
    } else {
      val nSampleMinority = targetStats.values.min

      targetStats.map {
        case (y, cnt) =>
          (y, nSampleMinority * 1.0 / cnt)
      }.filter(_._2 > 0)
    }
  }

  def samplingStrategyNumber(targetStats: Map[String, Long], samplingType: String, number: Long): Map[String, Double] = {
    if (samplingType.equals(SAMPLING_TYPE_OVER_SAMPLING)) {
      targetStats.filter(_._2 < number).map {
        case (y, cnt) =>
          (y, (number - cnt) * 1.0 / cnt)
      }.filter(_._2 > 0)
    } else {
      targetStats.filter(_._2 > number).map {
        case (y, cnt) =>
          (y, number * 1.0 / cnt)
      }.filter(_._2 > 0)
    }
  }

  def samplingStrategyAuto(targetStats: Map[String, Long], samplingType: String): Map[String, Double] = {
    if (samplingType.equals(SAMPLING_TYPE_OVER_SAMPLING)) {
      samplingStrategyNotMajority(targetStats, samplingType)
    } else {
      samplingStrategyNotMinority(targetStats, samplingType)
    }
  }
}