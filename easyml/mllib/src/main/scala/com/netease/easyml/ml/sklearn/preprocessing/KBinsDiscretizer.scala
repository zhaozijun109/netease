package com.netease.easyml.ml.sklearn.preprocessing

import com.netease.easyml.common.reflection.FromNDArray
import com.netease.easyml.ml.sklearn.{DefaultSklearnReader, SklearnReader}
import com.netease.easyml.ml.util.{DefaultEasyMLParamsReader, DefaultEasyMLParamsWriter, SchemaUtils, Utils}
import net.razorvine.pickle.objects.ClassDict
import numpy.core.NDArray
import org.apache.hadoop.fs.Path
import org.apache.spark.internal.Logging
import org.apache.spark.ml.feature.{Bucketizer, QuantileDiscretizer}
import org.apache.spark.ml.linalg.{Vector, Vectors}
import org.apache.spark.ml.param._
import org.apache.spark.ml.param.shared.{HasInputCol, HasOutputCol}
import org.apache.spark.ml.util._
import org.apache.spark.ml.{Estimator, Model}
import org.apache.spark.sql.types.{DoubleType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, Dataset, Row}
import org.apache.spark.storage.StorageLevel

/**
 * Created by linjiuning on 2020/8/18.
 */
trait KBinsDiscretizerParams extends Params with HasInputCol with HasOutputCol {
  val nBins: IntParam = new IntParam(this, "nBins",
    "The number of bins to produce.", ParamValidators.gt(2))

  def getNBins: Int = $(nBins)

  def setNBins(value: Int): this.type = set(nBins, value)

  val encode: Param[String] = new Param[String](this, "encode", "Method used to encode the transformed result.",
    ParamValidators.inArray(Array("onehot", "onehot-dense", "ordinal")))

  def getEncode: String = $(encode)

  def setEncode(value: String): this.type = set(encode, value)

  val strategy: Param[String] = new Param[String](this, "strategy", "Strategy used to define the widths of the bins.",
    ParamValidators.inArray(Array("uniform", "quantile", "kmeans")))

  def getStrategy: String = $(strategy)

  def setStrategy(value: String): this.type = set(strategy, value)

  setDefault(nBins -> 5, encode -> "onehot", strategy -> "quantile")

  /** Validates and transforms the input schema. */
  protected def validateAndTransformSchema(schema: StructType): StructType = {
    SchemaUtils.checkColumnType(schema, $(inputCol), SchemaUtils.vectorUDT)
    val outputFields = schema.fields :+ StructField($(outputCol), SchemaUtils.vectorUDT, false)
    StructType(outputFields)
  }
}

class KBinsDiscretizer(override val uid: String) extends Estimator[KBinsDiscretizerModel]
  with KBinsDiscretizerParams with DefaultParamsWritable {

  import KBinsDiscretizer._

  def this() = this(Identifiable.randomUID("kbinsDiscretizer"))

  def setInputCol(value: String): this.type = set(inputCol, value)

  def setOutputCol(value: String): this.type = set(outputCol, value)

  def uniform(dataset: Dataset[_]): Array[Array[Double]] = {
    val (min, max) = dataset.select($(inputCol)).rdd
      .map {
        case Row(vector: Vector) => vector
      }
      .aggregate((Array.empty[Double], Array.empty[Double]))(
        seqOp = (accum, vector) => {
          val array = vector.toArray
          val min = if (accum._1.isEmpty) {
            array
          } else {
            accum._1.zip(array).map {
              case (d1, d2) => Math.min(d1, d2)
            }
          }
          val max = if (accum._2.isEmpty) {
            array
          } else {
            accum._2.zip(array).map {
              case (d1, d2) => Math.max(d1, d2)
            }
          }
          (min, max)
        },
        combOp = (accum1, accum2) => {
          val min = if (accum1._1.isEmpty) {
            accum2._1
          } else if (accum2._1.isEmpty) {
            accum1._1
          } else {
            accum1._1.zip(accum2._1).map {
              case (d1, d2) => Math.min(d1, d2)
            }
          }
          val max = if (accum1._2.isEmpty) {
            accum2._2
          } else if (accum2._2.isEmpty) {
            accum1._2
          } else {
            accum1._2.zip(accum2._2).map {
              case (d1, d2) => Math.max(d1, d2)
            }
          }
          (min, max)
        }
      )
    min.zip(max).map {
      case (min_, max_) =>
        val step = (max_ - min_) / $(nBins)
        val array = (min_ to max_ by step).toArray
        if (array.length < $(nBins) + 1)
          array :+ max_
        else {
          array($(nBins)) = max_
          array
        }
    }.map(getDistinctSplits)
  }

  override def fit(dataset: Dataset[_]): KBinsDiscretizerModel = {
    transformSchema(dataset.schema, logging = true)

    val binEdges = if ($(strategy).equals("quantile")) {
      val handelPersistent = dataset.storageLevel == StorageLevel.NONE
      if (handelPersistent) dataset.persist(StorageLevel.MEMORY_AND_DISK)
      val (cols, newDs) = Utils.flatten(dataset, $(inputCol), DoubleType)
      val bucketizer = new QuantileDiscretizer()
        .setInputCols(cols)
        .setOutputCols(cols.map(_ + "_output"))
        .setNumBuckets($(nBins))
        .fit(newDs)

      if (handelPersistent) dataset.unpersist()
      bucketizer.getSplitsArray
    } else if ($(strategy).equals("uniform")) {
      uniform(dataset)
    } else {
      throw new IllegalArgumentException("kmeans strategy is not support yet")
    }
    copyValues(new KBinsDiscretizerModel(uid, binEdges).setParent(this))
  }

  override def copy(extra: ParamMap): Estimator[KBinsDiscretizerModel] = defaultCopy(extra)

  override def transformSchema(schema: StructType): StructType = {
    validateAndTransformSchema(schema)
  }
}

