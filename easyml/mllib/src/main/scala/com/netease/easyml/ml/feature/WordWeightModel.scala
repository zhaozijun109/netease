package com.netease.easyml.ml.feature

/**
 * Created by linjiuning on 2020/8/21.
 */

import java.util
import java.util.function.Supplier

import com.hankcs.hanlp.corpus.tag.Nature
import com.hankcs.hanlp.dictionary.CoreDictionary
import com.hankcs.hanlp.seg.Other.AhoCorasickDoubleArrayTrieSegment
import com.hankcs.hanlp.seg.Segment
import com.netease.easyml.common.resource.ResourceManager
import com.netease.easyml.common.util.IOUtil
import com.netease.easyml.ml.util._
import org.apache.hadoop.fs.Path
import org.apache.spark.ml.Model
import org.apache.spark.ml.param._
import org.apache.spark.ml.param.shared.{HasInputCol, HasOutputCol}
import org.apache.spark.ml.util.{SchemaUtils => _, _}
import org.apache.spark.sql.functions.{col, udf}
import org.apache.spark.sql.types.{DoubleType, StringType, StructType}
import org.apache.spark.sql.{DataFrame, Dataset}

import scala.collection.JavaConverters._


trait WordWeightParams extends Params with HasInputCol with HasOutputCol {
  val STRATEGY_MEAN: String = "mean"
  val STRATEGY_MIN: String = "min"
  val STRATEGY_MAX: String = "max"
  val STRATEGY_MEDIAN: String = "median"

  val segment = new BooleanParam(this, "segment", "whether do segment for oov.")

  def getSegment: Boolean = $(segment)

  val strategy: Param[String] = new Param[String](this, "strategy", "reduce strategy if segment is enabled.",
    ParamValidators.inArray(Array(STRATEGY_MEAN, STRATEGY_MIN, STRATEGY_MAX, STRATEGY_MEDIAN))
  )

  def getStrategy: String = $(strategy)

  protected def validateAndTransformSchema(schema: StructType): StructType = {
    SchemaUtils.checkColumnType(schema, $(inputCol), StringType)
    SchemaUtils.appendColumn(schema, $(outputCol), DoubleType)
  }

  setDefault(segment -> false, strategy -> STRATEGY_MEAN)
}

class WordWeightModel(override val uid: String, val wordWeights: Map[String, Float])
  extends Model[WordWeightModel] with WordWeightParams with MLWritable {

  import WordWeightModel._

  def this(wordWeights: Map[String, Float]) = {
    this(Identifiable.randomUID("w2w"), wordWeights)
  }

  def setSegment(value: Boolean): this.type = set(segment, value)

  def setStrategy(value: String): this.type = set(strategy, value)

  def setInputCol(value: String): this.type = set(inputCol, value)

  def setOutputCol(value: String): this.type = set(outputCol, value)

  override def write: MLWriter = new WordWeightModelWriter(this)

  lazy val vocabs: Set[String] = wordWeights.keySet

  def transformOne(word: String): Double = {
    if (wordWeights.contains(word)) {
      wordWeights(word)
    } else if ($(segment)) {
      val segment = getOrCreate(uid, vocabs)
      val weights = segment.seg(word).asScala.map(_.word)
        .filter(wordWeights.contains).map(token => wordWeights(token))
      if (weights.isEmpty) {
        0.0
      } else {
        $(strategy) match {
          case STRATEGY_MAX =>
            weights.max
          case STRATEGY_MIN =>
            weights.min
          case STRATEGY_MEAN =>
            weights.sum / weights.size
          case STRATEGY_MEDIAN =>
            val sorted = weights.sorted
            val mid = sorted.size / 2
            if (sorted.size % 2 == 0) 0.5 * (sorted(mid) + sorted(mid - 1))
            else sorted(mid)
        }
      }
    } else {
      0.0
    }
  }

  override def copy(extra: ParamMap): WordWeightModel = defaultCopy(extra)

  override def transform(dataset: Dataset[_]): DataFrame = {
    transformSchema(dataset.schema, logging = true)

    val transformUdf = udf(transformOne _)
    dataset.withColumn($(outputCol), transformUdf(col($(inputCol))))
  }

  override def transformSchema(schema: StructType): StructType = validateAndTransformSchema(schema)
}

object WordWeightModel extends MLReadable[WordWeightModel] {

  def getOrCreate(uid: String, words: Set[String]): Segment = {
    ResourceManager.getOrCreate(uid, new Supplier[Segment] {
      override def get(): Segment = {
        val storage = new util.TreeMap[String, CoreDictionary.Attribute]
        val nature = new CoreDictionary.Attribute(Nature.n)
        for (word <- words) {
          storage.put(word, nature)
        }
        new AhoCorasickDoubleArrayTrieSegment(storage)
      }
    })
  }

  private case class Data(wordWeights: Map[String, Float])

  private class WordWeightModelWriter(instance: WordWeightModel) extends MLWriter {
    override protected def saveImpl(path: String): Unit = {
      DefaultEasyMLParamsWriter.saveMetadata(instance, path, sc)
      val dataPath = new Path(path, "data").toString
      val data = Data(instance.wordWeights)
      sparkSession.createDataFrame(Seq(data)).repartition(1).write.parquet(dataPath)
    }
  }

  private class WordWeightModelReader extends MLReader[WordWeightModel] {

    private val className = classOf[WordWeightModel].getName

    override def load(path: String): WordWeightModel = {
      if (IOUtil.isDirectory(path)) {
        val metadata = DefaultEasyMLParamsReader.loadMetadata(path, sc, className)
        val dataPath = new Path(path, "data").toString
        val data = sparkSession.read.parquet(dataPath)
          .select("wordWeights")
          .head()
        val wordWeights = data.getMap[String, Float](0).toMap
        val model = new WordWeightModel(metadata.uid, wordWeights)
        DefaultEasyMLParamsReader.getAndSetParams(model, metadata)
        model
      } else if (IOUtil.isFile(path)) {
        val wordWeights = MLUtils.loadWordWeight(path)
        new WordWeightModel(wordWeights)
      } else {
        val wordWeights = MLUtils.loadWordWeightHive(sparkSession, path)
        new WordWeightModel(wordWeights)
      }
    }
  }

  override def read: MLReader[WordWeightModel] = new WordWeightModelReader

  override def load(path: String): WordWeightModel = super.load(path)
}