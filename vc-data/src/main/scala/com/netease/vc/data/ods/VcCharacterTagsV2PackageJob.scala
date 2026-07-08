package com.netease.vc.data.ods

import org.apache.spark.sql.{SaveMode, SparkSession}

object VcCharacterTagsV2PackageJob {
  def main(args: Array[String]): Unit = {
//    val pargs = Args(args)
//    val dt = pargs.required("date")

    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val tag_relation = spark.read
      .format("jdbc")
      .option("url", "jdbc:mysql://vcharacter-mysql-rw-online.db.gy.ntes:4331/vcharacter?characterEncoding=utf-8&useSSL=false&autoReconnect=true&useAffectedRows=true&useLegacyDatetimeCode=false&useTimezone=true&serverTimezone=Asia/Shanghai")
      .option("dbtable", "vc_character_tag_relation")
      .option("user", "online_algorithm_r_user")
      .option("password", "_FN68@jDm")
      .option("driver","com.mysql.jdbc.Driver")
      .load()
      .filter("status = 2")
    tag_relation.createOrReplaceTempView("vc_character_tag_relation_sv")

    val vc_tag_v2 = spark.read
      .format("jdbc")
      .option("url", "jdbc:mysql://vcharacter-mysql-rw-online.db.gy.ntes:4331/vcharacter?characterEncoding=utf-8&useSSL=false&autoReconnect=true&useAffectedRows=true&useLegacyDatetimeCode=false&useTimezone=true&serverTimezone=Asia/Shanghai")
      .option("dbtable", "vc_tag")
      .option("user", "online_algorithm_r_user")
      .option("password", "_FN68@jDm")
      .option("driver","com.mysql.jdbc.Driver")
      .load()
      .filter("status = 2")
    vc_tag_v2.createOrReplaceTempView("vc_tag_sv")

    spark.sql("""select l1_id,
                |       l1_name,
                |       l1_code,
                |       l1_code_type,
                |       l2_id,
                |       l2_name,
                |       l2_code,
                |       l2_code_type
                |from (select id                                                                   as l1_id,
                |             name                                                                 as l1_name,
                |             category                                                             as l1_code,
                |             case when category = 1 then '类型' when category = 2 then '标签' end as l1_code_type
                |      from vc_tag_sv
                |      where pid = 0) x1
                |         left join
                |     (select id                                                                   as l2_id,
                |             name                                                                 as l2_name,
                |             category                                                             as l2_code,
                |             case when category = 1 then '类型' when category = 2 then '标签' end as l2_code_type,
                |             pid
                |      from vc_tag_sv
                |      where pid != 0) x2 on x1.l1_id = x2.pid""".stripMargin)
      .createOrReplaceTempView("vc_character_tags_sv")

    spark.sql("""select character_id,
                |       CONCAT_WS(',', COLLECT_SET(trim(l1_name)))                        as l1_name_set_str,
                |       CONCAT_WS(',', ARRAY_DISTINCT(FLATTEN(COLLECT_LIST(tag_l2_set)))) as l2_name_set_str,
                |       REPLACE(CONCAT_WS(',', COLLECT_SET(to_json(result))), '},{', ',') as l1l2json
                |from (
                |         select x1.character_id,
                |                x2.l1_name,
                |                COLLECT_SET(trim(l2_name))                  as tag_l2_set,
                |                map(x2.l1_name, COLLECT_SET(trim(l2_name))) as result
                |         from (select character_id, tag_id from vc_character_tag_relation_sv where status = 2) x1
                |                  left join
                |              (select l2_id, l2_name, l1_name
                |               from vc_character_tags_sv) x2
                |              on x1.tag_id = x2.l2_id
                |         where x2.l1_name is not null
                |         group by x1.character_id, x2.l1_name
                |         )
                |group by character_id""".stripMargin)
      .write.mode(SaveMode.Overwrite)
      .saveAsTable("vc.dwd_vc_character_tags_v2_dd")

    spark.stop()
  }
}
