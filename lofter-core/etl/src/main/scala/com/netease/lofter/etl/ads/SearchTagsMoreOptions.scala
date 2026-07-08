package com.netease.lofter.etl.ads

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}

/**
 * Offline job for LOFTER-15334
 */
object SearchTagsMoreOptions {
  case class TagScore(tag: String, score: Double, dataType: Int, id: Int = 0)

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Search More Options")
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val yesterday = DateTime.yesterday.toString("yyyy-MM-dd")
    val dt = pargs.optional("date").getOrElse(yesterday)
    val startDt = DateTime.parse(dt).minusDays(6).toString("yyyy-MM-dd")
    val endDt = DateTime.parse(dt).plusDays(1).toString("yyyy-MM-dd")

    import spark.implicits._

    val generalTagScore =
      s"""
         |select nvl(a.tag, b.tag) as tag,
         |       0.2 * nvl(a.hot,0) + 0.8 * nvl(b.postCount, 0) as score
         |from (
         |    select tag, count(1) hot
         |    from lofter.dwd_post_hot_di
         |         lateral view explode(post_tags) pt as tag
         |    where dt >= '$startDt' and dt < '$endDt'
         |    group by tag
         |) a full join (
         |    select tag, count(distinct postId) postCount
         |    from lofter.dwd_post_publish_di
         |         lateral view explode(post_tags) pt as tag
         |    where dt >= '$startDt' and dt < '$endDt'
         |    group by tag
         |) b on a.tag = b.tag
         |order by score desc
         |limit 5
         |""".stripMargin

    val newUserTagScore =
      s"""
         |select nvl(a.tag, b.tag) as tag,
         |       0.2 * nvl(a.hot,0) + 0.8 * nvl(b.postCount, 0) as score
         |from (
         |    select tag, sum(hot) hot
         |    from (
         |       select a.post_tags, a.hot
         |       from (
         |           select userId, 1 as hot, post_tags
         |           from lofter.dwd_post_hot_di
         |           where dt >= '$startDt' and dt < '$endDt'
         |       ) a join lofter.dim_user u on a.userId = u.id
         |       where u.createTime >= unix_timestamp('$startDt', 'yyyy-MM-dd') * 1000L and
         |             u.createTime < unix_timestamp('$endDt', 'yyyy-MM-dd') * 1000L
         |    ) lateral view explode(post_tags) pt as tag
         |    group by tag
         |) a full join (
         |    select tag, count(distinct postId) postCount
         |    from (
         |       select a.post_tags, a.postId
         |       from (
         |        select userId, postId, post_tags
         |        from lofter.dwd_post_publish_di
         |        where dt >= '$startDt' and dt < '$endDt'
         |       ) a join lofter.dim_user u on a.userId = u.id
         |       where u.createTime >= unix_timestamp('$startDt', 'yyyy-MM-dd') * 1000L and
         |             u.createTime < unix_timestamp('$endDt', 'yyyy-MM-dd') * 1000L
         |    ) lateral view explode(post_tags) pt as tag
         |    group by tag
         |) b on a.tag = b.tag
         |order by score desc
         |limit 15
         |""".stripMargin

    val generalTagScores = spark.sql(generalTagScore).collect().map {
      row => TagScore(row.getString(0), row.getDecimal(1).doubleValue(), 2)
    }

    val newUserTagScores = spark.sql(newUserTagScore).collect().map {
      row => TagScore(row.getString(0), row.getDecimal(1).doubleValue(), 1)
    }

    val generalTags = generalTagScores.map(_.tag).toSet

    val result = (generalTagScores ++ newUserTagScores.filterNot(s => generalTags(s.tag))).sortBy(-_.score)
      .zipWithIndex.map { case (s, i) => s.copy(id = i) }
      .take(15)

    spark.createDataset(result)
      .select("id", "tag", "score", "dataType")
      .withColumn("dt", lit(dt))
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_search_tag_score_top_di")

    spark.stop()
  }
}
