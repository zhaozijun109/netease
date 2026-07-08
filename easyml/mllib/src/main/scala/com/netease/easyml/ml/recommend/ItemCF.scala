package com.netease.easyml.ml.recommend

import com.netease.easyml.ml.util.SchemaUtils
import org.apache.spark.ml.Model
import org.apache.spark.ml.param._
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Dataset}

/**
 * Created by linjiuning on 2022/11/1.
 */

trait ItemCFParams extends Params {

  val userCol: Param[String] = new Param[String](this, "userCol", "user column name")

  def getUserCol: String = $(userCol)

  def setUserCol(value: String): this.type = set(userCol, value)

  val itemCol: Param[String] = new Param[String](this, "itemCol", "item column name")

  def getItemCol: String = $(itemCol)

  def setItemCol(value: String): this.type = set(itemCol, value)

  val alpha: DoubleParam = new DoubleParam(this, "alpha", "alpha")

  def getAlpha: Double = $(alpha)

  def setAlpha(value: Double): this.type = set(alpha, value)

  val minSameUser: IntParam = new IntParam(this, "minSameUser", "minSameUser")

  def getMinSameUser: Int = $(minSameUser)

  def setMinSameUser(value: Int): this.type = set(minSameUser, value)

  setDefault(alpha -> 1, minSameUser -> 0)

  protected def validateAndTransformSchema(schema: StructType): StructType = {
    SchemaUtils.checkStringType(schema, $(userCol))
    SchemaUtils.checkStringType(schema, $(itemCol))
    StructType(Seq(StructField("i1", StringType),
      StructField("i2", StringType),
      StructField("score", DoubleType)))
  }
}

class ItemCF(override val uid: String) extends Model[ItemCF] with ItemCFParams with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("itemcf"))

  override def transform(dataset: Dataset[_]): DataFrame = {
    transformSchema(dataset.schema, logging = true)

    val userCol = getUserCol
    val itemCol = getItemCol
    val alpha = getAlpha
    val minSameUser = getMinSameUser

    def cooccurrence(numOfRatersForAAndB: Double, numOfRatersForA: Double, numOfRatersForB: Double): Double = {
      if (numOfRatersForA > numOfRatersForB)
        numOfRatersForAAndB / (math.pow(numOfRatersForA, alpha) * math.pow(numOfRatersForB, 1 - alpha))
      else
        numOfRatersForAAndB / (math.pow(numOfRatersForA, 1 - alpha) * math.pow(numOfRatersForB, alpha))
    }

    val cooccurrenceUdf = udf(cooccurrence _)

    val uiDf = dataset.select(col(userCol).alias("u"), col(itemCol).alias("i"))

    val wI = uiDf.groupBy("i")
      .agg(count("u").alias("score"))

    val uiiDf = uiDf.alias("t1").join(uiDf.alias("t2"), "u")
      .where("t1.i < t2.i")
      .select(col("u"), col("t1.i").alias("i1"), col("t2.i").alias("i2"))

    var iiCommonDf = uiiDf.groupBy("i1", "i2")
      .agg(count("u").alias("inter"))

    if (minSameUser > 0) {
      iiCommonDf = iiCommonDf.filter(col("inter") >= lit(minSameUser))
    }

    val iiDf = iiCommonDf.alias("t1")
      .join(wI.alias("t2"), col("t1.i1") === col("t2.i"))
      .select("t1.*", "t2.score")
      .alias("t1")
      .join(wI.alias("t2"), col("t1.i2") === col("t2.i"))
      .select(col("t1.i1"), col("t1.i2"),
        cooccurrenceUdf(col("t1.inter"), col("t1.score"), col("t2.score")).alias("score"))

    val iiTDf = iiDf.select(col("i2").alias("i1"), col("i1").alias("i2"), col("score"))
    iiDf.union(iiTDf)
  }

  override def copy(extra: ParamMap): ItemCF = defaultCopy(extra)

  override def transformSchema(schema: StructType): StructType = {
    validateAndTransformSchema(schema)
  }

}

object ItemCF extends DefaultParamsReadable[ItemCF] {
  override def load(path: String): ItemCF = super.load(path)
}
