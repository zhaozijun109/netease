package com.netease.yuanqi.test

import com.netease.yuanqi.config.DorisConfig
import com.netease.yuanqi.doris.schema.{DorisDDLGenerator, DorisTypeMapper}
import com.netease.yuanqi.doris.util.Args
import com.netease.yuanqi.doris.synchronizer.SyncMode
import com.netease.yuanqi.metaservice.FieldInfo

import java.sql.{Connection, DriverManager}

// ────────────────────────────────────────────────────────────────────────────────
// Hive2Doris 功能验证测试类
//
// 不引入任何测试框架，通过 main 方法直接调用各模块方法，
// 使用 assert 验证结果，println 输出测试进度。
//
// 覆盖范围：
//   1. DorisTypeMapper — 类型映射正确性
//   2. DorisDDLGenerator — DDL 生成正确性
//   3. SyncMode — 模式解析正确性
//   4. Args — CLI 参数解析正确性
//   5. DorisTypeMapper.splitTopLevelComma — 嵌套泛型分割正确性
//   6. DorisDDLExecute — DDL 实际执行到 Doris（DROP + CREATE）
//
// 运行方式：
//   spark-submit --class com.netease.yuanqi.hive2doris.Hive2DorisTest hive2doris-assembly.jar
//   或 IDE 中直接运行 main 方法
// ────────────────────────────────────────────────────────────────────────────────

private class Hive2DorisTest

object Hive2DorisTest {

  private var passed = 0
  private var failed = 0

  /** 单条断言验证 */
  private def check(testName: String, condition: => Boolean): Unit = {
    try {
      if (condition) {
        println(s"  ✓ $testName")
        passed += 1
      } else {
        println(s"  ✗ $testName — FAILED (condition is false)")
        failed += 1
      }
    } catch {
      case e: Throwable =>
        println(s"  ✗ $testName — EXCEPTION: ${e.getMessage}")
        failed += 1
    }
  }

  /** 验证预期抛出异常 */
  private def checkThrows[T <: Throwable](testName: String, expectedType: Class[T])(block: => Any): Unit = {
    try {
      block
      println(s"  ✗ $testName — FAILED (no exception thrown)")
      failed += 1
    } catch {
      case e: Throwable if expectedType.isInstance(e) =>
        println(s"  ✓ $testName (caught: ${e.getClass.getSimpleName}: ${e.getMessage})")
        passed += 1
      case e: Throwable =>
        println(s"  ✗ $testName — WRONG EXCEPTION: ${e.getClass.getSimpleName}: ${e.getMessage}")
        failed += 1
    }
  }

  def main(args: Array[String]): Unit = {
    println("╔══════════════════════════════════════════════════════════════╗")
    println("║          Hive2Doris — Functional Tests                     ║")
    println("╚══════════════════════════════════════════════════════════════╝")
    println()

    testDorisTypeMapper()
    testSplitTopLevelComma()
    testDorisDDLGenerator()
    testSyncMode()
    testArgs()
    testDorisDDLExecute()

    println()
    println("╔══════════════════════════════════════════════════════════════╗")
    println(s"║  Results: $passed passed, $failed failed" + " " * (39 - passed.toString.length - failed.toString.length) + "║")
    println("╚══════════════════════════════════════════════════════════════╝")

    if (failed > 0) {
      throw new RuntimeException(s"$failed test(s) failed!")
    }
  }

  // ════════════════════════════════════════════════════════════════════════════
  //  1. DorisTypeMapper
  // ════════════════════════════════════════════════════════════════════════════

