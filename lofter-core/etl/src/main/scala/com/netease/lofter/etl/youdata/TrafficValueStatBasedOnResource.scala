package com.netease.lofter.etl.youdata

import com.github.nscala_time.time.Imports
import com.netease.wm.util.Args
import org.apache.spark.sql.{SaveMode, SparkSession}

object TrafficValueStatBasedOnResource {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Traffic value Stats Based On Different Resource")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(Imports.DateTime.yesterday.toString("yyyy-MM-dd"))

    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")

    val sql_mda =
      s"""
         |select dt,userId,eventId,scene,action,itemId,itemType,kafkaTime,occurTime,params['ad_source'] as ad_source
         |from lofter.ods_mda_app_partition_di
         |where dt='$date' and
         |     (
         |      (action in (0,200) and itemtype='PRODUCT')
         |      or eventid in ('a2-69','b1-45','b9-4','t3-2','l1-2','f1-46','h1-22','t2-5')
         |      or (eventid in ('ad-1','ad-9','ad-21','ad-23','ad-31','ad-53','ad-3','ad-5','ad-7','ad-25','ad-38','ad-43','ad-44') and params['ad_source'] <> 0 )
         |      or (eventid in  ('ad-2','ad-4','ad-8','ad-22','ad-24','ad-26','ad-37','ad-39','ad-45') and params['ad_source'] <> 0)
         |      )
         |""".stripMargin

    spark.sql(sql_mda).cache().createOrReplaceTempView("mdaTable")

