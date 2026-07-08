package com.netease.lofter.etl.dim

import com.netease.wm.util.Args
import org.apache.hadoop.fs.{FileContext, Path}
import org.apache.spark.sql.functions.{expr, lit}
import org.apache.spark.sql.{SaveMode, SparkSession}

import java.net.URI

/**
 * post category dimension
 */
object PostCategory {
  val ScorePairRegex = """(?U)([\w\-]*):([\d\.\-]*)""".r
  val catTriplePattern = """(?U)(\w+)\-(\w+)\-(\w+)""".r

  def loadDirContent(parent: String, fc: FileContext): Seq[String] = {
    val fileIterator = fc.listStatus(new Path(parent))

    Iterator.continually(fileIterator.hasNext).takeWhile(identity).map(_ => fileIterator.next())
      .filter(_.isFile)
      .flatMap{ f =>
        scala.io.Source.fromInputStream(fc.open(f.getPath)).getLines()
      }.toList
  }

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.required("date")

    val fc = FileContext.getFileContext(new URI("hdfs://gy-cluster8/"))

    val domainCategoryMapping = loadDirContent("/user/da_lofter/warehouse/category_domain_mapping", fc)
      .map(_.split(","))
      .filter(_.length >= 4)
      .map(s => s.slice(0,3).mkString("_") -> s(3) ).toMap

    if(domainCategoryMapping.isEmpty) {
      throw new RuntimeException("can't resolve table lofter.category_domain_mapping")
    }

    spark.sqlContext.udf.register("max_score_item", (scored: String) =>  {
      val scores = scored.split(";").toSeq.collect {
        case ScorePairRegex(tag, score) if !tag.startsWith("无") => (tag, score.toDouble)
      }
      if(scores.nonEmpty) {
        val result = scores.maxBy(_._2)._1
        result match {
          case catTriplePattern("CP", "无", "无") => s"CP-其他cp-其他cp"
          case catTriplePattern("CP", "其他cp", "无") => s"CP-其他cp-其他cp"
          case catTriplePattern(c1, "无", "无") => s"$c1-${c1}其他-${c1}其他"
          case catTriplePattern(c1, c2, "无") => s"$c1-$c2-${c2}其他"
          case _ => result
        }
      } else null
    })

    spark.sqlContext.udf.register("category_domain", (c1: String, c2: String, c3: String) =>  {
      domainCategoryMapping.get(s"${c1}_${c2}_${c3}")
        .orElse(domainCategoryMapping.get(s"${c1}_${c2}_其他"))
        .orElse(domainCategoryMapping.get(s"${c1}_其他_其他"))
        .getOrElse("其他")
    })

    spark.sql("create temporary function parse_array as 'com.netease.wm.udf.ParseArrayJson'")

    val sql =
     s"""
        |select p.id as postId,
        |       p.blogId as blogId,
        |       p.contentType,
        |       coalesce(a.category, b.category, c.category, d.category) as category,
        |       coalesce(a.category2, b.category2, c.category2, d.category2, concat(coalesce(a.category, b.category, c.category, d.category), '其他')) as category2,
        |       coalesce(a.category3, b.category3, c.category3, d.category3, concat(coalesce(a.category2, b.category2, c.category2, d.category2, a.category, b.category, c.category, d.category), '其他')) as category3
        |from lofter.dim_post p
        |    left join (
        |        select id as postId, 'CP' as category, '其他cp' as category2, '其他cp' as category3
        |        from (select id, tag from lofter.dim_post lateral view explode(tags) as tag) a
        |            join (select name as tag from lofter_db_dump.ods_db_cmb_tag_nd where fanflag = 1 and status = 0) b on a.tag = b.tag
        |        group by id
        |    ) a on p.id = a.postId
        |    left join (
        |        select postId,
        |               nvl(nullif(first_value(get_json_object(customTags, '$$[0].firstTag'), true), '无'), '其他') as category,
        |               nvl(nullif(first_value(get_json_object(customTags, '$$[0].secondTag'), true), '无'), '其他') as category2,
        |               nvl(nullif(first_value(get_json_object(customTags, '$$[0].thirdTag'), true),'无'), '其他') as category3
        |        from lofter_db_dump.ods_db_recommend_review_post_nd
        |        where customTags is not null and recomStatus=1 and
        |              from_unixtime(cast(createTime / 1000 as bigint), 'yyyy-MM-dd') >= '2021-07-01' and
        |              get_json_object(customTags, '$$[0].firstTag') != '无'
        |        group by postId
        |    ) b on p.id = b.postId
        |    left join (
        |        select postId,
        |               first_value(split(max_score_item(label), '-')[0], true) as category,
        |               first_value(split(max_score_item(label), '-')[1], true) as category2,
        |               first_value(split(max_score_item(label), '-')[2], true) as category3
        |        from (
        |            select postId, label
        |            from lofter.stg_post_content_feature_dd
        |            where dt = '$date' and pt_type='category'
        |        ) t
        |        group by postId
        |    ) c on p.id = c.postId
        |    left join (
        |        select postId,
        |               first_value(split(max_score_item(label), '-')[0], true) as category,
        |               first_value(split(max_score_item(label), '-')[1], true) as category2,
        |               first_value(split(max_score_item(label), '-')[2], true) as category3
        |        from (
        |            select postId, label
        |            from lofter.stg_post_content_feature_dd
        |            where dt = '$date' and pt_type='video'
        |        ) t
        |        group by postId
        |    ) d on p.id = d.postId
        |    where length(coalesce(a.category, b.category, c.category, d.category)) > 0 and publishDate >= '2021-01-01'
        |""".stripMargin

