package com.netease.easyml.ml.sklearn.feature_extraction

import breeze.linalg.{DenseVector => BDV}
import com.netease.easyml.ml.param.HasNorm
import com.netease.easyml.ml.sklearn.{DefaultSklearnReader, SklearnReader}
import com.netease.easyml.ml.util.{DefaultEasyMLParamsReader, DefaultEasyMLParamsWriter, SchemaUtils, VectorUtils}
import net.razorvine.pickle.objects.ClassDict
import org.apache.hadoop.fs.Path
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.ml.linalg.{DenseVector, SparseVector, Vector, Vectors}
import org.apache.spark.ml.param._
import org.apache.spark.ml.param.shared.{HasInputCol, HasOutputCol}
import org.apache.spark.ml.util._
import org.apache.spark.ml.{Estimator, Model}
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.functions.{col, udf}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, Dataset, Row}
import scipy.sparse.CSRMatrix

import scala.collection.JavaConversions._

/**
 * Created by linjiuning on 2020/8/4.
 */
trait TfidfTransformerParams extends Params with HasInputCol with HasOutputCol with HasNorm {

  val minDocFreq = new IntParam(
    this, "minDocFreq", "minimum number of documents in which a term should appear for filtering" +
      " (>= 0)", ParamValidators.gtEq(0))

  def getMinDocFreq: Int = $(minDocFreq)

  val useIdf: BooleanParam =
    new BooleanParam(this, "useIdf", "Enable inverse-document-frequency reweighting.")

  def getUseIdf: Boolean = $(useIdf)

  val smoothIdf: BooleanParam =
    new BooleanParam(this, "smoothIdf", "Smooth idf weights by adding one to document frequencies, as if an " +
      "extra document was seen containing every term in the collection " +
      "exactly once. Prevents zero divisions.")

  def getSmoothIdf: Boolean = $(smoothIdf)

  val sublinearTf: BooleanParam =
    new BooleanParam(this, "sublinearTf", "Apply sublinear tf scaling, i.e. replace tf with 1 + log(tf).")

  def getSublinearTf: Boolean = $(sublinearTf)

  protected def validateAndTransformSchemaTfidf(schema: StructType): StructType = {
    SchemaUtils.checkColumnType(schema, $(inputCol), SchemaUtils.vectorUDT)
    SchemaUtils.appendColumn(schema, $(outputCol), SchemaUtils.vectorUDT)
  }

  setDefault(minDocFreq -> 1, norm -> "l2", useIdf -> true, smoothIdf -> true, sublinearTf -> false, outputCol -> "features")
}

class TfidfTransformer(override val uid: String)
  extends Estimator[TfidfTransformerModel] with TfidfTransformerParams with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("idf"))

  def setInputCol(value: String): this.type = set(inputCol, value)

  def setOutputCol(value: String): this.type = set(outputCol, value)

  def setMinDocFreq(value: Int): this.type = set(minDocFreq, value)

  def setUseIdf(value: Boolean): this.type = set(useIdf, value)

  def setSmoothIdf(value: Boolean): this.type = set(smoothIdf, value)

  def setSublinearTf(value: Boolean): this.type = set(sublinearTf, value)

  override def fit(dataset: Dataset[_]): TfidfTransformerModel = {
    transformSchema(dataset.schema, logging = true)
    val input: RDD[Vector] = dataset.select($(inputCol)).rdd.map {
      case Row(v: Vector) => v
    }

    val idf = input.treeAggregate(new TfidfTransformer.DocumentFrequencyAggregator(
      minDocFreq = $(minDocFreq), smoothIdf = $(smoothIdf)))(
      seqOp = (df, v) => df.add(v),
      combOp = (df1, df2) => df1.merge(df2)
    ).idf()
    copyValues(new TfidfTransformerModel(uid, idf).setParent(this))
  }

  override def transformSchema(schema: StructType): StructType = {
    validateAndTransformSchemaTfidf(schema)
  }

  override def copy(extra: ParamMap): TfidfTransformer = defaultCopy(extra)
}

object TfidfTransformer extends DefaultParamsReadable[TfidfTransformer] {

  /** Document frequency aggregator.
   * Mainly copy from spark IDF.DocumentFrequencyAggregator
   */
  class DocumentFrequencyAggregator(val minDocFreq: Int, val smoothIdf: Boolean) extends Serializable {

    /** number of documents */
    private var m = 0L
    /** document frequency vector */
    private var df: BDV[Long] = _


    def this() = this(0, true)

    /** Adds a new document. */
    def add(doc: Vector): this.type = {
      if (isEmpty) {
        df = BDV.zeros(doc.size)
      }
      doc match {
        case SparseVector(size, indices, values) =>
          val nnz = indices.length
          var k = 0
          while (k < nnz) {
            if (values(k) > 0) {
              df(indices(k)) += 1L
            }
            k += 1
          }
        case DenseVector(values) =>
          val n = values.length
          var j = 0
          while (j < n) {
            if (values(j) > 0.0) {
              df(j) += 1L
            }
            j += 1
          }
        case other =>
          throw new UnsupportedOperationException(
            s"Only sparse and dense vectors are supported but got ${other.getClass}.")
      }
      m += 1L
      this
    }

    /** Merges another. */
    def merge(other: DocumentFrequencyAggregator): this.type = {
      if (!other.isEmpty) {
        m += other.m
        if (df == null) {
          df = other.df.copy
        } else {
          df += other.df
        }
      }
      this
    }

    private def isEmpty: Boolean = m == 0L

