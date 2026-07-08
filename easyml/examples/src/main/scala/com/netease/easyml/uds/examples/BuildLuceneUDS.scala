package com.netease.easyml.uds.examples

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.serializer.SerializeFilter
import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.lucene.LuceneConfig
import com.netease.easyml.common.util.{ConvertUtil, IOUtil, SparkUtil, StringUtil}
import com.netease.easyml.ml.util.SchemaUtils
import com.netease.easyml.uds.util.Constant.NULL
import org.apache.lucene.analysis.core.WhitespaceAnalyzer
import org.apache.lucene.index.{IndexWriter, IndexWriterConfig, Term}
import org.apache.lucene.store.FSDirectory
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.concat_ws

import java.nio.file.Paths
import scala.collection.JavaConverters._;

/**
 * Created by linjiuning on 2021/7/26.
 * Build lucene index
 * <p>
 * data schema:
 * [input] String or Numeric or Array[String]
 * [output] word: String, weight: Float
 * <p>
 * params:
 * input: input table
 * output: output path
 * inputCols: fields col name
 * config: config path
 */
case class BuildLuceneArgs(input: String, output: String, inputCols: String = NULL,
                           config: String, overwrite: Boolean = true, batchSize: Int = 128)

object BuildLuceneUDS extends UDS[BuildLuceneArgs] {
  val DOC_ID = "doc_id"

  def run(spark: SparkSession, args: Args): Unit = {
    val stream = if (IOUtil.exists(args.config)) {
      IOUtil.getInputStream(args.config)
    } else {
      IOUtil.getResourceAsStream(args.config)
    }
    val config = LuceneConfig.create(stream)

    if (!IOUtil.exists(args.output) || args.overwrite) {
      var df = SparkUtil.loadFromTable(spark, args.input, keys = args.inputCols.split(";").mkString(","))

      val cols = df.columns
      cols.foreach { col =>
        if (SchemaUtils.isArrayType(df.schema, col)) {
          df = df.withColumn(col, concat_ws(" ", df(col)))
        }
      }

      val documents = df.rdd.map(row => {
        cols.map(col => (col, row.getAs[Any](col)))
          .filter(_._2 != null)
          .map(it => (it._1, ConvertUtil.toString(it._2)))
      }).filter(_.nonEmpty)
        .collect()

      val tmp = IOUtil.createTemporaryDirectory("lucene")
      tmp.deleteOnExit()
      val tempDirectory = tmp.getCanonicalPath
      println(s"tempDirectory = $tempDirectory")

      val analyzer = new WhitespaceAnalyzer

      val indexWriterConfig = new IndexWriterConfig(analyzer)

      indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND)

      val startTime = System.currentTimeMillis

      val directory = FSDirectory.open(Paths.get(tempDirectory))
      val indexWriter = new IndexWriter(directory, indexWriterConfig)

      documents.grouped(args.batchSize)
        .foreach(batch => {
          batch.foreach(array => {
            val map = array.toMap
            val doc = config.document(map.asJava)
            val (key, docId) = if (!StringUtil.isEmpty(config.getId)) {
              val key = config.getId
              val docId = map(key)
              (key, docId)
            } else {
              val docId = StringUtil.md5(JSON.toJSONString(map, new Array[SerializeFilter](0)))
              (DOC_ID, docId)
            }
            indexWriter.updateDocument(new Term(key, docId), doc)
          })
          indexWriter.commit
        })
      indexWriter.close()
      val endTime = System.currentTimeMillis
      System.out.println("Cost: " + (endTime - startTime))

      if (IOUtil.exists(args.output)) {
        IOUtil.delete(args.output)
      }
      if (!IOUtil.exists(IOUtil.parentName(args.output))) {
        IOUtil.mkdirs(IOUtil.parentName(args.output))
      }
      IOUtil.copyDirectory(tempDirectory, args.output)
    } else {
      logWarning(String.format("path=%s already exist...", args.output))
    }
  }
}
