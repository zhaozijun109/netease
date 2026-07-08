package com.netease.music.da.transfer.common.conf

import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap
import java.util.{Properties => JProperties}

import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.apache.spark.network.util.JavaUtils
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods._

import scala.collection.mutable.ArrayBuffer

class Properties extends Serializable {

  private val properties: ConcurrentHashMap[String, String] = new ConcurrentHashMap[String, String]()

  def contains(key: String): Boolean = {
    properties.containsKey(key)
  }

  def put(key: String, value: String): Properties = {
    properties.put(key, value)
    this
  }

  def putProperties(other: Properties): Properties = {
    properties.putAll(other.properties)
    this
  }

  def putMap(map: Map[String, String]): Properties = {
    import scala.collection.JavaConverters._
    properties.putAll(map.asJava)
    this
  }

  def putJson(string: String): Properties = {
    implicit val formats: DefaultFormats.type = DefaultFormats
    val map = parse(string).extract[Map[String, Any]].map { pair =>
      pair._1 -> pair._2.toString
    }
    putMap(map)
  }

  def putJson(inputStream: InputStream): Properties = {
    val content = IOUtils.toString(inputStream, StandardCharsets.UTF_8)
    putJson(content)
  }

  def clear(): Properties = {
    properties.clear()
    this
  }

  def getOption(key: String): Option[String] = Option(properties.get(key))

  private def getList(key: String, default: List[String],
                      separator: String = Properties.DEFAULT_LIST_SEPARATOR): List[String] = {
    val option = getOption(key)
    if (option.isDefined) {
      val value = option.get
      if (StringUtils.isBlank(value)) {
        default
      } else {
        option.get.split(separator).map(_.trim).toList
      }
    } else {
      default
    }
  }

  def getInt(key: String, default: Int): Int = getOption(key).map(_.toInt).getOrElse(default)

  def getLong(key: String, default: Long): Long = getOption(key).map(_.toLong).getOrElse(default)

  def getDouble(key: String, default: Double): Double = getOption(key).map(_.toDouble).getOrElse(default)

  def getBoolean(key: String, default: Boolean): Boolean = getOption(key).map(_.toBoolean).getOrElse(default)

  def getString(key: String, default: String): String = getOption(key).getOrElse(default)

  def getProperty[T](property: Property[T]): Option[T] =
    getOption(property.key).map(property.convertFunc(_)).orElse(property.default)

  def getSerial[T](property: Property[T]): List[T] = {
    var i = 0
    val ret = ArrayBuffer[T]()
    while (contains(Properties.reduceKey(property.key, i.toString))) {
      ret += property.convertFunc(getOption(Properties.reduceKey(property.key, i.toString)).get)
      i += 1
    }
    ret.toList
  }

  def getProperties[T](property: Property[T]): List[Properties] = {
    val ids = getList(property.key, List[String]())
    val all = toMap
    ids.map { id =>
      val keyPrefix = Properties.reduceKey(property.key, id) + "."
      val newProperties = new Properties()
      all.filter(_._1.startsWith(keyPrefix)).foreach { pair =>
        val key = pair._1.substring(keyPrefix.length)
        newProperties.put(key, pair._2)
      }
      newProperties.put(Properties.MAP_ID.key, id)
      newProperties
    }
  }

  def toMap: Map[String, String] = {
    import scala.collection.JavaConverters._
    properties.asScala.toMap
  }

  def toJProperties: JProperties = {
    val jProperties = new JProperties()
    jProperties.putAll(this.properties)
    jProperties
  }
}

object Properties {
  val DEFAULT_LIST_SEPARATOR = ","

  val toIntFunc: String => Int = { string: String => string.toInt }
  val toLongFunc: String => Long = { string: String => string.toLong }
  val toDoubleFunc: String => Double = { string: String => string.toDouble }
  val toBooleanFunc: String => Boolean = { string: String => string.toBoolean }
  val toStringFunc: String => String = { string: String => string }
  val toListFunc: String => List[String] = {
    string: String =>
      if (StringUtils.isBlank(string)) {
        List[String]()
      } else {
        string.split(DEFAULT_LIST_SEPARATOR).map(_.trim).toList
      }
  }
  val toBytesFunc: String => Long = JavaUtils.byteStringAsBytes

  def reduceKey(args: String*): String = {
    args.mkString(".")
  }

  def apply(): Properties = new Properties()

  val MAP_ID: Property[String] = Property[String]("mapId", Option.empty, toStringFunc)
}
