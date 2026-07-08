package com.netease.easyml.ml.sklearn.preprocessing

import java.util

import com.netease.easyml.common.reflection.FromNDArray
import com.netease.easyml.ml.sklearn.{DefaultSklearnReader, SklearnReader}
import com.netease.easyml.ml.util.{DefaultEasyMLParamsReader, DefaultEasyMLParamsWriter, SchemaUtils}
import net.razorvine.pickle.objects.ClassDict
import numpy.core.NDArray
import org.apache.hadoop.fs.Path
import org.apache.spark.ml.linalg.{Vector, Vectors}
import org.apache.spark.ml.param.shared.{HasInputCol, HasOutputCol}
import org.apache.spark.ml.param.{BooleanParam, IntParam, ParamMap, Params}
import org.apache.spark.ml.util._
import org.apache.spark.ml.{Estimator, Model}
import org.apache.spark.sql.functions.udf
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Dataset, Row}
import org.apache.spark.storage.StorageLevel

/**
 * Created by linjiuning on 2020/8/18.
 */

trait LabelBinarizerParams extends Params with HasInputCol with HasOutputCol {
  val negLabel: IntParam = new IntParam(this, "negLabel",
    "Value with which negative labels must be encoded.")

  def getNegLabel: Int = $(negLabel)

  def setNegLabel(value: Int): this.type = set(negLabel, value)

  val posLabel: IntParam = new IntParam(this, "posLabel",
    "Value with which positive labels must be encoded.")

  def getPosLabel: Int = $(posLabel)

  def setPosLabel(value: Int): this.type = set(posLabel, value)

  val sparseOutput: BooleanParam = new BooleanParam(this, "sparseOutput",
    "True if the returned array from transform is desired to be in sparse CSR format.")

  def getSparseOutput: Boolean = $(sparseOutput)

  def setSparseOutput(value: Boolean): this.type = set(sparseOutput, value)

  setDefault(negLabel -> 0, posLabel -> 1, sparseOutput -> false)

  protected def validateAndTransformSchema(schema: StructType): StructType = {
    require(SchemaUtils.isNumericType(schema, $(inputCol)) || SchemaUtils.isColumnType(schema, $(inputCol), StringType),
      s"Column $getInputCol must be of type " +
        s"NumericType or StringType but was actually of type ${schema(getInputCol).dataType}")
    SchemaUtils.appendColumn(schema, $(outputCol), SchemaUtils.vectorUDT)
  }
}

class LabelBinarizer(override val uid: String) extends Estimator[LabelBinarizerModel]
  with LabelBinarizerParams with DefaultParamsWritable {

  def this() = {
    this(Identifiable.randomUID("labelBinarizer"))
  }

  def setInputCol(value: String): this.type = set(inputCol, value)

  def setOutputCol(value: String): this.type = set(outputCol, value)

  override def fit(dataset: Dataset[_]): LabelBinarizerModel = {
    transformSchema(dataset.schema, logging = true)
    val handelPersistent = dataset.storageLevel == StorageLevel.NONE
    if (handelPersistent) dataset.persist(StorageLevel.MEMORY_AND_DISK)
    var categories = dataset.select($(inputCol)).rdd
      .map(row => {
        row.get(0)
      }).distinct().collect()

    categories = if (categories.isEmpty || categories(0).getClass == classOf[String]) {
      categories.map(_.toString).sorted.map(_.asInstanceOf[Any])
    } else {
      categories.map(_.toString.toDouble).sorted.map(_.asInstanceOf[Any])
    }

    if (handelPersistent) dataset.unpersist()
    copyValues(new LabelBinarizerModel(uid, categories).setParent(this))
  }

  override def copy(extra: ParamMap): Estimator[LabelBinarizerModel] = defaultCopy(extra)

  override def transformSchema(schema: StructType): StructType = schema

}

object LabelBinarizer extends DefaultParamsReadable[LabelBinarizer] {

  override def load(path: String): LabelBinarizer = super.load(path)
}

class LabelBinarizerModel(override val uid: String,
                          val categories: Array[Any])
  extends Model[LabelBinarizerModel] with LabelBinarizerParams with MLWritable {

  import LabelBinarizerModel._

  def this(categories: Array[Any]) = {
    this(Identifiable.randomUID("labelBinarizer"), categories)
  }

  override def transform(dataset: Dataset[_]): DataFrame = {
    transformSchema(dataset.schema, logging = true)
    val transformUDF = udf(this.createTransformFunc, outputDataType)
    dataset.withColumn($(outputCol), transformUDF(dataset($(inputCol))))
  }

  def createTransformFunc: Any => Vector = {
    (label: Any) => {
      val id = categories.indexOf(label)
      val array = Array.fill(categories.length)($(negLabel).toDouble)
      array(id) = $(posLabel)
      val vector = Vectors.dense(array)
      if ($(sparseOutput))
        vector.toSparse
      else
        vector
    }
  }

  def outputDataType: DataType = SchemaUtils.vectorUDT

  override def transformSchema(schema: StructType): StructType = {
    validateAndTransformSchema(schema)
  }

  override def write: MLWriter = new LabelBinarizerModelWriter(this)

  override def copy(extra: ParamMap): LabelBinarizerModel = defaultCopy(extra)

  def categoryDataType: DataType = {
    val dtype = if (categories.isEmpty) {
      StringType
    } else {
      categories(0) match {
        case _: String =>
          StringType
        case _: Double =>
          DoubleType
      }
    }
    ArrayType(dtype)
  }
}

object LabelBinarizerModel extends MLReadable[LabelBinarizerModel] with SklearnReader[LabelBinarizerModel] {

  private class LabelBinarizerModelWriter(instance: LabelBinarizerModel) extends MLWriter {

    override protected def saveImpl(path: String): Unit = {
      DefaultEasyMLParamsWriter.saveMetadata(instance, path, sc)
      val rows = new util.ArrayList[Row]()
      val row = Row.fromSeq(Seq(instance.categories))
      rows.add(row)
      val field = StructType(Seq(
        StructField("categories", instance.categoryDataType)))
      val dataPath = new Path(path, "data").toString
      sparkSession.createDataFrame(rows, field).repartition(1).write.parquet(dataPath)
    }
  }

  private class LabelBinarizerModelReader extends MLReader[LabelBinarizerModel] {

    private val className = classOf[LabelBinarizerModel].getName

    override def load(path: String): LabelBinarizerModel = {
      val metadata = DefaultEasyMLParamsReader.loadMetadata(path, sc, className)
      val dataPath = new Path(path, "data").toString
      val data = sparkSession.read.parquet(dataPath)
      val Row(categories: Seq[Any]) = data.select("categories")
        .head()
      val model = new LabelBinarizerModel(metadata.uid, categories.toArray)
      DefaultEasyMLParamsReader.getAndSetParams(model, metadata)
      model
    }
  }

  override def read: MLReader[LabelBinarizerModel] = new LabelBinarizerModelReader

  override def load(path: String): LabelBinarizerModel = super.load(path)

  override def readPickle(pickle: ClassDict): LabelBinarizerModel = {
    val classes = pickle.remove("classes_").asInstanceOf[NDArray]
    val categories = FromNDArray.toArray(classes).asInstanceOf[Array[_]].map(_.asInstanceOf[Any])
    val model = new LabelBinarizerModel(categories)
    DefaultSklearnReader.getAndSetValues(model, pickle)
    model
  }
}
