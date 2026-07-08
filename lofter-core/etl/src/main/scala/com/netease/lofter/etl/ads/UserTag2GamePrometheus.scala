package com.netease.lofter.etl.ads

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

object UserTag2GamePrometheus {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val dt = pargs.required("date")

    val dtNum = dt.replace("-", "").toLong

    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", value = true)
      .getOrCreate()

    val twoWeeksAgo = DateTime.parse(dt).minusWeeks(2).toString("yyyy-MM-dd")

    val buildPrimary =
     s"""
        |insert overwrite table lofter.lofter_user_portrait_to_game_staging_primary_dd partition(dt = '$dt')
        |select *, 'userid' as id_type
        |from (
        |    select a.userId as userid,
        |           if(a.accountType = '邮箱', a.account,null) as urs,
        |           concat_ws(',', a.phones) as phone,
        |           concat_ws(',', b.devices) as dev
        |    from (
        |       select userId, accountType, account, phones, idNumber
        |       from lofter.dws_par_user_base_dd
        |       where dt='$dt'
        |    ) a
        |    left join (
        |      select user_id,
        |             collect_set(concat(device_type, '_', trim(device_id))) as devices
        |      from lofter.dwb_par_lofter_device_tag_wd
        |      where pt_d >= '$twoWeeksAgo'
        |      group by user_id
        |    ) b on a.userId = b.user_id
        |) t
        |where length(urs) > 0 or length(phone) > 0 or length(dev) > 0
        |distribute by 1
        |""".stripMargin

    spark.sql(buildPrimary)

