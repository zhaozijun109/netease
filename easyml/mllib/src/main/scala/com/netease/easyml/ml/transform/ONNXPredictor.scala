package com.netease.easyml.ml.transform

import java.util
import java.util.function.Supplier

import ai.onnxruntime._
import com.netease.easyml.common.collection.{Params => JParams}
import com.netease.easyml.common.resource.ResourceManager
import com.netease.easyml.common.util.IOUtil
import com.netease.easyml.local.mllib.{ONNXPredictor => JONNXPredictor}
import com.netease.easyml.ml.param.{HasBatchSize, HasPath}
import org.apache.spark.ml.Transformer
import org.apache.spark.ml.param.shared.{HasInputCols, HasOutputCols}
import org.apache.spark.ml.param.{IntParam, ParamMap, ParamValidators, Params}
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Dataset, Row}

import scala.collection.JavaConversions._

/**
 * Created by linjiuning on 2020/7/27.
 */
trait ONNXParams extends Params with HasInputCols with HasOutputCols with HasBatchSize with HasPath {
  val logStep: IntParam = new IntParam(this, "logStep", "log every step", ParamValidators.gt(0))

  def getLogStep: Int = $(logStep)

  setDefault(logStep, 128)
}

class ONNXPredictor(override val uid: String) extends Transformer with ONNXParams with DefaultParamsWritable {

  import ONNXPredictor._

  def this() = this(Identifiable.randomUID("onnx"))

  def setInputCols(value: Array[String]): this.type = set(inputCols, value)

  def setBatchSize(value: Int): this.type = set(batchSize, value)

  def setPath(value: String): this.type = set(path, value)

  def setLogStep(value: Int): this.type = set(logStep, value)

  lazy val fields: Array[StructField] = {
    val model = getOrCreate($(path))
    val session = model.getSession
    val inputInfo = session.getInputInfo
    val outputInfo = session.getOutputInfo
    model.close()

    if (!isDefined(batchSize)) {
      val bsz = inputInfo.values()
        .map(_.getInfo)
        .filter(_.isInstanceOf[TensorInfo])
        .map(it => it.asInstanceOf[TensorInfo].getShape)
        .filter(_.nonEmpty)
        .map(_ (0))
        .min.toInt
      if (bsz == -1)
        set(batchSize, BATCH_SIZE)
      else
        set(batchSize, bsz)
    }

    val inputKeys = inputInfo.keySet().toSeq.toArray
    if (!isDefined(inputCols)) {
      set(inputCols, inputKeys)
    } else {
      require(inputKeys.toIndexedSeq == $(inputCols).toIndexedSeq, "inputCols must match the model inputKeys.")
    }

    set(outputCols, outputInfo.keySet().toSeq.toArray.sorted)

    $(outputCols).map(name => {
      val info = outputInfo.get(name)
      val dataType = valueInfoToDataType(info.getInfo)
      StructField(name, dataType)
    })
  }

  def predictOne(row: Row, model: JONNXPredictor, outputCols: Seq[String]): Row = {
    val params = rowToParams(row, model.getSession)
    val result = model.predict(params)
    val seq = outputCols.map(col => result.get(col))

    Row.fromSeq(row.toSeq ++ seq.map {
      case tmp: util.Map[_, _] =>
        tmp.toMap
      case value =>
        value
    })
  }

  def predictBatch(rows: Seq[Row], model: JONNXPredictor, outputCols: Seq[String]): Seq[Row] = {
    val params = rows.map(row => rowToParams(row, model.getSession))
    val result = model.predictBatch(params)

    rows.indices.map(i => {
      val seq = outputCols.map(col => result(i).get(col))
      Row.fromSeq(rows(i).toSeq ++ seq.map {
        case tmp: util.Map[_, _] =>
          tmp.toMap
        case value =>
          value
      })
    })
  }

  override def transform(dataset: Dataset[_]): DataFrame = {
    val rdd = dataset.toDF().rdd.mapPartitions(iter => {
      val model = getOrCreate($(path))
      val env = model.getEnv
      val session = model.getSession

      val outputCols = session.getOutputNames.toSeq.sorted
      var step = 0
      var startTime = System.currentTimeMillis
      if ($(batchSize) > 1) {
        iter.grouped($(batchSize)).flatMap(rows => {
          val resRows = predictBatch(rows, model, outputCols)
          step += resRows.length
          if (step >= $(logStep)) {
            val endTime = System.currentTimeMillis
            log.info(s"Cost ${endTime - startTime} / $step = ${(endTime - startTime) * 1.0 / step}ms")
            startTime = endTime
            step = step % $(logStep)
          }
          resRows
        })
      } else {
        iter.map(row => {
          val resRow = predictOne(row, model, outputCols)
          step += 1
          if (step >= $(logStep)) {
            val endTime = System.currentTimeMillis
            log.info(s"Cost ${endTime - startTime} / $step = ${(endTime - startTime) * 1.0 / step}ms")
            startTime = endTime
            step = step % $(logStep)
          }
          resRow
        })
      }
    })
    dataset.sparkSession.createDataFrame(rdd, transformSchema(dataset.schema))
  }

