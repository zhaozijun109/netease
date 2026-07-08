package com.netease.easyml.ml.sklearn.preprocessing

import com.netease.easyml.ml.sklearn.{DefaultSklearnReader, SklearnReader}
import com.netease.easyml.ml.util.{SchemaUtils, Utils}
import net.razorvine.pickle.objects.ClassDict
import org.apache.spark.ml.Estimator
import org.apache.spark.ml.feature.StandardScalerModel
import org.apache.spark.ml.linalg.{SparseVector, Vector, Vectors}
import org.apache.spark.ml.param._
import org.apache.spark.ml.param.shared.{HasInputCol, HasOutputCol}
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.types.{DoubleType, StructField, StructType}
import org.apache.spark.sql.{Dataset, Row}
import org.apache.spark.storage.StorageLevel

import scala.collection.mutable.ArrayBuffer

/**
 * Created by linjiuning on 2020/8/19.
 */
trait RobustScalerParams extends Params with HasInputCol with HasOutputCol {

  val withCentering: BooleanParam = new BooleanParam(this, "withCentering",
    "If True, center the data before scaling. " +
      "This will cause ``transform`` to raise an exception when attempted on " +
      "sparse matrices, because centering them entails building a dense " +
      "matrix which in common use cases is likely to be too large to fit in memory.")

  def getWithCentering: Boolean = $(withCentering)

  val withScaling: BooleanParam = new BooleanParam(this, "withScaling",
    "If True, scale the data to interquartile range.")

  def getWithScaling: Boolean = $(withScaling)

  val quantileRange: DoubleArrayParam = new DoubleArrayParam(this, "quantileRange", "tuple (q_min, q_max), 0.0 < q_min < q_max < 100.0 " +
    "Default: (25.0, 75.0) = (1st quantile, 3rd quantile) = IQR", (range: Array[Double]) => {
    range.length == 2 && range(0) > 0 && range(1) > range(0) && range(1) < 100
  })

  val relativeError = new DoubleParam(this, "relativeError", "The relative target precision " +
    "for the approximate quantile algorithm used to generate buckets. " +
    "Must be in the range [0, 1].", ParamValidators.inRange(0.0, 1.0))

  def getRelativeError: Double = getOrDefault(relativeError)

  /** Validates and transforms the input schema. */
  protected def validateAndTransformSchema(schema: StructType): StructType = {
    SchemaUtils.checkColumnType(schema, $(inputCol), SchemaUtils.vectorUDT)
    val outputFields = schema.fields :+ StructField($(outputCol), SchemaUtils.vectorUDT, false)
    StructType(outputFields)
  }

  setDefault(withCentering -> true, withScaling -> true, quantileRange -> Array(25.0, 75.0), relativeError -> 0.001)
}

class RobustScaler(override val uid: String)
  extends Estimator[StandardScalerModel] with RobustScalerParams with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("robustScal"))

  def setInputCol(value: String): this.type = set(inputCol, value)

  def setOutputCol(value: String): this.type = set(outputCol, value)

  def setWithCentering(value: Boolean): this.type = set(withCentering, value)

  def setWithScaling(value: Boolean): this.type = set(withScaling, value)

  def setRelativeError(value: Double): this.type = set(relativeError, value)

  override def fit(dataset: Dataset[_]): StandardScalerModel = {
    transformSchema(dataset.schema, logging = true)

    val handelPersistent = dataset.storageLevel == StorageLevel.NONE
    if (handelPersistent) dataset.persist(StorageLevel.MEMORY_AND_DISK)

    val vector = dataset.select($(inputCol)).rdd.map {
      case Row(v: Vector) => v
    }.first()

    if ($(withCentering))
      vector match {
        case SparseVector(i, ints, doubles) =>
          throw new IllegalArgumentException("Cannot center sparse matrices: use `with_centering=False` instead. See docstring for motivation and alternatives.")
        case _: Vector =>
      }

    val center = new ArrayBuffer[Double]
    val scale = new ArrayBuffer[Double]

    val probabilities = new ArrayBuffer[Double]
    if ($(withCentering))
      probabilities += 0.5
    if ($(withScaling))
      probabilities ++= $(quantileRange).map(_ / 100.0)
    val distinct = probabilities.toArray.distinct
    // Warning: the behavior of dataset.stat.approxQuantile is different from np.nanpercentile
    // so that the result is not consistent with sklearn RobustScaler
    if (distinct.nonEmpty) {
      val (cols, tmpDf) = Utils.flatten(dataset, $(inputCol), DoubleType)
      tmpDf.stat.approxQuantile(cols, distinct, $(relativeError))
        .foreach(array => {
          if ($(withCentering)) {
            val c = array(distinct.indexOf(0.5))
            center += c
          }
          if ($(withScaling)) {
            val s = array(distinct.indexOf($(quantileRange)(1) / 100.0)) - array(distinct.indexOf($(quantileRange)(0) / 100.0))
            scale += (if (s == 0) 1.0 else s)
          }
        })
    }
    if (handelPersistent) dataset.unpersist()

    val model = copyValues(Utils.newStandardScalerModel(uid, Vectors.dense(center.toArray), Vectors.dense(scale.toArray)).setParent(this))
    Utils.set(model, "withMean", $(withCentering))
    Utils.set(model, "withStd", $(withScaling))
  }

  override def transformSchema(schema: StructType): StructType = {
    validateAndTransformSchema(schema)
  }

  override def copy(extra: ParamMap): RobustScaler = defaultCopy(extra)
}

object RobustScaler extends DefaultParamsReadable[RobustScaler] with SklearnReader[StandardScalerModel] {

  override def load(path: String): RobustScaler = super.load(path)

  /**
   * Returns an Spark ML instance for this class.
   */
  override def readPickle(pickle: ClassDict): StandardScalerModel = {
    val model = Utils.newStandardScalerModel(null.asInstanceOf[Vector], null.asInstanceOf[Vector])
    DefaultSklearnReader.getAndSetValues(model, pickle, mapping = Some(Map("center_" -> "mean", "scale_" -> "std",
      "with_centering" -> "withMean", "with_scaling" -> "withStd")))
    model
  }
}


