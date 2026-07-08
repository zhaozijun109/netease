package com.netease.easyml.ml.sklearn.feature_selection

import com.netease.easyml.ml.feature.VectorizerModel
import com.netease.easyml.ml.sklearn.feature_extraction.CountVectorizerModel
import com.netease.easyml.ml.sklearn.preprocessing.OneHotEncoderModel
import com.netease.easyml.ml.util.{DefaultEasyMLParamsReader, DefaultEasyMLParamsWriter, SchemaUtils}
import org.apache.hadoop.fs.Path
import org.apache.spark.SparkException
import org.apache.spark.ml.attribute.{Attribute, AttributeGroup, NumericAttribute, UnresolvedAttribute}
import org.apache.spark.ml.feature.{VectorAssembler, CountVectorizerModel => SparkCountVectorizerModel}
import org.apache.spark.ml.linalg.{Vector, Vectors}
import org.apache.spark.ml.param._
import org.apache.spark.ml.param.shared.{HasFeaturesCol, HasInputCols, HasOutputCol}
import org.apache.spark.ml.util._
import org.apache.spark.ml.{Estimator, Model, Transformer}
import org.apache.spark.sql.functions.{col, struct, udf}
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}
import org.apache.spark.storage.StorageLevel

import scala.collection.mutable
import scala.util.control.Breaks

/**
 * Created by linjiuning on 2020/11/2.
 */

trait FeatureSelectorParams extends Params with HasInputCols with HasFeaturesCol {

  final val numTopFeatures = new IntParam(this, "numTopFeatures",
    "Number of features that selector will select. If the" +
      " number of features is < numTopFeatures, then this will select all features.",
    ParamValidators.gtEq(1))

  def getNumTopFeatures: Int = $(numTopFeatures)

  final val percentile = new DoubleParam(this, "percentile",
    "Percentile of features that selector will select.",
    ParamValidators.inRange(0, 1))

  def getPercentile: Double = $(percentile)

  final val threshold = new DoubleParam(this, "threshold",
    "If the importance of features is > threshold, then this will select all features.")

  def getThreshold: Double = $(threshold)

  final val cumulativePercentile = new DoubleParam(this, "cumulativePercentile",
    "Percentile of features that selector will select based on cumulative importance.")

  def getCumulativePercentile: Double = $(cumulativePercentile)

  final val selectorType = new Param[String](this, "selectorType",
    "The selector type of the FeatureSelector. " +
      "Supported options: " + FeatureSelector.supportedSelectorTypes.mkString(", "),
    ParamValidators.inArray[String](FeatureSelector.supportedSelectorTypes))

  def getSelectorType: String = $(selectorType)

  setDefault(numTopFeatures -> 50, percentile -> 0.1, threshold -> 0, cumulativePercentile -> 0.9,
    selectorType -> FeatureSelector.NumTopFeatures)

  protected def validateAndTransformSchema(schema: StructType): StructType = {
    val outputFields = schema.fields :+ StructField($(featuresCol), SchemaUtils.vectorUDT, false)
    StructType(outputFields)
  }
}

abstract class FeatureSelector(override val uid: String) extends Estimator[FeatureSelectorModel] with FeatureSelectorParams {

  def setInputCols(value: Array[String]): this.type = set(inputCols, value)

  def setNumTopFeatures(value: Int): this.type = set(numTopFeatures, value)

  def setPercentile(value: Double): this.type = set(percentile, value)

  def setThreshold(value: Double): this.type = set(threshold, value)

  def setCumulativePercentile(value: Double): this.type = set(cumulativePercentile, value)

  def setSelectorType(value: String): this.type = set(selectorType, value)

  def computeImportance(dataset: Dataset[_]): Array[Double]

  var numFeatures: Int = _

