package com.netease.easyml.ml.transform

import java.util
import java.util.function.Supplier

import com.google.common.collect.Lists
import com.netease.easyml.common.resource.ResourceManager
import com.netease.easyml.common.util.{ArrayUtil, ConvertUtil, SparkUtil, TorchUtil}
import com.netease.easyml.ml.param.{HasBatchSize, HasNumPartitions, HasPath}
import com.tencent.miaobinlp.backend.{Backends, Graph}
import org.apache.spark.ml.Transformer
import org.apache.spark.ml.param._
import org.apache.spark.ml.param.shared.{HasInputCols, HasOutputCols}
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Dataset, Row}
import org.apache.spark.storage.StorageLevel

/**
 * Created by linjiuning on 2021/2/17.
 */

trait TorchParams extends Params with HasInputCols with HasOutputCols with
  HasPath with HasNumPartitions with HasBatchSize {

  val inputDtypes: StringArrayParam = new StringArrayParam(this, "inputDtypes", "input data types")

  def getInputDtypes: Array[String] = $(inputDtypes)

  val debug = new Param[Boolean](this, "debug", "whether to log debug info")

  def getDebug: Boolean = $(debug)

  val dropInputs = new Param[Boolean](this, "dropInputs", "whether to drop inputs")

  def getDropInputs: Boolean = $(dropInputs)

  val numThreads = new IntParam(this, "numThreads", "torch parallelism, default spark.task.cpus", ParamValidators.gt(0))

  def getNumThreads: Int = $(numThreads)

  val backend = new Param[String](
    this, "backend", "type of backend", (value: String) => Array("auto", "t4j", "djl").contains(value.toLowerCase()))

  def getBackend: String = $(backend)

  setDefault(batchSize -> 32, debug -> true, dropInputs -> true, backend -> "t4j")
}

