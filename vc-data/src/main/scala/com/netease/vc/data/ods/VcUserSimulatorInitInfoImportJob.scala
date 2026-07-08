package com.netease.vc.data.ods

import org.apache.spark.sql.{SaveMode, SparkSession}

object VcUserSimulatorInitInfoImportJob {
  def main(args: Array[String]): Unit = {
//    val pargs = Args(args)
//    val dt = pargs.required("date")

    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val adConfigDf = spark.read
      .format("jdbc")
      .option("url", "jdbc:mysql://10.46.246.75:6000/vcharacter_online?connectTimeout=5000&socketTimeout=1800000&characterEncoding=utf-8&allowMultiQueries=true")
      .option("dbtable", "vc_user_simulator_init_info")
      .option("user", "vcharacter_online_ro")
      .option("password", "@kKsE_J71")
      .option("driver","com.mysql.jdbc.Driver")
      .load()

    adConfigDf.write.mode(SaveMode.Overwrite)
      .saveAsTable("vc.ods_db_rt1h_vc_user_simulator_init_info_nd")

    spark.stop()
  }
}
