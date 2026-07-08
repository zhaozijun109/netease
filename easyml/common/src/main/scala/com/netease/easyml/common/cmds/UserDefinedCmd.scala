package com.netease.easyml.common.cmds

import com.alibaba.fastjson.JSON
import com.netease.easyml.common.collection.Params
import com.netease.easyml.common.util.{Cmds, ConvertUtil, IOUtil, SparkUtil}
import org.apache.commons.lang3.StringUtils
import org.apache.spark.internal.Logging
import org.apache.spark.ml.util.MLWritable
import org.apache.spark.ml.{Estimator, PipelineStage, Transformer}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.storage.StorageLevel

import java.util
import java.util.Collections
import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer
import scala.util.Try
import scala.util.control.Breaks

abstract class UserDefinedCmd[T: Manifest] extends Serializable with Logging {
  type Args = T

  def parseArgs(params: util.Map[String, String]): T = {
    ConvertUtil.fromMap[T](params.asScala.toMap)
  }

  def fromParams[E](json: String): E = {
    val params = Params.fromJson(json)
    var clazz = SparkUtil.classForName("com.netease.easyml.launcher.RegisterManager")
    var method = clazz.getDeclaredMethod("register")
    method.invoke(null)

    clazz = SparkUtil.classForName("com.netease.easyml.launcher.FromParams")
    method = clazz.getDeclaredMethod("fromParams", classOf[Class[_]], classOf[Params])
    method.setAccessible(true)
    method.invoke(null, classOf[PipelineStage], params).asInstanceOf[E]
  }

  def apply(spark: SparkSession, params: util.Map[String, String]): DataFrame = {
    val args = parseArgs(params)
    println(s"Args: ${ConvertUtil.toJson(args)}")
    apply(spark, args = args)
  }

  def apply(spark: SparkSession, args: Args): DataFrame = {
    null
  }
}

abstract class VoidUserDefinedCmd[T: Manifest] extends UserDefinedCmd[T] {

  def run(spark: SparkSession, args: Args): Unit

  override def apply(spark: SparkSession, args: Args): DataFrame = {
    run(spark, args)
    null
  }
}

abstract class Script[T: Manifest] extends UserDefinedCmd[T] {
  def run(params: util.Map[String, String]): Unit = {
    run(parseArgs(params))
  }

  def run(args: Args): Unit = {}

  override def apply(spark: SparkSession, params: util.Map[String, String]): DataFrame = {
    run(params)
    null
  }
}


case class SqlArgs(sqlText: String)

class Sql extends UserDefinedCmd[SqlArgs] {

  override def apply(spark: SparkSession, args: SqlArgs): DataFrame = {
    SparkUtil.sqlText(spark, args.sqlText)
  }
}

case class LoadArgs(path: String, source: String = "parquet", kwargs: Map[String, String] = null)

class Load extends UserDefinedCmd[LoadArgs] {
  override def apply(spark: SparkSession, args: LoadArgs): DataFrame = {
    var reader = spark.read
    if (StringUtils.isNoneBlank(args.source)) {
      reader = reader.format(args.source)
    }
    reader = reader.options(args.kwargs)
    reader.load(args.path.split("[@,;]"): _*)
  }
}

case class SaveArgs(input: String, path: String, source: String = "parquet", saveMode: String = "overwrite",
                    partitionBy: String = null, gzip: Boolean = false, kwargs: Map[String, String] = null)

class Save extends UserDefinedCmd[SaveArgs] {
  override def apply(spark: SparkSession, args: SaveArgs): DataFrame = {
    val df = spark.table(args.input)
    var writer = df.write
    if (StringUtils.isNoneBlank(args.source)) {
      writer = writer.format(args.source)
    }
    if (StringUtils.isNoneBlank(args.saveMode)) {
      writer = writer.mode(args.saveMode)
    }
    if (StringUtils.isNoneBlank(args.partitionBy)) {
      writer = writer.partitionBy(args.partitionBy.split(","): _*)
    }
    if (args.gzip) {
      writer = writer.option("codec", SparkUtil.COMPRESS_MODE)
    }
    writer = writer.options(args.kwargs)
    IOUtil.mkParentDirs(args.path)
    writer.save(args.path)
    df
  }
}

case class ShowArgs(input: String, numRows: Int = 20, truncate: Boolean = true)

class Show extends UserDefinedCmd[ShowArgs] {
  override def apply(spark: SparkSession, args: ShowArgs): DataFrame = {
    val df = spark.table(args.input)
    df.show(args.numRows, args.truncate)
    df
  }
}

case class InputArgs(input: String)

class Count extends UserDefinedCmd[InputArgs] {
  override def apply(spark: SparkSession, args: InputArgs): DataFrame = {
    val df = spark.table(args.input)
    val count = df.count()
    println(s"COUNT: $count")
    df
  }
}

class Cache extends UserDefinedCmd[InputArgs] {
  override def apply(spark: SparkSession, args: InputArgs): DataFrame = {
    val df = spark.table(args.input)
    df.cache()
  }
}

