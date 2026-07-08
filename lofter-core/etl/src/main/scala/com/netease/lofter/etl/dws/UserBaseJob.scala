package com.netease.lofter.etl.dws

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.lit

object UserBaseJob {

  val accountTypeMap: Map[String, String] = Map(
    "1" -> "新浪",
    "2" -> "网易微博",
    "3" -> "QQ",
    "4" -> "豆瓣",
    "5" -> "人人",
    "6" -> "TQQ",
    "7" -> "TWITTER",
    "8" -> "FACEBOOK",
    "9" -> "GOOGLE",
    "10" -> "FLICKR",
    "11" -> "5OOPX",
    "12" -> "微信",
    "13" -> "URS",
    "14" -> "手机号",
    "15" -> "APPLE",
    "16" -> "NETEASE_RICH_MEDIA",
    "17" -> "INDEPENDENT", // 子博客独立账号登录
    "100" -> "匿名").withDefault(_ => "邮箱")

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val dt = pargs.required("date")
    val twoWeeksAgo = DateTime.parse(dt).minusWeeks(2).toString("yyyy-MM-dd")
    val monthAgo = DateTime.parse(dt).minusDays(30).toString("yyyy-MM-dd")

    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", value = true)
      .appName("User Login Info Extract")
      .getOrCreate()

    spark.udf.register("parse_account_from_email", (mail: String) => {
      if (mail == null) {
        null
      } else if (mail.contains("#")) {
        val parts = mail.split("#")
        if (parts.length >= 2) parts.apply(1) else null
      } else if (mail.contains("@")) {
        mail
      } else null
    })

    spark.udf.register("parse_account_type_from_email", (mail: String) => {
      if(mail == null) {
        null
      } else if (mail.contains("#")) {
        accountTypeMap(mail.split("#").apply(0))
      } else if (mail.contains("@")) {
        "邮箱"
      } else null
    })

    spark.udf.register("parse_site_source", (siteType: String) => accountTypeMap(siteType))

    spark.udf.register("parse_from_platform", (profile: String) => {
      profile match {
        case _ if profile == null => "pc"
        case _ if profile.startsWith("web") => "web"
        case _ if profile.contains("android") => "android"
        case _ if profile.contains("iphone") => "iphone"
        case _ if profile.contains("mobile") => "mobile"
        case _ if profile.contains("ipad") => "ipad"
        case _ if profile.contains("weibo") => "weibo"
        case _ if profile.startsWith("blog163_activity") => "blog163_activity"
        case _ if profile.startsWith("blog_post_sync") => "blog_post_sync"
        case _ if profile.startsWith("blogmove2018") => "blogmove2018"
        case _ if profile.startsWith("sub_blog") => "sub_blog"
        case _ if profile.startsWith("weixinmp") => "weixinmp"
        case _ if profile.startsWith("instagram_mirror") => "instagram_mirror"
        case _ if profile.startsWith("RichMedia") => "RichMedia"
        case _ => profile
      }
    })

    spark.sql("create temporary function resolve_ip as 'com.netease.wm.udf.ResolveIp'")

    val connectSites =
      s"""
         |select userId, collect_set(site) as connect_sites
         |from (
         |  select UserId,named_struct('SiteType', SiteType, 'CreateTime', CreateTime, 'SiteUserId', SiteUserId, 'SiteSource',SiteSource) as site
         |  from (
         |      select userId, siteType, createTime, siteUserId,
         |             parse_site_source(siteType) as siteSource
         |    from lofter_db_dump.ods_db_connect_login_nd
         |   ) a
         |) b
         |group by UserId
       """.stripMargin

