package com.netease.easyml.ml.metric.ranking

import com.netease.easyml.annotation.Register
import com.netease.easyml.ml.metric.Metric
import org.apache.spark.ml.param.shared.{HasLabelCol, HasPredictionCol}
import org.apache.spark.ml.param.{Param, Params}
import org.apache.spark.ml.util.{DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.functions.col
import org.apache.spark.storage.StorageLevel

/**
 * Created by linjiuning on 2021/3/22.
 * Based on https://github.com/qiaoguan/deep-ctr-prediction/blob/master/DeepCross/metric.py
 */

trait HasUserIdCol extends Params {

  final val userIdCol: Param[String] = new Param[String](this, "userIdCol", "userId column name")

  setDefault(userIdCol, "user_id")

  final def getUserIdCol: String = $(userIdCol)
}

@Register(alias = Array("gauc", "group_auc"))
class GroupAUC(override val uid: String) extends Metric
  with HasPredictionCol with HasLabelCol with HasUserIdCol with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("groupAUC"))

  def setUserId(value: String): this.type = set(userIdCol, value)

  def setPredictionCol(value: String): this.type = set(predictionCol, value)

  def setLabelCol(value: String): this.type = set(labelCol, value)

  override lazy val shortName: String = "group_auc"

  override def evaluate(dataset: Dataset[_]): Double = {
    val spark = dataset.sparkSession
    import spark.implicits._
    val userId = getUserIdCol
    val prediction = getPredictionCol
    val label = getLabelCol
    val columns = Array(userId, prediction, label).map(col)
    val df = dataset.select(columns: _*)

    df.persist(StorageLevel.MEMORY_AND_DISK)

    val posDf = df.filter(col(label).equalTo(1.0))
    val negDf = df.filter(col(label).equalTo(0.0))

    posDf.createOrReplaceTempView("pos")
    negDf.createOrReplaceTempView("neg")
    val joinDf = spark.sql(s"select pos.$userId, pos.$prediction, neg.$prediction from pos join neg on pos.$userId=neg.$userId")

    val aucDf = joinDf.toDF.rdd.map(row => {
      val id = row.getString(0)
      val ps = row.getDouble(1)
      val ns = row.getDouble(2)
      val auc = if (ps > ns) {
        1.0
      } else if (ps == ns) {
        0.5
      } else {
        0.0
      }
      (id, (auc, 1L))
    }).reduceByKey((it1, it2) => {
      (it1._1 + it2._1, it1._2 + it2._2)
    }).map(it => (it._1, it._2._1 / it._2._2))
      .toDF(userId, "auc")

    val weightDf = df.rdd.map(row => (row.getString(0), 1L))
      .reduceByKey(_ + _)
      .toDF(userId, "freq")

    val result = aucDf.join(weightDf, aucDf(userId) === weightDf(userId))
      .select("auc", "freq")
      .rdd
      .map(row => (row.getDouble(0), row.getLong(1)))

    result.persist(StorageLevel.MEMORY_AND_DISK)

    val total = result.map(_._2).reduce(_ + _)

    val auc = result.map(it => it._1 * it._2 / total).reduce(_ + _)
    result.unpersist()
    auc
  }

  override def isLargerBetter: Boolean = true
}
