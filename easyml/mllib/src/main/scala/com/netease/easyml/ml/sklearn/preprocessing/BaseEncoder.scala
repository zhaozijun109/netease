package com.netease.easyml.ml.sklearn.preprocessing

import java.util

import com.netease.easyml.common.reflection.FromNDArray
import com.netease.easyml.common.util.ArrayUtil
import com.netease.easyml.ml.util.SchemaUtils
import net.razorvine.pickle.objects.ClassDict
import numpy.core.NDArray
import org.apache.spark.ml.linalg.Vector
import org.apache.spark.ml.param.shared.{HasInputCol, HasOutputCol}
import org.apache.spark.ml.param.{Param, ParamValidators, Params}
import org.apache.spark.ml.util.DefaultParamsWritable
import org.apache.spark.ml.{Estimator, Model}
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.types._
import org.apache.spark.storage.StorageLevel

import scala.collection.JavaConversions._

/**
 * Created by linjiuning on 2020/8/11.
 */
trait BaseEncoderParams extends Params with HasInputCol with HasOutputCol {
  val handleUnknown: Param[String] = new Param[String](this, "handleUnknown",
    "Whether to raise an error or ignore if an unknown categorical feature is present during transform (default is to raise). " +
      "When this parameter is set to ‘ignore’ and an unknown category is encountered during transform, the resulting one-hot encoded columns for this feature will be all zeros. " +
      "In the inverse transform, an unknown category will be denoted as None.",
    ParamValidators.inArray(Array("ignore", "error")))

  def getHandleUnknown: String = $(handleUnknown)

  setDefault(handleUnknown -> "error")

  protected def validateAndTransformSchema(schema: StructType): StructType = {
    SchemaUtils.checkColumnTypes(schema, $(inputCol), Seq(StringType, ArrayType(StringType), SchemaUtils.vectorUDT))
    SchemaUtils.appendColumn(schema, $(outputCol), SchemaUtils.vectorUDT)
  }
}

abstract class BaseEncoderEstimator[T <: BaseEncoderModel[T]] extends Estimator[T]
  with BaseEncoderParams with DefaultParamsWritable {

  def setInputCol(value: String): this.type = set(inputCol, value)

  def setOutputCol(value: String): this.type = set(outputCol, value)

  def setHandleUnknown(value: String): this.type = set(handleUnknown, value)

  override def transformSchema(schema: StructType): StructType = {
    validateAndTransformSchema(schema)
  }

  protected def fit_(dataset: Dataset[_]): Array[Array[Any]] = {
    val handelPersistent = dataset.storageLevel == StorageLevel.NONE
    if (handelPersistent) dataset.persist(StorageLevel.MEMORY_AND_DISK)
    val categories = dataset.select($(inputCol)).rdd
      .map(row => {
        val seq = row.get(0) match {
          case v: Seq[Any] =>
            v
          case v: Vector =>
            v.toArray.toSeq
          case v: Any =>
            Seq(v)
        }
        seq.map(Set(_))
      }).reduce((it1, it2) => {
      it1.zip(it2).map {
        case (s1, s2) =>
          s1 ++ s2
      }
    }).map(it => {
      val array = it.toArray
      if (array.isEmpty || array(0).getClass == classOf[String]) {
        array.map(_.toString).sorted.map(_.asInstanceOf[Any])
      } else {
        array.map(_.toString.toDouble).sorted.map(_.asInstanceOf[Any])
      }
    }).toArray

    if (handelPersistent) dataset.unpersist()
    categories
  }

}

abstract class BaseEncoderModel[T <: BaseEncoderModel[T]] extends Model[T]
  with BaseEncoderParams with DefaultParamsWritable {

  val categories: Array[Array[Any]]

  def setInputCol(value: String): this.type = set(inputCol, value)

  def setOutputCol(value: String): this.type = set(outputCol, value)

  def setHandleUnknown(value: String): this.type = set(handleUnknown, value)

  override def transformSchema(schema: StructType): StructType = {
    validateAndTransformSchema(schema)
  }

  def categoryDataType: DataType = {
    val dtype = if (categories.isEmpty) {
      StringType
    } else {
      categories(0)(0) match {
        case _: String =>
          StringType
        case _: Double =>
          DoubleType
      }
    }
    ArrayType(ArrayType(dtype))
  }
}

object BaseEncoder {
  def parseCategories(pickle: ClassDict): Unit = {
    val categoriesList = pickle.remove("categories_").asInstanceOf[util.List[NDArray]]
    var categories = categoriesList.map(it => FromNDArray.toArray(it).asInstanceOf[Array[_]]).toArray
    val size = categories.map(ArrayUtil.componentType).distinct.length
    if (size > 1) {
      categories = categories.map(it => {
        it.map(_.toString)
      })
    }
    pickle.put("categories", categories.map(it => it.map(_.asInstanceOf[Any])))
  }
}
