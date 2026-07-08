package com.netease.easyml.ml.sklearn.preprocessing

import breeze.linalg.sum
import com.netease.easyml.ml.sklearn.{DefaultSklearnReader, SklearnReader}
import com.netease.easyml.ml.util.VectorUtils.{asBreeze, fromBreeze}
import com.netease.easyml.ml.util.{DefaultEasyMLParamsReader, DefaultEasyMLParamsWriter, SchemaUtils}
import net.razorvine.pickle.objects.ClassDict
import org.apache.hadoop.fs.Path
import org.apache.spark.ml.linalg.Vector
import org.apache.spark.ml.param.ParamMap
import org.apache.spark.ml.param.shared.{HasInputCol, HasOutputCol}
import org.apache.spark.ml.util._
import org.apache.spark.ml.{Estimator, Model}
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.sql.functions.udf
import org.apache.spark.sql.types.{DataType, StructType}
import org.apache.spark.sql.{DataFrame, Dataset, Row}
import org.apache.spark.storage.StorageLevel

/**
 * Created by linjiuning on 2020/8/18.
 */
class KernalCenterer(override val uid: String) extends Estimator[KernalCentererModel]
  with HasInputCol with HasOutputCol with DefaultParamsWritable {

  def this() = {
    this(Identifiable.randomUID("kernalCenterer"))
  }

  def setInputCol(value: String): this.type = set(inputCol, value)

  def setOutputCol(value: String): this.type = set(outputCol, value)

  override def fit(dataset: Dataset[_]): KernalCentererModel = {
    SchemaUtils.checkVectorType(dataset.schema, $(inputCol))
    val handlePersistence = dataset.storageLevel == StorageLevel.NONE

    if (handlePersistence) dataset.persist(StorageLevel.MEMORY_AND_DISK)

    val nSamples = dataset.count().toDouble

    val fitRows = dataset.select($(inputCol)).toDF()
      .rdd
      .map {
        case Row(vector: Vector) =>
          asBreeze(vector) / nSamples
      }.reduce(_ + _)

    val fitAll = sum(fitRows) / nSamples

    if (handlePersistence) dataset.unpersist()

    copyValues(new KernalCentererModel(uid, fromBreeze(fitRows), fitAll))
  }

  override def copy(extra: ParamMap): Estimator[KernalCentererModel] = defaultCopy(extra)

  override def transformSchema(schema: StructType): StructType = schema

}

object KernalCenterer extends DefaultParamsReadable[KernalCenterer] {

  override def load(path: String): KernalCenterer = super.load(path)
}

class KernalCentererModel(override val uid: String,
                          val fitRows: Vector,
                          val fitAll: Double)
  extends Model[KernalCentererModel] with HasInputCol with HasOutputCol with MLWritable {

  import KernalCentererModel._

  def this(fitRows: Vector, fitAll: Double) = {
    this(Identifiable.randomUID("kernalCenterer"), fitRows, fitAll)
  }

  override def transform(dataset: Dataset[_]): DataFrame = {
    transformSchema(dataset.schema, logging = true)
    val transformUDF = udf(this.createTransformFunc, outputDataType)
    dataset.withColumn($(outputCol), transformUDF(dataset($(inputCol))))
  }

  def createTransformFunc: Vector => Vector = {
    (vector: Vector) => {
      val breezeVector = asBreeze(vector)
      fromBreeze(breezeVector - asBreeze(fitRows) - sum(breezeVector) / vector.size + fitAll)
    }
  }

  def outputDataType: DataType = SchemaUtils.vectorUDT

  override def transformSchema(schema: StructType): StructType = {
    SchemaUtils.checkVectorType(schema, $(inputCol))
    SchemaUtils.appendColumn(schema, $(outputCol), SchemaUtils.vectorUDT)
  }

  override def write: MLWriter = new KernalCentererModelWriter(this)

  override def copy(extra: ParamMap): KernalCentererModel = defaultCopy(extra)
}

object KernalCentererModel extends MLReadable[KernalCentererModel] with SklearnReader[KernalCentererModel] {

  private class KernalCentererModelWriter(instance: KernalCentererModel) extends MLWriter {

    private case class Data(fitRows: Vector, fitAll: Double)

    override protected def saveImpl(path: String): Unit = {
      DefaultEasyMLParamsWriter.saveMetadata(instance, path, sc)
      val data = Data(instance.fitRows, instance.fitAll)
      val dataPath = new Path(path, "data").toString
      sparkSession.createDataFrame(Seq(data)).repartition(1).write.parquet(dataPath)
    }
  }

  private class KernalCentererModelReader extends MLReader[KernalCentererModel] {

    private val className = classOf[KernalCentererModel].getName

    override def load(path: String): KernalCentererModel = {
      val metadata = DefaultEasyMLParamsReader.loadMetadata(path, sc, className)
      val dataPath = new Path(path, "data").toString
      val data = sparkSession.read.parquet(dataPath)
      val Row(fitRows: Vector, fitAll: Double) = MLUtils.convertVectorColumnsToML(data, "fitRows")
        .select("fitRows", "fitAll")
        .head()
      val model = new KernalCentererModel(metadata.uid, fitRows, fitAll)
      DefaultEasyMLParamsReader.getAndSetParams(model, metadata)
      model
    }
  }

  override def read: MLReader[KernalCentererModel] = new KernalCentererModelReader

  override def load(path: String): KernalCentererModel = super.load(path)

  override def readPickle(pickle: ClassDict): KernalCentererModel = {
    val model = new KernalCentererModel(null.asInstanceOf[Vector], 0.0)
    DefaultSklearnReader.getAndSetValues(model, pickle, mapping = Some(Map("K_fit_rows_" -> "fitRows", "K_fit_all_" -> "fitAll")))
    model
  }
}
