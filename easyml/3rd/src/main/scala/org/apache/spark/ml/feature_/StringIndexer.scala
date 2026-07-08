package org.apache.spark.ml.feature_

import org.apache.hadoop.fs.Path
import org.apache.spark.SparkException
import org.apache.spark.annotation.Since
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.ml.attribute.{Attribute, NominalAttribute}
import org.apache.spark.ml.param._
import org.apache.spark.ml.param.shared._
import org.apache.spark.ml.util._
import org.apache.spark.ml.{Estimator, Model, Transformer}
import org.apache.spark.sql._
import org.apache.spark.sql.catalyst.expressions.{If, Literal}
import org.apache.spark.sql.expressions.Aggregator
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.util.collection.OpenHashMap
import org.apache.spark.util_.ThreadUtils

/**
 * Base trait for [[StringIndexer]] and [[StringIndexerModel]].
 */
trait HasStartOffset extends Params {

  final val startOffset: IntParam = new IntParam(this, "startOffset",
    "Start offset, default 0.")

  def getStartOffset: Int = $(startOffset)

  setDefault(startOffset, 0)
}

trait StringIndexerBase extends Params with HasHandleInvalid with HasInputCol
  with HasInputCols with HasOutputCols with HasStartOffset {

  final val outputCol: Param[String] = new Param[String](this, "outputCol", "output column name")

  /** @group getParam */
  final def getOutputCol: String = $(outputCol)

  /**
   * Param for how to handle invalid data (unseen labels or NULL values).
   * Options are 'skip' (filter out rows with invalid data),
   * 'error' (throw an error), or 'keep' (put invalid data in a special additional
   * bucket, at index numLabels).
   * Default: "error"
   *
   * @group param
   */
  @Since("1.6.0")
  override val handleInvalid: Param[String] = new Param[String](this, "handleInvalid",
    "How to handle invalid data (unseen labels or NULL values). " +
      "Options are 'skip' (filter out rows with invalid data), error (throw an error), " +
      "or 'keep' (put invalid data in a special additional bucket, at index numLabels).",
    ParamValidators.inArray(StringIndexer.supportedHandleInvalids))

  setDefault(handleInvalid, StringIndexer.ERROR_INVALID)

  /**
   * Param for how to order labels of string column. The first label after ordering is assigned
   * an index of 0.
   * Options are:
   *   - 'frequencyDesc': descending order by label frequency (most frequent label assigned 0)
   *   - 'frequencyAsc': ascending order by label frequency (least frequent label assigned 0)
   *   - 'alphabetDesc': descending alphabetical order
   *   - 'alphabetAsc': ascending alphabetical order
   * Default is 'frequencyDesc'.
   *
   * Note: In case of equal frequency when under frequencyDesc/Asc, the strings are further sorted
   * alphabetically.
   *
   * @group param
   */
  @Since("2.3.0")
  final val stringOrderType: Param[String] = new Param(this, "stringOrderType",
    "How to order labels of string column. " +
      "The first label after ordering is assigned an index of 0. " +
      s"Supported options: ${StringIndexer.supportedStringOrderType.mkString(", ")}.",
    ParamValidators.inArray(StringIndexer.supportedStringOrderType))

  /** @group getParam */
  @Since("2.3.0")
  def getStringOrderType: String = $(stringOrderType)

  final val outputType: Param[String] = new Param(this, "outputType",
    "Output index dataType. " +
      s"Supported options: ${StringIndexer.supportedOutputType.mkString(", ")}.",
    ParamValidators.inArray(StringIndexer.supportedOutputType))

  def getOutputType: String = $(outputType)

  final val minCount: IntParam = new IntParam(this, "minCount",
    "Min count, default 1.")

  def getMinCount: Int = $(minCount)

  setDefault(minCount, 1)

  /** Returns the input and output column names corresponding in pair. */
  def getInOutCols(): (Array[String], Array[String]) = {
    ParamValidators.checkSingleVsMultiColumnParams(this, Seq(outputCol), Seq(outputCols))

    if (isSet(inputCol)) {
      (Array($(inputCol)), Array($(outputCol)))
    } else {
      require($(inputCols).length == $(outputCols).length,
        "The number of input columns does not match output columns")
      ($(inputCols), $(outputCols))
    }
  }

  private def validateAndTransformField(
                                         schema: StructType,
                                         inputColName: String,
                                         outputColName: String): StructField = {
    val inputDataType = schema(inputColName).dataType
    require(inputDataType == StringType || inputDataType.isInstanceOf[NumericType] || (inputDataType.isInstanceOf[ArrayType] && inputDataType.asInstanceOf[ArrayType].elementType.isInstanceOf[StringType]),
      s"The input column $inputColName must be either string type or numeric type, " +
        s"but got $inputDataType.")
    require(schema.fields.forall(_.name != outputColName),
      s"Output column $outputColName already exists.")

    val outputType = getOutputType
    val dataType = if (outputType.equals("double")) {
      DoubleType
    } else if (outputType.equals("long")) {
      LongType
    } else {
      IntegerType
    }
    if (inputDataType == StringType) {
      StructField(outputColName, dataType)
    } else {
      StructField(outputColName, ArrayType(dataType))
    }
    //    NominalAttribute.defaultAttr.withName(outputColName).toStructField()
  }

  /** Validates and transforms the input schema. */
  protected def validateAndTransformSchema(
                                            schema: StructType,
                                            skipNonExistsCol: Boolean = false): StructType = {
    val (inputColNames, outputColNames) = getInOutCols()

    require(outputColNames.distinct.length == outputColNames.length,
      s"Output columns should not be duplicate.")

    val outputFields = inputColNames.zip(outputColNames).flatMap {
      case (inputColName, outputColName) =>
        schema.fieldNames.contains(inputColName) match {
          case true => Some(validateAndTransformField(schema, inputColName, outputColName))
          case false if skipNonExistsCol => None
          case _ => throw new SparkException(s"Input column $inputColName does not exist.")
        }
    }
    StructType(schema.fields ++ outputFields)
  }
}

