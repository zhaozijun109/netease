package com.netease.easyml.ml.sklearn.preprocessing

import com.netease.easyml.ml.sklearn.{DefaultSklearnReader, SklearnReader}
import com.netease.easyml.ml.util.Utils
import net.razorvine.pickle.objects.ClassDict
import org.apache.spark.ml.feature.{StandardScalerModel => SparkStandardScalerModel}
import org.apache.spark.ml.linalg.Vector

/**
 * Created by linjiuning on 2020/8/7.
 */
object StandardScalerModel extends SklearnReader[SparkStandardScalerModel] {
  /**
   * Returns an Spark ML instance for this class.
   */
  override def readPickle(pickle: ClassDict): SparkStandardScalerModel = {
    val model = Utils.newStandardScalerModel(null.asInstanceOf[Vector], null.asInstanceOf[Vector])
    DefaultSklearnReader.getAndSetValues(model, pickle, mapping = Some(Map("mean_" -> "mean", "scale_" -> "std")))
    model
  }
}
