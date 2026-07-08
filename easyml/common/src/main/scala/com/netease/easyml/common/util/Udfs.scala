package com.netease.easyml.common.util

import com.linkedin.spark.shaded.org.tensorflow.example.Example
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions.udf


/**
 * Created by linjiuning on 2022/8/12.
 */
object Udfs {
  val mergeExampleUdf: UserDefinedFunction = udf(mergeExample _)

  def mergeExample(a: Array[Byte], b: Array[Byte]): Array[Byte] = {
    if (a != null && b != null) {
      Example.parseFrom(a).toBuilder.mergeFrom(b).build().toByteArray
    } else if (a != null) {
      a
    } else {
      b
    }
  }
}
