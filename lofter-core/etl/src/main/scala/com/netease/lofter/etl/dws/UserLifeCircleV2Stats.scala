package com.netease.lofter.etl.dws

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}

object UserLifeCircleV2Stats {

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)

    val spark = SparkSession.builder()
      .appName("Lofter User Life Circle V2 Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))
    val day_15Ago = DateTime.parse(date).minusDays(14).toString("yyyy-MM-dd")
    val day_one_year_ago = DateTime.parse(date).minusDays(364).toString("yyyy-MM-dd")

    val sql_user_life_circle =
      s"""
         |select a.userid,create_date,last_login_date,
         |  nvl(post_cnt_1y,0) post_cnt_1y,
         |	nvl(discuss_cnt_1y,0) discuss_cnt_1y,
         |  nvl(send_comment_cnt_1y,0)  send_comment_cnt_1y,
         |	nvl(trade_num_1y,0) trade_num_1y,
         |	nvl(trade_money_1y,0) trade_money_1y,
         |	nvl(real_browse_pv_15d,0) real_browse_pv_15d,
         |	nvl(discuss_browse_15d,0) discuss_browse_15d, 
         |	nvl(post_cnt_15d,0) post_cnt_15d, 
         |	nvl(discuss_cnt_15d,0) discuss_cnt_15d, 
         |	nvl(send_comment_cnt_15d,0) send_comment_cnt_15d,
         |	nvl(trade_num_15d,0) trade_num_15d,
         |	nvl(trade_money_15d,0) trade_money_15d, 
         |	nvl(paid_content_real_browse_pv_15d,0) paid_content_real_browse_pv_15d,
         |	nvl(product_browse_pv_15d,0) product_browse_pv_15d, 
         |	nvl(card_browse_pv_15d,0) card_browse_pv_15d
         |from
         |(select id userid,createdate as create_date
         |from lofter.dim_user
         |where isanonymous=0 and istest=0  and isrobot=0 and createdate<='$date'
         |) a
         |left join
         |(select accountId as userid, from_unixtime(cast(time/1000 as bigint),'yyyy-MM-dd') as last_login_date  from lofter.dws_evt_login_user_last_dd where dt='$date') a1
         |on a.userid=a1.userid
         |left join
         |(
         |	select userid,sum(browse_pv) real_browse_pv_15d,sum(browse_question_count) discuss_browse_15d
         |	from lofter.dws_par_user_content_di
         |	where dt between '$day_15Ago' and  '$date'
         |	group by userid
         |) b  
         |on a.userid=b.userid
         |
         |left join
         |(
         |	select blogid,
         |	    count(case when contenttype in('图片','文字','视频') and publishdate between '$day_15Ago' and  '$date'  then id end) post_cnt_15d,
         |	    count(case when contenttype ='问答' and publishdate between '$day_15Ago' and  '$date' then id end) discuss_cnt_15d,
         |	    count(case when contenttype in('图片','文字','视频')  then id end) post_cnt_1y,
         |	    count(case when contenttype ='问答' then id end) discuss_cnt_1y
         |	from
         |      (select blogId,id,contenttype,movefrom,publishdate
         |       from lofter.dim_post
         |       where ispublished=true and isforbidden=false and iscitedpost=false
         |          and allowview=0
         |          and ((movefrom not in('blog','lofternetease','blog163like','loftmove','BLOGPOST','bbs','photo-pp','163_news','instagram_mirror','weibo_sync','news','pp','netease_photo' )
         |          and  movefrom not like '%blog%' and  movefrom not like '%move%') or movefrom is null)
         |          and  publishdate between '$day_one_year_ago' and  '$date'
         |          and contenttype in('图片','文字','视频','问答')
         |          and isActivityAutoPost = 0
         |          and isImported = 0
         |      ) t1
         |	group by blogid
         |) c 
         |on a.userid=c.blogid
         |
         |left join
         |(
         |	select userid,send_comment_cnt_15d,send_comment_cnt_1y
         |	from lofter.dws_par_user_interaction_dd
         |	where dt='$date'
         |)d  
         |on a.userid=d.userid
         |
         |left join
         |(
         |	select userid,
         |      sum(if(datediff('$date', pay_date) < 15, 1, 0)) trade_num_15d,
         |	    sum(if(datediff('$date', pay_date) < 15, money, 0)) trade_money_15d,
         |	    sum(if(datediff('$date', pay_date) < 365, 1, 0)) trade_num_1y,
         |	    sum(if(datediff('$date', pay_date) < 365, money, 0)) trade_money_1y
         |	from lofter.dwd_user_order_dd
         |	where dt='$date' and money > 0 and pay_date <= '$date'
         |	group by userid
         |) e  
         |on a.userid=e.userid
         |
         |left join
         |(
         |	select userid,sum(cnt) as paid_content_real_browse_pv_15d
         |	from
         |	(
         |		select userid,postid,count(1) cnt
         |		from lofter.dwd_post_browse_di
         |		where dt between '$day_15Ago' and  '$date' and is_real > 0
         |		group by userid,postid
         |	) t1   
         |	inner join
         |	(
         |		select postid
         |		from lofter.dim_gift_post_dd
         |		where dt = '$date' and is_pay_return_gift in ('5','7','6','4','3','2')
         |		group by postid
         |	) t2
         |	on t1.postid=t2.postid
         |	group by userid
         |) f   
         |on a.userid=f.userid
         |
         |left join
         |(
         |	select userid,count(1) as product_browse_pv_15d
         |	from lofter.dwd_evt_benefit_page_view_di
         |	where eventid='w3-2'
         |	and dt between '$day_15Ago' and  '$date'
         |	group by userid
         |) g  
         |on a.userid=g.userid
         |
         |left join
         |(
         |	select userid,count(1) card_browse_pv_15d
         |	from lofter.dwd_act_card_action_di
         |	where dt between '$day_15Ago' and  '$date'
         |	and eventid='card-0'
         |	group by userid
         |)h  
         |on a.userid=h.userid
         |""".stripMargin

    spark.sql(sql_user_life_circle)
      .withColumn("dt", lit(date))
      .repartition(10)
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter.dws_user_life_circle_index_dd")

    spark.stop()
  }
}
