package com.netease.easyudf.cmd

import com.microsoft.azure.synapse.ml.lightgbm.{LightGBMClassificationModel, LightGBMRegressionModel}
import com.netease.easyml.common.cmds.UserDefinedCmd
import com.netease.easyml.common.collection.Params
import com.netease.easyml.common.util.{IOUtil, SparkUtil}
import com.netease.easyml.ml.metric.toDoubleArrayUdf
import com.netease.easyml.ml.sklearn.preprocessing.Normalizer
import com.netease.easyml.ml.transform.{DropColumn, ColumnRenamed => Renamed}
import com.netease.easyml.ml.util.{SchemaUtils, Utils => EMUtils}
import com.netease.easyudf.cmd.Estimator._
import ml.dmlc.xgboost4j.scala.{Booster, XGBoost}
import org.apache.commons.lang3.StringUtils
import org.apache.spark.ml.Transformer
import org.apache.spark.ml.feature_.VectorAssembler
import org.apache.spark.ml.util.Identifiable
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.collection.JavaConverters._
import scala.collection.mutable

case class GBMPredictorArgs(input: String, path: String, params: String, include: String = "", exclude: String = "",
                            libsvm: Boolean = false, normalizer: String = "")

class GBMPredictor extends UserDefinedCmd[GBMPredictorArgs] {

  override def apply(spark: SparkSession, args: GBMPredictorArgs): DataFrame = {
    val stages = mutable.ArrayBuffer.empty[Transformer]

    var df = spark.table(args.input)
    var columns = df.columns
    df = df.selectExpr(columns.map(col => if (SchemaUtils.isNumericType(df.schema, col)) s"nvl($col, 0) as $col" else col): _*)
    if (args.libsvm) {
      df = LibsvmDecoder.decode(df, args.include)
      columns = df.columns
    } else {
      columns = columns.filter(col => SchemaUtils.isNumericType(df.schema, col))
    }
    columns = Select.filterColumns(columns, args.include, args.exclude)

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
    val uid = Identifiable.randomUID("booster")

    val model = params.remove("type")
    val predictor = model match {
      case "XGBoostRegressor" | "XGBoostClassifier" =>
        params.put("allowNonZeroForMissing", true)
        val booster = XGBoost.loadModel(IOUtil.getInputStream(args.path))
        if (model.equals("XGBoostRegressor")) {
          val constructor = SparkUtil.classForName("ml.dmlc.xgboost4j.scala.spark.XGBoostRegressionModel").getConstructor(classOf[String], classOf[Booster])
          constructor.newInstance(uid, booster).asInstanceOf[Transformer]
        }
        else {
          val constructor = SparkUtil.classForName("ml.dmlc.xgboost4j.scala.spark.XGBoostClassificationModel").getConstructor(classOf[String], classOf[Int], classOf[Booster])
          val numClasses = if (params.containsKey("numClasses")) params.remove("numClasses").toString.toInt else 2
          constructor.newInstance(uid, numClasses.asInstanceOf[Object], booster).asInstanceOf[Transformer]
        }
      case "LightGBMRegressor" =>
        LightGBMRegressionModel.loadNativeModelFromFile(args.path)
      case "LightGBMClassifier" =>
        LightGBMClassificationModel.loadNativeModelFromFile(args.path)
      case _ =>
        throw new Exception(s"unsupported model type $model, current supported model types [XGBoostRegressor, XGBoostClassifier, LightGBMRegressor, LightGBMClassifier]")
    }
    params.keySet().asScala.foreach(k => EMUtils.set(predictor, k, params.get(k)))

    stages.append(predictor)

    val dropColumn = new DropColumn().setInputCols(Array(FEATURES))
    stages.append(dropColumn)

    val pipeline = EMUtils.newPipeLineModel(stages.toArray)

    var newDf = pipeline.transform(df)

    newDf.columns.filter(it => SchemaUtils.isVectorType(newDf.schema, it))
      .foreach(it => newDf = newDf.withColumn(it, toDoubleArrayUdf(col(it))))
    newDf
  }
}