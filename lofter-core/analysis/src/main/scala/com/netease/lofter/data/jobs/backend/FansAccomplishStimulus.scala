package com.netease.lofter.data.jobs.backend

import com.github.nscala_time.time.Imports._
import com.netease.lofter.data.common.kafkaConfig
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

object FansAccomplishStimulus {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    import spark.implicits._

    val date = pargs.optional("date").getOrElse(DateTime.now().toString("yyyy-MM-dd"))

    val fansAccomplishSql =
     s"""
        |select *, unix_timestamp() * 1000L as stimulusTime
        |from (
        |     select blogId as userId,
        |            currentFans as fans,
        |            case when currentFans >= 500000 and prevFans < 500000 then 500000
        |                 when currentFans >= 100000 and prevFans < 100000 then 100000
        |                 when currentFans >= 95000 and prevFans < 95000 then 95000
        |                 when currentFans >= 50000 and prevFans < 50000 then 50000
        |                 when currentFans >= 10000 and prevFans < 10000 then 10000
        |                 when currentFans >= 5000 and prevFans < 5000 then 5000
        |                 when currentFans >= 1000 and prevFans < 1000 then 1000
        |                 when currentFans >= 500 and prevFans < 500 then 500
        |                 when currentFans >= 100 and prevFans < 100 then 100
        |                 when currentFans >= 50 and prevFans < 50 then 50
        |                 end as fans_accomplish,
        |            nvl(prevFans,0) as prev_fans
        |         from (
        |           select blogId, dt,
        |                  sum(fans) over (partition by blogId order by dt ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) as currentFans,
        |                  nvl(sum(fans) over (partition by blogId order by dt ROWS BETWEEN UNBOUNDED PRECEDING AND 1 PRECEDING), 0) as prevFans
        |           from (
        |             select blogId, from_unixtime(cast(followTime/1000 as bigint), 'yyyy-MM-dd') as dt, count(distinct userId) fans
        |             from lofter_db_dump.ods_db_user_following_nd
        |             where from_unixtime(cast(followTime/1000 as bigint), 'yyyy-MM-dd') <= '$date'
        |             group by blogId, from_unixtime(cast(followTime/1000 as bigint), 'yyyy-MM-dd')
        |           ) tt
        |         )
        |         where dt = '$date'
        |) t
        |where fans_accomplish > 0
        |""".stripMargin

    spark.sql(fansAccomplishSql)
      .selectExpr("CAST(userId as STRING) as key", """concat('{"userId":', userId, ',"time":', stimulusTime, ',"stimulateType": "fans_accomplish"', ',"data": {"fans": ', fans, ', "prev_fans":', prev_fans,', "fans_accomplish": ', fans_accomplish , '}}') as value""")
      .write
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaConfig.BOOTSTRAP_SERVERS)
      .option("topic", "lofter.creator-stimulus-pm.staging")
      .save()

    spark.stop()
  }
}
