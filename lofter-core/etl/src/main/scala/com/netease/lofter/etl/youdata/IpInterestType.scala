package com.netease.lofter.etl.youdata

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.hadoop.fs.{FileContext, Path}
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}

import java.net.URI

object IpInterestType {
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
    val monthAgo = DateTime.parse(date).minusDays(29).toString("yyyy-MM-dd")

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

    spark.sql("create temporary function bitmap_union as 'com.netease.wm.udf.bitmap.BitmapUnionUDAF';")
    spark.sql("create temporary function bitmap_count as 'com.netease.wm.udf.bitmap.BitmapCountUDF';")

    val sql =
      s"""
         |select m.ip as interest_name, n.category, n.derivedflag, m.realPv as interest_realpv, m.userUv as interest_realuv,
         |       case when m.userUv >= 300000 and m.realPv >= 1000000 then '头部'
         |            when m.userUv >= 100000 and m.realPv >= 200000 then '腰部'
         |            else '其他' end as interest_type,
         |       category_domain(n.category) as domain
         |from (
         |    select ip, sum(a.realBrowsePlayPv * post / total) / 30 as realPv,
         |           count(distinct if(total > 3 and post > 2, b.userId, null)) as userUv
         |    from (
         |        select userId, sum(browse_pv) as realBrowsePlayPv, bitmap_count(bitmap_union(browse_post_bitmap)) as total
         |        from lofter.dws_par_user_content_di
         |        where dt <= '$date' and dt >= '$monthAgo'
         |        group by userId
         |    ) a
         |    join (
         |        select ip, userId,
         |               post_30d as post
         |        from lofter.dws_ip_user_consume_dd
         |        where dt = '$date'
         |    ) b on a.userId = b.userId
         |    group by ip
         |) m
         |join (select ip, category, derivedflag from lofter.dim_ip_dd lateral view explode(categories) as category where dt = '$date') n on m.ip = n.ip
         |""".stripMargin

    spark.sql(sql).repartition(1)
      .withColumn("dt", lit(date))
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_ip_interest_type_di")

    spark.close()
  }

}
