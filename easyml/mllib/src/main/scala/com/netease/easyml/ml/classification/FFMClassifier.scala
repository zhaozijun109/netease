package com.netease.easyml.ml.classification

import com.intel.imllib.ffm.classification.{FFMModel => OldFFMModel, FFMWithAdag => MLLibFFM}
import com.netease.easyml.ml.param.HasVectorSize
import com.netease.easyml.ml.util.{DefaultEasyMLParamsReader, DefaultEasyMLParamsWriter}
import org.apache.hadoop.fs.Path
import org.apache.spark.ml.classification.{ProbabilisticClassificationModel, ProbabilisticClassifier}
import org.apache.spark.ml.linalg.{Vector, Vectors}
import org.apache.spark.ml.param._
import org.apache.spark.ml.param.shared._
import org.apache.spark.ml.util._
import org.apache.spark.sql.types.{DataType, StringType}
import org.apache.spark.sql.{Dataset, Row}
import org.apache.spark.storage.StorageLevel

/**
 * Created by linjiuning on 2020/7/1.
 */

trait FFMClassifierParams extends HasMaxIter with HasFitIntercept
  with HasStandardization with HasStepSize with HasSolver with HasVectorSize {

  setDefault(maxIter -> 100)

  setDefault(fitIntercept, true)

  setDefault(stepSize, 0.1)

  setDefault(standardization, true)

  setDefault(vectorSize, 8)

  setDefault(solver, "adagrad")

  val numField: IntParam = new IntParam(this, "numField", "number of fields of input data")

  def getNumField: Int = $(numField)

  val numFeature: IntParam = new IntParam(this, "numFeature", "number of features of input data")

  def getNumFeature: Int = $(numFeature)

  val fitLinear: BooleanParam = new BooleanParam(this, "fitLinear", "whether fit linear model in ffm")
  setDefault(fitLinear, true)

  def getFitLinear: Boolean = $(fitLinear)

  val random: BooleanParam = new BooleanParam(this, "random", "whether randomize data")
  setDefault(random -> false)

  def getRandom: Boolean = $(random)

  //  val oneWayReg: DoubleParam = new DoubleParam(this, "oneWayReg", "regularization params of 1-way interactions")
  //  setDefault(oneWayReg -> 0.001)
  //
  //  def getOneWayReg: Double = $(oneWayReg)

  val twoWayReg: DoubleParam = new DoubleParam(this, "twoWayReg", "regularization params of 2-way interactions")
  setDefault(twoWayReg -> 0.0002)

  def getTwoWayReg: Double = $(twoWayReg)

}

