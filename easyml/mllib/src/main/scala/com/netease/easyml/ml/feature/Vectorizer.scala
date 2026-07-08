package com.netease.easyml.ml.feature

import com.alibaba.fastjson.JSON
import com.netease.easyml.common.util.ConvertUtil
import com.netease.easyml.ml.util.{DefaultEasyMLParamsReader, DefaultEasyMLParamsWriter, SchemaUtils}
import org.apache.hadoop.fs.Path
import org.apache.spark.SparkException
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.ml.linalg.{DenseVector, SparseVector, Vector, Vectors}
import org.apache.spark.ml.param._
import org.apache.spark.ml.param.shared.{HasInputCol, HasOutputCol}
import org.apache.spark.ml.util._
import org.apache.spark.ml.{Estimator, Model}
import org.apache.spark.sql.functions.{col, udf}
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Dataset}

import scala.collection.JavaConverters._

/**
 * Created by linjiuning on 2020/10/29.
 * Convert to Vector
 */
trait VectorizerParams extends Params with HasInputCol with HasOutputCol {
  final val sparse: BooleanParam = new BooleanParam(this, "sparse", "whether return sparse vector")

  final def getSparse: Boolean = $(sparse)

  final val json: BooleanParam = new BooleanParam(this, "json", "whether json string")

  final def getJson: Boolean = $(json)

  val handleUnknown: Param[String] = new Param[String](this, "handleUnknown",
    "Whether to raise an error or ignore if an unknown categorical feature is present during transform (default is to raise). " +
      "When this parameter is set to ‘ignore’ and an unknown category is encountered during transform, the resulting one-hot encoded columns for this feature will be all zeros. " +
      "In the inverse transform, an unknown category will be denoted as None.",
    ParamValidators.inArray(Array("ignore", "error")))

  def getHandleUnknown: String = $(handleUnknown)

  setDefault(sparse -> false, json -> false, handleUnknown -> "ignore")

  /**
   * Validate and transform the input schema.
   */
  protected def validateAndTransformSchema(schema: StructType): StructType = {
    val actualDataType = schema($(inputCol)).dataType
    require(SchemaUtils.isNumericType(schema, $(inputCol)) ||
      SchemaUtils.isColumnType(schema, $(inputCol), StringType) ||
      SchemaUtils.isArrayType(schema, $(inputCol)) ||
      SchemaUtils.isVectorType(schema, $(inputCol)) ||
      actualDataType.isInstanceOf[MapType], s"Column $getInputCol must be of type " +
      s"NumericType, MapType, StringType, ArrayType or VectorType but was actually of type $actualDataType")

    SchemaUtils.appendColumn(schema, $(outputCol), SchemaUtils.vectorUDT)
  }
}

class Vectorizer(override val uid: String) extends Estimator[VectorizerModel]
  with VectorizerParams with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("vectorizer"))

  def setInputCol(value: String): this.type = set(inputCol, value)

  def setOutputCol(value: String): this.type = set(outputCol, value)

  def setSparse(value: Boolean): this.type = set(sparse, value)

  def setJson(value: Boolean): this.type = set(json, value)

  def setHandleUnknown(value: String): this.type = set(handleUnknown, value)

  override def transformSchema(schema: StructType): StructType = {
    validateAndTransformSchema(schema)
  }

  override def fit(dataset: Dataset[_]): VectorizerModel = {
    val schema = dataset.schema
    transformSchema(schema, logging = true)

    val actualDataType = schema($(inputCol)).dataType

    val vocabulary = if (actualDataType.isInstanceOf[MapType]) {
      dataset.select($(inputCol))
        .filter(col($(inputCol)).isNotNull)
        .toDF().rdd.flatMap(row => {
        row.getMap[Any, Any](0).keys.map(ConvertUtil.toString)
      }).distinct().collect().sorted
    } else if ($(json) && actualDataType.isInstanceOf[StringType]) {
      dataset.select($(inputCol))
        .filter(col($(inputCol)).isNotNull)
        .toDF().rdd.flatMap(row => {
        val json = Option(row.getString(0)).getOrElse("")
        if (json.nonEmpty) {
          JSON.parseObject(json).asScala.keys.map(ConvertUtil.toString)
        } else {
          Array.empty[String]
        }
      }).distinct().collect().sorted
    } else {
      Array.empty[String]
    }

    copyValues(new VectorizerModel(vocabulary).setParent(this))
  }

  override def copy(extra: ParamMap): Estimator[VectorizerModel] = defaultCopy(extra)
}

object Vectorizer extends DefaultParamsReadable[Vectorizer] {

  override def load(path: String): Vectorizer = super.load(path)
}

