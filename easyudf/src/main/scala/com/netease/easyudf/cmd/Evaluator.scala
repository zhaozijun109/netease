package com.netease.easyudf.cmd

import com.alibaba.fastjson.JSON
import com.netease.easyml.common.cmds.VoidUserDefinedCmd
import com.netease.easyml.common.collection.Params
import com.netease.easyml.launcher.FromParams
import com.netease.easyml.ml.metric.Metric
import com.netease.easyudf.util.Utils
import org.apache.spark.ml.evaluation.{Evaluator => MLEvaluator}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.storage.StorageLevel

case class EvaluatorArgs(input: String, params: String)

class Evaluator extends VoidUserDefinedCmd[EvaluatorArgs] {

  override def run(spark: SparkSession, args: Args): Unit = {
    val df = spark.table(args.input)
    val array = JSON.parseArray(args.params)
    Utils.register()
    val metrics = (0 until array.size()).map(i => {
      val params = Params.fromJson(array.getString(i))
      FromParams.fromParams(classOf[MLEvaluator], params).asInstanceOf[Metric]
    })
    Evaluator.evaluate(df, metrics)
  }
}

object Evaluator {

  def evaluate(df: DataFrame, metrics: Seq[Metric]): Unit = {
    val params = new Params()
    val handelPersistent = df.storageLevel == StorageLevel.NONE
    if (handelPersistent && metrics.size > 1) df.persist(StorageLevel.MEMORY_AND_DISK)
    for (elem <- metrics if elem != null) {
      val p_ = elem.evaluateJson(df)
      params.putAll(p_)
    }
    if (handelPersistent && metrics.size > 1) df.unpersist()

    println(s"METRIC: ${params.toJson}")
  }
}