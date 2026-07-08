package com.netease.easyudf.util

import com.netease.easyml.common.collection.Params
import com.netease.easyml.common.util.{IOUtil, SparkUtil}
import com.netease.easyml.ml.util.{Utils => EMUtils}
import org.apache.commons.lang3.StringUtils
import org.apache.spark.ml.{PipelineModel, Transformer}

import java.util
import java.util.Collections
import java.util.regex.Pattern
import scala.collection.JavaConverters._

object Utils {
  val CLASSNAME = "className"
  val PIPELINE = "pipeline"
  val NATIVE = "native"
  val NATIVE_PATTERN: Pattern = Pattern.compile(NATIVE + "_([0-9]+)")


  def safeSavePipelineModel(pipelineModel: PipelineModel, path: String): Unit = {
    IOUtil.mkParentDirs(path)
    val stages = pipelineModel.stages.filterNot(it => it.getClass.getSimpleName.startsWith("LightGBM"))
    if (stages.length == pipelineModel.stages.length) {
      pipelineModel.write.overwrite().save(path)
    } else {
      if (IOUtil.exists(path)) {
        IOUtil.delete(path)
      }
      IOUtil.mkdirs(path)
      if (stages.nonEmpty) {
        EMUtils.newPipeLineModel(stages).write.overwrite().save(IOUtil.join(path, PIPELINE))
      }
      val map = new Params()
      pipelineModel.stages.zipWithIndex.filter(it => it._1.getClass.getSimpleName.startsWith("LightGBM"))
        .foreach(it => {
          val clazz = it._1.getClass
          val saveNativeModel = clazz.getDeclaredMethod("saveNativeModel", classOf[String], classOf[Boolean])
          saveNativeModel.setAccessible(true)
          saveNativeModel.invoke(it._1, IOUtil.join(path, s"${NATIVE}_${it._2}").asInstanceOf[Object], true.asInstanceOf[Object])
          map.put(it._2.toString, clazz.getName)
        })
      IOUtil.writeLines(IOUtil.join(path, CLASSNAME), Collections.singleton(map.toJson()))
    }
  }

  def safeLoadPipelineModel(path: String): PipelineModel = {
    val files = IOUtil.listDirectory(path).asScala.map(it => IOUtil.baseName(it))
    val idx = files.filter(it => NATIVE_PATTERN.matcher(it).matches())
      .map(it => {
        val m = NATIVE_PATTERN.matcher(it)
        m.find()
        m.group(1).toInt
      }).sorted
    if (idx.isEmpty) {
      PipelineModel.load(path)
    } else {
      val stages = if (files.exists(it => it.equals(PIPELINE))) {
        PipelineModel.load(IOUtil.join(path, PIPELINE)).stages
      } else {
        Array[Transformer]()
      }
      val list = new util.ArrayList[Transformer]()
      stages.foreach(it => list.add(it))

      val params = Params.fromFile(IOUtil.join(path, CLASSNAME))
      idx.foreach(i => {
        val clazz = SparkUtil.classForName(params.get(i.toString).asInstanceOf[String])
        val loadNativeModelFromFile = clazz.getDeclaredMethod("loadNativeModelFromFile", classOf[String])
        val model = loadNativeModelFromFile.invoke(null, IOUtil.join(path, s"${NATIVE}_$i")).asInstanceOf[Transformer]
        if (i >= list.size()) {
          list.add(model)
        } else {
          list.add(i, model)
        }
      })

      EMUtils.newPipeLineModel(list.asScala.toArray)
    }
  }

  def register(): Unit = {
    val clazz = SparkUtil.classForName("com.netease.easyml.launcher.RegisterManager")
    clazz.getDeclaredMethod("register").invoke(null)
  }

  def isNotBlank(value: String): Boolean = {
    StringUtils.isNotBlank(value) && !SparkUtil.NULL.equalsIgnoreCase(value)
  }

  def isBlank(value: String): Boolean = {
    StringUtils.isBlank(value) || SparkUtil.NULL.equalsIgnoreCase(value)
  }
}
