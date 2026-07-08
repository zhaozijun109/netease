package com.netease.easyml.ml.sklearn.preprocessing

import com.netease.easyml.annotation.Register
import com.netease.easyml.ml.sklearn.DefaultSklearnReader
import com.netease.easyml.ml.util.{SchemaUtils, VectorUtils}
import org.apache.spark.ml.UnaryTransformer
import org.apache.spark.ml.linalg.Vector
import org.apache.spark.ml.param.{Param, ParamValidators}
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.types.DataType

/**
 * Created by linjiuning on 2020/8/7.
 */

@Register(prefix = "sklearn.")
class Normalizer(override val uid: String)
  extends UnaryTransformer[Vector, Vector, Normalizer] with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("normalizer"))

  val norm: Param[String] =
    new Param[String](this, "norm", "The norm to use to normalize each non zero sample. " +
      "If norm=’max’ is used, values will be rescaled by the maximum of the absolute values.",
      ParamValidators.inArray(Array("l1", "l2", "max")))

  setDefault(norm -> "l2")

  def getNorm: String = $(norm)

  def setNorm(value: String): this.type = set(norm, value)

  override protected def createTransformFunc: Vector => Vector = {
    (vector: Vector) => {
      if ($(norm).equals("l1")) {
        VectorUtils.normalize(vector, 1.0)
      } else if ($(norm).equals("l2")) {
        VectorUtils.normalize(vector, 2.0)
      } else {
        VectorUtils.normalizeMax(vector)
      }
    }
  }

  override protected def outputDataType: DataType = SchemaUtils.vectorUDT
}


object Normalizer extends DefaultParamsReadable[Normalizer] with DefaultSklearnReader[Normalizer] {

  override def load(path: String): Normalizer = super.load(path)
}
