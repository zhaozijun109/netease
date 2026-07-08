package com.netease.lofter.etl.dim

import com.netease.wm.util.Args
import org.apache.hadoop.fs.{FileContext, Path}
import org.apache.spark.sql.functions.{expr, lit}
import org.apache.spark.sql.{SaveMode, SparkSession}

import java.net.URI

object IpJob {
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

    spark.sqlContext.udf.register("normalize_cat", (input: String) =>  {
        input match {
          case catTriplePattern("CP", "无", "无") => s"CP-其他cp-其他cp"
          case catTriplePattern("CP", "其他cp", "无") => s"CP-其他cp-其他cp"
          case catTriplePattern(c1, "无", "无") => s"$c1-${c1}其他-${c1}其他"
          case catTriplePattern(c1, c2, "无") => s"$c1-$c2-${c2}其他"
          case _ => input
        }
    })

    spark.sqlContext.udf.register("category_domain", (c1: String, c2: String, c3: String) =>  {
      domainCategoryMapping.get(s"${c1}_${c2}_${c3}")
        .orElse(domainCategoryMapping.get(s"${c1}_${c2}_其他"))
        .orElse(domainCategoryMapping.get(s"${c1}_其他_其他"))
        .getOrElse("其他")
    })

    val sql =
      """
        |select l.name as ip, l.derivedflag,
        |       collect_set(normalize_cat(n.category)) as categories,
        |       collect_set(n.domain) as domains
        |from (
        |    select id, name, derivedflag
        |    from lofter_db_dump.ods_db_cmb_ip_nd
        |    where status = 0
        |    group by 1, 2, 3
        |) l
        |left join (
        |    select id,name,derivedflag, cast(categoryId as bigint) as categoryId
        |    from lofter_db_dump.ods_db_cmb_ip_nd
        |          lateral view explode(split(categoryIds,';')) t as categoryId
        |    where status = 0
        |) m on l.id = m.id
        |left join (
        |    select id as categoryId, name as category, category_domain(name, '其他','其他') as domain
        |    from lofter_db_dump.ods_db_cmb_category_nd where level = 1 and status = 0
        |
        |    union all
        |
        |    select a.id as categoryId, concat_ws('-', b.name, a.name) as category, category_domain(b.name, a.name,'其他') as domain
        |    from lofter_db_dump.ods_db_cmb_category_nd a
        |    join lofter_db_dump.ods_db_cmb_category_nd b on a.parentId = b.id
        |    where a.level = 2 and a.status = 0 and b.level = 1 and b.status = 0
        |
        |    union all
        |
        |    select a.id as categoryId, concat_ws('-', c.name, b.name, a.name) as category, category_domain(c.name, b.name, a.name) as domain
        |    from lofter_db_dump.ods_db_cmb_category_nd a
        |    join lofter_db_dump.ods_db_cmb_category_nd b on a.parentId = b.id
        |    join lofter_db_dump.ods_db_cmb_category_nd c on b.parentId = c.id
        |    where a.level = 3 and a.status = 0 and b.level = 2 and b.status = 0 and c.level = 1 and c.status = 0
        |) n on m.categoryId = n.categoryId
        |group by l.name, l.derivedflag
        |
        |""".stripMargin

    spark.sql(sql).repartition(1)
      .withColumn("dt", lit(date))
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter.dim_ip_dd")

    spark.close()
  }

}
