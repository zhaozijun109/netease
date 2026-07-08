package com.netease.easyml.ml.recommend

import com.netease.easyml.ml.param.HasNumPartitions
import com.netease.easyml.ml.util.SchemaUtils
import org.apache.spark.ml.Model
import org.apache.spark.ml.param._
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Dataset}

/**
 * Created by linjiuning on 2020/12/30.
 * https://www.jianshu.com/p/a5d46cdc2b4e
 * https://zhuanlan.zhihu.com/p/143564029
 */

trait SwingParams extends Params with HasNumPartitions {

  val userCol: Param[String] = new Param[String](this, "userCol", "user column name")

  def getUserCol: String = $(userCol)

  def setUserCol(value: String): this.type = set(userCol, value)

  val itemCol: Param[String] = new Param[String](this, "itemCol", "item column name")

  def getItemCol: String = $(itemCol)

  def setItemCol(value: String): this.type = set(itemCol, value)

  val power: DoubleParam = new DoubleParam(this, "power", "score power")

  def getPower: Double = $(power)

  def setPower(value: Double): this.type = set(power, value)

  val alpha: DoubleParam = new DoubleParam(this, "alpha", "alpha")

  def getAlpha: Double = $(alpha)

  def setAlpha(value: Double): this.type = set(alpha, value)

  val eps: DoubleParam = new DoubleParam(this, "eps", "eps")

  def getEps: Double = $(eps)

  def setEps(value: Double): this.type = set(eps, value)

  def setNumPartitions(value: Int): this.type = set(numPartitions, value)

  setDefault(power -> -0.35, alpha -> 1, eps -> 5)

  protected def validateAndTransformSchema(schema: StructType): StructType = {
    SchemaUtils.checkStringType(schema, $(userCol))
    SchemaUtils.checkStringType(schema, $(itemCol))
    StructType(Seq(StructField("i1", StringType),
      StructField("i2", StringType),
      StructField("score", DoubleType)))
  }
}

class Swing(override val uid: String) extends Model[Swing] with SwingParams with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("swing"))

  override def transform(dataset: Dataset[_]): DataFrame = {
    transformSchema(dataset.schema, logging = true)
    val spark = dataset.sparkSession

    val nPartitions = if (isSet(numPartitions)) {
      getNumPartitions
    } else {
      spark.sparkContext.defaultParallelism
    }

    val userCol = getUserCol
    val itemCol = getItemCol
    val power = getPower

    val userItem = dataset.select(col(userCol).alias("u"), col(itemCol).alias("i"))
      .filter(col("u").isNotNull && col("i").isNotNull)
      .distinct()
      .repartition(nPartitions)

    val eps = getEps
    val wU = userItem.groupBy("u")
      .agg(pow(count("i") + lit(eps), power).alias("score"))

    val itemU1U2 = userItem.alias("t1").join(userItem.alias("t2"), "i")
      .where("t1.u < t2.u")
      .select(col("i"), col("t1.u").alias("u1"), col("t2.u").alias("u2"))

    val u1U2CommonDf = itemU1U2.groupBy("u1", "u2")
      .agg(count("i").alias("inter"))

    val u1U2I1I2 = itemU1U2.alias("t1").join(itemU1U2.alias("t2"), Array("u1", "u2"))
      .where("t1.i < t2.i")
      .select(col("u1"), col("u2"), col("t1.i").alias("i1"), col("t2.i").alias("i2"))

    val alpha = getAlpha
    val wPair = u1U2CommonDf.alias("t1")
      .join(wU.alias("t2"), col("t1.u1") === col("t2.u"))
      .select("t1.*", "t2.score")
      .alias("t1")
      .join(wU.alias("t2"), col("t1.u2") === col("t2.u"))
      .select(col("t1.u1"), col("t1.u2"),
        (col("t2.score") * col("t1.score") / (lit(alpha) + col("t1.inter"))).alias("score"))

    u1U2I1I2.alias("t1").join(wPair.alias("t2"), Array("u1", "u2"))
      .select("t1.i1", "t1.i2", "t2.score")
      .groupBy("i1", "i2")
      .agg(sum("score").alias("score"))
  }

  override def copy(extra: ParamMap): Swing = defaultCopy(extra)

  override def transformSchema(schema: StructType): StructType = {
    validateAndTransformSchema(schema)
  }

}

object Swing extends DefaultParamsReadable[Swing] {
  override def load(path: String): Swing = super.load(path)
}
