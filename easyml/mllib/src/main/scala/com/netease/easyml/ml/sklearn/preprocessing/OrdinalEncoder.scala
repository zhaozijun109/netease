package com.netease.easyml.ml.sklearn.preprocessing

import java.util

import com.netease.easyml.ml.sklearn.{DefaultSklearnReader, SklearnReader}
import com.netease.easyml.ml.util.{DefaultEasyMLParamsReader, DefaultEasyMLParamsWriter}
import net.razorvine.pickle.objects.ClassDict
import org.apache.hadoop.fs.Path
import org.apache.spark.SparkException
import org.apache.spark.ml.Estimator
import org.apache.spark.ml.linalg.{Vector, Vectors}
import org.apache.spark.ml.param.ParamMap
import org.apache.spark.ml.util._
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions.{col, udf}
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Dataset, Row}

/**
 * Created by linjiuning on 2020/8/10.
 */

class OrdinalEncoder(override val uid: String) extends BaseEncoderEstimator[OrdinalEncoderModel] {
  def this() = {
    this(Identifiable.randomUID("ordinalEncoder"))
  }

  override def fit(dataset: Dataset[_]): OrdinalEncoderModel = {
    val categories = fit_(dataset)
    val model = new OrdinalEncoderModel(uid, categories).setParent(this)
    copyValues(model)
  }

  override def copy(extra: ParamMap): Estimator[OrdinalEncoderModel] = defaultCopy(extra)
}

object OrdinalEncoder extends DefaultParamsReadable[OrdinalEncoder] {

  override def load(path: String): OrdinalEncoder = super.load(path)
}

class OrdinalEncoderModel(override val uid: String,
                          override val categories: Array[Array[Any]])
  extends BaseEncoderModel[OrdinalEncoderModel] with MLWritable {

  import OrdinalEncoderModel._

  def this(categories: Array[Array[Any]]) = {
    this(Identifiable.randomUID("ordinalEncoder"), categories)
  }

  def this(uid: String) = {
    this(uid, null)
  }

  private def encoder: UserDefinedFunction = {
    val handleUnknown = getHandleUnknown
    udf { (label: Any) =>
      val labelArray = label match {
        case v: Seq[Any] =>
          v.toArray
        case v: Vector =>
          v.toArray
        case v: Any =>
          Array(v)
      }
      val array = labelArray.zipWithIndex.map {
        case (label, idx) =>
          var labelIdx = categories(idx).indexOf(label)
          if (labelIdx < 0) {
            if (handleUnknown.equals("error")) {
              throw new SparkException(s"Unseen value: $label. To handle unseen values, " +
                s"set Param handleInvalid to ignore.")
            } else {
              labelIdx = 0
            }
          }
          labelIdx.toDouble
      }
      Vectors.dense(array)
    }
  }

  override def transform(dataset: Dataset[_]): DataFrame = {
    dataset.withColumn($(outputCol), encoder(col($(inputCol))))
  }

  override def copy(extra: ParamMap): OrdinalEncoderModel = defaultCopy(extra)

  override def write: MLWriter = new OrdinalEncoderModelWriter(this)
}

object OrdinalEncoderModel extends MLReadable[OrdinalEncoderModel] with SklearnReader[OrdinalEncoderModel] {

  private class OrdinalEncoderModelWriter(instance: OrdinalEncoderModel) extends MLWriter {
    override protected def saveImpl(path: String): Unit = {
      DefaultEasyMLParamsWriter.saveMetadata(instance, path, sc)
      val rows = new util.ArrayList[Row]()
      val row = Row.fromSeq(Seq(instance.categories))
      rows.add(row)
      val field = StructType(Seq(StructField("categories", instance.categoryDataType)))
      val dataPath = new Path(path, "data").toString
      sparkSession.createDataFrame(rows, field).repartition(1).write.parquet(dataPath)
    }
  }

  private class OrdinalEncoderModelReader extends MLReader[OrdinalEncoderModel] {

    private val className = classOf[OrdinalEncoderModel].getName

    override def load(path: String): OrdinalEncoderModel = {
      val metadata = DefaultEasyMLParamsReader.loadMetadata(path, sc, className)
      val dataPath = new Path(path, "data").toString
      val data = sparkSession.read.parquet(dataPath)
      val Row(categories: Seq[Seq[Any]]) = data.select("categories")
        .head()
      val model = new OrdinalEncoderModel(metadata.uid, categories.map(_.toArray).toArray)
      DefaultEasyMLParamsReader.getAndSetParams(model, metadata)
      model
    }
  }

  override def read: MLReader[OrdinalEncoderModel] = new OrdinalEncoderModelReader

  override def load(path: String): OrdinalEncoderModel = super.load(path)

  override def readPickle(pickle: ClassDict): OrdinalEncoderModel = {
    BaseEncoder.parseCategories(pickle)
    val model = new OrdinalEncoderModel(null.asInstanceOf[Array[Array[Any]]])
    DefaultSklearnReader.getAndSetValues(model, pickle)
    model
  }
}