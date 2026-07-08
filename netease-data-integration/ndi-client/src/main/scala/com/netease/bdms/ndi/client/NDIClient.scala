package com.netease.bdms.ndi.client

import java.io.File

import com.netease.bdms.ndi.client.instance.HttpInstanceProtocol
import com.netease.bdms.ndi.client.property.Properties.properties
import com.netease.bdms.ndi.client.property.Property._
import com.netease.bdms.ndi.client.spark.SparkExecutor
import com.netease.bdms.ndi.client.util.{LogTrait, Precondition}
import com.netease.bdms.ndi.client.util.Precondition._

class NDIClient(val args: Array[String]) extends LogTrait {
  var sparkExecutor: SparkExecutor = new SparkExecutor(new HttpInstanceProtocol())

  def checkArguments(): Unit = {
    checkPropertyExist(TASK)
    checkPropertyExist(SPARK_HOME)
    Precondition.check(System.getenv("HADOOP_CONF_DIR") != null,
      Option("HADOOP_CONF_DIR must be set in the environment"))
  }

  def init(): Unit = {
    import com.netease.bdms.ndi.client.util.PropertyUtils._
    properties.putAll(args)

    checkArguments()
    if (properties.getProperty(SPARK_CONF_DIR).isEmpty) {
      val sparkHome = properties.getProperty(SPARK_HOME).get
      if (sparkHome.endsWith(File.separator)) {
        properties.put(SPARK_CONF_DIR.key, sparkHome + "conf")
      } else {
        properties.put(SPARK_CONF_DIR.key, sparkHome + File.separator + "conf")
      }
    }
    sparkExecutor.init()
  }

  def start(): Unit = {
    sparkExecutor.start()
    val result = sparkExecutor.waitFor
    if (!result) {
      System.exit(-1)
    }
  }
}

object NDIClient {
  def main(args: Array[String]): Unit = {
    val client = new NDIClient(args)
    client.init()
    client.start()
  }
}
