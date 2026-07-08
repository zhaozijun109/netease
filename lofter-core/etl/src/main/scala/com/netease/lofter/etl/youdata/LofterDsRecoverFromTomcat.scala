package com.netease.lofter.etl.youdata

import com.github.nscala_time.time.Imports
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

object LofterDsRecoverFromTomcat {

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Ds Recover From Tomcat")
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(Imports.DateTime.yesterday.toString("yyyy-MM-dd"))

    import org.apache.spark.sql.types._
    val schema_new = StructType(Array(StructField("actiontype",StringType,false),
      StructField("eventid",StringType,false),
      StructField("itemid",StringType,false),
      StructField("itemtype",StringType,false),
      StructField("userid",StringType,false),
      StructField("type",StringType,true)
    ))

    spark.read.json(s"/user/da_lofter/datastream/tomcat/online/$date").createOrReplaceTempView("tomcat_online")

    val sql_json =
      s"""
         |select body from (select a.body as body,b.* from tomcat_online a lateral view json_tuple(a.body,'actiontype','action') b as actiontype,action) c where actiontype is not null
         |and action not in ('click','page')
       """.stripMargin

    val df = spark.sql(sql_json)

    df.repartition(1).write.mode("overwrite").text(s"/user/da_lofter/hive/lofter_ds_behavior/dt=$date")

    //df.repartition(1).rdd.saveAsTextFile(s"/user/da_lofter/hive/lofter_ds_behavior/dt=$date",classOf[com.hadoop.compression.lzo.LzopCodec])
    spark.close()

  }

}
