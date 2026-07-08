package com.netease.easyml.ml.transform

import java.util
import java.util.function.Supplier
import com.google.common.collect.Maps
import com.linkedin.spark.datasources.tfrecordv2.{TFRecordSerializer => Serializer}
import com.netease.easyml.common.resource.ResourceManager
import com.netease.easyml.common.util.{ConvertUtil, SparkUtil, TensorFlowUtil}
import com.netease.easyml.ml.param.{HasBatchSize, HasNumPartitions, HasPath}
import org.apache.spark.ml.Transformer
import org.apache.spark.ml.param._
import org.apache.spark.ml.param.shared.HasInputCols
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Dataset, Row}
import org.tensorflow.ndarray.Shape
import org.tensorflow.proto.framework.ConfigProto
import org.tensorflow.types.{TFloat32, TFloat64, TInt32, TInt64, TString}
import org.tensorflow.{SavedModelBundle, Signature}

import scala.collection.JavaConverters._

/**
 * Created by linjiuning on 2021/2/7.
 */

trait TensorFlowParams extends Params with HasInputCols with
  HasPath with HasNumPartitions with HasBatchSize {

  val debug = new Param[Boolean](this, "debug", "whether to log debug info")

  def getDebug: Boolean = $(debug)

  val dropInputs = new Param[Boolean](this, "dropInputs", "whether to drop inputs")

  def getDropInputs: Boolean = $(dropInputs)

  val numThreads = new IntParam(this, "numThreads", "torch parallelism, default spark.task.cpus", ParamValidators.gt(0))

  def getNumThreads: Int = $(numThreads)

  val recordType = new Param[String](this, "recordType", "input format of TensorFlow records. By default it is Example.", ParamValidators.inArray(Array("Example", "SequenceExample")))

  def getRecordType: String = $(recordType)

  val exampleName = new Param[String](this, "exampleName", "whether do serialize.")

  def getExampleName: String = $(exampleName)

  val signatureKey = new Param[String](this, "signatureKey", "signatureKey name of the {@code SignatureDef} in the saved model.")

  def getSignatureKey: String = $(signatureKey)

  setDefault(batchSize -> 32, debug -> true, dropInputs -> true, recordType -> "Example", exampleName -> "", signatureKey -> Signature.DEFAULT_KEY)
}

