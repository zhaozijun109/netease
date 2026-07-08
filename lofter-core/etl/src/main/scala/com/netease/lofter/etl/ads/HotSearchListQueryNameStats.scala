package com.netease.lofter.etl.ads

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

object HotSearchListQueryNameStats {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Hot Search List QueryName Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))
    val oneMonthAgo = DateTime.parse(date).minusDays(29).toString("yyyy-MM-dd")
    val last2Day = DateTime.parse(date).minusDays(2).toString("yyyy-MM-dd")

    val sql_search_all =
      s"""
         |select dt,deviceudid, eventId, occur_time as occurTime, userid, itemType, search_query as queryName, tab
         |from lofter.dwd_search_action_di
         |where dt between '$last2Day' and '$date' and
         |      eventId in ('b5-10','b5-12','b5-13','b5-20','b5-24')
         |""".stripMargin

    spark.sql(sql_search_all).cache().createOrReplaceTempView("mdaSearch")

    val sql_search_no_result =
      s"""
         |select a.queryname as target, count(distinct a.deviceUdid) as uv1, count(distinct b.deviceUdid) as uv2
         |from (
         |    select dt,deviceudid, queryname
         |    from mdaSearch
         |    where eventId in ('b5-13','b5-12')
         |    group by 1,2,3
         |) a
         |left join (
         |    select dt, deviceudid, queryname
         |    from mdaSearch
         |    where eventId in ('b5-20') and tab = 'all'
         |    group by 1,2,3
         |) b on a.dt=b.dt and a.deviceudid=b.deviceudid and a.queryname=b.queryname
         |group by a.queryname
         |having uv2 / uv1 >= 0.4
         |""".stripMargin

    val sql_search =
      s"""
         |select *
         |from (
         |    select distinct deviceudid,dt, queryname
         |    from mdaSearch
         |) a
         |where length(queryname) > 0
         |""".stripMargin

    val ip_tag_search =
      s"""
         |select a.ip, a.tag, b.userId
         |from (
         |    select *
         |    from lofter.dwd_tag_ip_mapping_nd
         |) a
         |join (
         |    select userid, search_query as query
         |    from lofter.dwd_search_action_di
         |    where dt between '$last2Day' and '$date' and
         |          eventId in ('b5-10','b5-12','b5-13','b5-20','b5-24')
         |    group by 1, 2
         |) b on a.tag = b.query
         |""".stripMargin

    val ip_users =
      s"""
         |select a.userId, ip
         |from (
         |    select userId, postId
         |    from lofter.dwd_post_response_di
         |    where dt >= '$oneMonthAgo' and dt <= '$date'
         |  union all
         |    select userId, postId
         |    from lofter.dwd_post_publish_di
         |    where dt >= '$oneMonthAgo' and dt <= '$date'
         |) a
         |join lofter.dim_post b on a.postId = b.id
         |lateral view explode(b.ips) as ip
         |group by 1, 2
         |""".stripMargin

    spark.sql(sql_search_no_result).createOrReplaceTempView("searchNoResult")
    spark.sql(sql_search).createOrReplaceTempView("searchQueryName")
    spark.sql(ip_tag_search).createOrReplaceTempView("ip_tag_search")
    spark.sql(ip_users).createOrReplaceTempView("ip_users")

    val sql_search_result_all =
      s"""
         |select a.queryName as targetWord, cast(a.uv as double) as indexValue, a.rk as listRank,
         |    case when b.actpwd is null then '非搜索口令' else '搜索口令' end source,
         |    if((row_number() over (order by c.ratio desc)) <= 5, c.ip, null) as searchingCircle
         |from (
         |    select a.queryName,a.uv,a.rk,a.listName
         |    from (
         |        select a.queryName,a.uv,a.listName,row_number() over(partition by a.listName order by a.uv desc) as rk
         |        from (
         |            select a.queryName ,1 as listName, count(distinct a.deviceudid) as uv
         |            from searchQueryName a
         |            group by  1,2
         |        ) a
         |        left join (
         |            select distinct target as queryName
         |            from searchNoResult
         |        ) b on a.queryName = b.queryName
         |        where b.queryName is null and a.queryName is not null
         |    ) a
         |    where a.rk<=100
         |) a
         |left join (
         |    select distinct actpwd
         |    from lofter.dwd_growth_actpwd_access_di
         |    where dt between '$oneMonthAgo' and '$date'
         |) b on a.queryName = b.actpwd
         |left join (
         |    select *
         |    from (
         |        select tag, ip, ratio,
         |               row_number() over (partition by tag order by ratio desc) as tag_ip_rank
         |        from (
         |            select a.tag, a.ip,
         |                   count(distinct b.userId) / count(distinct a.userId) as ratio,
         |                   count(distinct a.userId) as search_uv,
         |                   count(distinct b.userId) as ip_user_search_uv
         |            from ip_tag_search a
         |            left join ip_users b on a.ip = b.ip and a.userId = b.userId
         |            group by 1, 2
         |        ) t
         |        where search_uv >= 10 and ratio > 0.12
         |    ) tt
         |    where tag_ip_rank = 1
         |) c on a.queryName = c.tag
         |""".stripMargin

    val sql_search_result_new =
      s"""
         |select a.queryname as targetWord,cast(a.uv as double) as indexValue,a.rk as listRank,
         |    case when b.actpwd is null then '非搜索口令' else '搜索口令' end source,
         |    null as searchingCircle
         |from
         |(select a.queryname,a.uv,a.rk,a.listName
         |from
         |  (select a.queryname,a.uv,a.listName,row_number() over(partition by a.listName order by a.uv desc) as rk
         |  from
         |      (select a.queryname ,2 as listName, count(distinct a.deviceudid) as uv
         |      from
         |         ( select a1.*
         |           from searchQueryName a1
         |           join
         |           (select deviceudid,dt from lofter.device_new where dt  between '$last2Day' and  '$date' group by deviceudid,dt) b1
         |           on a1.deviceudid=b1.deviceudid and a1.dt=b1.dt
         |         ) a
         |      group by  1,2) a
         |      left join
         |      (select distinct target as queryname
         |      from searchNoResult) b
         |      on a.queryname=b.queryname
         |  where b.queryname is null and a.queryname is not null
         |  ) a
         |  where a.rk<=100
         |) a
         |left  join
         |(select distinct actpwd
         | from lofter.dwd_growth_actpwd_access_di
         | where dt between '$oneMonthAgo' and '$date') b
         | on a.queryname=b.actpwd
         |""".stripMargin

    spark.sql(sql_search_result_all).createOrReplaceTempView("t1")
    spark.sql(sql_search_result_new).createOrReplaceTempView("t2")

    val sql_challenge_top5 =
      s"""
         |select question, userid, answercount, viewcount, rn
         |from (
         |    select question, userid, answercount, viewcount,
         |            row_number() over (
         |                order by (viewcount + 200 * answercount)
         |                        / pow(datediff('$date', from_unixtime(cast(createtime/1000 as bigint), 'yyyy-MM-dd')) + 2, 1.5) desc
         |            ) as rn
         |    from lofter_db_dump.ods_db_ask_question_nd
         |    where from_unixtime(cast(createtime/1000 as bigint), 'yyyy-MM-dd') >= '2026-01-01'
         |        and cosplay     = 6
         |        and status      = 1
         |        and answercount >= 40
         |) t where rn <= 5
         |""".stripMargin

    spark.sql(sql_challenge_top5).createOrReplaceTempView("challenge_top5")

    val sql_result_1 =
      s"""
         |with origin_raw as (
         |    select b.targetWord                                  as targetword,
         |           case when b.listRank <= 3 then 1 else 0 end   as icon,
         |           b.source                                      as source,
         |           (80 + rate * (b.indexValue - a.uv_min))       as score,
         |           cast(null as bigint)                          as postid,
         |           cast(null as bigint)                          as blogid,
         |           b.indexValue                                  as indexvalue,
         |           b.indexValue * 10                             as interaction_count,
         |           b.searchingCircle                             as searching_circle,
         |           b.listRank                                    as origin_rk
         |    from (
         |        select (20 / (a.uv_max - a.uv_min)) as rate, a.uv_min, a.uv_max
         |        from (
         |            select min(indexValue) as uv_min, max(indexValue) as uv_max
         |            from t1
         |        ) a
         |    ) a
         |    cross join t1 b
         |),
         |origin_list as (
         |    select o.targetword, o.icon, o.source, o.score, o.postid, o.blogid,
         |           o.indexvalue, o.interaction_count, o.searching_circle,
         |           cast(row_number() over (order by o.origin_rk) as double) as rk
         |    from origin_raw o
         |    left join challenge_top5 c on o.targetword = c.question
         |    where c.question is null
         |),
         |position_map as (
         |              select 1 as rn, 4 as target_pos, 3.5 as rk,  3  as before_rk, 4  as after_rk
         |    union all select 2,       8,               6.5,        6,               7
         |    union all select 3,       12,              9.5,        9,               10
         |    union all select 4,       16,              12.5,       12,              13
         |    union all select 5,       20,              15.5,       15,              16
         |),
         |challenge_rows as (
         |    select t.question                                   as targetword,
         |           3                                            as icon,
         |           cast(null as string)                         as source,
         |           cast(null as double)                         as score,
         |           cast(null as bigint)                         as postid,
         |           cast(null as bigint)                         as blogid,
         |           cast(null as double)                         as indexvalue,
         |           cast(round(
         |               (coalesce(b.interaction_count, 0) + coalesce(a.interaction_count, 0)) / 2.0
         |           ) as bigint)                                 as interaction_count,
         |           cast(null as string)                         as searching_circle,
         |           p.rk                                         as rk
         |    from challenge_top5 t
         |    join position_map p on t.rn = p.rn
         |    left join origin_list b on b.rk = p.before_rk
         |    left join origin_list a on a.rk = p.after_rk
         |)
         |insert overwrite table lofter_dm.ads_hot_search_list_di partition(dt = '$date', listName = 1)
         |select targetword, icon, source, score, postid, blogid, indexvalue,
         |       row_number() over (order by rk asc) as listrank,
         |       interaction_count, searching_circle
         |from (
         |    select targetword, icon, source, score, postid, blogid,
         |           indexvalue, interaction_count, searching_circle, rk
         |    from origin_list
         |    union all
         |    select targetword, icon, source, score, postid, blogid,
         |           indexvalue, interaction_count, searching_circle, rk
         |    from challenge_rows
         |) merged
         |""".stripMargin

    val sql_result_2 =
      s"""
         |with origin_raw as (
         |    select b.targetWord                                  as targetword,
         |           case when b.listRank <= 3 then 1 else 0 end   as icon,
         |           b.source                                      as source,
         |           (80 + rate * (b.indexValue - a.uv_min))       as score,
         |           cast(null as bigint)                          as postid,
         |           cast(null as bigint)                          as blogid,
         |           b.indexValue                                  as indexvalue,
         |           b.indexValue * 10                             as interaction_count,
         |           b.searchingCircle                             as searching_circle,
         |           b.listRank                                    as origin_rk
         |    from (
         |        select (20 / (a.uv_max - a.uv_min)) as rate, a.uv_min, a.uv_max
         |        from (
         |            select min(indexValue) as uv_min, max(indexValue) as uv_max
         |            from t2
         |        ) a
         |    ) a
         |    cross join t2 b
         |),
         |origin_list as (
         |    select o.targetword, o.icon, o.source, o.score, o.postid, o.blogid,
         |           o.indexvalue, o.interaction_count, o.searching_circle,
         |           cast(row_number() over (order by o.origin_rk) as double) as rk
         |    from origin_raw o
         |    left join challenge_top5 c on o.targetword = c.question
         |    where c.question is null
         |),
         |position_map as (
         |              select 1 as rn, 4 as target_pos, 3.5 as rk,  3  as before_rk, 4  as after_rk
         |    union all select 2,       8,               6.5,        6,               7
         |    union all select 3,       12,              9.5,        9,               10
         |    union all select 4,       16,              12.5,       12,              13
         |    union all select 5,       20,              15.5,       15,              16
         |),
         |challenge_rows as (
         |    select t.question                                   as targetword,
         |           3                                            as icon,
         |           cast(null as string)                         as source,
         |           cast(null as double)                         as score,
         |           cast(null as bigint)                         as postid,
         |           cast(null as bigint)                         as blogid,
         |           cast(null as double)                         as indexvalue,
         |           cast(round(
         |               (coalesce(b.interaction_count, 0) + coalesce(a.interaction_count, 0)) / 2.0
         |           ) as bigint)                                 as interaction_count,
         |           cast(null as string)                         as searching_circle,
         |           p.rk                                         as rk
         |    from challenge_top5 t
         |    join position_map p on t.rn = p.rn
         |    left join origin_list b on b.rk = p.before_rk
         |    left join origin_list a on a.rk = p.after_rk
         |)
         |insert overwrite table lofter_dm.ads_hot_search_list_di partition(dt = '$date', listName = 2)
         |select targetword, icon, source, score, postid, blogid, indexvalue,
         |       row_number() over (order by rk asc) as listrank,
         |       interaction_count, searching_circle
         |from (
         |    select targetword, icon, source, score, postid, blogid,
         |           indexvalue, interaction_count, searching_circle, rk
         |    from origin_list
         |    union all
         |    select targetword, icon, source, score, postid, blogid,
         |           indexvalue, interaction_count, searching_circle, rk
         |    from challenge_rows
         |) merged
         |""".stripMargin

    spark.sql(sql_result_1)
    spark.sql(sql_result_2)

    spark.close()
  }

}
