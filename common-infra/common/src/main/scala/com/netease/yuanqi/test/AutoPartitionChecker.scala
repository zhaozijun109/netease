package com.netease.yuanqi.test

import com.netease.yuanqi.config.DorisConfig
import com.netease.yuanqi.metaservice.MammutMetaService

import java.sql.{Connection, DriverManager}
import scala.collection.mutable

/**
 * Auto Partition 检查工具
 *
 * 功能：
 * 1. 通过 JDBC 连接 Doris lofter 库，遍历所有表的 SHOW CREATE TABLE，
 *    找出使用了 AUTO PARTITION 的表名
 * 2. 调用 MammutMetaService.getTableList 获取 Hive 中 lofter 和 lofter_dm 两个库的所有表名
 * 3. 对每个 Doris AUTO PARTITION 表，判断是否存在于 Hive 的 lofter 或 lofter_dm 库中
 * 4. 输出分类结果：存在于 Hive / 不存在于 Hive
 */
object AutoPartitionChecker {

  /** Doris 中要扫描的数据库 */
  private val DORIS_DATABASE = "lofter"

  /** Hive 中要查找的数据库列表 */
  private val HIVE_DATABASES = List("lofter", "lofter_dm")

  /** DROP 排除列表：精确匹配的表名（小写），这些表即使符合条件也不会被 DROP */
  private val EXCLUDE_TABLES: Set[String] = Set(
    "dwd_reward_product_exchange_detail_history_di",
    "ods_lofter_mda_online_di"
  )

  /** DROP 排除列表：前缀匹配，以这些前缀开头的表不会被 DROP */
  private val EXCLUDE_PREFIXES: List[String] = List(
    "ads_cp_"
  )

  /** 判断表名是否在排除列表中（精确匹配或前缀匹配） */
  private def isExcluded(tableName: String): Boolean = {
    val lower = tableName.toLowerCase
    EXCLUDE_TABLES.contains(lower) || EXCLUDE_PREFIXES.exists(p => lower.startsWith(p))
  }

