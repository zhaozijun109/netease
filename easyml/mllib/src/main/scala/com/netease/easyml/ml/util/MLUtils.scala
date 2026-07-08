package com.netease.easyml.ml.util

import java.lang.reflect.Method

import com.netease.easyml.common.util.{ConvertUtil, IOUtil, ResourceUtil, SparkUtil}
import com.netease.easyml.ml.sklearn.SklearnUtils
import org.apache.hadoop.fs.Path
import org.apache.spark.SparkContext
import org.apache.spark.internal.Logging
import org.apache.spark.ml.linalg.Vector
import org.apache.spark.ml.util_.{Utils => OldUtils}
import org.apache.spark.mllib.feature_.{Word2VecModel => OldWord2VecModel}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

import scala.collection.JavaConversions._

/**
 * Created by linjiuning on 2020/8/21.
 */
object MLUtils extends Logging {

  val WORD_COL = "word"
  val VECTOR_COL = "vector"
  val WEIGHT_COL = "weight"

  def getModelClass[T](clazz: Class[T]): Class[_] = {
    var method: Method = null
    try method = clazz.getDeclaredMethod("fit", classOf[Dataset[_]])
    catch {
      case _: NoSuchMethodException =>
        try method = clazz.getDeclaredMethod("train", classOf[Dataset[_]])
        catch {
          case _: NoSuchMethodException =>
            return null
        }
    }
    method.getReturnType
  }

  def read[T](clazz: Class[T], path: String): T = {
    if (path.endsWith(".pkl") || path.endsWith(".pickle")) {
      val model = SklearnUtils.read(path).asInstanceOf[T]
      if (model == null) {
        log.error(String.format("Failed to read from %s.", path))
      }
      model
    } else {
      try {
        val method = clazz.getDeclaredMethod("load", classOf[String])
        method.setAccessible(true)
        if (clazz.isAssignableFrom(method.getReturnType))
          return method.invoke(null, path).asInstanceOf[T]
      } catch {
        case _: Exception =>
          log.error(String.format("Failed to read from %s.", path))
      }
      null.asInstanceOf[T]
    }
  }

  def loadWordVec(path: String, delimiter: String = " "): Map[String, Array[Float]] = {
    ResourceUtil.loadWordVec(path, delimiter).toMap
  }

  def loadWordVecHive(spark: SparkSession, table: String,
                      wordCol: String = WORD_COL, vectorCol: String = VECTOR_COL): Map[String, Array[Float]] = {
    spark.sql(s"select $wordCol, $vectorCol from $table")
      .rdd.map(row => {
      val word = row.getString(0)
      val vector = row.getSeq[Float](1).toArray
      (word, vector)
    }).collectAsMap().toMap
  }

  def saveWordVec(path: String, wordVectors: Map[String, Array[Float]], delimiter: String = " "): Unit = {
    ResourceUtil.saveWordVec(path, wordVectors, delimiter)
  }

  def saveWordVecToHive(spark: SparkSession, table: String, wordVectors: RDD[(String, Array[Float])]): Unit = {
    import spark.implicits._

    val df = wordVectors.toDF(WORD_COL, VECTOR_COL)
    SparkUtil.saveAsTable(df, table)
  }

  def saveWordVecToHive(spark: SparkSession, table: String, wordVectors: OldWord2VecModel): Unit = {
    val rdd = wordVecToRDD(spark, wordVectors)
    saveWordVecToHive(spark, table, rdd)
  }

  def wordVecToRDD(spark: SparkSession, wordVectors: OldWord2VecModel): RDD[(String, Array[Float])] = {
    OldUtils.wordVecToRDD(spark, wordVectors)
  }

