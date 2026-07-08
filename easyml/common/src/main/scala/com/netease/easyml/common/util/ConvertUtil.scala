package com.netease.easyml.common.util

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.{DefaultScalaModule, ScalaObjectMapper}
import org.json4s._
import org.json4s.jackson.Serialization.{read, write}

import scala.collection.JavaConverters._
import scala.collection.MapLike
import scala.reflect.runtime.universe.typeOf

/**
 * Created by linjiuning on 2020/11/25.
 */
object ConvertUtil {
  def toInt(value: Any): Int = {
    value match {
      case int: Int => int
      case numeric: java.lang.Number => numeric.intValue()
      case string: String => string.toInt
      case obj: Any => obj.toString.toInt
    }
  }

  def toLong(value: Any): Long = {
    value match {
      case int: Long => int
      case numeric: java.lang.Number => numeric.longValue()
      case string: String => string.toLong
      case obj: Any => obj.toString.toLong
    }
  }

  def toFloat(value: Any): Float = {
    value match {
      case float: Float => float
      case numeric: java.lang.Number => numeric.floatValue()
      case string: String => string.toFloat
      case obj: Any => obj.toString.toFloat
    }
  }

  def toDouble(value: Any): Double = {
    value match {
      case double: Double => double
      case numeric: java.lang.Number => numeric.doubleValue()
      case string: String => string.toDouble
      case obj: Any => obj.toString.toDouble
    }
  }

  def toString(value: Any): String = {
    value match {
      case string: String => string
      case obj: Any => obj.toString
    }
  }

  def mayNestToArray(value: Any): Any = {
    value match {
      case map: MapLike[_, _, _] =>
        map.mapValues(mayNestToArray).toMap
      case array: Array[_] =>
        array.map(mayNestToArray)
      case iter: Iterable[_] =>
        iter.map(mayNestToArray).toArray
      case _ =>
        value
    }
  }

  private val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)
  mapper.setSerializationInclusion(Include.NON_NULL)

  def fromJson[T](json: String)(implicit m: Manifest[T]): T = {
    mapper.readValue[T](json)
  }

  def toJson(value: Any): String = {
    mapper.writeValueAsString(value)
  }

  implicit val formats: DefaultFormats.type = DefaultFormats

  def fromJson4s[T](json: String)(implicit m: Manifest[T]): T = {
    read[T](json)
  }

  def toJson4s[T <: AnyRef](value: T): String = {
    write(value)
  }

  def fromMap[T: Manifest](params: Map[String, String]): T = {
    // tricky: json4s don't support automatic type conversion
    val nParams = new java.util.HashMap[String, Any]()
    val symbols = typeOf[T].members.filterNot(_.isMethod)
    symbols.foreach(field => {
      val name = field.name.toString.trim
      if (params.contains(name)) {
        val value = field.typeSignature.toString match {
          case "Int" => params(name).toInt
          case "Long" => params(name).toLong
          case "Float" => params(name).toFloat
          case "Double" => params(name).toDouble
          case "Boolean" => params(name).toBoolean
          case _ => params(name)
        }
        nParams.put(name, value)
      }
    })

    if (symbols.count(field => field.name.toString.trim.equals("kwargs")) > 0) {
      val kwargs = new java.util.HashMap[String, Any]()
      params.filterNot(kv => nParams.containsKey(kv._1)).foreach(kv => kwargs.put(kv._1, kv._2))
      nParams.put("kwargs", kwargs.asScala)
    }

    fromJson4s[T](ConvertUtil.toJson(nParams))
  }
}
