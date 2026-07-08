package com.netease.easyml.ml

import org.apache.spark.ml.linalg.Vector
import org.apache.spark.ml.param.{Param, ParamValidators, Params}
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions.udf

/**
 * Created by linjiuning on 2020/7/16.
 */
package object metric {

  def toDoubleArray(y: Any): Seq[Double] = {
    y match {
      case e: java.lang.Number => Seq(e.doubleValue())
      case vec: Vector => vec.toArray
      case arr: Seq[java.lang.Number] => arr.map(_.doubleValue())
    }
  }

  val toDoubleArrayUdf: UserDefinedFunction = udf(toDoubleArray _)

  trait HasAverageType extends Params {
    final val average = new Param[String](this, "average",
      "This parameter is required for multiclass/multilabel targets. If None, the scores for each class are returned. Otherwise, this determines the type of averaging performed on the data",
      ParamValidators.inArray(Array("micro", "macro", "weighted", "none")))

    def getAverage: String = $(average)

    setDefault(average, "none")
  }

}
