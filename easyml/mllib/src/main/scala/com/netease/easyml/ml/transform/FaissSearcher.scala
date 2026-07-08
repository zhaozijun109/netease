package com.netease.easyml.ml.transform

import java.util.function.Supplier

import com.netease.easyml.common.resource.ResourceManager
import com.netease.easyml.common.util.{ConvertUtil, IOUtil, SparkUtil}
import com.netease.easyml.ml.param.{HasBatchSize, HasNumPartitions, HasPath}
import com.netease.easyml.ml.util.SchemaUtils
import org.apache.spark.SparkException
import org.apache.spark.ml.Transformer
import org.apache.spark.ml.linalg.Vector
import org.apache.spark.ml.param._
import org.apache.spark.ml.param.shared.HasFeaturesCol
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Dataset, Row}
import org.faiss.jni.JniUtils
import org.faiss.{DataType => FDataType, _}

import scala.collection.mutable

/**
 * Created by linjiuning on 2020/11/6.
 */

trait FaissSearcherParams extends Params with HasFeaturesCol with
  HasPath with HasNumPartitions with HasBatchSize {

  val labelsCol: Param[String] = new Param[String](this, "labels", "labels column name")

  setDefault(labelsCol, "labels")

  def getLabelsCol: String = $(labelsCol)

  val distancesCol: Param[String] = new Param[String](this, "distancesCol", "distances column name")

  setDefault(distancesCol, "distances")

  def getDistancesCol: String = $(distancesCol)

  val handleUnknown: Param[String] = new Param[String](this, "handleUnknown",
    "Whether to raise an error or ignore if an unknown categorical feature is present during transform (default is to raise). " +
      "When this parameter is set to ‘ignore’ and an unknown category is encountered during transform, the resulting one-hot encoded columns for this feature will be all zeros. " +
      "In the inverse transform, an unknown category will be denoted as None.",
    ParamValidators.inArray(Array("ignore", "error")))

  def getHandleUnknown: String = $(handleUnknown)

  val debug = new Param[Boolean](this, "debug", "whether to log debug info")

  def getDebug: Boolean = $(debug)

  val numThreads = new IntParam(this, "numThreads", "faiss parallelism, default spark.task.cpus")

  def getNumThreads: Int = $(numThreads)

  val vocabs = new StringArrayParam(this, "vocabs", "vocabulary of labels")

  def getVocabs: Array[String] = $(vocabs)

  val ioFlag = new IntParam(this, "ioFlag", "io flag.")

  def getIoFlag: Int = $(ioFlag)

  val k = new IntParam(this, "k", "return at most k vectors. If there are not enough results for a query, the result array is padded with -1s.",
    ParamValidators.gt(0))

  def getK: Int = $(k)

  val nprobe = new IntParam(this, "nprobe", "number of probes at query time.",
    ParamValidators.gt(0))

  def getNprobe: Int = $(nprobe)

  val threshold: FloatParam = new FloatParam(this, "threshold", "threshold of distance", ParamValidators.gtEq(0))

  def getThreshold: Float = $(threshold)

  val isGreaterBetter: BooleanParam = new BooleanParam(this, "isGreaterBetter", "whether distance value is greater and better")

  def getIsGreaterBetter: Boolean = $(isGreaterBetter)

  /**
   * Validate and transform the input schema.
   */
  protected def validateAndTransformSchema(schema: StructType): StructType = {
    SchemaUtils.isVectorType(schema, $(featuresCol))
    val actualDataType = schema($(featuresCol)).dataType
    require(SchemaUtils.isNumericArrayType(schema, $(featuresCol)) ||
      SchemaUtils.isVectorType(schema, $(featuresCol)), s"Column $getFeaturesCol must be of type " +
      s"NumericArrayType or VectorType but was actually of type $actualDataType")

    var newSchema = schema
    if ($(vocabs).isEmpty) {
      newSchema = SchemaUtils.appendColumn(newSchema, $(labelsCol), ArrayType(LongType, containsNull = true), nullable = true)
    } else {
      newSchema = SchemaUtils.appendColumn(newSchema, $(labelsCol), ArrayType(StringType, containsNull = true), nullable = true)
    }
    SchemaUtils.appendColumn(newSchema, $(distancesCol), ArrayType(FloatType, containsNull = true), nullable = true)
  }

  setDefault(vocabs -> Array.empty[String], debug -> true, batchSize -> 128, k -> 5,
    ioFlag -> 0, handleUnknown -> "error", numThreads -> 0)
}

