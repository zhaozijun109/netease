package com.netease.lofter.etl.sparksqltest

import org.scalatest.funsuite.AnyFunSuite

class SqlRewriteIntegrationTest extends AnyFunSuite {

  test("complete SQL rewrite workflow") {
    // 模拟多种历史分区读取模式
    val testCases = Seq(
      // 案例1：完整的范围查询（dt < X and dt >= Y）
      """
insert overwrite table lofter_dm.ads_creator_gift_open_entry_dd partition(dt = '2024-01-01')
select userId from (
    select userId from lofter_dm.ads_creator_gift_open_entry_dd
    where dt < '2024-01-01' and dt >= '2023-12-03'
) t
""".trim,

      // 案例2：只有小于查询（dt < X）
      """
insert overwrite table lofter_dm.test_table partition(dt = '2024-01-01')
select userId from (
    select userId from lofter_dm.test_table
    where dt < '2024-01-01'
) t
""".trim,

      // 案例3：小于等于查询（dt <= X）
      """
insert overwrite table lofter_dm.test_table2 partition(dt = '2024-01-01')
select userId from (
    select userId from lofter_dm.test_table2
    where dt<='2023-12-31'
) t
""".trim
    )

    println("=== 测试多种历史分区读取模式 ===")
    testCases.zipWithIndex.foreach { case (sql, index) =>
      println(s"案例 ${index + 1}:")
      val analysis = TableExtractor.analyze(sql)
      println(s"   Self references: ${analysis.selfReferences}")

      if (analysis.selfReferences.nonEmpty) {
        val rewritten = SqlRewriter.rewriteForSelfReference(sql, analysis.selfReferences)
        if (rewritten != sql) {
          println(s"   ✅ SQL 重写成功!")
          assert(rewritten.contains("_history"), "重写后的SQL应该包含历史表名")
        } else {
          println(s"   ❌ SQL 重写失败")
          fail(s"案例${index + 1}: SQL重写没有生效")
        }
      } else {
        fail(s"案例${index + 1}: 没有检测到自引用")
      }
    }
    
    println("=== 集成测试完成 ===")
  }

}