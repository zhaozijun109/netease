package com.netease.lofter.etl.ads

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.lit

object QuestionSquareHotListStats {
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Lofter Question Square Hot Search List Stats")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val date = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))
    val last6Day = DateTime.parse(date).minusDays(6).toString("yyyy-MM-dd")
    val last1Day = DateTime.parse(date).minusDays(1).toString("yyyy-MM-dd")
    val last29Day = DateTime.parse(date).minusDays(29).toString("yyyy-MM-dd")

    val sql_cmb_tag_filter =
      s"""
         |select name, ipids, derivedflag, status
         |from lofter_db_dump.ods_db_cmb_tag_nd
         |where  name not in
         |(select tag from lofter.zq_lofter_searchbangdan_black_post_tag)
         |""".stripMargin

    val sql_tag_source =
      s"""
         |select  a.name , 'IP'  as source
         |from 
         |(select name, ipid, derivedflag
         |from cmbTag
         |LATERAL VIEW explode(split(ipids, ';')) t2  as ipid
         |where status=0 )a
         |join
         |(select id,name as ipname,derivedflag from lofter_db_dump.ods_db_cmb_ip_nd ) b
         |on a.ipid=b.id
         |where b.derivedflag=1 
         |group by a.name , 'IP'
         |
         |union all
         |select name, '衍生'  as source
         |from cmbTag
         |where ipids is null and status=0 and derivedflag=1
         |group by name , '衍生'
         |
         |union all
         |
         |select a.name , '原创' as source
         |from cmbTag a
         |left join
         |(select  a.name
         |from 
         |    (select name, ipid,derivedflag
         |    from cmbTag
         |    LATERAL VIEW explode(split(ipids, ';')) t2  as ipid
         |    where status=0 ) a
         |    join
         |    (select id,name as ipname,derivedflag from lofter_db_dump.ods_db_cmb_ip_nd ) b
         |    on a.ipid=b.id
         |    where b.derivedflag=1
         |    group by a.name
         |    union
         |    select name from cmbTag
         |    where ipids is null and status=0 and derivedflag=1
         |    group by name
         |) b
         |on a.name=b.name
         |where b.name is null
         |group by a.name , '原创'
         |""".stripMargin

    spark.sql(sql_cmb_tag_filter).createOrReplaceTempView("cmbTag")
    spark.sql(sql_tag_source).cache().createOrReplaceTempView("tagSource")