  def main(args: Array[String]): Unit = {
    println(s"${"=" * 70}")
    println("  Auto Partition 检查工具")
    println(s"  Doris 扫描库: $DORIS_DATABASE")
    println(s"  Hive 查找库:  ${HIVE_DATABASES.mkString(", ")}")
    println(s"${"=" * 70}")
    println()

    // ─── Step 1: 从 Doris 查出使用 AUTO PARTITION 的表 ─────────────
    println("[Step 1] 从 Doris 查找使用 AUTO PARTITION 的表...")
    val autoPartTables = findAutoPartitionTables()
    println(s"[Step 1] 共找到 ${autoPartTables.size} 张使用 AUTO PARTITION 的表")
    if (autoPartTables.isEmpty) {
      println("[INFO] 未发现使用 AUTO PARTITION 的表，退出。")
      return
    }
    autoPartTables.zipWithIndex.foreach { case (t, i) =>
      println(s"  ${i + 1}. $DORIS_DATABASE.$t")
    }
    println()

    // ─── Step 2: 从 Hive 获取表名列表 ────────────────────────────
    println("[Step 2] 从 Hive (Mammut Meta) 获取表名列表...")
    val service = new MammutMetaService
    try {
      // db -> Set[tableName]
      val hiveTableMap = mutable.Map.empty[String, Set[String]]
      var totalHiveTables = 0

      HIVE_DATABASES.foreach { db =>
        print(s"  获取 Hive $db 库表列表... ")
        try {
          val items = service.getTableList(db)
          val names = items.flatMap(_.table).map(_.toLowerCase).toSet
          hiveTableMap(db) = names
          totalHiveTables += names.size
          println(s"✓ (${names.size} 张表)")
        } catch {
          case e: Exception =>
            hiveTableMap(db) = Set.empty
            println(s"✗ (${e.getMessage})")
        }
      }
      println(s"[Step 2] Hive 共获取 $totalHiveTables 张表名")
      println()

      // 合并所有 Hive 表名（用于快速查找）
      // 格式: tableName -> List[db1, db2, ...]
      val hiveTableToDb = mutable.Map.empty[String, mutable.ListBuffer[String]]
      hiveTableMap.foreach { case (db, tables) =>
        tables.foreach { t =>
          hiveTableToDb.getOrElseUpdate(t, mutable.ListBuffer.empty) += db
        }
      }

      // ─── Step 3: 交叉比对 ─────────────────────────────────────
      println("[Step 3] 交叉比对 Doris AUTO PARTITION 表在 Hive 中的存在情况...")
      println()

      val inHive = mutable.ListBuffer.empty[(String, List[String])]   // (tableName, hiveDbs)
      val notInHive = mutable.ListBuffer.empty[String]

      autoPartTables.foreach { tableName =>
        val lowerName = tableName.toLowerCase
        hiveTableToDb.get(lowerName) match {
          case Some(dbs) =>
            inHive += ((tableName, dbs.toList))
          case None =>
            notInHive += tableName
        }
      }

      // ─── Step 4: 输出结果 ─────────────────────────────────────
      println(s"${"=" * 70}")
      println("  比对结果汇总")
      println(s"${"=" * 70}")
      println(s"  AUTO PARTITION 表总数: ${autoPartTables.size}")
      println(s"  ✓ 存在于 Hive:        ${inHive.size}")
      println(s"  ✗ 不存在于 Hive:      ${notInHive.size}")
      println(s"${"=" * 70}")
      println()

      if (inHive.nonEmpty) {
        println(s"── ✓ 存在于 Hive 的表 (${inHive.size} 张) ──")
        println()
        println(f"  ${"序号"}%-6s ${"Doris 表名"}%-50s ${"Hive 所在库"}%s")
        println(s"  ${"-" * 6} ${"-" * 50} ${"-" * 30}")
        inHive.sortBy(_._1).zipWithIndex.foreach { case ((tableName, hiveDbs), idx) =>
          println(f"  ${idx + 1}%-6d ${s"$DORIS_DATABASE.$tableName"}%-50s ${hiveDbs.mkString(", ")}%s")
        }
        println()
      }

      if (notInHive.nonEmpty) {
        println(s"── ✗ 不存在于 Hive 的表 (${notInHive.size} 张) ──")
        println()
        println(f"  ${"序号"}%-6s ${"Doris 表名"}%s")
        println(s"  ${"-" * 6} ${"-" * 50}")
        notInHive.sorted.zipWithIndex.foreach { case (tableName, idx) =>
          println(f"  ${idx + 1}%-6d ${s"$DORIS_DATABASE.$tableName"}%s")
        }
        println()
      }

      // ─── Step 5: DROP 存在于 Hive 的 AUTO PARTITION 表（排除受保护的表）──
      if (inHive.nonEmpty) {
        // 按排除规则分离
        val allInHiveNames = inHive.map(_._1).toList
        val excludedTables = allInHiveNames.filter(isExcluded)
        val tablesToDrop = allInHiveNames.filterNot(isExcluded)

        println("[Step 5] 开始在 Doris 中 DROP 存在于 Hive 的 AUTO PARTITION 表...")
        println(s"  存在于 Hive 的表: ${allInHiveNames.size} 张")

        if (excludedTables.nonEmpty) {
          println(s"  ⛔ 排除不 DROP 的表: ${excludedTables.size} 张")
          excludedTables.foreach { t =>
            println(s"     - $DORIS_DATABASE.$t")
          }
        }

        println(s"  实际将 DROP 的表: ${tablesToDrop.size} 张")
        println()

        if (tablesToDrop.nonEmpty) {
          val dropResults = dropTablesInDoris(tablesToDrop)

          val succeeded = dropResults.count(_._2)
          val failed = dropResults.count(!_._2)

          println()
          println(s"${"=" * 70}")
          println("  DROP 结果汇总")
          println(s"${"=" * 70}")
          println(s"  存在于 Hive 的表:   ${allInHiveNames.size}")
          println(s"  ⛔ 排除的表:         ${excludedTables.size}")
          println(s"  实际 DROP 的表:      ${tablesToDrop.size}")
          println(s"  ✓ DROP 成功:         $succeeded")
          println(s"  ✗ DROP 失败:         $failed")
          println(s"${"=" * 70}")
          println()

          if (failed > 0) {
            println("── ✗ DROP 失败的表 ──")
            dropResults.filter(!_._2).foreach { case (name, _, msg) =>
              println(s"  $DORIS_DATABASE.$name - $msg")
            }
            println()
          }
        } else {
          println("  所有存在于 Hive 的表均在排除列表中，无需 DROP。")
          println()
        }
      } else {
        println("[Step 5] 没有需要 DROP 的表（所有 AUTO PARTITION 表均不存在于 Hive 中）。")
        println()
      }

      println(s"${"=" * 70}")
      println("[INFO] 全部完成！")
      println(s"${"=" * 70}")

    } finally {
      service.close()
    }
  }