class FFMClassifier(override val uid: String)
  extends ProbabilisticClassifier[String, FFMClassifier, FFMModel]
    with FFMClassifierParams with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("FFM"))

  def setMaxIter(value: Int): this.type = set(maxIter, value)

  def setFitIntercept(value: Boolean): this.type = set(fitIntercept, value)

  def setStepSize(value: Double): this.type = set(stepSize, value)

  def setStandardization(value: Boolean): this.type = set(standardization, value)

  def setSolver(value: String): this.type = set(solver, value)

  def setNumField(value: Int): this.type = set(numField, value)

  def setNumFeature(value: Int): this.type = set(numFeature, value)

  def setFitLinear(value: Boolean): this.type = set(fitLinear, value)

  def setRandom(value: Boolean): this.type = set(random, value)

  def setVectorSize(value: Int): this.type = set(vectorSize, value)

  //  def setOneWayReg(value: Double): this.type = set(oneWayReg, value)

  def setTwoWayReg(value: Double): this.type = set(twoWayReg, value)

  override def copy(extra: ParamMap): FFMClassifier = defaultCopy(extra)

  override protected def train(dataset: Dataset[_]): FFMModel = {
    require($(solver).equals("adagrad") || $(solver).equals("sgd"), this.getClass.getSimpleName +
      ".train() called with non-matching solver." +
      s" solver=$solver, but only support adagrad and sgd yet.")

    val numClasses = getNumClasses(dataset)
    require(numClasses == 2, this.getClass.getSimpleName +
      ".train() called with non-matching numClasses." +
      s" numClasses=$numClasses, but only support binary classification yet.")

    val trainData = dataset.select($(labelCol), $(featuresCol)).rdd.mapPartitions(iter => {
      iter.map {
        case Row(label: Double, feature: String) =>
          val nLabel = if (label > 0) {
            1.0
          } else {
            -1.0
          }
          (nLabel, FFMModel.featureTransform(feature))
      }
    })

    val handlePersistence = trainData.getStorageLevel == StorageLevel.NONE
    if (handlePersistence)
      trainData.persist(StorageLevel.MEMORY_AND_DISK)

    if (!isDefined(numField)) {
      val m = trainData.flatMap(x => x._2).map(_._1).collect.max
      println(s"Set num field = $m")
      setNumField(m)
    }

    if (!isDefined(numFeature)) {
      val n = trainData.flatMap(x => x._2).map(_._2).collect.max
      println(s"Set num feature = $n")
      setNumFeature(n)
    }

    val model = MLLibFFM.train(
      trainData, $(numField), $(numFeature),
      ($(fitIntercept), $(fitLinear), $(vectorSize)),
      $(maxIter), $(stepSize), $(twoWayReg), $(standardization),
      $(random), $(solver))

    if (handlePersistence)
      trainData.unpersist()

    copyValues(new FFMModel(uid, model).setParent(this))
  }

  override def featuresDataType: DataType = StringType
}

object FFMClassifier extends DefaultParamsReadable[FFMClassifier] {

  override def load(path: String): FFMClassifier = super.load(path)
}

class FFMModel(override val uid: String,
               val model: OldFFMModel)
  extends ProbabilisticClassificationModel[String, FFMModel] with MLWritable {

  override val numFeatures: Int = model.numFeatures
  override val numClasses: Int = 2

  override def copy(extra: ParamMap): FFMModel = {
    copyValues(new FFMModel(uid, model).setParent(this.parent), extra)
  }

  override def write: MLWriter = new FFMModel.FFMModelWriter(this)

  override def predictRaw(features: String): Vector = {
    val input = FFMModel.featureTransform(features)
    val score = model.predict(input)
    Vectors.dense(Array(1.0 - score, score))
  }

  override protected def raw2probabilityInPlace(rawPrediction: Vector): Vector = {
    rawPrediction
  }

  override protected def featuresDataType: DataType = StringType
}

object FFMModel extends MLReadable[FFMModel] {
  // fieldId:featureId:value
  def featureTransform(feature: String): Array[(Int, Int, Double)] = {
    val x = feature.split("\\s")
    x.map(_.split(":")).map(x => {
      (x(0).toInt, x(1).toInt, x(2).toDouble)
    })
  }

  override def read: MLReader[FFMModel] = new FFMModelReader

  override def load(path: String): FFMModel = super.load(path)

  private[FFMModel] class FFMModelWriter(instance: FFMModel) extends MLWriter {

    override protected def saveImpl(path: String): Unit = {
      // Save metadata and Params
      DefaultEasyMLParamsWriter.saveMetadata(instance, path, sc)

      val dataPath = new Path(path, "data").toString
      instance.model.save(sparkSession.sparkContext, dataPath)
    }
  }

  private class FFMModelReader extends MLReader[FFMModel] {

    /** Checked against metadata when loading model */
    private val className = classOf[FFMModel].getName

    override def load(path: String): FFMModel = {
      val metadata = DefaultEasyMLParamsReader.loadMetadata(path, sc, className)

      val dataPath = new Path(path, "data").toString
      val fmModel = OldFFMModel.load(sparkSession.sparkContext, dataPath)

      val model = new FFMModel(metadata.uid, fmModel)

      DefaultEasyMLParamsReader.getAndSetParams(model, metadata)
      model
    }
  }

}