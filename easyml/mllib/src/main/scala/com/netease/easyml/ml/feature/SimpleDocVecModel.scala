package com.netease.easyml.ml.feature

import com.netease.easyml.common.util.IOUtil
import com.netease.easyml.ml.util.{DefaultEasyMLParamsReader, DefaultEasyMLParamsWriter, SchemaUtils}
import org.apache.hadoop.fs.Path
import org.apache.spark.ml.Model
import org.apache.spark.ml.linalg.{Vector, Vectors}
import org.apache.spark.ml.linalg_.BLAS
import org.apache.spark.ml.param.shared.{HasInputCol, HasOutputCol}
import org.apache.spark.ml.param.{BooleanParam, ParamMap, Params}
import org.apache.spark.ml.util._
import org.apache.spark.sql.functions.{col, udf}
import org.apache.spark.sql.types.{ArrayType, StringType, StructType}
import org.apache.spark.sql.{DataFrame, Dataset}

/**
 * Created by linjiuning on 2020/8/20.
 */
trait SimpleDocVecParams extends Params with HasInputCol with HasOutputCol {
  val divBySize: BooleanParam = new BooleanParam(this, "divBySize", "whether div by token size or weight sum")

  def getDivBySize: Boolean = $(divBySize)

  setDefault(divBySize -> false)

  protected def validateAndTransformSchema(schema: StructType): StructType = {
    SchemaUtils.checkColumnType(schema, $(inputCol), ArrayType(StringType))
    SchemaUtils.appendColumn(schema, $(outputCol), SchemaUtils.vectorUDT)
  }
}

class SimpleDocVecModel(override val uid: String,
                        val wordWeights: WordWeightModel,
                        val wordVectors: WordVecModel)
  extends Model[SimpleDocVecModel] with SimpleDocVecParams with MLWritable {

  import SimpleDocVecModel._

  def this(wordWeights: WordWeightModel, wordVectors: WordVecModel) = {
    this(Identifiable.randomUID("wv"), wordWeights, wordVectors)
  }

  def this(wordVectors: WordVecModel) = {
    this(null, wordVectors)
  }

  def setInputCol(value: String): this.type = set(inputCol, value)

  def setOutputCol(value: String): this.type = set(outputCol, value)

  def setDivBySize(value: Boolean): this.type = set(divBySize, value)

  def transformOne(sentence: Seq[String]): Vector = {
    val emptyVec = Vectors.sparse(wordVectors.vectorSize, Array.emptyIntArray, Array.emptyDoubleArray)
    if (sentence.isEmpty) {
      emptyVec
    } else {
      val sum = Vectors.zeros(wordVectors.vectorSize)
      var sumWt = 0.0
      sentence.foreach { word =>
        wordVectors.transformOne(word).foreach(v => {
          val wt = if (wordWeights == null) 1.0 else wordWeights.transformOne(word)
          sumWt += wt
          BLAS.axpy(wt, v, sum)
        })
      }
      val wt = if ($(divBySize)) sentence.size else sumWt
      BLAS.scal(1.0 / wt, sum)
      sum
    }
  }

  override def transform(dataset: Dataset[_]): DataFrame = {
    transformSchema(dataset.schema, logging = true)
    val bVectors = dataset.sparkSession.sparkContext.broadcast((wordWeights, wordVectors))
    val d = wordVectors.vectorSize
    val emptyVec = Vectors.sparse(d, Array.emptyIntArray, Array.emptyDoubleArray)
    val word2Vec = udf {
      sentence: Seq[String] =>
        if (sentence.isEmpty) {
          emptyVec
        } else {
          val (bWordWeights, bWordVectors) = bVectors.value
          val sum = Vectors.zeros(wordVectors.vectorSize)
          var sumWt = 0.0
          sentence.foreach { word =>
            bWordVectors.transformOne(word).foreach(v => {
              val wt = if (bWordWeights == null) 1.0 else bWordWeights.transformOne(word)
              sumWt += wt
              BLAS.axpy(wt, v, sum)
            })
          }
          val wt = if ($(divBySize)) sentence.size else sumWt
          if (wt != 0) {
            BLAS.scal(1.0 / wt, sum)
          }
          sum
        }
    }
    dataset.withColumn($(outputCol), word2Vec(col($(inputCol))))
  }

  override def transformSchema(schema: StructType): StructType = {
    validateAndTransformSchema(schema)
  }

  override def copy(extra: ParamMap): SimpleDocVecModel = defaultCopy(extra)

  override def write: MLWriter = new SimpleDocVecModelWriter(this)
}

object SimpleDocVecModel extends MLReadable[SimpleDocVecModel] {

  private class SimpleDocVecModelWriter(instance: SimpleDocVecModel) extends MLWriter {
    override protected def saveImpl(path: String): Unit = {
      DefaultEasyMLParamsWriter.saveMetadata(instance, path, sc)
      val dataPath = new Path(path, "data").toString
      if (instance.wordWeights != null) {
        val w2wPath = new Path(dataPath, "w2w").toString
        instance.wordWeights.save(w2wPath)
      }
      val w2vPath = new Path(dataPath, "w2v").toString
      instance.wordVectors.save(w2vPath)
    }
  }

  private class SimpleDocVecModelReader extends MLReader[SimpleDocVecModel] {

    private val className = classOf[SimpleDocVecModel].getName

    override def load(path: String): SimpleDocVecModel = {
      val metadata = DefaultEasyMLParamsReader.loadMetadata(path, sc, className)
      val dataPath = new Path(path, "data").toString
      val w2wPath = new Path(dataPath, "w2w").toString
      var wordWeights: WordWeightModel = null
      if (IOUtil.exists(w2wPath)) {
        wordWeights = WordWeightModel.load(w2wPath)
      }
      val w2vPath = new Path(dataPath, "w2v").toString
      val wordVectors = WordVecModel.load(w2vPath)
      val model = new SimpleDocVecModel(metadata.uid, wordWeights, wordVectors)
      DefaultEasyMLParamsReader.getAndSetParams(model, metadata)
      model
    }
  }

  override def read: MLReader[SimpleDocVecModel] = new SimpleDocVecModelReader

  override def load(path: String): SimpleDocVecModel = super.load(path)
}
