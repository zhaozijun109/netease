package com.netease.easyml.ml.sklearn.preprocessing

import com.netease.easyml.common.reflection.FromNDArray
import com.netease.easyml.ml.sklearn.SklearnReader
import com.netease.easyml.ml.util.Utils
import net.razorvine.pickle.objects.ClassDict
import numpy.core.NDArray
import org.apache.spark.ml.feature.{MinMaxScalerModel => SparkMinMaxScalerModel}

/**
 * Created by linjiuning on 2020/8/7.
 */
object MinMaxScalerModel extends SklearnReader[SparkMinMaxScalerModel] {
  /**
   * Returns an Spark ML instance for this class.
   */
  override def readPickle(pickle: ClassDict): SparkMinMaxScalerModel = {
    val minArray = pickle.get("data_min_").asInstanceOf[NDArray]
    val min = FromNDArray.toVector(minArray)
    val maxArray = pickle.get("data_max_").asInstanceOf[NDArray]
    val max = FromNDArray.toVector(maxArray)
    Utils.newMinMaxScalerModel(min, max)
      .setMin(0.0)
      .setMax(1.0)
  }
}