/**
 * A label indexer that maps string column(s) of labels to ML column(s) of label indices.
 * If the input columns are numeric, we cast them to string and index the string values.
 * The indices are in [0, numLabels). By default, this is ordered by label frequencies
 * so the most frequent label gets index 0. The ordering behavior is controlled by
 * setting `stringOrderType`.
 *
 * @see `IndexToString` for the inverse transformation
 */
@Since("1.4.0")
class StringIndexer @Since("1.4.0")(
                                     @Since("1.4.0") override val uid: String) extends Estimator[StringIndexerModel]
  with StringIndexerBase with DefaultParamsWritable {

  @Since("1.4.0")
  def this() = this(Identifiable.randomUID("strIdx"))

  /** @group setParam */
  @Since("1.6.0")
  def setHandleInvalid(value: String): this.type = set(handleInvalid, value)

  /** @group setParam */
  @Since("2.3.0")
  def setStringOrderType(value: String): this.type = set(stringOrderType, value)

  def setOutputType(value: String): this.type = set(outputType, value)

  def setStartOffset(value: Int): this.type = set(startOffset, value)

  def setMinCount(value: Int): this.type = set(minCount, value)

  setDefault(stringOrderType -> StringIndexer.frequencyDesc, outputType -> "double")

  /** @group setParam */
  @Since("1.4.0")
  def setInputCol(value: String): this.type = set(inputCol, value)

  /** @group setParam */
  @Since("1.4.0")
  def setOutputCol(value: String): this.type = set(outputCol, value)

  /** @group setParam */
  @Since("3.0.0")
  def setInputCols(value: Array[String]): this.type = set(inputCols, value)

  /** @group setParam */
  @Since("3.0.0")
  def setOutputCols(value: Array[String]): this.type = set(outputCols, value)

  /**
   * Gets columns from dataset. If a column is not string type, we replace NaN values
   * with null. Columns are casted to string type.
   */
  private def getSelectedCols(dataset: Dataset[_], inputCols: Seq[String]): Seq[Column] = {
    inputCols.map { colName =>
      val col = dataset.col(colName)
      if (col.expr.dataType == StringType || col.expr.dataType == ArrayType(StringType)) {
        col
      } else {
        // We don't count for NaN values. Because `StringIndexerAggregator` only processes strings,
        // we replace NaNs with null in advance.
        new Column(If(col.isNaN.expr, Literal(null), col.expr)).cast(StringType)
      }
    }
  }

  private def countByValue(
                            dataset: Dataset[_],
                            inputCols: Array[String]): Array[OpenHashMap[String, Long]] = {
    val aggregator = new StringIndexerAggregator(inputCols.length, getMinCount)
    implicit val encoder = Encoders.kryo[Array[OpenHashMap[String, Long]]]
    val selectedCols = getSelectedCols(dataset, inputCols)
    dataset.select(selectedCols: _*)
      .toDF
      .groupBy().agg(aggregator.toColumn)
      .as[Array[OpenHashMap[String, Long]]]
      .collect()(0)
  }

  private def sortByFreq(dataset: Dataset[_], ascending: Boolean): Array[Array[String]] = {
    val (inputCols, _) = getInOutCols()

    val sortFunc = StringIndexer.getSortFunc(ascending = ascending)
    val orgStrings = countByValue(dataset, inputCols).toSeq
    val minCount = getMinCount
    ThreadUtils.parmap(orgStrings, "sortingStringLabels", 8) { counts =>
      counts.toSeq.filter(_._2 >= minCount).sortWith(sortFunc).map(_._1).toArray
    }.toArray
  }

  private def sortByAlphabet(dataset: Dataset[_], ascending: Boolean): Array[Array[String]] = {
    val (inputCols, _) = getInOutCols()

    val selectedCols = getSelectedCols(dataset, inputCols).map(collect_set(_))
    val allLabels = dataset.select(selectedCols: _*)
      .collect().toSeq.flatMap(_.toSeq).asInstanceOf[Seq[Seq[String]]]
    ThreadUtils.parmap(allLabels, "sortingStringLabels", 8) { labels =>
      val sorted = labels.filter(_ != null).sorted
      if (ascending) {
        sorted.toArray
      } else {
        sorted.reverse.toArray
      }
    }.toArray
  }

  @Since("2.0.0")
  override def fit(dataset: Dataset[_]): StringIndexerModel = {
    transformSchema(dataset.schema, logging = true)

    // In case of equal frequency when frequencyDesc/Asc, the strings are further sorted
    // alphabetically.
    val labelsArray = $(stringOrderType) match {
      case StringIndexer.frequencyDesc => sortByFreq(dataset, ascending = false)
      case StringIndexer.frequencyAsc => sortByFreq(dataset, ascending = true)
      case StringIndexer.alphabetDesc => sortByAlphabet(dataset, ascending = false)
      case StringIndexer.alphabetAsc => sortByAlphabet(dataset, ascending = true)
    }
    copyValues(new StringIndexerModel(uid, labelsArray).setParent(this))
  }

  @Since("1.4.0")
  override def transformSchema(schema: StructType): StructType = {
    validateAndTransformSchema(schema)
  }

  @Since("1.4.1")
  override def copy(extra: ParamMap): StringIndexer = defaultCopy(extra)
}

