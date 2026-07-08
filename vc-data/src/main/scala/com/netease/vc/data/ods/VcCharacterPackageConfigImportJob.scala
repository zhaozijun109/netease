package com.netease.vc.data.ods

import com.netease.wm.util.Args
import org.apache.spark.sql.{SaveMode, SparkSession}

object VcCharacterPackageConfigImportJob {
  def main(args: Array[String]): Unit = {
//    val pargs = Args(args)
//    val dt = pargs.required("date")

    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val adConfigDf = spark.read
      .format("jdbc")
      .option("url", "jdbc:mysql://vcharacter-mysql-rw-online.db.gy.ntes:4331/vcharacter?characterEncoding=utf-8&useSSL=false&autoReconnect=true&useAffectedRows=true&useLegacyDatetimeCode=false&useTimezone=true&serverTimezone=Asia/Shanghai")
      .option("dbtable", "vc_character_package_config")
      .option("user", "online_algorithm_r_user")
      .option("password", "_FN68@jDm")
      .option("driver","com.mysql.jdbc.Driver")
      .load()
      .filter("status > 0 and id >= 1000")

    adConfigDf.write.mode(SaveMode.Overwrite)
      .saveAsTable("vc.ods_db_rt1h_vc_character_package_config_nd")

    spark.stop()
  }
}
