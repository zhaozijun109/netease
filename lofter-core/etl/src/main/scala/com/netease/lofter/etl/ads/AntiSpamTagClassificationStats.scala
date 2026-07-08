package com.netease.lofter.etl.ads

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}

object AntiSpamTagClassificationStats {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter AntiSpam Tag Classification Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))
    val oneMonthAgo = DateTime.parse(date).minusDays(29).toString("yyyy-MM-dd")

    val sql_his =
      s"""
         |select tag,count(distinct id) as postNum
         |from lofter.dim_post
         |lateral view explode(tags) myTable as tag
         |where ispublished=true and valid=0 and allowview=0 and iscitedpost=false and
         |      publishdate>= '2020-01-01' and tags[0] is not null
         |group by tag
         |""".stripMargin

    spark.sql(sql_his).createOrReplaceTempView("postHis")

    val sql_result =
      s"""
         |select  a.tag,a.type,a.source
         |from 
         |(select a.tag,a.source,a.type,rank()over(partition by a.tag order by a.sourceflg asc) as rk
         |    from
         |    (select case when source='rengong' then 1 when source='moxing' then 2 else 3 end as sourceflg,a.tag,a.type,a.source
         |    from
         |    (
         |    select distinct a.tag,
         |           case when chaosongnum>=10 and b.pinbinum is null then '白'
         |                when chaosongnum>=5 and b.pinbinum =chaosongnum  then '黑'
         |                else '其他' end as type,
         |           'moxing' as source
         |    from
         |        (select content as tag,count(1) as chaosongnum
         |         from lofter.ods_log_anti_spam_copy_di
         |         where dt between '$oneMonthAgo' and '$date'
         |              and from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') between '$oneMonthAgo' and '$date'
         |              and bizinfo='genTagLog' and category='tag'
         |         group by content
         |         ) a
         |        left join
         |        (select get_json_object(content,"$$.tagName") as tag,count(1) as pinbinum
         |        from lofter_db_dump.ods_db_risk_antispam_callback_record_nd
         |        where from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd')  between '$oneMonthAgo' and '$date'
         |              and  get_json_object(content,"$$.method")='postTagAuditResult' and status=2
         |        group by 1
         |        ) b
         |        on a.tag=b.tag
         |
         |    union
         |    select tag,'白' as type, 'histpost' as source
         |    from postHis
         |    where postNum>=100
         |    group by tag ,'白','histpost'
         |
         |    union
         |    select a.tag,a.type,a.source from
         |    (select tag,
         |         rank()over(partition by tag order by createtime desc ) as rk,
         |         case when optype=1 then '白'
         |              when optype=2 then '黑'
         |              else '其他' end as type,
         |        'rengong' as source
         |    from lofter.ods_log_artificial_import_tag_di
         |    where dt<='$date'
         |    )  a
         |    where rk=1
         |    group by a.tag,a.type,a.source
         |    ) a
         |    ) a
         |) a
         |where a.rk=1
         |group by a.tag,a.type,a.source
         |""".stripMargin

    spark.sql(sql_result).repartition(1)
      .withColumn("dt", lit(date))
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_anti_spam_tag_classification_di")

    spark.close()

  }

}