  def flatFeatureNames(dataset: Dataset[_]): Array[String] = {
    val schema = dataset.schema
    val row = dataset.toDF().first()
    $(inputCols).flatMap(col => {
      val size = if (SchemaUtils.isNumericType(schema, col)) {
        1
      } else if (SchemaUtils.isArrayType(schema, col)) {
        row.getAs[Seq[_]](col).length
      } else {
        row.getAs[Vector](col).size
      }
      (0 until size).map(i => FeatureSelector.encodeFeatureName(col, i))
    })
  }

  override def transformSchema(schema: StructType): StructType = {
    validateAndTransformSchema(schema)
  }

  override def fit(dataset: Dataset[_]): FeatureSelectorModel = {
    transformSchema(dataset.schema, logging = true)
    val handlePersistence = dataset.storageLevel == StorageLevel.NONE

    if (handlePersistence) dataset.persist(StorageLevel.MEMORY_AND_DISK)

    val newDs = if ($(inputCols).length > 1 || !SchemaUtils.isVectorType(dataset.schema, $(inputCols)(0))) {
      new VectorAssembler()
        .setInputCols($(inputCols))
        .setOutputCol($(featuresCol))
        .transform(dataset)
    } else {
      set(featuresCol, $(inputCols)(0))
      dataset
    }

    val featureNames = flatFeatureNames(dataset)
    numFeatures = featureNames.length
    val importance = computeImportance(newDs)

    if (handlePersistence) dataset.unpersist()

    copyValues(new FeatureSelectorModel(uid, featureNames, importance).setParent(this))
  }

  override def copy(extra: ParamMap): Estimator[FeatureSelectorModel] = defaultCopy(extra)
}

object FeatureSelector {
  val SHOW_ROWS: Int = 100

  val NumTopFeatures: String = "numTopFeatures"

  val Percentile: String = "percentile"

  val CumulativePercentile: String = "cumulativePercentile"

  val Threshold: String = "threshold"

  /** Set of selector types that supports. */
  val supportedSelectorTypes: Array[String] = Array(NumTopFeatures, Percentile, CumulativePercentile, Threshold)

  def encodeFeatureName(name: String, id: Int): String = {
    name + ":" + id
  }

  def decodeFeatureName(nameWithId: String): (String, Int) = {
    val i = nameWithId.lastIndexOf(":")
    if (i < 0) {
      (nameWithId, 0)
    } else {
      (nameWithId.slice(0, i), nameWithId.slice(i + 1, nameWithId.length).toInt)
    }
  }

  def show(features: Array[String], importance: Array[Double]): Unit = {
    val spark = SparkSession.builder().getOrCreate()
    import spark.implicits._
    spark.sparkContext.parallelize(features.zip(importance).sortBy(-_._2), 1)
      .toDF("feature", "importance")
      .show(SHOW_ROWS, false)
  }

  def show(features: Array[String], importance: Array[Double], selected: Array[Int]): Unit = {
    val spark = SparkSession.builder().getOrCreate()
    import spark.implicits._
    val set = selected.toSet
    spark.sparkContext.parallelize(features.zip(importance).zipWithIndex.map {
      case ((feature, score), i) => (feature, score, set.contains(i))
    }.sortBy(-_._2), 1)
      .toDF("feature", "importance", "selected")
      .show(SHOW_ROWS, false)
  }
}

