package com.netease.lofter.etl.youdata

import com.github.nscala_time.time.Imports.DateTime
import com.netease.lofter.etl.common.databases
import com.netease.wm.util.Args
import org.apache.spark.sql.SparkSession

import java.sql.Connection

object RecommendDisOrderMoneyStat {
  private val ONE_HOUR_MS = 3600000L
  private val FIVE_MINUTES_MS = -300000L
  private val batchSize = 100

  def main(args: Array[String]): Unit = {

    val pargs = Args(args)
    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val yesterday = DateTime.yesterday.toString("yyyy-MM-dd")
    val date = pargs.optional("date").getOrElse(yesterday)

    recommendDisMoneyStat(spark, date)
    spark.close()

  }

  def recommendDisMoneyStat(spark: SparkSession, date: String): Unit = {

    // get the click behavior from mda
    val sql_click =
      s"""
         |select *
         |from (
         |    select userId,kafkaTime,itemType,scene,
         |           get_json_object(algInfo, '$$.taskId') taskId,itemId as productId
         |    from lofter.ods_mda_app_partition_di
         |    where dt = '$date' and actionType = 'cell_click' and eventId in ('b1-46', 'g1-41')
         |) a
         |where taskId is not null and itemType='PRODUCT' and productId>0 and scene in ('discovery','note')
       """.stripMargin

    spark.sql(sql_click).createOrReplaceTempView("t1")

    val sql_order =
      s"""
        |select buyerId as userId,productId,status,(storePrice * productnum - newCouponPreferential) AS amount,createTime
        |from lofter_db_dump.ods_db_benefit_order_product_nd where status in (1,3,4) and from_unixtime(cast(createTime/1000 as bigint),'yyyy-MM-dd')='$date'
      """.stripMargin
    spark.sql(sql_order).createOrReplaceTempView("t2")

    // find the latest click behavior before the order based on the t1.userId=t2.userId and t1.productId=t2.productId
    // one orderId may correspond to many click behavior, so we should partition by order createTime to row_number to get the latest click behavior
    val sql_stat =
      s"""
         |select projectId,locationType,day,hour,cast(sum(amount)*100 as bigint) as money from
         |   ( select taskId as projectId,case when scene='discovery' then 0 else 1 end as locationType,
         |     cast(from_unixtime(cast(kafkaTime/1000 as bigint),'yyyyMMdd') as bigint) as day,
         |     cast(split(from_unixtime(cast(kafkaTime/1000 as bigint),'yyyyMMdd-HH'),'-')[1] as int) as hour,
         |     amount
         |     from
         |          (select *,row_number() over (partition by createTime,userId,productId order by diffTime) as rk
         |          from
         |              (select t1.taskId,t1.scene,t1.userId,t1.productId,t2.amount,t1.kafkaTime,t2.createTime,t2.createTime-t1.kafkaTime diffTime
         |              from
         |                  t1 join t2 on t1.userId=t2.userId and t1.productId=t2.productId and
         |                  t2.createTime-t1.kafkaTime>$FIVE_MINUTES_MS and t2.createTime-t1.kafkaTime<$ONE_HOUR_MS
         |               ) a
         |        )b
         |     where rk=1
         |     ) c
         | group by projectId,locationType,day,hour
       """.stripMargin

    val result = spark.sql(sql_stat).filter("money>0").collect()
    result.take(20)

    import com.netease.lofter.etl.common.spark.SparkSqlImplicits._
    import com.netease.wm.util.Sql._
    implicit val db: Connection = databases.getRecFlowDDBConn

    // update the ip_tag guideAddSubCount overall data every day based on the ipName and tagName
    result.grouped(batchSize).foreach { row =>
      val parms = row.map(x => rowParam(x))
      sql"update Dispatch_ProjectStatHour set hotCount=${"money"} where projectId=${"projectId"} and locationType=${"locationType"} and day=${"day"} and hour=${"hour"}".batchUpdate(parms)
    }

    db.close()

  }

}
