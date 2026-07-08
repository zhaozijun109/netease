package com.netease.lofter.etl.ads

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

object HotSearchListContentStats {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Hot Search List Content Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))
    val last6Day = DateTime.parse(date).minusDays(6).toString("yyyy-MM-dd")

    val sql_cmb_tag_filter =
      s"""
         |select name, ipIds, derivedFlag, status
         |from lofter_db_dump.ods_db_cmb_tag_nd
         |where name not in (select tag from lofter.zq_lofter_searchbangdan_black_post_tag)
         |""".stripMargin

    spark.sql(sql_cmb_tag_filter).createOrReplaceTempView("cmbTag")

    val sql_tag_source =
      s"""
         |select a.name, 'IP'  as source
         |from (
         |    select name, ipId, derivedFlag
         |    from cmbTag lateral view explode(split(ipIds, ';')) t2  as ipId
         |    where status = 0
         |) a
         |join (
         |    select id, name as ipName, derivedFlag from lofter_db_dump.ods_db_cmb_ip_nd
         |) b
         |on a.ipId = b.id
         |where b.derivedFlag=1
         |group by a.name, 'IP'
         |
         |union all
         |
         |select name, '衍生'  as source
         |from cmbTag
         |where ipIds is null and status = 0 and derivedFlag = 1
         |group by name, '衍生'
         |
         |union all
         |
         |select a.name, '原创' as source
         |from cmbTag a
         |left join (
         |    select a.name
         |    from (
         |        select name, ipId, derivedFlag
         |        from cmbTag lateral view explode(split(ipIds, ';')) t2  as ipId
         |        where status = 0
         |    ) a
         |    join (
         |        select id, name as ipName, derivedFlag from lofter_db_dump.ods_db_cmb_ip_nd
         |    ) b
         |    on a.ipId = b.id
         |    where b.derivedFlag = 1
         |    group by a.name
         |
         |    union all
         |
         |    select name from cmbTag
         |    where ipIds is null and status = 0 and derivedFlag = 1
         |    group by name
         |) b
         |on a.name = b.name
         |where b.name is null
         |group by a.name, '原创'
         |""".stripMargin

    val sql_post_filter =
      s"""
         |select a.dt, a.postId, a.blogId, a.exposureUv, a.playUv, a.realPlayUv, a.realBrowseUv, a.tag, a.hdPv, a.hdUv
         |from (
         |    select dt, postId, userId as blogId, exposureUv, playUv, realPlayUv, realBrowseUv, substr(publishDate, 1, 10) as publishDate, tag, hdPv, hdUv
         |    from lofter.dws_post_base_stats_di lateral view explode(tags) t2 AS tag
         |    where substr(publishDate, 1, 10) between '$last6Day' and '$date' and (exposureUv > 0 or playUv > 0) and
         |    dt between  '$last6Day' and '$date' and tag <> '短视频'
         |) a
         |left join (
         |    select a.postId
         |    from (
         |        select postId, tag
         |        from lofter.dws_post_base_stats_di lateral view explode(tags) t2 AS tag
         |        where substr(publishDate, 1, 10) between  '$last6Day' and '$date' and (exposureUv > 0 or playUv > 0) and
         |        dt between  '$last6Day' and '$date'
         |        group by postId, tag
         |    ) a
         |    join (
         |        select tag from lofter.zq_lofter_searchbangdan_black_post_tag
         |    ) b
         |    on a.tag = b.tag
         |    group by a.postId
         |) b
         |on a.postId = b.postId
         |where b.postId is null
         |""".stripMargin

    spark.sql(sql_tag_source).createOrReplaceTempView("tagSource")
    spark.sql(sql_post_filter).createOrReplaceTempView("postChoice")

    val sql_content =
      s"""
         |select source,
         |       postId,
         |       blogId,
         |       sum(exposureUv + playUv) as exposureUv,
         |       sum(realPlayUv + realBrowseUv) as realBrowseUv,
         |       sum(hdPv) as hdPv,
         |       sum(hdUv) as hdUv
         |from (
         |    select a.postId,
         |           a.blogId,
         |           exposureUv,
         |           playUv,
         |           realPlayUv,
         |           realBrowseUv,
         |           b.source,
         |           hdPv,
         |           hdUv
         |    from postChoice a
         |    left join tagSource b
         |    on a.tag = b.name
         |    where b.source is not null
         |) t
         |group by source, postId, blogId
         |""".stripMargin

    spark.sql(sql_content).createOrReplaceTempView("hotContent")

    val sql_content_source =
      s"""
         |select postId,
         |       blogId,
         |       blogName,
         |       contentType,
         |       tags,
         |       source,
         |       hdPv,
         |       realBrowseUv,
         |       publishDate,
         |       score,
         |       rn4
         |from (
         |    select postId,
         |           blogId,
         |           blogName,
         |           contentType,
         |           tags,
         |           source,
         |           hdPv,
         |           realBrowseUv,
         |           publishDate,
         |           score,
         |           row_number() over(partition by contentType order by score desc, hdPv desc) as rn4
         |    from (
         |        select postId,
         |               blogId,
         |               blogName,
         |               contentType,
         |               tags,
         |               source,
         |               hdPv,
         |               realBrowseUv,
         |               publishDate,
         |               score,
         |               row_number() over (partition by postId order by rn2 asc) as rn3
         |        from (
         |            select postId,
         |                   blogId,
         |                   blogName,
         |                   contentType,
         |                   tags,
         |                   source,
         |                   hdPv,
         |                   realBrowseUv,
         |                   publishDate,
         |                   score,
         |                   row_number() over (partition by source order by score desc, hdPv desc) as rn2
         |            from (
         |                select a.postId,
         |                       a.blogId,
         |                       blogName,
         |                       contentType,
         |                       tags,
         |                       source,
         |                       hdPv,
         |                       realBrowseUv,
         |                       publishDate,
         |                       hdPv * ln(1+(7/datediff('$date', publishDate))) as score,
         |                       row_number() over (partition by a.blogId order by hdPv * ln(1+(7/datediff('$date', publishDate))) desc) as rn1
         |                from hotContent a
         |                join (
         |                    select id,
         |                           publishDate,
         |                           blogId,
         |                           blogName,
         |                           tags,
         |                           contentType
         |                    from lofter.dim_post_article
         |                    where contentType in ('文字','图片','视频') and isPublished = true and valid = 0 and allowView = 0
         |                          and isCitedPost = false and is_book_store = 0 and recomStatus=1
         |                ) b
         |                on a.postId = b.id
         |                where hdPv >= 50
         |            ) t
         |            where rn1 <= 2
         |        ) tt
         |    ) ttt
         |    where rn3 = 1
         |) tttt
         |where rn4 <= 100
         |""".stripMargin

    spark.sql(sql_content_source).createOrReplaceTempView("hotContentSource")

    val sql_content_choice =
      s"""
         |with text_post_source as (
         |       select a.postId,
         |              blogId,
         |              source,
         |              realBrowseUv,
         |              score,
         |              rn4,
         |              if(b.postId is null, 0, 1) as is_pay
         |       from (
         |              select postId,
         |                     blogId,
         |                     source,
         |                     realBrowseUv,
         |                     score,
         |                     rn4
         |              from hotContentSource
         |              where contentType = '文字'
         |       ) a
         |       left join (
         |              select postId
         |              from lofter.dim_gift_post_return_dd
         |              where dt = '$date'
         |              group by postId
         |       ) b
         |       on a.postId = b.postId
         |)
         |select postId,
         |       blogId,
         |       source,
         |       realBrowseUv,
         |       score,
         |       row_number() over (order by score desc) as rn1,
         |       row_number() over (order by realBrowseUv desc) as rn2
         |from (
         |    select postId,
         |           blogId,
         |           source,
         |           realBrowseUv,
         |           score
         |    from (
         |       select postId,
         |              blogId,
         |              source,
         |              realBrowseUv,
         |              case when contentType = '图片' then score
         |                   when contentType = '视频' then score * 10
         |              end as score
         |       from hotContentSource
         |       where (contentType = '图片' and rn4 <= 100) or (contentType = '视频' and rn4 <= 40)
         |       union all
         |       select postId,
         |              blogId,
         |              source,
         |              realBrowseUv,
         |              score
         |       from (
         |          select postId,
         |                 blogId,
         |                 source,
         |                 realBrowseUv,
         |                 score * 5 as score,
         |                 is_pay,
         |                 row_number() over(partition by is_pay order by rn4 desc) as rn
         |          from text_post_source
         |       ) a
         |       where (is_pay = 0 and rn <= 80) or (is_pay = 1 and rn <= 20)
         |    ) t
         |    group by postId,
         |             blogId,
         |             source,
         |             realBrowseUv,
         |             score
         |) tt
         |""".stripMargin

    spark.sql(sql_content_choice).createOrReplaceTempView("hotContentChoice")

    val sql_result =
      s"""
         |insert overwrite table lofter_dm.ads_hot_search_list_di partition(dt = '$date', listName = 3)
         |select null as targetWord,
         |       case when tmp2.rn2 is not null then 1 else 0 end as icon,
         |       tmp1.source,
         |       tmp1.lastScore as score,
         |       tmp1.postId,
         |       tmp1.blogId,
         |       tmp1.score as indexValue,
         |       tmp1.rn1 as listRank,
         |       null as interaction_count,
         |       null as searching_circle
         |from (
         |    select postId,
         |           blogId,
         |           score,
         |           source,
         |           rn1,
         |           rn2,
         |           (100 - rate * (score_max - score)) as lastScore
         |    from (
         |        select (20 / (score_max - score_min)) as rate,
         |               score_min,
         |               score_max
         |        from (
         |            select min(score) as score_min,
         |                   max(score) as score_max
         |            from hotContentChoice
         |        ) t
         |    ) t1
         |    cross join (
         |        select postId,
         |               blogId,
         |               score,
         |               source,
         |               rn1,
         |               rn2
         |        from hotContentChoice
         |    ) t2
         |) tmp1
         |left join (
         |    select postId,
         |           blogId,
         |           source,
         |           rn1,
         |           rn2
         |    from hotContentChoice
         |    where rn1 <= 10
         |    order by rn2 asc
         |    limit 3
         |) tmp2
         |on tmp1.postId = tmp2.postId
         |""".stripMargin

    spark.sql(sql_result)
    spark.close()
  }
}
