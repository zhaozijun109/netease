package com.netease.easyml.ml.classification

import com.intel.imllib.fm.regression.{FMModel, FMWithSGD}
import com.netease.easyml.ml.param.HasVectorSize
import com.netease.easyml.ml.util.{DefaultEasyMLParamsReader, DefaultEasyMLParamsWriter}
import org.apache.hadoop.fs.Path
import org.apache.spark.ml.classification.{ProbabilisticClassificationModel, ProbabilisticClassifier}
import org.apache.spark.ml.linalg.{Vector, Vectors}
import org.apache.spark.ml.param._
import org.apache.spark.ml.param.shared.{HasFitIntercept, HasMaxIter, HasStepSize}
import org.apache.spark.ml.util._
import org.apache.spark.mllib.linalg.{Vectors => OldVectors}
import org.apache.spark.mllib.regression.{LabeledPoint => OldLabeledPoint}
import org.apache.spark.sql.Dataset
import org.apache.spark.storage.StorageLevel

/**
 * Created by linjiuning on 2020/7/1.
 */
trait FMWithSGDParams extends HasMaxIter with HasStepSize with HasFitIntercept with HasVectorSize {
  setDefault(maxIter -> 100)

  setDefault(fitIntercept, true)

  setDefault(stepSize, 1.0)

  setDefault(vectorSize, 8)

  val fitLinear = new BooleanParam(this, "fitLinear", "whether fit linear model in ffm")
  setDefault(fitLinear, true)

  def getFitLinear: Boolean = $(fitLinear)

  val initStd: DoubleParam = new DoubleParam(this, "initStd", "standard deviation used for factorization matrix initialization.", ParamValidators.gt(0))
  setDefault(initStd, 0.01)

  def getInitStd: Double = $(initStd)

  val interceptReg = new DoubleParam(this, "interceptReg", "regularization params of intercept")
  setDefault(interceptReg -> 0)

  def getInterceptReg: Double = $(interceptReg)

  val oneWayReg = new DoubleParam(this, "oneWayReg", "regularization params of 1-way interactions")
  setDefault(oneWayReg -> 0.001)

  def getOneWayReg: Double = $(oneWayReg)

  val twoWayReg = new DoubleParam(this, "twoWayReg", "regularization params of 2-way interactions")
  setDefault(twoWayReg -> 0.0001)

  def getTwoWayReg: Double = $(twoWayReg)
}

class FMWithSGDClassifier(override val uid: String)
  extends ProbabilisticClassifier[Vector, FMWithSGDClassifier, FMWithSGDModel]
    with FMWithSGDParams with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("FM"))

  def setMaxIter(value: Int): this.type = set(maxIter, value)

  def setStepSize(value: Double): this.type = set(stepSize, value)

  def setFitIntercept(value: Boolean): this.type = set(fitIntercept, value)

  def setFitLinear(value: Boolean): this.type = set(fitLinear, value)

  def setVectorSize(value: Int): this.type = set(vectorSize, value)

  def setInterceptReg(value: Double): this.type = set(interceptReg, value)

  def setOneWayReg(value: Double): this.type = set(oneWayReg, value)

  def setTwoWayReg(value: Double): this.type = set(twoWayReg, value)

  def setInitStd(value: Double): this.type = set(initStd, value)

  override def copy(extra: ParamMap): FMWithSGDClassifier = defaultCopy(extra)

  override protected def train(dataset: Dataset[_]): FMWithSGDModel = {
    val numClasses = getNumClasses(dataset)
    require(numClasses == 2, this.getClass.getSimpleName +
      ".train() called with non-matching numClasses." +
      s" numClasses=$numClasses, but only support binary classification yet.")
    val trainData = extractLabeledPoints(dataset)
      .map(it => {
        val label = if (it.label > 0) {
          1.0
        } else {
          -1.0
        }
        OldLabeledPoint(label, OldVectors.fromML(it.features))
      })

    val handlePersistence = trainData.getStorageLevel == StorageLevel.NONE
    if (handlePersistence)
      trainData.persist(StorageLevel.MEMORY_AND_DISK)

    val fMModel = FMWithSGD.train(trainData, 1, $(maxIter), $(stepSize),
      ($(fitIntercept), $(fitLinear), $(vectorSize)),
      ($(interceptReg), $(oneWayReg), $(twoWayReg)), $(initStd)
    )

    if (handlePersistence)
      trainData.unpersist()
    copyValues(new FMWithSGDModel(uid, fMModel).setParent(this))
  }
}

object FMWithSGDClassifier extends DefaultParamsReadable[FMWithSGDClassifier] {
  override def load(path: String): FMWithSGDClassifier = super.load(path)
}

class FMWithSGDModel(override val uid: String,
                     val model: FMModel)
  extends ProbabilisticClassificationModel[Vector, FMWithSGDModel]
    with MLWritable {

  override val numFeatures: Int = model.numFeatures
  override val numClasses: Int = 2

  override def copy(extra: ParamMap): FMWithSGDModel = {
    copyValues(new FMWithSGDModel(uid, model).setParent(this.parent), extra)
  }

  override def write: MLWriter = new FMWithSGDModel.FMWithSGDModelWriter(this)

  override def predictRaw(features: Vector): Vector = {
    val score = model.predict(OldVectors.fromML(features))
    Vectors.dense(Array(1 - score, score))
  }

  override protected def raw2probabilityInPlace(rawPrediction: Vector): Vector = {
    rawPrediction
  }
}

object FMWithSGDModel extends MLReadable[FMWithSGDModel] {

  override def read: MLReader[FMWithSGDModel] = new FMWithSGDModelReader

  override def load(path: String): FMWithSGDModel = super.load(path)

  private[FMWithSGDModel] class FMWithSGDModelWriter(instance: FMWithSGDModel) extends MLWriter {

    override protected def saveImpl(path: String): Unit = {
      // Save metadata and Params
      DefaultEasyMLParamsWriter.saveMetadata(instance, path, sc)

      val dataPath = new Path(path, "data").toString
      instance.model.save(sparkSession.sparkContext, dataPath)
    }
  }

  private class FMWithSGDModelReader extends MLReader[FMWithSGDModel] {

    /** Checked against metadata when loading model */
    private val className = classOf[FMWithSGDModel].getName

    override def load(path: String): FMWithSGDModel = {
      val metadata = DefaultEasyMLParamsReader.loadMetadata(path, sc, className)

      val dataPath = new Path(path, "data").toString
      val fmModel = FMModel.load(sparkSession.sparkContext, dataPath)

      val model = new FMWithSGDModel(metadata.uid, fmModel)

      DefaultEasyMLParamsReader.getAndSetParams(model, metadata)
      model
    }
  }

}