package com.netease.easyml.ml.sklearn.cluster

import com.netease.easyml.common.reflection.FromNDArray
import com.netease.easyml.ml.sklearn.{DefaultSklearnReader, SklearnReader}
import com.netease.easyml.ml.util.Utils
import net.razorvine.pickle.objects.ClassDict
import numpy.core.NDArray
import org.apache.spark.ml.clustering.{GaussianMixtureModel => SparkGaussianMixtureModel}
import org.apache.spark.ml.linalg.Matrices
import org.apache.spark.ml.stat.distribution.MultivariateGaussian

import scala.collection.mutable.ArrayBuffer

/**
 * Created by linjiuning on 2020/8/11.
 */
object GaussianMixture extends SklearnReader[SparkGaussianMixtureModel] {
  /**
   * Returns an Spark ML instance for this class.
   */
  override def readPickle(pickle: ClassDict): SparkGaussianMixtureModel = {
    val covarianceType = pickle.get("covariance_type").asInstanceOf[String]
    require(covarianceType.equals("full"), s"only support covariance_type = full, but get $covarianceType instead.")
    val weightsArray = pickle.get("weights_").asInstanceOf[NDArray]
    val weights = FromNDArray.toArray(weightsArray).asInstanceOf[Array[Double]]
    val meansArray = pickle.get("means_").asInstanceOf[NDArray]
    val covariancesArray = pickle.get("covariances_").asInstanceOf[NDArray]
    val means = FromNDArray.toMatrix(meansArray)
    val nFeatures = means.numCols
    val covariances = FromNDArray.toArray(covariancesArray).asInstanceOf[Array[Double]]
    val gaussians = new ArrayBuffer[MultivariateGaussian]
    var i = 0
    val step = nFeatures * nFeatures
    for (mean <- means.rowIter) {
      val array = covariances.slice(i, i + step)
      val covariance = Matrices.dense(nFeatures, nFeatures, array)
      val gaussian = new MultivariateGaussian(mean, covariance)
      gaussians.append(gaussian)
      i += step
    }
    val model = Utils.newGaussianMixtureModel(weights, gaussians.toArray)
    DefaultSklearnReader.getAndSetValues(model, pickle)
    model
  }
}
