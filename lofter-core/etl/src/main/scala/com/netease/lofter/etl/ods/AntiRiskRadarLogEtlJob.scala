package com.netease.lofter.etl.ods

import com.alibaba.fastjson.JSON
import com.github.nscala_time.time.Imports.DateTime
import com.netease.lofter.common.HdfsUtil
import com.netease.wm.util.Args
import org.apache.spark.sql.{SaveMode, SparkSession}

object AntiRiskRadarLogEtlJob {

  case class Post(deviceType: String, eventId: String, postLink: String, eventTime: Long, mobile: String, eventName: String,
                  userIp: String, postId: Long, opType: Int, blogId: Long, userId: Long,
                  rgName: String, rgRisk: String, rgScore: Int, ruleName: String, ruleKey: String, ruleScore: Int)

  case class Comment(deviceType: String, eventId: String, appVersion: String, ip: String, eventTime: Long, mobile: String,
                     commentId: Long, postId: Long, userId: Long,
                     rgName: String, rgRisk: String, rgScore: Int, ruleName: String, ruleKey: String, ruleScore: Int)

  case class Message(eventId: String, publishTime: Long, ip: String, mobile: String, eventName: String, msgId: Long,
                     publisherBlogId: Long, blogId: Long, otherBlogId: Long,
                     rgName: String, rgRisk: String, rgScore: Int, ruleName: String, ruleKey: String, ruleScore: Int)

