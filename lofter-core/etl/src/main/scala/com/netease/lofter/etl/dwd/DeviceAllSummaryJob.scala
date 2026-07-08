package com.netease.lofter.etl.dwd

import com.github.nscala_time.time.Imports.DateTime
import org.apache.spark.sql.{SaveMode, SparkSession}

object DeviceAllSummaryJob {
  val INPUT_PATH_PREFIX = "/user/da_lofter/hive_db/lofter.db/dwd_device_all_dd"

  def main(args: Array[String]): Unit = {
    val day = args(0)
    val yesterday = DateTime.parse(day).minusDays(1).toString("yyyy-MM-dd")
    val spark = SparkSession.builder()
      .appName("device all summary job")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .getOrCreate()

    spark.read
      .parquet(s"$INPUT_PATH_PREFIX/dt=$yesterday")
      .createOrReplaceTempView("dwd_device_all_yesterday")

    val sql_result =
      s"""
         |select a.deviceudid,deviceOs,a.firstAccessTime as first_active_time,
         |    b.userIds,last_active_date,if(last_active_date='$day',1,0) as is_active_today,
         |    c.last_return_date as last_return_date,
         |    case when d.last_dt='$day' then d.device_type
         |         when datediff('$day',b.last_active_date)>30 or b.last_active_date is null then 'lost'
         |         else 'remain' end as device_type,
         |    d.origin_type,origin_channel,d.origin_actpwd,d.last_dt as last_growth_date
         |from
         |(select deviceudid,lower(deviceOs) as deviceOs,min(firstAccessTime) as firstAccessTime
         |from lofter.dwd_par_device_all_dd
         |where dt='$day'
         |group by 1,2) a
         |
         |left join
         |(select deviceudid, collect_set(userId) as userIds,max(dt) as last_active_date
         |from
         |(select deviceudid,userids,dt
         |from lofter.device_active where dt = '$day'
         |union all
         |select deviceudid,userids,last_active_date as dt
         |from dwd_device_all_yesterday
         |where last_active_date is not null) a
         |lateral view outer explode(userids) as userId
         |group by 1 )  b
         |on a.deviceudid=b.deviceudid
         |
         |left join
         |(select deviceudid, max(dt) as last_return_date
         |from
         |(select deviceudid,dt from lofter.device_return
         |where period=30 and dt = '$day'
         |union all
         |select deviceudid,last_return_date as dt
         |from dwd_device_all_yesterday
         |where last_return_date is not null) a
         |group by 1)  c
         |on a.deviceudid=c.deviceudid
         |
         |left join
         |(select * from
         |  (select deviceudid,device_type,origin_type,origin_channel,origin_actpwd,dt as last_dt,
         |    row_number() over(partition by deviceudid order by dt desc) as rk
         |  from
         |    (select distinct deviceudid,device_type,origin_type,origin_channel,origin_actpwd,dt
         |    from lofter.dwd_device_growth_attribution_di where dt = '$day'
         |    union all
         |    select deviceudid,device_type,origin_type,origin_channel,origin_actpwd,last_growth_date as dt
         |    from dwd_device_all_yesterday
         |    where origin_type is not null
         |    ) a1
         |  ) a2
         |where rk=1
         |) d
         |on a.deviceudid=d.deviceudid
         |distribute by cast(rand(500) * 10 as bigint)
         |""".stripMargin

    spark.sql(sql_result)
      .write
      .mode(SaveMode.Overwrite)
      .parquet(s"$INPUT_PATH_PREFIX/dt=$day")

     spark.sql(s"alter table lofter.dwd_device_all_dd add if not exists partition(dt='$day')")

    spark.close()

  }

}
