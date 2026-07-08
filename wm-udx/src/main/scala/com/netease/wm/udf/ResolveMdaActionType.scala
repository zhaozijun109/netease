package com.netease.wm.udf

import com.github.mjakubowski84.parquet4s.ParquetReader

import org.apache.hadoop.hive.ql.exec.{Description, UDF}

import scala.collection.JavaConverters._

case class EventActionTypeMapping(eventid: String, `type`: String, enable: Int)

@Description(name = "ResolveMdaActionType", value = "resolve mda event action type")
class ResolveMdaActionType extends UDF {
  lazy val event2ActionTypeMapping: Map[String, String] = {
    val m =  ParquetReader.read[EventActionTypeMapping]("hdfs://gy-cluster8/user/da_lofter/hive_db/lofter_db_dump.db/ods_db_data_analyse_point_nd")
    m.filter(_.enable > 0)
      .map { row =>
        row.eventid -> row.`type`
      }.toMap
  }

  def evaluate(eventId: String): String = {
    if(eventId == null) {
      null
    } else {
      event2ActionTypeMapping.getOrElse(eventId, "other")
    }
  }
}