class FaissSearcher(override val uid: String) extends Transformer
  with FaissSearcherParams with DefaultParamsWritable {

  import FaissSearcher._

  def this() = this(Identifiable.randomUID("faiss"))

  def setFeaturesCol(value: String): this.type = set(featuresCol, value)

  def setLabelsCol(value: String): this.type = set(labelsCol, value)

  def setDistancesCol(value: String): this.type = set(distancesCol, value)

  def setPath(value: String): this.type = set(path, value)

  def setBatchSize(value: Int): this.type = set(batchSize, value)

  def setDebug(value: Boolean): this.type = set(debug, value)

  def setNumThreads(value: Int): this.type = set(numThreads, value)

  def setVocabs(value: Array[String]): this.type = set(vocabs, value)

  def setNumPartitions(value: Int): this.type = set(numPartitions, value)

  def setIoFlag(value: Int): this.type = set(ioFlag, value)

  def setK(value: Int): this.type = set(k, value)

  def setNprobe(value: Int): this.type = set(nprobe, value)

  def setHandleUnknown(value: String): this.type = set(handleUnknown, value)

  def setThreshold(value: Float): this.type = set(threshold, value)

  def setIsGreaterBetter(value: Boolean): this.type = set(isGreaterBetter, value)

  override def transform(dataset: Dataset[_]): DataFrame = {
    val structType = transformSchema(dataset.schema, logging = true)

    val conf = dataset.sparkSession.sparkContext.getConf
    val nThreads = if (getNumThreads != 0) {
      getNumThreads
    } else {
      SparkUtil.getNumTaskCpus(conf)
    }

    val nPartitions = if (!isSet(numPartitions)) {
      4 * SparkUtil.getParallelism(conf)
    } else {
      getNumPartitions
    }

    val newDs = if (dataset.rdd.getNumPartitions < nPartitions) {
      dataset.repartition(nPartitions)
    } else {
      dataset
    }

    val idWord = $(vocabs).zipWithIndex.map(it => (it._2, it._1)).toMap
    val idWordBc = dataset.sparkSession.sparkContext.broadcast(idWord)

    val path = getPath
    val ioFlag = getIoFlag
    val batchSize = getBatchSize
    val featuresCol = getFeaturesCol
    val k = getK
    val debug = getDebug
    val handleUnknown = getHandleUnknown
    val nprobe_ = if (isSet(nprobe)) {
      getNprobe
    } else {
      -1
    }

    val nullRdd = newDs.toDF.filter(col(featuresCol).isNull).toDF.rdd
      .map(row => Row.fromSeq(row.toSeq ++ Seq(null, null)))
    val nullDf = dataset.sparkSession.createDataFrame(nullRdd, structType)

    val newRdd = newDs.toDF.filter(col(featuresCol).isNotNull).rdd.mapPartitions(iter => {
      val idWord = idWordBc.value

      if (nThreads > 0) {
        logInfo(s"Set omp num threads = $nThreads")
        JniUtils.ompSetNumThreads(nThreads)
      }

      val index = getOrCreate(path, ioFlag)
      index match {
        case f: IndexIVF if nprobe_ >= 0 =>
          f.setNprobe(nprobe_)
        case _ =>
      }

      val (threshold_, isGreaterBetter_) = if (isSet(threshold)) {
        val isGreaterBetter_ = if (isSet(isGreaterBetter)) {
          getIsGreaterBetter
        } else {
          index.metricType() == MetricType.METRIC_INNER_PRODUCT
        }
        (getThreshold, isGreaterBetter_)
      } else {
        (-1.0f, true)
      }

      val numOfBytes = FDataType.FLOAT32.getNumOfBytes
      iter.grouped(batchSize).flatMap(rows => {
        val n = rows.length
        val dim = index.d()
        val size = n * dim * numOfBytes
        val buffer = BufferUtils.allocateDirect(size)

        for (row <- rows) {
          row.getAs[Any](featuresCol) match {
            case vector: Vector =>
              if (vector.size != dim) {
                throw new SparkException(s"Vector size = ${vector.size} must be equals to index d = $dim.")
              }
              for (i <- 0 until dim) {
                buffer.putFloat(vector(i).toFloat)
              }
            case array: Seq[_] =>
              if (array.length != dim) {
                throw new SparkException(s"Array size = ${array.length} must be equals to index d = $dim.")
              }
              array.foreach(e => {
                buffer.putFloat(ConvertUtil.toFloat(e))
              })
          }
        }

        val sTime = System.currentTimeMillis()
        val result = index.search(n, buffer, k)
        val eTime = System.currentTimeMillis()
        if (debug) {
          logInfo(s"Cost: ${eTime - sTime}/$n = ${(eTime - sTime) * 1.0 / n}ms")
        }
        val labels = result.getValue0
        val distances = result.getValue1

        var s = 0
        (0 until n).map(i => {
          val e = Math.min(s + k, labels.length)
          val oLabels = mutable.ArrayBuilder.make[Long]()
          val sLabels = mutable.ArrayBuilder.make[String]()
          val oDistances = mutable.ArrayBuilder.make[Float]()
          for (j <- s until e) {
            val label = labels(j)
            val distance = distances(j)
            if (label != -1 &&
              (threshold_ < 0 ||
                (isGreaterBetter_ && distance >= threshold_) ||
                (!isGreaterBetter_ && distance <= threshold_))) {
              if (idWord.nonEmpty) {
                if (!idWord.contains(label.toInt) && handleUnknown.equals("error")) {
                  throw new SparkException(s"Unseen value: $label. To handle unseen values, " +
                    s"set Param handleInvalid to ignore.")
                }
                if (idWord.contains(label.toInt)) {
                  sLabels += idWord(label.toInt)
                  oDistances += distance
                }
              } else {
                oLabels += label
                oDistances += distance
              }
            }
          }
          s = e
          val row = rows(i)
          val distArr = oDistances.result()
          if (distArr.isEmpty) {
            Row.fromSeq(row.toSeq ++ Seq(null, null))
          } else {
            val labelArr = if (idWord.nonEmpty) {
              sLabels.result()
            } else {
              oLabels.result()
            }
            Row.fromSeq(row.toSeq ++ Seq(labelArr, distArr))
          }
        })
      })
    })

    val newDf = dataset.sparkSession.createDataFrame(newRdd, structType)
    newDf.union(nullDf)
  }

  override def copy(extra: ParamMap): Transformer = defaultCopy(extra)

  override def transformSchema(schema: StructType): StructType = {
    validateAndTransformSchema(schema)
  }
}

object FaissSearcher extends DefaultParamsReadable[FaissSearcher] {

  def getOrCreate(path: String, ioFlag: Int): Index = synchronized {
    ResourceManager.getOrCreate(path, new Supplier[Index] {
      override def get(): Index = {
        val model = IOUtil.mayCopyHdfsToLocal(path)
        Index.readIndex(model, ioFlag)
      }
    })
  }

  override def load(path: String): FaissSearcher = super.load(path)
}