class TorchPredictor(override val uid: String) extends Transformer
  with TorchParams with DefaultParamsWritable {

  import TorchPredictor._

  def this() = this(Identifiable.randomUID("torch"))

  private var rows: Array[Row] = _
  private var nThreads: Int = _

  def setInputCols(value: Array[String]): this.type = set(inputCols, value)

  def setOutputCols(value: Array[String]): this.type = set(outputCols, value)

  def setInputDtypes(value: Array[String]): this.type = set(inputDtypes, value)

  def setBackend(value: String): this.type = set(backend, value)

  def setPath(value: String): this.type = set(path, value)

  def setBatchSize(value: Int): this.type = set(batchSize, value)

  def setDebug(value: Boolean): this.type = set(debug, value)

  def setDropInputs(value: Boolean): this.type = set(dropInputs, value)

  def setNumThreads(value: Int): this.type = set(numThreads, value)

  def setNumPartitions(value: Int): this.type = set(numPartitions, value)

  def call(graph: Graph, rows: Seq[Row], cols: Array[String], dtypes: Array[String], debug: Boolean): util.List[util.List[AnyRef]] = {
    val inputs = Lists.newArrayList[util.List[Object]]()
    rows.foreach(row => {
      cols.zipWithIndex.foreach {
        case (col, i) =>
          if (i >= inputs.size()) {
            inputs.add(new util.ArrayList[Object]())
          }
          val obj = ConvertUtil.mayNestToArray(row.getAs[Any](col))
          inputs.get(i).add(obj.asInstanceOf[Object])
      }
    })
    val startTime = if (debug) {
      System.currentTimeMillis
    } else {
      0
    }
    val outputs = TorchUtil.call(graph, inputs, dtypes)
    if (debug) {
      val endTime = System.currentTimeMillis
      val msg = s"COST: ${endTime - startTime} / ${rows.size} = ${(endTime - startTime) * 1.0 / rows.size}ms"
      log.info(msg)
    }
    outputs
  }

  override def transform(dataset: Dataset[_]): DataFrame = {
    val handlePersistence = dataset.storageLevel == StorageLevel.NONE

    if (handlePersistence) dataset.persist(StorageLevel.MEMORY_AND_DISK)

    rows = dataset.toDF().take(getBatchSize)

    val structType = transformSchema(dataset.schema, logging = true)
    val conf = dataset.sparkSession.sparkContext.getConf
    nThreads = if (isSet(numThreads)) {
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

    val backend = getBackend
    val exportDir = getPath
    val cols = getInputCols
    val debug = getDebug
    val dropInputs = getDropInputs
    val outputCols = getOutputCols

    val dtypes = if (isSet(inputDtypes)) {
      getInputDtypes
    } else {
      Array.empty[String]
    }

    val names = dataset.schema.map(_.name)
    val inputIdx = cols.map(col => names.indexOf(col)).toSet

    val df = if (nPartitions > 0) {
      dataset.repartition(nPartitions)
    } else {
      dataset
    }

    val newRdd = df.toDF()
      .rdd
      .mapPartitions(iter => {
        val graph = getOrCreate(backend, exportDir, nThreads)
        iter.grouped(batchSize).flatMap(rows => {
          val outputs = call(graph, rows, cols, dtypes, debug)
          rows.zipWithIndex.map {
            case (row, i) =>
              val seq = if (dropInputs) {
                row.toSeq.zipWithIndex.filter(it => !inputIdx.contains(it._2)).map(_._1)
              } else {
                row.toSeq
              }
              val array = outputCols.indices.map(j => outputs.get(j).get(i))
              Row.fromSeq(seq ++ array)
          }
        })
      })
    println(structType)
    if (handlePersistence) SparkUtil.unpersist(dataset)
    dataset.sparkSession.createDataFrame(newRdd, structType)
  }

  override def copy(extra: ParamMap): Transformer = defaultCopy(extra)

  override def transformSchema(schema: StructType): StructType = {
    val graph = getOrCreate(getBackend, getPath, nThreads)
    val inputCols = getInputCols
    val dtypes = if (isSet(inputDtypes)) {
      getInputDtypes
    } else {
      Array.empty[String]
    }
    val outputs = call(graph, rows, inputCols, dtypes, false)
    val outputCols = getOutputCols
    val outputDataTypes = outputCols.indices.map(i => ArrayUtil.componentType(outputs.get(i).get(0)).getSimpleName.toUpperCase)
    val outputDims = outputCols.indices.map(i => ArrayUtil.dim(outputs.get(i).get(0)))

    val cols = getInputCols.toSet
    val dropInputs = getDropInputs

    val resultField = outputCols.indices.map(i => {
      val col = outputCols(i)
      val dtype = outputDataTypes(i)
      val shape = outputDims(i)
      outputSchemaField(col, dtype, shape)
    })

    val fields = if (dropInputs) {
      schema.fields.filter(field => !cols.contains(field.name))
    } else {
      schema.fields
    }
    StructType(fields ++ resultField)
  }
}

object TorchPredictor extends DefaultParamsReadable[TorchPredictor] {

  def getOrCreate(backend: String, exportDir: String, numThreads: Int): Graph = synchronized {
    ResourceManager.getOrCreate(exportDir, new Supplier[Graph] {
      override def get(): Graph = {
        Backends.setup(backend)

        try {
          if (numThreads > 0) {
            Backends.setNumThreads(numThreads)
            Backends.setNumInteropThreads(numThreads)
          }
          else {
            Backends.initNumThreads()
          }
        } catch {
          case e: UnsatisfiedLinkError =>
            println(s"UnsatisfiedLinkError ${e.getMessage}")
        }
        Backends.load(exportDir)
      }
    })
  }

  def outputSchemaField(name: String, dataType: String, dim: Int): StructField = {
    val dtype = dataType match {
      case "INT" =>
        IntegerType
      case "LONG" =>
        LongType
      case "FLOAT" =>
        FloatType
      case "DOUBLE" =>
        DoubleType
      case _ =>
        throw new IllegalArgumentException(s"dataType $dataType is not valid")
    }
    if (dim < 1) {
      StructField(name, dtype)
    } else {
      var arrayType = ArrayType(dtype)
      (1 until dim).foreach(_ => arrayType = ArrayType(arrayType))
      StructField(name, arrayType)
    }
  }

  override def load(path: String): TorchPredictor = super.load(path)
}