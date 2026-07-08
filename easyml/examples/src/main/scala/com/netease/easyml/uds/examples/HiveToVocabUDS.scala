package com.netease.easyml.uds.examples

import com.netease.easyml.common.uds.UDS
import com.netease.easyml.common.util.IOUtil
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col, length}

import scala.collection.JavaConverters._

/**
 * Created by linjiuning on 2021/6/23.
 * Write hive to tf vocab file
 * <p>
 * data schema:
 * [input] word: String
 * <p>
 * params:
 * input: input table
 * output: output hdfs path
 * wordCol: word column name
 * numPartitions: output partitions
 */
case class HiveToVocabArgs(input: String, output: String, wordCol: String, numPartitions: Int = -1)

object HiveToVocabUDS extends UDS[HiveToVocabArgs] {

  def run(spark: SparkSession, args: Args): Unit = {
    val df = spark.sql(s"select ${args.wordCol} from ${args.input}")

    val words = df.filter(col(args.wordCol).isNotNull.and(length(col(args.wordCol)) > 0))
      .rdd.map(row => row.getString(0))

    if (IOUtil.exists(args.output)) {
      IOUtil.delete(args.output)
    } else {
      IOUtil.mkParentDirs(args.output)
    }

    if (args.numPartitions < 0) {
      words.saveAsTextFile(args.output)
    } else if (args.numPartitions > 1) {
      words.coalesce(args.numPartitions).saveAsTextFile(args.output)
    } else {
      val lines = words.collect()
      IOUtil.writeLines(args.output, lines.toList.asJava)
    }
  }
}