@Since("1.6.0")
object StringIndexer extends DefaultParamsReadable[StringIndexer] {
  val SKIP_INVALID: String = "skip"
  val ERROR_INVALID: String = "error"
  val KEEP_INVALID: String = "keep"
  val supportedHandleInvalids: Array[String] =
    Array(SKIP_INVALID, ERROR_INVALID, KEEP_INVALID)
  val frequencyDesc: String = "frequencyDesc"
  val frequencyAsc: String = "frequencyAsc"
  val alphabetDesc: String = "alphabetDesc"
  val alphabetAsc: String = "alphabetAsc"
  val supportedStringOrderType: Array[String] =
    Array(frequencyDesc, frequencyAsc, alphabetDesc, alphabetAsc)
  val supportedOutputType: Array[String] =
    Array("long", "int", "double")

  @Since("1.6.0")
  override def load(path: String): StringIndexer = super.load(path)

  // Returns a function used to sort strings by frequency (ascending or descending).
  // In case of equal frequency, it sorts strings by alphabet (ascending).
  private def getSortFunc(
                           ascending: Boolean): ((String, Long), (String, Long)) => Boolean = {
    if (ascending) {
      case ((strA: String, freqA: Long), (strB: String, freqB: Long)) =>
        if (freqA == freqB) {
          strA < strB
        } else {
          freqA < freqB
        }
    } else {
      case ((strA: String, freqA: Long), (strB: String, freqB: Long)) =>
        if (freqA == freqB) {
          strA < strB
        } else {
          freqA > freqB
        }
    }
  }
}

