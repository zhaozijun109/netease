package com.netease.lofter.etl.dim

import com.netease.wm.util.Args
import org.apache.spark.sql.{SaveMode, SparkSession}

object BenefitProductJob {

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.required("date")
    val output = pargs.required("output")

    spark.sql("create temporary function parse_array as 'com.netease.wm.udf.ParseArrayJson'")

    val sql_1 =
      s"""
         |select a.productId,
         |       a.productName, a.productType,
         |       c.name as supplyName, b.purchaseSupplierId as supplierId,
         |       a.activityCode, a.cardProductId, a.activityid, a.slotType,
         |       a.createtime,a.updatetime,a.onsaletime,a.offsaletime,a.presaletype,a.cardPoolName
         |from (
         |    select t1.id as productId, t1.productName, t1.type as productType, t2.activityCode,t2.name as cardPoolName,
         |           if(t1.type = 5, t2.cardProductId, t1.id) cardProductId,
         |           if(t1.type in (5, 6, 9), t3.id, t1.id) activityId,
         |           t3.slotType,
         |           t1.createtime,t1.updatetime,t1.onsaletime,t1.offsaletime,t1.presaletype
         |    from lofter_db_dump.ods_db_benefit_product_info_nd t1
         |    left join lofter_db_dump.ods_db_benefit_card_pool_nd t2 on (
         |        t1.type = 5 and t1.id = t2.chanceProductId) or
         |        (t1.type in (6, 9) and t1.id = t2.cardProductId
         |    )
         |    left join lofter_db_dump.ods_db_benefit_card_activity_nd t3 on t2.activityCode = t3.code
         |) a
         |left join lofter_db_dump.ods_db_benefit_product_info_nd b on a.cardProductId = b.id
         |left join lofter_db_dump.ods_db_benefit_supplier_info_nd c on b.purchaseSupplierId = c.id
         |""".stripMargin

    val sql_2 =
      s"""
         |select a.productId, a.categoryType,
         |       b.category1, b.category1_name,
         |       b.category2, b.category2_name,
         |       b.category3, b.category3_name
         |from (
         |  select productId, categoryId, categoryType
         |  from lofter_db_dump.ods_db_benefit_category_product_relation_nd
         |  where categoryType = 0
         |) a join lofter.dim_benefit_product_category b on a.categoryId = b.id
         | where b.categoryType = 0
         |union all
         |    select c.productId, c.categoryType, null as category1, concat_ws(',', collect_set(d.name)) as category1_name,
         |        null as category2, null as category2_name,
         |        null as category3, null as category3_name
         |    from (
         |        select productId, categoryId, categoryType
         |        from lofter_db_dump.ods_db_benefit_category_product_relation_nd
         |        where categoryType = 3
         |    ) c left join (
         |        select id, name
         |        from lofter_db_dump.ods_db_benefit_category_nd
         |        where type = 3
         |    ) d
         |    on c.categoryId = d.id
         |    group by c.productId, c.categoryType
         |""".stripMargin


    spark.sql(sql_1).createOrReplaceTempView("t1")
    spark.sql(sql_2).createOrReplaceTempView("t2")

    val sql_res =
      s"""
         |select t1.productId,t1.productName,t1.supplyName, t1.productType, t3.ips,
         |       t2.category1, t2.category1_name, t2.category2, t2.category2_name, t2.category3, t2.category3_name,
         |       t1.activityCode,supplierId,slotType,presaletype,cardPoolName,
         |       createtime,updatetime,onsaletime,offsaletime
         |from t1 left join t2 on t1.activityId=t2.productId
         |        left join (
         |          select productId, collect_set(cmbIpName) as ips
         |          from lofter_db_dump.ods_db_benefit_product_cmb_ip_relation_nd
         |          where status = 0
         |          group by productId
         |        ) t3 on t1.activityid = t3.productId
         |""".stripMargin

    val outPath =  s"$output/dt=$date"
    spark.sql(sql_res).repartition(1).write.mode(SaveMode.Overwrite).parquet(outPath)
    spark.sql(s"alter table lofter.dim_benefit_product set location '$outPath'")
    spark.close()
  }

}
