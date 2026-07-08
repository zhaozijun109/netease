package com.netease.easyml.uds.examples

import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.{IOUtil, SparkUtil, StringUtil}
import com.netease.easyml.uds.util.RankUtil._
import org.apache.spark.sql.SparkSession

/**
 * Created by linjiuning on 2021/3/9.
 * Count String to index Vocabulary
 *
 * params:
 * input: input table
 * output: output dir
 * config: feature config file
 * minCount: min count, default 50.
 */
case class RankCountIdsArgs(input: String, output: String, config: String,
                            startDay: String = "", endDay: String = "", minCount: Int = MIN_COUNT,
                            doFilter: Boolean = true, isFea: Boolean = true,
                            recordType: String = EXAMPLE)


object RankCountIdsUDS extends UDS[RankCountIdsArgs] {

  def run(spark: SparkSession, args: Args): Unit = {
    var configs = readConfig(args.config, minCount = args.minCount).filterNot(_.embedName.isEmpty)
    if (args.isFea) {
      configs = configs.filter(_.isFea)
    }

    val isTfrecord = IOUtil.isDirectory(args.input)
    val df = if (isTfrecord) {
      val keys = configs.map(it => if (it.format.equals("kv")) it.name + KV_FEA_SUFFIX_IDS else it.name).mkString(",")
      loadTfRecords(spark, args.input, keys = keys, startDay = args.startDay, endDay = args.endDay, recordType = args.recordType, source = "tfrecordv2")
    } else {
      val keys = configs.map(_.name).mkString(",")
      SparkUtil.loadFromTable(spark, args.input, keys = keys, startDay = args.startDay, endDay = args.endDay)
    }

    val embedNameIndex = configs.map(_.embedName).distinct.zipWithIndex.toMap
    val rEmbedNameIndex = embedNameIndex.map(it => (it._2, it._1))
    val embedNameIndexBc = spark.sparkContext.broadcast(embedNameIndex)
    val rEmbedNameIndexBc = spark.sparkContext.broadcast(rEmbedNameIndex)

    val nameIndex = configs.map(_.name).distinct.zipWithIndex.toMap
    val nameIndexBc = spark.sparkContext.broadcast(nameIndex)

    val flatDf = df.toDF.rdd.flatMap(row => {
      configs.flatMap(it => {
        val name = it.name
        val key = it.embedName
        val isTfrecordKv = isTfrecord && it.format.equals("kv")
        val i = if (isTfrecordKv) row.fieldIndex(name + KV_FEA_SUFFIX_IDS) else row.fieldIndex(name)
        val value = row.get(i)
        val words = if (value != null) {
          if (isTfrecordKv) {
            row.getSeq[String](i)
          } else {
            it.format match {
              case "string" =>
                Seq(row.getString(i))
              case "list_string" =>
                row.getSeq[String](i)
              case "kv" =>
                row.getSeq[String](i).map(str => {
                  val j = str.lastIndexOf(":")
                  if (j >= 0) {
                    str.slice(0, j)
                  } else {
                    ""
                  }
                })
              case "nested_list_string" =>
                row.getSeq[String](i).flatMap(str => str.split(","))
              case _ =>
                Seq()
            }
          }
        } else {
          Seq()
        }
        val keyId = embedNameIndexBc.value(key)
        val nameId = nameIndexBc.value(name)
        words.map(it => StringUtil.strip(it))
          .filter(_.nonEmpty)
          .map(it => (keyId, nameId, it))
      })
    })

    if (args.doFilter) {
      val minCounts = configs.map(it => (it.embedName, it.minCount))
        .groupBy(_._1)
        .mapValues(it => it.map(_._2).max)
        .map(it => (embedNameIndexBc.value(it._1), it._2))

      val minCountsBc = spark.sparkContext.broadcast(minCounts)

      val vocabsRdd = flatDf.map((_, 1L))
        .reduceByKey(_ + _)
        .filter(it => it._2 >= minCountsBc.value(it._1._1))
        .map(it => (it._1._1, it._1._3))
        .aggregateByKey(Set.empty[String])(
          seqOp = (set, word) => set + word,
          combOp = (set1, set2) => set1 ++ set2
        )
      val vocabs = vocabsRdd.collect()
      if (!IOUtil.exists(args.output)) {
        IOUtil.mkdirs(args.output)
      }

      vocabs.foreach {
        case (keyId, words) =>
          val key = rEmbedNameIndex(keyId)
          val outPath = IOUtil.join(args.output, key + ".txt")
          if (IOUtil.exists(outPath)) {
            IOUtil.delete(outPath)
          }
          spark.sparkContext.parallelize(words.toSeq, 1).saveAsTextFile(outPath)
      }
    } else {
      val vocabsDf = flatDf.map((_, 1L))
        .reduceByKey(_ + _)
        .map(it => ((it._1._1, it._1._3), it._2))
        .reduceByKey((a, b) => Math.max(a, b))
        .map(it => (rEmbedNameIndexBc.value(it._1._1), it._1._2, it._2))

      import spark.implicits._

      vocabsDf.toDF("type", "word", "counts").createOrReplaceTempView("t")
      SparkUtil.setDynamicPartition(spark)

      spark.sql(s"insert overwrite table ${args.output} partition (day, type)" +
        s"select word, counts, '${args.endDay}' as day, type from t")
    }
  }
}