  def saveWordVecToHive(df: DataFrame, table: String, numPartitions: Option[Int] = None,
                        wordCol: String = WORD_COL, vectorCol: String = VECTOR_COL): Unit = {
    import df.sparkSession.implicits._
    var vecs = df.select(wordCol, vectorCol)
      .rdd
      .map(row => {
        val word = row.getString(0)
        val vector = row.get(1) match {
          case vector: Vector => vector.toArray.map(_.toFloat)
          case seq: Any => seq.asInstanceOf[Seq[_]].map(ConvertUtil.toFloat).toArray
        }
        (word, vector)
      }).toDF(WORD_COL, VECTOR_COL)

    if (numPartitions.nonEmpty) {
      vecs = vecs.coalesce(numPartitions.get)
    }
    SparkUtil.saveAsTable(vecs, table)
  }

  def saveWordVecToText(df: DataFrame, outPath: String, sep: String = " ", numPartitions: Option[Int] = None,
                        wordCol: String = WORD_COL, vectorCol: String = VECTOR_COL): Unit = {
    val vecs = df.select(wordCol, vectorCol)
      .rdd
      .map(row => {
        val word = row.getString(0)
        val vector = row.get(1) match {
          case vector: Vector => vector.toArray.mkString(sep)
          case seq: Any => seq.asInstanceOf[Seq[_]].mkString(sep)
        }
        word + sep + vector
      })

    if (!IOUtil.exists(IOUtil.parentName(outPath))) {
      IOUtil.mkdirs(IOUtil.parentName(outPath))
    } else {
      IOUtil.delete(outPath)
    }
    if (numPartitions.nonEmpty) {
      vecs.coalesce(numPartitions.get).saveAsTextFile(outPath)
    } else {
      vecs.saveAsTextFile(outPath)
    }
  }

  def loadWordWeight(path: String, delimiter: String = "\t"): Map[String, Float] = {
    ResourceUtil.loadWordWeight(path, delimiter).mapValues(_.toFloat).toMap
  }

  def loadWordWeightHive(spark: SparkSession, table: String): Map[String, Float] = {
    spark.sql(s"select $WORD_COL, $WEIGHT_COL from $table")
      .rdd.map(row => {
      val word = row.getString(0)
      val weight = row.getFloat(1)
      (word, weight)
    }).collectAsMap().toMap
  }

  def saveWordWeight(path: String, wordWeights: Map[String, Float], delimiter: String = "\t"): Unit = {
    ResourceUtil.saveWordWeight(path, wordWeights.mapValues(float2Float), delimiter)
  }

  def saveWordWeightToHive(spark: SparkSession, table: String, wordWeights: Seq[(String, Float)]): Unit = {
    import spark.implicits._

    val df = wordWeights.toDF(WORD_COL, WEIGHT_COL)
    SparkUtil.saveAsTable(df, table)
  }

  def saveWordWeightToHive(spark: SparkSession, table: String, wordWeights: RDD[(String, Float)]): Unit = {
    import spark.implicits._

    val df = wordWeights.toDF(WORD_COL, WEIGHT_COL)
    SparkUtil.saveAsTable(df, table)
  }

  val ANGEL_EMB_MAPPING: String = "mapping"
  val ANGEL_EMB_VEC: String = "embedding"

  def loadAngelWordVec(spark: SparkContext, path: String): (Array[String], Array[Float]) = {
    val vocab = spark.textFile(IOUtil.join(path, ANGEL_EMB_MAPPING))
      .map(line => {
        val i = line.indexOf(":")
        val id = line.substring(0, i)
        val word = line.substring(i + 1)
        //        val Array(word, id) = line.split(":")
        (id.toInt, word)
      }).collect()
      .sortBy(_._1)
      .map(_._2)

    val vecArrays = spark.textFile(IOUtil.join(path, ANGEL_EMB_VEC))
      .flatMap(line => {
        try {
          val Array(id, vec) = line.split(":")
          val vecArr = vec.split(" ").map(_.toFloat)
          Some((id.toInt, vecArr))
        } catch {
          case _: Exception => None
        }
      }).collect()
      .sortBy(_._1)
      .map(_._2)

    require(vocab.length == vecArrays.length, "Vocab size must be the same.")

    if (vecArrays.isEmpty) {
      (Array.empty[String], Array.empty[Float])
    } else {
      require(vecArrays.map(_.length).distinct.length == 1, "Vector size must be the same.")
      val size = vecArrays(0).length
      val totalSize = vocab.length * size
      val vector = new Array[Float](totalSize)
      var offset = 0
      for (vec <- vecArrays) {
        Array.copy(vec, 0, vector, offset, size)
        offset += size
      }
      (vocab, vector)
    }
  }

