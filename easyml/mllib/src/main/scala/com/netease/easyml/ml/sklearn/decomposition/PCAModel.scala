package com.netease.easyml.ml.sklearn.decomposition

import com.netease.easyml.annotation.Register
import com.netease.easyml.ml.sklearn.{DefaultSklearnReader, SklearnReader}
import com.netease.easyml.ml.util.{DefaultEasyMLParamsReader, DefaultEasyMLParamsWriter, SchemaUtils, VectorUtils}
import net.razorvine.pickle.objects.ClassDict
import org.apache.hadoop.fs.Path
import org.apache.spark.ml.Model
import org.apache.spark.ml.linalg.{DenseMatrix, DenseVector, Matrices, SparseVector, Vector, Vectors}
import org.apache.spark.ml.param._
import org.apache.spark.ml.param.shared.{HasInputCol, HasOutputCol}
import org.apache.spark.ml.util._
import org.apache.spark.sql.functions.{col, udf}
import org.apache.spark.sql.types.{StructField, StructType}
import org.apache.spark.sql.{DataFrame, Dataset, Row}

/**
 * Created by linjiuning on 2020/8/10.
 */
trait PCAParams extends Params with HasInputCol with HasOutputCol {

  /**
   * The number of principal components.
   */
  final val k: IntParam = new IntParam(this, "k", "the number of principal components (> 0)",
    ParamValidators.gt(0))

  def getK: Int = $(k)

  final val whiten: BooleanParam = new BooleanParam(this, "whiten", "When True (False by default) the components_ vectors are multiplied by the square root of n_samples and then divided by the singular values to ensure uncorrelated outputs with unit component-wise variances." +
    "Whitening will remove some information from the transformed signal (the relative variance scales of the components) but can sometime improve the predictive accuracy of the downstream estimators by making their data respect some hard-wired assumptions.")

  def getWhiten: Boolean = $(whiten)

  /** Validates and transforms the input schema. */
  protected def validateAndTransformSchema(schema: StructType): StructType = {
    SchemaUtils.checkColumnType(schema, $(inputCol), SchemaUtils.vectorUDT)
    require(!schema.fieldNames.contains($(outputCol)),
      s"Output column ${$(outputCol)} already exists.")
    val outputFields = schema.fields :+ StructField($(outputCol), SchemaUtils.vectorUDT, false)
    StructType(outputFields)
  }

}

@Register(prefix = "sklearn")
class PCAModel(override val uid: String,
               val mean: DenseVector,
               val pc: DenseMatrix,
               val explainedVariance: DenseVector)
  extends Model[PCAModel] with PCAParams with MLWritable {

  import PCAModel._

  def this(uid: String) = {
    this(uid, null, null, null)
  }

  def setInputCol(value: String): this.type = set(inputCol, value)

  def setOutputCol(value: String): this.type = set(outputCol, value)

  def transformUdf(vector: Vector): Vector = {
    var resVector = if (mean != null) {
      VectorUtils.subtractionVectors(vector, mean)
    } else {
      vector
    }
    resVector = resVector match {
      case dv: DenseVector =>
        pc.multiply(dv)
      case SparseVector(size, indices, values) =>
        /* SparseVector -> single row SparseMatrix */
        val sm = Matrices.sparse(size, 1, Array(0, indices.length), indices, values).transpose
        val projection = sm.multiply(pc.transpose)
        Vectors.dense(projection.values)
      case _ =>
        throw new IllegalArgumentException("Unsupported vector format. Expected " +
          s"SparseVector or DenseVector. Instead got: ${vector.getClass}")
    }

    if ($(whiten)) {
      VectorUtils.divisionVectors(resVector, VectorUtils.sqrtVector(explainedVariance))
    } else {
      resVector
    }
  }

  /**
   * Transform a vector by computed Principal Components.
   *
   * @note Vectors to be transformed must be the same length as the source vectors given
   *       to `PCA.fit()`.
   */
  override def transform(dataset: Dataset[_]): DataFrame = {
    transformSchema(dataset.schema, logging = true)

    val pcaOp = udf(transformUdf _)
    dataset.withColumn($(outputCol), pcaOp(col($(inputCol))))
  }

  override def transformSchema(schema: StructType): StructType = {
    validateAndTransformSchema(schema)
  }

  override def copy(extra: ParamMap): PCAModel = {
    val copied = new PCAModel(uid, mean, pc, explainedVariance)
    copyValues(copied, extra).setParent(parent)
  }

  override def write: MLWriter = new PCAModelWriter(this)
}

object PCAModel extends MLReadable[PCAModel] with SklearnReader[PCAModel] {

  private[PCAModel] class PCAModelWriter(instance: PCAModel) extends MLWriter {

    private case class Data(mean: DenseVector, pc: DenseMatrix, explainedVariance: DenseVector)

    override protected def saveImpl(path: String): Unit = {
      DefaultEasyMLParamsWriter.saveMetadata(instance, path, sc)
      val data = Data(instance.mean, instance.pc, instance.explainedVariance)
      val dataPath = new Path(path, "data").toString
      sparkSession.createDataFrame(Seq(data)).repartition(1).write.parquet(dataPath)
    }
  }

  private class PCAModelReader extends MLReader[PCAModel] {

    private val className = classOf[PCAModel].getName

    override def load(path: String): PCAModel = {
      val metadata = DefaultEasyMLParamsReader.loadMetadata(path, sc, className)

      val dataPath = new Path(path, "data").toString
      val Row(mean: DenseVector, pc: DenseMatrix, explainedVariance: DenseVector) =
        sparkSession.read.parquet(dataPath)
          .select("mean", "pc", "explainedVariance")
          .head()
      val model = new PCAModel(metadata.uid, mean, pc, explainedVariance)
      DefaultEasyMLParamsReader.getAndSetParams(model, metadata)
      model
    }
  }

  override def read: MLReader[PCAModel] = new PCAModelReader

  override def load(path: String): PCAModel = super.load(path)

  override def readPickle(pickle: ClassDict): PCAModel = {
    val model = new PCAModel(Identifiable.randomUID("pca"))
    DefaultSklearnReader.getAndSetValues(model, pickle, mapping = Some(Map(
      "n_components_" -> "k",
      "components_" -> "pc",
      "explained_variance_" -> "explained_variance")))
    model
  }
}
