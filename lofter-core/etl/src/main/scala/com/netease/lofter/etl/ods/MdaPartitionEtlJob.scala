package com.netease.lofter.etl.ods

import com.github.mjakubowski84.parquet4s.ParquetReader
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

object MdaPartitionEtlJob {

  case class EventActionTypeMapping(eventid: String, `type`: String, enable: Int)

  lazy val event2ActionTypeMapping: Map[String, String] = {
    val m =  ParquetReader.read[EventActionTypeMapping]("hdfs://gy-cluster8/user/da_lofter/hive_db/lofter_db_dump.db/ods_db_data_analyse_point_nd")
    m.filter(_.enable > 0)
      .map { row =>
        row.eventid -> row.`type`
      }.toMap
  }

  def resolveMdaActionType(eventId: String): Option[String] = {
    eventId match {
      case _ if eventId.startsWith("w") => Some("benefit_market")
      case _ if eventId.startsWith("ad-") => Some("advertisement")
      case _ if eventId.startsWith("sc-") => Some("bookstore")
      case _ if eventId.startsWith("da_") => Some("system")
      case _ => event2ActionTypeMapping.get(eventId)
    }
  }

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Mda Etl")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    import spark.implicits._

    val dt = pargs.required("date")

    spark.sqlContext.udf.register("resolve_mda_action_type", (eventId: String) => resolveMdaActionType(eventId).getOrElse("other"))

    val outputPath = s"/user/da_lofter/hive_db/lofter.db/ods_mda_app_di/dt=$dt"

    val sql =
      s"""
        |    select eventId,deviceUdid,userId,userType,userName,appVersion,appChannel,occurTime,category,label,customUDID,
        |           sessionUuid,ip,devicePlatform,deviceOs,deviceOsVersion,deviceModel,deviceAdid,deviceIdfv,deviceImei,
        |           deviceCarrier,appKey,city,costTime,source,deviceAndroidId,
        |           itemId,itemType,recId,scene,action,tagName,layout,algInfo,
        |           params,
        |           kafkaTime,isBeta,oaid,tdid,
        |           resolve_mda_action_type(eventId) as actiontype
        |    from lofter.ods_mda_app_raw_di
        |    where dt = '$dt' and eventId is not null
        |""".stripMargin

    spark.sql(sql)
      .coalesce(600)
      .sortWithinPartitions('deviceOs, 'deviceUdid, 'occurTime)
      .write.mode("overwrite")
      .parquet(outputPath)

    spark.sql(s"alter table lofter.ods_mda_app_di add if not exists partition(dt = '$dt') location '$outputPath' ")
  }
}
