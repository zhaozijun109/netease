package com.netease.wm.udf

import com.github.mjakubowski84.parquet4s.ParquetReader

import org.apache.hadoop.hive.ql.exec.{Description, UDF}

import scala.collection.JavaConverters._

case class TagIpMapping(tag: String, ip: String)

@Description(name = "ResolveTagIp", value = "convert tag array to ip array")
class ResolveTagIp extends UDF {
  val tag2IpMapping: Map[String, List[String]] = {
    val m =  ParquetReader.read[TagIpMapping]("hdfs://gy-cluster8/user/da_lofter/hive_db/lofter.db/dwd_tag_ip_mapping_nd")
    m.groupBy(_.tag)
      .mapValues { values =>
        values.map(_.ip).toList
      }.toMap
  }

  def evaluate(tags: java.util.List[String]): java.util.List[String] = {
    if(tags == null || tags.isEmpty) {
      null
    } else {
      val result = tags.asScala.flatMap(t => tag2IpMapping.get(t).getOrElse(Seq.empty)).toSet.toList.asJava
      if(result.isEmpty) null else result
    }
  }
}
