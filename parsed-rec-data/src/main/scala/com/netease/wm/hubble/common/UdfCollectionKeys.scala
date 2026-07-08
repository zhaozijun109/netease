package com.netease.wm.hubble.common

import org.apache.flink.table.functions.ScalarFunction
import org.apache.flink.table.annotation.DataTypeHint
import scala.collection.JavaConverters._

class UdfCollectionKeys extends ScalarFunction {
  def eval(@DataTypeHint("MULTISET<BIGINT>") multiset: java.util.Map[Long, Integer]): String = {
    multiset.keySet().asScala.mkString(",")
  }
}