case class PersistArgs(input: String, newLevel: String = "")

class Persist extends UserDefinedCmd[PersistArgs] {
  override def apply(spark: SparkSession, args: PersistArgs): DataFrame = {
    val df = spark.table(args.input)
    if (args.newLevel.isEmpty) {
      df.persist()
    } else {
      df.persist(StorageLevel.fromString(args.newLevel))
    }
  }
}

case class UnpersistArgs(input: String, blocking: Boolean = false)

class Unpersist extends UserDefinedCmd[UnpersistArgs] {
  override def apply(spark: SparkSession, args: UnpersistArgs): DataFrame = {
    val df = spark.table(args.input)
    df.unpersist(args.blocking)
  }
}

case class RepartitionArgs(input: String, numPartitions: Int)

class Repartition extends UserDefinedCmd[RepartitionArgs] {
  override def apply(spark: SparkSession, args: RepartitionArgs): DataFrame = {
    val df = spark.table(args.input)
    df.repartition(args.numPartitions)
  }
}

class Coalesce extends UserDefinedCmd[RepartitionArgs] {
  override def apply(spark: SparkSession, args: RepartitionArgs): DataFrame = {
    val df = spark.table(args.input)
    df.coalesce(args.numPartitions)
  }
}

case class LoadWordVecArgs(path: String, vectorSize: Int = 0)

class LoadWordVec extends UserDefinedCmd[LoadWordVecArgs] {
  override def apply(spark: SparkSession, args: Args): DataFrame = {
    val rdd = spark.sparkContext.textFile(args.path)
      .map(line => line.split(" "))
      .filter(_.length > 1)

    val size = if (args.vectorSize <= 0) {
      rdd.take(100).map(_.length - 1).map((_, 1))
        .groupBy(_._1)
        .map(it => (it._1, it._2.map(_._2).sum))
        .maxBy(_._2)
        ._1
    } else {
      args.vectorSize
    }

    println(s"Vector size = $size")

    val w2v = rdd.filter(_.length > size)
      .map(it => {
        val word = it.slice(0, it.length - size).mkString(" ")
        val vector = it.slice(it.length - size, it.length).map(_.toFloat)
        (word, vector)
      })

    import spark.implicits._

    w2v.toDF("word", "vector")
  }
}

case class LoadAnnJsonArgs(path: String, k: Int = 100, threshold: Float = 0, kv: Boolean = true)

class LoadAnnJson extends UserDefinedCmd[LoadAnnJsonArgs] {
  override def apply(spark: SparkSession, args: LoadAnnJsonArgs): DataFrame = {
    val rdd = spark.sparkContext.textFile(args.path)
      .filter(it => it.nonEmpty)
      .map(it => {
        val obj = JSON.parseObject(it)
        val src = obj.getString("src")
        val topn = obj.getJSONArray("topn")
        val labels = ArrayBuffer.empty[String]
        val distances = ArrayBuffer.empty[Float]
        val loop = new Breaks
        loop.breakable {
          for (i <- 0 until Math.min(args.k, topn.size)) {
            val obj = topn.getJSONObject(i)
            val score = obj.getFloat("score")
            if (score < args.threshold) {
              loop.break
            }
            labels.append(obj.getString("dst"))
            distances.append(score)
          }
        }
        (src, labels.toArray, distances.toArray)
      }).filter(it => it._1.nonEmpty && it._2.nonEmpty)

    import spark.implicits._

    if (args.kv) {
      rdd.map(it => (it._1, it._2.zip(it._3).map(it => it._1 + ":" + it._2)))
        .toDF("src", "labels")
    } else {
      rdd.toDF("src", "labels", "scores")
    }
  }
}

object FromParams {
  var MODELS: Map[String, Transformer] = Map()

  def stored(name: String, model: Transformer): Unit = {
    assert(!MODELS.contains(name))
    MODELS = MODELS + (name -> model)
  }

  def get(name: String): Transformer = {
    MODELS(name)
  }
}

case class FitArgs(input: String, path: String, overwrite: Boolean = false,
                   params: String = "{}", stored: String = null)

class Fit extends UserDefinedCmd[FitArgs] {
  def fit(spark: SparkSession, args: FitArgs): Transformer = {
    val estimator = fromParams[Estimator[_]](args.params)

    val df = spark.table(args.input)
    val model = estimator.fit(df).asInstanceOf[Transformer]
    if (StringUtils.isNoneBlank(args.path)) {
      if (classOf[MLWritable].isAssignableFrom(model.getClass)) {
        IOUtil.mkParentDirs(args.path)
        val writer = model.asInstanceOf[MLWritable].write
        if (args.overwrite) {
          writer.overwrite()
        }
        writer.save(args.path)
      }
    }
    if (StringUtils.isNoneBlank(args.stored)) {
      FromParams.stored(args.stored, model)
    }
    model
  }