  case class ShuaRe(name: String, guid: String, eventId: String, eventTime: Long, userId: String, targetPostId: String, targetBlogId: String,
                    regIp: String, clientIp: String, regIpUv: Long, praisePv: Long, recommendPv: Long, otherPv: Long,
                    actionType: String, phone: String, blackPhone: String, channel: String, ua: String,
                    rgName: String, rgRisk: String, rgScore: Int, ruleName: String, ruleKey: String, ruleScore: Int)

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)
    val spark = SparkSession.builder()
      .appName("Anti Risk Radar Log Etl")
      .config("spark.sql.parquet.binaryAsString", true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val dt = pargs.optional("date").getOrElse(DateTime.yesterday.toString("yyyy-MM-dd"))
    val path = s"/user/da_lofter/datastream/antiRisk/$dt"

    if (HdfsUtil.isHdfsPathEmpty(path)) {
      spark.close()
      return
    }

    val df = spark.read.json(path)

    import spark.implicits._

    df.filter("model.name='发文'").select("fields", "rgs").toJSON.flatMap { body: String =>
      val input = JSON.parseObject(body)
      val fieldsObject = input.getJSONObject("fields")

      val deviceType = fieldsObject.getString("deviceType")
      val eventId = fieldsObject.getString("eventId")
      val postLink = fieldsObject.getString("postLink")
      val eventTime = fieldsObject.getLongValue("eventTime")
      val eventName = fieldsObject.getString("eventName")
      val mobile = fieldsObject.getString("mobile")
      val userIP = fieldsObject.getString("userIP")
      val opType = fieldsObject.getIntValue("type")
      val postId = fieldsObject.getLongValue("postId")
      val blogId = fieldsObject.getLongValue("blogId")
      val userId = fieldsObject.getLongValue("userId")

      val rgsBody = input.getJSONArray("rgs")

      var postList: List[Post] = List()
      for (i <- 0 until rgsBody.size()) {
        val ruleGroupObject = rgsBody.getJSONObject(i)
        val group = ruleGroupObject.getString("group")
        val risk = ruleGroupObject.getString("risk")
        val score = ruleGroupObject.getIntValue("score")
        val hitObjects = ruleGroupObject.getJSONArray("hitObjects")

        // if hitObjects is empty,then rule detail is empty, else put the value in it
        if (hitObjects.isEmpty) {
          postList = Post(deviceType, eventId, postLink, eventTime, mobile, eventName, userIP, postId, opType, blogId, userId, group, risk, score, "", "", 0) :: postList
        } else {
          for (j <- 0 until hitObjects.size()) {
            val ruleObject = hitObjects.getJSONObject(j)
            val desc = ruleObject.getString("desc")
            val key = ruleObject.getString("key")
            val value = ruleObject.getIntValue("value")
            postList = Post(deviceType, eventId, postLink, eventTime, mobile, eventName, userIP, postId, opType, blogId, userId, group, risk, score, desc, key, value) :: postList
          }
        }
      }
      postList
    }.repartition(5)
      .write.mode(SaveMode.Overwrite)
      .parquet(s"/user/da_lofter/warehouse/anti_risk_radar_post/dt=$dt")

    spark.sql(s"alter table lofter.ods_log_anti_risk_post_di add if not exists partition(dt='$dt')")

    // extract the comment record
    df.filter("model.name='评论'").select("fields", "rgs").toJSON.flatMap { body: String =>
      val input = JSON.parseObject(body)
      val fieldsObject = input.getJSONObject("fields")

      val deviceType = fieldsObject.getString("deviceType")
      val eventId = fieldsObject.getString("eventId")
      val appVersion = fieldsObject.getString("appVersion")
      val ip = fieldsObject.getString("ip")
      val eventTime = fieldsObject.getLongValue("eventTime")
      val mobile = fieldsObject.getString("mobile")
      val commentId = fieldsObject.getLongValue("id")
      val postId = fieldsObject.getLongValue("postId")
      val userId = fieldsObject.getLongValue("userId")

      val rgsBody = input.getJSONArray("rgs")

      var commentList: List[Comment] = List()
      for (i <- 0 until rgsBody.size()) {
        val ruleGroupObject = rgsBody.getJSONObject(i)
        val group = ruleGroupObject.getString("group")
        val risk = ruleGroupObject.getString("risk")
        val score = ruleGroupObject.getIntValue("score")
        val hitObjects = ruleGroupObject.getJSONArray("hitObjects")

        // if hitObjects is empty,then rule detail is empty, else put the value in it
        if (hitObjects.isEmpty) {
          commentList = Comment(deviceType, eventId, appVersion, ip, eventTime, mobile, commentId, postId, userId, group, risk, score, "", "", 0) :: commentList
        } else {
          for (j <- 0 until hitObjects.size()) {
            val ruleObject = hitObjects.getJSONObject(j)
            val desc = ruleObject.getString("desc")
            val key = ruleObject.getString("key")
            val value = ruleObject.getIntValue("value")
            commentList = Comment(deviceType, eventId, appVersion, ip, eventTime, mobile, commentId, postId, userId, group, risk, score, desc, key, value) :: commentList
          }
        }
      }
      commentList
    }.repartition(5)
      .write.mode(SaveMode.Overwrite)
      .parquet(s"/user/da_lofter/warehouse/anti_risk_radar_comment/dt=$dt")

    spark.sql(s"alter table lofter.ods_log_anti_risk_comment_di add if not exists partition(dt='$dt')")

    // extract the message record
    df.filter("model.name='私信'").select("fields", "rgs").toJSON.flatMap { body: String =>
      val input = JSON.parseObject(body)
      val fieldsObject = input.getJSONObject("fields")

      val eventId = fieldsObject.getString("eventId")
      val publishTime = fieldsObject.getLongValue("publishTime")
      val ip = fieldsObject.getString("ip")
      val mobile = fieldsObject.getString("mobile")
      val eventName = fieldsObject.getString("eventName")
      val msgId = fieldsObject.getLongValue("msgId")
      val publisherBlogId = fieldsObject.getLongValue("publisherBlogId")
      val blogId = fieldsObject.getLongValue("blogId")
      val otherBlogId = fieldsObject.getLongValue("otherBlogId")

      val rgsBody = input.getJSONArray("rgs")

      var messageList: List[Message] = List()
      for (i <- 0 until rgsBody.size()) {
        val ruleGroupObject = rgsBody.getJSONObject(i)
        val group = ruleGroupObject.getString("group")
        val risk = ruleGroupObject.getString("risk")
        val score = ruleGroupObject.getIntValue("score")
        val hitObjects = ruleGroupObject.getJSONArray("hitObjects")

        // if hitObjects is empty,then rule detail is empty, else put the value in it
        if (hitObjects.isEmpty) {
          messageList = Message(eventId, publishTime, ip, mobile, eventName, msgId, publisherBlogId, blogId, otherBlogId, group, risk, score, "", "", 0) :: messageList
        } else {
          for (j <- 0 until hitObjects.size()) {
            val ruleObject = hitObjects.getJSONObject(j)
            val desc = ruleObject.getString("desc")
            val key = ruleObject.getString("key")
            val value = ruleObject.getIntValue("value")
            messageList = Message(eventId, publishTime, ip, mobile, eventName, msgId, publisherBlogId, blogId, otherBlogId, group, risk, score, desc, key, value) :: messageList
          }
        }
      }
      messageList
    }.repartition(5)
      .write.mode(SaveMode.Overwrite)
      .parquet(s"/user/da_lofter/warehouse/anti_risk_radar_message/dt=$dt")

    spark.sql(s"alter table lofter.ods_log_anti_risk_message_di add if not exists partition(dt='$dt')")

    // extract the shuare record
    df.filter("model.name='事中嫌疑刷热' or model.name='事前刷热' or model.name='事后嫌疑刷热'").select("model", "fields", "rgs").toJSON.flatMap { body: String =>
      val input = JSON.parseObject(body)
      val modelObject = input.getJSONObject("model")
      val modelName = modelObject.getString("name")
      val modelGuid = modelObject.getString("guid")

      val fieldsObject = input.getJSONObject("fields")
      val eventId = fieldsObject.getString("eventId")
      val channel = fieldsObject.getString("channel")
      val blackPhone = fieldsObject.getString("blackPhone")
      val ua = fieldsObject.getString("ua")
      val userId = fieldsObject.getString("userId")
      val regIp = fieldsObject.getString("regIp")
      val targetPostId = fieldsObject.getString("targetPostId")
      val praisePv = fieldsObject.getLongValue("praisePv")
      val actionType = fieldsObject.getString("actionType")
      val regIpUv = fieldsObject.getLongValue("regIpUv")
      val phone = fieldsObject.getString("phone")
      val recommendPv = fieldsObject.getLongValue("recommendPv")
      val clientIp = fieldsObject.getString("clientIp")
      val eventTime = fieldsObject.getLongValue("eventTime")
      val otherPv = fieldsObject.getLongValue("otherPv")
      val targetBlogId = fieldsObject.getString("targetBlogId")

      val rgsBody = input.getJSONArray("rgs")

      var shuaReList: List[ShuaRe] = List()
      for (i <- 0 until rgsBody.size()) {
        val ruleGroupObject = rgsBody.getJSONObject(i)
        val group = ruleGroupObject.getString("group")
        val risk = ruleGroupObject.getString("risk")
        val score = ruleGroupObject.getIntValue("score")
        val hitObjects = ruleGroupObject.getJSONArray("hitObjects")

        // if hitObjects is empty,then rule detail is empty, else put the value in it
        if (hitObjects.isEmpty) {
          shuaReList = ShuaRe(modelName, modelGuid, eventId, eventTime, userId, targetPostId, targetBlogId, regIp, clientIp,
            regIpUv, praisePv, recommendPv, otherPv, actionType, phone, blackPhone, channel, ua, group, risk, score, "", "", 0) :: shuaReList
        } else {
          for (j <- 0 until hitObjects.size()) {
            val ruleObject = hitObjects.getJSONObject(j)
            val desc = ruleObject.getString("desc")
            val key = ruleObject.getString("key")
            val value = ruleObject.getIntValue("value")
            shuaReList = ShuaRe(modelName, modelGuid, eventId, eventTime, userId, targetPostId, targetBlogId, regIp, clientIp,
              regIpUv, praisePv, recommendPv, otherPv, actionType, phone, blackPhone, channel, ua, group, risk, score, desc, key, value) :: shuaReList
          }
        }
      }
      shuaReList
    }.repartition(5)
      .write.mode(SaveMode.Overwrite)
      .parquet(s"/user/da_lofter/warehouse/anti_risk_radar_shuare/dt=$dt")

    spark.sql(s"alter table lofter.ods_log_anti_risk_shuare_di add if not exists partition(dt='$dt')")
  }
}