/**
 * Model fitted by [[StringIndexer]].
 *
 * @param labelsArray Array of ordered list of labels, corresponding to indices to be assigned
 *                    for each input column.
 * @note During transformation, if any input column does not exist,
 *       `StringIndexerModel.transform` would skip the input column.
 *       If all input columns do not exist, it returns the input dataset unmodified.
 *       This is a temporary fix for the case when target labels do not exist during prediction.
 */
@Since("1.4.0")
class StringIndexerModel(
                          @Since("1.4.0") override val uid: String,
                          @Since("3.0.0") val labelsArray: Array[Array[String]])
  extends Model[StringIndexerModel] with StringIndexerBase with MLWritable {

  import StringIndexerModel._

  @Since("1.5.0")
  def this(uid: String, labels: Array[String]) = this(uid, Array(labels))

  @Since("1.5.0")
  def this(labels: Array[String]) = this(Identifiable.randomUID("strIdx"), Array(labels))

  @Since("3.0.0")
  def this(labelsArray: Array[Array[String]]) = this(Identifiable.randomUID("strIdx"), labelsArray)

  def setStartOffset(value: Int): this.type = set(startOffset, value)

  def setOutputType(value: String): this.type = set(outputType, value)

  @deprecated("`labels` is deprecated and will be removed in 3.1.0. Use `labelsArray` " +
    "instead.", "3.0.0")
  @Since("1.5.0")
  def labels: Array[String] = {
    require(labelsArray.length == 1, "This StringIndexerModel is fit on multiple columns. " +
      "Call `labelsArray` instead.")
    labelsArray(0)
  }

  // Prepares the maps for string values to corresponding index values.
  private lazy val labelsToIndexArray: Array[OpenHashMap[String, Int]] = {
    val offset = getStartOffset
    for (labels <- labelsArray) yield {
      val n = labels.length
      val map = new OpenHashMap[String, Int](n)
      labels.zipWithIndex.foreach { case (label, idx) =>
        map.update(label, idx + offset)
      }
      map
    }
  }

  private var labelsToIndexArrayBc: Broadcast[Array[OpenHashMap[String, Int]]] = _

  def destroy(): Unit = {
    if (labelsToIndexArrayBc != null) {
      labelsToIndexArrayBc.destroy()
    }
  }

  /** @group setParam */
  @Since("1.6.0")
  def setHandleInvalid(value: String): this.type = set(handleInvalid, value)

  /** @group setParam */
  @Since("1.4.0")
  def setInputCol(value: String): this.type = set(inputCol, value)

  /** @group setParam */
  @Since("1.4.0")
  def setOutputCol(value: String): this.type = set(outputCol, value)

  /** @group setParam */
  @Since("3.0.0")
  def setInputCols(value: Array[String]): this.type = set(inputCols, value)

  /** @group setParam */
  @Since("3.0.0")
  def setOutputCols(value: Array[String]): this.type = set(outputCols, value)

  // This filters out any null values and also the input labels which are not in
  // the dataset used for fitting.
  private def filterInvalidData(dataset: Dataset[_], inputColNames: Seq[String]): Dataset[_] = {
    val conditions: Seq[Column] = (0 until inputColNames.length).map { i =>
      val inputColName = inputColNames(i)
      val labelToIndex = labelsToIndexArrayBc.value(i)
      // We have this additional lookup at `labelToIndex` when `handleInvalid` is set to
      // `StringIndexer.SKIP_INVALID`. Another idea is to do this lookup natively by SQL
      // expression, however, lookup for a key in a map is not efficient in SparkSQL now.
      // See `ElementAt` and `GetMapValue` expressions. If SQL's map lookup is improved,
      // we can consider to change this.
      val filter = udf { labelAny: Any =>
        labelAny match {
          case label: String =>
            labelToIndex.contains(label)
          case labelSeq: Seq[_] =>
            labelSeq.exists(label => labelToIndex.contains(label.asInstanceOf[String]))
        }
      }
      filter(dataset(inputColName))
    }

    dataset.na.drop(inputColNames.filter(dataset.schema.fieldNames.contains(_)))
      .where(conditions.reduce(_ and _))
  }

  @Since("2.0.0")
  override def transform(dataset: Dataset[_]): DataFrame = {
    transformSchema(dataset.schema, logging = true)

    val (inputColNames, outputColNames) = getInOutCols()

    // Skips invalid rows if `handleInvalid` is set to `StringIndexer.SKIP_INVALID`.
    //    val filteredDataset = if (getHandleInvalid == StringIndexer.SKIP_INVALID) {
    //      filterInvalidData(dataset, inputColNames)
    //    } else {
    //      dataset
    //    }

    val filteredDataset = dataset

    val filteredOutputColNames = outputColNames.zipWithIndex.filter(it => {
      if (!dataset.schema.fieldNames.contains(inputColNames(it._2))) {
        logWarning(s"Input column ${inputColNames(it._2)} does not exist during transformation. " +
          "Skip StringIndexerModel for this column.")
        false
      } else {
        true
      }
    })

    val keepInvalid = (getHandleInvalid == StringIndexer.KEEP_INVALID)
    val skipInvalid = (getHandleInvalid == StringIndexer.SKIP_INVALID)

    val outputType = getOutputType

    def index(label: String, labelToIndex: OpenHashMap[String, Int]): Any = {
      val id = if (label == null) {
        if (skipInvalid) {
          return null
        } else if (keepInvalid) {
          labelToIndex.size
        } else {
          throw new SparkException("StringIndexer encountered NULL value. To handle or skip " +
            "NULLS, try setting StringIndexer.handleInvalid.")
        }
      } else {
        if (labelToIndex.contains(label)) {
          labelToIndex(label)
        } else if (keepInvalid) {
          labelToIndex.size
        } else if (skipInvalid) {
          return null
        } else {
          throw new SparkException(s"Unseen label: $label. To handle unseen labels, " +
            s"set Param handleInvalid to ${StringIndexer.KEEP_INVALID} or ${StringIndexer.SKIP_INVALID}.")
        }
      }
      if (outputType.equals("double")) {
        id.toDouble
      } else if (outputType.equals("long")) {
        id.toLong
      } else {
        id
      }
    }

    labelsToIndexArrayBc = dataset.sparkSession.sparkContext.broadcast(labelsToIndexArray)
    val newRdd = filteredDataset.toDF.rdd.map(row => {
      val outputs = filteredOutputColNames.map {
        case (_, i) =>
          val inputColName = inputColNames(i)
          val labelToIndex = labelsToIndexArrayBc.value(i)

          val value = row.get(row.fieldIndex(inputColName))
          value match {
            case label: String =>
              index(label, labelToIndex)
            case labelSeq: Seq[_] =>
              var seq = labelSeq.asInstanceOf[Seq[String]]
              if (skipInvalid) {
                seq = seq.filter(labelToIndex.contains)
              }
              val indices = seq.map(label => index(label, labelToIndex))
              if (indices.isEmpty) {
                null
              } else {
                indices
              }
            case null =>
              null
          }
      }
      Row.fromSeq(row.toSeq ++ outputs)
    })
    dataset.sparkSession.createDataFrame(newRdd, transformSchema(dataset.schema))
  }

  @Since("1.4.0")
  override def transformSchema(schema: StructType): StructType = {
    validateAndTransformSchema(schema, skipNonExistsCol = true)
  }

  @Since("1.4.1")
  override def copy(extra: ParamMap): StringIndexerModel = {
    val copied = new StringIndexerModel(uid, labelsArray)
    copyValues(copied, extra).setParent(parent)
  }

  @Since("1.6.0")
  override def write: StringIndexModelWriter = new StringIndexModelWriter(this)

  @Since("3.0.0")
  override def toString: String = {
    s"StringIndexerModel: uid=$uid, handleInvalid=${$(handleInvalid)}" +
      get(stringOrderType).map(t => s", stringOrderType=$t").getOrElse("") +
      get(inputCols).map(c => s", numInputCols=${c.length}").getOrElse("") +
      get(outputCols).map(c => s", numOutputCols=${c.length}").getOrElse("")
  }
}