class TensorFlowPredictor(override val uid: String) extends Transformer
  with TensorFlowParams with DefaultParamsWritable {

  import TensorFlowPredictor._

  def this() = this(Identifiable.randomUID("tensorflow"))

  private var outputCols: Array[String] = _

  def setInputCols(value: Array[String]): this.type = set(inputCols, value)

  def setPath(value: String): this.type = set(path, value)

  def setBatchSize(value: Int): this.type = set(batchSize, value)

  def setDebug(value: Boolean): this.type = set(debug, value)

  def setDropInputs(value: Boolean): this.type = set(dropInputs, value)

  def setNumThreads(value: Int): this.type = set(numThreads, value)

  def setNumPartitions(value: Int): this.type = set(numPartitions, value)

  def setRecordType(value: String): this.type = set(recordType, value)

  def setExampleName(value: String): this.type = set(exampleName, value)

  def setSignatureKey(value: String): this.type = set(signatureKey, value)

  def getOutputCols: Array[String] = outputCols

  override def transform(dataset: Dataset[_]): DataFrame = {
    val structType = transformSchema(dataset.schema, logging = true)
    val conf = dataset.sparkSession.sparkContext.getConf
    val nThreads = if (isSet(numThreads)) {
      $(numThreads)
    } else {
      SparkUtil.getNumTaskCpus(conf)
    }

    val nPartitions = if (!isSet(numPartitions)) {
      4 * SparkUtil.getParallelism(conf)
    } else {
      getNumPartitions
    }

    val batchSize = getBatchSize

    val exportDir = getPath
    val debug = getDebug
    val dropInputs = getDropInputs
    val recordType = getRecordType
    val exampleName = getExampleName
    val signatureKey = getSignatureKey

    val cols = if (isSet(inputCols)) {
      getInputCols
    } else {
      dataset.columns
    }

    val names = dataset.schema.map(_.name)
    val inputIdx = cols.map(col => names.indexOf(col))
    val inputIdxSet = inputIdx.toSet
    val dropDumpIdxSet = names.filter(outputCols.contains).map(names.indexOf).toSet

    val serializer = if (exampleName.nonEmpty) {
      val fields = dataset.schema.fields
      val inputSchema = StructType(inputIdx.map(it => fields(it)))
      new Serializer(inputSchema)
    } else {
      null
    }

    val df = if (nPartitions > 0) {
      dataset.repartition(nPartitions)
    } else {
      dataset
    }

    val newRdd = df.toDF()
      .rdd
      .mapPartitions(iter => {
        val bundle = getOrCreate(exportDir, nThreads)
        val function = TensorFlowUtil.function(bundle, signatureKey)
        iter.grouped(batchSize).flatMap(rows => {
          val inputs = Maps.newHashMap[String, util.List[Object]]()
          if (serializer != null) {
            rows.foreach(row => {
              val nRow = Row.fromSeq(inputIdx.map(row.get))
              val record = recordType match {
                case "Example" =>
                  serializer.serializeExample(nRow).toByteArray
                case "SequenceExample" =>
                  serializer.serializeSequenceExample(nRow).toByteArray
                case _ =>
                  throw new IllegalArgumentException(s"Unsupported recordType ${recordType}: recordType can be Example or SequenceExample")
              }

              if (!inputs.containsKey(exampleName)) {
                inputs.put(exampleName, new util.ArrayList[Object]())
              }
              inputs.get(exampleName).add(record.asInstanceOf[Object])
            })
          } else {
            rows.foreach(row => {
              cols.foreach(col => {
                if (!inputs.containsKey(col)) {
                  inputs.put(col, new util.ArrayList[Object]())
                }
                val obj = ConvertUtil.mayNestToArray(row.getAs[Any](col))
                inputs.get(col).add(obj.asInstanceOf[Object])
              })
            })
          }
          val startTime = if (debug) {
            System.currentTimeMillis
          } else {
            0
          }
          val outputs = TensorFlowUtil.call(function, inputs)
          if (debug) {
            val endTime = System.currentTimeMillis
            val msg = s"COST: ${endTime - startTime} / ${rows.size} = ${(endTime - startTime) * 1.0 / rows.size}ms"
            log.info(msg)
          }
          rows.zipWithIndex.map {
            case (row, i) =>
              val seq = if (dropInputs) {
                row.toSeq.zipWithIndex
                  .filterNot(it => inputIdxSet.contains(it._2) || dropDumpIdxSet.contains(it._2)).map(_._1)
              } else {
                row.toSeq.zipWithIndex.filterNot(it => dropDumpIdxSet.contains(it._2)).map(_._1)
              }
              val array = outputCols.map(col => outputs.get(col).get(i))
              Row.fromSeq(seq ++ array)
          }
        })
      })
    dataset.sparkSession.createDataFrame(newRdd, structType)
  }

  override def copy(extra: ParamMap): Transformer = defaultCopy(extra)

  override def transformSchema(schema: StructType): StructType = {
    val bundle = getOrCreate(getPath, 1)
    val function = TensorFlowUtil.function(bundle, getSignatureKey)
    val outputDataTypes = TensorFlowUtil.getOutputDataTypes(function)
    val outputShapes = TensorFlowUtil.getOutputShapes(function)
    outputCols = outputDataTypes.keySet().asScala.toArray

    val cols = if (isSet(inputCols)) {
      getInputCols
    } else {
      schema.fields.map(_.name)
    }

    val dropInputs = getDropInputs

    val resultField = outputCols.map(col => {
      val dtype = outputDataTypes.get(col)
      val shape = outputShapes.get(col)
      outputSchemaField(col, dtype, shape)
    })

    var fields = if (dropInputs) {
      schema.fields.filter(field => !cols.contains(field.name))
    } else {
      schema.fields
    }
    fields = fields.filter(field => !outputCols.contains(field.name))
    StructType(fields ++ resultField)
  }
}

object TensorFlowPredictor extends DefaultParamsReadable[TensorFlowPredictor] {

  def getOrCreate(exportDir: String, numThreads: Int): SavedModelBundle = synchronized {
    ResourceManager.getOrCreate(exportDir, () => {
      val config = ConfigProto.newBuilder
        .setInterOpParallelismThreads(numThreads)
        .setIntraOpParallelismThreads(numThreads)
        .build
      SavedModelBundle.loader(exportDir).withTags(TensorFlowUtil.DEFAULT_TAGS)
        .withConfigProto(config).load
    })
  }

  def outputSchemaField(name: String, dataType: org.tensorflow.DataType[_], shape: Shape): StructField = {
    val dtype = dataType.name() match {
      case TInt32.NAME =>
        IntegerType
      case TInt64.NAME =>
        LongType
      case TFloat32.NAME =>
        FloatType
      case TFloat64.NAME =>
        DoubleType
      case TString.NAME =>
        StringType
      case _ =>
        throw new IllegalArgumentException(s"dataType ${dataType.name()} is not valid")
    }
    if (shape.numDimensions() < 2) {
      StructField(name, dtype)
    } else {
      var arrayType = ArrayType(dtype)
      (2 until shape.numDimensions()).foreach(_ => arrayType = ArrayType(arrayType))
      StructField(name, arrayType)
    }
  }

  override def load(path: String): TensorFlowPredictor = super.load(path)
}