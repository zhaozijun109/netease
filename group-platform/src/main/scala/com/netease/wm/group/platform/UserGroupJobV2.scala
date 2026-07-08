package com.netease.wm.group.platform

import com.github.nscala_time.time.Imports.DateTime
import com.netease.wm.group.platform.ContentGroupJob.KafkaPush
import com.netease.wm.group.platform.GroupJobManager.{GroupJobResult, ReturnResult}
import com.netease.wm.group.platform.common.kafkaConfig
import com.netease.wm.util.Args
import org.apache.kafka.clients.consumer.{ConsumerRecords, KafkaConsumer}
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{concat_ws, lit}
import com.alibaba.fastjson.{JSON, JSONObject}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer

import java.sql.Connection
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.write

import java.time.format.DateTimeFormatter
import java.time.{Duration, LocalDateTime}
import java.util.{Collections, Properties}

object UserGroupJobV2 {
  case class RefreshNotify(packageId: Long)
  def main(args: Array[String]): Unit = {
    val pargs = Args(args)

    val dt = pargs.optional("dt").getOrElse(DateTime.yesterday().toString("yyyy-MM-dd"))
    val dayAgo = DateTime.parse(dt).minusDays(1).toString("yyyy-MM-dd")

    val userGroupQueryBuilder = GroupQueryBuilder(dt)
      .withTagResolve {
        case "tag_consume2" | "tag_consume3" => "tag_consume"
        case "登录活跃情况" => "消费者活跃情况"
        case v => v
      }
      .withTagValueResolve {
        case ("specialty_consume" | "specialty_publish", "搞笑沙雕") => "shadiao"
        case ("specialty_consume" | "specialty_publish", "虐文") => "nuewen"
        case ("specialty_consume" | "specialty_publish", "小甜饼") => "xiaotianbing"
        case ("specialty_consume" | "specialty_publish", "论坛体") => "luntanti"
        case ("内容类型" | "创作内容类型", "PHOTOPOST") => "图片"
        case ("内容类型" | "创作内容类型", "VIDEOPOST") => "视频"
        case ("内容类型" | "创作内容类型", "TEXTPOST") => "文字"
        case ("内容类型" | "创作内容类型", "QAPOST") => "问答"
        case ("内容类型" | "创作内容类型", "LONGTEXTPOST") => "长文章"
        case ("内容类型" | "创作内容类型", "CHAT") => "聊聊"
        case ("deviceType", "安卓") => "ANDROID"
        case ("deviceType", "IOS") => "IOS"
        case ("入池文章" | "累计粉丝" | "互动粉丝" | "涨粉情况" | "打赏收礼金额" | "tag_post" | "ip_post" | "product_type_pay" | "年龄" | "热度贡献量" | "蓝手量" | "点赞量" | "收藏量" | "评论量" | "送礼金额" | "打赏金额" | "发文频次", v) if v.matches("[\\dkKwW]+-[\\dkKwW]+") => "COUNT_" + v.replace("-", "_")
        case ("入池文章" | "累计粉丝" | "互动粉丝" | "涨粉情况" | "打赏收礼金额" | "tag_post" | "ip_post" | "product_type_pay" | "年龄" | "热度贡献量" | "蓝手量" | "点赞量" | "收藏量" | "评论量" | "送礼金额" | "打赏金额" | "发文频次", v) if v.contains("及以上") => "COUNT_" + v.replace("及以上", "")
        case ("tag_consume" | "ip_consume" | "category_consume" | "乐乎币充值"|"乐乎币消费"|"粮票赠送", v) if v.matches("[\\dkKwW]+-[\\dkKwW]+") => "COUNT_" + v.replace("-", "_").toLowerCase
        case ("tag_consume" | "ip_consume" | "category_consume" | "乐乎币充值"|"乐乎币消费"|"粮票赠送", v) if v.contains("及以上") => "COUNT_" + v.replace("及以上", "").toLowerCase()
        case ("活跃情况", "活跃") => "1"
        case ("活跃情况", "不活跃") => "0"
        case ("是否发文", "发文") => "1"
        case ("是否发文", "不发文") => "0"
        case ("购买行为", "有购买行为") => "1"
        case ("购买行为", "无购买行为") => "0"
        case ("加购行为", "有加购行为") => "1"
        case ("加购行为", "无加购行为") => "0"
        case ("消费广告", "消费过") => "1"
        case ("消费广告", "未消费") => "0"
        case ("消费者活跃情况" | "avg_login_active", "活跃") => "1"
        case ("消费者活跃情况" | "avg_login_active", "不活跃") => "0"
        case ("avg_user_is_author", "作者") => "1"
        case ("avg_user_is_author", "非作者") => "0"
        case ("涨粉情况-突破", "1W") => "10000"
        case ("涨粉情况-突破", "10W") => "100000"
        case ("生命周期", "新用户") => "LIFE_NEW"
        case ("生命周期", "成长期") => "LIFE_GROWING"
        case ("生命周期", "休眠期") => "LIFE_SLEEPING"
        case ("生命周期", "流失期") => "LIFE_LOSING"
        case ("生命周期", "回流用户") => "LIFE_BACK"
        case ("product_type_pay", "抽赏") => "card"
        case ("product_type_pay", "市集") => "market"
        case ("地域", v) if v.contains('-') => v.split("-").last

        // time range
        //case ("发文时间" | "最后活跃日期" | "vc_last_active_date", v) => v.replace("/", "-")
        case (_, "近1天") => "DAY_1"
        case (_, "近3天") => "DAY_3"
        case (_, "近7天") => "DAY_7"
        case (_, "近15天") => "DAY_15"
        case (_, "近30天") => "DAY_30"
        case (_, "近90天") => "DAY_90"
        case (_, "近180天") => "DAY_180"
        case (_, "近1年") => "DAY_365"
        case (_, "累计") => "DAY_ALL"

        // quantity
        case ("入池文章" | "累计粉丝" | "互动粉丝" | "涨粉情况" | "打赏收礼金额" | "tag_post" | "ip_post" | "product_type_pay" | "年龄" | "热度贡献量" | "蓝手量" | "点赞量" | "收藏量" | "评论量" | "送礼金额" | "打赏金额" | "发文频次", v) if v.matches("[\\dkKwW]+-[\\dkKwW]+") => "COUNT_" + v.replace("-", "_")
        case ("入池文章" | "累计粉丝" | "互动粉丝" | "涨粉情况" | "打赏收礼金额" | "tag_post" | "ip_post" | "product_type_pay" | "年龄" | "热度贡献量" | "蓝手量" | "点赞量" | "收藏量" | "评论量" | "送礼金额" | "打赏金额" | "发文频次", v) if v.contains("及以上") => "COUNT_" + v.replace("及以上", "")
        case ("tag_consume" | "ip_consume" | "category_consume" | "specialty_consume" | "specialty_publish", v) if v.matches("[\\dkKwW]+-[\\dkKwW]+") => "COUNT_" + v.replace("-", "_").toLowerCase
        case ("tag_consume" | "ip_consume" | "category_consume" | "specialty_consume" | "specialty_publish", v) if v.contains("及以上") => "COUNT_" + v.replace("及以上", "").toLowerCase()
        case ("付费文章曝光篇数" | "付费文章有效浏览篇数", v) if v.contains("及以上") => v.replace("及以上", "+")

        // 质量分：grp 直接使用 ETL 写入的区间字符串（0-150 / 150-200 / 200-300 / 300及以上），原样透传
        case ("质量分", v) => v

        case (_, v) => v
      }
      .withTagValueSetResolve {
        case "消费偏好" => Set("文字", "图片", "视频")
        case _ => Set.empty
      }
      .withTagValueRangeResolve {  //value为范围的
        case ("权益中心icon曝光天数" | "活跃天数" | "注册天数" | "付费金额" | "单人向偏好度" | "vc_registered_days" | "vc_last_active_days" | "avg_register_days" | "avg_user_charge" | "avg_user_pay" | "push_receive_cnt", _) => true
        case ("pve_user_top_up_cnt" | "pve_user_chats_cnt" | "pve_user_interview_cnt" | "pve_user_access_free_cp_cnt", _) => true
        case ("pve_user_role_amount" | "pve_user_role_chats" | "pve_user_last_chats" | "pve_user_trade_product", _) => true
        case ("vc_user_character_talk_days" | "vc_user_character_talk_cnt" | "vc_user_character_talk_intervals" | "vc_newold_users_talk_cnt" | "vc_user_consume_items" | "vc_uc_avg_talk_cnt" ,_) => true
        case ("vc_user_total_active_days" | "vc_user_consume_order_cnt" | "vc_user_character_type_talk_cnt" | "vc_user_consume_amount" | "vc_user_character_fetter" | "vc_user_cy_coin_consume", _) => true
        case ("chat_new_users" | "vc_lose_users" | "vc_user_energy_consume" | "pve_user_create_roles" | "avg_pay_type_money" | "vc_user_simulator_chats"  | "lofter_vc_user_simulator_chats" | "vc_user_simulator_energy_consume", _) => true
        case ("发文时间" | "最后活跃日期" | "vc_last_active_date" | "avg_last_login_date" | "avg_total_play_time" | "avg_user_pay_credit", _) => true
        case ("权益中心看广告" | "权益中心商品兑换行为" | "权益中心活跃天数", _) => true
        case _ => false
      }
      .withTagValueComplementResolve {
        case ("达人" | "同人创作者" | "签约创作者" | "发文频次" | "tag_post" | "ip_post" | "tag_consume" | "ip_consume" | "category_consume"  | "specialty_consume" | "specialty_publish" | "热度贡献量" | "蓝手量" | "点赞量" | "收藏量" | "评论量" | "送礼金额" | "打赏金额" | "coupon_exchange", "0") => true
        case ("外部引入"|"安全等级"|"累计粉丝"|"互动粉丝"|"消费广告"|"是否发文"|"消费者活跃情况"|"活跃情况"|"购买行为"|"涨粉情况"|"加购行为"|"热度"|"乐乎币充值"|"乐乎币消费"|"粮票赠送"|"激励视频获得粮票" | "push_receive_cnt", "0") => true
        case ("消费者活跃情况" | "权益中心活跃", "不活跃") => true
        case ("付费文章曝光篇数" | "付费文章有效浏览篇数" | "发文总有效pv" | "篇均有效pv" | "是否开过书城会员", "0") => true
        case ("性别", "未知") => true
        case ("付费金额" | "push_receive_cnt" | "权益中心看广告" | "权益中心商品兑换行为" | "pve_user_top_up_cnt", "0-0") => true
        case _ => false
      }
      .withTagGroupTableResolve { (tag, level, time) =>   //tag到表的映射关系
        (level, tag, time) match {
          case (_, "vc_login_type" | "vc_registered_times" | "vc_appversion" | "vc_is_notified" | "vc_rest_energy" | "vc_registered_days" | "vc_registered_not_talk"
                   | "vc_login_os" | "vc_last_active_days" | "vc_last_active_date" | "vc_all_registered_users" | "vc_interflow_users" | "vc_user_cy_coin_consume"
                   | "vc_character_talk_users" | "vc_all_registered_users_isnotanty" | "vc_user_simulator_churn" | "vc_30d_active_user" , _) => "vc.user_portrait_datas_bitmap"
          case (_, "vc_user_total_active_days" | "vc_lose_users" |  "vc_user_consume_items" | "vc_user_character_talk_days" | "vc_user_character_talk_cntvc_user_character_talk_cnt"
                   | "vc_user_character_fetter" | "vc_newold_users_talk_cnt" | "vc_user_consume_order_cnt" | "vc_user_energy_consume" | "vc_user_character_talk_intervals"
                   | "vc_user_consume_amount" | "vc_uc_avg_talk_cnt" | "vc_user_character_type_talk_cnt" | "vc_user_character_talk_cnt" | "vc_user_simulator_chats" | "vc_user_simulator_energy_consume", _) => "vc.user_portrait_datas"
          case (_,"avg_pay_type_money" | "avg_user_charge" | "avg_user_cycle_sign" | "avg_user_pay" | "avg_user_sign" | "avg_total_play_time" | "avg_user_pay_credit", _) => "avg.user_portrait_datas"
          case (_,"avg_last_login_date" | "avg_login_active" | "avg_register_days" | "avg_user_is_author" , _) => "avg.user_portrait_datas_bitmap"
          case (_, "pve_user_interview_cnt" | "pve_user_top_up_cnt" | "付费金额" | "单人向偏好度" | "pve_user_trade_product" | "pve_user_last_chats" | "pve_user_role_chats" |  "pve_user_chats_cnt" | "pve_user_access_free_cp_cnt" | "pve_user_create_roles" | "pve_user_role_amount" | "chat_new_users" | "push_receive_cnt"  | "lofter_vc_user_simulator_chats" , _) => "lofter.user_portrait_datas"
          case (_, "ip_consume" | "category_consume" | "tag_consume", "DAY_90" | "DAY_180") => "lofter.user_portrait_datas_bitmap_wd"
          case (_,_,_) => "lofter.user_portrait_datas_bitmap"
        }
      }
      .withTagRawResolve {  //是否是原始值处理
        case "pve_user_role_amount" | "pve_user_role_chats" | "pve_user_last_chats" | "pve_user_trade_product" | "pve_user_create_roles" |
             "vc_user_character_talk_days" | "vc_user_character_talk_cnt" | "vc_user_character_talk_intervals" |
             "vc_newold_users_talk_cnt" | "vc_user_consume_items" | "vc_lose_users" | "vc_user_energy_consume" |
             "vc_user_consume_amount" | "vc_user_character_fetter" | "vc_user_total_active_days" | "vc_user_consume_order_cnt" |
             "pve_user_top_up_cnt" | "pve_user_chats_cnt" | "pve_user_interview_cnt" | "pve_user_access_free_cp_cnt" |
             "付费金额" | "单人向偏好度" | "chat_new_users" | "vc_uc_avg_talk_cnt" | "avg_total_play_time" | "vc_user_character_type_talk_cnt" | "push_receive_cnt" |
             "avg_user_charge" | "avg_user_pay" | "avg_pay_type_money" | "avg_user_pay_credit" | "vc_user_simulator_chats" | "lofter_vc_user_simulator_chats" | "vc_user_simulator_energy_consume" => true
        case _ => false
      }

////    test 本地调试
//    val servers = "10.122.119.6:9092"
//    val jobStore = new LocalFileJobStateStore("/Users/xyy/IdeaProjects/group-platform/target/user_group", "/Users/xyy/IdeaProjects/group-platform/target/user_group_extra")
//    import com.netease.wm.group.platform.common.databases.getTestDDBConn
//    implicit val conn: Connection = getTestDDBConn

//    线上
    val servers = "lofter-kafka-dc1.gy.ntes:9092,lofter-kafka-dc2.gy.ntes:9092,lofter-kafka-dc3.gy.ntes:9092"
    val jobStore = new HdfsJobStateStore("/user/da_lofter/warehouse/user_group", "/user/da_lofter/warehouse/user_group_extra")
    import com.netease.wm.group.platform.common.databases.getDDBConn
    implicit val conn: Connection = getDDBConn

    val jobManager = new GroupJobManager(userGroupQueryBuilder, jobStore, jobType = 2)

    val spark = SparkSession.builder()
//      .master("local[*]")   // test 本地调试
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val flowtopic = "YQ.CMB.CROWD_FLOW"

    val consumer = getConsumer(servers)
    consumer.subscribe(Collections.singletonList(flowtopic))

    val producer = getProducer(servers)

    var errors: Int = 0
    import spark.implicits._

    val startTime = System.currentTimeMillis()

    while (System.currentTimeMillis() - startTime < 500 * 1000) {
      val records: ConsumerRecords[String, String] = consumer.poll(Duration.ofMillis(1000))
      val it = records.iterator()
      while (it.hasNext) {
        val record = it.next()
        println("----------------------------分割线-------------------------------")
        val dataJson = JSON.parseObject(record.value())
        val b_line = dataJson.getString("b_line")
        val tags = dataJson.getString("tag")
        val packageid = dataJson.getInteger("packageid")
        val dailyid = dataJson.getInteger("dailyid")
        val datadt = dataJson.getString("dt")
        val message_type = dataJson.getString("message_type")

        if(message_type.equals("crowd.start")){
          println(s"Kafka received message json: ${dataJson}")
          if(tags != null && packageid != null && datadt == dt){
            println(s"1.开始运行,packid=$packageid")
            errors += jobManager.runJobs(
              dt,
              completeNotify = { (jobResult: ReturnResult) =>
                println(s"4.执行回调函数：syncStatus：${jobResult.syncStatus.exists(_ > 0)},status:${jobResult.status}")
                if(jobResult.status == 1) {
                  val currentDateTime: LocalDateTime = LocalDateTime.now()
                  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                  val time = currentDateTime.format(formatter)
                  val sendjson = new JSONObject()
                  sendjson.put("message_type","crowd.end")
                  sendjson.put("b_line",b_line)
                  sendjson.put("crowd_type","user")
                  sendjson.put("packageid",packageid)
                  sendjson.put("dailyid",dailyid)
                  sendjson.put("dt",dt)
                  sendjson.put("sendtime",time)

                  val senddata = new ProducerRecord[String, String](
                    flowtopic,
                    sendjson.toString
                  )
                  producer.send(senddata).get()
                  println(s"5.kafka回调：${sendjson.toString}")
                }
              },
              packageid = packageid,
              dailyid = dailyid,
              tags = tags.split(",").map(tag => s"'$tag'").mkString(","),
              bline = b_line
            )
          }
          Thread.sleep(1000)
        }
      }
      consumer.commitAsync()
    }

    consumer.close()

    if (errors > 0) {
      throw new RuntimeException("execute user group jobs failed: " + errors)
    }

  }

  def getConsumer(servers:String):KafkaConsumer[String, String] = {
    val props = new Properties()
    props.put("bootstrap.servers", servers) // Kafka 服务器地址
    props.put("key.deserializer", classOf[StringDeserializer].getName) // Key 反序列化器
    props.put("value.deserializer", classOf[StringDeserializer].getName) // Value 反序列化器
    props.put("group.id", "prod-gy-102") // 消费者组 ID
    props.put("auto.offset.reset", "latest") // 偏移量重置策略   earliest   latest
    props.put("enable.auto.commit", "false") // 自动提交偏移量
    props.put("max.poll.records", "1")
    props.put("max.poll.interval.ms", "900000")
    props.put("session.timeout.ms", "180000")

    val consumer = new KafkaConsumer[String, String](props)

    consumer
  }

  def getProducer(servers:String):KafkaProducer[String, String] = {
    val props = new Properties()
    props.put("bootstrap.servers", servers)
    props.put("acks", "all")  // 确保所有副本确认写入
    props.put("retries", "3")  // 失败自动重试次数
    props.put("key.serializer", classOf[StringSerializer].getName)
    props.put("value.serializer", classOf[StringSerializer].getName)

    val producer = new KafkaProducer[String, String](props)

    producer
  }

}