  /**
   * 通过 JDBC 连接 Doris，遍历指定数据库的所有表，
   * 执行 SHOW CREATE TABLE 并检查 DDL 中是否包含 AUTO PARTITION。
   *
   * @return 使用了 AUTO PARTITION 的表名列表
   */
  private def findAutoPartitionTables(): List[String] = {
    val autoPartTables = mutable.ListBuffer.empty[String]

    var conn: Connection = null
    try {
      // 显式加载 MySQL JDBC 驱动，避免 SPI 机制在某些类加载器下失效
      Class.forName("com.mysql.cj.jdbc.Driver")

      val jdbcUrl = s"${DorisConfig.jdbcUrl}/${DORIS_DATABASE}"
      println(s"  连接 Doris: $jdbcUrl")
      conn = DriverManager.getConnection(jdbcUrl, DorisConfig.user, DorisConfig.password)

      // 1. 获取所有表名
      val tableNames = mutable.ListBuffer.empty[String]
      val showTablesStmt = conn.createStatement()
      try {
        val rs = showTablesStmt.executeQuery("SHOW TABLES")
        while (rs.next()) {
          tableNames += rs.getString(1)
        }
      } finally {
        showTablesStmt.close()
      }

      println(s"  Doris $DORIS_DATABASE 库共有 ${tableNames.size} 张表")

      // 2. 逐表检查 SHOW CREATE TABLE
      var checked = 0
      tableNames.foreach { tableName =>
        checked += 1
        if (checked % 50 == 0 || checked == tableNames.size) {
          println(s"  已检查 $checked/${tableNames.size} ...")
        }

        val stmt = conn.createStatement()
        try {
          val rs = stmt.executeQuery(s"SHOW CREATE TABLE `$tableName`")
          if (rs.next()) {
            val createDDL = rs.getString(2) // 第二列是建表语句
            if (createDDL != null && createDDL.toUpperCase.contains("AUTO PARTITION")) {
              autoPartTables += tableName
            }
          }
        } catch {
          case e: Exception =>
            println(s"  [WARN] 获取 $tableName 建表语句失败: ${e.getMessage}")
        } finally {
          stmt.close()
        }
      }

    } catch {
      case e: Exception =>
        println(s"  [ERROR] 连接 Doris 失败: ${e.getMessage}")
        e.printStackTrace()
    } finally {
      if (conn != null) {
        try { conn.close() } catch { case _: Exception => }
      }
    }

    autoPartTables.toList
  }

  /**
   * 在 Doris 中批量执行 DROP TABLE IF EXISTS。
   *
   * @param tableNames 要 DROP 的表名列表
   * @return List[(tableName, success, message)]
   */
  private def dropTablesInDoris(tableNames: List[String]): List[(String, Boolean, String)] = {
    val results = mutable.ListBuffer.empty[(String, Boolean, String)]

    var conn: Connection = null
    try {
      Class.forName("com.mysql.cj.jdbc.Driver")

      val jdbcUrl = s"${DorisConfig.jdbcUrl}/${DORIS_DATABASE}"
      println(s"  连接 Doris: $jdbcUrl")
      conn = DriverManager.getConnection(jdbcUrl, DorisConfig.user, DorisConfig.password)

      tableNames.zipWithIndex.foreach { case (tableName, idx) =>
        val progress = s"[${idx + 1}/${tableNames.size}]"
        val dropSQL = s"DROP TABLE IF EXISTS `$tableName`"
        val stmt = conn.createStatement()
        try {
          print(s"  $progress DROP TABLE `$tableName` ... ")
          stmt.execute(dropSQL)
          println("✓")
          results += ((tableName, true, "OK"))
        } catch {
          case e: Exception =>
            println(s"✗ (${e.getMessage})")
            results += ((tableName, false, e.getMessage))
        } finally {
          stmt.close()
        }
      }

    } catch {
      case e: Exception =>
        println(s"  [ERROR] 连接 Doris 失败: ${e.getMessage}")
        e.printStackTrace()
        // 所有表标记为失败
        tableNames.foreach { t =>
          if (!results.exists(_._1 == t)) {
            results += ((t, false, s"连接失败: ${e.getMessage}"))
          }
        }
    } finally {
      if (conn != null) {
        try { conn.close() } catch { case _: Exception => }
      }
    }

    results.toList
  }
}