    spark.sql(sql)
      .withColumn("domain", expr("category_domain(category, category2, category3)"))
      .withColumn("dt", lit(date))
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter.dim_post_category_dd")

    val extSql =
     s"""
        |select postId, blogId, contentType,
        |       split(category, '-')[0] as category,
        |       nvl(nullif(split(category, '-')[1], '无'), '其他') as category2,
        |       nvl(nullif(split(category, '-')[2], '无'), '其他') as category3
        |from (
        |  select p.id as postId, p.blogId as blogId,
        |       p.contentType,
        |       coalesce(a.category, b.category, c.category, d.category) as category
        |  from lofter.dim_post p
        |    left join (
        |        select id as postId, 'CP-其他-其他' as category
        |        from (select id, tag from lofter.dim_post lateral view explode(tags) as tag) a
        |            join (select name as tag from lofter_db_dump.ods_db_cmb_tag_nd where fanflag = 1 and status = 0) b on a.tag = b.tag
        |        group by id
        |    ) a on p.id = a.postId
        |    left join (
        |        select postId, concat_ws('-', get_json_object(label, '$$.firstTag'), get_json_object(label, '$$.secondTag'), get_json_object(label, '$$.thirdTag')) as category
        |        from lofter_db_dump.ods_db_recommend_review_post_nd lateral view explode(parse_array(customTags)) as label
        |        where customTags is not null and recomStatus=1 and from_unixtime(cast(createTime / 1000 as bigint), 'yyyy-MM-dd') >= '2021-07-01' and
        |              length(nullif(get_json_object(label, '$$.firstTag'),'无')) > 0
        |    ) b on p.id = b.postId
        |    left join (
        |        select postId,
        |               first_value(max_score_item(label), true) as category
        |        from (
        |            select postId, label
        |            from lofter.stg_post_content_feature_dd
        |            where dt = '$date' and pt_type='category'
        |        ) t
        |        group by postId
        |    ) c on p.id = c.postId
        |    left join (
        |        select postId,
        |               first_value(max_score_item(label), true) as category
        |        from (
        |            select postId, label
        |            from lofter.stg_post_content_feature_dd
        |            where dt = '$date' and pt_type='video'
        |        ) t
        |        group by postId
        |    ) d on p.id = d.postId
        |    where length(coalesce(a.category, b.category, c.category, d.category)) > 0 and publishDate >= '2021-01-01'
        |) t
        |where length(split(category, '-')[0]) > 0 and split(category, '-')[0] != '无'
        |group by postId, blogId, contentType, category
        |""".stripMargin

    spark.sql(extSql)
      .withColumn("domain", expr("category_domain(category, category2, category3)"))
      .withColumn("dt", lit(date))
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter.dim_post_category_set_dd")

    spark.close()
  }
}
