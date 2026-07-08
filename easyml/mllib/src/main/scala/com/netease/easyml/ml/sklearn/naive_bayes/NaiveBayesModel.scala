package com.netease.easyml.ml.sklearn.naive_bayes

import com.netease.easyml.common.reflection.FromNDArray
import com.netease.easyml.ml.sklearn.SklearnReader
import com.netease.easyml.ml.util.Utils
import net.razorvine.pickle.objects.ClassDict
import numpy.core.NDArray
import org.apache.spark.ml.classification.{NaiveBayesModel => SparkNaiveBayesModel}

/**
 * Created by linjiuning on 2020/8/12.
 */
object NaiveBayesModel extends SklearnReader[SparkNaiveBayesModel] {
  /**
   * Returns an Spark ML instance for this class.
   */
  override def readPickle(pickle: ClassDict): SparkNaiveBayesModel = {
    val className = pickle.getClassName
    val modelType = className.split("\\.").last.toLowerCase.replaceAll("nb$", "")
    val piArray = pickle.get("class_log_prior_").asInstanceOf[NDArray]
    val pi = FromNDArray.toVector(piArray)
    val thetaArray = pickle.get("feature_log_prob_").asInstanceOf[NDArray]
    val theta = FromNDArray.toMatrix(thetaArray)
    Utils.newNaiveBayesModel(modelType, pi, theta)
  }
}
