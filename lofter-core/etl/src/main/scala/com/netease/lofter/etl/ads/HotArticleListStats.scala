package com.netease.lofter.etl.ads

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.lit

object HotArticleListStats {
  def main(args: Array[String]): Unit = {

    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Hot Article List Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))
    val oneWeekAgo = DateTime.parse(date).minusDays(6).toString("yyyy-MM-dd")

    val sql_hot_article_expand =
      s"""
         |select a.postid,a.blogid,a.publishtime,a.publishdate,url,a.tag,b.ip
         |from
         |(select a.postid,a.blogid,a.publishtime,a.publishdate,url,a.tag
         |from
         |    (select id as postid ,blogid, publishtime, publishdate,
         |            lower(concat('https://',blogname,'.lofter.com/post/',conv(blogid, 10, 16),'_',conv(id, 10, 16))) as url,
         |            tag from lofter.dim_post lateral view explode(tags) myTable as tag
         |    where ispublished=true and valid=0 and allowview=0 and iscitedpost=false and
         |          publishdate between '$oneWeekAgo' and '$date' and ((contenttype='文字' and title is not null ) or contenttype!='文字')
         |    ) a
         |    join
         |    (select  postid,blogid
         |    from lofter_db_dump.ods_db_recommend_review_post_nd
         |    where recomstatus=1
         |    ) b
         |    on a.postid=b.postid
         |    left join
         |    (select blogid from   lofter_db_dump.ods_db_blog_settings_nd
         |    where  securityrank  in (9999,9998)
         |    group by blogid) c
         |    on a.blogid=c.blogid
         |
         |    join
         |    (select tagname from lofter_db_dump.ods_db_recommend_tag_new_nd
         |    where blacktag=0 and forbidtag=0
         |    group by tagname
         |    )d
         |    on a.tag=d.tagname
         |
         |    left join
         |    (select userid from  lofter_db_dump.ods_db_media_account_import_nd where platformtype!=0 group by userid) e
         |    on a.blogid=e.userid
         |where e.userid is null and c.blogid is null
         |group by  a.postid,a.blogid,a.publishtime,a.publishdate,a.url,a.tag
         |) a
         |
         |left join
         |lofter.dwd_tag_ip_mapping_nd b
         |on a.tag=b.tag
         |group by a.postid,a.blogid,a.publishtime,a.publishdate,a.url,a.tag,b.ip
         |""".stripMargin

    val sql_ip_hd =
      s"""
         |select a.ip,sum(b.hdPv) as hdPv
         |from
         |    (select distinct postid,blogid,publishdate,ip
         |    from hotArticleExpand
         |    where ip is not null) a
         |join
         |    (select postid,sum(hdpv) as hdPv
         |    from
         |    lofter.dws_post_base_stats_di where dt between '$oneWeekAgo' and '$date'
         |    group by postid) b
         |on a.postid=b.postid
         |group by a.ip
         |""".stripMargin

    spark.sql(sql_hot_article_expand).cache().createOrReplaceTempView("hotArticleExpand")
    spark.sql(sql_ip_hd).cache().createOrReplaceTempView("ipHd")

    val sql_hot_ip =
      s"""
         |select a.ip,a.hdPv
         |from ipHd a
         |left join
         |(select percentile_approx( hdPv , 0.95) as percentValue,std(hdPv) as sigma
         |from ipHd) b
         |on 1==1
         |where a.hdPv > (b.percentValue+ 3*b.sigma)
         |""".stripMargin

    val sql_article_hd =
      s"""
         |select a.*,b.hdPv,LN(b.hdPv) as score
         |from
         |(select distinct postid,blogid,publishtime,publishdate,url
         |from hotArticleExpand) a
         |join
         |(select postid,sum(hdpv) as hdPv
         |from
         |lofter.dws_post_base_stats_di where dt between '$oneWeekAgo' and '$date' and hdpv>0
         |group by postid) b
         |on a.postId =b.postId
         |""".stripMargin

    spark.sql(sql_article_hd).createOrReplaceTempView("articleHd")
    spark.sql(sql_hot_ip).createOrReplaceTempView("hotIpHd")

    val sql_article_hd_source =
      s"""
         |select a.*,
         |    case when count(distinct c.ip)>0 then '热门IP'
         |         when count(distinct c.ip)=0 and count(distinct b.ip)>0 then '非热门IP'
         |         else '原创' end as source
         |from articleHd a
         |join hotArticleExpand b
         |on a.postId = b.postId
         |left join
         |hotIpHd c
         |on b.ip=c.ip
         |group by a.postid,a.blogid,a.publishtime,a.publishdate,a.url,a.hdPv,a.score
         |""".stripMargin

    // to do : write result to hive table

    spark.sql(sql_article_hd_source).createOrReplaceTempView("articleHdSource")

    val sql_post_rank =
      s"""
         |select a.postid,a.blogid,a.publishtime,a.publishdate,a.url,a.hdPv,a.score,source,
         |      row_number() over(partition by source order by score desc,publishtime desc) as rkSource,
         |      row_number() over(order by score desc,publishtime desc) as rkScore
         |from articleHdSource a
         |""".stripMargin

    spark.sql(sql_post_rank).cache().createOrReplaceTempView("postRank")

    val sql_level1 =
      s"""
         |select *,case when b.rk<=3 then 1 else 0 end as flag from
         |(select *,row_number() over(order by score desc,publishtime desc) as rk
         |from
         |    (select * from postRank
         |    where (source='热门IP' and rkSource=1) or  (source='非热门IP' and rkSource=1) or (source='原创' and rkSource between 1 and 3)
         |    ) a
         |) b
         |""".stripMargin

    spark.sql(sql_level1).createOrReplaceTempView("level1Article")

    val sql_level2 =
      s"""
         |select *,case when b.rk<=7 then 1 else 0 end as flag
         |from
         |(select *,row_number() over(order by score desc,publishtime desc) as rk
         |from
         |    (select aa.*
         |    from
         |        (select a.postid,a.blogid,a.publishtime,a.publishdate,a.url,a.hdPv,a.score,a.source,
         |                row_number() over(partition by source order by score desc,publishtime desc) as rkSource
         |        from
         |        postRank a
         |        left join (select postId from level1Article where flag=1) b
         |        on a.postId=b.postId
         |        where b.postId is null
         |        ) aa
         |    where (source='热门IP' and rkSource between 1 and 2) or  (source='非热门IP' and rkSource between 1 and 2) or (source='原创' and rkSource between 1 and 7)
         |    ) a
         |) b
         |""".stripMargin

    spark.sql(sql_level2).createOrReplaceTempView("level2Article")

    val sql_level3 =
      s"""
         |select *,case when b.rk<=10 then 1 else 0 end as flag
         |from
         |(select *,row_number() over(order by score desc,publishtime desc) as rk
         |from
         |    (select aa.*
         |    from
         |        (select a.postid,a.blogid,a.publishtime,a.publishdate,a.url,a.hdPv,a.score,a.source,
         |                row_number() over(partition by source order by score desc,publishtime desc) as rkSource
         |        from
         |        postRank a
         |        left join (
         |          select postId from level1Article where flag=1
         |          union
         |          select postId from level2Article where flag=1
         |        ) b
         |        on a.postId=b.postId
         |        where b.postId is null
         |        ) aa
         |    where (source='热门IP' and rkSource between 1 and 3) or  (source='非热门IP' and rkSource between 1 and 3) or (source='原创' and rkSource between 1 and 10)
         |    ) a
         |) b
         |""".stripMargin

    spark.sql(sql_level3).createOrReplaceTempView("level3Article")

    val sql_level4 =
      s"""
         |select *,case when b.rk<=80 then 1 else 0 end as flag
         |from
         |(select *,row_number() over(order by score desc,publishtime desc) as rk
         |from
         |    (select aa.*
         |    from
         |        (select a.postid,a.blogid,a.publishtime,a.publishdate,a.url,a.hdPv,a.score,a.source,
         |                row_number() over(partition by source order by score desc,publishtime desc) as rkSource
         |        from
         |        postRank a
         |        left join (
         |          select postId from level1Article where flag=1
         |          union
         |          select postId from level2Article where flag=1
         |          union
         |          select postId from level3Article where flag=1
         |        ) b
         |        on a.postId=b.postId
         |        where b.postId is null
         |        ) aa
         |    where (source='热门IP' and rkSource between 1 and 20) or  (source='非热门IP' and rkSource between 1 and 20) or (source='原创' and rkSource between 1 and 80)
         |    ) a
         |) b
         |""".stripMargin

    spark.sql(sql_level4).createOrReplaceTempView("level4Article")

    val sql_level5 =
      s"""
         |select *,case when b.rk<=100 then 1 else 0 end as flag
         |from
         |(select *,row_number() over(order by score desc,publishtime desc) as rk
         |from
         |    (select aa.*
         |    from
         |        (select a.postid,a.blogid,a.publishtime,a.publishdate,a.url,a.hdPv,a.score,a.source,
         |                row_number() over(partition by source order by score desc,publishtime desc) as rkSource
         |        from
         |        postRank a
         |        left join (
         |          select postId from level1Article where flag=1
         |          union
         |          select postId from level2Article where flag=1
         |          union
         |          select postId from level3Article where flag=1
         |          union
         |          select postId from level4Article where flag=1
         |        ) b
         |        on a.postId=b.postId
         |        where b.postId is null
         |        ) aa
         |    where (source='热门IP' and rkSource between 1 and 30) or  (source='非热门IP' and rkSource between 1 and 30) or (source='原创' and rkSource between 1 and 100)
         |    ) a
         |) b
         |""".stripMargin

    spark.sql(sql_level5).createOrReplaceTempView("level5Article")

    val sql_level_result_hive =
      s"""
         |select postId,blogId,publishDate,url,source,hdPv,score,rk,'level1' as level,flag from level1Article where flag=1
         |union all
         |select postId,blogId,publishDate,url,source,hdPv,score,rk,'level2' as level,flag from level2Article where flag=1
         |union all
         |select postId,blogId,publishDate,url,source,hdPv,score,rk,'level3' as level,flag from level3Article where flag=1
         |union all
         |select postId,blogId,publishDate,url,source,hdPv,score,rk,'level4' as level,flag from level4Article where flag=1
         |union all
         |select postId,blogId,publishDate,url,source,hdPv,score,rk,'level5' as level,flag from level5Article
         |""".stripMargin

    spark.sql(sql_level_result_hive)
      .withColumn("dt", lit(date))
      .repartition(5)
      .write
      .mode("overwrite")
      .insertInto("lofter.dws_hot_article_stat_di")

    spark.stop()
  }

}
