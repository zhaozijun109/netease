package com.netease.easyml.ml.sklearn.preprocessing

import java.util

import com.netease.easyml.annotation.Register
import com.netease.easyml.ml.sklearn.{DefaultSklearnReader, SklearnReader}
import com.netease.easyml.ml.util.{DefaultEasyMLParamsReader, DefaultEasyMLParamsWriter}
import net.razorvine.pickle.objects.ClassDict
import numpy.core.NDArray
import org.apache.hadoop.fs.Path
import org.apache.spark.SparkException
import org.apache.spark.ml.Estimator
import org.apache.spark.ml.linalg.{Vector, Vectors}
import org.apache.spark.ml.param.{BooleanParam, Param, ParamMap, ParamValidators}
import org.apache.spark.ml.util._
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions.{col, udf}
import org.apache.spark.sql.types.{ArrayType, IntegerType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, Dataset, Row}

import scala.collection.JavaConversions._

/**
 * Created by linjiuning on 2020/8/10.
 */

trait OneHotEncoderParams extends BaseEncoderParams {
  val drop: Param[String] = new Param[String](this, "drop",
    "Specifies a methodology to use to drop one of the categories per feature. " +
      "This is useful in situations where perfectly collinear features cause problems, " +
      "such as when feeding the resulting data into a neural network or an unregularized regression.",
    ParamValidators.inArray(Array("first", "if_binary", "none")))

  def getDrop: String = $(drop)

  def setDrop(value: String): this.type = set(drop, value)

  val sparse: BooleanParam = new BooleanParam(this, "sparse", "Will return sparse matrix if set True else will return an array.")

  def getSparse: Boolean = $(sparse)

  def setSparse(value: Boolean): this.type = set(sparse, value)

  setDefault(drop -> "none", sparse -> true)
}

@Register(prefix = "sklearn.")
class OneHotEncoder(override val uid: String)
  extends BaseEncoderEstimator[OneHotEncoderModel] with OneHotEncoderParams {
  def this() = {
    this(Identifiable.randomUID("oneHotEncoder"))
  }

  def computeDropIdx(categories: Array[Array[Any]]): Array[Int] = {
    if ($(drop).equals("first")) {
      Array.fill[Int](categories.length)(0)
    } else if ($(drop).equals("if_binary")) {
      categories.map(it => {
        if (it.length == 2) 0 else -1
      })
    } else {
      null.asInstanceOf[Array[Int]]
    }
  }

  override def fit(dataset: Dataset[_]): OneHotEncoderModel = {
    if (!$(drop).equals("none") && !$(handleUnknown).equals("error")) {
      throw new IllegalArgumentException("`handleUnknown` must be 'error' when the drop parameter is " +
        "specified, as both would create categories that are all zero.")
    }
    val categories = fit_(dataset)
    val dropIdx = computeDropIdx(categories)
    val model = new OneHotEncoderModel(uid, categories, dropIdx).setParent(this)
    copyValues(model)
  }

  override def copy(extra: ParamMap): Estimator[OneHotEncoderModel] = defaultCopy(extra)
}

object OneHotEncoder extends DefaultParamsReadable[OneHotEncoder] {

  override def load(path: String): OneHotEncoder = super.load(path)
}

