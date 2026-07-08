package org.apache.spark.ml.feature_


import org.apache.spark.SparkException
import org.apache.spark.annotation.Since
import org.apache.spark.ml.Transformer
import org.apache.spark.ml.linalg.{Vector, VectorUDT, Vectors}
import org.apache.spark.ml.param.shared._
import org.apache.spark.ml.param.{BooleanParam, Param, ParamMap, ParamValidators}
import org.apache.spark.ml.util._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Dataset, Row}

import scala.collection.mutable

/**
 * A feature transformer that merges multiple columns into a vector column.
 *
 * This requires one pass over the entire dataset. In case we need to infer column lengths from the
 * data we require an additional call to the 'first' Dataset method, see 'handleInvalid' parameter.
 */
@Since("1.4.0")
class VectorAssembler @Since("1.4.0")(@Since("1.4.0") override val uid: String)
  extends Transformer with HasInputCols with HasOutputCol with HasHandleInvalid
    with DefaultParamsWritable {

  @Since("1.4.0")
  def this() = this(Identifiable.randomUID("vecAssembler"))

  /** @group setParam */
  @Since("1.4.0")
  def setInputCols(value: Array[String]): this.type = set(inputCols, value)

  /** @group setParam */
  @Since("1.4.0")
  def setOutputCol(value: String): this.type = set(outputCol, value)

  /** @group setParam */
  @Since("2.4.0")
  def setHandleInvalid(value: String): this.type = set(handleInvalid, value)

  def setKeepInputCol(value: Boolean): this.type = set(keepInputCol, value)

  /**
   * Param for how to handle invalid data (NULL values). Options are 'skip' (filter out rows with
   * invalid data), 'error' (throw an error), or 'keep' (return relevant number of NaN in the
   * output). Column lengths are taken from the size of ML Attribute Group, which can be set using
   * `VectorSizeHint` in a pipeline before `VectorAssembler`. Column lengths can also be inferred
   * from first rows of the data since it is safe to do so but only in case of 'error' or 'skip'.
   * Default: "error"
   *
   * @group param
   */
  @Since("2.4.0")
  override val handleInvalid: Param[String] = new Param[String](this, "handleInvalid",
    """Param for how to handle invalid data (NULL and NaN values). Options are 'skip' (filter out
      |rows with invalid data), 'error' (throw an error), or 'keep' (return relevant number of NaN
      |in the output). Column lengths are taken from the size of ML Attribute Group, which can be
      |set using `VectorSizeHint` in a pipeline before `VectorAssembler`. Column lengths can also
      |be inferred from first rows of the data since it is safe to do so but only in case of 'error'
      |or 'skip'.""".stripMargin.replaceAll("\n", " "),
    ParamValidators.inArray(VectorAssembler.supportedHandleInvalids))

  setDefault(handleInvalid, VectorAssembler.ERROR_INVALID)

  val keepInputCol: BooleanParam = new BooleanParam(this, "keepInputCol", "whether keep input")

  setDefault(keepInputCol, true)

  def getKeepInputCol: Boolean = $(keepInputCol)

  @Since("2.0.0")
  override def transform(dataset: Dataset[_]): DataFrame = {
    val structType = transformSchema(dataset.schema, logging = true)
    // Schema transformation.
    val schema = dataset.schema

    val inputCols = getInputCols
    val vectorCols = inputCols.filter { c =>
      schema(c).dataType match {
        case _: VectorUDT => true
        case _ => false
      }
    }
    val vectorColsLengths = VectorAssembler.getVectorLengthsFromFirstRow(dataset, vectorCols)
    val lengths = inputCols.map(it => vectorColsLengths.getOrElse(it, 1))
    val filteredDataset = $(handleInvalid) match {
      case VectorAssembler.SKIP_INVALID => dataset.na.drop(inputCols)
      case VectorAssembler.KEEP_INVALID | VectorAssembler.ERROR_INVALID | VectorAssembler.KEEP_ZERO_INVALID => dataset
    }
    val keepInvalid = $(handleInvalid) == VectorAssembler.KEEP_INVALID || $(handleInvalid) == VectorAssembler.KEEP_ZERO_INVALID
    val keepZeroInvalid = $(handleInvalid) == VectorAssembler.KEEP_ZERO_INVALID
    // Data transformation.
    val columns = schema.fields.map(_.name)
    val keepInputCol = getKeepInputCol
    val inputColIdx = inputCols.map { c => columns.indexOf(c) }
    val otherColIdx = columns.filterNot(inputCols.contains).map { c => columns.indexOf(c) }
    val newRdd = dataset.toDF().rdd.map(row => {
      val seq = row.toSeq
      val vectors = inputColIdx.map(i => seq(i))
      val assemble = VectorAssembler.assemble(lengths, keepInvalid, keepZeroInvalid)(vectors)
      if (keepInputCol) {
        Row.fromSeq(seq :+ assemble)
      } else {
        Row.fromSeq(otherColIdx.map(i => seq(i)) :+ assemble)
      }
    })

    filteredDataset.sparkSession.createDataFrame(newRdd, structType)
  }

  @Since("1.4.0")
  override def transformSchema(schema: StructType): StructType = {
    val inputColNames = $(inputCols)
    val outputColName = $(outputCol)
    val incorrectColumns = inputColNames.flatMap { name =>
      schema(name).dataType match {
        case _: NumericType | BooleanType => None
        case t if t.isInstanceOf[VectorUDT] => None
        case other => Some(s"Data type ${other.catalogString} of column $name is not supported.")
      }
    }
    if (incorrectColumns.nonEmpty) {
      throw new IllegalArgumentException(incorrectColumns.mkString("\n"))
    }
    if (schema.fieldNames.contains(outputColName)) {
      throw new IllegalArgumentException(s"Output column $outputColName already exists.")
    }
    var fields = schema.fields
    if (!$(keepInputCol)) {
      val inputCols = getInputCols
      fields = fields.filterNot(it => inputCols.contains(it.name))
    }
    StructType(fields :+ StructField(outputColName, new VectorUDT, true))
  }

  @Since("1.4.1")
  override def copy(extra: ParamMap): VectorAssembler = defaultCopy(extra)

  @Since("3.0.0")
  override def toString: String = {
    s"VectorAssembler: uid=$uid, handleInvalid=${$(handleInvalid)}" +
      get(inputCols).map(c => s", numInputCols=${c.length}").getOrElse("")
  }
}

