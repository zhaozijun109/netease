package com.netease.music.da.transfer.common.metrics

import org.apache.spark.SparkConf

trait MetricsRecorder {
  def record(sparkConf: SparkConf)
}
