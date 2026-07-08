package com.netease.lofter.etl.youdata

import java.time.{DayOfWeek, LocalDate}

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

object NewUserRecPostIdStats {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter New User Rec PostId Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))
    val dayOfWeek = LocalDate.parse(date).getDayOfWeek
    if (dayOfWeek.equals(DayOfWeek.MONDAY)) {
      spark.sql("create temporary function bitmap_to_array as 'com.netease.wm.udf.bitmap.BitmapToArrayUDF'")
      weeklyNewUserPostIdStats(spark,date)
    } else {
      println(s"there is no need to cal on date $date ")
    }

    def weeklyNewUserPostIdStats(spark: SparkSession, date: String): Unit = {
      val startDay = DateTime.parse(date).minusDays(6).toString("yyyy-MM-dd")
      val endDay = date

      val sql_result =
        s"""
          |insert overwrite table lofter_dm.ads_new_user_rec_post_wd partition(dt='$date')
          |select a.ip,a.postid,contenttype,posturl,hdpv,device_type
          |from
          |(
          |select device_type,ip,
          |       case when device_type='new' and t2.postid is null and t3.postid is null then t1.postid 
          |            when device_type='return_30' and t2.postid is null then t1.postid end as postid
          |from
          |(
          |    select device_type,ip,postid
          |    from
          |        (
          |        select device_type,post_ip ip,bitmap_to_array(browse_post_bitmap) postids
          |        from lofter.dws_growth_device_ip_di
          |        where dt between '$startDay' and '$endDay'
          |        and is_paid_subscribe=0
          |        group by device_type,post_ip,postids
          |        )
          |    lateral view explode(postids) t1 as postid
          |    group by device_type,ip,postid 
          |    having count(1)>2
          |) t1
          |left join
          |(
          |    select cast(conv(split(item_id,'_')[1], 16, 10) as bigint)as postid
          |    from rec.rec_ra_item_pool_v1
          |    where day = '$endDay'
          |    group by item_id
          |) t2
          |on t1.postid=t2.postid
          |left join
          |(
          |    select cast(conv(split(item_id,'_')[1], 16, 10) as bigint)as postid
          |    from rec.rec_new_user_addition_item_pool_v1
          |    where day = '$endDay'
          |    group by item_id
          |) t3
          |on t1.postid=t3.postid
          |) a
          |join
          |(
          |    select postid,hdpv
          |    from lofter.dws_post_base_stats_dd
          |    where dt='$endDay'
          |    and hdpv>100
          |) c
          |on a.postid=c.postid
          |join
          |(
          |    select id postid,concat('https://',blogname,'.lofter.com/post/',conv(blogid, 10, 16),'_',conv(id, 10, 16)) as posturl,title,tags,contenttype
          |    from lofter.dim_post
          |    where ispublished=true and isforbidden=false and iscitedpost=false
          |    and allowview=0
          |    and contenttype in ("图片","文字","视频")
          |    and isactivityautopost=0 and isimported=0 and ismoved=0
          |    and valid in (0,12) and recomStatus=1
          |) d
          |on a.postid=d.postid
          |group by  a.ip,a.postid,posturl,hdpv,contenttype,device_type
          |distribute by 1
          |""".stripMargin

      spark.sql(sql_result)
    }

    spark.close()
  }

}
