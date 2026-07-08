package com.netease.easyml.ml.nlp

import com.netease.easyml.ml.param.{HasLowercase, HasNumPartitions}
import com.netease.easyml.ml.util.SchemaUtils
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.ml.Model
import org.apache.spark.ml.param._
import org.apache.spark.ml.param.shared.HasInputCol
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Dataset, Row}
import org.apache.spark.storage.StorageLevel

import scala.collection.mutable.ArrayBuffer

/**
 * Created by linjiuning on 2020/8/20.
 */

trait PMIStatParams extends Params with HasInputCol with HasLowercase with HasNumPartitions {
  def setInputCol(value: String): this.type = set(inputCol, value)

  def setLowercase(value: Boolean): this.type = set(lowercase, value)

  def setNumPartitions(value: Int): this.type = set(numPartitions, value)

  val normalize: BooleanParam = new BooleanParam(this, "normalize", "whether to normalize pmi")

  def getNormalize: Boolean = $(normalize)

  def setNormalize(value: Boolean): this.type = set(normalize, value)

  val minCount: IntParam = new IntParam(this, "minCount", "min count of word")

  def getMinCount: Int = $(minCount)

  def setMinCount(value: Int): this.type = set(minCount, value)

  val window: IntParam = new IntParam(this, "window", "max window to count pair words")

  def getWindow: Int = $(window)

  def setWindow(value: Int): this.type = set(window, value)

  val centerWords: StringArrayParam = new StringArrayParam(this, "centerWords", "center words to reduce compute cost.")

  def getCenterWords: Array[String] = $(centerWords)

  def setCenterWords(value: Array[String]): this.type = set(centerWords, value)

  val considerWords: StringArrayParam = new StringArrayParam(this, "considerWords", "filter unconsidered words.")

  def getConsiderWords: Array[String] = $(considerWords)

  def setConsiderWords(value: Array[String]): this.type = set(considerWords, value)

  val minPMI: DoubleParam = new DoubleParam(this, "minPMI", "min pmi", ParamValidators.gtEq(0))

  def getMinPMI: Double = $(minPMI)

  def setMinPMI(value: Double): this.type = set(minPMI, value)

  setDefault(normalize -> false, window -> 0, lowercase -> true, minPMI -> 0.0, minCount -> 1)

  protected def validateAndTransformSchema(schema: StructType): StructType = {
    SchemaUtils.checkColumnType(schema, $(inputCol), ArrayType(StringType))
    StructType(Seq(StructField("word1", StringType),
      StructField("word2", StringType),
      StructField("pmi", DoubleType)))
  }
}

class PMIStat(override val uid: String) extends Model[PMIStat] with PMIStatParams with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("pmi"))

  def makeKey(text1: String, text2: String): (String, String) = {
    if (text1.compareTo(text2) < 0) (text1, text2)
    else (text2, text1)
  }

  private var broadcastDict: Option[Broadcast[(Set[String], Set[String])]] = None

  override def transform(dataset: Dataset[_]): DataFrame = {
    transformSchema(dataset.schema, logging = true)
    if (broadcastDict.isEmpty) {
      val centerWordSet = if (isSet(centerWords)) $(centerWords).toSet else Set.empty[String]
      val considerWordSet = if (isSet(considerWords)) $(considerWords).toSet else Set.empty[String]
      val dict = (centerWordSet, considerWordSet)
      broadcastDict = Some(dataset.sparkSession.sparkContext.broadcast(dict))
    }
    val dictBr = broadcastDict.get
    var counter = dataset.filter(col($(inputCol)).isNotNull).select($(inputCol)).rdd.map(row => {
      val tokens = row.getAs[Seq[String]](0)
      tokens.map(it => it.trim)
        .filter(it => !it.isEmpty)
      if ($(lowercase))
        tokens.map(it => it.toLowerCase()).distinct
      else
        tokens.distinct
    }).map(it => {
      val considerWordSet = dictBr.value._2
      if (considerWordSet.isEmpty)
        it
      else
        it.filter(considerWordSet.contains)
    }).filter(it => it.length > 1)
      .flatMap(tokens => {
        val centerWordSet = dictBr.value._1
        var pairs = new ArrayBuffer[(String, String)]
        pairs ++= tokens.map((_, ""))
        for (i <- tokens.indices) {
          val wd1 = tokens(i)
          val right = if ($(window) > 0)
            Math.min(tokens.length, i + $(window) + 1)
          else
            tokens.length
          for (j <- (i + 1).until(right)) {
            val wd2 = tokens(j)
            if (centerWordSet.isEmpty || centerWordSet.contains(wd1) || centerWordSet.contains(wd2)) {
              val coWd = makeKey(wd1, wd2)
              pairs += coWd
            }
          }
        }
        pairs.map(it => (it, 1L))
      })
      .reduceByKey(_ + _)
      .map(it => (it._1._1, it._1._2, it._2))

    if (isSet(numPartitions)) {
      counter = counter.repartition($(numPartitions))
    }

    counter.persist(StorageLevel.MEMORY_AND_DISK)

    val count = counter.map(_._3).sum
    import dataset.sparkSession.implicits._

    val minCount = getMinCount
    val singleDf = counter.filter(it => it._1.nonEmpty && it._2.isEmpty && it._3 >= minCount)
      .map(it => (it._1, it._3))
      .toDF("s_wd", "s_cnt")

    val pairDf = counter.filter(it => it._1.nonEmpty && it._2.nonEmpty)
      .toDF("p_wd1", "p_wd2", "p_cnt")

    pairDf.createOrReplaceTempView("t1")
    singleDf.createOrReplaceTempView("t2")

    val spark = dataset.sparkSession

    spark.sql("select t1.*, t2.s_cnt as s_cnt1 from t1 join t2 on t1.p_wd1 = t2.s_wd")
      .createOrReplaceTempView("t1")

    val joinDf = spark.sql("select t1.*, t2.s_cnt as s_cnt2 from t1 join t2 on t1.p_wd2 = t2.s_wd")

    val resultRdd = joinDf.toDF().rdd
      .map {
        case Row(wd1: String, wd2: String, cnt: Long, cnt1: Long, cnt2: Long) =>
          var pmi = Math.log(count * cnt * 1.0 / (cnt1 * cnt2))
          if ($(normalize))
            pmi /= (-Math.log(cnt * 1.0 / count))
          (wd1, wd2, pmi)
      }.filter(_._3 >= $(minPMI))
      .map(it => Row.fromTuple(it))
    dataset.sparkSession.createDataFrame(resultRdd, transformSchema(dataset.schema))
  }

  override def copy(extra: ParamMap): PMIStat = defaultCopy(extra)

  override def transformSchema(schema: StructType): StructType = {
    validateAndTransformSchema(schema)
  }

}

object PMIStat extends DefaultParamsReadable[PMIStat] {
  override def load(path: String): PMIStat = super.load(path)
}
