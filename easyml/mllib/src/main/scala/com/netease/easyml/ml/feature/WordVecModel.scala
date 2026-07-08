package com.netease.easyml.ml.feature

/**
 * Created by linjiuning on 2020/8/21.
 */

import java.util
import java.util.function.Supplier

import com.hankcs.hanlp.corpus.tag.Nature
import com.hankcs.hanlp.dictionary.CoreDictionary
import com.hankcs.hanlp.seg.Other.AhoCorasickDoubleArrayTrieSegment
import com.hankcs.hanlp.seg.Segment
import com.netease.easyml.common.resource.ResourceManager
import com.netease.easyml.common.util.IOUtil
import com.netease.easyml.ml.util._
import org.apache.hadoop.fs.Path
import org.apache.spark.ml.Model
import org.apache.spark.ml.feature.Word2VecModel
import org.apache.spark.ml.linalg.{Vector, Vectors}
import org.apache.spark.ml.param.shared.{HasInputCol, HasOutputCol}
import org.apache.spark.ml.param.{BooleanParam, ParamMap, Params}
import org.apache.spark.ml.util.{SchemaUtils => _, _}
import org.apache.spark.mllib.feature.{Word2VecModel => OldWord2VecModel}
import org.apache.spark.sql.functions.{col, udf}
import org.apache.spark.sql.types.{ArrayType, StringType, StructType}
import org.apache.spark.sql.{DataFrame, Dataset}

import scala.collection.JavaConverters._


trait WordVecParams extends Params with HasInputCol with HasOutputCol {
  val segment = new BooleanParam(this, "segment", "whether do segment for oov.")

  def getSegment: Boolean = $(segment)

  protected def validateAndTransformSchema(schema: StructType): StructType = {
    SchemaUtils.checkColumnType(schema, $(inputCol), ArrayType(StringType))
    SchemaUtils.appendColumn(schema, $(outputCol), SchemaUtils.vectorUDT)
  }

  setDefault(segment, false)
}

class WordVecModel(override val uid: String, val wordVectors: Map[String, Array[Float]])
  extends Model[WordVecModel] with WordVecParams with MLWritable {

  import WordVecModel._

  def this(wordVectors: Map[String, Array[Float]]) = {
    this(Identifiable.randomUID("wv"), wordVectors)
  }

  def setSegment(value: Boolean): this.type = set(segment, value)

  def setInputCol(value: String): this.type = set(inputCol, value)

  def setOutputCol(value: String): this.type = set(outputCol, value)

  override def write: MLWriter = new WordVecModelWriter(this)

  lazy val vectorSize: Int = wordVectors.head._2.length

  lazy val vocabs: Set[String] = wordVectors.keySet

  def transformOne(word: String): Option[Vector] = {
    val array = if (wordVectors.contains(word)) {
      wordVectors(word)
    } else if ($(segment)) {
      val segment = getOrCreate(uid, vocabs)
      val emb = Array.emptyFloatArray
      var count = 0
      segment.seg(word).asScala.map(_.word).filter(wordVectors.contains).foreach(token => {
        val arr = wordVectors(token)
        count += 1
        if (emb.isEmpty) {
          arr
        } else {
          for (i <- arr.indices) {
            emb(i) += arr(i)
          }
        }
      })
      if (count > 0) {
        for (i <- emb.indices) {
          emb(i) /= count
        }
      }
      emb
    } else {
      Array.emptyFloatArray
    }
    if (array.isEmpty) {
      None
    } else {
      Some(Vectors.dense(array.map(_.toDouble)))
    }
  }

  override def copy(extra: ParamMap): WordVecModel = defaultCopy(extra)

  override def transform(dataset: Dataset[_]): DataFrame = {
    transformSchema(dataset.schema, logging = true)

    val transformUdf = udf {
      word: String => transformOne(word).getOrElse(Vectors.sparse(vectorSize, Array.emptyIntArray, Array.emptyDoubleArray))
    }
    dataset.withColumn($(outputCol), transformUdf(col($(inputCol))))
  }

  override def transformSchema(schema: StructType): StructType = validateAndTransformSchema(schema)
}

object WordVecModel extends MLReadable[WordVecModel] {

  def getOrCreate(uid: String, words: Set[String]): Segment = {
    ResourceManager.getOrCreate(uid, new Supplier[Segment] {
      override def get(): Segment = {
        val storage = new util.TreeMap[String, CoreDictionary.Attribute]
        val nature = new CoreDictionary.Attribute(Nature.n)
        for (word <- words) {
          storage.put(word, nature)
        }
        new AhoCorasickDoubleArrayTrieSegment(storage)
      }
    })
  }

  private class WordVecModelWriter(instance: WordVecModel) extends MLWriter {
    override protected def saveImpl(path: String): Unit = {
      DefaultEasyMLParamsWriter.saveMetadata(instance, path, sc)
      val dataPath = new Path(path, "data").toString

      val wordVectors = new OldWord2VecModel(instance.wordVectors)
      Utils.newWord2VecModel(instance.uid, wordVectors).save(dataPath)
    }
  }

  private class WordVecModelReader extends MLReader[WordVecModel] {

    private val className = classOf[Word2VecModel].getName

    override def load(path: String): WordVecModel = {
      if (IOUtil.isDirectory(path)) {
        val metadata = DefaultEasyMLParamsReader.loadMetadata(path, sc, className)
        val dataPath = new Path(path, "data").toString
        val w2v = Word2VecModel.load(dataPath)
        val clazz = classOf[Word2VecModel]
        val field = clazz.getDeclaredField("wordVectors")
        field.setAccessible(true)
        val oldW2v = field.get(w2v).asInstanceOf[OldWord2VecModel]
        val model = new WordVecModel(metadata.uid, oldW2v.getVectors)
        DefaultEasyMLParamsReader.getAndSetParams(model, metadata)
        model
      } else if (IOUtil.isFile(path)) {
        val wordVectors = MLUtils.loadWordVec(path)
        new WordVecModel(wordVectors)
      } else {
        val wordVectors = MLUtils.loadWordVecHive(sparkSession, path)
        new WordVecModel(wordVectors)
      }
    }
  }

  override def read: MLReader[WordVecModel] = new WordVecModelReader

  override def load(path: String): WordVecModel = super.load(path)
}