@Since("1.6.0")
object StringIndexerModel extends MLReadable[StringIndexerModel] {

  private[StringIndexerModel]
  class StringIndexModelWriter(instance: StringIndexerModel) extends MLWriter {

    private case class Data(labelsArray: Array[Array[String]])

    override protected def saveImpl(path: String): Unit = {
      DefaultParamsWriter.saveMetadata(instance, path, sc)
      val data = Data(instance.labelsArray)
      val dataPath = new Path(path, "data").toString
      sparkSession.createDataFrame(Seq(data)).repartition(1).write.parquet(dataPath)
    }
  }

  private class StringIndexerModelReader extends MLReader[StringIndexerModel] {

    private val className = classOf[StringIndexerModel].getName

    override def load(path: String): StringIndexerModel = {
      val metadata = DefaultParamsReader.loadMetadata(path, sc, className)
      val dataPath = new Path(path, "data").toString

      // We support loading old `StringIndexerModel` saved by previous Spark versions.
      // Previous model has `labels`, but new model has `labelsArray`.
      val df = sparkSession.read.parquet(dataPath)
      val labelsArray = if (df.columns.contains("labels")) {
        // Spark 2.4 and before.
        val data = df
          .select("labels")
          .head()
        val labels = data.getAs[Seq[String]](0).toArray
        Array(labels)
      } else {
        // After Spark 3.0.
        val data = df
          .select("labelsArray")
          .head()
        data.getAs[Seq[Seq[String]]](0).map(_.toArray).toArray
      }
      val model = new StringIndexerModel(metadata.uid, labelsArray)
      metadata.getAndSetParams(model)
      model
    }
  }

