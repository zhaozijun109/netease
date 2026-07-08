package com.netease.lofter.etl.sparksqltest.ads

import com.netease.lofter.etl.sparksqltest._
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.scalatest.matchers.should.Matchers._

class AdsParCreatorGiftLevelInternshipTaskJobTest extends SparkSqlJobTestBase {

  test("ads_par_creator_gift_level_internship_task_dd computes internship task thresholds correctly") {
    
    // 定义模式结构
    val blogLevelDetailSchema = StructType(Seq(
      StructField("userId", LongType),
      StructField("paid_postnum", IntegerType), // 有效回礼内容数量
      StructField("post_all_quality_percent_avg_score", DoubleType), // 篇均质量分
      StructField("top_money_avg_all", DoubleType), // 新发文篇均收益
      StructField("low_postnum", IntegerType), // 预警文章数量
      StructField("dt", StringType) // 分区列
    ))

    val premiumScoringSchema = StructType(Seq(
      StructField("blogId", LongType),
      StructField("publishDate", StringType),
      StructField("zhenrate", DoubleType), // 正向反馈率
      StructField("dt", StringType) // 分区列
    ))

    val payGradeSchema = StructType(Seq(
      StructField("userId", LongType),
      StructField("grade", IntegerType)
    ))

    val giftUnlockSchema = StructType(Seq(
      StructField("blogId", LongType),
      StructField("postId", LongType),
      StructField("userId", LongType),
      StructField("unlock_time", LongType),
      StructField("dt", StringType) // 分区列
    ))
    
    // 定义输出表Schema（基于job文件中的SQL选择的所有列）
    val outputSchema = StructType(Seq(
      StructField("blogId", LongType),
      StructField("t1_gift_post_count", IntegerType),
      StructField("t2_avg_positive_feedback_percentile", DoubleType),
      StructField("t2_avg_positive_feedback_rate", DoubleType),
      StructField("t3_total_income", LongType),
      StructField("t3_avg_new_post_income", DoubleType),
      StructField("t4_warn_post_count", IntegerType),
      StructField("t1_status", IntegerType),
      StructField("t2_status", IntegerType),
      StructField("t3_status", IntegerType),
      StructField("t4_status", IntegerType),
      StructField("status", IntegerType),
      StructField("dt", StringType)
    ))
    
    // baseDate = 2024-01-02, azkaban.flow.1.days.ago = 2024-01-01
    // azkaban.flow.30.days.ago = 2023-12-03
    
    // 准备测试数据
    val blogLevelDetailData = Seq(
      // 用户1001: 全部满足条件 - 有效回礼≥3篇，质量分≥120，篇均收益≥20元，预警文章≤2篇
      Row(1001L, 5, 125.0, 25.0, 1, "2024-01-01"),

      // 用户1002: 仅满足收益条件（累计收益≥100元）- 有效回礼不足，质量分不足，预警文章过多
      Row(1002L, 2, 100.0, 15.0, 3, "2024-01-01"),

      // 用户1003: 满足回礼数量和质量分条件，但收益和预警不满足
      Row(1003L, 4, 130.0, 15.0, 3, "2024-01-01"),

      // 用户1004: 满足所有条件但质量分刚好达到阈值（120分）
      Row(1004L, 3, 120.0, 20.0, 2, "2024-01-01"),

      // 用户1005: 边界测试 - 有效回礼刚好3篇，预警文章刚好2篇
      Row(1005L, 3, 125.0, 25.0, 2, "2024-01-01"),

      // 用户1006: 不满足任何条件
      Row(1006L, 1, 80.0, 5.0, 5, "2024-01-01")
    )

    val premiumScoringData = Seq(
      // 用户1001的正向反馈率 - 高质量
      Row(1001L, "2024-01-01", 0.08, "2024-01-01"),
      Row(1001L, "2023-12-20", 0.09, "2024-01-01"),
      Row(1001L, "2023-12-15", 0.07, "2024-01-01"),

      // 用户1002的正向反馈率 - 中等质量
      Row(1002L, "2024-01-01", 0.05, "2024-01-01"),
      Row(1002L, "2023-12-25", 0.04, "2024-01-01"),

      // 用户1003的正向反馈率 - 高质量
      Row(1003L, "2024-01-01", 0.10, "2024-01-01"),
      Row(1003L, "2023-12-30", 0.08, "2024-01-01"),
      Row(1003L, "2023-12-20", 0.09, "2024-01-01"),

      // 用户1004的正向反馈率 - 高质量
      Row(1004L, "2024-01-01", 0.07, "2024-01-01"),
      Row(1004L, "2023-12-28", 0.08, "2024-01-01"),

      // 用户1005的正向反馈率 - 高质量
      Row(1005L, "2024-01-01", 0.09, "2024-01-01"),
      Row(1005L, "2023-12-25", 0.08, "2024-01-01"),

      // 用户1006的正向反馈率 - 低质量
      Row(1006L, "2024-01-01", 0.03, "2024-01-01")
    )
    
    val payGradeData = Seq(
      // 所有用户都是1级（试用期门槛只对1级用户）
      Row(1001L, 1),
      Row(1002L, 1),
      Row(1003L, 1),
      Row(1004L, 1),
      Row(1005L, 1),
      Row(1006L, 1)
    )
    
    val giftUnlockData = (
      // 用户1002: 累计收益≥100元（模拟100+次解锁）
      (100 to 250).map(i => Row(1002L, (1000L + i), i.toLong, 1704067200000L, "2024-01-01")) ++

      // 用户1001: 中等收益（50次解锁）
      (300 to 350).map(i => Row(1001L, (2000L + i), i.toLong, 1704067200000L, "2024-01-01")) ++

      // 用户1003: 低收益（20次解锁）
      (400 to 420).map(i => Row(1003L, (3000L + i), i.toLong, 1704067200000L, "2024-01-01")) ++

      // 用户1004: 中等收益（60次解锁）
      (500 to 560).map(i => Row(1004L, (4000L + i), i.toLong, 1704067200000L, "2024-01-01")) ++

      // 用户1005: 高收益（120次解锁）
      (600 to 720).map(i => Row(1005L, (5000L + i), i.toLong, 1704067200000L, "2024-01-01")) ++

      // 用户1006: 极低收益（5次解锁）
      (800 to 805).map(i => Row(1006L, (6000L + i), i.toLong, 1704067200000L, "2024-01-01"))
    ).toSeq

    // 创建Mock数据
    val mockInputData = Seq(
      MockData("lofter_dm", "ads_paid_post_blog_level_detail_di", blogLevelDetailData, blogLevelDetailSchema),
      MockData("lofter_dm", "ads_paid_post_premium_scoring_all_round_di", premiumScoringData, premiumScoringSchema),
      MockData("lofter_db_dump", "ods_db_trade_pay_grade_nd", payGradeData, payGradeSchema),
      MockData("lofter", "dwd_gift_post_unlock_dd", giftUnlockData, giftUnlockSchema)
    )

    // 创建 schemaOverrides 以定义目标表结构
    val schemaOverrides = Map(
      "lofter_dm.ads_par_creator_gift_level_internship_task_dd" -> MockTableDef("lofter_dm", "ads_par_creator_gift_level_internship_task_dd",
        outputSchema.fields.map(f => MockColumn(f.name, f.dataType)).toSeq, Seq("dt"))
    )

    // 执行测试
    val result = testJob(
      "ads-paid-post/ads_par_creator_gift_level_internship_task_dd.job",
      mockInputData = mockInputData,
      schemaOverrides = schemaOverrides
    )
    
    // 验证测试结果
    printJobReport(result)
    assertJobSuccess(result)
    assertJobProducesRows(result, minRows = 1)
    
    withOutputTable(result) { df =>
      val allRows = df.collect().sortBy(_.getLong(0))
      
      println("=== 试用期门槛任务测试结果 ===")
      allRows.foreach { row =>
        val userId = row.getLong(0)
        val t1Status = row.getInt(7)  // t1_status
        val t2Status = row.getInt(8)  // t2_status
        val t3Status = row.getInt(9)  // t3_status
        val t4Status = row.getInt(10) // t4_status
        val finalStatus = row.getInt(11) // status
        println(s"用户ID: $userId, T1(回礼≥3): $t1Status, T2(质量分≥120): $t2Status, T3(收益): $t3Status, T4(预警≤2): $t4Status, 最终状态: $finalStatus")
      }
      
      // 验证满足所有条件的用户
      val passedUsers = allRows.filter(_.getInt(11) == 1).map(_.getLong(0)).toSet
      println(s"通过试用期门槛的用户: $passedUsers")
      
      // 用户1001: 全部条件满足 - 有效回礼5篇（≥3），质量分125分（≥120），篇均收益25元（≥20），预警文章1篇（≤2）
      passedUsers should contain(1001L)
      
      // 用户1004: 边界条件满足 - 有效回礼3篇（刚好≥3），质量分120分（刚好≥120），篇均收益20元（刚好≥20），预警文章2篇（刚好≤2）
      passedUsers should contain(1004L)
      
      // 用户1005: 边界条件满足 - 有效回礼3篇（刚好≥3），质量分125分（≥120），篇均收益25元（≥20），预警文章2篇（刚好≤2）
      passedUsers should contain(1005L)
      
      // 验证未满足条件的用户
      val failedUsers = allRows.filter(_.getInt(11) == 0).map(_.getLong(0)).toSet
      println(s"未通过试用期门槛的用户: $failedUsers")
      
      // 用户1002: 仅满足T3条件（累计收益≥100元），但T1（回礼2篇<3）、T2（质量分100<120）、T4（预警3篇>2）不满足
      failedUsers should contain(1002L)
      
      // 用户1003: 满足T1（回礼4篇≥3）、T2（质量分130≥120），但T3（收益15<20且累计<100）、T4（预警3篇>2）不满足
      failedUsers should contain(1003L)
      
      // 用户1006: 所有条件都不满足 - 回礼1篇<3，质量分80<120，收益5<20且累计<100，预警5篇>2
      failedUsers should contain(1006L)

      // 详细验证各项条件
      println("=== 详细条件验证 ===")
      
      allRows.foreach { row =>
        val userId = row.getLong(0)
        val t1Count = row.getInt(1)
        val t2Percentile = row.getDouble(2)
        val t3Income = row.getLong(4)
        val t3AvgIncome = row.getDouble(5)
        val t4WarnCount = row.getInt(6)
        val t1Status = row.getInt(7)
        val t2Status = row.getInt(8)
        val t3Status = row.getInt(9)
        val t4Status = row.getInt(10)
        
        println(s"用户$userId:")
        println(s"  T1-有效回礼: ${t1Count}篇 (≥3?) -> 状态: $t1Status")
        println(s"  T2-质量分: ${t2Percentile}分 (≥120?) -> 状态: $t2Status")
        println(s"  T3-收益: 累计${t3Income}次解锁, 篇均${t3AvgIncome}元 (≥100次 OR ≥20元?) -> 状态: $t3Status")
        println(s"  T4-预警: ${t4WarnCount}篇 (≤2?) -> 状态: $t4Status")
        println()
      }
      
      // 验证总数合理性
      val totalUsers = allRows.length
      println(s"总用户数: $totalUsers")
      totalUsers should be >= 3 // 至少有用户1001、1004、1005满足条件
      
      // 验证状态计算逻辑正确性
      allRows.foreach { row =>
        val t1Status = row.getInt(7)
        val t2Status = row.getInt(8)
        val t3Status = row.getInt(9)
        val t4Status = row.getInt(10)
        val finalStatus = row.getInt(11)
        
        // 最终状态应该是所有条件的最小值（AND逻辑）
        val expectedFinalStatus = Math.min(Math.min(t1Status, t2Status), Math.min(t3Status, t4Status))
        finalStatus should equal(expectedFinalStatus)
      }

      println("=== 测试通过: 试用期门槛任务逻辑验证成功 ===")
    }
  }
}