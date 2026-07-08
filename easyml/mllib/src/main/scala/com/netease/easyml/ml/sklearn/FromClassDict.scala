package com.netease.easyml.ml.sklearn

import java.lang.reflect.{Field, Method}

import com.netease.easyml.common.reflection.FromValue
import net.razorvine.pickle.objects.ClassDict
import org.apache.spark.ml.util.Identifiable;

/**
 * Created by linjiuning on 2020/8/4.
 */
class FromClassDict extends FromValue {
  override def isMatch(field: Field, value: Object): Boolean = {
    value.isInstanceOf[ClassDict]
  }

  def readable(clazz: Class[_]): Option[Method] = {
    try {
      val method = clazz.getDeclaredMethod("readPickle", classOf[ClassDict])
      Some(method)
    } catch {
      case _: NoSuchMethodException =>
        None
    }
  }

  override def fromValue(field: Field, value: Object): Object = {
    val classDict = value.asInstanceOf[ClassDict]
    val mlClazz = SklearnUtils.getMLClass(classDict)
    if (mlClazz.isDefined) {
      val clazz = mlClazz.get
      val maybeMethod = readable(clazz)
      if (maybeMethod.isDefined) {
        maybeMethod.get.invoke(null, value)
      } else {
        val constructor = clazz.getConstructor(classOf[String])
        constructor.setAccessible(true)
        val instance = constructor
          .newInstance(Identifiable.randomUID(clazz.getSimpleName))
          .asInstanceOf[Object]
        fromValue(instance, classDict)
        instance
      }
    } else {
      value
    }
  }
}
