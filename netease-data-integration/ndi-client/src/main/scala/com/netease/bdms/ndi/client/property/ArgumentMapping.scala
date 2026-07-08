package com.netease.bdms.ndi.client.property

import com.netease.bdms.ndi.client.util.Precondition._

case class ArgumentMapping(argumentName: String, propertyName: String, isMap: Boolean = false) {
  def toPair(value: String): (String, String) = {
    if (isMap) {
      val splits = value.split("=", 2)
      check(splits.length == 2, Option.apply(s"Illegal argument $argumentName."))
      s"$propertyName.${splits(0)}" -> splits(1)
    } else {
      propertyName -> value
    }
  }
}

object ArgumentMapping {
  val SERVICE = ArgumentMapping("--service", Property.SERVICE.key)
  val KEYTAB = ArgumentMapping("--keytab", Property.KEYTAB.key)
  val PRINCIPAL = ArgumentMapping("--principal", Property.PRINCIPAL.key)
  val TASK = ArgumentMapping("--task", Property.TASK.key)
  val IS_DEVELOP = ArgumentMapping("--develop", Property.IS_DEVELOP.key)

  // spark options
  val SPARK_HOME = ArgumentMapping("--spark-home", Property.SPARK_HOME.key)
  val SPARK_CONF_DIR = ArgumentMapping("--spark-conf-dir", Property.SPARK_CONF_DIR.key)
  val SPARK_CONF = ArgumentMapping("--spark-conf", "ndi.spark.spark-conf", isMap = true)
  val SPARK_ARGUMENT = ArgumentMapping("--spark-argument", "ndi.spark.spark-argument", isMap = true)
  val SPARK_APP_JAR = ArgumentMapping("--spark-app-jar", Property.SPARK_APP_JAR.key)
  val HADOOP_CONF_DIR = ArgumentMapping("--hadoop-conf-dir", Property.HADOOP_CONF_DIR.key)

  val options: List[ArgumentMapping] = List(
    SERVICE,
    KEYTAB,
    PRINCIPAL,
    TASK,
    SPARK_HOME,
    SPARK_CONF_DIR,
    SPARK_CONF,
    SPARK_ARGUMENT,
    SPARK_APP_JAR,
    HADOOP_CONF_DIR,
    IS_DEVELOP
  )

  val switches: List[ArgumentMapping] = List(
  )
}

