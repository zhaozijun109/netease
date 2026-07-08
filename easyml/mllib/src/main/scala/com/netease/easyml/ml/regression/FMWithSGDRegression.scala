package com.netease.easyml.ml.regression

import com.intel.imllib.fm.regression.{FMModel, FMWithSGD}
import com.netease.easyml.ml.classification.FMWithSGDParams
import com.netease.easyml.ml.util.{DefaultEasyMLParamsReader, DefaultEasyMLParamsWriter}
import org.apache.hadoop.fs.Path
import org.apache.spark.ml.Predictor
import org.apache.spark.ml.linalg.Vector
import org.apache.spark.ml.param._
import org.apache.spark.ml.regression.RegressionModel
import org.apache.spark.ml.util._
import org.apache.spark.mllib.linalg.{Vectors => OldVectors}
import org.apache.spark.mllib.regression.{LabeledPoint => OldLabeledPoint}
import org.apache.spark.sql.Dataset
import org.apache.spark.storage.StorageLevel

/**
 * Created by linjiuning on 2020/7/1.
 */
class FMWithSGDRegression(override val uid: String)
  extends Predictor[Vector, FMWithSGDRegression, FMWithSGDRegressionModel]
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

  override def copy(extra: ParamMap): FMWithSGDRegression = defaultCopy(extra)

  override protected def train(dataset: Dataset[_]): FMWithSGDRegressionModel = {
    val trainData = extractLabeledPoints(dataset)
      .map(it => OldLabeledPoint(it.label, OldVectors.fromML(it.features)))

    val handlePersistence = trainData.getStorageLevel == StorageLevel.NONE
    if (handlePersistence)
      trainData.persist(StorageLevel.MEMORY_AND_DISK)

    val fMModel = FMWithSGD.train(trainData, 0, $(maxIter), $(stepSize),
      ($(fitIntercept), $(fitLinear), $(vectorSize)),
      ($(interceptReg), $(oneWayReg), $(twoWayReg)), $(initStd)
    )

    if (handlePersistence)
      trainData.unpersist()
    copyValues(new FMWithSGDRegressionModel(uid, fMModel).setParent(this))
  }
}

object FMWithSGDClassifier extends DefaultParamsReadable[FMWithSGDRegression] {
  override def load(path: String): FMWithSGDRegression = super.load(path)
}

class FMWithSGDRegressionModel(override val uid: String,
                               val model: FMModel)
  extends RegressionModel[Vector, FMWithSGDRegressionModel]
    with MLWritable {

  override val numFeatures: Int = model.numFeatures

  override def copy(extra: ParamMap): FMWithSGDRegressionModel = {
    copyValues(new FMWithSGDRegressionModel(uid, model).setParent(this.parent), extra)
  }

  override def write: MLWriter = new FMWithSGDRegressionModel.FMWithSGDModelWriter(this)

  override def predict(features: Vector): Double = {
    model.predict(OldVectors.fromML(features))
  }
}

object FMWithSGDRegressionModel extends MLReadable[FMWithSGDRegressionModel] {

  override def read: MLReader[FMWithSGDRegressionModel] = new FMWithSGDModelReader

  override def load(path: String): FMWithSGDRegressionModel = super.load(path)

  private[FMWithSGDRegressionModel] class FMWithSGDModelWriter(instance: FMWithSGDRegressionModel) extends MLWriter {

    override protected def saveImpl(path: String): Unit = {
      // Save metadata and Params
      DefaultEasyMLParamsWriter.saveMetadata(instance, path, sc)

      val dataPath = new Path(path, "data").toString
      instance.model.save(sparkSession.sparkContext, dataPath)
    }
  }

  private class FMWithSGDModelReader extends MLReader[FMWithSGDRegressionModel] {

    /** Checked against metadata when loading model */
    private val className = classOf[FMWithSGDRegressionModel].getName

    override def load(path: String): FMWithSGDRegressionModel = {
      val metadata = DefaultEasyMLParamsReader.loadMetadata(path, sc, className)

      val dataPath = new Path(path, "data").toString
      val fmModel = FMModel.load(sparkSession.sparkContext, dataPath)

      val model = new FMWithSGDRegressionModel(metadata.uid, fmModel)

      DefaultEasyMLParamsReader.getAndSetParams(model, metadata)
      model
    }
  }

}