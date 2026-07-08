package com.netease.easyml.ml.trainer

import com.netease.easyml.annotation.Register
import com.netease.easyml.common.collection
import com.netease.easyml.common.util.IOUtil
import com.netease.easyml.ml.sklearn.model_selection.BaseCrossValidator
import com.netease.easyml.ml.util.MLUtils
import org.apache.spark.ml.Transformer
import org.apache.spark.ml.param.{Param, ParamMap, Params}
import org.apache.spark.ml.util.{Identifiable, MLWritable}
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.lit

import scala.collection.JavaConversions._

/**
 * Created by linjiuning on 2020/7/7.
 */
@Register(alias = Array("basic"))
class BasicTrainer(override val uid: String) extends Trainer {
  val CROSS_VAL_COL_NAME: String = "cross_validation"

  def this() = this(Identifiable.randomUID("basic_trainer"))

  val split: Param[BaseCrossValidator] = new Param(this, "split", "base cross validator")

  def setSplit(value: BaseCrossValidator): this.type = set(split, value)

  def trainable: Boolean = estimator != null

  override def fit(trainDf: DataFrame): Unit = {
    val dataFrames = if (isSet(split)) {
      $(split).split(trainDf)
    } else {
      Iterator(trainDf -> null)
    }
    val nSplits = if (isSet(split)) $(split).getNSplits else 1
    models = dataFrames.zipWithIndex.map { case ((train, eval), i) =>
      println(s"Start training model[${i + 1}/$nSplits]...")
      val model = estimator.fit(train).asInstanceOf[Transformer]
      if (eval != null) {
        val params = metric(model.transform(eval))
        var msg = "VALIDATION METRICS: " + params.toJson(true)
        if (isSet(split))
          msg = s"Model[${i + 1}/$nSplits] $msg"
        println(msg)
      }
      model
    }.toArray
  }

  override def transform(testDf: DataFrame): DataFrame = {
    if (models.isEmpty)
      fit(testDf)

    if (models.length > 1) {
      models.zipWithIndex.map {
        case (model, i) =>
          model.transform(testDf).withColumn(CROSS_VAL_COL_NAME, lit(i))
      }.reduce((df1, df2) => df1.union(df2))
    } else {
      models.last.transform(testDf)
    }
  }

  override def fitTransform(trainDf: DataFrame): DataFrame = {
    val dataFrames = if (isSet(split)) {
      $(split).split(trainDf)
    } else {
      Iterator(trainDf -> trainDf)
    }
    var predDf: DataFrame = null
    val nSplits = if (isSet(split)) $(split).getNSplits else 1
    models = dataFrames.zipWithIndex.map { case ((train, eval), i) =>
      println(s"Start training model[${i + 1}/$nSplits]...")
      val model = estimator.fit(train).asInstanceOf[Transformer]
      val predDf_ = model.transform(eval)
      if (predDf == null)
        predDf = predDf_
      else
        predDf = predDf.union(predDf_)
      model
    }.toArray
    predDf
  }

  override def evaluate(evalDf: DataFrame): collection.Params = {
    if (models.isEmpty)
      fit(evalDf)

    val allParams = models.map(model => metric(model.transform(evalDf)))
    if (allParams.length > 1) {
      val params = new collection.Params()
      allParams.zipWithIndex.foreach(it => params.put(CROSS_VAL_COL_NAME + it._2, it._1.getMap))
      params
    } else {
      allParams.last
    }
  }

  override def load(path: String): Unit = {
    val modelClazz = if (models.nonEmpty) models.last.getClass.asInstanceOf[Class[_]]
    else if (estimator != null) {
      val clazz = estimator.getClass
      MLUtils.getModelClass(clazz)
    } else {
      null.asInstanceOf[Class[_]]
    }
    val pathFile = IOUtil.join(path, CROSS_VAL_COL_NAME)
    val paths = if (IOUtil.exists(pathFile)) {
      IOUtil.readLines(pathFile).map(it => IOUtil.join(path, it))
        .filter(IOUtil.exists).toArray
    } else {
      Array(path)
    }
    models = paths.map(path => {
      val basename = IOUtil.baseName(path)
      val id = if (basename.startsWith(CROSS_VAL_COL_NAME))
        basename.substring(CROSS_VAL_COL_NAME.length).toInt
      else 0
      (id, MLUtils.read(modelClazz, path).asInstanceOf[Transformer])
    }).filter(_._2 != null).sortBy(_._1).map(_._2)
  }

  override def save(path: String): Unit = {
    val outputPaths = if (models.length > 1) {
      models.indices.map(i => IOUtil.join(path, CROSS_VAL_COL_NAME + i)).toArray
    } else {
      Array(path)
    }
    if (outputPaths.length > 1) {
      val pathFile = IOUtil.join(path, CROSS_VAL_COL_NAME)
      IOUtil.mkdirs(IOUtil.parentName(pathFile))
      IOUtil.writeLines(pathFile, outputPaths.toList.map(IOUtil.baseName))
    }
    models.zip(outputPaths).foreach {
      case (model, path) =>
        if (classOf[MLWritable].isAssignableFrom(model.getClass)) {
          model.asInstanceOf[MLWritable].save(path)
        }
    }
  }

  override def copy(extra: ParamMap): Params = defaultCopy(extra)
}