  override def copy(extra: ParamMap): ONNXPredictor = defaultCopy(extra)

  override def transformSchema(schema: StructType): StructType = {
    StructType(schema.fields ++ fields)
  }
}

object ONNXPredictor extends DefaultParamsReadable[ONNXPredictor] {
  // may encounter dimension not match problem if batch size > 1
  val BATCH_SIZE: Int = 1

  def getOrCreate(path: String): JONNXPredictor = synchronized {
    val model = ResourceManager.getOrCreate(path, new Supplier[JONNXPredictor] {
      override def get(): JONNXPredictor = {
        val modelPath = IOUtil.mayCopyHdfsToLocal(path)
        JONNXPredictor.builder()
          .setModelPath(modelPath)
          .build()
      }
    })
    if (model.getEnv.isClosed) {
      model.close()
      ResourceManager.remove(path)
      getOrCreate(path)
    } else {
      model
    }
  }

  def onnxJavaTypeToDataType(onnxJavaType: OnnxJavaType): DataType = {
    onnxJavaType match {
      case OnnxJavaType.BOOL =>
        BooleanType
      case OnnxJavaType.DOUBLE =>
        DoubleType
      case OnnxJavaType.FLOAT =>
        FloatType
      case OnnxJavaType.INT8 =>
        ByteType
      case OnnxJavaType.INT16 =>
        ShortType
      case OnnxJavaType.INT32 =>
        IntegerType
      case OnnxJavaType.INT64 =>
        LongType
      case OnnxJavaType.STRING =>
        StringType
    }
  }

  def onnxJavaTypeToScalaClass(onnxJavaType: OnnxJavaType): Class[_] = {
    onnxJavaType match {
      case OnnxJavaType.BOOL =>
        classOf[Boolean]
      case OnnxJavaType.DOUBLE =>
        classOf[Double]
      case OnnxJavaType.FLOAT =>
        classOf[Float]
      case OnnxJavaType.INT8 =>
        classOf[Byte]
      case OnnxJavaType.INT16 =>
        classOf[Short]
      case OnnxJavaType.INT32 =>
        classOf[Int]
      case OnnxJavaType.INT64 =>
        classOf[Long]
      case OnnxJavaType.STRING =>
        classOf[String]
    }
  }

  def valueInfoToDataType(valueInfo: ValueInfo): DataType = {
    valueInfo match {
      case tensorInfo: TensorInfo =>
        tensorInfoToDataType(tensorInfo)
      case mapInfo: MapInfo =>
        mapInfoToDataType(mapInfo)
      case sequenceInfo: SequenceInfo =>
        val dataType = if (sequenceInfo.sequenceOfMaps) {
          mapInfoToDataType(sequenceInfo.mapInfo)
        } else {
          onnxJavaTypeToDataType(sequenceInfo.sequenceType)
        }
        if (sequenceInfo.length > 1) {
          ArrayType(dataType)
        } else {
          dataType
        }
    }
  }

  def tensorInfoToDataType(tensorInfo: TensorInfo): DataType = {
    var dataType = onnxJavaTypeToDataType(tensorInfo.`type`)
    if (tensorInfo.isScalar || tensorInfo.getShape.length == 1) {
      dataType
    } else {
      tensorInfo.getShape.foreach(_ => dataType = ArrayType(dataType))
      dataType
    }
  }

  def mapInfoToDataType(mapInfo: MapInfo): DataType = {
    MapType(onnxJavaTypeToDataType(mapInfo.keyType), onnxJavaTypeToDataType(mapInfo.valueType))
  }

  def rowToParams(row: Row, session: OrtSession): JParams = {
    val params = new JParams
    session.getInputInfo.foreach(info => {
      val i = row.fieldIndex(info._1)
      params.put(info._1, row.get(i))
    })
    params
  }

  override def load(path: String): ONNXPredictor = super.load(path)
}