    val blogFans =
      s"""
         |select b.userid,
         |       sum(blogFans) all_blog_fans,
         |       sum(if(b.userId = b.blogId, a.blogFans, 0)) as main_blog_fans
         |from (
         |  select blogid, count(distinct userid) blogFans
         |  from lofter_db_dump.ods_db_user_following_nd
         |  group by blogid
         |) a join (
         |  select distinct userid, blogid
         |  from lofter_db_dump.ods_db_user_blog_account_nd
         |) b on a.blogid=b.blogid
         |group by b.userid
       """.stripMargin

    spark.sql(connectSites).createOrReplaceTempView("user_connect_sites")
    spark.sql(blogFans).createOrReplaceTempView("user_blog_fans")

    val userBase =
      s"""
         |select u.id as userId, u.mainBlogId as blogId,
         |       b.blogName, b.blogNickName, u.createTime,
         |       u.from_platform, u.accountType, u.account,
         |       f.name, f.idNumber,
         |       c.edu_degree, c.profession, nvl(c.city_reside,z6.city) as city_reside, c.phone_price_180d,
         |       nvl(case c.gender when '男' then 1 when '女' then 2 else null end, n.gender) as gender,
         |       c.birth_year, nvl(e.isPushOn, 0) as is_push_on,
         |       g.totalRewardGiftAmount as total_reward_gift_amount,
         |       h.phones, h.last_phone, h.last_phone_create_time,
         |       j.tagNames as subscribe_tags, k.collectionNames as subscribe_collections,
         |       if(size(m.imeis) > 0, m.imeis, null) as imeis,
         |       if(size(m.idfas) > 0, m.idfas, null) as idfas,
         |       if(size(m.deviceudids) > 0, m.deviceudids, null) as deviceudids,
         |       x.time as last_login_time, x.clientType as last_login_platform, x.deviceUdid as last_login_deviceid,
         |       x.appVersion as last_login_app_version, b.authDomainIds as auth_domain_ids, b.authDomainNames as auth_domain_names,
         |       nvl(y.openReward, 0) as is_open_reward, z.connect_sites, z1.all_blog_fans, z1.main_blog_fans,
         |       z2.follow_blogs, z3.emails, z4.blogs,
         |       z5.post_count_std, z5.post_count_since_2020,
         |       z6.lastLoginIp as last_login_ip,z6.country,z6.province,z6.city,
         |       case when r1.userId is not null then 0
         |            when r2.userId is not null then 1
         |            when r3.userId is not null then 2
         |            else 3 end as privilegeLevel,
         |        z7.registerIp as register_ip,z7.country as register_country,z7.province as register_province,z7.city as register_city,
         |        z8.blog_status,
         |        z9.subscribe_grains,
         |        nvl(z10.login_days_30d, 0) as login_days_30d,
         |        nvl(h.is_black_phone,0) as is_black_phone
         |from (
         |   select userId as id, mainBlogID, profileCreateTime as createTime,
         |          parse_from_platform(profileCreateFrom) as from_platform,
         |          parse_account_type_from_email(email) as accountType,
         |          parse_account_from_email(email) as account
         |   from lofter_db_dump.ods_db_profile_nd
         |) u
         |  join (select id from lofter.dim_user where isanonymous = 0) du on u.id = du.id
         |  left join lofter.dim_blog b on u.id = b.id
         |  left join (
         |       select cast(a.user_id as bigint) as userid,
         |              first_value(if(pt_tag='edu_degree', tag_value, null), true) edu_degree,
         |              first_value(if(pt_tag='profession', tag_value, null), true) profession,
         |              first_value(if(pt_tag='city_reside', tag_value, null), true) city_reside,
         |              first_value(if(pt_tag='phone_price_180d', tag_value, null), true) phone_price_180d,
         |              first_value(if(pt_tag='birth_y', tag_value, null), true) birth_year,
         |              first_value(if(pt_tag='gender', tag_value, null), true) gender
         |       from lofter.dwb_par_lofter_tag_wd a
         |       where pt_d>='$twoWeeksAgo' and cast(a.user_id as bigint) > 0 and
         |             pt_tag in ('edu_degree','profession', 'city_reside', 'phone_price_180d','birth_y','gender')
         |       group by cast(a.user_id as bigint)
         |  ) c on u.id = c.userid
         |  left join (
         |      select userId, is_push_on as isPushON
         |      from lofter.dws_par_user_push_dd
         |      where dt = '$dt' and is_push_on = 1
         |  ) e on u.id = e.userId
         |  left join (
         |       select userid, name, idNumber
         |       from (
         |          select userid,name,idNumber,row_number() over (partition by userid order by createTime desc) rk
         |          from (
         |              select userid,name,idNumber,createTime
         |              from lofter_db_dump.ods_db_trade_user_bind_nd
         |              where status=1
         |            union all
         |              select userid,name,idNo as idNumber,createTime
         |              from lofter_db_dump.ods_db_act_reward_user_pay_info_nd
         |          ) t0
         |       ) t1
         |       where t1.rk=1
         |  ) f on u.id = f.userid
         |  left join (
         |       select blogId, sum(amount) as totalRewardGiftAmount
         |       from (
         |              select blogid, coin * 0.1 as amount
         |              from lofter_db_dump.ods_db_trade_gift_present_record_nd
         |
         |              union all
         |
         |              select authorblogid as blogid, rewardamount as amount
         |              from lofter_db_dump.ods_db_trade_reward_order_nd
         |              where status=10
         |       ) t
         |       group by blogId
         |  ) g on u.id = g.blogId
         |  left join(
         |       select userId, collect_set(phone) as phones,max(is_black_phone) as is_black_phone,
         |              first_value(if(timeRank = 1, phone, null), true) as last_phone,
         |              first_value(if(timeRank = 1, createTime, null), true) as last_phone_create_time
         |       from (
         |         select userId, tt.phone, createTime, if(tt2.phone is null, 0, 1) as is_black_phone,
         |                row_number() over (partition by userId order by createTime desc) timeRank
         |         from (
         |               select userId, phone, createTime
         |               from lofter_db_dump.ods_db_connect_phone_account_nd
         |
         |               union all
         |               select userid, verifyPhone as phone, updateTime as createTime
         |               from lofter_db_dump.ods_db_verify_phone_account_nd
         |
         |               union all
         |               select userid, split(email,'#')[1] as phone, profileCreateTime as createTime
         |               from lofter_db_dump.ods_db_profile_nd where email like '14#%'
         |
         |               union all
         |               select userid, tel as phone, createTime
         |               from lofter_db_dump.ods_db_act_reward_user_pay_info_nd
         |         ) tt
         |         left join
         |         (select phone from lofter_db_dump.ods_db_risk_black_phone_nd where status=0 ) tt2
         |         on tt.phone=tt2.phone
         |       ) t where userId > 0 and length(phone) > 0
         |       group by userId
         |  ) h on u.id = h.userId
         |  left join (
         |       select userid, collect_set(tagName) tagNames
         |       from lofter_db_dump.ods_db_favorite_tag_nd
         |       where length(tagName) > 0
         |       group by userid
         |  ) j on u.id = j.userId
         |  left join (
         |       select t1.userid, collect_set(t2.name) collectionNames
         |       from lofter_db_dump.ods_db_subscribe_collection_nd t1
         |            join lofter_db_dump.ods_db_post_collection_nd t2
         |            on t1.collectionId = t2.id
         |       group by t1.userid
         |  ) k on u.id = k.userId
         |  left join (
         |       select cast(sid as bigint) as userId,
         |              collect_set(if(tid_tp='imei', tid, null)) as imeis,
         |              collect_set(if(tid_tp='idfa', tid, null)) as idfas,
         |              collect_set(if(tid_tp='deviceudid', tid, null)) as deviceudids
         |       from lofter.dwd_device_mapping
         |       where sid_tp='userid' and tid_tp in ('imei', 'idfa', 'deviceudid') and
         |             cast(sid as bigint) > 0
         |       group by cast(sid as bigint)
         |  ) m on u.id = m.userId
         |  left join (
         |     select blogId, gendar as gender from lofter_db_dump.ods_db_blog_info_nd
         |  ) n on u.id = n.blogId
         |  left join (
         |     select * from lofter.dws_evt_login_user_last_dd where dt='$dt'
         |  ) x on u.id = x.accountId
         |  left join lofter_db_dump.ods_db_trade_reward_author_nd y on u.id = y.userId
         |  left join user_connect_sites z on u.id = z.userId
         |  left join user_blog_fans z1 on u.id = z1.userId
         |  left join (
         |     select userId, collect_set(blogId) as follow_blogs
         |     from lofter_db_dump.ods_db_user_following_nd
         |     group by userId
         |  ) z2 on u.id = z2.userId
         |  left join (
         |     select userId, collect_set(email) as emails
         |     from lofter_db_dump.ods_db_account_nd
         |     where email not like '%#%' and length(email) > 0
         |     group by userId
         |  ) z3 on u.id = z3.userId
         |  left join (
         |    select userId, collect_set(blogId) as blogs
         |    from lofter_db_dump.ods_db_user_blog_account_nd
         |    group by userId
         |  ) z4 on u.id = z4.userId
         |  left join (
         |     select userId,
         |            count(1) as post_count_std,
         |            sum(if(publishDate >= '2020-01-01', 1, 0)) as post_count_since_2020
         |     from lofter.dim_post
         |     where isPublished = true
         |     group by userId
         |  ) z5 on u.id = z5.userId
         |left join
         |(select userId,lastLoginIp,inline(Array(resolve_ip(lastLoginIp))) as (country, province, city)
         | from lofter_db_dump.ods_db_user_statistic_nd
         | where lastlogintime>0 and lastLoginIp is not null
         | ) z6 on u.id=z6.userId
         |
         | left join
         |(select userId,ip as registerIp,inline(Array(resolve_ip(ip))) as (country, province, city)
         | from lofter_db_dump.ods_db_audit_log_nd where opType=6 and ip is not null
         | ) z7 on u.id=z7.userId
         |
         |left join
         |(select blogId as userId from lofter_db_dump.ods_db_risk_antispam_white_user_nd where status=1 group by blogid) r1
         |on u.id=r1.userId
         |left join
         |(select blogId as userId from lofter_db_dump.ods_db_verify_blog_nd group by blogId) r2
         |on u.id=r2.userId
         |left join
         |(select blogId as userId from lofter_db_dump.ods_db_authenticate_blog_nd group by blogId ) r3
         |on u.id=r3.userId
         |
         |left join
         |(select blogId,
         |    case when securityrank=9999 then '删除'
         |         when securityrank=9998 then '封禁'
         |         else '正常'end as blog_status
         | from   lofter_db_dump.ods_db_blog_settings_nd
         | group by 1,2
         |) z8 on u.id = z8.blogId
         |left join (
         |  select userId, count(distinct grainId) as subscribe_grains
         |  from lofter_db_dump.ods_db_grain_follower_nd
         |  where status = 0 and from_unixtime(cast(createTime/1000 as bigint), 'yyyy-MM-dd') <= '$dt'
         |  group by userId
         |) z9 on u.id = z9.userId
         |left join (
         |  select accountId as userId, count(distinct dt) as login_days_30d
         |  from (
         |      select accountId, dt
         |      from lofter.dwd_evt_user_login_di
         |      where dt <= '$dt' and dt > '$monthAgo' and accountId > 0
         |      group by 1, 2
         |  ) t
         |  group by 1
         |) z10 on u.id = z10.userId
       """.stripMargin

    spark.sql(userBase)
      .withColumn("dt", lit(dt))
      .repartition(200)
      .write.mode("overwrite")
      .insertInto("lofter.dws_par_user_base_dd")

    spark.close()
  }
}
