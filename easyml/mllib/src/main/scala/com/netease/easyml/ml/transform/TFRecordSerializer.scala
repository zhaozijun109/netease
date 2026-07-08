package com.netease.easyml.ml.transform

import java.util.Base64

import com.linkedin.spark.datasources.tfrecordv2.{TFRecordSerializer => Serializer}
import org.apache.spark.ml.Transformer
import org.apache.spark.ml.param._
import org.apache.spark.ml.param.shared.{HasInputCols, HasOutputCol}
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.catalyst.encoders.{ExpressionEncoder, RowEncoder}
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Dataset, Row}

/**
 * Created by linjiuning on 2021/6/5.
 */

trait TFRecordSerializerParams extends Params with HasInputCols with HasOutputCol {

  val recordType = new Param[String](this, "recordType", "input format of TensorFlow records. By default it is Example.", ParamValidators.inArray(Array("Example", "SequenceExample")))

  def getRecordType: String = $(recordType)

  val dropInputs = new Param[Boolean](this, "dropInputs", "whether to drop inputs")

  def getDropInputs: Boolean = $(dropInputs)

  /**
   * Validate and transform the input schema.
   */
  protected def validateAndTransformSchema(schema: StructType): StructType = {
    val resultField = StructField($(outputCol), StringType)

    val cols = if (isSet(inputCols)) {
      getInputCols
    } else {
      schema.fields.map(_.name)
    }

    val fields = if ($(dropInputs)) {
      schema.fields.filter(field => !cols.contains(field.name))
    } else {
      schema.fields
    }
    StructType(fields :+ resultField)
  }

  setDefault(recordType -> "Example", outputCol -> "examples", dropInputs -> true)
}

class TFRecordSerializer(override val uid: String) extends Transformer
  with TFRecordSerializerParams with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("tfrecord"))

  def setInputCols(value: Array[String]): this.type = set(inputCols, value)

  def setOutputCol(value: String): this.type = set(outputCol, value)

  def setRecordType(value: String): this.type = set(recordType, value)

  def setDropInputs(value: Boolean): this.type = set(dropInputs, value)

  override def transform(dataset: Dataset[_]): DataFrame = {
    val structType = transformSchema(dataset.schema, logging = true)

    val recordType = getRecordType
    val dropInputs = getDropInputs

    val cols = if (isSet(inputCols)) {
      getInputCols
    } else {
      dataset.columns
    }

    val names = dataset.schema.map(_.name)
    val inputIdx = cols.map(col => names.indexOf(col))
    val inputIdxSet = inputIdx.toSet

    val schema = dataset.schema
    val newRdd = dataset.toDF().rdd.mapPartitions(rows => {
      val serializer = new Serializer(schema)
      val encoder: ExpressionEncoder[Row] = RowEncoder.apply(schema).resolveAndBind()
      rows.map(row => {
        val nRow = if (inputIdx.length < row.size) {
          Row.fromSeq(inputIdx.map(row.get))
        } else {
          row
        }

        val internalRow = encoder.createSerializer()(nRow)
        val record = recordType match {
          case "Example" =>
            serializer.serializeExample(internalRow)
          case "SequenceExample" =>
            serializer.serializeSequenceExample(internalRow)
          case _ =>
            throw new IllegalArgumentException(s"Unsupported recordType ${recordType}: recordType can be Example or SequenceExample")
        }
        val example = Base64.getEncoder.encodeToString(record.toByteArray)
        val seq = if (dropInputs) {
          row.toSeq.zipWithIndex.filter(it => !inputIdxSet.contains(it._2)).map(_._1)
        } else {
          row.toSeq
        }
        Row.fromSeq(seq :+ example)
      })
    })
    dataset.sparkSession.createDataFrame(newRdd, structType)
  }

  override def copy(extra: ParamMap): Transformer = defaultCopy(extra)

  override def transformSchema(schema: StructType): StructType = {
    validateAndTransformSchema(schema)
  }
}

object TFRecordSerializer extends DefaultParamsReadable[TFRecordSerializer] {

  override def load(path: String): TFRecordSerializer = super.load(path)
}