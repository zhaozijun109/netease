package com.netease.bdms.ndi.client.property

import com.netease.bdms.ndi.client.property.Properties._

case class Property[T](key: String, defaultVale: Option[T], func: String => T)

object Property {
  val toIntFunc: String => Int = { string: String => string.toInt }
  val toLongFunc: String => Long = { string: String => string.toLong }
  val toDoubleFunc: String => Double = { string: String => string.toDouble }
  val toBooleanFunc: String => Boolean = { string: String => string.toBoolean }
  val toStringFunc: String => String = { string: String => string }
  val toListFunc: String => List[String] = {
    string: String => string.split(DEFAULT_LIST_SEPARATOR).map(_.trim).toList
  }

  val SERVICE = Property("ndi.service", Option.empty, toStringFunc)
  val APP_ID = Property("ndi.app-id", Option.empty, toStringFunc)
  val APP_SECRET = Property("ndi.app-secret", Option.empty, toStringFunc)
  val KEYTAB = Property("ndi.keytab", Option.empty, toStringFunc)
  val PRINCIPAL = Property("ndi.principal", Option.empty, toStringFunc)
  val TASK = Property("ndi.task", Option.empty, toStringFunc)
  val IS_DEVELOP = Property("ndi.is-develop", Option(false), toBooleanFunc)
  val WORKER_HOME = Property("ndi.worker-home", Option.empty, toStringFunc)

  val SPARK_LOG_SIZE = Property("ndi.spark-log-size", Option.apply(200), toIntFunc)

  val SPARK_HOME = Property("ndi.spark.spark-home", Option.apply(System.getenv("SPARK_HOME")), toStringFunc)
  val SPARK_CONF_DIR = Property("ndi.spark.spark-conf-dir", Option(System.getenv("SPARK_CONF_DIR")), toStringFunc)
  val SPARK_APP_JAR = Property("ndi.spark.app-jar", Option.empty, toStringFunc)

  val HADOOP_CONF_DIR = Property("ndi.hadoop-conf-dir",
    Option.apply(System.getenv("HADOOP_CONF_DIR")),
    toStringFunc
  )

  val SPARK_CONF_PREFIX = "ndi.spark.spark-conf."
  val SPARK_ARGUMENT_PREFIX = "ndi.spark.spark-argument."
  val SPARK_ENV_PREFIX = "ndi.spark.spark-env."
}