  @Since("1.6.0")
  override def read: MLReader[StringIndexerModel] = new StringIndexerModelReader

  @Since("1.6.0")
  override def load(path: String): StringIndexerModel = super.load(path)
}

/**
 * A `Transformer` that maps a column of indices back to a new column of corresponding
 * string values.
 * The index-string mapping is either from the ML attributes of the input column,
 * or from user-supplied labels (which take precedence over ML attributes).
 *
 * @see `StringIndexer` for converting strings into indices
 */
@Since("1.5.0")
class IndexToString @Since("2.2.0")(@Since("1.5.0") override val uid: String)
  extends Transformer with HasInputCol with HasOutputCol with HasStartOffset with DefaultParamsWritable {

  @Since("1.5.0")
  def this() =
    this(Identifiable.randomUID("idxToStr"))

  /** @group setParam */
  @Since("1.5.0")
  def setInputCol(value: String): this.type = set(inputCol, value)

  /** @group setParam */
  @Since("1.5.0")
  def setOutputCol(value: String): this.type = set(outputCol, value)

  /** @group setParam */
  @Since("1.5.0")
  def setLabels(value: Array[String]): this.type = set(labels, value)

  def setStartOffset(value: Int): this.type = set(startOffset, value)

  /**
   * Optional param for array of labels specifying index-string mapping.
   *
   * Default: Not specified, in which case [[inputCol]] metadata is used for labels.
   *
   * @group param
   */
  @Since("1.5.0")
  final val labels: StringArrayParam = new StringArrayParam(this, "labels",
    "Optional array of labels specifying index-string mapping." +
      " If not provided or if empty, then metadata from inputCol is used instead.")

  /** @group getParam */
  @Since("1.5.0")
  final def getLabels: Array[String] = $(labels)

  @Since("1.5.0")
  override def transformSchema(schema: StructType): StructType = {
    val inputColName = $(inputCol)
    val inputDataType = schema(inputColName).dataType
    require(inputDataType.isInstanceOf[NumericType] || (inputDataType.isInstanceOf[ArrayType] && inputDataType.asInstanceOf[ArrayType].elementType.isInstanceOf[NumericType]),
      s"The input column $inputColName must be a numeric type, " +
        s"but got $inputDataType.")
    val inputFields = schema.fields
    val outputColName = $(outputCol)
    require(inputFields.forall(_.name != outputColName),
      s"Output column $outputColName already exists.")
    val outputFields = if (inputDataType.isInstanceOf[NumericType]) {
      inputFields :+ StructField($(outputCol), StringType)
    } else {
      inputFields :+ StructField($(outputCol), ArrayType(StringType))
    }
    StructType(outputFields)
  }

  @Since("2.0.0")
  override def transform(dataset: Dataset[_]): DataFrame = {
    transformSchema(dataset.schema, logging = true)
    val inputColSchema = dataset.schema($(inputCol))
    // If the labels array is empty use column metadata
    val values = if (!isDefined(labels) || $(labels).isEmpty) {
      Attribute.fromStructField(inputColSchema)
        .asInstanceOf[NominalAttribute].values.get
    } else {
      $(labels)
    }
    val inputColName = $(inputCol)
    val startOffset = getStartOffset
    val newRdd = dataset.toDF.rdd.map(row => {
      val indices = row.getAs[Any](inputColName) match {
        case index: java.lang.Number =>
          val idx = index.intValue() - startOffset
          if (0 <= idx && idx < values.length) {
            values(idx)
          } else {
            throw new SparkException(s"Unseen index: $index ??")
          }
        case indexSeq: Seq[_] =>
          val indices = indexSeq.map(index => {
            val idx = index.asInstanceOf[java.lang.Number].intValue() - startOffset
            if (0 <= idx && idx < values.length) {
              values(idx)
            } else {
              throw new SparkException(s"Unseen index: $index ??")
            }
          })
          if (indices.isEmpty) {
            null
          } else {
            indices
          }
        case null =>
          null
      }
      Row.fromSeq(row.toSeq :+ indices)
    })
    dataset.sparkSession.createDataFrame(newRdd, transformSchema(dataset.schema))
  }

  @Since("1.5.0")
  override def copy(extra: ParamMap): IndexToString = {
    defaultCopy(extra)
  }
}

