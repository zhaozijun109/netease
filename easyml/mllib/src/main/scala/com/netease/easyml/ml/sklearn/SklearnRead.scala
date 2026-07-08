package com.netease.easyml.ml.sklearn

import com.netease.easyml.common.util.PythonUtil
import net.razorvine.pickle.objects.ClassDict
import org.apache.spark.ml.util.Identifiable

/**
 * Created by linjiuning on 2020/8/4.
 */
trait SklearnReader[T] {

  /**
   * Returns an Spark ML instance for this class.
   */
  def readPickle(pickle: ClassDict): T

  /**
   * Reads an Sklearn ML instance from the input path.
   *
   * @note Implementing classes should override this to be Java-friendly.
   */
  def loadPickle(path: String): T = {
    val obj = PythonUtil.unpickle(path).asInstanceOf[ClassDict]
    readPickle(obj)
  }
}

object DefaultSklearnReader {
  val defaultInputCol: String = "inputCol"
  val defaultOutputCol: String = "outputCol"

  def getAndSetValues(instance: Object,
                      pickle: ClassDict,
                      skipParams: Option[List[String]] = None,
                      mapping: Option[Map[String, String]] = None,
                      setDefaultCols: Option[Boolean] = None): Unit = {
    val dict = pickle.asInstanceOf[ClassDict]
    if (mapping.isDefined) {
      mapping.get.foreach {
        case (o, n) =>
          if (dict.containsKey(o)) {
            val value = dict.remove(o)
            dict.put(n, value)
          }
      }
    }
    if (setDefaultCols.getOrElse(true)) {
      if (!pickle.containsKey(defaultInputCol))
        pickle.put(defaultInputCol, defaultInputCol)
      if (!pickle.containsKey(defaultOutputCol))
        pickle.put(defaultOutputCol, defaultOutputCol)
    }
    skipParams.foreach(it => it.foreach(dict.remove))
    SklearnUtils.read(instance, dict)
  }
}


trait DefaultSklearnReader[T] extends SklearnReader[T] {

  override def readPickle(pickle: ClassDict): T = {
    val cls = SklearnUtils.getMLClass(pickle).get
    val instance = cls.getConstructor(classOf[String]).newInstance(Identifiable.randomUID(cls.getSimpleName)).asInstanceOf[Object]
    DefaultSklearnReader.getAndSetValues(instance, pickle)
    instance.asInstanceOf[T]
  }
}