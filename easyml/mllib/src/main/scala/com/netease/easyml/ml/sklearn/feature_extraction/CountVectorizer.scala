package com.netease.easyml.ml.sklearn.feature_extraction

import com.netease.easyml.annotation.Register
import com.netease.easyml.common.util.ConvertUtil
import com.netease.easyml.ml.sklearn.{DefaultSklearnReader, SklearnReader}
import com.netease.easyml.ml.util.{DefaultEasyMLParamsReader, DefaultEasyMLParamsWriter}
import net.razorvine.pickle.objects.ClassDict
import numpy.core.Scalar
import org.apache.hadoop.fs.Path
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.ml.param._
import org.apache.spark.ml.util._
import org.apache.spark.ml.{Estimator, Model}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.functions.{col, udf}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, Dataset}
import org.apache.spark.util.collection_.OpenHashMap

import scala.collection.JavaConversions._

/**
 * Created by linjiuning on 2020/8/4.
 */
trait CountVectorizerParams extends VectorizerParams {

  val maxFeatures: IntParam =
    new IntParam(this, "maxFeatures", "If not None, build a vocabulary that only consider the top max_features ordered by term frequency across the corpus.", ParamValidators.gtEq(0))

  def getMaxFeatures: Int = $(maxFeatures)

  val minTF: DoubleParam = new DoubleParam(this, "minTF", "Filter to ignore rare words in" +
    " a document. For each document, terms with frequency/count less than the given threshold are" +
    " ignored. If this is an integer >= 1, then this specifies a count (of times the term must" +
    " appear in the document); if this is a double in [0,1), then this specifies a fraction (out" +
    " of the document's token count). Note that the parameter is only used in transform of" +
    " CountVectorizerModel and does not affect fitting.", ParamValidators.gtEq(0.0))

  /** @group getParam */
  def getMinTF: Double = $(minTF)

  val maxDf: DoubleParam = new DoubleParam(this, "maxDf", "When building the vocabulary ignore terms that have a document " +
    "frequency strictly higher than the given threshold (corpus-specific stop words). " +
    "If float, the parameter represents a proportion of documents, integer absolute counts. " +
    "This parameter is ignored if vocabulary is not None.",
    ParamValidators.gtEq(0.0))

  def getMaxDf: Double = $(maxDf)

  val minDf: DoubleParam = new DoubleParam(this, "minDf", "Specifies the minimum number of" +
    " different documents a term must appear in to be included in the vocabulary." +
    " If this is an integer >= 1, this specifies the number of documents the term must" +
    " appear in; if this is a double in [0,1), then this specifies the fraction of documents.",
    ParamValidators.gtEq(0.0))

  /** @group getParam */
  def getMinDf: Double = $(minDf)

  setDefault(maxFeatures -> (1 << 18), minTF -> 1.0, maxDf -> Double.MaxValue, minDf -> 1.0)
}