    val sql_summary =
      s"""
         |select 'product' as resource_type,'all' as scene,a.exposure_pv,a.exposure_uv,transfrom_pv,transfrom_uv,sum_reward,a.dt
         |from
         |(select dt,count(userid) as exposure_pv,
         |     count(distinct userid ) as exposure_uv
         |from
         |     (select *
         |     from mdaTable
         |     where dt='$date' and action in (0,200) and itemtype='PRODUCT'
         |     ) t1
         |     inner join
         |     (
         |     select id,productname
         |     from lofter_db_dump.ods_db_benefit_product_info_nd
         |     where linkid is null
         |     and type=3
         |     ) t2
         |     on t1.itemid=t2.id
         |group by t1.dt
         |) a
         |left join
         |(
         |select from_unixtime(cast(createTime/1000 as bigint),'yyyy-MM-dd') dt,
         |    count(buyerid) transfrom_pv,count(distinct buyerid) transfrom_uv,sum(amount) as sum_reward
         |from lofter_db_dump.ods_db_benefit_trade_nd
         |where from_unixtime(cast(createtime/1000 as bigint),'yyyy-MM-dd')='$date' and status in (1,3,4) and producttype=3
         |group by 1
         |) b
         |on a.dt=b.dt
         |
         |union all
         |select 'gift' as resource_type,'all' as scene,a.exposure_pv,a.exposure_uv,transfrom_pv,transfrom_uv,sum_reward,a.dt
         |from
         |(select dt,count( t1.userid  ) as exposure_pv,
         |     count(distinct  t1.userid ) as exposure_uv
         |from
         |    (
         |    select *
         |    from mdaTable
         |    where dt =  '$date'
         |    and eventid in ('a2-69','b1-45','b9-4','t3-2','l1-2','f1-46','h1-22','t2-5')
         |    ) t1
         |    inner join
         |    (
         |    select id,userid
         |    from lofter.dim_post
         |    ) t2
         |    on t1.itemid=t2.id
         |    inner join
         |    (
         |    select userid,agreetime
         |    from lofter_db_dump.ods_db_trade_gift_account_nd
         |    where status=2
         |    group by userid,agreetime
         |    ) t3
         |    on  t2.userid=t3.userid and t1.occurTime>t3.agreetime
         |group by dt
         |) a
         |left join
         |(
         |select dt,count(sender) transfrom_pv,count(distinct sender) transfrom_uv,
         |    sum(coin)/10+sum(case when (createtime>=agreetime and coin=0) then count end)*0.028 as sum_reward
         |from
         |    (select blogid,from_unixtime(cast(createtime/ 1000 AS BIGINT), 'yyyy-MM-dd') dt,id,createtime,coin,count,sender
         |    from lofter_db_dump.ods_db_trade_gift_present_record_nd
         |    where from_unixtime(cast(createtime/ 1000 AS BIGINT), 'yyyy-MM-dd') = '$date'
         |    group by blogid,from_unixtime(cast(createtime/ 1000 AS BIGINT), 'yyyy-MM-dd') ,id,createtime,coin,count,sender
         |    ) t1
         |    inner join
         |    (
         |    select userid,agreetime
         |    from lofter_db_dump.ods_db_trade_gift_account_nd
         |    where status=2
         |    group by userid,agreetime
         |    ) t2
         |    on t1.blogid=t2.userid
         |group by dt
         |) b
         |on a.dt=b.dt
         |
         |union all
         |select 'reward' as resource_type,'all' as scene,a.exposure_pv,a.exposure_uv,transfrom_pv,transfrom_uv,sum_reward,a.dt
         |from
         |(select dt,count( t1.userid  ) as exposure_pv,
         |     count(distinct  t1.userid ) as exposure_uv
         |from
         |    (
         |    select *
         |    from mdaTable
         |    where dt =  '$date'
         |    and eventid in ('a2-69','b1-45','b9-4','t3-2','l1-2','f1-46','h1-22','t2-5')
         |    ) t1
         |    inner join
         |    (
         |    select id,userid
         |    from lofter.dim_post
         |    ) t2
         |    on t1.itemid=t2.id
         |    inner join
         |    (
         |    select userid,lastoptime
         |    from lofter_db_dump.ods_db_trade_reward_author_nd
         |    where status=1
         |    group by userid,lastoptime
         |    ) t3
         |    on  t2.userid=t3.userid and t1.occurTime>t3.lastoptime
         |group by dt
         |) a
         |left join
         |(
         |select dt,count(userid) as transfrom_pv,count(distinct userid) as transfrom_uv,sum(rewardamount) as sum_reward
         |from
         |    (
         |    select id,from_unixtime(cast(createtime as BIGINT),'yyyy-MM-dd')  dt,userid,rewardamount
         |    from lofter_db_dump.ods_db_trade_reward_order_nd
         |    where status=10
         |    and from_unixtime(cast(createtime AS BIGINT), 'yyyy-MM-dd') ='$date' and rewardamount>0
         |    group by id,from_unixtime(cast(createtime as BIGINT),'yyyy-MM-dd'),userid,rewardamount
         |    )
         |group by dt
         |) b
         |on a.dt=b.dt
         |
         |union all
         |select 'ad' as resource_type,'all' as scene,exposure_pv,exposure_uv,transfrom_pv,transfrom_uv,sum_reward,a.dt
         |from
         |(select dt,count(userid ) as exposure_pv,
         |     count(distinct userid ) as exposure_uv
         |from
         |    (
         |    select *
         |    from mdaTable
         |    where dt =  '$date'
         |    and eventid in ('ad-1','ad-9','ad-21','ad-23','ad-31','ad-53','ad-3','ad-5','ad-7','ad-25','ad-38','ad-43','ad-44')
         |    and ad_source <> 0
         |    )
         |group by dt
         |) a
         |left join
         |(
         |select t1.dt,exposure_kaiji_pv,exposure_banner_pv,exposure_else_pv,transfrom_pv,transfrom_uv,
         |    (exposure_kaiji_pv/1000)*10+(exposure_banner_pv/1000)*1+(exposure_else_pv/1000)*2 as sum_reward
         |from
         |(
         |select dt,count(case when eventid='ad-1' then userid end) as exposure_kaiji_pv,
         |    count(case when eventid in ('ad-9','ad-21','ad-23','ad-31','ad-53') then userid end) as exposure_banner_pv,
         |    count(case when eventid in ('ad-3','ad-5','ad-7','ad-25','ad-38','ad-43','ad-44') then userid end) as exposure_else_pv
         |from mdaTable
         |where dt =  '$date' and ad_source <> 0
         |      and eventid in ('ad-1','ad-9','ad-21','ad-23','ad-31','ad-53','ad-3','ad-5','ad-7','ad-25','ad-38','ad-43','ad-44')
         |group by dt
         |) t1
         |left join
         |(
         |select dt,count(userid) as transfrom_pv,count(distinct userid) as transfrom_uv
         |from mdaTable
         |where dt =  '$date' and ad_source <> 0
         |      and eventid in  ('ad-2','ad-4','ad-8','ad-22','ad-24','ad-26','ad-37','ad-39','ad-45')
         |group by dt
         |) t2
         |on t1.dt=t2.dt) b
         |on a.dt=b.dt
         |""".stripMargin