    val sql_question_source =
      s"""
         |with question_source as (
         |    select t1.questionid,
         |           nvl(2d_answerCount,0) + nvl(2d_commentCount,0) as 2d_discussCount,
         |           nvl(7d_answerCount,0) + nvl(7d_commentCount,0) as 7d_discussCount,
         |           nvl(30d_answerCount,0) + nvl(30d_commentCount,0) as 30d_discussCount
         |    from (
         |        select questionid,
         |            sum(case when from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') between '$last1Day' and '$date' then 1 else 0 end) as 2d_answerCount,
         |            sum(case when from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') between '$last6Day' and '$date' then 1 else 0 end) as 7d_answerCount,
         |            count(1) as 30d_answerCount
         |        from lofter_db_dump.ods_db_ask_answer_post_nd
         |        where questionid <> 0 and from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') between '$last29Day' and '$date'
         |        group by questionId
         |    ) t1
         |    left join (
         |        select questionId,
         |            sum(case when from_unixtime(cast(publishtime / 1000 AS BIGINT), 'yyyy-MM-dd') between '$last1Day' and '$date' then 1 else 0 end) as 2d_commentCount,
         |            sum(case when from_unixtime(cast(publishtime / 1000 AS BIGINT), 'yyyy-MM-dd') between '$last6Day' and '$date' then 1 else 0 end) as 7d_commentCount,
         |            count(1) as 30d_commentCount
         |        from (
         |            select questionid,get_json_object(ext,'$$.commentId') as commentid,get_json_object(ext,'$$.postId') as postId,get_json_object(ext,'$$.blogId') as blogId from lofter_db_dump.ods_db_ask_answer_post_nd
         |            where questionid <> 0
         |        ) a
         |        join (
         |            select id as commentId, postId, blogId, publishtime from lofter_db_dump.ods_db_post_response_nd where from_unixtime(cast(publishtime / 1000 AS BIGINT), 'yyyy-MM-dd') between '$last29Day' and '$date'
         |        ) b
         |        on a.commentid = b.commentid and a.postId = b.postId and a.blogId = b.blogId
         |        group by questionid
         |    ) t2
         |    on t1.questionid = t2.questionid
         |),
         |score_question_source as (
         |    select parentquestionid as questionid,
         |        sum(case when from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') between '$last1Day' and '$date' then 1 else 0 end) as 2d_scoreCount,
         |        sum(case when from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') between '$last6Day' and '$date' then 1 else 0 end) as 7d_scoreCount,
         |        count(1) as 30d_scoreCount
         |    from (
         |        select parentquestionid, id as subquestionid
         |        from lofter_db_dump.ods_db_ask_question_nd
         |        where parentquestionid <> 0 and cosplay = 3
         |    ) a
         |    join (
         |        select questionid,userId,createtime from lofter_db_dump.ods_db_ask_user_item_score_nd
         |        where from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') between '$last29Day' and '$date'
         |    ) b
         |    on a.subquestionid = b.questionid
         |    group by parentquestionid
         |)
         |select *
         |from (
         |    select t1.*,
         |        row_number() over(partition by source order by t2.2d_discusscount desc) as rn
         |    from (
         |        select distinct a.id,a.userid,a.question,a.answercount,a.discusscount,a.questioner,a.type,images,tags,nvl(b.source,'原创') as source
         |        from (
         |            select id,userid,question,questioner,type,answercount,discusscount,images,tags,tag
         |            from lofter_db_dump.ods_db_ask_question_nd
         |            LATERAL VIEW explode(split(nvl(tags,'空'), ',')) t2 AS tag
         |            where status = 1 and discusscount >= 10 and cosplay in (0,1) and type <> 0 and from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') between '$last1Day' and '$date'
         |        ) a
         |        left join tagSource b
         |        on a.tag=b.name
         |    ) t1
         |    join (
         |        select * from question_source
         |    ) t2
         |    on t1.id=t2.questionid
         |) t3
         |where rn <= 5
         |union all
         |select *
         |from (
         |    select t1.*,
         |        row_number() over(partition by source order by t2.2d_scoreCount desc) as rn
         |    from (
         |        select distinct a.id,a.userid,a.question,a.answercount,a.discusscount,a.questioner,a.type,images,tags,nvl(b.source,'原创') as source
         |        from (
         |            select id,userid,question,questioner,type,answercount,discusscount,images,tags,tag
         |            from lofter_db_dump.ods_db_ask_question_nd
         |            LATERAL VIEW explode(split(nvl(tags,'空'), ',')) t2 AS tag
         |            where status = 1 and cosplay = 2 and from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') between '$last1Day' and '$date'
         |        ) a
         |        left join tagSource b
         |        on a.tag=b.name
         |    ) t1
         |    join (
         |        select * from score_question_source where 2d_scoreCount >= 10
         |    ) t2
         |    on t1.id=t2.questionid
         |) t3
         |where rn <= 5
         |union all
         |select *
         |from (
         |    select t1.*,
         |        row_number() over(partition by source order by t2.7d_discusscount desc) as rn
         |    from (
         |        select distinct a.id,a.userid,a.question,a.answercount,a.discusscount,a.questioner,a.type,images,tags,nvl(b.source,'原创') as source
         |        from (
         |            select id,userid,question,questioner,type,answercount,discusscount,images,tags,tag
         |            from lofter_db_dump.ods_db_ask_question_nd
         |            LATERAL VIEW explode(split(nvl(tags,'空'), ',')) t2 AS tag
         |            where status = 1 and discusscount >= 10 and cosplay in (0,1) and type <> 0 and from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') between '$last6Day' and '$date'
         |        ) a
         |        left join tagSource b
         |        on a.tag=b.name
         |    ) t1
         |    join (
         |        select * from question_source
         |    ) t2
         |    on t1.id=t2.questionid
         |) t3
         |where rn <= 10
         |union all
         |select *
         |from (
         |    select t1.*,
         |        row_number() over(partition by source order by t2.7d_scoreCount desc) as rn
         |    from (
         |        select distinct a.id,a.userid,a.question,a.answercount,a.discusscount,a.questioner,a.type,images,tags,nvl(b.source,'原创') as source
         |        from (
         |            select id,userid,question,questioner,type,answercount,discusscount,images,tags,tag
         |            from lofter_db_dump.ods_db_ask_question_nd
         |            LATERAL VIEW explode(split(nvl(tags,'空'), ',')) t2 AS tag
         |            where status = 1 and cosplay = 2 and from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') between '$last6Day' and '$date'
         |        ) a
         |        left join tagSource b
         |        on a.tag=b.name
         |    ) t1
         |    join (
         |        select * from score_question_source where 7d_scoreCount >= 10
         |    ) t2
         |    on t1.id=t2.questionid
         |) t3
         |where rn <= 10
         |union all
         |select *
         |from (
         |    select t1.*,
         |        row_number() over(partition by source order by t2.30d_discusscount desc) as rn
         |    from (
         |        select distinct a.id,a.userid,a.question,a.answercount,a.discusscount,a.questioner,a.type,images,tags,nvl(b.source,'原创') as source
         |        from (
         |            select id,userid,question,questioner,type,answercount,discusscount,images,tags,tag
         |            from lofter_db_dump.ods_db_ask_question_nd
         |            LATERAL VIEW explode(split(nvl(tags,'空'), ',')) t2 AS tag
         |            where status = 1 and discusscount >= 10 and cosplay in (0,1) and type <> 0
         |        ) a
         |        left join tagSource b
         |        on a.tag=b.name
         |    ) t1
         |    join (
         |        select * from question_source
         |    ) t2
         |    on t1.id=t2.questionid
         |) t3
         |where rn <= 10
         |union all
         |select *
         |from (
         |    select t1.*,
         |        row_number() over(partition by source order by t2.30d_scoreCount desc) as rn
         |    from (
         |        select distinct a.id,a.userid,a.question,a.answercount,a.discusscount,a.questioner,a.type,images,tags,nvl(b.source,'原创') as source
         |        from (
         |            select id,userid,question,questioner,type,answercount,discusscount,images,tags,tag
         |            from lofter_db_dump.ods_db_ask_question_nd
         |            LATERAL VIEW explode(split(nvl(tags,'空'), ',')) t2 AS tag
         |            where status = 1 and cosplay = 2
         |        ) a
         |        left join tagSource b
         |        on a.tag=b.name
         |    ) t1
         |    join (
         |        select * from score_question_source where 30d_scoreCount >= 10
         |    ) t2
         |    on t1.id=t2.questionid
         |) t3
         |where rn <= 10
         |""".stripMargin