@Register(prefix = "sklearn.")
class CountVectorizer(override val uid: String)
  extends Estimator[CountVectorizerModel] with CountVectorizerParams with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("cntVec"))

  def setInputCol(value: String): this.type = set(inputCol, value)

  def setOutputCol(value: String): this.type = set(outputCol, value)

  def setMaxFeatures(value: Int): this.type = set(maxFeatures, value)

  def setMinDF(value: Double): this.type = set(minDf, value)

  def setMaxDF(value: Double): this.type = set(maxDf, value)

  def setMinTF(value: Double): this.type = set(minTF, value)

  def setBinary(value: Boolean): this.type = set(binary, value)

  def setNgramRange(value: Array[Int]): this.type = set(ngramRange, value)

  def setLowercase(value: Boolean): this.type = set(lowercase, value)

  def setTokenPattern(value: String): this.type = set(tokenPattern, value)

  def setAnalyzer(value: String): this.type = set(analyzer, value)

  override def fit(dataset: Dataset[_]): CountVectorizerModel = {
    transformSchema(dataset.schema, logging = true)
    val vocSize = getMaxFeatures
    val analyzer = getAnalyzer
    val Array(minN, maxN) = getNgramRange
    val lowercase = getLowercase
    //TODO: java pattern java.lang.StackOverflowError
    val tokenize = if ($(tokenPattern).nonEmpty) {
      Some(TextUtil.buildTokenizer($(tokenPattern)))
    } else {
      None
    }
    val input = dataset.select($(inputCol)).rdd.map(row => {
      val text = row.getString(0)
      TextUtil.analyze(text, analyzer, minN, maxN, lowercase, None, tokenize)
    })
    val minDf_ = if ($(minDf) >= 1.0) {
      $(minDf)
    } else {
      $(minDf) * input.cache().count()
    }

    val maxDf_ = if ($(maxDf) >= 1.0) {
      $(maxDf)
    } else {
      $(maxDf) * input.cache().count()
    }

    val wordCounts: RDD[(String, Long)] = input.flatMap { tokens =>
      val wc = new OpenHashMap[String, Long]
      tokens.foreach { w =>
        wc.changeValue(w, 1L, _ + 1L)
      }
      wc.map { case (word, count) => (word, (count, 1)) }
    }.reduceByKey { case ((wc1, df1), (wc2, df2)) =>
      (wc1 + wc2, df1 + df2)
    }.filter { case (word, (wc, df)) =>
      df >= minDf_ && df <= maxDf_
    }.map { case (word, (count, dfCount)) =>
      (word, count)
    }.cache()
    val fullVocabSize = wordCounts.count()

    val vocab = wordCounts
      .top(math.min(fullVocabSize, vocSize).toInt)(Ordering.by(_._2))
      .map(_._1)

    require(vocab.length > 0, "The vocabulary size should be > 0. Lower minDF as necessary.")
    copyValues(new CountVectorizerModel(uid, vocab).setParent(this))
  }

  override def transformSchema(schema: StructType): StructType = {
    validateAndTransformSchema(schema)
  }

  override def copy(extra: ParamMap): CountVectorizer = defaultCopy(extra)
}

object CountVectorizer extends DefaultParamsReadable[CountVectorizer] {

  override def load(path: String): CountVectorizer = super.load(path)
}