    val buildAttr =
     s"""
        |with user_devices as (
        | select a.userId, a.deviceUdid, b.appchannel, trim(split(b.deviceModel, ',')[0]) as deviceModel, b.deviceOs
        | from (
        |     select cast(sid as bigint) as userId, tid as deviceUdid
        |     from lofter.dwd_device_mapping
        |     where sid_tp ='userid' and tid_tp = 'deviceudid'
        | ) a join (select deviceUdid, appchannel, deviceModel, deviceOs from lofter.dwd_par_device_all_dd where dt= '$dt') b on a.deviceUdid = b.deviceUdid
        | where userId > 0
        |)
        |insert overwrite table lofter.lofter_user_portrait_to_game_staging_attr_dd
        |select b.userId,b.urs,b.phone,b.device,b.id_type, a.val, '$dt' as dt, a.label
        |from (
        |        select userId, 'birth_year' as label, birth_year as val
        |        from lofter.dws_par_user_base_dd
        |        where dt='$dt' and birth_year is not null
        |    union all
        |        select userId, 'gender' as label, case gender when '2' then '0' else '1' end as val
        |        from lofter.dws_par_user_base_dd
        |        where dt='$dt' and gender in ('1', '2')
        |    union all
        |        select userId, 'id_md5' as label, md5(idNumber) as val
        |        from lofter.dws_par_user_base_dd
        |        where dt='$dt' and length(idNumber) > 0
        |    union all
        |        select userId, 'id_md5' as label, md5(idNumber) as val
        |        from lofter.dws_par_user_base_dd
        |        where dt='$dt' and length(idNumber) > 0
        |    union all
        |        select userId, 'id_area' as label, substr(idNumber, 0, 6) as val
        |        from lofter.dws_par_user_base_dd
        |        where dt='$dt' and length(idNumber) > 0
        |    union all
        |        select userId, 'ip' as label, last_login_ip as val
        |        from lofter.dws_par_user_base_dd
        |        where dt='$dt' and length(last_login_ip) > 0  and userId > 0
        |    union all
        |        select userId, 'city' as label, city as val
        |        from lofter.dws_par_user_base_dd
        |        where dt='$dt' and length(city) > 0  and userId > 0
        |    union all
        |        select userId, 'province' as label, province as val
        |        from lofter.dws_par_user_base_dd
        |        where dt='$dt' and length(province) > 0  and userId > 0
        |    union all
        |        select userId, 'new_time' as label, from_unixtime(cast(createTime/1000 as bigint), 'yyyyMMdd HH:mm:ss')  as val
        |        from lofter.dws_par_user_base_dd
        |        where dt='$dt' and createTime > 0  and userId > 0
        |    union all
        |        select userId, 'final_time' as label, from_unixtime(cast(last_login_time/1000 as bigint), 'yyyyMMdd HH:mm:ss') as val
        |        from lofter.dws_par_user_base_dd
        |        where dt='$dt' and last_login_time > 0
        |    union all
        |        select userId, 'cnt_total_day' as label, count(distinct dt) as val
        |        from lofter.dws_par_user_session_di
        |        where session_total_time > 0 and dt <= '$dt'
        |        group by userId
        |    union all
        |        select userId, 'total_online' as label, sum(session_total_time/1000) as val
        |        from lofter.dws_par_user_session_di
        |        where session_total_time > 0 and dt <= '$dt'
        |        group by userId
        |    union all
        |        select userId, 'first_pay_dt' as label, from_unixtime(cast(min(first_pay_time)/1000 as bigint), 'yyyyMMdd') as val
        |        from lofter.dws_user_pay_type_info_dd
        |        where dt='$dt' and first_pay_time > 0
        |        group by userId
        |    union all
        |        select userId, 'total_pay' as label, trade_money as val
        |        from
        |        (select userId, sum(money) as trade_money
        |        from lofter.dwd_user_order_dd
        |        where dt='$dt' and money > 0
        |        group by userId) a
        |    union all
        |        select userId, 'cnt_total_pay' as label, trade_num as val
        |        from
        |        (select userId, count(1) as trade_num
        |        from lofter.dwd_user_order_dd
        |        where dt='$dt'
        |        group by userId) a
        |    union all
        |        select user_id as userId, 'prefer_ips' as label, to_json(str_to_map(prefer_ips, ',', ':')) as val
        |        from (
        |            select *, row_number() over (partition by user_id order by day desc) rk
        |            from rec.rec_lofter_user_profile_ips_kv
        |            where day <= '$dt'
        |        ) t
        |        where rk = 1
        |    union all
        |      select userId, label, val
        |      from (
        |         select cast(user_id as bigint) as userId,
        |                pt_tag as label,
        |                tag_value as val,
        |                row_number() over (partition by user_id order by pt_d desc ) as rk
        |         from lofter.dwb_par_lofter_tag_wd
        |         where pt_d >= '$twoWeeksAgo' and cast(user_id as bigint) > 0 and
        |               pt_tag in ('has_chd', 'married', 'has_car', 'edu_degree','profession', 'prof_type', 'phone_price_180d', 'city_reside_grade')
        |     ) t where rk = 1
        |
        |  union all
        |    select userId, 'app_channel' as label, concat_ws(',', collect_set(appchannel)) as val
        |    from user_devices
        |    where length(appChannel) > 0
        |    group by userId
        |  union all
        |    select userId, 'device_model' as label,  concat_ws(',', collect_set(deviceModel)) as val
        |    from user_devices
        |    where length(deviceModel) > 0
        |    group by userId
        |  union all
        |   select userId, 'os' as label,  concat_ws(',', collect_set(deviceOs)) as val
        |   from user_devices
        |   where length(deviceOs) > 0
        |   group by userId
        |) a
        |join (
        |  select * from lofter.lofter_user_portrait_to_game_staging_primary_dd where dt = '$dt'
        |) b on a.userId = b.userId
        |""".stripMargin

    spark.sql(buildAttr)

