package com.netease.easyml.ml.util

import com.netease.easyml.common.util.SparkUtil
import org.apache.spark.SparkContext
import org.apache.spark.ml.attribute.NominalAttribute
import org.apache.spark.ml.classification._
import org.apache.spark.ml.clustering.{GaussianMixtureModel, KMeansModel}
import org.apache.spark.ml.feature._
import org.apache.spark.ml.linalg.{DenseMatrix, DenseVector, Matrix, Vector}
import org.apache.spark.ml.param.{Param, Params}
import org.apache.spark.ml.stat.distribution.MultivariateGaussian
import org.apache.spark.ml.util.Identifiable
import org.apache.spark.ml.{PipelineModel, Transformer}
import org.apache.spark.mllib.clustering.{KMeansModel => MLlibKMeansModel}
import org.apache.spark.mllib.feature.{Word2VecModel => OldWord2VecModel}
import org.apache.spark.mllib.linalg.{Vectors => OldVectors}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.functions.udf
import org.apache.spark.sql.types.{DataType, Metadata}
import org.apache.spark.sql.{Dataset, SparkSession}
import org.apache.spark.util.random.RandomSampler

import java.util.Random
import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag

// based on org.apache.spark.util copy /paste
object Utils {
  val random = new Random()

  def getSparkSession(sc: SparkContext): SparkSession = {
    val builder = SparkSession.builder()
    val method = builder.getClass.getDeclaredMethod("sparkContext", classOf[SparkContext])
    method.setAccessible(true)
    method.invoke(builder, sc).asInstanceOf[SparkSession.Builder].getOrCreate()
  }

  /* Calculates 'x' modulo 'mod', takes to consideration sign of x,
  * i.e. if 'x' is negative, than 'x' % 'mod' is negative too
  * so function return (x % mod) + mod in that case.
  */
  def nonNegativeMod(x: Int, mod: Int): Int = {
    val rawMod = x % mod
    rawMod + (if (rawMod < 0) mod else 0)
  }

  def set[T <: Params](to: T, param: String, value: Any): T = {
    val method = classOf[Params].getDeclaredMethod("set", classOf[String], classOf[Any])
    method.setAccessible(true)
    method.invoke(to, param, value.asInstanceOf[Object])
    to
  }

  def flatten(dataset: Dataset[_], column: String, dataType: DataType,
              namePrefix: Option[String] = None, size: Option[Int] = None): (Array[String], Dataset[_]) = {
    if (!SchemaUtils.isArrayType(dataset.schema, column) && !SchemaUtils.isVectorType(dataset.schema, column)) {
      (Array(), dataset)
    } else {
      val prefix = namePrefix.getOrElse("_" + column)
      val size_ = size.getOrElse({
        dataset.select(column).first().get(0) match {
          case seq: Seq[_] => seq.length
          case vector: Vector => vector.size
        }
      })
      var newDs = dataset
      val cols = new ArrayBuffer[String]
      for (i <- 0 until size_) {
        val transformUDF = udf((value: Any) =>
          value match {
            case vector: Vector =>
              vector(i)
            case seq: Seq[_] =>
              seq(i)
          }, dataType)
        val col = s"${prefix}_$i"
        cols += col
        newDs = newDs.withColumn(col, transformUDF(newDs(column)))
      }
      (cols.toArray, newDs)
    }
  }

  def newPartitionwiseSampledRDD[T: ClassTag, U: ClassTag](prev: RDD[T],
                                                           sampler: RandomSampler[T, U],
                                                           preservesPartitioning: Boolean,
                                                           seed: Long = random.nextLong)(implicit tagT: ClassTag[T], tagU: ClassTag[U]): RDD[U] = {
    val clazz = SparkUtil.classForName("org.apache.spark.rdd.PartitionwiseSampledRDD")
    val constructor = clazz.getConstructors()(0)
    constructor.newInstance(prev, sampler, preservesPartitioning.asInstanceOf[Object], seed.asInstanceOf[Object], tagT, tagU).asInstanceOf[RDD[U]]
  }

