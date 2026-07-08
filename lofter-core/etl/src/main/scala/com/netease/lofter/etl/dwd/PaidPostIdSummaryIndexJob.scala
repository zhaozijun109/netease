package com.netease.lofter.etl.dwd

import com.netease.wm.util.Args
import org.apache.spark.sql.{SaveMode, SparkSession}

/**
 * 开通付费用户的文章详情
 */
object PaidPostIdSummaryIndexJob {
  val OFFICIAL_USER = 1943463653
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Paid PostId Summary Index Job")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.required("date")

    val sql_post_detail =
      s"""
         |select a.userid,a.accept_gift_flag,a.agree_day,
         |    b.postid,b.blogname,b.blognickname,b.title,b.tags,b.url,b.contenttype,b.publishdate,
         |    d.return_gift_ids,
         |    case when e.postid is not null then 'cp' else 'nocp' end  as cp_type,
         |    case when f.blogid is not null and g.postid is null then '博客引入'
         |         when f.blogid is  null and g.postid is not null  then '单文签约'
         |         when  f.blogid is  null and g.postid is null   then '图文UGC'
         |         else '其他' end as platform_type,
         |    nvl(i.is_pay_return_gift,'0') as is_pay_return_gift
         |from (
         |    select userid,accept_gift_flag,min(a.day) as agree_day
         |    from (
         |        select userid,acceptgiftflag as accept_gift_flag,from_unixtime(cast(agreetime / 1000 AS BIGINT), 'yyyy-MM-dd') as day
         |        from lofter_db_dump.ods_db_trade_gift_account_nd
         |        where status=2
         |      union
         |        select userid,acceptgiftflag as accept_gift_flag,from_unixtime(cast(agreetime / 1000 AS BIGINT), 'yyyy-MM-dd') as day
         |        from  lofter_db_dump.ods_db_trade_gift_pay_account_nd
         |        where  status=2
         |    ) a
         |    group by userid,accept_gift_flag
         |) a
         |join (
         |    select id as postid,blogid,blogname,blognickname,title,tags,contenttype,publishdate,
         |           concat(blogName,'.lofter.com/post/',conv(blogId, 10, 16),'_',conv(id, 10, 16)) as url
         |    from lofter.dim_post
         |    where contenttype not in ('视频','音乐')
         |) b on a.userid=b.blogid
         |left join (
         |    select postid,
         |           concat_ws(';',collect_set(get_json_object(giftjson ,'$$.giftIds'))) as return_gift_ids
         |    from lofter_db_dump.ods_db_trade_return_gift_plan_nd
         |    where status=1 and auditstatus=1
         |    group by postid
         |) d on b.postid=d.postid
         |left join (
         |    select postid from lofter.dim_post_category_dd  where domain='同人CP' group by postid
         |) e on b.postid=e.postid
         |left join (
         |    select id as blogid from lofter.dim_user where sourceType='PGC'
         |) f on a.userid=f.blogid
         |left join (
         |    select postid
         |    from lofter_db_dump.ods_db_post_hot_nd
         |    where publisheruserid=$OFFICIAL_USER and type=1 group by postid
         |) g on b.postid=g.postid
         |left join (
         |    select postid,
         |      case when num1>0 and num2>0 and num3>0 then '7'
         |           when num1>0 and num2>0 and num3=0 then '6'
         |           when num1>0 and num2=0 and num3>0 then '5'
         |           when num1>0 and num2=0 and num3=0 then '4'
         |           when num1=0 and num2>0 and num3>0 then '3'
         |           when num1=0 and num2>0 and num3=0 then '2'
         |           when num1=0 and num2=0 and num3>0 then '1'
         |           end as is_pay_return_gift
         |    from (
         |        select postid,
         |               count(case when is_pay_return_gift='付费免费' then id else null end) as num1,
         |               count(case when is_pay_return_gift='仅付费' then id else null end ) as num2,
         |               count(case when is_pay_return_gift='仅免费' then id else null end ) as num3
         |        from (
         |            select a.postid, a.id,
         |              case when a.ispaynum>0 and a.nopaynum>0 then '付费免费'
         |                   when a.ispaynum>0 and a.nopaynum=0 then '仅付费'
         |                   when a.ispaynum=0 and a.nopaynum>0 then '仅免费'
         |                   end as is_pay_return_gift
         |            from (
         |                select a.postid,a.id,count(case when b.ispay=1 then b.giftid else null end) as ispaynum,
         |                       count(case when b.ispay=0 then b.giftid else null end ) as nopaynum
         |                from (
         |                    select id, postid,regexp_replace(giftId,'\\\\[|\\\\]', '') as giftid
         |                    from lofter_db_dump.ods_db_trade_return_gift_plan_nd
         |                         LATERAL VIEW explode(split(get_json_object(giftjson ,'$$.giftIds') ,',')) t2 AS giftId
         |                    where status=1 and auditstatus=1  group by id, postid,giftId
         |                ) a
         |                join (
         |                    select id as giftid, is_pay as ispay
         |                    from lofter.dim_gift
         |                ) b on a.giftid=b.giftid
         |                group by a.postid,a.id
         |            ) a
         |            group by 1,2,3
         |        ) a
         |        group by postid
         |    ) a
         |    group by 1,2
         |) i on b.postid=i.postid
       """.stripMargin

    spark.sql(sql_post_detail)
      .repartition(10)
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter.dwd_paid_gift_postId_info_nd")

    spark.close()
  }
}
