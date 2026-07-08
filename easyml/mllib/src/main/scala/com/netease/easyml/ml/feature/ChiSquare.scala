package com.netease.easyml.ml.feature

import com.netease.easyml.common.util.ConvertUtil
import com.netease.easyml.ml.util.SchemaUtils
import org.apache.spark.ml.Model
import org.apache.spark.ml.param._
import org.apache.spark.ml.param.shared.{HasInputCol, HasLabelCol, HasOutputCol}
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Dataset, Row}
import org.apache.spark.storage.StorageLevel

/**
 * Created by linjiuning on 2020/12/4.
 */

trait ChiSquareParams extends Params with HasInputCol with HasLabelCol with HasOutputCol {
  val countCol: Param[String] = new Param[String](this, "countCol", "count column name")

  def getCountCol: String = $(countCol)

  val sign: BooleanParam = new BooleanParam(this, "sign", "sign")

  def getSign: Boolean = $(sign)

  setDefault(sign -> true, inputCol -> "feature", countCol -> "count", outputCol -> "score")

  protected def validateAndTransformSchema(schema: StructType): StructType = {
    SchemaUtils.checkStringType(schema, $(labelCol))
    SchemaUtils.checkStringType(schema, $(inputCol))
    SchemaUtils.checkNumericType(schema, $(countCol))
    StructType(Seq(schema($(labelCol)), schema($(inputCol)), StructField($(countCol), DoubleType), StructField($(outputCol), DoubleType)))
  }
}

class ChiSquare(override val uid: String) extends Model[ChiSquare] with ChiSquareParams with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("chi"))

  def setInputCol(value: String): this.type = set(inputCol, value)

  def setLabelCol(value: String): this.type = set(labelCol, value)

  def setOutputCol(value: String): this.type = set(outputCol, value)

  def setCountCol(value: String): this.type = set(countCol, value)

  def setSign(value: Boolean): this.type = set(sign, value)

  override def transform(dataset: Dataset[_]): DataFrame = {
    transformSchema(dataset.schema, logging = true)
    val counter = dataset
      .filter(col($(labelCol)).isNotNull &&
        col($(inputCol)).isNotNull &&
        col($(countCol)).isNotNull)
      .select($(labelCol), $(inputCol), $(countCol)).rdd
      .flatMap(row => {
        val count = ConvertUtil.toDouble(row.get(2))
        Array(
          ((row.getString(0), null), count),
          ((null, row.getString(1)), count),
          ((row.getString(0), row.getString(1)), count)
        )
      }).reduceByKey(_ + _)

    val spark = dataset.sparkSession
    import spark.implicits._

    val rowSumDf = counter.filter(it => it._1._2 == null)
      .map(it => (it._1._1, it._2))
      .toDF("i", "count")

    val colSumDf = counter.filter(it => it._1._1 == null)
      .map(it => (it._1._2, it._2))
      .toDF("j", "count")

    val pairDf = counter.filter(it => it._1._1 != null && it._1._2 != null)
      .map(it => (it._1._1, it._1._2, it._2))
      .toDF("i", "j", "count")

    rowSumDf.persist(StorageLevel.MEMORY_AND_DISK)
    colSumDf.persist(StorageLevel.MEMORY_AND_DISK)
    pairDf.persist(StorageLevel.MEMORY_AND_DISK)

    val total = rowSumDf.rdd.map(_.getDouble(1)).sum()

    pairDf.createOrReplaceTempView("pair")
    rowSumDf.createOrReplaceTempView("row")
    colSumDf.createOrReplaceTempView("col")

    spark.sql("select pair.*, row.count as r_c from pair join row on pair.i = row.i")
      .createOrReplaceTempView("pair")

    val joinDf = spark.sql("select pair.*, col.count as c_c from pair join col on pair.j = col.j")

    val sign = getSign
    val resultRdd = joinDf.rdd
      .map {
        case Row(label: String, feature: String, cnt: Double, row: Double, col: Double) =>
          val rc = row * col / total
          var significant = Math.pow(cnt - rc, 2) / rc
          if (sign && cnt < rc) {
            significant *= -1
          }
          (label, feature, cnt, significant)
      }.map(it => Row.fromTuple(it))
    dataset.sparkSession.createDataFrame(resultRdd, transformSchema(dataset.schema))
  }

  override def copy(extra: ParamMap): ChiSquare = defaultCopy(extra)

  override def transformSchema(schema: StructType): StructType = {
    validateAndTransformSchema(schema)
  }

}

object ChiSquare extends DefaultParamsReadable[ChiSquare] {
  override def load(path: String): ChiSquare = super.load(path)
}
