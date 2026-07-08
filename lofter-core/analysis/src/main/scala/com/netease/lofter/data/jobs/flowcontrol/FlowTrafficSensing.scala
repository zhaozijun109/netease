package com.netease.lofter.data.jobs.flowcontrol

import com.github.nscala_time.time.Imports.DateTime
import com.netease.lofter.data.common.kafkaConfig
import com.netease.wm.util.Args
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{SaveMode, SparkSession}

/**
 *
 * 1. maintain a full fledged post dd table of flow task expose/hot data
 * 2. join flow task data change with push config
 * 3. when data threshold is exceeded, generate push data into kafka and hive table
 * push message structure:
 * simulateType: flow_traffic_sense
 * data: {"itemId": <itemId long>, "itemType": <itemType string>, "msg": "<msg template>", "msgType": <msgType int 1私信 2评论 >,  "blogId": <blogId long>}
 * 4. collect flow task push send data into es for cms query
 *
 *
 *
 */
object FlowTrafficSensing {

  case class FlowTaskPushConfig(flowTaskId: Long, flowTaskType: Int, itemId: String, itemType: String, blogId: Long,
                                postId: Long, msgType: Int, msg: String, exposure: Long, hot: Long)

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val date = pargs.required("date")
    val dayAgo = DateTime.parse(date).minusDays(1).toString("yyyy-MM-dd")

    val spark = SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val blackList = Seq(
      1982963750,1262878655,1284709851,838614547,1963070825,1994829606,1961051908,508934362,501768398,1264801067,
      822212695,538887494,509258523,1954426029,523292157,535449650,533618170,1264928174,824746143,2002882187,508934362,1975509693
    ).mkString(",")

    val pushes =
      s"""
         |select flowTaskId, flowTaskType, postId,
         |       itemId, itemType, blogId, msg, msgType
         |from (
         |   select a.flowTaskId, a.flowTaskType, a.postId,
         |          nvl(b.exposurePv,0) as exposureUv, nvl(b.postHotPv,0) as postHotPv,
         |          nvl(c.exposurePv, 0) as exposureUvPrev, nvl(c.postHotPv, 0) as postHotPvPrev,
         |          nvl(a.exposure, 0) as pushExposure, nvl(a.hot,0) as pushHot,
         |          a.itemId, a.itemType, a.blogId, a.msg, a.msgType
         |   from (
         |     select *, row_number() over (partition by flowTaskId, flowTaskType, itemId, itemType, msgType order by dt desc) as rk
         |     from lofter.ods_log_dstr_flow_post_push_config_di
         |   ) a
         |   left join (
         |     select postId, exposurePv, posHotPv as postHotPv from lofter.dws_post_base_stats_dd  where dt = '$date'
         |   ) b on a.postId = b.postId
         |   left join (
         |     select postId, exposurePv, posHotPv as postHotPv from lofter.dws_post_base_stats_dd  where dt = '$dayAgo'
         |   ) c on a.postId = c.postId
         |   where a.rk = 1
         |) t
         |where ((pushExposure > 0 and exposureUv >= pushExposure and exposureUvPrev < pushExposure) or
         |       (pushHot > 0 and postHotPv >= pushHot and postHotPvPrev < pushHot)) and
         |      blogId not in ($blackList)
         |""".stripMargin

    val stimulusTime = System.currentTimeMillis()

    val df = spark.sql(pushes)
      .repartition(1).cache()

    df.withColumn("dt", lit(date))
      .write.mode(SaveMode.Overwrite)
      .insertInto("lofter_dm.ads_rec_dis_post_push_di")

    df.withColumn("stimulusTime", lit(stimulusTime))
      .selectExpr("CAST(blogId as STRING) as key", """concat('{"userId":', blogId, ',"time":', stimulusTime, ',"stimulateType": "flow_traffic_sense", "data": {"itemId":', postId, ', "itemType":"', itemType, '","msg":"', msg, '", "msgType":', msgType, ', "flowTaskId":', flowTaskId, ',"flowTaskType":', flowTaskType, ',"blogId":', blogId, '}}') as value""")
      .write
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaConfig.BOOTSTRAP_SERVERS)
      .option("topic", "lofter.creator-stimulus-pm.staging")
      .save()

    spark.stop()
  }
}