class FeatureSelectorModel(override val uid: String,
                           val features: Array[String],
                           val importance: Array[Double])
  extends Model[FeatureSelectorModel] with FeatureSelectorParams with MLWritable {

  import FeatureSelector._
  import FeatureSelectorModel._

  def setInputCols(value: Array[String]): this.type = set(inputCols, value)

  def setNumTopFeatures(value: Int): this.type = set(numTopFeatures, value)

  def setPercentile(value: Double): this.type = set(percentile, value)

  def setThreshold(value: Double): this.type = set(threshold, value)

  def setCumulativePercentile(value: Double): this.type = set(cumulativePercentile, value)

  def setSelectorType(value: String): this.type = set(selectorType, value)

  def inverseMapIndex(transformer: Transformer, featureName: String = null): this.type = {
    val fName = if (featureName != null) {
      featureName
    } else {
      transformer.asInstanceOf[HasOutputCol].getOutputCol
    }

    val indices = features.map(FeatureSelector.decodeFeatureName)
      .zipWithIndex.filter(_._1._1.equals(fName)).map(it => (it._1._2, it._2))

    val vocabulary = transformer match {
      case model: SparkCountVectorizerModel =>
        model.vocabulary
      case model: CountVectorizerModel =>
        model.vocabulary
      case model: VectorizerModel =>
        model.vocabulary
      case model: OneHotEncoderModel =>
        model.categories.flatten.map(_.toString)
      case _ =>
        logWarning(s"Class ${transformer.getClass} has no inverse map index.")
        Array.empty[String]
    }

    indices.foreach {
      case (localI, globalI) =>
        if (localI < vocabulary.length) {
          val word = vocabulary(localI)
          features(globalI) = FeatureSelector.encodeFeatureName(fName + "#" + word, localI)
        }
    }
    this
  }

  private def select(): Array[Int] = {
    val featuresImportance = features.indices.zip(importance).toArray
    val selected = $(selectorType) match {
      case NumTopFeatures =>
        featuresImportance
          .sortBy(-_._2)
          .take($(numTopFeatures))
      case Percentile =>
        featuresImportance
          .sortBy(-_._2)
          .take((features.length * $(percentile)).toInt)
      case Threshold =>
        featuresImportance
          .filter(_._2 > $(threshold))
      case CumulativePercentile =>
        val sum = featuresImportance.map(_._2).sum
        val normFeaturesImportance = if (sum > 0) {
          featuresImportance.map(it => (it._1, it._2 / sum))
        } else {
          featuresImportance
        }
        var cum = 0.0
        val percentile = getCumulativePercentile
        val selected = mutable.ArrayBuilder.make[(Int, Double)]()
        val loop = new Breaks
        loop.breakable {
          for (elem <- normFeaturesImportance.sortBy(-_._2)) {
            if (cum >= percentile) {
              loop.break
            }
            selected += elem
            cum += elem._2
          }
        }
        selected.result()
      case errorType =>
        throw new IllegalStateException(s"Unknown Selector Type: $errorType")
    }
    selected.map(_._1)
  }

  def show(filter: Boolean = true): Unit = {
    if (filter) {
      val selected = select()
      FeatureSelector.show(features, importance, selected)
    } else {
      FeatureSelector.show(features, importance)
    }
  }

  override def transform(dataset: Dataset[_]): DataFrame = {
    transformSchema(dataset.schema, logging = true)
    // Schema transformation.
    val schema = dataset.schema
    lazy val first = dataset.toDF.first()
    val attrs = $(inputCols).flatMap { c =>
      val field = schema(c)
      val index = schema.fieldIndex(c)
      field.dataType match {
        case DoubleType =>
          val attr = Attribute.fromStructField(field)
          // If the input column doesn't have ML attribute, assume numeric.
          if (attr == UnresolvedAttribute) {
            Some(NumericAttribute.defaultAttr.withName(c))
          } else {
            Some(attr.withName(c))
          }
        case _: NumericType | BooleanType =>
          // If the input column type is a compatible scalar type, assume numeric.
          Some(NumericAttribute.defaultAttr.withName(c))
        case _: Any if SchemaUtils.isVectorType(schema, c) =>
          val group = AttributeGroup.fromStructField(field)
          if (group.attributes.isDefined) {
            // If attributes are defined, copy them with updated names.
            group.attributes.get.zipWithIndex.map { case (attr, i) =>
              if (attr.name.isDefined) {
                // TODO: Define a rigorous naming scheme.
                attr.withName(c + "_" + attr.name.get)
              } else {
                attr.withName(c + "_" + i)
              }
            }
          } else {
            // Otherwise, treat all attributes as numeric. If we cannot get the number of attributes
            // from metadata, check the first row.
            val numAttrs = group.numAttributes.getOrElse(first.getAs[Vector](index).size)
            Array.tabulate(numAttrs)(i => NumericAttribute.defaultAttr.withName(c + "_" + i))
          }
        case otherType =>
          throw new SparkException(s"FeatureSelectorModel does not support the $otherType type")
      }
    }
    val metadata = new AttributeGroup($(featuresCol), attrs).toMetadata()

    // Data transformation.
    val indices = select().toSet
    val selectFunc = udf { r: Row =>
      FeatureSelectorModel.select(indices)(r.toSeq: _*)
    }.asNondeterministic()
    val args = $(inputCols).map { c =>
      schema(c).dataType match {
        case DoubleType => dataset(c)
        case _: Any if SchemaUtils.isVectorType(schema, c) => dataset(c)
        case _: NumericType | BooleanType => dataset(c).cast(DoubleType).as(s"${c}_double_$uid")
      }
    }

    dataset.select(col("*"), selectFunc(struct(args: _*)).as($(featuresCol), metadata))
  }

  override def transformSchema(schema: StructType): StructType = {
    validateAndTransformSchema(schema)
  }

  override def copy(extra: ParamMap): FeatureSelectorModel = defaultCopy(extra)

  override def write: MLWriter = new FeatureSelectorModelWriter(this)
}