    val sql_scene =
      s"""
         |select 'product' as resource_type,nvl(a.scene,'else') as scene,a.exposure_pv,a.exposure_uv,transfrom_pv,transfrom_uv,sum_reward,a.dt
         |from
         |(select dt,scene,count(userid) as exposure_pv,
         |     count(distinct userid ) as exposure_uv
         |from
         |     (select *
         |     from mdaTable
         |     where dt='$date' and action in (0,200) and itemtype='PRODUCT'
         |     ) t1
         |     inner join
         |     (
         |     select id,productname
         |     from lofter_db_dump.ods_db_benefit_product_info_nd
         |     where linkid is null
         |     and type=3
         |     ) t2
         |     on t1.itemid=t2.id
         |group by t1.dt,scene
         |) a
         |left join
         |(
         |select t1.dt,scene,count(buyerid) transfrom_pv,count(distinct buyerid) transfrom_uv,sum(amount) as sum_reward
         |from
         |    (
         |    select dt,scene,userid
         |    from
         |    (select dt,scene,userid,row_number()over(partition by userid order by occurTime desc) as rk
         |    from mdaTable
         |    where dt =  '$date' and action in (0,200) and itemtype='PRODUCT'
         |    )
         |    where rk=1
         |    ) t1
         |    left join
         |    (select from_unixtime(cast(createTime/1000 as bigint),'yyyy-MM-dd') dt,buyerid,amount
         |    from lofter_db_dump.ods_db_benefit_trade_nd
         |    where from_unixtime(cast(createtime/1000 as bigint),'yyyy-MM-dd')='$date' and status in (1,3,4) and producttype=3
         |    )t2
         |    on t1.dt=t2.dt and t1.userid=t2.buyerid
         |group by t1.dt,scene
         |) b
         |on a.dt=b.dt and a.scene=b.scene
         |
         |union all
         |select 'gift' as resource_type,nvl(a.scene,'else') as scene,a.exposure_pv,a.exposure_uv,transfrom_pv,transfrom_uv,sum_reward,a.dt
         |from
         |(select dt,scene,count( t1.userid  ) as exposure_pv,
         |     count(distinct  t1.userid ) as exposure_uv
         |from
         |    (
         |    select *
         |    from mdaTable
         |    where dt =  '$date'
         |    and eventid in ('a2-69','b1-45','b9-4','t3-2','l1-2','f1-46','h1-22','t2-5')
         |    ) t1
         |    inner join
         |    (
         |    select id,userid
         |    from lofter.dim_post
         |    ) t2
         |    on t1.itemid=t2.id
         |    inner join
         |    (
         |    select userid,agreetime
         |    from lofter_db_dump.ods_db_trade_gift_account_nd
         |    where status=2
         |    group by userid,agreetime
         |    ) t3
         |    on  t2.userid=t3.userid and t1.occurTime>t3.agreetime
         |group by dt,scene
         |) a
         |left join
         |(
         |select t0.dt,scene,count(sender) transfrom_pv,count(distinct sender) transfrom_uv,
         |    sum(coin)/10+sum(case when (createtime>=agreetime and coin=0) then count end)*0.028 as sum_reward
         |from
         |    (select dt,scene,userid
         |    from
         |    (select dt,eventid,scene,userid,row_number()over(partition by userid order by occurTime desc) as rk
         |    from mdaTable
         |    where dt =  '$date'
         |    and eventid in ('a2-69','b1-45','b9-4','t3-2','l1-2','f1-46','h1-22','t2-5')
         |    )
         |    where rk=1
         |    ) t0
         |    left join
         |    (select blogid,from_unixtime(cast(createtime/ 1000 AS BIGINT), 'yyyy-MM-dd') dt,id,createtime,coin,count,sender
         |    from lofter_db_dump.ods_db_trade_gift_present_record_nd
         |    where from_unixtime(cast(createtime/ 1000 AS BIGINT), 'yyyy-MM-dd')  =  '$date'
         |    group by blogid,from_unixtime(cast(createtime/ 1000 AS BIGINT), 'yyyy-MM-dd') ,id,createtime,coin,count,sender
         |    ) t1
         |    on t0.dt=t1.dt and t0.userid=t1.sender
         |    inner join
         |    (
         |    select userid,agreetime
         |    from lofter_db_dump.ods_db_trade_gift_account_nd
         |    where status=2
         |    group by userid,agreetime
         |    ) t2
         |    on t1.blogid=t2.userid
         |group by t0.dt,scene
         |) b
         |on a.dt=b.dt and a.scene=b.scene
         |
         |union all
         |select 'reward' as resource_type,nvl(a.scene,'else') as scene,a.exposure_pv,a.exposure_uv,transfrom_pv,transfrom_uv,sum_reward,a.dt
         |from
         |(select dt,scene,count( t1.userid  ) as exposure_pv,
         |     count(distinct  t1.userid ) as exposure_uv
         |from
         |    (
         |    select *
         |    from mdaTable
         |    where dt =  '$date'
         |    and eventid in ('a2-69','b1-45','b9-4','t3-2','l1-2','f1-46','h1-22','t2-5')
         |    ) t1
         |    inner join
         |    (
         |    select id,userid
         |    from lofter.dim_post
         |    ) t2
         |    on t1.itemid=t2.id
         |    inner join
         |    (
         |    select userid,lastoptime
         |    from lofter_db_dump.ods_db_trade_reward_author_nd
         |    where status=1
         |    group by userid,lastoptime
         |    ) t3
         |    on  t2.userid=t3.userid and t1.occurTime>t3.lastoptime
         |group by dt,scene
         |) a
         |left join
         |(
         |select t1.dt,t1.scene,count(t2.userid) as transfrom_pv,count(distinct t2.userid) as transfrom_uv,sum(rewardamount) as sum_reward
         |from
         |    (select dt,scene,userid
         |    from
         |    (select dt,eventid,scene,userid,row_number()over(partition by userid order by occurTime desc) as rk
         |    from mdaTable
         |    where dt =  '$date'
         |    and eventid in ('a2-69','b1-45','b9-4','t3-2','l1-2','f1-46','h1-22','t2-5')
         |    )
         |    where rk=1
         |    ) t1
         |    left join
         |    (
         |    select id,from_unixtime(cast(createtime as BIGINT),'yyyy-MM-dd')  dt,userid,rewardamount
         |    from lofter_db_dump.ods_db_trade_reward_order_nd
         |    where status=10
         |    and from_unixtime(cast(createtime AS BIGINT), 'yyyy-MM-dd') ='$date'
         |    and rewardamount>0
         |    group by id,from_unixtime(cast(createtime as BIGINT),'yyyy-MM-dd'),userid,rewardamount
         |    ) t2
         |    on t1.dt=t2.dt and t1.userid=t2.userid
         |group by t1.dt,t1.scene
         |) b
         |on a.dt=b.dt and a.scene=b.scene
         |
         |union all
         |select 'ad' as resource_type,nvl(a.scene1,'else') as scene,exposure_pv,exposure_uv,transfrom_pv,transfrom_uv,sum_reward,a.dt
         |from
         |(select dt,scene1,count(userid ) as exposure_pv,
         |     count(distinct userid ) as exposure_uv
         |from
         |     (
         |     select *,
         |     case when eventid in ('ad-1') then 'openscreen'
         |     when eventid in ('ad-21') then 'domain'
         |     when  eventid in ('ad-23') then 'hotsearch'
         |     when eventid in ('ad-3') then 'attention'
         |     when eventid in ('ad-5','ad-7','ad-25','ad-38') then 'discovery'
         |     when  eventid in ('ad-43','ad-44') then 'video' end as scene1
         |     from mdaTable
         |     where dt = '$date' and ad_source <> 0
         |     and eventid in ('ad-1','ad-21','ad-23','ad-3','ad-5','ad-7','ad-25','ad-38','ad-43','ad-44')
         |     )
         |group by dt,scene1
         |) a
         |left join
         |(
         |select t1.dt,t1.scene1,exposure_kaiji_pv,exposure_banner_pv,exposure_else_pv,transfrom_pv,transfrom_uv,
         |      (exposure_kaiji_pv/1000)*10+(exposure_banner_pv/1000)*1+(exposure_else_pv/1000)*2 as sum_reward
         |from
         |    (
         |    select dt,
         |        case when eventid in ('ad-1') then 'openscreen'
         |        when eventid in ('ad-21') then 'domain'
         |        when  eventid in ('ad-23') then 'hotsearch'
         |        when eventid in ('ad-3') then 'attention'
         |        when eventid in ('ad-5','ad-7','ad-25','ad-38') then 'discovery'
         |        when  eventid in ('ad-43','ad-44') then 'video' end as scene1,
         |        count(case when eventid='ad-1' then userid end) as exposure_kaiji_pv,
         |        count(case when eventid in ('ad-21','ad-23') then userid end) as exposure_banner_pv,
         |        count(case when eventid in ('ad-3','ad-5','ad-7','ad-25','ad-38','ad-43','ad-44') then userid end) as exposure_else_pv
         |    from mdaTable
         |    where dt = '$date' and ad_source <> 0
         |          and eventid in ('ad-1','ad-21','ad-23','ad-3','ad-5','ad-7','ad-25','ad-38','ad-43','ad-44')
         |    group by dt,scene1
         |    ) t1
         |    left join
         |    (
         |    select dt,
         |        case when eventid in ('ad-2') then 'openscreen'
         |        when eventid in ('ad-22') then 'domain'
         |        when  eventid in ('ad-24') then 'hotsearch'
         |        when eventid in ('ad-4') then 'attention'
         |        when eventid in ('ad-37','ad-8','ad-26','ad-39') then 'discovery'
         |        when  eventid in ('ad-45') then 'video' end as scene1,
         |        count(userid) as transfrom_pv,count(distinct userid) as transfrom_uv
         |    from mdaTable
         |    where dt = '$date'  and ad_source <> 0
         |          and eventid in  ('ad-2','ad-4','ad-8','ad-22','ad-24','ad-26','ad-37','ad-39','ad-45')
         |    group by dt,scene1
         |    ) t2
         |    on t1.dt=t2.dt and t1.scene1=t2.scene1
         |) b
         |on a.dt=b.dt and a.scene1=b.scene1
         |""".stripMargin

    spark.sql(sql_summary).union(spark.sql(sql_scene))
      .repartition(1)
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_resource_traffic_value_di")

    spark.close()
  }

}
