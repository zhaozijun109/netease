package com.netease.easyml.ml.feature

import com.netease.easyml.ml.util.{DefaultEasyMLParamsReader, DefaultEasyMLParamsWriter, SchemaUtils}
import org.apache.hadoop.fs.Path
import org.apache.spark.ml.feature.{PCA, PCAModel}
import org.apache.spark.ml.linalg.{DenseMatrix, DenseVector, Vector}
import org.apache.spark.ml.linalg_.BLAS
import org.apache.spark.ml.param._
import org.apache.spark.ml.param.shared.{HasInputCol, HasOutputCol}
import org.apache.spark.ml.util._
import org.apache.spark.ml.{Estimator, Model}
import org.apache.spark.mllib.linalg.distributed.RowMatrix
import org.apache.spark.mllib.linalg.{Matrix, SingularValueDecomposition, DenseMatrix => OldDenseMatrix, SparseMatrix => OldSparseMatrix, Vector => OldVector, Vectors => OldVectors}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.functions.{col, udf}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, Dataset, Row}
import org.apache.spark.storage.StorageLevel

/**
 * Created by linjiuning on 2020/8/20.
 * A Simple but Tough-to-Beat Baseline for Sentence Embeddings
 */
trait SIFDocVecParams extends Params with HasInputCol with HasOutputCol {
  val k: IntParam = new IntParam(this, "k", "the number of principal components (> 0)",
    ParamValidators.gt(0))

  def getK: Int = $(k)

  val useSVD: BooleanParam = new BooleanParam(this, "useSVD", "if not, use pca to compute principal components")

  def getUseSVD: Boolean = $(useSVD)

  setDefault(k -> 1, useSVD -> true)

  protected def validateAndTransformSchema(schema: StructType): StructType = {
    SchemaUtils.checkVectorType(schema, $(inputCol))
    SchemaUtils.appendColumn(schema, $(outputCol), SchemaUtils.vectorUDT)
  }
}

class SIFDocVec(override val uid: String) extends Estimator[SIFDocVecModel]
  with SIFDocVecParams with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("sif_d2v"))

  def setInputCol(value: String): this.type = set(inputCol, value)

  def setOutputCol(value: String): this.type = set(outputCol, value)

  def setK(value: Int): this.type = set(k, value)

  def setUseSVD(value: Boolean): this.type = set(useSVD, value)

  override def fit(dataset: Dataset[_]): SIFDocVecModel = {
    validateAndTransformSchema(dataset.schema)

    val pc = if ($(useSVD)) {
      val input: RDD[OldVector] = dataset.select($(inputCol)).rdd.map {
        case Row(v: Vector) => OldVectors.fromML(v)
      }
      input.persist(StorageLevel.MEMORY_AND_DISK)
      val mat = new RowMatrix(input)
      val svd: SingularValueDecomposition[RowMatrix, Matrix] = mat.computeSVD($(k))

      val pc = svd.V match {
        case dm: OldDenseMatrix =>
          dm.asML
        case sm: OldSparseMatrix =>
          sm.toDense.asML
        case m =>
          throw new IllegalArgumentException("Unsupported matrix format. Expected " +
            s"SparseMatrix or DenseMatrix. Instead got: ${m.getClass}")
      }
      input.unpersist()
      pc
    } else {
      // WARN: must call StandardScaler(withMean = true, withStd = false) before pca
      val pca = new PCA()
        .setInputCol($(inputCol))
        .setK($(k))
        .fit(dataset)
      val pcField = classOf[PCAModel].getDeclaredField("pc")
      pcField.setAccessible(true)
      pcField.get(pca).asInstanceOf[DenseMatrix]
    }

    copyValues(new SIFDocVecModel(uid, pc).setParent(this))
  }

  override def copy(extra: ParamMap): Estimator[SIFDocVecModel] = defaultCopy(extra)

  override def transformSchema(schema: StructType): StructType = {
    validateAndTransformSchema(schema)
  }
}

object SIFDocVec extends DefaultParamsReadable[SIFDocVec] {
  override def load(path: String): SIFDocVec = super.load(path)
}

class SIFDocVecModel(override val uid: String,
                     val pc: DenseMatrix)
  extends Model[SIFDocVecModel] with SIFDocVecParams with MLWritable {

  import SIFDocVecModel._

  def transformOne(vector: Vector): Vector = {
    val weights = new DenseVector(new Array[Double](pc.numCols))
    BLAS.gemv(1.0, pc.transpose, vector, 0.0, weights)
    val sub = new DenseVector(new Array[Double](vector.size))
    BLAS.gemv(1.0, pc, weights, 0.0, sub)
    BLAS.axpy(-1, sub, vector)
    vector
  }

  override def transform(dataset: Dataset[_]): DataFrame = {
    transformSchema(dataset.schema, logging = true)
    val sifOp = udf(transformOne _)
    dataset.withColumn($(outputCol), sifOp(col($(inputCol))))
  }

  override def transformSchema(schema: StructType): StructType = {
    validateAndTransformSchema(schema)
  }

  override def copy(extra: ParamMap): SIFDocVecModel = defaultCopy(extra)

  override def write: MLWriter = new SIFDoc2VecModelWriter(this)
}

object SIFDocVecModel extends MLReadable[SIFDocVecModel] {

  private case class Data(pc: DenseMatrix)

  private class SIFDoc2VecModelWriter(instance: SIFDocVecModel) extends MLWriter {
    override protected def saveImpl(path: String): Unit = {
      DefaultEasyMLParamsWriter.saveMetadata(instance, path, sc)
      val dataPath = new Path(path, "data").toString
      val data = Data(instance.pc)
      sparkSession.createDataFrame(Seq(data)).repartition(1).write.parquet(dataPath)
    }
  }

  private class SIFDoc2VecModelReader extends MLReader[SIFDocVecModel] {

    private val className = classOf[SIFDocVecModel].getName

    override def load(path: String): SIFDocVecModel = {
      val metadata = DefaultEasyMLParamsReader.loadMetadata(path, sc, className)
      val dataPath = new Path(path, "data").toString
      val Row(pc: DenseMatrix) =
        sparkSession.read.parquet(dataPath)
          .select("pc")
          .head()
      val model = new SIFDocVecModel(metadata.uid, pc)
      DefaultEasyMLParamsReader.getAndSetParams(model, metadata)
      model
    }
  }

  override def read: MLReader[SIFDocVecModel] = new SIFDoc2VecModelReader

  override def load(path: String): SIFDocVecModel = super.load(path)

}
