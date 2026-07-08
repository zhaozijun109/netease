package com.netease.easyml.ml.sklearn.linear_model

import com.netease.easyml.common.reflection.FromNDArray
import com.netease.easyml.ml.sklearn.SklearnReader
import com.netease.easyml.ml.util.Utils
import net.razorvine.pickle.objects.ClassDict
import numpy.core.NDArray
import org.apache.spark.ml.classification.{LogisticRegressionModel => SparkLogisticRegressionModel}

object LogisticRegressionModel extends SklearnReader[SparkLogisticRegressionModel] {
  /**
   * Returns an Spark ML instance for this class.
   */
  override def readPickle(pickle: ClassDict): SparkLogisticRegressionModel = {
    val coef_ = pickle.get("coef_").asInstanceOf[NDArray]
    val coef = FromNDArray.toMatrix(coef_)
    val intercept_ = pickle.get("intercept_").asInstanceOf[NDArray]
    val intercept = FromNDArray.toVector(intercept_)
    var numClass = coef.numRows
    if (numClass == 1) {
      numClass = 2
    }
    val isMultinomial = coef.numRows > 1
    val model = Utils.newLogisticRegressionModel(coef, intercept, numClass, isMultinomial)
    if (!isMultinomial) {
      model.setThreshold(0.5)
    }
    model
  }
}

