package com.netease.easyml.ml.param

import org.apache.spark.ml.param._

/**
 * Created by linjiuning on 2020/7/1.
 */

trait HasNumEstimator extends Params {

  /**
   * Param for num estimator.
   *
   * @group param
   */
  final val numEstimator: IntParam = new IntParam(this, "numEstimator",
    "num estimator for ensemble model")

  /** @group getParam */
  final def getNumEstimator: Int = $(numEstimator)
}

trait HasVectorSize extends Params {

  /**
   * The dimension of vector.
   *
   * @group param
   */
  final val vectorSize = new IntParam(
    this, "vectorSize", "the dimension of vector",
    ParamValidators.gt(0))

  /** @group getParam */
  def getVectorSize: Int = $(vectorSize)
}

trait HasSeparator extends Params {

  /**
   * The separator of string.
   *
   * @group param
   */
  final val separator = new Param[String](
    this, "separator", "the separator of string")
  setDefault(separator, " ")

  /** @group getParam */
  def getSeparator: String = $(separator)
}

trait HasNormalize extends Params {

  /**
   * Whether do normalize.
   *
   * @group param
   */
  final val normalize = new BooleanParam(
    this, "normalize", "whether do normalize")
  setDefault(normalize, true)

  /** @group getParam */
  def getNormalize: Boolean = $(normalize)
}

trait HasEpsilon extends Params {

  /**
   * Epsilon value.
   *
   * @group param
   */
  final val epsilon = new DoubleParam(
    this, "epsilon", "epsilon value")
  setDefault(epsilon, 1e-15)

  /** @group getParam */
  def getEpsilon: Double = $(epsilon)
}

trait HasPosLabel extends Params {

  /**
   * Label of the positive class.
   *
   * @group param
   */
  final val posLabel = new DoubleParam(
    this, "posLabel", "label of the positive class.")

  /** @group getParam */
  def getPosLabel: Double = $(posLabel)
}

trait HasK extends Params {
  final val k = new IntParam(this, "k",
    "The ranking position value, must be > 0.",
    ParamValidators.gt(0))

  /** @group getParam */
  def getK: Int = $(k)
}

trait HasNumPartitions extends Params {
  final val numPartitions = new IntParam(this, "numPartitions",
    "The num partitions of dataset, must be >= 0, 0 means numPartitions = numCores.",
    ParamValidators.gtEq(0))

  /** @group getParam */
  def getNumPartitions: Int = $(numPartitions)
}

trait HasSQLs extends Params {
  final val sqls: StringArrayParam = new StringArrayParam(this, "sqls", "Specifies the sqls.")

  def getSqls: Array[String] = $(sqls)

  setDefault(sqls, Array.empty[String])
}

trait HasColumns extends Params {
  final val columns: StringArrayParam = new StringArrayParam(this, "columns", "Specifies the select columns")

  def getColumns: Array[String] = $(columns)

  setDefault(columns, Array.empty[String])
}

trait HasBatchSize extends Params {
  final val batchSize = new IntParam(this, "batchSize",
    "The batch size, must be > 0.",
    ParamValidators.gt(0))

  /** @group getParam */
  def getBatchSize: Int = $(batchSize)
}


trait HasPath extends Params {

  /**
   * The path of model.
   *
   * @group param
   */
  final val path = new Param[String](
    this, "path", "the path of model")

  /** @group getParam */
  def getPath: String = $(path)
}

trait HasNorm extends Params {
  val norm: Param[String] =
    new Param[String](this, "norm", "Each output row will have unit norm, either: " +
      "* 'l2': Sum of squares of vector elements is 1. The cosine " +
      "similarity between two vectors is their dot product when l2 norm has been applied. " +
      "* 'l1': Sum of absolute values of vector elements is 1.",
      ParamValidators.inArray(Array("l1", "l2", "none")))

  def getNorm: String = $(norm)

  def getNormP: Double = {
    $(norm) match {
      case "l1" =>
        1.0
      case "l2" =>
        2.0
      case _ =>
        0.0
    }
  }
}

trait HasRandomState extends Params {

  /**
   * randomness
   *
   * @group param
   */
  final val randomState: LongParam = new LongParam(this, "randomState", "Controls the randomness.")


  /** @group getParam */
  def getRandomState: Long = $(randomState)
}

trait HasLowercase extends Params {

  /**
   * whether to convert all characters to lowercase
   *
   * @group param
   */
  val lowercase: BooleanParam = new BooleanParam(this, "lowercase",
    "whether to convert all characters to lowercase.")

  /** @group getParam */
  def getLowercase: Boolean = $(lowercase)
}

trait HasNgramRange extends Params {

  /**
   * ngram range
   *
   * @group param
   */
  val ngramRange: IntArrayParam = new IntArrayParam(this, "ngramRange", "The lower and upper boundary of the range of n-values for different " +
    "word n-grams or char n-grams to be extracted. All values of n such " +
    "such that min_n <= n <= max_n will be used. For example an " +
    "``ngram_range`` of ``(1, 1)`` means only unigrams, ``(1, 2)`` means " +
    "unigrams and bigrams, and ``(2, 2)`` means only bigrams.")

  /** @group getParam */
  def getNgramRange: Array[Int] = $(ngramRange)
}