package com.netease.easyudf.cmd

import com.alibaba.fastjson.JSON
import com.netease.easyml.common.cmds.UserDefinedCmd
import com.netease.easyml.common.collection.Params
import com.netease.easyml.common.util.SparkUtil
import com.netease.easyml.launcher.FromParams
import com.netease.easyml.ml.metric.{Metric, toDoubleArrayUdf}
import com.netease.easyml.ml.sklearn.feature_selection.SelectFromModel
import com.netease.easyml.ml.sklearn.preprocessing.Normalizer
import com.netease.easyml.ml.transform.{DropColumn, ColumnRenamed => Renamed}
import com.netease.easyml.ml.util.{SchemaUtils, Utils => EMUtils}
import com.netease.easyudf.cmd.Estimator._
import com.netease.easyudf.util.Utils
import ml.dmlc.xgboost4j.scala.spark.{TrackerConf, XGBoostClassifier, XGBoostRegressor}
import org.apache.commons.lang3.StringUtils
import org.apache.spark.ml.evaluation.{Evaluator => MLEvaluator}
import org.apache.spark.ml.feature_.{IndexToString, StringIndexer, StringIndexerModel, VectorAssembler}
import org.apache.spark.ml.{Pipeline, PipelineStage, Predictor}
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

case class EstimatorArgs(input: String, path: String, eval: String = null, include: String = "", exclude: String = "",
                         labelCol: String = "label", weightCol: String = "weight",
                         params: String = "{}", mode: String = Estimator.TRAIN, task: String = Estimator.CLASSIFIER,
                         metric: String = null, libsvm: Boolean = false, printFeatureImportance: Boolean = false,
                         normalizer: String = "", dynamicAllocation: Boolean = false)

class Estimator extends UserDefinedCmd[EstimatorArgs] {