  def angelWordVecToText(spark: SparkSession, angelPath: String, outPath: String, sep: String = " ", numPartitions: Option[Int] = None): Unit = {
    val vocab = spark.sparkContext.textFile(IOUtil.join(angelPath, ANGEL_EMB_MAPPING))
      .map(line => {
        val i = line.indexOf(":")
        val id = line.substring(0, i)
        val word = line.substring(i + 1)
        //        val Array(word, id) = line.split(":")
        (id.toInt, word)
      }).collectAsMap()

    val vocabBc = spark.sparkContext.broadcast(vocab)

    val vecs = spark.sparkContext.textFile(IOUtil.join(angelPath, ANGEL_EMB_VEC))
      .mapPartitions(iter => {
        val vocab = vocabBc.value
        iter.map(line => {
          try {
            val Array(id, vec) = line.split(":")
            val word = vocab(id.toInt)
            if (sep.equals(" "))
              word + sep + vec
            else
              word + sep + vec.replaceAll(" ", sep)
          } catch {
            case _: Exception => null
          }
        }).filter(_ != null)
      })
    if (!IOUtil.exists(IOUtil.parentName(outPath))) {
      IOUtil.mkdirs(IOUtil.parentName(outPath))
    } else {
      IOUtil.delete(outPath)
    }
    if (numPartitions.nonEmpty) {
      vecs.coalesce(numPartitions.get).saveAsTextFile(outPath)
    } else {
      vecs.saveAsTextFile(outPath)
    }
    vocabBc.destroy()
  }

  def angelWordVecToHive(spark: SparkSession, angelPath: String, outTable: String): Unit = {
    val vocab = spark.sparkContext.textFile(IOUtil.join(angelPath, ANGEL_EMB_MAPPING))
      .map(line => {
        val i = line.indexOf(":")
        val id = line.substring(0, i)
        val word = line.substring(i + 1)
        //        val Array(word, id) = line.split(":")
        (id.toInt, word)
      }).collectAsMap()

    val vocabBc = spark.sparkContext.broadcast(vocab)
    import spark.implicits._
    val vecsDf = spark.sparkContext.textFile(IOUtil.join(angelPath, ANGEL_EMB_VEC))
      .mapPartitions(iter => {
        val vocab = vocabBc.value
        iter.map(line => {
          try {
            val Array(id, vec) = line.split(":")
            val word = vocab(id.toInt)
            val vecArr = vec.split(" ").map(_.toFloat)
            (word, vecArr)
          } catch {
            case _: Exception => null
          }
        }).filter(_ != null)
      }).toDF(WORD_COL, VECTOR_COL)
    SparkUtil.saveAsTable(vecsDf, outTable)
    vocabBc.destroy()
  }

  def parquetWordVecToText(spark: SparkSession, inPath: String, outPath: String, sep: String = " ", numPartitions: Option[Int] = None): Unit = {
    val dataPath = new Path(inPath, "data").toString
    val df = spark.read.parquet(dataPath)

    saveWordVecToText(df, outPath, sep, numPartitions)
  }

  def parquetWordVecToHive(spark: SparkSession, inPath: String, outTable: String): Unit = {
    val dataPath = new Path(inPath, "data").toString
    val vecsDf = spark.read.parquet(dataPath)
      .select(WORD_COL, VECTOR_COL)

    SparkUtil.saveAsTable(vecsDf, outTable)
  }
}
