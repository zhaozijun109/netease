package com.netease.lofter.data.jobs.backend

import java.sql.Connection

import com.github.nscala_time.time.Imports.DateTime
import com.netease.lofter.data.common.databases
import com.netease.wm.util.Args
import org.apache.spark.sql.{Row, SaveMode, SparkSession}

/**
 * LOFTER-17852 广告平台传媒领域人群包
 */
object AdTagUserGroupForMedia {
  val BATCH_SIZE = 100

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val yesterday = DateTime.yesterday.toString("yyyy-MM-dd")
    val date = pargs.optional("date").getOrElse(yesterday)
    val prevDate = DateTime.parse(date).minusDays(1).toString("yyyy-MM-dd")
    val prevWeekTagDate = DateTime.parse(date).minusDays(3).withDayOfWeek(6).minusWeeks(1).toString("yyyy-MM-dd")
    val threeMonthAgo = DateTime.parse(date).minusMonths(3).toString("yyyy-MM-dd")

    val userGroupSql =
      s"""
         |select
         |  aa.domains,
         |  bb.userId, bb.idfa as deviceId,
         |  cast(bb.birth_y as int) as birth_y,
         |  case bb.gender when '男' then 2 when '女' then 1 else 0 end as gender
         |from
         |  (
         |    select
         |      cast(a.userid as bigint) as userId,
         |      concat_ws(',', collect_set(c.domainName)) as domains
         |    from
         |      (
         |        select
         |          user_id userid,
         |          tag,
         |          score
         |        from
         |          rec.rec_fea_user_long_profile_tags_v3 lateral view explode(str_to_map(l_p_tag_pos)) t as tag,
         |          score
         |        where day = '$date'
         |      ) a
         |      /**用户喜欢的标签**/
         |      join (
         |        select
         |          domainName,
         |          tag
         |        from
         |          lofter.dim_domain lateral view explode(tags) t1 as tag
         |      ) c
         |      /**标签和领域对应关系**/
         |      on a.tag=c.tag
         |      group by a.userId
         |  ) aa
         |  right join (
         |    select
         |      cast(a.userid as bigint) as userId,
         |      b.type1,
         |      b.idfa,
         |      first_value(if(pt_tag = 'birth_y', tag_value, null)) as birth_y,
         |      first_value(if(pt_tag = 'gender', tag_value, null)) as gender
         |    from
         |      (
         |        select
         |          user_id as userId,
         |          tag_value,
         |          pt_tag
         |        from
         |          lofter.dwb_par_lofter_tag_wd
         |        where
         |          pt_d = '$prevWeekTagDate' and (pt_tag = 'birth_y' or pt_tag = 'gender')
         |          /**数据业务部的画像数据，pt_Tag代表画像的类别**/
         |      ) a
         |      join (
         |        select
         |          lower(sid) userid,
         |          tid idfa,
         |          "idfa" type1
         |        from
         |          lofter.dwd_device_mapping
         |        where
         |          sid_tp='userid'
         |          and tid_tp='idfa' and lastNo = 1
         |          and  from_unixtime(cast(lastTime/1000 as bigint), 'yyyy-MM-dd') >= '$threeMonthAgo'
         |        union all
         |        select
         |          lower(sid) userid,
         |          tid idfa,
         |          "imei" type1
         |        from
         |          lofter.dwd_device_mapping
         |        where
         |          sid_tp='userid'
         |          and tid_tp='imei' and lastNo = 1
         |          and from_unixtime(cast(lastTime/1000 as bigint), 'yyyy-MM-dd') >= '$threeMonthAgo'
         |        union all
         |        select
         |          lower(sid) userid,
         |          tid idfa,
         |          "idfv" type1
         |        from
         |          lofter.dwd_device_mapping
         |        where
         |          sid_tp='userid'
         |          and tid_tp='idfv' and lastNo = 1
         |          and from_unixtime(cast(lastTime/1000 as bigint), 'yyyy-MM-dd') >= '$threeMonthAgo'
         |        union all
         |        select
         |          lower(sid) userid,
         |          tid idfa,
         |          "oaid" type1
         |        from
         |          lofter.dwd_device_mapping
         |        where
         |          sid_tp='userid'
         |          and tid_tp='oaid' and lastNo = 1
         |          and from_unixtime(cast(lastTime/1000 as bigint), 'yyyy-MM-dd') >= '$threeMonthAgo'
         |        union all
         |        select
         |          lower(sid) userid,
         |          tid idfa,
         |          "android_id" type1
         |        from
         |          lofter.dwd_device_mapping
         |        where
         |          sid_tp='userid'
         |          and tid_tp='androidid' and lastNo = 1
         |          and from_unixtime(cast(lastTime/1000 as bigint), 'yyyy-MM-dd') >= '$threeMonthAgo'
         |      ) b on a.userId=b.userId
         |      group by a.userId, b.idfa, b.type1
         |  ) bb on aa.userId = bb.userId
         |""".stripMargin

    spark.sql(userGroupSql).createOrReplaceTempView("ug_origin")

    val ugDeviceSql =
      """
        |select domains, deviceId, userId, birth_y, gender
        |from (
        |    select domains, deviceId, userId, birth_y, gender, row_number() over (partition by deviceId order by userId desc) as rnk
        |    from ug_origin
        |) t
        |where rnk = 1
        |""".stripMargin

    spark.sql(ugDeviceSql).cache().createOrReplaceTempView("device")

    spark.table("device").write.mode(SaveMode.Overwrite).parquet(s"/user/da_lofter/warehouse/ad_media_device/$date")

    spark.read.parquet(s"/user/da_lofter/warehouse/ad_media_device/$prevDate").createOrReplaceTempView("device_prev")

    // delete old deviceId ext
    spark.sql("select a.deviceId from device_prev a left join device b on a.deviceId = b.deviceId where b.deviceId is null")
      .repartition(10)
      .foreachPartition { xs: Iterator[Row]=>
        import databases.getDDBConn
        import com.netease.lofter.data.common.spark.SparkSqlImplicits._
        import com.netease.wm.util.Sql._
        implicit val conn: Connection = getDDBConn

        try {
          xs.toSeq.grouped(BATCH_SIZE).foreach { rows =>
            val batch = rows.map(rowParam)
            sql"delete from Ad_UserFeatureExt where deviceId = ${"deviceId"}".batchUpdate(batch)
          }
        } finally {
          conn.close()
        }
      }

    // insert new deviceId ext
    spark.sql("select a.deviceId, a.userId, a.birth_y, a.gender, a.domains from device a left join device_prev b on a.deviceId = b.deviceId where b.deviceId is null")
      .repartition(10)
      .foreachPartition { xs: Iterator[Row]=>
        import databases.getDDBConn
        import com.netease.lofter.data.common.spark.SparkSqlImplicits._
        import com.netease.wm.util.Sql._
        implicit val conn: Connection = getDDBConn

        try {
          xs.toSeq.grouped(BATCH_SIZE).foreach { rows =>
            val batch = rows.map(rowParam)
            sql"insert ignore into Ad_UserFeatureExt(id, deviceId, userId, gender, age, domains, createTime, updateTime) values(seq, ${"deviceId"}, ${"userId"}, ${"gender"}, ${"birth_y"}, ${"domains"}, now(), now())".batchUpdate(batch)
          }
        } finally {
          conn.close()
        }
      }

  }
}