    spark.sql(sql_question_source).cache().createOrReplaceTempView("questionSource")

    val sql_result_square =
      s"""
         |select f.id as topicId, question as topicName,userId,source as topicType,tags,images,discusscount as discuss_count,score,
         |    row_number()over(partition by source order by  score desc ,answercount desc , discusscount desc )  as rank
         |from (
         |    select e.*,row_number()over(partition by id order by  rk1 asc )  as rk2
         |    from (
         |       select d.*,row_number()over(partition by source order by  score  desc , answercount desc, discusscount desc )  as rk1
         |       from (
         |          select c.*,(ln(answercount)*0.4+ln(discusscount)*0.3+ln(nvl(dailyactivecount,1))*0.3) as score
         |          from (
         |            select distinct id,userid,question,answercount,discusscount,questioner,type,images,tags,source,dailyactivecount
         |            from (
         |                select * from questionSource
         |            ) a
         |            left join (
         |              select questionid,dailyactivecount from lofter_db_dump.ods_db_ask_question_statistic_nd
         |            ) b
         |            on a.id=b.questionid
         |          ) c
         |       ) d
         |    ) e
         |) f
         |where rk2=1
         |""".stripMargin

    val sql_result_tag =
      s"""
         |select tag,source as tagType,(answer_count+response_count) as discuss_num,question_count,
         |    row_number() over(partition by source order by (answer_count+response_count) desc) as rank
         |from
         |(select a.tag,a.source,
         |    count(distinct case when createDate between '$last6Day' and '$date' then a.id end) as question_count,
         |    count(distinct b.id) as answer_count,
         |    count(distinct c.id) as response_count
         |from
         |      (select distinct a.id,a.userid,a.question,a.tag,a.questioner,a.questioner,a.type,a.createDate,b.source
         |      from
         |          (select id,userid,question,tag,questioner,type,
         |              from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd') as createDate
         |          from lofter_db_dump.ods_db_ask_question_nd
         |          LATERAL VIEW explode(split(tags, ',')) t2 AS tag
         |          where type!=0 and status in (0,1) and auditstatus in (0,1)
         |          ) a
         |          join tagSource b
         |          on a.tag=b.name
         |      ) a
         |      left join
         |      (select id,blogid,createtime,questionid
         |      from lofter_db_dump.ods_db_ask_answer_post_nd
         |      where from_unixtime(cast(createtime / 1000 AS BIGINT), 'yyyy-MM-dd')  between '$last6Day' and '$date' ) b
         |      on a.id=b.questionid
         |
         |      left join
         |      (select a.questionid,b.id
         |       from
         |            (select id,blogid,createtime,questionid
         |            from lofter_db_dump.ods_db_ask_answer_post_nd) a
         |            join
         |            (select id,postid,publisheruserid,publishtime
         |            from lofter_db_dump.ods_db_post_response_nd
         |            where from_unixtime(cast(publishtime / 1000 AS BIGINT), 'yyyy-MM-dd') between '$last6Day' and '$date'
         |            ) b
         |            on b.postid=a.id
         |      ) c
         |      on a.id=c.questionid
         |group by a.tag,a.source
         |) aa
         |where (answer_count+response_count)>=50 and length(tag)>0
         |""".stripMargin

    spark.sql(sql_result_square)
      .repartition(1)
      .withColumn("dt", lit(date))
      .write
      .mode("overwrite")
      .insertInto("lofter_dm.ads_question_square_host_list_di")

    spark.sql(sql_result_tag)
      .repartition(1)
      .withColumn("dt", lit(date))
      .write
      .mode("overwrite")
      .insertInto("lofter_dm.ads_question_square_tag_host_list_di")


    spark.close()
  }

}