    val buildIncr =
     s"""
        |insert overwrite table lofter.lofter_user_portrait_to_game_staging_incr_di
        |select b.userId,b.urs,b.phone,b.device,b.id_type, a.val, '$dt' as dt, a.label
        |from (
        |   select m.*
        |   from (
        |	    select user_id as userId, 'prefer_ips' as label, to_json(str_to_map(prefer_ips, ',', ':')) as val
        |		  from rec.rec_lofter_user_profile_ips_kv
        |	    where day = '$dt'
        |  ) m join (select userId from lofter.dws_par_user_active_di where dt='$dt') n on m.userId = n.userId
        |	union all
        |	    select userId, 'day_pay' as label, trade_money as val
        |     from
        |	    (select userId, sum(money) as trade_money
        |     from lofter.dwd_user_order_dd
        |	    where dt='$dt' and money > 0
        |     group by userId) a
        |	union all
        |	    select userId, 'day_online' as label, sum(session_total_time/1000) as val
        |	    from lofter.dws_par_user_session_di
        |	    where dt = '$dt' and session_total_time > 0
        |	    group by userId
        |	union all
        |	    select accountId as userId, 'login_detail' as label, concat_ws(',',collect_set(from_unixtime(cast(actionTime/1000 as bigint), 'yyyyMMdd HH:mm:00'))) as val
        |	    from lofter.dwd_evt_user_login_di
        |	    where dt='$dt' and accountId > 0 and length(loginType) > 0 and actionType = 'login'
        |	    group by accountId
        | union all
        |     select userId, 'pay_detail' as label,
        |            concat_ws(',', collect_list(concat_ws('#', from_unixtime(cast(order_time/1000 as bigint), 'yyyyMMdd HH:mm:ss'), case when order_type = 'gift_present' then 'coin' else '' end , money))) as val
        |     from lofter.dwd_user_order_dd
        |     where dt = '$dt' and money > 0 and pay_date = '$dt'
        |     group by userId
        |) a
        |join (
        |  select * from lofter.lofter_user_portrait_to_game_staging_primary_dd where dt = '$dt'
        |) b on a.userId = b.userId
        |""".stripMargin

    spark.sql(buildIncr)

    // users(486450383, 486500141) with special code in urs cause tsv output row mismatch
    // temp workaround just filtering these data
    val outputIncr =
     s"""
        |insert overwrite table lofter.lofter_user_portrait_to_game_di partition(pt_d = '$dt')
        |select *, '$dtNum' as dt
        |from (
        |  select urs, phone, regexp_replace(device, '\\\\s+','') as device, b.userId as userId, id_type, label, value
        |  from (
        |    select userId
        |    from lofter.lofter_user_portrait_to_game_staging_incr_di
        |    where dt = '$dt'
        |    group by userId
        |  ) a
        |  join (
        |    select * from lofter.lofter_user_portrait_to_game_staging_attr_dd where dt = '$dt'
        |  ) b on a.userId = b.userId
        |
        |union all
        |  select urs, phone, regexp_replace(device, '\\\\s+','') as device, userId, id_type, label, value
        |  from lofter.lofter_user_portrait_to_game_staging_incr_di
        |  where dt = '$dt'
        |) t
        |where (urs is null or regexp_replace(urs, '[^\\\\x00-\\\\x7F]+', '')= urs)
        |distribute by userId % 40
        |""".stripMargin

    val outputAll =
      s"""
         |insert overwrite table lofter.lofter_user_portrait_to_game_di partition(pt_d = '$dt')
         |select urs, phone, regexp_replace(device, '\\\\s+','') as device, userId, id_type, label, value, '$dtNum' as dt
         |from lofter.lofter_user_portrait_to_game_staging_attr_dd
         |where dt = '$dt' and (urs is null or regexp_replace(urs, '[^\\\\x00-\\\\x7F]+', '')= urs)
         |distribute by userId % 40
         |""".stripMargin

    spark.sql("SET hive.exec.compress.output=true;")
    spark.sql("SET mapred.output.compress=true;")
    spark.sql("SET mapred.output.compression.codec=org.apache.hadoop.io.compress.GzipCodec")

    if(pargs.boolean("full")) {
      spark.sql(outputAll)
    } else {
      spark.sql(outputIncr)
    }
  }
}
