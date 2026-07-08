package com.netease.lofter.etl.youdata

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

object RiskMessageStats {
  def main(args: Array[String]): Unit = {
    val params = Args(args)
    val spark = SparkSession.builder()
      .appName("lofter risk message stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val yesterday = DateTime.yesterday().toString("yyyy-MM-dd")
    val date = params.optional("date").getOrElse(yesterday)

    val sql_result =
      s"""
        |insert overwrite table lofter_dm.ads_risk_message_stats_di partition(dt='$date')
        |select distinct a.id,a.sendblogid,a.acceptblogid,a.content,a.target,a.sendday,a.sendtime,b.blogname as sendblogname,
        |       d.blogname as acceptblogname,c.postnum as sendpostnum,e.postnum as acceptpostnum
        |from
        |(select a.id,a.sendblogid,a.acceptblogid,a.content,b.target,a.sendday,a.sendtime
        |from
        |     (select distinct id,blogid as sendblogid,otherblogid as acceptblogid,content,
        |             from_unixtime(cast(publishtime / 1000 AS BIGINT), 'yyyy-MM-dd') as sendday,
        |             from_unixtime(cast(publishtime / 1000 AS BIGINT), 'yyyy-MM-dd HH:mm') as sendtime
        |     from  lofter_db_dump.ods_db_message_nd
        |     where from_unixtime(cast(publishtime / 1000 AS BIGINT), 'yyyy-MM-dd') = '$date'
        |           and issender=1 and blogid!=1230207
        |           and blogid not in  (select blogid as blogid1 from   lofter_db_dump.ods_db_verify_blog_nd) ---豁免名单
        |     ) a
        |cross join
        |(select target from lofter.zq_lofter_black_message_zeppelin  ---私信黑名单
        |) b
        |where  locate(b.target,a.content)>0
        |group by a.id,a.sendblogid,a.acceptblogid,a.content,b.target,a.sendday,a.sendtime) a
        |
        |left join
        |(select blogid,blogname from lofter_db_dump.ods_db_blog_info_nd ) b
        |on a.sendblogid=b.blogid
        |left join
        |( select count(distinct id) as postnum,blogid
        | from lofter.dim_post
        | where contenttype!='问答'  and ispublished=true and valid=0 and allowview=0 and iscitedpost=false
        |group by blogid) c
        |on a.sendblogid=c.blogid
        |left join
        |(select blogid,blogname from lofter_db_dump.ods_db_blog_info_nd ) d
        |on a.acceptblogid=d.blogid
        |left join
        |( select count(distinct id) as postnum,blogid
        | from lofter.dim_post
        | where contenttype!='问答'  and ispublished=true and valid=0 and allowview=0 and iscitedpost=false
        | group by blogid) e
        |on a.acceptblogid=e.blogid
        |""".stripMargin

    spark.sql(sql_result)
    spark.close()
  }
}