  def newPipeLineModel(stages: Array[Transformer]): PipelineModel = {
    val uid = Identifiable.randomUID("pipeline")
    newPipeLineModel(uid, stages)
  }

  def newPipeLineModel(uid: String, stages: Array[Transformer]): PipelineModel = {
    val constructor = classOf[PipelineModel].getConstructor(classOf[String], classOf[Array[Transformer]])
    constructor.newInstance(uid, stages)
  }

  def newLogisticRegressionModel(coefficientMatrix: Matrix, interceptVector: Vector,
                                 numClasses: Int, isMultinomial: Boolean): LogisticRegressionModel = {
    val uid = Identifiable.randomUID("logreg")
    newLogisticRegressionModel(uid, coefficientMatrix, interceptVector, numClasses, isMultinomial)
  }

  def newLogisticRegressionModel(uid: String, coefficientMatrix: Matrix, interceptVector: Vector,
                                 numClasses: Int, isMultinomial: Boolean): LogisticRegressionModel = {
    val constructor = classOf[LogisticRegressionModel]
      .getConstructor(classOf[String], classOf[Matrix], classOf[Vector], classOf[Int], classOf[Boolean])
    constructor.newInstance(uid, coefficientMatrix, interceptVector, numClasses.asInstanceOf[Object], isMultinomial.asInstanceOf[Object])
  }

  def newMaxAbsScalerModel(maxAbs: Vector): MaxAbsScalerModel = {
    val uid = Identifiable.randomUID("maxAbsScal")
    newMaxAbsScalerModel(uid, maxAbs)
  }

  def newMaxAbsScalerModel(uid: String, maxAbs: Vector): MaxAbsScalerModel = {
    val constructor = classOf[MaxAbsScalerModel].getConstructor(classOf[String], classOf[Vector])
    constructor.newInstance(uid, maxAbs)
  }

  def newMinMaxScalerModel(min: Vector, max: Vector): MinMaxScalerModel = {
    val uid = Identifiable.randomUID("minMaxScal")
    newMinMaxScalerModel(uid, min, max)
  }

  def newMinMaxScalerModel(uid: String, min: Vector, max: Vector): MinMaxScalerModel = {
    val constructor = classOf[MinMaxScalerModel].getConstructor(classOf[String], classOf[Vector], classOf[Vector])
    constructor.newInstance(uid, min, max)
  }

  def newStandardScalerModel(mean: Vector, std: Vector): StandardScalerModel = {
    val uid = Identifiable.randomUID("stdScal")
    newStandardScalerModel(uid, mean, std)
  }

  def newStandardScalerModel(uid: String, mean: Vector, std: Vector): StandardScalerModel = {
    val constructor = classOf[StandardScalerModel].getConstructor(classOf[String], classOf[Vector], classOf[Vector])
    constructor.newInstance(uid, std, mean)
  }

  def newPCAModel(pc: DenseMatrix, explainedVariance: DenseVector): PCAModel = {
    val uid = Identifiable.randomUID("pca")
    newPCAModel(uid, pc, explainedVariance)
  }

  def newPCAModel(uid: String, pc: DenseMatrix, explainedVariance: DenseVector): PCAModel = {
    val constructor = classOf[PCAModel].getConstructor(classOf[String], classOf[DenseMatrix], classOf[DenseVector])
    constructor.newInstance(uid, pc, explainedVariance)
  }

  def newKMeansModel(clusterCenters: Array[Vector]): KMeansModel = {
    val uid = Identifiable.randomUID("kmeans")
    newKMeansModel(uid, clusterCenters)
  }

