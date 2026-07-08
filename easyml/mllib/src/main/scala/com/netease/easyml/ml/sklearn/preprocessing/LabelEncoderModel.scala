package com.netease.easyml.ml.sklearn.preprocessing

import com.netease.easyml.common.reflection.FromNDArray
import com.netease.easyml.ml.sklearn.SklearnReader
import net.razorvine.pickle.objects.ClassDict
import numpy.core.NDArray
import org.apache.spark.ml.feature.StringIndexerModel

/**
 * Created by linjiuning on 2020/8/7.
 */

object LabelEncoderModel extends SklearnReader[StringIndexerModel] {
  /**
   * Returns an Spark ML instance for this class.
   */
  override def readPickle(pickle: ClassDict): StringIndexerModel = {
    val array = pickle.get("classes_").asInstanceOf[NDArray]
    val labels = FromNDArray.toArray(array).asInstanceOf[Array[String]]
    new StringIndexerModel(labels)
  }
}
