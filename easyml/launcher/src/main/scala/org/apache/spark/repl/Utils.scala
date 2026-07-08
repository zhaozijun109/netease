package org.apache.spark.repl

import java.io._
import java.net.URLClassLoader

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

import scala.collection.mutable.ArrayBuffer

/**
 * Created by linjiuning on 2020/9/7.
 */
object Utils {
  def runInterpreter(conf: SparkConf, input: String): String = {
    SparkSession.cleanupAnyExistingSession()
    val CONF_EXECUTOR_CLASSPATH = "spark.executor.extraClassPath"

    val in = new BufferedReader(new StringReader(input + "\n"))
    val out = new StringWriter()
    val cl = getClass.getClassLoader
    var paths = new ArrayBuffer[String]
    if (cl.isInstanceOf[URLClassLoader]) {
      val urlLoader = cl.asInstanceOf[URLClassLoader]
      for (url <- urlLoader.getURLs) {
        if (url.getProtocol == "file") {
          paths += url.getFile
        }
      }
    }
    val classpath = paths.map(new File(_).getAbsolutePath).mkString(File.pathSeparator)

    val oldExecutorClasspath = System.getProperty(CONF_EXECUTOR_CLASSPATH)
    System.setProperty(CONF_EXECUTOR_CLASSPATH, classpath)
    conf.getAll.foreach {
      case (k, v) => Main.conf.set(k, v)
    }
    Main.doMain(Array("-classpath", classpath, "-usejavacp"), new SparkILoop(in, new PrintWriter(out)))

    if (oldExecutorClasspath != null) {
      System.setProperty(CONF_EXECUTOR_CLASSPATH, oldExecutorClasspath)
    } else {
      System.clearProperty(CONF_EXECUTOR_CLASSPATH)
    }
    out.toString
  }
}