  private def testDorisTypeMapper(): Unit = {
    println("── DorisTypeMapper ──────────────────────────────────────────")

    // 简单类型
    check("boolean → BOOLEAN",       DorisTypeMapper.mapType("boolean") == "BOOLEAN")
    check("tinyint → TINYINT",       DorisTypeMapper.mapType("tinyint") == "TINYINT")
    check("smallint → SMALLINT",     DorisTypeMapper.mapType("smallint") == "SMALLINT")
    check("int → INT",               DorisTypeMapper.mapType("int") == "INT")
    check("integer → INT",           DorisTypeMapper.mapType("integer") == "INT")
    check("bigint → BIGINT",         DorisTypeMapper.mapType("bigint") == "BIGINT")
    check("float → FLOAT",           DorisTypeMapper.mapType("float") == "FLOAT")
    check("double → DOUBLE",         DorisTypeMapper.mapType("double") == "DOUBLE")
    check("string → STRING",         DorisTypeMapper.mapType("string") == "STRING")
    check("binary → STRING",         DorisTypeMapper.mapType("binary") == "STRING")
    check("date → DATE",             DorisTypeMapper.mapType("date") == "DATE")
    check("timestamp → DATETIME",    DorisTypeMapper.mapType("timestamp") == "DATETIME")
    check("decimal → DECIMAL(27, 9)", DorisTypeMapper.mapType("decimal") == "DECIMAL(27, 9)")

    // 大小写不敏感
    check("STRING → STRING",         DorisTypeMapper.mapType("STRING") == "STRING")
    check("BigInt → BIGINT",         DorisTypeMapper.mapType("BigInt") == "BIGINT")

    // decimal(p, s)
    check("decimal(18,2) → DECIMAL(18, 2)", DorisTypeMapper.mapType("decimal(18,2)") == "DECIMAL(18, 2)")
    check("decimal(38,10) → DECIMAL(38, 10)", DorisTypeMapper.mapType("decimal(38,10)") == "DECIMAL(38, 10)")
    // 溢出截断
    check("decimal(50,20) → DECIMAL(38, 20)", DorisTypeMapper.mapType("decimal(50,20)") == "DECIMAL(38, 20)")

    // char / varchar
    check("char(10) → CHAR(10)",         DorisTypeMapper.mapType("char(10)") == "CHAR(10)")
    check("char(300) → CHAR(255)",       DorisTypeMapper.mapType("char(300)") == "CHAR(255)")  // 截断
    check("varchar(100) → VARCHAR(100)", DorisTypeMapper.mapType("varchar(100)") == "VARCHAR(100)")
    check("varchar(99999) → VARCHAR(65533)", DorisTypeMapper.mapType("varchar(99999)") == "VARCHAR(65533)")  // 截断

    // array
    check("array<string> → ARRAY<STRING>",
      DorisTypeMapper.mapType("array<string>") == "ARRAY<STRING>")
    check("array<int> → ARRAY<INT>",
      DorisTypeMapper.mapType("array<int>") == "ARRAY<INT>")

    // map
    check("map<string,int> → MAP<STRING, INT>",
      DorisTypeMapper.mapType("map<string,int>") == "MAP<STRING, INT>")

    // struct
    check("struct<a:int,b:string> → STRUCT<a:INT, b:STRING>",
      DorisTypeMapper.mapType("struct<a:int,b:string>") == "STRUCT<a:INT, b:STRING>")

    // 嵌套复合类型
    check("array<map<string,int>> → ARRAY<MAP<STRING, INT>>",
      DorisTypeMapper.mapType("array<map<string,int>>") == "ARRAY<MAP<STRING, INT>>")
    check("map<string,array<int>> → MAP<STRING, ARRAY<INT>>",
      DorisTypeMapper.mapType("map<string,array<int>>") == "MAP<STRING, ARRAY<INT>>")
    check("struct<a:int,b:array<string>> → STRUCT<a:INT, b:ARRAY<STRING>>",
      DorisTypeMapper.mapType("struct<a:int,b:array<string>>") == "STRUCT<a:INT, b:ARRAY<STRING>>")

    // 未知类型 fallback
    check("unknown_type → STRING",    DorisTypeMapper.mapType("unknown_type") == "STRING")

    println()
  }

  // ════════════════════════════════════════════════════════════════════════════
  //  2. splitTopLevelComma
  // ════════════════════════════════════════════════════════════════════════════

  private def testSplitTopLevelComma(): Unit = {
    println("── splitTopLevelComma ───────────────────────────────────────")

    check("simple: 'a, b' → [a, b]",
      DorisTypeMapper.splitTopLevelComma("a, b") == List("a", "b"))

    check("nested: 'string, array<int>' → [string, array<int>]",
      DorisTypeMapper.splitTopLevelComma("string, array<int>") == List("string", "array<int>"))

    check("deep nested: 'string, map<string, int>' → [string, map<string, int>]",
      DorisTypeMapper.splitTopLevelComma("string, map<string, int>") == List("string", "map<string, int>"))

    check("struct fields: 'a:int, b:map<string,int>, c:string'",
      DorisTypeMapper.splitTopLevelComma("a:int, b:map<string,int>, c:string") == List("a:int", "b:map<string,int>", "c:string"))

    check("single element: 'string' → [string]",
      DorisTypeMapper.splitTopLevelComma("string") == List("string"))

    println()
  }