@Since("1.6.0")
object IndexToString extends DefaultParamsReadable[IndexToString] {

  @Since("1.6.0")
  override def load(path: String): IndexToString = super.load(path)
}

/**
 * A SQL `Aggregator` used by `StringIndexer` to count labels in string columns during fitting.
 */
private class StringIndexerAggregator(numColumns: Int, minCount: Int)
  extends Aggregator[Row, Array[OpenHashMap[String, Long]], Array[OpenHashMap[String, Long]]] {

  override def zero: Array[OpenHashMap[String, Long]] =
    Array.fill(numColumns)(new OpenHashMap[String, Long]())

  def reduce(
              array: Array[OpenHashMap[String, Long]],
              row: Row): Array[OpenHashMap[String, Long]] = {
    for (i <- 0 until numColumns) {
      row.get(i) match {
        case stringValue: String =>
          // We don't count for null values.
          if (stringValue != null) {
            array(i).changeValue(stringValue, 1L, _ + 1)
          }
        case stringSeq: Seq[_] =>
          stringSeq.foreach(stringValue => {
            if (stringValue != null) {
              array(i).changeValue(stringValue.asInstanceOf[String], 1L, _ + 1)
            }
          })
        case null =>
      }
    }
    array
  }

  def merge(array1: Array[OpenHashMap[String, Long]],
            array2: Array[OpenHashMap[String, Long]]): Array[OpenHashMap[String, Long]] = {
    for (i <- 0 until numColumns) {
      array2(i).foreach { case (key: String, count: Long) =>
        array1(i).changeValue(key, count, _ + count)
      }
    }
    array1
  }

  def finish(array: Array[OpenHashMap[String, Long]]): Array[OpenHashMap[String, Long]] = {
    if (minCount <= 0) {
      array
    } else {
      array.map(hmap => {
        val newMap = new OpenHashMap[String, Long]()
        hmap.foreach {
          case (k, v) =>
            if (v >= minCount) {
              newMap.update(k, v)
            }
        }
        newMap
      })
    }
  }

  override def bufferEncoder: Encoder[Array[OpenHashMap[String, Long]]] = {
    Encoders.kryo[Array[OpenHashMap[String, Long]]]
  }

  override def outputEncoder: Encoder[Array[OpenHashMap[String, Long]]] = {
    Encoders.kryo[Array[OpenHashMap[String, Long]]]
  }
}

