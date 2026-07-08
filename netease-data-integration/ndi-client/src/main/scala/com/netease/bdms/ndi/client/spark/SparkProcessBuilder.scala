package com.netease.bdms.ndi.client.spark

import com.netease.bdms.ndi.client.property.Properties.properties
import com.netease.bdms.ndi.client.property.Property._
import com.netease.bdms.ndi.client.util.LogTrait

import scala.collection.mutable.ArrayBuffer

private[spark] class SparkProcessBuilder(val sparkProperties: SparkProperties) extends LogTrait {

  private def showArguments(arguments: ArrayBuffer[String]): String = {
    val stringBuilder = new StringBuilder
    stringBuilder ++= arguments(0) + " \\\n"
    for (i <- 1 until arguments.size - 1 by 2) {
      if (!arguments(i + 1).contains("password")) {
        stringBuilder ++= "\t" + arguments(i) + " " + arguments(i + 1) + " \\\n"
      }
    }
    stringBuilder ++= "\t" + arguments(arguments.size - 1)
    stringBuilder.toString()
  }

  def build(): SparkApp = {
    var arguments = ArrayBuffer(sparkProperties.sparkSubmit)
    sparkProperties.sparkArguments.getMap.toSeq.sortBy(_._1).foreach { pair =>
      arguments += "--" + pair._1
      arguments += pair._2
    }
    sparkProperties.sparkConf.getMap.toSeq.sortBy(_._1).foreach { pair =>
      arguments += "--conf"
      arguments += pair._1 + "=" + pair._2
    }

    arguments += sparkProperties.appJar

    LOG.info(s"Command:\n${showArguments(arguments)}")

    import scala.collection.JavaConverters._
    val pb = new ProcessBuilder(arguments.asJava)
    val env = pb.environment()

    sparkProperties.sparkEnv.getMap.foreach { pair =>
      env.put(pair._1, pair._2)
    }

    new SparkApp(new LineBufferedProcessBuilder(pb, properties.getProperty(SPARK_LOG_SIZE).get))
  }
}
