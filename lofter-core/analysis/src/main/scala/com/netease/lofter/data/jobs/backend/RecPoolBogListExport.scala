package com.netease.lofter.data.jobs.backend

import com.github.nscala_time.time.Imports.DateTime
import com.netease.lofter.data.common.databases.getRecDDBConn
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

import java.sql.Connection

object RecPoolBogListExport {
  val BATCH_SIZE = 500

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val yesterday = DateTime.yesterday.toString("yyyy-MM-dd")
    val date = pargs.optional("date").getOrElse(yesterday)
    val dateNum = DateTime.parse(date).toString("yyyyMMdd").toInt
    val twoWeekAgo = DateTime.parse(date).minusDays(13).toString("yyyy-MM-dd")
    val tenDaysAgo = DateTime.parse(date).minusDays(9).toString("yyyyMMdd").toInt

    val recPoolBlogListSql =
      s"""
         |select a.blogId as itemId, 'BLOG' as itemType, a.blogId as belong_to, a.scenes,
         |       b.tags, c.domains as fields, 1 as recState, $dateNum as day
         |from (
         |    select blogId, concat('["', concat_ws('","', collect_set(scene)), '"]') as scenes
         |    from (
         |        select blogId, case module when 'tag榜单' then 'suggest_blog' when '猜你喜欢' then 'suggest_blog' when '小编推荐' then 'feed_blog' end as scene
         |        from lofter_dm.dwb_rec_pool_blog_list_di
         |        where dt='$date'
         |      union
         |        select cast(carry_item as bigint) as blogid, 'search' as scene
         |        from rec.search_known_query_basic_data where day="$date" and query_type in ("blog")
         |    ) t
         |    group by 1
         |) a
         |left join (
         |    select blogId, concat('["', concat_ws('","', collect_set(tag)), '"]') as tags
         |    from (
         |      select blogId, replace(tag, '"','') as tag, row_number() over (partition by blogId order by postCount desc) as ra
         |      from (
         |        select p.blogId, p.tag, count(1) postCount
         |        from
         |          (select blogId, tag from lofter.dim_post lateral view explode(tags) as tag
         |           where publishDate <= '$date' and publishDate >= '$twoWeekAgo'
         |           and isPublished=true
         |           and isForbidden=false
         |           and allowView=0
         |           and contentType <> '问答'
         |          ) p
         |         join (select blogId from lofter_dm.dwb_rec_pool_blog_list_di where dt = '$date') r on p.blogId = r.blogId
         |         group by p.blogId, p.tag
         |         having postCount >= 3
         |      ) t
         |    ) tt
         |    where ra <= 5
         |    group by blogId
         |) b on a.blogId = b.blogId
         |left join (
         |    select blogId, concat('["', concat_ws('","', collect_set(domainName)), '"]') as domains
         |    from (
         |      select blogId, replace(d.domainName, '"','') as domainName, row_number() over (partition by blogId order by postCount desc) as ra
         |      from (
         |        select p.blogId, p.domain, count(1) postCount
         |        from
         |          (select blogId, domain from lofter.dim_post lateral view explode(domains) as domain
         |           where publishDate <= '$date' and publishDate >= '$twoWeekAgo'
         |           and isPublished=true
         |           and isForbidden=false
         |           and allowView=0
         |           and contentType <> '问答'
         |          ) p
         |         join (select blogId from lofter_dm.dwb_rec_pool_blog_list_di where dt='$date') r on p.blogId = r.blogId
         |         group by p.blogId, p.domain
         |         having postCount >= 3
         |      ) t join lofter.dim_domain d on t.domain = d.id
         |    ) tt
         |    where ra <= 1
         |    group by blogId
         |) c on a.blogId = c.blogId
         |""".stripMargin

    import com.netease.lofter.data.common.spark.SparkSqlImplicits._
    import com.netease.wm.util.Sql._
    implicit val conn: Connection = getRecDDBConn

    sql"""delete from Pool_Blog where day < ${0} or day = ${1}""".update(param(tenDaysAgo, dateNum))

    val results = spark.sql(recPoolBlogListSql).collect()
    results.toSeq.grouped(BATCH_SIZE).foreach { rows =>
      val batch = rows.map(rowParam _)
      sql"insert into Pool_Blog(itemId, itemType, scenes, belong_to, tags, fields, recState, day) values(${"itemId"}, ${"itemType"}, ${"scenes"}, ${"belong_to"}, ${"tags"}, ${"fields"}, ${"recState"}, ${"day"})".batchUpdate(batch)
    }

    spark.close()
  }
}
