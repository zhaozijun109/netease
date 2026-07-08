package com.netease.easyml.ml.feature

import com.netease.easyml.ml.util.SchemaUtils
import org.apache.spark.ml.Transformer
import org.apache.spark.ml.linalg.{DenseVector, SQLDataTypes, SparseVector, Vector, Vectors}
import org.apache.spark.ml.param.ParamMap
import org.apache.spark.ml.param.shared.{HasInputCol, HasOutputCol}
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.functions.{col, udf}
import org.apache.spark.sql.types.{StructField, StructType}
import org.apache.spark.sql.{DataFrame, Dataset}

/**
 * Created by linjiuning on 2020/7/20.
 */
class ToDenseVector(override val uid: String) extends Transformer
  with HasInputCol with HasOutputCol with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("to_dense"))

  def setInputCol(value: String): this.type = set(inputCol, value)

  def setOutputCol(value: String): this.type = set(outputCol, value)

  override def transform(dataset: Dataset[_]): DataFrame = {
    val schema = dataset.schema
    SchemaUtils.checkArrayOrVectorType(schema, $(inputCol))

    def toVector(y: Any): Vector = {
      y match {
        case vec: DenseVector => vec
        case vec: SparseVector => vec.toDense
        case arr: Seq[java.lang.Number] => Vectors.dense(arr.map(_.doubleValue()).toArray)
      }
    }

    val toVectorUdf = udf(toVector _)

    dataset.withColumn($(outputCol), toVectorUdf(col($(inputCol))))
  }

  override def copy(extra: ParamMap): Transformer = defaultCopy(extra)

  override def transformSchema(schema: StructType): StructType = {
    val outDataType = SQLDataTypes.VectorType
    val field = StructField($(outputCol), outDataType, false)
    if ($(inputCol).equals($(outputCol))) {
      val idx = schema.fieldIndex($(inputCol))
      schema.fields(idx) = field
      StructType(schema.fields)
    } else {
      StructType(schema.fields ++ Array(field))
    }
  }
}

object ToDenseVector extends DefaultParamsReadable[ToDenseVector] {
  override def load(path: String): ToDenseVector = super.load(path)
}
