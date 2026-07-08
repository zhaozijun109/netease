package com.netease.music.da.transfer.common


import com.netease.music.da.transfer.common.conf.Properties
import com.netease.music.da.transfer.common.log.LogTrait
import org.apache.spark.SparkConf

trait Pluggable extends LogTrait with Serializable {
  def sparkConf: SparkConf

  lazy val properties: Properties = {
    val props: Properties = Properties()
    addDefaultProperties(props)
    sparkConf.getAllWithPrefix(confPrefix).foreach { pair =>
      props.put(pair._1.substring(1), pair._2)
    }
    props
  }

  def confPrefix: String

  def addDefaultProperties(props: Properties): Unit = {}

  def option(key: String, value: String): Unit = {
    this.properties.put(key, value)
  }

  def options(options: Map[String, String]): Unit = {
    this.properties.putMap(options)
  }
}