class VectorizerModel(override val uid: String, val vocabulary: Array[String]) extends Model[VectorizerModel]
  with VectorizerParams with MLWritable {

  import com.netease.easyml.ml.feature.VectorizerModel._

  def this(vocabulary: Array[String]) = this(Identifiable.randomUID("vectorizer"), vocabulary)

  def this() = this(Array.empty[String])

  def setInputCol(value: String): this.type = set(inputCol, value)

  def setOutputCol(value: String): this.type = set(outputCol, value)

  def setSparse(value: Boolean): this.type = set(sparse, value)

  def setJson(value: Boolean): this.type = set(json, value)

  def setHandleUnknown(value: String): this.type = set(handleUnknown, value)

  /** Dictionary created from [[vocabulary]] and its indices, broadcast once for [[transform()]] */
  private var broadcastDict: Option[Broadcast[Map[String, Int]]] = None

  override def transform(dataset: Dataset[_]): DataFrame = {
    transformSchema(dataset.schema, logging = true)
    if (broadcastDict.isEmpty) {
      val dict = vocabulary.zipWithIndex.toMap
      broadcastDict = Some(dataset.sparkSession.sparkContext.broadcast(dict))
    }
    val dictBr = broadcastDict.get

    val sparse = getSparse
    val handleUnknown = getHandleUnknown
    val size = vocabulary.length
    val json = getJson

    def mapToVector(map: Map[_, _]): Vector = {
      val elements = map.toArray
        .filter(it => it._1 != null && it._2 != null).map {
        case (key, value) =>
          val skey = ConvertUtil.toString(key)
          dictBr.value.get(skey) match {
            case Some(index) =>
              val dValue = ConvertUtil.toDouble(value)
              if(dValue == Double.NaN){
                logWarning(s"NAN value: $value")
              }
              (index, dValue)
            case None =>
              if (handleUnknown.equals("error")) {
                throw new SparkException(s"Unseen value: $skey. To handle unseen values, " +
                  s"set Param handleInvalid to ignore.")
              }
              null
          }
      }.filter(_ != null)
      if (sparse) {
        Vectors.sparse(size, elements)
      } else {
        if (elements.length == size) {
          Vectors.dense(elements.sortBy(_._1).map(_._2))
        } else {
          Vectors.sparse(size, elements).toDense
        }
      }
    }

    def toVector(y: Any): Vector = {
      y match {
        case vec: DenseVector => if (sparse) vec.toSparse else vec
        case vec: SparseVector => if (sparse) vec else vec.toDense
        case arr: Seq[_] =>
          val doubles = arr.filter(_ != null).map(ConvertUtil.toDouble)
          val vector = Vectors.dense(doubles.toArray)
          if (sparse) {
            vector.toSparse
          } else {
            vector
          }
        case value: java.lang.Number => if (sparse) Vectors.sparse(1, Seq((0, value.doubleValue()))) else Vectors.dense(value.doubleValue())
        case value: String =>
          if (json) {
            val json = Option(value).getOrElse("")
            val map = if (json.isEmpty) {
              Map.empty[Any, Any]
            } else {
              JSON.parseObject(value).asScala.toMap
            }
            mapToVector(map)
          } else {
            if (sparse) Vectors.sparse(1, Seq((0, value.toDouble))) else Vectors.dense(value.toDouble)
          }
        case map: Map[_, _] =>
          mapToVector(map)
        case null =>
          Vectors.sparse(size, Seq())
      }
    }

    val toVectorUdf = udf(toVector _)

    dataset.withColumn($(outputCol), toVectorUdf(col($(inputCol))))
  }

  override def transformSchema(schema: StructType): StructType = {
    validateAndTransformSchema(schema)
  }

  override def copy(extra: ParamMap): VectorizerModel = defaultCopy(extra)

  override def write: MLWriter = new VectorizerModelWriter(this)
}

object VectorizerModel extends MLReadable[VectorizerModel] {

  private class VectorizerModelWriter(instance: VectorizerModel) extends MLWriter {

    private case class Data(vocabulary: Seq[String])

    override protected def saveImpl(path: String): Unit = {
      DefaultEasyMLParamsWriter.saveMetadata(instance, path, sc)
      val data = Data(instance.vocabulary)
      val dataPath = new Path(path, "data").toString
      sparkSession.createDataFrame(Seq(data)).repartition(1).write.parquet(dataPath)
    }
  }

  private class VectorizerModelReader extends MLReader[VectorizerModel] {

    private val className = classOf[VectorizerModel].getName

    override def load(path: String): VectorizerModel = {
      val metadata = DefaultEasyMLParamsReader.loadMetadata(path, sc, className)
      val dataPath = new Path(path, "data").toString
      val data = sparkSession.read.parquet(dataPath)
        .select("vocabulary")
        .head()
      val vocabulary = data.getAs[Seq[String]](0).toArray
      val model = new VectorizerModel(metadata.uid, vocabulary)
      DefaultEasyMLParamsReader.getAndSetParams(model, metadata)
      model
    }
  }

  override def read: MLReader[VectorizerModel] = new VectorizerModelReader

  override def load(path: String): VectorizerModel = super.load(path)
}