package com.netease.easyudf.cmd

import com.netease.easyml.common.cmds.UserDefinedCmd
import com.netease.easyml.common.util.IOUtil
import com.netease.easyudf.udf.util.{HashStringToIntUDF, Utils}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.storage.StorageLevel

case class GraphLearnNodeArgs(input: String, path: String, keys: String,
                              keyDelimiter: String = "@", attrDelimiter: String = "\\x01", arrayDelimiter: String = "\\x02",
                              naValue: String = "\\N", overwrite: Boolean = false, uniqueCheck: Boolean = true,
                              nbytes: Int = 8, hashAlg: String = "SHA-256", numPartitions: Int = 1
                             )

class GraphLearnNode extends UserDefinedCmd[GraphLearnNodeArgs] {

  override def apply(spark: SparkSession, args: GraphLearnNodeArgs): DataFrame = {
    def stringify(value: Any): String = {
      if (value == null) {
        return args.naValue
      }
      value match {
        case seq: Seq[_] => seq.map(stringify).mkString(args.arrayDelimiter)
        case any: Any => Utils.toString(any)
      }
    }

    val keys = args.keys.split(",")
    var df = spark.table(args.input)
    val keyIdx = keys.map(df.columns.indexOf)
    val valIdx = df.columns.indices.filterNot(keyIdx.contains)
    IOUtil.mkParentDirs(args.path)
    if (args.overwrite && IOUtil.exists(args.path)) {
      IOUtil.delete(args.path)
    }

    import spark.implicits._

    df = df.rdd.map(row => {
      val key = keyIdx.map(i => stringify(row.get(i))).mkString(args.keyDelimiter)
      val hashKey = if (args.nbytes > 0) {
        HashStringToIntUDF.hash(key, args.hashAlg, args.nbytes)
      } else {
        Utils.toLong(key)
      }
      val value = valIdx.map(i => stringify(row.get(i))).mkString(args.attrDelimiter)
      (hashKey, hashKey + "\t" + value)
    }).toDF("key", "kv")
      .persist(StorageLevel.MEMORY_AND_DISK)

    if (args.uniqueCheck) {
      val (count, unique) = df.selectExpr("count(1)", "count(distinct key)").map(row => (row.getLong(0), row.getLong(1))).collect().last
      println(s"Record num = $count, unique = $unique")
      assert(count == unique)
    } else {
      println(s"Record num = ${df.count()}")
    }

    df = df.select("kv")
    if (args.numPartitions == 1)
      df = df.coalesce(1)
    else
      df = df.repartition(args.numPartitions)
    df.write.text(args.path)

    null
  }
}