object FeatureSelectorModel extends MLReadable[FeatureSelectorModel] {

  private class FeatureSelectorModelWriter(instance: FeatureSelectorModel) extends MLWriter {

    private case class Data(features: Seq[String], importance: Seq[Double])

    override protected def saveImpl(path: String): Unit = {
      DefaultEasyMLParamsWriter.saveMetadata(instance, path, sc)
      val data = Data(instance.features, instance.importance)
      val dataPath = new Path(path, "data").toString
      sparkSession.createDataFrame(Seq(data)).repartition(1).write.parquet(dataPath)
    }
  }

  private class FeatureSelectorModelReader extends MLReader[FeatureSelectorModel] {

    private val className = classOf[FeatureSelectorModel].getName

    override def load(path: String): FeatureSelectorModel = {
      val metadata = DefaultEasyMLParamsReader.loadMetadata(path, sc, className)
      val dataPath = new Path(path, "data").toString
      val data = sparkSession.read.parquet(dataPath)
        .select("features", "importance")
        .head()
      val features = data.getAs[Seq[String]](0).toArray
      val importance = data.getAs[Seq[Double]](1).toArray
      val model = new FeatureSelectorModel(metadata.uid, features, importance)
      DefaultEasyMLParamsReader.getAndSetParams(model, metadata)
      model
    }
  }

  override def read: MLReader[FeatureSelectorModel] = new FeatureSelectorModelReader

  override def load(path: String): FeatureSelectorModel = super.load(path)

  def select(keepIndices: Set[Int])(vv: Any*): Vector = {
    val indices = mutable.ArrayBuilder.make[Int]
    val values = mutable.ArrayBuilder.make[Double]
    var idx = 0
    var cur = 0
    vv.foreach {
      case v: Double =>
        if (keepIndices.contains(cur)) {
          if (v != 0.0) {
            indices += idx
            values += v
          }
          idx += 1
        }
        cur += 1
      case vec: Vector =>
        (0 until vec.size).foreach(i => {
          if (keepIndices.contains(cur)) {
            val v = vec(i)
            if (v != 0.0) {
              indices += idx
              values += v
            }
            idx += 1
          }
          cur += 1
        })
      case null =>
        // TODO: output Double.NaN?
        throw new SparkException("Values to assemble cannot be null.")
      case o =>
        throw new SparkException(s"$o of type ${o.getClass.getName} is not supported.")
    }
    Vectors.sparse(idx, indices.result(), values.result()).compressed
  }
}
