package com.netease.easyml.launcher

import java.util.Locale

import com.netease.easyml.common.util.{IOUtil, ReflectionUtil}
import org.apache.spark.SparkConf
import org.apache.spark.internal.Logging

/**
 * Created by linjiuning on 2020/9/8.
 */
object EasyMLUtils extends Logging {
  def initBigDL(conf: SparkConf): Unit = this.synchronized {
    // https://github.com/intel-analytics/BigDL-Tutorials/issues/61
    conf.set("spark.serializer", "org.apache.spark.serializer.JavaSerializer")
    conf.set("spark.dynamicAllocation.enabled", "false")

    def localMode: Boolean = {
      System.getProperty("bigdl.localMode", "false").toLowerCase(Locale.ROOT) match {
        case "true" => true
        case "false" => false
        case option => throw new IllegalArgumentException(s"Unknown bigdl.localMode $option")
      }
    }

    try {
      val obj = ReflectionUtil.getScalaObject("com.intel.analytics.bigdl.utils.Engine")
      val clazz = obj.getClass
      val method = clazz.getDeclaredMethod("createSparkConf", classOf[SparkConf])
      method.setAccessible(true)
      method.invoke(obj, conf)

      val nodeAndCore = clazz.getDeclaredMethod("setNodeAndCore", classOf[Int], classOf[Int])
      nodeAndCore.setAccessible(true)

      def setNodeAndCore(nodeNum: Int, coreNum: Int): Unit = {
        nodeAndCore.invoke(obj, nodeNum.asInstanceOf[Object], coreNum.asInstanceOf[Object])
      }

      if (localMode) {
        log.info("Detect bigdl.localMode is set. Run workload without spark")
        val coreNumberFromProperty = clazz.getDeclaredMethod("getCoreNumberFromProperty")
        coreNumberFromProperty.setAccessible(true)
        val coreNum = coreNumberFromProperty.invoke(obj).asInstanceOf[Int]
        // The physical core number should have been initialized
        // by java property -Dbigdl.coreNumber=xx
        setNodeAndCore(1, coreNum)
      } else {
        log.info("Auto detect executor number and executor cores number")
        val parseExecutorAndCore = clazz.getDeclaredMethod("parseExecutorAndCore", classOf[SparkConf])
        parseExecutorAndCore.setAccessible(true)
        val sparkExecutorAndCore = parseExecutorAndCore.invoke(obj, conf).asInstanceOf[Option[(Int, Int)]]
        val (nExecutor, executorCores) = sparkExecutorAndCore.get
        //        val nExecutor = SparkUtil.getNumExecutors(conf)
        //        val executorCores = SparkUtil.getNumCoresPerExecutor(conf)
        log.info(s"Executor number is $nExecutor and executor cores number is $executorCores")
        setNodeAndCore(nExecutor, executorCores)
      }
    } catch {
      case e: Exception =>
        log.error("Exception: " + e.getMessage)
    }
  }

  def initAngel(conf: SparkConf): Unit = {
    val version = conf.get(Constant.EASYML_ANGEL_VERSION)
    val home = conf.get(Constant.EASYML_ANGEL_HOME)
    val jars = conf.get(Constant.EASYML_ANGEL_JARS)

    val nJars = jars.split(",").filter(_.nonEmpty)
      .map(name => {
        IOUtil.join(home, name).replaceAll("\\$\\{version}", version)
      }).mkString(",")
    conf.set("spark.ps.jars", nJars)

    if (conf.contains("spark.yarn.keytab")) {
      conf.set("angel.kerberos.keytab", conf.get("spark.yarn.keytab"))
    }
    if (conf.contains("spark.yarn.principal")) {
      conf.set("angel.kerberos.principal", conf.get("spark.yarn.principal"))
    }
  }
}
