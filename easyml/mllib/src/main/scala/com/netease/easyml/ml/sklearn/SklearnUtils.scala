package com.netease.easyml.ml.sklearn

import com.netease.easyml.common.reflection._
import com.netease.easyml.common.util.{IOUtil, PythonUtil}
import net.razorvine.pickle.objects.ClassDict
import org.apache.spark.ml.Transformer
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConversions._

/**
 * Created by linjiuning on 2020/8/4.
 */
object SklearnUtils {
  val log: Logger = LoggerFactory.getLogger(SklearnUtils.getClass)

  val KEY_CLASS: String = "__class__"

  lazy val env: Environment = {
    val env = new Environment
    env
      .add(new FromScalar)
      .add(new FromNDArray)
      .add(new FromCSRMatrix)
      .add(new FromClassDict)
      .add(new FromArrayToMLParam)
      .add(new FromMapToMLParam)
      .add(new FromObjectToMLParam)
      .add(new FromValue)
    env
  }

  lazy val (sklearnToML, mlToSklearn) = {
    var sklearnToML = Map.empty[String, String]
    var mlTosklearn = Map.empty[String, String]
    IOUtil.readLines(IOUtil.getResourceAsStream("sklearn2spark.properties")).foreach(line => {
      var Array(sklearn, ml) = line.split("=")
      sklearn = sklearn.trim
      ml = ml.trim
      sklearnToML += (sklearn -> ml)
      mlTosklearn += (ml -> sklearn)
    })
    (sklearnToML, mlTosklearn)
  }

  def getSklearnClassName(pickle: ClassDict): String = {
    pickle.getOrDefault(KEY_CLASS, "").asInstanceOf[String]
  }

  def getMLClass(pickle: ClassDict): Option[Class[_]] = {
    val name = getSklearnClassName(pickle)
    if (!sklearnToML.contains(name)) {
      throw new IllegalArgumentException(s"Sklearn class $name is not supported.")
    } else {
      val clazzName = sklearnToML(name)
      try {
        Some(Class.forName(clazzName))
      } catch {
        case _: ClassNotFoundException =>
          log.warn(s"ML class $clazzName not found.")
          None
      }
    }
  }

  def read(clazz: Class[_], pickle: Object): Option[Object] = {
    try {
      val method = clazz.getDeclaredMethod("readPickle", classOf[Object])
      method.setAccessible(true)
      if (clazz.isAssignableFrom(method.getReturnType))
        Some(method.invoke(null, pickle))
      else
        None
    } catch {
      case _: Exception =>
        log.warn(s"Failed to instance ${clazz.getSimpleName}.")
        None
    }
  }

  def getSklearnClassNames(pickle: Object): Map[String, String] = {
    val map = Map.empty[String, String]

    def walk(prefix: String, pickle: Object): Unit = {
      pickle match {
        case dict: ClassDict =>
          if (dict.containsKey(KEY_CLASS)) {
            map.put(prefix, dict.get(KEY_CLASS).asInstanceOf[String])
          }
          val newPrefix = if (prefix.endsWith(".")) {
            prefix
          } else {
            prefix + "."
          }
          dict.entrySet().foreach(entry => {
            walk(newPrefix + entry.getKey, entry.getValue)
          })
        case _ =>
      }
    }

    val prefix = "."
    walk(prefix, pickle)
    map
  }


  def read(path: String): Transformer = {
    val pickle = PythonUtil.unpickle(path)
    env.fromValue(pickle).asInstanceOf[Transformer]
  }

  def read(pickle: Object): Transformer = {
    env.fromValue(pickle).asInstanceOf[Transformer]
  }

  def read(instance: Object, pickle: java.util.Map[String, Object]): Unit = {
    env.fromValue(instance, pickle)
  }

  def read[M](path: String, cls: Class[M]): M = {
    read(path).asInstanceOf[M]
  }
}
