package com.netease.lofter.etl.ods

import com.netease.lofter.common.HdfsUtil
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.lit

object SpmEtlJob {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Spm Etl")
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    import spark.implicits._

    val dt = pargs.required("date")

    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")

    val path = s"/user/da_lofter/datastream/spm/$dt"

    if (HdfsUtil.isHdfsPathEmpty(path)) {
      spark.close()
      return
    }

    val datasetWithoutApmHeader = spark.read.textFile(path).map(_.replaceFirst("""^\[[\S,]+\]\s*""", ""))

    spark.read.json(datasetWithoutApmHeader)
      .withColumn("dt", lit(dt))
      .select("productid", "skuid", "spm", "tradeid", "userid", "createtime", "paytime", "dt")
      .write
      .mode("overwrite")
      .insertInto("lofter.ods_log_spm_di")

    spark.stop()
  }
}
