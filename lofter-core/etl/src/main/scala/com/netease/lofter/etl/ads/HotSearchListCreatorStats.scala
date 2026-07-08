package com.netease.lofter.etl.ads

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

object HotSearchListCreatorStats {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Hot Search List Creator Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))
    val last6Day = DateTime.parse(date).minusDays(6).toString("yyyy-MM-dd")

    val sql_creator =
      s"""
         |select * from
         |(select a.blogid,a.hdfans,a.realbrowsepv,a.hdpv,a.score,
         |      row_number() over (order by realbrowsepv desc) as rk1,row_number() over (order by score desc) as rk
         |  from
         |      (select a.blogid,a.hdfans,b.realbrowsepv,b.hdpv,(ln(hdpv)*0.5+ln(hdfans)*0.3+ln(realbrowsepv)*0.2) as score
         |      from
         |          (select userId as blogid, hd_fans_1y as hdfans
         |          from lofter.dws_par_creator_level_scoring_dd
         |          where dt='$date'
         |          ) a
         |
         |          join
         |          (select userid as blogid,sum(realplaypv+realbrowsepv) as realbrowsepv,sum(hdpv) as hdpv
         |          from lofter.dws_post_base_stats_di
         |          where substr(publishdate, 1, 10) between  '$last6Day' and '$date' and dt between '$last6Day' and '$date'
         |          group by userid) b
         |          on a.blogid=b.blogid
         |     ) a
         |) a
         |where rk <=100
         |""".stripMargin

    spark.sql(sql_creator).createOrReplaceTempView("hotCreator")

    val sql_result =
      s"""
         |insert overwrite table lofter_dm.ads_hot_search_list_di partition(dt = '$date', listName = 4)
         |select null as targetWord,
         |       case when b.rk1 is not null then 1 else 0 end as icon,
         |       null as source,
         |       a.lastScore as score,
         |       null as postId, a.blogId,
         |       a.score as indexValue,
         |       a.rk as listRank,
         |       null as interaction_count,
         |       null as searching_circle
         |from
         |(select b.*,(80+rate*(b.score-a.score_min)) as lastScore
         |from
         |(select (20/(a.score_max-a.score_min)) as rate,a.score_min,a.score_max
         |from
         |(select min(score) as score_min,max(score) as score_max
         |from  hotCreator ) a
         |) a
         |cross join
         |(select blogid,rk, rk1,score from hotCreator) b) a
         |
         |left join
         |(select blogid,rk, rk1 from hotCreator where rk<=10 order by rk1 asc limit 3 ) b
         |on a.blogid=b.blogid
         |""".stripMargin

    spark.sql(sql_result)
    spark.close()
  }

}