@Since("1.6.0")
object VectorAssembler extends DefaultParamsReadable[VectorAssembler] {

  val SKIP_INVALID: String = "skip"
  val ERROR_INVALID: String = "error"
  val KEEP_INVALID: String = "keep"
  val KEEP_ZERO_INVALID: String = "keep_zero"
  val supportedHandleInvalids: Array[String] =
    Array(SKIP_INVALID, ERROR_INVALID, KEEP_INVALID, KEEP_ZERO_INVALID)

  /**
   * Infers lengths of vector columns from the first row of the dataset
   *
   * @param dataset the dataset
   * @param columns name of vector columns whose lengths need to be inferred
   * @return map of column names to lengths
   */
  def getVectorLengthsFromFirstRow(dataset: Dataset[_],
                                   columns: Seq[String]): Map[String, Int] = {
    try {
      if (columns.isEmpty) {
        Map()
      } else {
        val first_row = dataset.toDF().select(columns.map(col): _*).first()
        columns.zip(first_row.toSeq).map {
          case (c, x) => c -> x.asInstanceOf[Vector].size
        }.toMap
      }
    } catch {
      case e: NullPointerException => throw new NullPointerException(
        s"""Encountered null value while inferring lengths from the first row. Consider using
           |VectorSizeHint to add metadata for columns: ${columns.mkString("[", ", ", "]")}. """
          .stripMargin.replaceAll("\n", " ") + e.toString)
      case e: NoSuchElementException => throw new NoSuchElementException(
        s"""Encountered empty dataframe while inferring lengths from the first row. Consider using
           |VectorSizeHint to add metadata for columns: ${columns.mkString("[", ", ", "]")}. """
          .stripMargin.replaceAll("\n", " ") + e.toString)
    }
  }


  @Since("1.6.0")
  override def load(path: String): VectorAssembler = super.load(path)

  /**
   * Returns a function that has the required information to assemble each row.
   *
   * @param lengths     an array of lengths of input columns, whose size should be equal to the number
   *                    of cells in the row (vv)
   * @param keepInvalid indicate whether to throw an error or not on seeing a null in the rows
   * @return a udf that can be applied on each row
   */
  def assemble(lengths: Array[Int], keepInvalid: Boolean, keepZeroInvalid: Boolean)(vv: Array[Any]): Vector = {
    val indices = mutable.ArrayBuilder.make[Int]
    val values = mutable.ArrayBuilder.make[Double]
    var featureIndex = 0

    var inputColumnIndex = 0
    vv.foreach {
      case vec: Vector =>
        vec.foreachNonZero { case (i, v) =>
          indices += featureIndex + i
          values += v
        }
        inputColumnIndex += 1
        featureIndex += vec.size
      case number: java.lang.Number =>
        val v = number.doubleValue()
        if (v.isNaN && !keepInvalid) {
          throw new SparkException(
            s"""Encountered NaN while assembling a row with handleInvalid = "error". Consider
               |removing NaNs from dataset or using handleInvalid = "keep" or "skip"."""
              .stripMargin)
        } else if (v != 0.0) {
          indices += featureIndex
          values += v
        }
        inputColumnIndex += 1
        featureIndex += 1
      case null =>
        if (keepInvalid) {
          val length: Int = lengths(inputColumnIndex)
          if (!keepZeroInvalid) {
            Array.range(0, length).foreach { i =>
              indices += featureIndex + i
              values += Double.NaN
            }
          }
          inputColumnIndex += 1
          featureIndex += length
        } else {
          throw new SparkException(
            s"""Encountered null while assembling a row with handleInvalid = "error". Consider
               |removing nulls from dataset or using handleInvalid = "keep" or "skip"."""
              .stripMargin)
        }
      case o =>
        throw new SparkException(s"$o of type ${o.getClass.getName} is not supported.")
    }
    Vectors.sparse(featureIndex, indices.result(), values.result()).compressed
  }
}