  override def apply(spark: SparkSession, args: EstimatorArgs): DataFrame = {
    val stages = mutable.ArrayBuffer.empty[PipelineStage]

    val numWorkers = SparkUtil.getParallelism(spark.sparkContext.getConf)
    val numThreads = SparkUtil.getNumTaskCpus(spark.sparkContext.getConf)
    logInfo(s"Num workers: $numWorkers, num threads per worker: $numThreads")
    var df = spark.table(args.input)
    var columns = df.columns
    df = df.selectExpr(columns.map(col => if (SchemaUtils.isNumericType(df.schema, col)) s"nvl($col, 0) as $col" else col): _*)
    if (args.libsvm) {
      df = LibsvmDecoder.decode(df, args.include)
      columns = df.columns
    } else {
      columns = columns.filter(col => SchemaUtils.isNumericType(df.schema, col))
    }
    val weightCol = if (StringUtils.isBlank(args.weightCol) || !columns.contains(args.weightCol)) null else args.weightCol
    columns = Select.filterColumns(columns, args.include, args.exclude)

    if (args.mode.equals(TRAIN)) {
      if (StringUtils.isNoneBlank(args.labelCol) && df.columns.contains(args.labelCol)) {
        columns = columns.filterNot(_.equals(args.labelCol))
        if (args.task.equals(CLASSIFIER) && SchemaUtils.isStringType(df.schema, args.labelCol)) {
          val stringIndexer = new StringIndexer().setInputCol(args.labelCol).setOutputCol(s"tmp_${args.labelCol}")
          stages.append(stringIndexer)
          stages.append(new DropColumn().setInputCols(Array(stringIndexer.getInputCol)))
          stages.append(new Renamed().setInputCols(Array(stringIndexer.getOutputCol)).setOutputCols(Array(stringIndexer.getInputCol)))
        }
      }
      if (StringUtils.isNoneBlank(weightCol) && columns.contains(weightCol)) {
        columns = columns.filterNot(_.equals(weightCol))
      }

      var featuresCol = FEATURES
      if (!args.libsvm) {
        val assembler = new VectorAssembler()
          .setInputCols(columns)
          .setOutputCol(FEATURES)
          .setKeepInputCol(false)
          .setHandleInvalid("keep_zero")

        stages.append(assembler)
      } else {
        featuresCol = args.include
      }

      if (StringUtils.isNoneBlank(args.normalizer)) {
        val normalizer = new Normalizer().setInputCol(featuresCol).setOutputCol(s"tmp_$featuresCol").setNorm(args.normalizer)
        stages.append(normalizer)
        stages.append(new DropColumn().setInputCols(Array(normalizer.getInputCol)))
        stages.append(new Renamed().setInputCols(Array(normalizer.getOutputCol)).setOutputCols(Array(normalizer.getInputCol)))
      }

      val params = Params.fromJson(args.params)
      params.put("featuresCol", featuresCol)
      params.put("labelCol", args.labelCol)
      if (StringUtils.isNoneBlank(weightCol)) {
        params.put("weightCol", weightCol)
      }
      if (!SparkUtil.isLocalMaster(spark.sparkContext.getConf) && !args.dynamicAllocation) {
        params.put("numWorkers", numWorkers)
        params.put("nthread", numThreads)
        params.put("numTasks", numWorkers)
        params.put("numThreads", numThreads)
      }
      val booster = fromParams[Predictor[_, _, _]](params.toJson)

      booster match {
        case classifier: XGBoostClassifier =>
          classifier.set(classifier.trackerConf, TrackerConf(0, "scala"))
        case regressor: XGBoostRegressor =>
          regressor.set(regressor.trackerConf, TrackerConf(0, "scala"))
        case _ =>
      }
      stages.append(booster)

      val dropColumn = new DropColumn().setInputCols(Array(FEATURES))
      stages.append(dropColumn)

      val pipeline = new Pipeline()
        .setStages(stages.toArray)

      val model = pipeline.fit(df)
      Utils.safeSavePipelineModel(model, args.path)
      if (args.printFeatureImportance) {
        val transformer = model.stages(stages.size - 2)
        val importance = SelectFromModel.modelFeatureImportance(transformer)
        if (args.libsvm) {
          importance.zipWithIndex.map(it => (it._2, it._1)).sortBy(_._2).reverse.foreach(println)
        } else {
          columns.zip(importance).sortBy(_._2).reverse.foreach(println)
        }
      }
      if (StringUtils.isNoneBlank(args.eval)) {
        var evalDf = spark.table(args.eval)
        evalDf = evalDf.selectExpr(evalDf.columns.map(col => if (SchemaUtils.isNumericType(evalDf.schema, col)) s"nvl($col, 0) as $col" else col): _*)
        if (args.libsvm) {
          evalDf = LibsvmDecoder.decode(evalDf, args.include)
        }
        val prediction = model.transform(evalDf)
        evaluate(prediction, args.task, args.metric, labelCol = args.labelCol, predictionCol = booster.getPredictionCol)
      }
      null
    } else {
      var model = Utils.safeLoadPipelineModel(args.path)
      var transformers = model.stages
      transformers.reverse.filter(it => it.isInstanceOf[StringIndexerModel]).foreach(it => {
        val m = it.asInstanceOf[StringIndexerModel]
        val stringIndexer = new IndexToString().setInputCol(PREDICTION).setOutputCol(PREDICTION_LABEL).setLabels(m.labelsArray(0))
        transformers = transformers :+ stringIndexer
      })
      model = EMUtils.newPipeLineModel(transformers)
      var newDf = model.transform(df)
      if (args.mode.equals(EVAL)) {
        evaluate(newDf, args.task, args.metric, labelCol = args.labelCol)
        null
      } else {
        newDf.columns.filter(it => SchemaUtils.isVectorType(newDf.schema, it))
          .foreach(it => newDf = newDf.withColumn(it, toDoubleArrayUdf(col(it))))
        newDf
      }
    }
  }
}

object Estimator {
  val TRAIN = "train"
  val EVAL = "eval"
  val CLASSIFIER = "classifier"
  val NATIVE = "native"

  val FEATURES = "features"
  val PREDICTION = "prediction"
  val PREDICTION_LABEL = "prediction_label"

  def evaluate(prediction: DataFrame, task: String, metric: String,
               rawPredictionCol: String = "rawPrediction", predictionCol: String = "prediction",
               labelCol: String = "label"): Unit = {
    val temp = new Params()
    temp.put("rawPredictionCol", rawPredictionCol)
    temp.put("predictionCol", predictionCol)
    temp.put("labelCol", labelCol)

    val metricParams = ArrayBuffer.empty[Params]
    if (StringUtils.isBlank(metric)) {
      if (task.equals(CLASSIFIER)) {
        val param = temp.duplicate()
        param.put("type", "auc")
        metricParams.append(param)
      } else {
        val param = temp.duplicate()
        param.put("type", "mse")
        metricParams.append(param)
      }
    } else {
      val array = JSON.parseArray(metric)
      (0 until array.size()).foreach(i => {
        val param = temp.duplicate()
        param.putAll(Params.fromJson(array.getString(i)))
        metricParams.append(param)
      })
    }
    val metrics = metricParams.map(it => FromParams.fromParams(classOf[MLEvaluator], it).asInstanceOf[Metric])
    Evaluator.evaluate(prediction, metrics)
  }
}