object KBinsDiscretizer extends DefaultParamsReadable[KBinsDiscretizer] with Logging {
  override def load(path: String): KBinsDiscretizer = super.load(path)

  def getDistinctSplits(splits: Array[Double]): Array[Double] = {
    splits(0) = Double.NegativeInfinity
    splits(splits.length - 1) = Double.PositiveInfinity
    val distinctSplits = splits.distinct
    if (splits.length != distinctSplits.length) {
      log.warn(s"Some quantiles were identical. Bucketing to ${distinctSplits.length - 1}" +
        s" buckets as a result.")
    }
    distinctSplits.sorted
  }
}

class KBinsDiscretizerModel(override val uid: String, val binEdges: Array[Array[Double]]) extends Model[KBinsDiscretizerModel]
  with KBinsDiscretizerParams with MLWritable {

  import KBinsDiscretizerModel._

  def this(binEdges: Array[Array[Double]]) = {
    this(Identifiable.randomUID("kbinsDiscretizer"), binEdges)
  }

  override def transform(dataset: Dataset[_]): DataFrame = {
    transformSchema(dataset.schema, logging = true)
    val dim = binEdges.length

    val (cols, newDs) = Utils.flatten(dataset, $(inputCol), DoubleType, size = Some(dim))

    val outputCols = cols.map(_ + "_output")
    var df = new Bucketizer()
      .setSplitsArray(binEdges)
      .setInputCols(cols)
      .setOutputCols(outputCols)
      .transform(newDs)

    df = df.drop(cols: _*)
    val newRdd = df.toDF().rdd.map(row => {
      val array = outputCols.map(i => row.getAs[Double](i))
      val vector = if (!$(encode).equals("ordinal")) {
        val indices = array.zipWithIndex.map {
          case (d, i) => d.toInt + i * $(nBins)
        }
        val value = Array.fill[Double](indices.length)(1.0)
        val vector = Vectors.sparse(array.length * $(nBins), indices, value)
        if ($(encode).equals("onehot-dense")) {
          vector.toDense
        } else {
          vector
        }
      } else {
        Vectors.dense(array)
      }
      val idx = outputCols.map(row.fieldIndex)
      val seq = row.toSeq.zipWithIndex.filter { case (value, i) => !idx.contains(i) }.map(_._1)
      Row.fromSeq(seq ++ Seq(vector))
    })
    dataset.sparkSession.createDataFrame(newRdd, transformSchema(dataset.schema))
  }

  override def copy(extra: ParamMap): KBinsDiscretizerModel = defaultCopy(extra)

  override def write: MLWriter = new KBinsDiscretizerModelWriter(this)

  override def transformSchema(schema: StructType): StructType = {
    validateAndTransformSchema(schema)
  }
}

object KBinsDiscretizerModel extends MLReadable[KBinsDiscretizerModel] with SklearnReader[KBinsDiscretizerModel] {

  private class KBinsDiscretizerModelWriter(instance: KBinsDiscretizerModel) extends MLWriter {

    private case class Data(binEdges: Array[Array[Double]])

    override protected def saveImpl(path: String): Unit = {
      DefaultEasyMLParamsWriter.saveMetadata(instance, path, sc)
      val data = Data(instance.binEdges)
      val dataPath = new Path(path, "data").toString
      sparkSession.createDataFrame(Seq(data)).repartition(1).write.parquet(dataPath)
    }
  }

  private class KBinsDiscretizerModelReader extends MLReader[KBinsDiscretizerModel] {

    private val className = classOf[KBinsDiscretizerModel].getName

    override def load(path: String): KBinsDiscretizerModel = {
      val metadata = DefaultEasyMLParamsReader.loadMetadata(path, sc, className)
      val dataPath = new Path(path, "data").toString
      val data = sparkSession.read.parquet(dataPath)
        .select("binEdges")
        .head()
      val binEdges = data.getAs[Seq[_]](0).toArray.map(it => it.asInstanceOf[Seq[Double]].toArray)
      val model = new KBinsDiscretizerModel(metadata.uid, binEdges)
      DefaultEasyMLParamsReader.getAndSetParams(model, metadata)
      model
    }
  }

  override def read: MLReader[KBinsDiscretizerModel] = new KBinsDiscretizerModelReader

  override def load(path: String): KBinsDiscretizerModel = super.load(path)

  /**
   * Returns an Spark ML instance for this class.
   */
  override def readPickle(pickle: ClassDict): KBinsDiscretizerModel = {
    val array = pickle.get("bin_edges_").asInstanceOf[NDArray]
    val splits = FromNDArray.toArray(array).asInstanceOf[Array[NDArray]].map(it => {
      val array = FromNDArray.toDoubleArray(it)
      array(0) = Double.NegativeInfinity
      array(array.length - 1) = Double.PositiveInfinity
      array
    })
    val model = new KBinsDiscretizerModel(splits)
    DefaultSklearnReader.getAndSetValues(model, pickle)
    model
  }
}
