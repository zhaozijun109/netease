package com.netease.easyml.ml.sklearn.pipeline

import java.util

import com.netease.easyml.ml.sklearn.{SklearnReader, SklearnUtils}
import com.netease.easyml.ml.util.Utils
import net.razorvine.pickle.objects.ClassDict
import org.apache.spark.ml.{PipelineModel => SparkPipelineModel}

import scala.collection.JavaConversions._

/**
 * Created by linjiuning on 2020/8/5.
 */
object PipelineModel extends SklearnReader[SparkPipelineModel] {
  /**
   * Returns an Spark ML instance for this class.
   */
  override def readPickle(pickle: ClassDict): SparkPipelineModel = {
    val steps = pickle.get("steps").asInstanceOf[util.List[_]].toSeq.toArray
    val stages = steps.map(tuple => {
      val dict = tuple.asInstanceOf[Array[_]](1).asInstanceOf[ClassDict]
      SklearnUtils.read(dict)
    })
    Utils.newPipeLineModel(stages)
  }
}
