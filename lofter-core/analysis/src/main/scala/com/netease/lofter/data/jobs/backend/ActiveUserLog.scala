package com.netease.lofter.data.jobs.backend

import com.github.nscala_time.time.Imports.DateTime
import com.netease.lofter.data.common.databases
import com.netease.wm.util.Args
import org.apache.spark.sql.{Row, SparkSession}

import java.sql.Connection

object ActiveUserLog {
  val BATCH_SIZE = 100

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val dt = pargs.required("date")
    val dayAgo =  DateTime.parse(dt).minusDays(1).toString("yyyy-MM-dd")

    spark.sql("create temporary function resolve_ip as 'com.netease.wm.udf.ResolveIp'")

    val riskUsers =
      s"""
         |    select userId
         |    from (
         |        select userId, inline(Array(resolve_ip(ip))) as (country, province, city)
         |        from (
         |            select userId, ip
         |            from lofter.dwd_device_mapping_detail_di
         |            where dt = '$dt'
         |            group by userId, ip
         |       ) t0
         |    ) t1
         |    where province in ('北京市', '浙江省')
         |    group by userId
         |""".stripMargin

    // insert new deviceId ext
    spark.sql(riskUsers)
      .repartition(4)
      .foreachPartition { xs: Iterator[Row]=>
        import com.netease.lofter.data.common.spark.SparkSqlImplicits._
        import com.netease.wm.util.Sql._
        import databases.getDDBConn
        implicit val conn: Connection = getDDBConn

        try {
          xs.toSeq.grouped(BATCH_SIZE).foreach { rows =>
            val batch = rows.map(rowParam)
            sql"delete from User_ActiveLog where userId = ${"userId"}".batchUpdate(batch)
          }
        } finally {
          conn.close()
        }
      }

  }
}