@Register(prefix = "sklearn.")
class OneHotEncoderModel(override val uid: String,
                         override val categories: Array[Array[Any]],
                         val dropIdx: Array[Int])
  extends BaseEncoderModel[OneHotEncoderModel] with OneHotEncoderParams with MLWritable {

  import OneHotEncoderModel._

  def this(categories: Array[Array[Any]], dropIdx: Array[Int]) = {
    this(Identifiable.randomUID("oneHotEncoder"), categories, dropIdx)
  }

  private def encoder: UserDefinedFunction = {
    var nFeatures = categories.map(_.length).sum
    if (dropIdx != null) {
      nFeatures -= dropIdx.count(_ >= 0)
    }
    val size = categories.indices.map(i => {
      if (dropIdx != null && dropIdx(i) >= 0) {
        categories(i).length - 1
      } else {
        categories(i).length
      }
    })
    var s = 0
    val offsets = size.map(d => {
      val old = s
      s += d
      old
    })

    val handleUnknown = getHandleUnknown
    val sparse = getSparse
    udf { (label: Any) =>
      val labelArray = label match {
        case v: Seq[Any] =>
          v.toArray
        case v: Vector =>
          v.toArray
        case v: Any =>
          Array(v)
      }
      val indices = labelArray.zipWithIndex.map {
        case (label, i) =>
          var labelIdx = categories(i).indexOf(label)
          if (labelIdx < 0) {
            if (handleUnknown.equals("error")) {
              throw new SparkException(s"Unseen value: $label. To handle unseen values, " +
                s"set Param handleInvalid to ignore.")
            } else {
              labelIdx = 0
            }
          }
          if (dropIdx != null && dropIdx(i) >= 0) {
            if (dropIdx(i) == labelIdx) {
              -1
            } else {
              labelIdx - 1 + offsets(i)
            }
          } else {
            labelIdx + offsets(i)
          }
      }.filter(_ >= 0)
      val vector = Vectors.sparse(nFeatures, indices, Array.fill(indices.length)(1.0))
      if (sparse) {
        vector
      } else {
        vector.toDense
      }
    }
  }

  override def transform(dataset: Dataset[_]): DataFrame = {
    dataset.withColumn($(outputCol), encoder(col($(inputCol))))
  }

  override def copy(extra: ParamMap): OneHotEncoderModel = defaultCopy(extra)

  override def write: MLWriter = new OneHotEncoderModelWriter(this)
}

object OneHotEncoderModel extends MLReadable[OneHotEncoderModel] with SklearnReader[OneHotEncoderModel] {

  private class OneHotEncoderModelWriter(instance: OneHotEncoderModel) extends MLWriter {
    override protected def saveImpl(path: String): Unit = {
      DefaultEasyMLParamsWriter.saveMetadata(instance, path, sc)
      val rows = new util.ArrayList[Row]()
      var dropIdx = instance.dropIdx
      if (dropIdx == null) {
        dropIdx = Array.fill[Int](instance.categories.length)(-1)
      }
      val row = Row.fromSeq(Seq(instance.categories, dropIdx))
      rows.add(row)
      val field = StructType(Seq(
        StructField("categories", instance.categoryDataType),
        StructField("dropIdx", ArrayType(IntegerType, true))))
      val dataPath = new Path(path, "data").toString
      sparkSession.createDataFrame(rows, field).repartition(1).write.parquet(dataPath)
    }
  }

  private class OneHotEncoderModelReader extends MLReader[OneHotEncoderModel] {

    private val className = classOf[OneHotEncoderModel].getName

    override def load(path: String): OneHotEncoderModel = {
      val metadata = DefaultEasyMLParamsReader.loadMetadata(path, sc, className)
      val dataPath = new Path(path, "data").toString
      val data = sparkSession.read.parquet(dataPath)
      val Row(categories: Seq[Seq[Any]], dropIdx: Seq[Int]) = data.select("categories", "dropIdx")
        .head()
      val count = dropIdx.count(_ >= 0)
      val model = new OneHotEncoderModel(metadata.uid, categories.map(_.toArray).toArray, if (count > 0) dropIdx.toArray else null.asInstanceOf[Array[Int]])
      DefaultEasyMLParamsReader.getAndSetParams(model, metadata)
      model
    }
  }

  override def read: MLReader[OneHotEncoderModel] = new OneHotEncoderModelReader

  override def load(path: String): OneHotEncoderModel = super.load(path)

  override def readPickle(pickle: ClassDict): OneHotEncoderModel = {
    BaseEncoder.parseCategories(pickle)
    val ndarray = pickle.remove("drop_idx_").asInstanceOf[NDArray]
    val dropIdx = if (ndarray != null) {
      val array = ndarray.getData.asInstanceOf[java.util.List[_]]
      array.map(it => if (it != null) it.toString.toInt else -1).toArray
    } else {
      null.asInstanceOf[Array[Int]]
    }
    pickle.put("drop_idx", dropIdx)
    val model = new OneHotEncoderModel(null, null)
    DefaultSklearnReader.getAndSetValues(model, pickle)
    model
  }
}