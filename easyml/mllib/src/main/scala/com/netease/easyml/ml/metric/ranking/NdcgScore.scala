package com.netease.easyml.ml.metric.ranking

import com.netease.easyml.annotation.Register
import com.netease.easyml.ml.metric.Metric
import com.netease.easyml.ml.param.HasK
import com.netease.easyml.ml.util.SchemaUtils
import org.apache.spark.ml.param.shared.{HasLabelCol, HasPredictionCol}
import org.apache.spark.ml.util.{DefaultParamsWritable, Identifiable}
import org.apache.spark.mllib.evaluation.RankingMetrics
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.types.{ArrayType, DoubleType}

/**
 * Created by linjiuning on 2020/7/16.
 * Compute Normalized Discounted Cumulative Gain.
 */
@Register(alias = Array("ndcg", "ndcg_score"))
class NdcgScore(override val uid: String) extends Metric
  with HasPredictionCol with HasLabelCol with HasK with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("ndcg"))

  override lazy val shortName: String = "ndcg_" + $(k)

  def setK(value: Int): this.type = set(k, value)

  setDefault(k, 10)

  def setPredictionCol(value: String): this.type = set(predictionCol, value)

  def setLabelCol(value: String): this.type = set(labelCol, value)

  override def evaluate(dataset: Dataset[_]): Double = {
    val schema = dataset.schema
    SchemaUtils.checkColumnTypes(schema, $(predictionCol),
      Seq(ArrayType(DoubleType, false), ArrayType(DoubleType, true)))
    SchemaUtils.checkColumnTypes(schema, $(labelCol),
      Seq(ArrayType(DoubleType, false), ArrayType(DoubleType, true)))

    val predictionAndLabels =
      dataset.select(col($(predictionCol)), col($(labelCol)))
        .rdd.map { row =>
        (row.getSeq[Double](0).toArray, row.getSeq[Double](1).toArray)
      }

    new RankingMetrics[Double](predictionAndLabels)
      .ndcgAt($(k))
  }

  override def isLargerBetter: Boolean = true
}

