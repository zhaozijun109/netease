package com.netease.easyml.ml.transform

import com.netease.easyml.common.util.SparkUtil
import com.netease.easyml.ml.param.{HasBatchSize, HasNumPartitions, HasPath}
import com.tencent.miaobinlp.SparkPredictManager
import com.tencent.miaobinlp.backend.BackendType
import org.apache.spark.ml.Transformer
import org.apache.spark.ml.param._
import org.apache.spark.ml.param.shared.{HasFeaturesCol, HasOutputCol}
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, Dataset}

/**
 * Created by linjiuning on 2020/9/21.
 */

trait MiaobiNLPParams extends Params with HasFeaturesCol with HasOutputCol with
  HasPath with HasNumPartitions with HasBatchSize {
  val sortByLength = new Param[Boolean](
    this, "sortByLength", "whether sort the features by length")

  def getSortByLength: Boolean = $(sortByLength)

  val predictor = new Param[String](
    this, "predictor", "type of predictor")

  def getPredictor: String = $(predictor)

  val useDatasetReader = new Param[Boolean](
    this, "useDatasetReader", "whether use dataset reader to parse features")

  def getUseDatasetReader: Boolean = $(useDatasetReader)

  val datasetReaderChoice = new Param[String](
    this, "datasetReaderChoice", "dataset reader choice")

  def getDatasetReaderChoice: String = $(datasetReaderChoice)

  val backend = new Param[String](
    this, "backend", "type of backend", (value: String) => Array("auto", "t4j", "djl").contains(value.toLowerCase()))

  def getBackend: String = $(backend)

  val debug = new Param[Boolean](this, "debug", "whether to log debug info")

  def getDebug: Boolean = $(debug)

  val numThreads = new IntParam(this, "numThreads", "torch parallelism, default spark.task.cpus", ParamValidators.gt(0))

  def getNumThreads: Int = $(numThreads)

  val jniFile = new Param[String](this, "jniFile", "path of jni jar or dir of so")

  def getJniFile: String = $(jniFile)

  val weightFile = new Param[String](this, "weightFile", "path of weight file")

  def getWeightFile: String = $(weightFile)

  val numSamplesPerTask = new IntParam(this, "numSamplesPerTask", "num samples perTask", ParamValidators.gt(0))

  def getNumSamplesPerTask: Int = $(numSamplesPerTask)

  setDefault(sortByLength -> true, backend -> "auto", predictor -> "text_classifier", datasetReaderChoice -> "validation", useDatasetReader -> true,
    batchSize -> 32, debug -> true, jniFile -> "", weightFile -> "", outputCol -> "json")
}

class MiaobiNLPPredictor(override val uid: String) extends Transformer
  with MiaobiNLPParams with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("miaobinlp"))

  def setFeaturesCol(value: String): this.type = set(featuresCol, value)

  def setOutputCol(value: String): this.type = set(outputCol, value)

  def setSortByLength(value: Boolean): this.type = set(sortByLength, value)

  def setPredictor(value: String): this.type = set(predictor, value)

  def setDatasetReaderChoice(value: String): this.type = set(datasetReaderChoice, value)

  def setBackend(value: String): this.type = set(backend, value)

  def setPath(value: String): this.type = set(path, value)

  def setBatchSize(value: Int): this.type = set(batchSize, value)

  def setUseDatasetReader(value: Boolean): this.type = set(useDatasetReader, value)

  def setDebug(value: Boolean): this.type = set(debug, value)

  def setNumThreads(value: Int): this.type = set(numThreads, value)

  def setJniFile(value: String): this.type = set(jniFile, value)

  def setWeightFile(value: String): this.type = set(weightFile, value)

  def setNumPartitions(value: Int): this.type = set(numPartitions, value)

  def setNumSamplesPerTask(value: Int): this.type = set(numSamplesPerTask, value)

  override def transform(dataset: Dataset[_]): DataFrame = {
    val conf = dataset.sparkSession.sparkContext.getConf
    val nThreads = if (isSet(numThreads)) {
      $(numThreads)
    } else {
      SparkUtil.getNumTaskCpus(conf)
    }

    val nPartitions = if (!isSet(numPartitions) && !isSet(numSamplesPerTask)) {
      4 * SparkUtil.getParallelism(conf)
    } else {
      getNumPartitions
    }

    val nSamplesPerTask = if (isSet(numSamplesPerTask)) $(numSamplesPerTask) else 0

    val manager = SparkPredictManager.builder()
      .backend(BackendType.of($(backend).toUpperCase))
      .jniFile($(jniFile))
      .archiveFile($(path))
      .weightsFile($(weightFile))
      .numThreads(nThreads)
      .datasetReaderChoice($(datasetReaderChoice))
      .useDatasetReader($(useDatasetReader))
      .predictor($(predictor))
      .batchSize($(batchSize))
      .debug($(debug))
      .build()

    val texts = dataset.toDF().select($(featuresCol)).rdd
      .map(row => Option(row.getString(0)).getOrElse(""))
      .filter(_.nonEmpty)
      .distinct()

    val result = manager.predict(texts, numPartitions = nPartitions, numSamplesPerTask = nSamplesPerTask, sortByLength = $(sortByLength))

    import dataset.sparkSession.implicits._
    val resultDf = result.toDF($(featuresCol), $(outputCol))

    dataset.alias("t1")
      .join(resultDf.alias("t2"), dataset($(featuresCol)) === resultDf($(featuresCol)))
      .select("t1.*", s"t2.${$(outputCol)}")
  }

  override def copy(extra: ParamMap): Transformer = defaultCopy(extra)

  override def transformSchema(schema: StructType): StructType = {
    val resultField = StructField($(outputCol), StringType)
    StructType(schema.fields :+ resultField)
  }
}

object MiaobiNLPPredictor extends DefaultParamsReadable[MiaobiNLPPredictor] {

  override def load(path: String): MiaobiNLPPredictor = super.load(path)
}