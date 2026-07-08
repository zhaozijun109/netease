package com.netease.bdms.ndi.client.property

import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern
import java.util.{Properties => JProperties}

import com.google.common.io.Resources
import com.netease.bdms.ndi.client.property.Properties.pattern
import com.netease.bdms.ndi.client.util.{LogTrait, Precondition}

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer
import scala.language.implicitConversions

class Properties {

  private val properties: ConcurrentHashMap[String, String] = new ConcurrentHashMap[String, String]()

  def contains(key: String): Boolean = {
    properties.containsKey(key)
  }

  def put(key: String, value: String): Properties = {
    properties.put(key, value)
    this
  }

  def putAll(other: Map[String, String]): Properties = {
    this.properties.putAll(other.asJava)
    this
  }

  def clear(): Properties = {
    properties.clear()
    this
  }

  def getInt(key: String, default: Int): Int = getOption(key).map(_.toInt).getOrElse(default)

  def getLong(key: String, default: Long): Long = getOption(key).map(_.toLong).getOrElse(default)

  def getDouble(key: String, default: Double): Double = getOption(key).map(_.toDouble).getOrElse(default)

  def getBoolean(key: String, default: Boolean): Boolean = getOption(key).map(_.toBoolean).getOrElse(default)

  def getString(key: String, default: String): String = getOption(key).getOrElse(default)

  def getOption(key: String): Option[String] = Option(properties.get(key))

  def getList(key: String, default: List[String],
              separator: String = Properties.DEFAULT_LIST_SEPARATOR): List[String] = {
    val option = getOption(key)
    if (option.isDefined) {
      option.get.split(separator).map(_.trim).toList
    } else {
      default
    }
  }

  def getProperty[T](property: Property[T]): Option[T] =
    getOption(property.key).map(property.func).orElse(property.defaultVale)

  def getMap: Map[String, String] = this.properties.asScala.toMap

  def getMapByPrefix(prefix: String): Map[String, String] = {
    this.properties.asScala.filter(_._1.startsWith(prefix)).map { pair =>
      pair._1.substring(prefix.length) -> pair._2
    }.toMap
  }

  def remove(key: String): Option[String] = Option.apply(this.properties.remove(key))


  def resolve(): Properties = {
    def resolveValue(value: String, visited: ArrayBuffer[String]): String = {
      val ret = new StringBuilder()
      if (visited.contains(value)) {
        throw new IllegalArgumentException(
          s"Circular variable substitution found: ${visited.mkString(" -> ")} -> $value")
      }
      visited += value
      val matcher = pattern.matcher(value)
      var index = 0
      while (matcher.find(index)) {
        if (index < matcher.start()) {
          ret ++= value.substring(index, matcher.start())
        }
        val subVariable = matcher.group(1)
        val replacement = getOption(subVariable)
        if (replacement.isEmpty) {
          throw new IllegalArgumentException(
            s"Could not find variable substitution for variable(s) ${visited.mkString(" -> ")}")
        }
        ret ++= resolveValue(replacement.get, visited)
        index = matcher.end()
      }
      visited -= value
      if (index < value.length) {
        ret ++= value.substring(index)
      }
      ret.toString()
    }

    val ret = new Properties()
    this.properties.asScala.foreach { pair =>
      val key = pair._1
      val visited = new ArrayBuffer[String]()
      val value = resolveValue(pair._2, visited)
      ret.put(key, value)
    }
    ret
  }
}

object Properties extends LogTrait {
  val pattern: Pattern = "\\$\\{([a-zA-Z_.0-9\\-]+)\\}".r.pattern

  val DEFAULT_PROPERTIES_FILE = "ndi.properties"
  val DEFAULT_LIST_SEPARATOR = ","
  val PREFIX = "ndi."

  val properties: Properties = {
    val properties = new Properties()
    if (!(this.getClass.getClassLoader.getResource(DEFAULT_PROPERTIES_FILE) == null)) {
      import com.netease.bdms.ndi.client.util.PropertyUtils._
      // load properties from DEFAULT_PROPERTIES_FILE
      val jProperties = new JProperties()
      jProperties.load(Resources.getResource(DEFAULT_PROPERTIES_FILE).openStream())
      properties.putAll(jProperties)

      // load properties from env
      properties.putAll(System.getenv().asScala.filter(_._1.startsWith(PREFIX)).toMap)

      //load properties from system properties
      properties.putAll(System.getProperties.asScala.toMap)
    }
    properties
  }
}
