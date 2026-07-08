package com.netease.easyml.ml.sklearn.preprocessing

import com.netease.easyml.common.reflection.FromNDArray
import com.netease.easyml.ml.sklearn.SklearnReader
import com.netease.easyml.ml.util.Utils
import net.razorvine.pickle.objects.ClassDict
import numpy.core.NDArray
import org.apache.spark.ml.feature.{MaxAbsScalerModel => SparkMaxAbsScalerModel}

/**
 * Created by linjiuning on 2020/8/7.
 */
object MaxAbsScalerModel extends SklearnReader[SparkMaxAbsScalerModel] {
  /**
   * Returns an Spark ML instance for this class.
   */
  override def readPickle(pickle: ClassDict): SparkMaxAbsScalerModel = {
    val array = pickle.get("scale_").asInstanceOf[NDArray]
    val scale = FromNDArray.toVector(array)
    Utils.newMaxAbsScalerModel(scale)
  }
}
