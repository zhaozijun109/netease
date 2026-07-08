package com.netease.lofter.etl.youdata

import com.netease.wm.util.Args
import org.apache.hadoop.fs.{FileContext, Path}
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}

import java.net.URI

object IpCategoryPostSupply {
  val catTriplePattern = """(?U)(\w+)\-(\w+)\-(\w+)""".r
  val catDoublePattern = """(?U)(\w+)\-(\w+)""".r

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

    spark.sqlContext.udf.register("category_domain", (input: String) =>  {
      val (c1, c2, c3) = input match {
        case catTriplePattern(c1, c2, c3) => (c1, c2, c3)
        case catDoublePattern(c1, c2) => (c1, c2, "其他")
        case _ => (input, "其他", "其他")
      }
      domainCategoryMapping.get(s"${c1}_${c2}_${c3}")
        .orElse(domainCategoryMapping.get(s"${c1}_${c2}_其他"))
        .orElse(domainCategoryMapping.get(s"${c1}_其他_其他"))
        .getOrElse("其他")
    })

    val sql =
     s"""
        |select c.ip, c.category, c.domain,
        |       count(distinct a.blogId) blog_total,
        |       count(distinct if(datediff('$date', a.publishDate) < 30, a.blogId, null)) blog_sum_30d,
        |       count(distinct if(datediff('$date', a.publishDate) < 7, a.blogId, null)) blog_sum_7d,
        |       count(distinct a.postId) post_total,
        |       count(distinct if(datediff('$date', a.publishDate) < 30, a.postId, null)) post_sum_30d,
        |       count(distinct if(datediff('$date', a.publishDate) < 7, a.postId, null)) post_sum_7d,
        |       count(distinct if(interest_post_real30d > 0, d.postid, null)) interest_post_real30d,
        |       count(distinct if(interest_post_real7d > 0, d.postid, null)) interest_post_real7d,
        |       count(distinct if(interest_post_real1d > 0, d.postid, null)) interest_post_real1d
        |from (
        |    select id as postId, blogId, tag, publishDate
        |    from lofter.dim_post
        |    lateral view explode(tags) t2 as tag
        |    where publishDate >= '2020-01-01' and
        |        isPublished = true  and
        |        isForbidden = false  and
        |        allowView in (0,100) and
        |        isCitedPost = false and
        |        contentType <> '问答'
        |) a
        |join lofter.dwd_tag_ip_mapping_nd b on a.tag = b.tag
        |join (
        |    select ip, category, category_domain(category) as domain
        |    from lofter.dim_ip_dd
        |    lateral view explode(categories) as category
        |    where dt = '$date'
        |) c on b.ip = c.ip
        |left join (
        |  select postId,
        |         sum(if(realBrowsePv > 0, 1, 0)) interest_post_real30d,
        |         sum(if(datediff('$date', dt) < 7 and realBrowsePv > 0, 1, 0)) interest_post_real7d,
        |         sum(if(dt = '$date' and realBrowsePv > 0, 1, 0)) interest_post_real1d
        |  from lofter.dws_post_base_stats_di
        |  where datediff('$date', dt) < 30 and dt <= '$date'
        |  group by postId
        |) d on a.postId = d.postId
        |group by c.ip, c.category, c.domain
        |""".stripMargin

    spark.sql(sql).repartition(1)
      .withColumn("dt", lit(date))
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_ip_category_post_supply_di")

    spark.close()
  }
}
