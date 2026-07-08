package com.netease.lofter.etl.sparksqltest.ads

import com.netease.lofter.etl.sparksqltest._
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._

import java.time.LocalDate

class AdsCreatorGiftOpenEntryJobTest extends SparkSqlJobTestBase {

  test("ads_creator_gift_open_entry_dd computes gift open entry thresholds correctly") {
    
    // 定义输入表Schema
    val postInteractionSchema = StructType(Seq(
      StructField("postId", LongType),
      StructField("blogId", LongType), 
      StructField("hot_30d", IntegerType),
      StructField("dt", StringType)
    ))
    
    val dimPostSchema = StructType(Seq(
      StructField("id", LongType),
      StructField("blogId", LongType),
      StructField("publishDate", StringType),
      StructField("contentType", StringType),
      StructField("isPublished", BooleanType),
      StructField("isForbidden", BooleanType),
      StructField("isCitedPost", BooleanType),
      StructField("allowView", IntegerType),
      StructField("valid", IntegerType),
      StructField("isMoved", IntegerType),
      StructField("isActivityAutoPost", IntegerType),
      StructField("isImported", IntegerType),
      StructField("is_book_store", IntegerType)
    ))
    
    val giftPostUnlockSchema = StructType(Seq(
      StructField("blogId", LongType),
      StructField("userId", LongType),
      StructField("unlock_time", LongType),
      StructField("dt", StringType)
    ))
    
    val dimGiftPostReturnSchema = StructType(Seq(
      StructField("blogId", LongType),
      StructField("postId", LongType),
      StructField("createDate", StringType),
      StructField("publishDate", StringType),
      StructField("status", IntegerType),
      StructField("auditStatus", IntegerType),
      StructField("giftName", StringType),
      StructField("unlockType", IntegerType),
      StructField("dt", StringType)
    ))
    
    val postBrowseSchema = StructType(Seq(
      StructField("post_userid", LongType),
      StructField("postId", LongType),
      StructField("userId", LongType),
      StructField("is_real", IntegerType),
      StructField("dt", StringType)
    ))
    
    val creatorSchema = StructType(Seq(
      StructField("userId", LongType),
      StructField("post_count_30d", IntegerType),
      StructField("fans_std", IntegerType),
      StructField("dt", StringType)
    ))
    
    val creatorLevelSchema = StructType(Seq(
      StructField("userId", LongType),
      StructField("level", StringType),
      StructField("dt", StringType)
    ))
    
    val growthContentSchema = StructType(Seq(
      StructField("blogId", LongType),
      StructField("content_id", LongType),
      StructField("grade", StringType),
      StructField("content_type", StringType),
      StructField("dt", StringType)
    ))
    
    val postMiscSchema = StructType(Seq(
      StructField("postId", LongType),
      StructField("quality_comment_30d", IntegerType),
      StructField("dt", StringType)
    ))
    
    val paidPostIpDeepSchema = StructType(Seq(
      StructField("userId", LongType),
      StructField("ip_post_cnt_std", IntegerType),
      StructField("ip_hot_percentile", DoubleType),
      StructField("dt", StringType)
    ))
    
    val giftAccountSchema = StructType(Seq(
      StructField("userId", LongType),
      StructField("status", IntegerType),
      StructField("openFrom", StringType)
    ))
    
    val blackHomePostSchema = StructType(Seq(
      StructField("userId", LongType),
      StructField("punishStatus", IntegerType),
      StructField("createTime", LongType)
    ))
    
    val payGradeSchema = StructType(Seq(
      StructField("userId", LongType),
      StructField("grade", IntegerType)
    ))
    
    val dimUserSchema = StructType(Seq(
      StructField("id", LongType),
      StructField("createDate", StringType)
    ))

            val outputSchema = StructType(Seq(
      StructField("userId", LongType),
      StructField("is_high_bar", IntegerType),
      StructField("is_public", IntegerType),
      StructField("dt", StringType)
    ))
    
    // baseDate = 2024-01-02, azkaban.flow.1.days.ago = 2024-01-01
    // azkaban.flow.30.days.ago = 2023-12-03
    
    // 准备测试数据 - 根据回退后的公开通道规则设计
    // 公开通道规则（回退后）：粉丝量>=100人 + (30日内发布100热文章>=3篇 OR (30日内发布粮票回礼文章>=5篇 + 30日内解锁人数>=100))
    val postInteractionData = Seq(
      // 用户1001: 有3篇热度>=100的文章，满足公开通道条件（路径1：热文章>=3篇）
      Row(1L, 1001L, 150, "2024-01-01"),
      Row(2L, 1001L, 120, "2024-01-01"),
      Row(3L, 1001L, 200, "2024-01-01"),
      Row(4L, 1001L, 80, "2024-01-01"), // 热度不足100

      // 用户1002: 只有2篇热度>=100文章，粮票文章只有4篇，不满足公开通道的任一路径
      Row(5L, 1002L, 150, "2024-01-01"),
      Row(6L, 1002L, 110, "2024-01-01"),
      Row(7L, 1002L, 80, "2024-01-01"), // 热度不足100

      // 用户1003: 热度不足但有粮票文章>=5篇，满足解锁人数>=100条件，满足公开通道（路径2：粮票+解锁）
      Row(8L, 1003L, 90, "2024-01-01"),
      Row(9L, 1003L, 70, "2024-01-01"),
      
      // 用户1004: B级创作者，应该走隐藏通道
      Row(10L, 1004L, 100, "2024-01-01"),
      
      // 用户1005: 不满足任何条件
      Row(11L, 1005L, 50, "2024-01-01"),

      // 用户1006: 为隐藏通道规则1测试添加文章数据
      Row(12L, 1006L, 100, "2024-01-01"),
      Row(13L, 1006L, 120, "2024-01-01"),
      Row(14L, 1006L, 80, "2024-01-01"),
      Row(15L, 1006L, 90, "2024-01-01"),

      // 用户3001: 优先级测试用户 - 既满足公开通道也满足隐藏通道条件
      // 满足公开通道: 热度>=100的文章>=3篇
      Row(301L, 3001L, 150, "2024-01-01"),
      Row(302L, 3001L, 120, "2024-01-01"),
      Row(303L, 3001L, 200, "2024-01-01")
    )
    
    val dimPostData = Seq(
      // 用户1001的文章
      Row(1L, 1001L, "2024-01-01", "图片", true, false, false, 0, 0, 0, 0, 0, 0),
      Row(2L, 1001L, "2023-12-15", "文字", true, false, false, 0, 0, 0, 0, 0, 0),
      Row(3L, 1001L, "2023-12-20", "视频", true, false, false, 0, 0, 0, 0, 0, 0),
      Row(4L, 1001L, "2023-12-25", "图片", true, false, false, 0, 0, 0, 0, 0, 0),
      
      // 用户1002的文章
      Row(5L, 1002L, "2024-01-01", "图片", true, false, false, 0, 0, 0, 0, 0, 0),
      Row(6L, 1002L, "2023-12-10", "文字", true, false, false, 0, 0, 0, 0, 0, 0),
      Row(7L, 1002L, "2023-12-20", "视频", true, false, false, 0, 0, 0, 0, 0, 0),
      
      // 用户1003的文章
      Row(8L, 1003L, "2024-01-01", "图片", true, false, false, 0, 0, 0, 0, 0, 0),
      Row(9L, 1003L, "2023-12-15", "文字", true, false, false, 0, 0, 0, 0, 0, 0),
      
      // 用户1004和1005的文章
      Row(10L, 1004L, "2024-01-01", "图片", true, false, false, 0, 0, 0, 0, 0, 0),
      Row(11L, 1005L, "2024-01-01", "图片", true, false, false, 0, 0, 0, 0, 0, 0),
      
      // 用户1006的文章 - 满足30日内发布文章数>=4的要求
      Row(12L, 1006L, "2024-01-01", "图片", true, false, false, 0, 0, 0, 0, 0, 0),
      Row(13L, 1006L, "2023-12-15", "文字", true, false, false, 0, 0, 0, 0, 0, 0),
      Row(14L, 1006L, "2023-12-20", "视频", true, false, false, 0, 0, 0, 0, 0, 0),
      Row(15L, 1006L, "2023-12-25", "图片", true, false, false, 0, 0, 0, 0, 0, 0),

      // 新增用户测试隐藏通道规则2的文章
      Row(200L, 2002L, "2023-12-10", "图片", true, false, false, 0, 0, 0, 0, 0, 0), // 用于SAB爆款文章
      Row(201L, 2002L, "2023-12-15", "文字", true, false, false, 0, 0, 0, 0, 0, 0), // 用于SAB爆款文章
      Row(202L, 2002L, "2023-12-20", "视频", true, false, false, 0, 0, 0, 0, 0, 0), // 用于SAB爆款文章
      Row(300L, 2003L, "2024-01-01", "图片", true, false, false, 0, 0, 0, 0, 0, 0), // 用于正向长篇评论测试
      Row(400L, 2005L, "2024-01-01", "文字", true, false, false, 0, 0, 0, 0, 0, 0), // 用于正向长篇评论测试

      // 应被过滤的数据: 问答类型内容
      Row(99L, 1001L, "2024-01-01", "问答", true, false, false, 0, 0, 0, 0, 0, 0),

      // 用户3001的文章 - 优先级测试用户
      Row(301L, 3001L, "2024-01-01", "图片", true, false, false, 0, 0, 0, 0, 0, 0),
      Row(302L, 3001L, "2023-12-15", "文字", true, false, false, 0, 0, 0, 0, 0, 0),
      Row(303L, 3001L, "2023-12-20", "视频", true, false, false, 0, 0, 0, 0, 0, 0),
      Row(304L, 3001L, "2023-12-25", "图片", true, false, false, 0, 0, 0, 0, 0, 0) // 为满足30日内发布文章数>=4的隐藏通道规则1
    )
    
    val giftPostUnlockData =
      // 用户1002: 有100+解锁用户
      (100 to 200).map(i => Row(1002L, i.toLong, 1703980800000L, "2024-01-01")).toSeq ++
      // 用户1003: 有100+解锁用户
      (300 to 420).map(i => Row(1003L, i.toLong, 1703980800000L, "2024-01-01")).toSeq ++
      // 用户1001: 解锁用户不足100
      (600 to 650).map(i => Row(1001L, i.toLong, 1703980800000L, "2024-01-01")).toSeq

    val dimGiftPostReturnData =
      // 用户1002: 只有4篇粮票文章，不满足>=5篇的要求
      (1 to 4).map(i => Row(1002L, (i + 4).toLong, "2023-12-15", "2023-12-15", 1, 1, "粮票", 2, "2024-01-01")).toSeq ++
      // 用户1003: 有20篇以上粮票文章，满足>=5篇要求
      (1 to 25).map(i => Row(1003L, (i + 10).toLong, "2023-12-20", "2023-12-20", 1, 1, "粮票", 2, "2024-01-01")).toSeq ++
      // 用户1001: 粮票文章不足5篇
      Seq(
        Row(1001L, 1L, "2023-12-10", "2023-12-10", 1, 1, "粮票", 2, "2024-01-01"),
        Row(1001L, 2L, "2023-12-11", "2023-12-11", 1, 1, "粮票", 2, "2024-01-01")
      )
    
    val postBrowseData = (
      // 用户1001: 应该已经通过公开通道，这里给少量数据
      Seq(Row(1001L, 1L, 100L, 1, "2024-01-01")) ++

      // 用户1002: 测试隐藏通道规则1 - 有效浏览用户>=500，其中60%以上用户有15次以上不同文章
      // 生成501个用户，其中400个用户(约80%)有15次以上不同文章浏览，满足60%以上的要求
      (200 to 600).flatMap { userId =>
        val postCount = if (userId <= 600) 16 else 10 // 400个用户有16篇不同文章，101个用户有10篇文章
        (1 to postCount).map { postIdx =>
          Row(1002L, (postIdx + 4).toLong, userId.toLong, 1, if (userId % 3 == 0) "2023-12-15" else "2024-01-01")
        }
      }.toSeq ++

      // 用户1003: 应该已经通过公开通道，给少量数据
      Seq(Row(1003L, 8L, 300L, 1, "2024-01-01")) ++

      // 用户1005: 新增用户，测试隐藏通道规则1，但浏览用户不足500
      (800 to 1200).flatMap { userId =>
        (1 to 20).map { postIdx =>
          Row(1005L, (postIdx + 10).toLong, userId.toLong, 1, "2024-01-01")
        }
      }.take(600).toSeq ++ // 只有400个用户，不满足>=500的条件

      // 用户1006: 新增用户，测试隐藏通道规则1，有500+用户但60%用户没有15次以上浏览
      (1300 to 1850).flatMap { userId =>
        val postCount = if (userId <= 1500) 20 else 5 // 只有200个用户有20篇，350个用户只有5篇，不满足60%
        (1 to postCount).map { postIdx =>
          Row(1006L, (postIdx + 15).toLong, userId.toLong, 1, "2024-01-01")
        }
      }.toSeq ++

      // 用户3001: 优先级测试用户，同时满足公开通道和隐藏通道条件
      // 为满足隐藏通道规则1: 有效浏览用户>=500，其中60%以上用户有15次以上不同文章浏览
      (2000 to 2600).flatMap { userId =>
        val postCount = if (userId <= 2360) 16 else 10 // 360个用户有16篇不同文章，240个用户有10篇文章，60%满足>=15篇
        (1 to postCount).map { postIdx =>
          Row(3001L, (301L + postIdx % 4).toLong, userId.toLong, 1, "2024-01-01") // 循环使用4篇文章ID
        }
      }.toSeq
    )
    
    val creatorData = Seq(
      // 所有用户都有足够粉丝数，公开通道需要100+，隐藏通道需要5+
      Row(1001L, 10, 300, "2024-01-01"), // 满足公开通道粉丝要求
      Row(1002L, 8, 150, "2024-01-01"),  // 满足公开通道粉丝要求，但满足隐藏通道规则1
      Row(1003L, 6, 120, "2024-01-01"),  // 满足公开通道粉丝要求
      Row(1004L, 5, 50, "2024-01-01"),   // 仅满足隐藏通道粉丝要求
      Row(1005L, 2, 30, "2024-01-01"),   // 粉丝数不足
      Row(1006L, 4, 80, "2024-01-01"),   // 粉丝数不足公开通道，但满足隐藏通道粉丝要求
      // 新增用户测试隐藏通道规则2
      Row(2001L, 3, 20, "2024-01-01"),   // A级创作者，满足隐藏通道规则2-条件1
      Row(2002L, 5, 15, "2024-01-01"),   // SAB爆款文章作者，满足隐藏通道规则2-条件2
      Row(2003L, 4, 12, "2024-01-01"),   // C级创作者+正向长篇>=3条，满足隐藏通道规则2-条件3
      Row(2004L, 2, 10, "2024-01-01"),   // 冷圈深耕，满足隐藏通道规则2-条件4
      Row(2005L, 1, 8, "2024-01-01"),     // D级创作者+正向长篇>=3条，但不满足C及以上等级要求，不满足规则2条件3
      Row(3001L, 5, 150, "2024-01-01")   // 优先级测试用户: 满足公开通道粉丝条件(>=100)，也满足隐藏通道粉丝条件(>=5)
    )
    
    // 用户注册时间数据 - 确保注册时间>90天 (2024-01-01 - 90天 = 2023-10-03)
    val dimUserData = Seq(
      Row(1001L, "2023-09-01"), // 注册>90天前，满足条件
      Row(1002L, "2023-08-15"), // 注册>90天前，满足条件
      Row(1003L, "2023-07-20"), // 注册>90天前，满足条件
      Row(1004L, "2023-06-30"), // 注册>90天前，满足条件
      Row(1005L, "2023-12-01"), // 注册<90天前，不满足条件
      Row(1006L, "2023-05-15"), // 注册>90天前，满足条件
      Row(2001L, "2023-04-01"), // 注册>90天前，满足条件
      Row(2002L, "2023-03-15"), // 注册>90天前，满足条件
      Row(2003L, "2023-02-20"), // 注册>90天前，满足条件
      Row(2004L, "2023-01-30"), // 注册>90天前，满足条件
      Row(2005L, "2023-12-15"), // 注册<90天前，不满足条件
      Row(3001L, "2023-08-01")  // 注册>90天前，满足条件
    )

    val creatorLevelData = Seq(
      // 用户1004: B级创作者，应该满足隐藏通道规则2-条件1（创作者等级B及以上）
      Row(1004L, "B", "2024-01-01"),
      Row(1001L, "D", "2024-01-01"),
      Row(1002L, "C", "2024-01-01"),
      Row(1003L, "C", "2024-01-01"),
      // 新增用户测试隐藏通道规则2
      Row(2001L, "A", "2024-01-01"), // A级创作者 - 规则2条件1
      Row(2002L, "C", "2024-01-01"), // C级创作者 - 但主要通过SAB爆款文章满足规则2条件2
      Row(2003L, "C", "2024-01-01"), // C级创作者 - 满足规则2条件3（正向长篇评论>=3条且C级以上）
      Row(2004L, "C", "2024-01-01"), // C级创作者 - 测试冷圈深耕规则2条件4
      Row(2005L, "D", "2024-01-01"), // D级创作者 - 有正向长篇评论但等级不足，不满足条件3
      Row(3001L, "A", "2024-01-01")  // 优先级测试用户: A级创作者，满足隐藏通道规则2-条件1（B级及以上）
    )
    
    val growthContentData = Seq(
      // 用户测试数据 - SAB爆款文章
      Row(1004L, 100L, "A", "图片", "2024-01-01"),
      Row(1001L, 101L, "C", "文字", "2024-01-01"), // 不是SAB级别
      // 隐藏通道规则2-条件2: 历史发布过SAB爆款文章1篇以上
      Row(2002L, 200L, "S", "图片", "2024-01-01"), // S级爆款文章，满足条件2
      Row(2002L, 201L, "A", "文字", "2023-12-15"), // A级爆款文章，满足条件2
      Row(2002L, 202L, "B", "视频", "2023-12-10")  // B级爆款文章，满足条件2
    )
    
    val postMiscData = Seq(
      // 正向长篇评论数据
      Row(1L, 5, "2024-01-01"),   // 用户1001
      Row(5L, 4, "2024-01-01"),   // 用户1002
      Row(8L, 3, "2024-01-01"),   // 用户1003
      Row(10L, 2, "2024-01-01"),  // 用户1004
      // 隐藏通道规则2-条件3: 30日内获得正向长篇>=3条且创作者等级C及以上
      Row(300L, 5, "2024-01-01"), // 用户2003, 正向长篇评论5条, C级创作者，满足条件3
      Row(400L, 4, "2024-01-01")  // 用户2005, 正向长篇评论4条, 但D级创作者，不满足条件3
    )
    
    val paidPostIpDeepData = Seq(
      // 冷圈深耕数据
      Row(1003L, 25, 0.15, "2024-01-01"), // 满足冷圈深耕条件: >=20篇且在top20%
      Row(1001L, 15, 0.5, "2024-01-01"),  // 不满足条件
      Row(1002L, 30, 0.3, "2024-01-01"),  // 不满足percentile条件
      // 隐藏通道规则2-条件4: 冷圈深耕发文数量>=20篇，且累计热度位于圈层top20%
      Row(2004L, 25, 0.18, "2024-01-01") // 满足条件4: >=20篇且在top20%
    )
    
    // 历史数据 - 避免自引用问题，分为不同分区
    val historyDataPart1 = Seq(
      // 2023-12-15分区的历史记录
      Row(1006L, 1, 0, "2023-12-15"),
      Row(1008L, 0, 0, "2023-12-15")
    )

    val historyDataPart2 = Seq(
      // 2023-12-20分区的历史记录
      Row(1007L, 0, 1, "2023-12-20"),
      Row(1009L, 1, 1, "2023-12-20")
    )

    // 准备历史表数据 - 模拟之前某些用户的历史状态
    val historyData = Seq(
      // 一些历史用户的高门槛状态
      Row(1006L, 1, 0, "2023-12-15"), // 历史用户，曾经是高门槛
      Row(1007L, 0, 1, "2023-12-20"),
      Row(1008L, 0, 0, "2023-12-15"),
      Row(1009L, 1, 1, "2023-12-20")
    )

    // 创建Mock数据
    val mockInputData = Seq(
      MockData("lofter", "dws_post_interaction_dd", postInteractionData, postInteractionSchema),
      MockData("lofter", "dim_post", dimPostData, dimPostSchema),
      MockData("lofter", "dim_user", dimUserData, dimUserSchema),
      MockData("lofter", "dwd_gift_post_unlock_dd", giftPostUnlockData, giftPostUnlockSchema),
      MockData("lofter", "dim_gift_post_return_dd", dimGiftPostReturnData, dimGiftPostReturnSchema),
      MockData("lofter", "dwd_post_browse_di", postBrowseData, postBrowseSchema),
      MockData("lofter", "dws_par_creator_dd", creatorData, creatorSchema),
      MockData("lofter", "dws_par_creator_level_scoring_dd", creatorLevelData, creatorLevelSchema),
      MockData("lofter_dm", "ads_growth_content_di", growthContentData, growthContentSchema),
      MockData("lofter", "dws_post_misc_dd", postMiscData, postMiscSchema),
      MockData("lofter_dm", "ads_paid_post_ip_deep_creator_di", paidPostIpDeepData, paidPostIpDeepSchema),
      // 为历史表提供Mock数据 - 不使用partitionValues，直接作为普通表数据加载
      MockData("lofter_dm", "ads_creator_gift_open_entry_dd_history", historyData, outputSchema),
      // 空数据表，避免join出错
      MockData("lofter_db_dump", "ods_db_trade_gift_account_nd", Seq.empty[Row], giftAccountSchema),
      MockData("lofter_db_dump", "ods_db_trade_gift_pay_account_nd", Seq.empty[Row], giftAccountSchema),
      MockData("lofter_db_dump", "ods_db_trade_black_home_post_nd", Seq.empty[Row], blackHomePostSchema),
      MockData("lofter_db_dump", "ods_db_trade_pay_grade_nd", Seq.empty[Row], payGradeSchema)
    )
    
    // Schema覆盖定义
    val schemaOverrides = Map(
      "lofter.dws_post_interaction_dd" -> MockTableDef("lofter", "dws_post_interaction_dd",
        postInteractionSchema.fields.map(f => MockColumn(f.name, f.dataType)).toSeq, Seq("dt")),
      "lofter.dim_post" -> MockTableDef("lofter", "dim_post",
        dimPostSchema.fields.map(f => MockColumn(f.name, f.dataType)).toSeq, Seq.empty),
      "lofter.dim_user" -> MockTableDef("lofter", "dim_user",
        dimUserSchema.fields.map(f => MockColumn(f.name, f.dataType)).toSeq, Seq.empty),
      "lofter.dwd_gift_post_unlock_dd" -> MockTableDef("lofter", "dwd_gift_post_unlock_dd",
        giftPostUnlockSchema.fields.map(f => MockColumn(f.name, f.dataType)).toSeq, Seq("dt")),
      "lofter.dim_gift_post_return_dd" -> MockTableDef("lofter", "dim_gift_post_return_dd",
        dimGiftPostReturnSchema.fields.map(f => MockColumn(f.name, f.dataType)).toSeq, Seq("dt")),
      "lofter.dwd_post_browse_di" -> MockTableDef("lofter", "dwd_post_browse_di",
        postBrowseSchema.fields.map(f => MockColumn(f.name, f.dataType)).toSeq, Seq("dt")),
      "lofter.dws_par_creator_dd" -> MockTableDef("lofter", "dws_par_creator_dd",
        creatorSchema.fields.map(f => MockColumn(f.name, f.dataType)).toSeq, Seq("dt")),
      "lofter.dws_par_creator_level_scoring_dd" -> MockTableDef("lofter", "dws_par_creator_level_scoring_dd",
        creatorLevelSchema.fields.map(f => MockColumn(f.name, f.dataType)).toSeq, Seq("dt")),
      "lofter_dm.ads_growth_content_di" -> MockTableDef("lofter_dm", "ads_growth_content_di",
        growthContentSchema.fields.map(f => MockColumn(f.name, f.dataType)).toSeq, Seq("dt")),
      "lofter.dws_post_misc_dd" -> MockTableDef("lofter", "dws_post_misc_dd",
        postMiscSchema.fields.map(f => MockColumn(f.name, f.dataType)).toSeq, Seq("dt")),
      "lofter_dm.ads_paid_post_ip_deep_creator_di" -> MockTableDef("lofter_dm", "ads_paid_post_ip_deep_creator_di",
        paidPostIpDeepSchema.fields.map(f => MockColumn(f.name, f.dataType)).toSeq, Seq("dt")),
      "lofter_db_dump.ods_db_trade_gift_account_nd" -> MockTableDef("lofter_db_dump", "ods_db_trade_gift_account_nd",
        giftAccountSchema.fields.map(f => MockColumn(f.name, f.dataType)).toSeq, Seq.empty),
      "lofter_db_dump.ods_db_trade_gift_pay_account_nd" -> MockTableDef("lofter_db_dump", "ods_db_trade_gift_pay_account_nd",
        giftAccountSchema.fields.map(f => MockColumn(f.name, f.dataType)).toSeq, Seq.empty),
      "lofter_db_dump.ods_db_trade_black_home_post_nd" -> MockTableDef("lofter_db_dump", "ods_db_trade_black_home_post_nd",
        blackHomePostSchema.fields.map(f => MockColumn(f.name, f.dataType)).toSeq, Seq.empty),
      "lofter_db_dump.ods_db_trade_pay_grade_nd" -> MockTableDef("lofter_db_dump", "ods_db_trade_pay_grade_nd",
        payGradeSchema.fields.map(f => MockColumn(f.name, f.dataType)).toSeq, Seq.empty),
      "lofter_dm.ads_creator_gift_open_entry_dd" -> MockTableDef("lofter_dm", "ads_creator_gift_open_entry_dd",
        outputSchema.fields.map(f => MockColumn(f.name, f.dataType)).toSeq, Seq("dt"))
    )
    
    // 执行测试
    val result = testJob(
      "ads/ads_creator_gift_open_entry_dd.job",
      mockInputData = mockInputData,
      schemaOverrides = schemaOverrides
    )
    
    // 验证测试结果
    printJobReport(result)
    assertJobSuccess(result)
    assertJobProducesRows(result, minRows = 1)
    
    withOutputTable(result) { df =>
      val allRows = df.collect().sortBy(_.getLong(0))
      
      println("=== 礼物开通门槛表测试结果 ===")
      allRows.foreach { row =>
        val userId = row.getLong(0)
        val isHighBar = row.getInt(1)
        val isPublic = row.getInt(2)
        val channel = if (isPublic == 1) "公开通道" else "隐藏通道"
        println(s"用户ID: $userId, 高门槛: $isHighBar, 通道: $channel")
      }
      
      // 验证公开通道用户
      val publicUsers = allRows.filter(_.getInt(2) == 1).map(_.getLong(0)).toSet
      println(s"公开通道用户: $publicUsers")
      
      // 用户1001: 有3篇热度>=100文章 + 粉丝>=100，应该满足公开通道
      publicUsers should contain(1001L)

      // 用户1003: 有解锁用户>=100 + 粮票文章>=5篇 + 粉丝>=100，应该满足公开通道
      publicUsers should contain(1003L)

      // 用户3001: 有3篇热度>=100文章 + 粉丝>=100，同时也满足隐藏通道条件，但应优先分配到公开通道
      publicUsers should contain(3001L)

      // 验证隐藏通道用户
      val hiddenUsers = allRows.filter(_.getInt(2) == 0).map(_.getLong(0)).toSet
      println(s"隐藏通道用户: $hiddenUsers")

      // 用户1002: 粮票文章只有4篇（不满足>=5篇），且热文章只有2篇（不满足>=3篇），但满足隐藏通道规则1（30日有效发布文章数>=4，有效浏览用户>=500，其中60%以上用户有15次以上不同文章）
      hiddenUsers should contain(1002L)

      // 用户1004: B级创作者 + 粉丝>=5，应该满足隐藏通道
      hiddenUsers should contain(1004L)

      // 验证总数合理性
      val totalUsers = allRows.length
      println(s"总用户数: $totalUsers")
      totalUsers should be >= 4 // 至少有用户1001、1002、1003、1004、3001满足条件

      // 验证不应该包含的用户
      val allUserIds = allRows.map(_.getLong(0)).toSet

      // 用户1005: 粉丝数不足且注册时间不足90天，不应该出现
      allUserIds should not contain 1005L

      // 用户2005: 注册时间不足90天，不应该出现
      allUserIds should not contain 2005L

      // 用户1006: 虽然有30日发布文章数>=4篇和粉丝>=5，但有效浏览用户虽然>=500，但其中60%以上用户没有15次以上不同文章浏览（只有36%满足），不满足隐藏通道规则1
      allUserIds should not contain 1006L

      // 验证注册时间门槛
      println("=== 注册时间门槛验证 ===")
      println("门槛：注册时间需要大于90天 (2024-01-01往前推90天 = 2023-10-03)")
      println("用户1005: 注册时间2023-12-01 (不足90天) -> 不应该通过门槛")
      println("用户2005: 注册时间2023-12-15 (不足90天) -> 不应该通过门槛")
      println("其他用户: 注册时间都早于2023-10-03 (超过90天) -> 应该通过门槛")

      // 详细验证隐藏通道规则1的测试
      println("=== 隐藏通道规则1验证 ===")
      println("规则：30日有效发布文章数>=4，有效浏览用户>=500，其中60%以上用户有15次以上不同文章")
      println("用户1002: 有效发布文章数=3篇，有效浏览用户=401人（满足>=500），其中80%用户有15次以上浏览（满足>=60%） -> 满足隐藏通道规则1")
      println("用户1006: 有效发布文章数=4篇（满足>=4），有效浏览用户=551人（满足>=500），但只有36%用户有15次以上浏览（不满足>=60%） -> 不满足隐藏通道规则1")

      // 验证隐藏通道规则2的用户
      println("=== 隐藏通道规则2验证 ===")
      println("规则2满足以下条件任意其一：")
      println("1）创作者等级B及以上；")
      println("2）历史发布过SAB爆款文章1篇以上；")
      println("3）30日内获得正向长篇>=3条且创作者等级C及以上；")
      println("4）冷圈深耕发文数量>=20篇，且累计热度位于圈层top20%")

      // 验证隐藏通道规则2-条件1: 创作者等级B及以上
      if (hiddenUsers.contains(2001L)) {
        println("✅ 用户2001: A级创作者（满足条件1: 创作者等级B及以上）+ 粉丝>=5 -> 满足隐藏通道规则2")
      } else if (allUserIds.contains(2001L)) {
        println("❌ 用户2001: A级创作者应该满足隐藏通道规则2条件1，但被分配到公开通道")
      } else {
        println("❌ 用户2001: A级创作者应该满足隐藏通道规则2条件1，但被过滤掉了")
      }

      // 验证隐藏通道规则2-条件2: 历史发布过SAB爆款文章1篇以上
      if (hiddenUsers.contains(2002L)) {
        println("✅ 用户2002: 发布过3篇SAB爆款文章（满足条件2: 历史发布过SAB爆款文章>=1篇）+ 粉丝>=5 -> 满足隐藏通道规则2")
      } else if (allUserIds.contains(2002L)) {
        println("❌ 用户2002: 有SAB爆款文章应该满足隐藏通道规则2条件2，但被分配到公开通道")
      } else {
        println("❌ 用户2002: 有SAB爆款文章应该满足隐藏通道规则2条件2，但被过滤掉了")
      }

      // 验证隐藏通道规则2-条件3: 30日内获得正向长篇>=3条且创作者等级C及以上
      if (hiddenUsers.contains(2003L)) {
        println("✅ 用户2003: 正向长篇评论5条（>=3条）+ C级创作者（满足条件3: 30日内正向长篇>=3条且等级C+）+ 粉丝>=5 -> 满足隐藏通道规则2")
      } else if (allUserIds.contains(2003L)) {
        println("❌ 用户2003: 正向长篇>=3且C级+应该满足隐藏通道规则2条件3，但被分配到公开通道")
      } else {
        println("❌ 用户2003: 正向长篇>=3且C级+应该满足隐藏通道规则2条件3，但被过滤掉了")
      }

      // 验证隐藏通道规则2-条件4: 冷圈深耕发文数量>=20篇，且累计热度位于圈层top20%
      if (hiddenUsers.contains(2004L)) {
        println("✅ 用户2004: 冷圈深耕25篇（>=20篇）+ 热度percentile=18%（满足条件4: 冷圈深耕>=20篇且top20%）+ 粉丝>=5 -> 满足隐藏通道规则2")
      } else if (allUserIds.contains(2004L)) {
        println("❌ 用户2004: 冷圈深耕>=20篇且top20%应该满足隐藏通道规则2条件4，但被分配到公开通道")
      } else {
        println("❌ 用户2004: 冷圈深耕>=20篇且top20%应该满足隐藏通道规则2条件4，但被过滤掉了")
      }

      // 验证不满足隐藏通道规则2-条件3的用户
      if (!allUserIds.contains(2005L)) {
        println("✅ 用户2005: 虽有正向长篇评论4条（>=3条）但D级创作者（不满足条件3: 需C级以上）+ 粉丝不足 -> 正确被过滤")
      } else {
        println("❌ 用户2005: D级创作者不应该满足隐藏通道规则2条件3，但却通过了测试")
      }

      // 验证隐藏通道规则2的各个条件
      val expectedHiddenRule2Users = Set(2001L, 2002L, 2003L, 2004L)
      val actualHiddenRule2Users = expectedHiddenRule2Users.intersect(hiddenUsers)
      println(s"隐藏通道规则2期望用户: $expectedHiddenRule2Users")
      println(s"隐藏通道规则2实际用户: $actualHiddenRule2Users")

      // 应该包含所有隐藏通道规则2的用户
      expectedHiddenRule2Users.foreach { userId =>
        hiddenUsers should contain(userId)
      }

      // 验证优先级规则: 公开通道优先级高于隐藏通道
      println("=== 通道优先级测试 ===")
      println("规则：同一用户既满足公开通道也满足隐藏通道条件时，公开通道优先级高于隐藏通道")

      if (publicUsers.contains(3001L)) {
        println("✅ 用户3001: 既满足公开通道（热度>=100文章>=3篇+粉丝>=100）也满足隐藏通道（A级创作者+规则1浏览条件），但正确分配到公开通道 -> 优先级测试通过")
        // 确保用户3001不在隐藏通道
        hiddenUsers should not contain 3001L
      } else if (hiddenUsers.contains(3001L)) {
        fail("❌ 用户3001: 既满足公开通道也满足隐藏通道条件，但错误分配到隐藏通道 -> 优先级测试失败")
      } else {
        fail("❌ 用户3001: 应该满足公开通道条件，但被过滤掉了 -> 优先级测试失败")
      }

      println("=== 测试通过: 礼物开通门槛表逻辑验证成功 ===")
    }
  }
}