@Register(prefix = "sklearn.")
class CountVectorizerModel(override val uid: String,
                           val vocabulary: Array[String])
  extends Model[CountVectorizerModel] with CountVectorizerParams with MLWritable {

  import CountVectorizerModel._

  def this(uid: String) = {
    this(uid, null)
  }

  def this(vocabulary: Array[String]) = {
    this(Identifiable.randomUID("cntVecModel"), vocabulary)
    set(maxFeatures, vocabulary.length)
  }

  def setInputCol(value: String): this.type = set(inputCol, value)

  def setOutputCol(value: String): this.type = set(outputCol, value)

  def setMinTF(value: Double): this.type = set(minTF, value)

  def setBinary(value: Boolean): this.type = set(binary, value)

  def setNgramRange(value: Array[Int]): this.type = set(ngramRange, value)

  def setLowercase(value: Boolean): this.type = set(lowercase, value)

  def setTokenPattern(value: String): this.type = set(tokenPattern, value)

  def setAnalyzer(value: String): this.type = set(analyzer, value)

  /** Dictionary created from [[vocabulary]] and its indices, broadcast once for [[transform()]] */
  private var broadcastDict: Option[Broadcast[Map[String, Int]]] = None
  private lazy val tokenize = if ($(tokenPattern).nonEmpty) {
    Some(TextUtil.buildTokenizer($(tokenPattern)))
  } else {
    None
  }

  def analyzeDoc(text: String): Seq[String] = {
    val Array(minN, maxN) = $(ngramRange)
    TextUtil.analyze(text, $(analyzer), minN, maxN, $(lowercase), None, tokenize)
  }

  override def transform(dataset: Dataset[_]): DataFrame = {
    transformSchema(dataset.schema, logging = true)
    if (broadcastDict.isEmpty) {
      val dict = vocabulary.zipWithIndex.toMap
      broadcastDict = Some(dataset.sparkSession.sparkContext.broadcast(dict))
    }
    val dictBr = broadcastDict.get
    val minTf = $(minTF)
    val vectorizer = udf { (text: String) =>
      val document = analyzeDoc(text)
      val termCounts = new OpenHashMap[Int, Double]
      var tokenCount = 0L
      document.foreach { term =>
        dictBr.value.get(term) match {
          case Some(index) => termCounts.changeValue(index, 1.0, _ + 1.0)
          case None => // ignore terms not in the vocabulary
        }
        tokenCount += 1
      }
      val effectiveMinTF = if (minTf >= 1.0) minTf else tokenCount * minTf
      val effectiveCounts = if ($(binary)) {
        termCounts.filter(_._2 >= effectiveMinTF).map(p => (p._1, 1.0)).toSeq
      } else {
        termCounts.filter(_._2 >= effectiveMinTF).toSeq
      }

      Vectors.sparse(dictBr.value.size, effectiveCounts)
    }
    dataset.withColumn($(outputCol), vectorizer(col($(inputCol))))
  }

  override def transformSchema(schema: StructType): StructType = {
    validateAndTransformSchema(schema)
  }

  override def copy(extra: ParamMap): CountVectorizerModel = {
    val copied = new CountVectorizerModel(uid, vocabulary).setParent(parent)
    copyValues(copied, extra)
  }

  override def write: MLWriter = new CountVectorizerModelWriter(this)
}


object CountVectorizerModel extends MLReadable[CountVectorizerModel] with SklearnReader[CountVectorizerModel] {

  private class CountVectorizerModelWriter(instance: CountVectorizerModel) extends MLWriter {

    private case class Data(vocabulary: Seq[String])

    override protected def saveImpl(path: String): Unit = {
      DefaultEasyMLParamsWriter.saveMetadata(instance, path, sc)
      val data = Data(instance.vocabulary)
      val dataPath = new Path(path, "data").toString
      sparkSession.createDataFrame(Seq(data)).repartition(1).write.parquet(dataPath)
    }
  }

  private class CountVectorizerModelReader extends MLReader[CountVectorizerModel] {

    private val className = classOf[CountVectorizerModel].getName

    override def load(path: String): CountVectorizerModel = {
      val metadata = DefaultEasyMLParamsReader.loadMetadata(path, sc, className)
      val dataPath = new Path(path, "data").toString
      val data = sparkSession.read.parquet(dataPath)
        .select("vocabulary")
        .head()
      val vocabulary = data.getAs[Seq[String]](0).toArray
      val model = new CountVectorizerModel(metadata.uid, vocabulary)
      DefaultEasyMLParamsReader.getAndSetParams(model, metadata)
      model
    }
  }

  override def read: MLReader[CountVectorizerModel] = new CountVectorizerModelReader

  override def load(path: String): CountVectorizerModel = super.load(path)

  /**
   * Returns an Spark ML instance for this class.
   */
  override def readPickle(pickle: ClassDict): CountVectorizerModel = {
    val vocabulary = pickle.get("vocabulary_").asInstanceOf[java.util.Map[String, Any]]
    val array = Array.fill[String](vocabulary.size)("")
    vocabulary.foreach {
      case (key, value) =>
        val idx = value match {
          case scalar: Scalar =>
            scalar.getContent.get(0).asInstanceOf[Long].toInt
          case any: Any =>
            ConvertUtil.toInt(any)
        }
        array(idx) = key
    }
    val model = new CountVectorizerModel(array)
    DefaultSklearnReader.getAndSetValues(model, pickle, Some(List("vocabulary", "max_features")))
    model
  }
}
