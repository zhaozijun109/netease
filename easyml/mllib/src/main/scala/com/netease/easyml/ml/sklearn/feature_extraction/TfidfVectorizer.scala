package com.netease.easyml.ml.sklearn.feature_extraction

import com.netease.easyml.ml.sklearn.{DefaultSklearnReader, SklearnReader}
import com.netease.easyml.ml.util.{DefaultEasyMLParamsReader, DefaultEasyMLParamsWriter}
import net.razorvine.pickle.objects.ClassDict
import org.apache.hadoop.fs.Path
import org.apache.spark.ml.param.{ParamMap, Params}
import org.apache.spark.ml.util._
import org.apache.spark.ml.{Estimator, Model}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, Dataset}

/**
 * Created by linjiuning on 2020/8/4.
 */

trait TfidfVectorizerParams extends Params with CountVectorizerParams with TfidfTransformerParams {
}

class TfidfVectorizer(override val uid: String) extends Estimator[TfidfVectorizerModel]
  with CountVectorizerParams with TfidfTransformerParams with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("tfidf"))

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

  def setMinDocFreq(value: Int): this.type = set(minDocFreq, value)

  def setUseIdf(value: Boolean): this.type = set(useIdf, value)

  def setSmoothIdf(value: Boolean): this.type = set(smoothIdf, value)

  def setSublinearTf(value: Boolean): this.type = set(sublinearTf, value)

  override def fit(dataset: Dataset[_]): TfidfVectorizerModel = {
    val tmpOutputCol = s"_tmp_${uid}_outputCol"
    val cntVec = new CountVectorizer()
      .setInputCol($(inputCol))
      .setOutputCol(tmpOutputCol)
      .setAnalyzer($(analyzer))
      .setBinary($(binary))
      .setLowercase($(lowercase))
      .setNgramRange($(ngramRange))
      .setMaxDF($(maxDf))
      .setMinDF($(minDf))
      .setMaxFeatures($(maxFeatures))
      .setMinTF($(minTF))
      .setTokenPattern($(tokenPattern))

    val idf = new TfidfTransformer()
      .setInputCol(tmpOutputCol)
      .setOutputCol($(outputCol))
      .setMinDocFreq($(minDocFreq))
      .setSmoothIdf($(smoothIdf))
      .setSublinearTf($(sublinearTf))
      .setUseIdf($(useIdf))

    val cntVecModel = cntVec.fit(dataset)
    val newDs = cntVecModel.transform(dataset)

    val idfModel = idf.fit(newDs)
    copyValues(new TfidfVectorizerModel(uid, cntVecModel, idfModel).setParent(this))
  }

  override def copy(extra: ParamMap): Estimator[TfidfVectorizerModel] = defaultCopy(extra)

  override def transformSchema(schema: StructType): StructType = validateAndTransformSchema(schema)
}

object TfidfVectorizer extends DefaultParamsReadable[TfidfVectorizer] {
  override def load(path: String): TfidfVectorizer = super.load(path)
}

class TfidfVectorizerModel(override val uid: String,
                           val cntVec: CountVectorizerModel,
                           val idf: TfidfTransformerModel)
  extends Model[TfidfVectorizerModel] with TfidfVectorizerParams with MLWritable {

  import TfidfVectorizerModel._

  def this(cntVec: CountVectorizerModel, idf: TfidfTransformerModel) = {
    this(Identifiable.randomUID("tfidf"), cntVec, idf)
  }

  def setInputCol(value: String): this.type = set(inputCol, value)

  def setOutputCol(value: String): this.type = set(outputCol, value)

  lazy val wordWeights: Map[String, Double] = {
    val vocabulary = cntVec.vocabulary
    val weights = idf.idf.toArray
    vocabulary.zip(weights).toMap
  }

  override def copy(extra: ParamMap): TfidfVectorizerModel = defaultCopy(extra)

  override def transform(dataset: Dataset[_]): DataFrame = {
    transformSchema(dataset.schema, logging = true)
    val tmpOutputCol = s"_tmp_${uid}_outputCol"
    cntVec.setInputCol($(inputCol))
    cntVec.setOutputCol(tmpOutputCol)

    var newDs = cntVec.transform(dataset)
    idf.setInputCol(tmpOutputCol)
    idf.setOutputCol($(outputCol))

    newDs = idf.transform(newDs)

    newDs.drop(tmpOutputCol)
  }

  override def transformSchema(schema: StructType): StructType = validateAndTransformSchema(schema)

  override def write: MLWriter = new TfidfVectorizerModelWriter(this)
}

object TfidfVectorizerModel extends MLReadable[TfidfVectorizerModel] with SklearnReader[TfidfVectorizerModel] {

  private class TfidfVectorizerModelWriter(instance: TfidfVectorizerModel) extends MLWriter {

    override protected def saveImpl(path: String): Unit = {
      DefaultEasyMLParamsWriter.saveMetadata(instance, path, sc)
      val dataPath = new Path(path, "data").toString
      val cntPath = new Path(dataPath, "cntVec").toString
      instance.cntVec.save(cntPath)
      val idfPath = new Path(dataPath, "idf").toString
      instance.idf.save(idfPath)
    }
  }

  private class TfidfVectorizerModelReader extends MLReader[TfidfVectorizerModel] {

    private val className = classOf[TfidfVectorizerModel].getName

    override def load(path: String): TfidfVectorizerModel = {
      val metadata = DefaultEasyMLParamsReader.loadMetadata(path, sc, className)
      val dataPath = new Path(path, "data").toString
      val cntPath = new Path(dataPath, "cntVec").toString
      val cntVec = CountVectorizerModel.load(cntPath)
      val idfPath = new Path(dataPath, "idf").toString
      val idf = TfidfTransformerModel.load(idfPath)
      val model = new TfidfVectorizerModel(metadata.uid, cntVec, idf)
      DefaultEasyMLParamsReader.getAndSetParams(model, metadata)
      model
    }
  }

  override def read: MLReader[TfidfVectorizerModel] = new TfidfVectorizerModelReader

  override def load(path: String): TfidfVectorizerModel = super.load(path)

  /**
   * Returns an Spark ML instance for this class.
   */
  override def readPickle(pickle: ClassDict): TfidfVectorizerModel = {
    val countVectorizerModel = CountVectorizerModel.readPickle(pickle)
    val tfidfTransformerModel = TfidfTransformerModel.readPickle(pickle.get("_tfidf").asInstanceOf[ClassDict])
    val model = new TfidfVectorizerModel(countVectorizerModel, tfidfTransformerModel)
    DefaultSklearnReader.getAndSetValues(model, pickle)
    model
  }
}