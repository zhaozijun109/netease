package com.netease.easyml.ml.sklearn.cluster

import com.netease.easyml.common.reflection.FromNDArray
import com.netease.easyml.ml.sklearn.{DefaultSklearnReader, SklearnReader}
import com.netease.easyml.ml.util.Utils
import net.razorvine.pickle.objects.ClassDict
import numpy.core.NDArray
import org.apache.spark.ml.clustering.{KMeansModel => SparkKMeansModel}

/**
 * Created by linjiuning on 2020/8/11.
 */
object KMeansModel extends SklearnReader[SparkKMeansModel] {
  /**
   * Returns an Spark ML instance for this class.
   */
  override def readPickle(pickle: ClassDict): SparkKMeansModel = {
    val nDArray = pickle.get("cluster_centers_").asInstanceOf[NDArray]
    val matrix = FromNDArray.toMatrix(nDArray)
    val clusterCenters = matrix.rowIter.toArray
    val model = Utils.newKMeansModel(clusterCenters)
    DefaultSklearnReader.getAndSetValues(model, pickle, mapping = Some(Map("n_clusters" -> "k")))
    model
  }
}
