package com.netease.easyml.ml.metric

import org.apache.spark.sql.Dataset

/**
 * Created by linjiuning on 2020/7/20.
 */
abstract class ReportMetric extends Metric {

  override def isLargerBetter: Boolean = {
    throw new UnsupportedOperationException(s"${getClass.getSimpleName} is not comparable.")
  }

  override def evaluate(dataset: Dataset[_]): Double = {
    throw new UnsupportedOperationException(s"${getClass.getSimpleName} is not a double value.")
  }

}
