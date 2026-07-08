package com.netease.easyml.ml.sklearn.feature_extraction

import com.netease.easyml.annotation.Register
import com.netease.easyml.ml.param.HasNorm
import com.netease.easyml.ml.sklearn.DefaultSklearnReader
import com.netease.easyml.ml.util.VectorUtils
import org.apache.spark.SparkException
import org.apache.spark.ml.Model
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.ml.param._
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.functions.{col, udf}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, Dataset}
import org.apache.spark.unsafe.hash.Murmur3_x86_32.{hashInt, hashLong, hashUnsafeBytes2}
import org.apache.spark.unsafe.types.UTF8String

import scala.collection.mutable

/**
 * Created by linjiuning on 2020/8/7.
 */
trait HashingVectorizerParams extends VectorizerParams with HasNorm {

  val nFeatures: IntParam =
    new IntParam(this, "nFeatures", "The number of features (columns) in the output matrices. Small numbers of features are likely to cause " +
      "hash collisions, but large numbers will cause larger coefficient dimensions in linear learners.", ParamValidators.gtEq(0))

  def getNFeatures: Int = $(nFeatures)

  val alternateSign: BooleanParam =
    new BooleanParam(this, "alternateSign", "When True, an alternating sign is added to the features as to approximately conserve the inner product in " +
      "the hashed space even for small n_features. This approach is similar to sparse random projection.")

  setDefault(nFeatures -> (1 << 20), norm -> "l2", alternateSign -> true)
}

@Register(prefix = "sklearn.")
class HashingVectorizer(override val uid: String)
  extends Model[HashingVectorizer] with HashingVectorizerParams with DefaultParamsWritable {

  import HashingVectorizer._

  def this() = {
    this(Identifiable.randomUID("hashingVec"))
  }

  def setInputCol(value: String): this.type = set(inputCol, value)

  def setOutputCol(value: String): this.type = set(outputCol, value)

  def setBinary(value: Boolean): this.type = set(binary, value)

  def setNgramRange(value: Array[Int]): this.type = set(ngramRange, value)

  def setLowercase(value: Boolean): this.type = set(lowercase, value)

  def setTokenPattern(value: String): this.type = set(tokenPattern, value)

  def setAnalyzer(value: String): this.type = set(analyzer, value)

  def setNFeatures(value: Int): this.type = set(nFeatures, value)

  def setAlternateSign(value: Boolean): this.type = set(alternateSign, value)

  def setNorm(value: String): this.type = set(norm, value)

  private lazy val tokenize = TextUtil.buildTokenizer($(tokenPattern))
  private lazy val p = getNormP

  def analyzeDoc(text: String): Seq[String] = {
    val Array(minN, maxN) = $(ngramRange)
    TextUtil.analyze(text, $(analyzer), minN, maxN, $(lowercase), None, Some(tokenize))
  }


  override def transform(dataset: Dataset[_]): DataFrame = {
    transformSchema(dataset.schema, logging = true)
    val vectorizer = udf { (text: String) =>
      val document = analyzeDoc(text)

      val termFrequencies = mutable.HashMap.empty[Int, Double]
      val setTF = if ($(binary)) (i: Int, j: Double) => 1.0 else (i: Int, j: Double) => termFrequencies.getOrElse(i, 0.0) + j
      document.foreach { term =>
        val h = murmur3Hash(term)
        val i = Math.abs(h) % $(nFeatures)
        termFrequencies.put(i, setTF(i, if ($(alternateSign) && h < 0) -1.0 else 1.0))
      }
      val vector = Vectors.sparse($(nFeatures), termFrequencies.toSeq.filter(_._2 != 0))
      VectorUtils.normalize(vector, p)
    }
    dataset.withColumn($(outputCol), vectorizer(col($(inputCol))))
  }

  override def transformSchema(schema: StructType): StructType = {
    validateAndTransformSchema(schema)
  }

  override def copy(extra: ParamMap): HashingVectorizer = {
    defaultCopy(extra)
  }

}

object HashingVectorizer extends DefaultParamsReadable[HashingVectorizer] with DefaultSklearnReader[HashingVectorizer] {

  val seed = 0

  /**
   * Calculate a hash code value for the term object using
   * Austin Appleby's MurmurHash 3 algorithm (MurmurHash3_x86_32).
   * This is the default hash algorithm used from Spark 2.0 onwards.
   */
  def murmur3Hash(term: Any): Int = {
    term match {
      case null => seed
      case b: Boolean => hashInt(if (b) 1 else 0, seed)
      case b: Byte => hashInt(b, seed)
      case s: Short => hashInt(s, seed)
      case i: Int => hashInt(i, seed)
      case l: Long => hashLong(l, seed)
      case f: Float => hashInt(java.lang.Float.floatToIntBits(f), seed)
      case d: Double => hashLong(java.lang.Double.doubleToLongBits(d), seed)
      case s: String =>
        val utf8 = UTF8String.fromString(s)
        // hashUnsafeBytes2 is consistent with sklearn
        hashUnsafeBytes2(utf8.getBaseObject, utf8.getBaseOffset, utf8.numBytes(), seed)
      case _ => throw new SparkException("HashingTF with murmur3 algorithm does not " +
        s"support type ${term.getClass.getCanonicalName} of input data.")
    }
  }

  override def load(path: String): HashingVectorizer = super.load(path)
}
