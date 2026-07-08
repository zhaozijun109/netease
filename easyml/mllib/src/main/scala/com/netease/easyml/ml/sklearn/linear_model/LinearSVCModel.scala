package com.netease.easyml.ml.sklearn.linear_model

import com.netease.easyml.common.reflection.FromNDArray
import com.netease.easyml.ml.sklearn.SklearnReader
import com.netease.easyml.ml.util.Utils
import net.razorvine.pickle.objects.ClassDict
import numpy.core.NDArray
import org.apache.spark.ml.Model
import org.apache.spark.ml.classification.LinearSVC

/**
 * Created by linjiuning on 2020/8/12.
 */
object LinearSVCModel extends SklearnReader[Model[_]] {
  /**
   * Returns an Spark ML instance for this class.
   */
  override def readPickle(pickle: ClassDict): Model[_] = {
    val multiClass = pickle.get("multi_class").asInstanceOf[String]
    if (!multiClass.equals("ovr")) {
      throw new IllegalArgumentException(s"only support multi_class = ovr, but get $multiClass instead.")
    }
    val coefArray = pickle.get("coef_").asInstanceOf[NDArray]
    val coef = FromNDArray.toMatrix(coefArray)
    val interceptArray = pickle.get("intercept_").asInstanceOf[NDArray]
    val intercept = FromNDArray.toVector(interceptArray).toArray
    val isMulti = intercept.length > 1
    if (isMulti) {
      val models = coef.rowIter.zipWithIndex.map {
        case (vector, i) =>
          Utils.newLinearSVC(vector, intercept(i)).setParent(new LinearSVC())
      }
      Utils.newOneVsRestModel(models.toArray)
    } else {
      val coefficients = coef.rowIter.next()
      val intercept_ = intercept(0)
      Utils.newLinearSVC(coefficients, intercept_)
    }
  }
}
