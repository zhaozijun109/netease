package com.netease.easyml.ml.sklearn.preprocessing

import com.netease.easyml.ml.sklearn.{DefaultSklearnReader, SklearnReader}
import net.razorvine.pickle.objects.ClassDict
import org.apache.spark.ml.feature.{Binarizer => SparkBinarizer}

/**
 * Created by linjiuning on 2020/8/9.
 */
object Binarizer extends SklearnReader[SparkBinarizer] {
  /**
   * Returns an Spark ML instance for this class.
   */
  override def readPickle(pickle: ClassDict): SparkBinarizer = {
    val binarizer = new SparkBinarizer()
    DefaultSklearnReader.getAndSetValues(binarizer, pickle)
    binarizer
  }
}