    /** Returns the current IDF vector. */
    def idf(): Vector = {
      if (isEmpty) {
        throw new IllegalStateException("Haven't seen any document yet.")
      }
      val n = df.length
      val inv = new Array[Double](n)
      var j = 0
      while (j < n) {
        /*
         * If the term is not present in the minimum
         * number of documents, set IDF to 0. This
         * will cause multiplication in IDFModel to
         * set TF-IDF to 0.
         *
         * Since arrays are initialized to 0 by default,
         * we just omit changing those entries.
         */
        if (df(j) >= minDocFreq) {
          inv(j) = if (smoothIdf) {
            math.log((m + 1.0) / (df(j) + 1.0)) + 1
          } else {
            if (df(j) > 0) {
              math.log(m / df(j)) + 1
            } else {
              1
            }
          }
        }
        j += 1
      }
      Vectors.dense(inv)
    }
  }

  override def load(path: String): TfidfTransformer = super.load(path)
}

class TfidfTransformerModel(override val uid: String,
                            val idf: Vector)
  extends Model[TfidfTransformerModel] with TfidfTransformerParams with MLWritable {

  import TfidfTransformerModel._

  def this(uid: String) = {
    this(uid, null)
  }

  def this(idf: Vector) = {
    this(Identifiable.randomUID("tfidfModel"), idf)
  }

  def setInputCol(value: String): this.type = set(inputCol, value)

  def setOutputCol(value: String): this.type = set(outputCol, value)

  def setUseIdf(value: Boolean): this.type = set(useIdf, value)

  def setSublinearTf(value: Boolean): this.type = set(sublinearTf, value)

  private lazy val p = getNormP

  def tfidf(tf: Double, idf: Double): Double = {
    val tf_ = if ($(sublinearTf)) {
      Math.log(tf) + 1
    } else {
      tf
    }

    if ($(useIdf)) {
      tf_ * idf
    } else {
      tf_
    }
  }

  def transform(idf: Vector, v: Vector): Vector = {
    val n = v.size
    val vector = v match {
      case SparseVector(_, indices, values) =>
        val nnz = indices.length
        val newValues = new Array[Double](nnz)
        var k = 0
        while (k < nnz) {
          newValues(k) = tfidf(values(k), if ($(useIdf)) idf(indices(k)) else 0.0)
          k += 1
        }
        Vectors.sparse(n, indices, newValues)
      case DenseVector(values) =>
        val newValues = new Array[Double](n)
        var j = 0
        while (j < n) {
          newValues(j) = tfidf(values(j), if ($(useIdf)) idf(j) else 0.0)
          j += 1
        }
        Vectors.dense(newValues)
      case other =>
        throw new UnsupportedOperationException(
          s"Only sparse and dense vectors are supported but got ${other.getClass}.")
    }
    VectorUtils.normalize(vector, p)
  }

  private var broadcastIdf: Option[Broadcast[Vector]] = None

  override def transform(dataset: Dataset[_]): DataFrame = {
    transformSchema(dataset.schema, logging = true)
    if (broadcastIdf.isEmpty) {
      broadcastIdf = Some(dataset.sparkSession.sparkContext.broadcast(idf))
    }
    val idfBr = broadcastIdf.get
    val idf_ = udf { tf: Vector => transform(idfBr.value, tf) }
    dataset.withColumn($(outputCol), idf_(col($(inputCol))))
  }

  override def transformSchema(schema: StructType): StructType = {
    validateAndTransformSchemaTfidf(schema)
  }

  override def copy(extra: ParamMap): TfidfTransformerModel = {
    val copied = new TfidfTransformerModel(uid, idf).setParent(parent)
    copyValues(copied, extra)
  }


  override def write: MLWriter = new TfidfTransformerModelWriter(this)
}

object TfidfTransformerModel extends MLReadable[TfidfTransformerModel] with SklearnReader[TfidfTransformerModel] {

  private class TfidfTransformerModelWriter(instance: TfidfTransformerModel) extends MLWriter {

    private case class Data(idf: Vector)

    override protected def saveImpl(path: String): Unit = {
      DefaultEasyMLParamsWriter.saveMetadata(instance, path, sc)
      val data = Data(instance.idf)
      val dataPath = new Path(path, "data").toString
      sparkSession.createDataFrame(Seq(data)).repartition(1).write.parquet(dataPath)
    }
  }

  private class TfidfTransformerModelReader extends MLReader[TfidfTransformerModel] {

    private val className = classOf[TfidfTransformerModel].getName

    override def load(path: String): TfidfTransformerModel = {
      val metadata = DefaultEasyMLParamsReader.loadMetadata(path, sc, className)
      val dataPath = new Path(path, "data").toString
      val data = sparkSession.read.parquet(dataPath)
      val Row(idf: Vector) = MLUtils.convertVectorColumnsToML(data, "idf")
        .select("idf")
        .head()
      val model = new TfidfTransformerModel(metadata.uid, idf)
      DefaultEasyMLParamsReader.getAndSetParams(model, metadata)
      model
    }
  }

  override def read: MLReader[TfidfTransformerModel] = new TfidfTransformerModelReader

  override def load(path: String): TfidfTransformerModel = super.load(path)

  /**
   * Returns an Spark ML instance for this class.
   */
  override def readPickle(pickle: ClassDict): TfidfTransformerModel = {
    val useIdf = pickle.get("use_idf").asInstanceOf[Boolean]
    val vector = if (useIdf) {
      val csrMatrix = pickle.get("_idf_diag").asInstanceOf[CSRMatrix]
      val array = csrMatrix.getData.map(_.asInstanceOf[Double]).toArray
      Vectors.dense(array)
    } else {
      null.asInstanceOf[Vector]
    }
    val model = new TfidfTransformerModel(vector)
    DefaultSklearnReader.getAndSetValues(model, pickle)
    model
  }
}