  // ════════════════════════════════════════════════════════════════════════════
  //  3. DorisDDLGenerator
  // ════════════════════════════════════════════════════════════════════════════

  private def testDorisDDLGenerator(): Unit = {
    println("── DorisDDLGenerator ────────────────────────────────────────")

    // 构造测试用的 FieldInfo 列表
    val fields = List(
      FieldInfo(name = Some("user_id"),   fieldType = Some("bigint"),  comment = Some("用户ID"), partitionKey = Some(false)),
      FieldInfo(name = Some("user_name"), fieldType = Some("string"),  comment = Some("用户名"), partitionKey = Some(false)),
      FieldInfo(name = Some("score"),     fieldType = Some("double"),  comment = Some("评分"),   partitionKey = Some(false)),
      FieldInfo(name = Some("dt"),        fieldType = Some("string"),  comment = Some("日期"),   partitionKey = Some(true))
    )

    // 3.1 分区表 DDL
    val partDDL = DorisDDLGenerator.generateCreateTable(
      database     = "lofter_test",
      tableName    = "user_report_di",
      fields       = fields,
      partitionCol = Some("dt")
    )
    println(s"  [Partitioned DDL]:\n$partDDL\n")

    check("分区表 DDL 包含 CREATE TABLE",
      partDDL.contains("CREATE TABLE IF NOT EXISTS `lofter_test`.`user_report_di`"))
    check("分区表 DDL 包含 AUTO PARTITION BY RANGE",
      partDDL.contains("AUTO PARTITION BY RANGE"))
    check("分区表 DDL 包含 date_trunc(`dt`, 'day')",
      partDDL.contains("date_trunc(`dt`, 'day')"))
    check("分区表 DDL 包含 DUPLICATE KEY",
      partDDL.contains("DUPLICATE KEY"))
    check("分区表 DDL 包含 BUCKETS AUTO",
      partDDL.contains("BUCKETS AUTO"))
    check("分区表 DDL dt列是DATE NOT NULL且在第一位",
      partDDL.indexOf("`dt` DATE NOT NULL") < partDDL.indexOf("`user_id`"))
    check("分区表 DDL DUPLICATE KEY 以 dt 开头",
      partDDL.contains("DUPLICATE KEY(`dt`"))
    check("分区表 DDL user_id列是BIGINT类型",
      partDDL.contains("`user_id` BIGINT"))
    check("分区表 DDL user_name是STRING但被选为KEY → VARCHAR(4096)",
      partDDL.contains("`user_name` VARCHAR(4096)"))
    check("分区表 DDL 包含 replication_num",
      partDDL.contains("replication_num"))

    // 3.x STRING KEY 替换验证 — 含复杂类型的表
    val fieldsWithComplex = List(
      FieldInfo(name = Some("arr_col"),  fieldType = Some("array<string>"), comment = None, partitionKey = Some(false)),
      FieldInfo(name = Some("id"),       fieldType = Some("bigint"),        comment = None, partitionKey = Some(false)),
      FieldInfo(name = Some("name"),     fieldType = Some("string"),        comment = None, partitionKey = Some(false)),
      FieldInfo(name = Some("map_col"),  fieldType = Some("map<string,int>"), comment = None, partitionKey = Some(false))
    )
    val complexDDL = DorisDDLGenerator.generateCreateTable(
      database  = "lofter_test",
      tableName = "complex_table",
      fields    = fieldsWithComplex
    )
    println(s"  [Complex type DDL]:\n$complexDDL\n")
    check("复杂类型表: ARRAY 列不在 KEY 中",
      !complexDDL.split("DUPLICATE KEY\\(")(1).split("\\)")(0).contains("arr_col"))
    check("复杂类型表: MAP 列不在 KEY 中",
      !complexDDL.split("DUPLICATE KEY\\(")(1).split("\\)")(0).contains("map_col"))
    check("复杂类型表: id 在 KEY 中",
      complexDDL.split("DUPLICATE KEY\\(")(1).split("\\)")(0).contains("id"))
    check("复杂类型表: name 在 KEY 中且改为 VARCHAR(4096)",
      complexDDL.contains("`name` VARCHAR(4096)"))
    // KEY 列必须是列定义的有序前缀 → 不兼容列（ARRAY/MAP）排在 KEY 兼容列之后
    check("复杂类型表: id 列在 arr_col 列之前（KEY 兼容列优先）",
      complexDDL.indexOf("`id`") < complexDDL.indexOf("`arr_col`"))
    check("复杂类型表: name 列在 map_col 列之前（KEY 兼容列优先）",
      complexDDL.indexOf("`name`") < complexDDL.indexOf("`map_col`"))

    // 3.2 非分区表 DDL
    val fieldsNoPart = List(
      FieldInfo(name = Some("id"),   fieldType = Some("int"),    comment = None, partitionKey = Some(false)),
      FieldInfo(name = Some("name"), fieldType = Some("string"), comment = None, partitionKey = Some(false))
    )
    val noPartDDL = DorisDDLGenerator.generateCreateTable(
      database  = "lofter_test",
      tableName = "dim_table",
      fields    = fieldsNoPart
    )
    println(s"  [Non-partitioned DDL]:\n$noPartDDL\n")

    check("非分区表 DDL 不包含 AUTO PARTITION",
      !noPartDDL.contains("AUTO PARTITION"))
    check("非分区表 DDL 包含 DUPLICATE KEY",
      noPartDDL.contains("DUPLICATE KEY"))
    check("非分区表 DDL 包含 BUCKETS AUTO",
      noPartDDL.contains("BUCKETS AUTO"))

    // 3.3 动态清理属性
    val dynamicDDL = DorisDDLGenerator.generateCreateTable(
      database             = "lofter_test",
      tableName            = "big_table_di",
      fields               = fields,
      partitionCol         = Some("dt"),
      enableDynamicCleanup = true
    )
    check("动态清理 DDL 包含 dynamic_partition.enable = true",
      dynamicDDL.contains("dynamic_partition.enable") && dynamicDDL.contains("true"))
    check("动态清理 DDL 包含 dynamic_partition.start = -30",
      dynamicDDL.contains("dynamic_partition.start") && dynamicDDL.contains("-30"))

    println(s"  [dynamic_partition DDL]:\n$dynamicDDL\n")

    // 3.4 ADD PARTITION DDL
    val addPartDDL = DorisDDLGenerator.generateAddPartitionDDL(
      database     = "lofter_test",
      tableName    = "user_report_di",
      partitionCol = "dt",
      partitionVal = "2026-03-11"
    )
    println(s"  [ADD PARTITION DDL]: $addPartDDL\n")

    check("ADD PARTITION DDL 包含分区名 p20260311",
      addPartDDL.contains("p20260311"))
    check("ADD PARTITION DDL 包含范围 2026-03-11 ~ 2026-03-12",
      addPartDDL.contains("2026-03-11") && addPartDDL.contains("2026-03-12"))
    check("ADD PARTITION DDL 包含 IF NOT EXISTS",
      addPartDDL.contains("IF NOT EXISTS"))

    // 3.5 isPartitionedTable
    check("isPartitionedTable(fields, Some('dt')) == true",
      DorisDDLGenerator.isPartitionedTable(fields, Some("dt")))
    check("isPartitionedTable(fieldsNoPart, None) == false",
      !DorisDDLGenerator.isPartitionedTable(fieldsNoPart, None))

    // 3.6 shouldEnableDynamicCleanup (threshold = 10G)
    check("shouldEnableDynamicCleanup(10GB) == true",
      DorisDDLGenerator.shouldEnableDynamicCleanup(10L * 1024 * 1024 * 1024))
    check("shouldEnableDynamicCleanup(9GB) == false",
      !DorisDDLGenerator.shouldEnableDynamicCleanup(9L * 1024 * 1024 * 1024))
    check("shouldEnableDynamicCleanup(500MB) == false",
      !DorisDDLGenerator.shouldEnableDynamicCleanup(500L * 1024 * 1024))

    // 3.7 端到端: 分区数据 >= 10G → shouldEnableDynamicCleanup → generateCreateTable → 验证 dynamic_partition.start = -30
    {
      val partitionSizeBytes = 15L * 1024 * 1024 * 1024 // 15GB — 超过 10G 阈值
      val enableCleanup = DorisDDLGenerator.shouldEnableDynamicCleanup(partitionSizeBytes)
      check("E2E: 15GB 分区 → shouldEnableDynamicCleanup == true", enableCleanup)

      val e2eDDL = DorisDDLGenerator.generateCreateTable(
        database             = "lofter_test",
        tableName            = "big_partition_di",
        fields               = fields,
        partitionCol         = Some("dt"),
        enableDynamicCleanup = enableCleanup
      )
      println(s"  [E2E Dynamic Cleanup DDL]:\n$e2eDDL\n")

      check("E2E: DDL 包含 dynamic_partition.enable = true",
        e2eDDL.contains(""""dynamic_partition.enable" = "true""""))
      check("E2E: DDL 包含 dynamic_partition.start = -30",
        e2eDDL.contains(""""dynamic_partition.start" = "-30""""))
      check("E2E: DDL 包含 dynamic_partition.time_unit = day",
        e2eDDL.contains(""""dynamic_partition.time_unit" = "day""""))
      check("E2E: DDL 包含 dynamic_partition.end = 0",
        e2eDDL.contains(""""dynamic_partition.end" = "0""""))
      check("E2E: DDL 包含 dynamic_partition.prefix = p",
        e2eDDL.contains(""""dynamic_partition.prefix" = "p""""))

      // 反向验证: 分区数据 < 10G 时 dynamic_partition 仍启用，但不包含 start（不清理历史）
      val smallPartSize = 5L * 1024 * 1024 * 1024 // 5GB — 小于 10G 阈值
      val noCleanup = DorisDDLGenerator.shouldEnableDynamicCleanup(smallPartSize)
      check("E2E: 5GB 分区 → shouldEnableDynamicCleanup == false", !noCleanup)

      val smallDDL = DorisDDLGenerator.generateCreateTable(
        database             = "lofter_test",
        tableName            = "small_partition_di",
        fields               = fields,
        partitionCol         = Some("dt"),
        enableDynamicCleanup = noCleanup
      )
      check("E2E: 5GB DDL 包含 dynamic_partition.enable (生命周期管理始终启用)",
        smallDDL.contains("dynamic_partition.enable"))
      check("E2E: 5GB DDL 不包含 dynamic_partition.start (< 10G 不清理历史)",
        !smallDDL.contains("dynamic_partition.start"))
    }

    println()
  }

  // ════════════════════════════════════════════════════════════════════════════
  //  4. SyncMode
  // ════════════════════════════════════════════════════════════════════════════

  private def testSyncMode(): Unit = {
    println("── SyncMode ─────────────────────────────────────────────────")

    check("fromString('full') == Full",
      SyncMode.fromString("full") == SyncMode.Full)
    check("fromString('partition') == Partition",
      SyncMode.fromString("partition") == SyncMode.Partition)
    check("fromString('partition_range') == PartitionRange",
      SyncMode.fromString("partition_range") == SyncMode.PartitionRange)
    check("fromString('auto') == Auto",
      SyncMode.fromString("auto") == SyncMode.Auto)

    // 大小写不敏感
    check("fromString('FULL') == Full",
      SyncMode.fromString("FULL") == SyncMode.Full)
    check("fromString('Auto') == Auto",
      SyncMode.fromString("Auto") == SyncMode.Auto)

    // 非法值
    checkThrows("fromString('invalid') throws exception", classOf[IllegalArgumentException]) {
      SyncMode.fromString("invalid")
    }

    println()
  }

  // ════════════════════════════════════════════════════════════════════════════
  //  5. Args
  // ════════════════════════════════════════════════════════════════════════════

  private def testArgs(): Unit = {
    println("── Args ─────────────────────────────────────────────────────")

    // 基础解析
    val args1 = Args(Array("--table", "user_info", "--mode", "full", "--full"))
    check("required('table') == 'user_info'",
      args1.required("table") == "user_info")
    check("required('mode') == 'full'",
      args1.required("mode") == "full")
    check("boolean('full') == true",
      args1.boolean("full"))
    check("boolean('missing') == false",
      !args1.boolean("missing"))
    check("optional('date') == None",
      args1.optional("date").isEmpty)

    // optional 有值
    val args2 = Args(Array("--hive-db", "lofter_dm", "--date", "2026-03-11"))
    check("optional('date') == Some('2026-03-11')",
      args2.optional("date").contains("2026-03-11"))
    check("required('hive-db') == 'lofter_dm'",
      args2.required("hive-db") == "lofter_dm")

    // required 缺失抛异常
    checkThrows("required('missing-key') throws exception", classOf[IllegalArgumentException]) {
      args2.required("missing-key")
    }

    // 多值逗号分隔（由调用方 split）
    val args3 = Args(Array("--table", "t1,t2,t3", "--date", "2026-03-10,2026-03-11"))
    check("table 逗号分隔: split 后 3 个",
      args3.required("table").split(",").length == 3)
    check("date 逗号分隔: split 后 2 个",
      args3.required("date").split(",").length == 2)

    // 空参数
    val args4 = Args(Array.empty[String])
    check("空参数 boolean('any') == false",
      !args4.boolean("any"))
    check("空参数 optional('any') == None",
      args4.optional("any").isEmpty)

    println()
  }

  // ════════════════════════════════════════════════════════════════════════════
  //  6. DorisDDLExecute — 实际连接 Doris 执行 DROP + CREATE
  // ════════════════════════════════════════════════════════════════════════════

  private def testDorisDDLExecute(): Unit = {
    println("── DorisDDLExecute (Doris 实际执行) ─────────────────────────")

    val db = DorisConfig.database  // "lofter_test"

    // ── 测试表定义 ──────────────────────────────────────────────────────
    // (表名, fields, partitionCol, enableDynamicCleanup)
    val testTables: List[(String, List[FieldInfo], Option[String], Boolean)] = List(
      // 6.1 分区表（无动态清理）
      ("test_user_report_di", List(
        FieldInfo(name = Some("user_id"),   fieldType = Some("bigint"),  comment = Some("用户ID"), partitionKey = Some(false)),
        FieldInfo(name = Some("user_name"), fieldType = Some("string"),  comment = Some("用户名"), partitionKey = Some(false)),
        FieldInfo(name = Some("score"),     fieldType = Some("double"),  comment = Some("评分"),   partitionKey = Some(false)),
        FieldInfo(name = Some("dt"),        fieldType = Some("string"),  comment = Some("日期"),   partitionKey = Some(true))
      ), Some("dt"), false),

      // 6.2 非分区表
      ("test_dim_table", List(
        FieldInfo(name = Some("id"),   fieldType = Some("int"),    comment = None, partitionKey = Some(false)),
        FieldInfo(name = Some("name"), fieldType = Some("string"), comment = None, partitionKey = Some(false))
      ), None, false),

      // 6.3 含复杂类型的表（ARRAY, MAP）
      ("test_complex_table", List(
        FieldInfo(name = Some("arr_col"),  fieldType = Some("array<string>"),    comment = None, partitionKey = Some(false)),
        FieldInfo(name = Some("id"),       fieldType = Some("bigint"),           comment = None, partitionKey = Some(false)),
        FieldInfo(name = Some("name"),     fieldType = Some("string"),           comment = None, partitionKey = Some(false)),
        FieldInfo(name = Some("map_col"),  fieldType = Some("map<string,int>"),  comment = None, partitionKey = Some(false))
      ), None, false),

      // 6.4 分区表 + 动态清理（模拟分区 >= 10G 场景）
      ("test_big_partition_di", List(
        FieldInfo(name = Some("user_id"),   fieldType = Some("bigint"),  comment = Some("用户ID"), partitionKey = Some(false)),
        FieldInfo(name = Some("user_name"), fieldType = Some("string"),  comment = Some("用户名"), partitionKey = Some(false)),
        FieldInfo(name = Some("score"),     fieldType = Some("double"),  comment = Some("评分"),   partitionKey = Some(false)),
        FieldInfo(name = Some("dt"),        fieldType = Some("string"),  comment = Some("日期"),   partitionKey = Some(true))
      ), Some("dt"), true),

      // 6.5 全类型覆盖表
      ("test_all_types", List(
        FieldInfo(name = Some("col_bool"),      fieldType = Some("boolean"),           comment = None, partitionKey = Some(false)),
        FieldInfo(name = Some("col_tinyint"),    fieldType = Some("tinyint"),           comment = None, partitionKey = Some(false)),
        FieldInfo(name = Some("col_smallint"),   fieldType = Some("smallint"),          comment = None, partitionKey = Some(false)),
        FieldInfo(name = Some("col_int"),        fieldType = Some("int"),               comment = None, partitionKey = Some(false)),
        FieldInfo(name = Some("col_bigint"),     fieldType = Some("bigint"),            comment = None, partitionKey = Some(false)),
        FieldInfo(name = Some("col_float"),      fieldType = Some("float"),             comment = None, partitionKey = Some(false)),
        FieldInfo(name = Some("col_double"),     fieldType = Some("double"),            comment = None, partitionKey = Some(false)),
        FieldInfo(name = Some("col_decimal"),    fieldType = Some("decimal(18,2)"),     comment = None, partitionKey = Some(false)),
        FieldInfo(name = Some("col_char"),       fieldType = Some("char(50)"),          comment = None, partitionKey = Some(false)),
        FieldInfo(name = Some("col_varchar"),    fieldType = Some("varchar(200)"),      comment = None, partitionKey = Some(false)),
        FieldInfo(name = Some("col_string"),     fieldType = Some("string"),            comment = None, partitionKey = Some(false)),
        FieldInfo(name = Some("col_date"),       fieldType = Some("date"),              comment = None, partitionKey = Some(false)),
        FieldInfo(name = Some("col_timestamp"),  fieldType = Some("timestamp"),         comment = None, partitionKey = Some(false)),
        FieldInfo(name = Some("col_array"),      fieldType = Some("array<string>"),     comment = None, partitionKey = Some(false)),
        FieldInfo(name = Some("col_map"),        fieldType = Some("map<string,int>"),   comment = None, partitionKey = Some(false)),
        FieldInfo(name = Some("col_struct"),     fieldType = Some("struct<a:int,b:string>"), comment = None, partitionKey = Some(false))
      ), None, false)
    )

    // ── 获取 JDBC 连接 ──────────────────────────────────────────────────
    var conn: Connection = null
    try {
      conn = DriverManager.getConnection(
        s"${DorisConfig.jdbcUrl}/${db}",
        DorisConfig.user,
        DorisConfig.password
      )
      println(s"  ✓ JDBC 连接成功: ${DorisConfig.jdbcUrl}/${db}")
    } catch {
      case e: Throwable =>
        println(s"  ⚠ 无法连接 Doris (${DorisConfig.jdbcUrl}/${db}): ${e.getMessage}")
        println(s"  ⚠ 跳过 DorisDDLExecute 测试（需要 Doris 实例在线）")
        return
    }

    try {
      val stmt = conn.createStatement()

      for ((tableName, fields, partCol, enableDynamic) <- testTables) {
        // 1. 生成 CREATE TABLE DDL
        val createDDL = DorisDDLGenerator.generateCreateTable(
          database             = db,
          tableName            = tableName,
          fields               = fields,
          partitionCol         = partCol,
          enableDynamicCleanup = enableDynamic
        )

        // 2. 先执行 DROP TABLE IF EXISTS
        val dropDDL = s"DROP TABLE IF EXISTS `$db`.`$tableName`"
        check(s"DROP TABLE $tableName 执行成功", {
          stmt.execute(dropDDL)
          println(s"    ↳ $dropDDL")
          true
        })

        // 3. 执行 CREATE TABLE
        check(s"CREATE TABLE $tableName 执行成功", {
          stmt.execute(createDDL)
          println(s"    ↳ CREATE TABLE `$db`.`$tableName` — OK")
          true
        })

        // 4. 验证表已存在
        check(s"SHOW TABLES 确认 $tableName 存在", {
          val rs = stmt.executeQuery(s"SHOW TABLES FROM `$db` LIKE '$tableName'")
          val exists = rs.next()
          rs.close()
          exists
        })
      }

      // ── 清理：DROP 所有测试表 ──────────────────────────────────────
      println()
      println("  ── 清理测试表 ──")
      for ((tableName, _, _, _) <- testTables) {
        val dropDDL = s"DROP TABLE IF EXISTS `$db`.`$tableName`"
        check(s"清理 DROP $tableName", {
          stmt.execute(dropDDL)
          true
        })
      }

      stmt.close()
    } finally {
      if (conn != null) conn.close()
    }

    println()
  }
}
