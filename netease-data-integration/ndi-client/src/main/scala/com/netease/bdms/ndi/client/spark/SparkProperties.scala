package com.netease.bdms.ndi.client.spark

import java.io.File

import com.netease.bdms.ndi.client.property.Properties
import com.netease.bdms.ndi.client.property.Property._
import com.netease.bdms.ndi.client.util.LogTrait

class SparkProperties extends LogTrait {

  private[this] val resolvedProperties = Properties.properties.resolve()

  val sparkConf: Properties =
    new Properties()
      .putAll(resolvedProperties.getMapByPrefix(SPARK_CONF_PREFIX))
      .put("spark.yarn.maxAppAttempts", "1")
      .put("spark.yarn.submit.waitAppCompletion", "false")

  val sparkArguments: Properties = {
    val ret = new Properties()
      .putAll(resolvedProperties.getMapByPrefix(SPARK_ARGUMENT_PREFIX))
      .put("master", master)
      .put("deploy-mode", deployMode)
    if (resolvedProperties.contains(KEYTAB.key) && resolvedProperties.contains(PRINCIPAL.key)) {
      ret
        .put("keytab", resolvedProperties.getProperty(KEYTAB).get)
        .put("principal", resolvedProperties.getProperty(PRINCIPAL).get)
    }
    ret
  }

  val sparkEnv: Properties = new Properties()
    .putAll(resolvedProperties.getMapByPrefix(SPARK_ENV_PREFIX))
    .put("SPARK_HOME", sparkHome)
    .put("SPARK_CONF_DIR", sparkConfDir)
    .put("HADOOP_CONF_DIR", hadoopConfDir)

  def removeAndLogConflict(value: String, confKey: String, argumentKey: Option[String] = Option.empty): Unit = {
    sparkConf.remove(confKey).foreach { origin =>
      if (value == origin) {
        LOG.info(s"Remove $confKey from spark conf. OriginValue: $origin, CurrentValue: $value")
      }
    }
    argumentKey.foreach(sparkArguments.remove(_).foreach { origin =>
      if (value == origin) {
        LOG.info(s"Remove $confKey from spark arguments. OriginValue: $origin, CurrentValue: $value")
      }
    })
  }

  def sparkHome: String = resolvedProperties.getProperty(SPARK_HOME).get

  def sparkConfDir: String = resolvedProperties.getProperty(SPARK_CONF_DIR).get

  def sparkSubmit: String = sparkHome + File.separator + "bin" + File.separator + "spark-submit"

  def hadoopConfDir: String = resolvedProperties.getProperty(HADOOP_CONF_DIR).get

  def appJar: String = resolvedProperties.getProperty(SPARK_APP_JAR).get

  def master: String = "yarn"

  def deployMode: String = "cluster"

}