  override def apply(spark: SparkSession, args: FitArgs): DataFrame = {
    fit(spark, args)
    null
  }
}

case class TransformArgs(input: String, ref: String = null, params: String = "{}")

class Transform extends UserDefinedCmd[TransformArgs] {
  override def apply(spark: SparkSession, args: TransformArgs): DataFrame = {
    val transform = if (StringUtils.isNoneBlank(args.ref)) {
      FromParams.get(args.ref)
    } else {
      fromParams[Transformer](args.params)
    }

    val df = spark.table(args.input)
    transform.transform(df)
  }
}

class FitTransform extends Fit {
  override def apply(spark: SparkSession, args: FitArgs): DataFrame = {
    val df = spark.table(args.input)
    val model = fit(spark, args)
    model.transform(df)
  }
}

case class UdsArgs(`type`: String, kwargs: Map[String, String] = null)

class Uds extends UserDefinedCmd[UdsArgs] {
  override def apply(spark: SparkSession, args: UdsArgs): DataFrame = {
    val clazz = SparkUtil.classForName(args.`type`)
    val uds = clazz.getDeclaredMethod("run", classOf[SparkSession], classOf[Array[String]])
    val params = args.kwargs.map(it => it._1 + ":" + it._2).toArray
    uds.invoke(null, spark, params)
    null
  }
}

case class PathArgs(path: String)

class Rm extends Script[PathArgs] {
  override def run(args: PathArgs): Unit = {
    IOUtil.delete(args.path)
  }
}

class Mkdir extends Script[PathArgs] {
  override def run(args: PathArgs): Unit = {
    IOUtil.mkdirs(args.path)
  }
}

class Touch extends Script[PathArgs] {
  override def run(args: PathArgs): Unit = {
    val path = args.path
    IOUtil.mkParentDirs(path)
    IOUtil.writeLines(path, Collections.singleton(""))
  }
}

case class EchoArgs(path: String, message: String)

class Echo extends Script[EchoArgs] {
  override def run(args: EchoArgs): Unit = {
    IOUtil.mkParentDirs(args.path)
    IOUtil.writeLines(args.path, Collections.singleton(args.message))
  }
}

case class CpArgs(src: String, dst: String)

class Cp extends Script[CpArgs] {
  override def run(args: CpArgs): Unit = {
    IOUtil.mkParentDirs(args.dst)
    IOUtil.copy(args.src, args.dst)
  }
}

class Mv extends Script[CpArgs] {
  override def run(args: CpArgs): Unit = {
    IOUtil.rename(args.src, args.dst)
  }
}

case class PrintArgs(message: String)

class Print extends Script[PrintArgs] {
  override def run(args: PrintArgs): Unit = {
    println(args.message)
  }
}


abstract class Control[T: Manifest] extends UserDefinedCmd[T] {

  def condition(spark: SparkSession, pred: String): Boolean = {
    Try(pred.toBoolean).getOrElse(Cmds.executeExpr(spark, pred).toBoolean)
  }
}


case class IfArgs(pred: String, true_fn: String = null, false_fn: String = null)

class If extends Control[IfArgs] {
  override def apply(spark: SparkSession, args: IfArgs): DataFrame = {
    val pred = condition(spark, args.pred)
    val fn = if (pred) {
      args.true_fn
    } else {
      args.false_fn
    }
    if (StringUtils.isNoneBlank(fn)) {
      Cmds.run(spark, fn)
    } else {
      null
    }
  }
}

case class ExitArgs(pred: String)

class Exit extends Control[ExitArgs] {
  override def apply(spark: SparkSession, args: ExitArgs): DataFrame = {
    val pred = condition(spark, args.pred)
    if (pred) {
      Cmds.setStop(true)
    }
    null
  }
}

case class UntilArgs(pred: String, secs: Int, fn: String)

class Until extends Control[UntilArgs] {
  override def apply(spark: SparkSession, args: UntilArgs): DataFrame = {
    val millis = args.secs * 1000
    var cond = condition(spark, args.pred)
    while (!cond) {
      logInfo(s"sleep ${args.secs}s.")
      Thread.sleep(millis)
      cond = condition(spark, args.pred)
    }
    Cmds.run(spark, args.fn)
  }
}


case class SleepArgs(secs: Int)

class Sleep extends Control[SleepArgs] {
  override def apply(spark: SparkSession, args: SleepArgs): DataFrame = {
    val millis = args.secs * 1000
    logInfo(s"sleep ${args.secs}s.")
    Thread.sleep(millis)
    null
  }
}

case class ForArgs(`var`: String, values: String, fn: String, sep: String = ",")

class For extends Control[ForArgs] {
  override def apply(spark: SparkSession, args: ForArgs): DataFrame = {
    var df: DataFrame = null
    args.values.split(args.sep).foreach(value => {
      val vars = Map(args.`var` -> value)
      df = Cmds.run(spark, Cmds.render(args.fn, vars))
    })
    df
  }
}