  def newKMeansModel(uid: String, clusterCenters: Array[Vector]): KMeansModel = {
    val constructor = classOf[KMeansModel].getConstructor(classOf[String], classOf[MLlibKMeansModel])
    val oldClusterCenters = clusterCenters.map(it => OldVectors.fromML(it))
    constructor.newInstance(uid, new MLlibKMeansModel(oldClusterCenters))
  }

  def newGaussianMixtureModel(weights: Array[Double], gaussians: Array[MultivariateGaussian]): GaussianMixtureModel = {
    val uid = Identifiable.randomUID("GaussianMixture")
    newGaussianMixtureModel(uid, weights, gaussians)
  }

  def newGaussianMixtureModel(uid: String, weights: Array[Double], gaussians: Array[MultivariateGaussian]): GaussianMixtureModel = {
    val constructor = classOf[GaussianMixtureModel].getConstructor(classOf[String], classOf[Array[Double]], classOf[Array[MultivariateGaussian]])
    constructor.newInstance(uid, weights, gaussians)
  }

  def newLinearSVC(coefficients: Vector, intercept: Double): LinearSVCModel = {
    val uid = Identifiable.randomUID("LinearSVC")
    newLinearSVC(uid, coefficients, intercept)
  }

  def newLinearSVC(uid: String, coefficients: Vector, intercept: Double): LinearSVCModel = {
    val constructor = classOf[LinearSVCModel].getConstructor(classOf[String], classOf[Vector], classOf[Double])
    constructor.newInstance(uid, coefficients, intercept.asInstanceOf[Object])
  }

  def newOneVsRestModel(models: Array[_ <: ClassificationModel[_, _]]): OneVsRestModel = {
    val uid = Identifiable.randomUID("OneVsRest")
    newOneVsRestModel(uid, models)
  }

  def newOneVsRestModel(uid: String, models: Array[_ <: ClassificationModel[_, _]]): OneVsRestModel = {
    val constructor = classOf[OneVsRestModel].getConstructor(classOf[String], classOf[Metadata], classOf[Array[_ <: ClassificationModel[_, _]]])
    val metadata = NominalAttribute.defaultAttr.withName("label").withNumValues(models.length).toMetadata()
    val model = constructor.newInstance(uid, metadata, models)
    val classifier = classOf[OneVsRestModel].getDeclaredField("classifier")
    classifier.setAccessible(true)
    model.set(classifier.get(model).asInstanceOf[Param[Any]], models(0).parent)
    model
  }

  def newNaiveBayesModel(modelType: String, pi: Vector, theta: Matrix): NaiveBayesModel = {
    val uid = Identifiable.randomUID("naivebayes")
    newNaiveBayesModel(uid, modelType, pi, theta)
  }

  def newNaiveBayesModel(uid: String, modelType: String, pi: Vector, theta: Matrix): NaiveBayesModel = {
    val constructor = classOf[NaiveBayesModel].getConstructor(classOf[String], classOf[Vector], classOf[Matrix])
    val model = constructor.newInstance(uid, pi, theta)
    val field = classOf[NaiveBayesModel].getDeclaredField("modelType")
    field.setAccessible(true)
    model.set(field.get(model).asInstanceOf[Param[Any]], modelType)
    model
  }

  def newWord2VecModel(wordVectors: OldWord2VecModel): Word2VecModel = {
    val uid = Identifiable.randomUID("w2v")
    newWord2VecModel(uid, wordVectors)
  }

  def newWord2VecModel(uid: String, wordVectors: OldWord2VecModel): Word2VecModel = {
    val constructor = classOf[Word2VecModel].getConstructor(classOf[String], classOf[OldWord2VecModel])
    constructor.newInstance(uid, wordVectors)
  }

  def newXORShiftRandom(seed: Long): Random = {
    val constructor = SparkUtil.classForName("org.apache.spark.util.random.XORShiftRandom").getConstructor(classOf[Long])
    constructor.newInstance(seed.asInstanceOf[Object]).asInstanceOf[Random]
  }
}