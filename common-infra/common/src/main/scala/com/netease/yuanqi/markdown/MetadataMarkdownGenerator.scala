package com.netease.yuanqi.markdown

import com.netease.yuanqi.metaservice.{MammutMetaService, TableInfoV3}

import java.io.{File, PrintWriter}
import java.text.SimpleDateFormat
import java.util.{Calendar, Date}
import scala.collection.mutable

/**
 * 元数据 Markdown 生成器
 *
 * 遍历 lofter_dm、lofter、lofter_db_dump、rec、vc、vc_dm 六个库，
 * 调用 MammutMetaService 的 getTableList 获取所有表名，
 * 再逐表调用 getTableDetailV3(datasourceType="hive") 获取 V3 元数据，
 * 按库生成 Markdown 文件到 output/ 目录，用于 AI 训练学习。
 */
object MetadataMarkdownGenerator {

  /** 默认目标数据库列表 */
  private val DEFAULT_DATABASES: List[String] = List("lofter_dm", "lofter", "lofter_db_dump", "rec", "vc", "vc_dm")

  /** 默认输出目录 */
  private val DEFAULT_OUTPUT_DIR: String = "output"

  /** 最近活跃表的时间窗口（天数） */
  private val RECENT_DAYS: Int = 30

  /** lastModifiedTime 日期格式 */
  private val DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"

  /**
   * 解析 lastModifiedTime 字符串为 Date 对象。
   * 格式为 yyyy-MM-dd HH:mm:ss（如 "2026-03-01 10:30:00"）。
   */
  private def parseLastModifiedTime(timeStr: Option[String]): Option[Date] = {
    timeStr.flatMap { s =>
      try {
        val sdf = new SimpleDateFormat(DATE_FORMAT)
        Some(sdf.parse(s.trim))
      } catch {
        case _: Exception => None
      }
    }
  }

  /**
   * 判断表是否在最近 N 天内有修改。
   * 如果 lastModifiedTime 为空或解析失败，默认保留该表（避免误删）。
   */
  private def isRecentlyModified(tableInfo: TableInfoV3, days: Int = RECENT_DAYS): Boolean = {
    val lastModified = parseLastModifiedTime(
      tableInfo.tableMetaInfo.flatMap(_.lastModifiedTime)
    )
    lastModified match {
      case Some(date) =>
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, -days)
        val cutoff = cal.getTime
        date.after(cutoff)
      case None =>
        // 无法解析时间，保守起见保留
        true
    }
  }

  private def printUsage(): Unit = {
    println(
      """Usage: MetadataMarkdownGenerator [databases] [outputDir]
        |
        |  databases  - 逗号分隔的数据库名列表（可选）
        |               默认: lofter_dm,lofter,lofter_db_dump,rec,vc,vc_dm
        |  outputDir  - Markdown 文件输出目录（可选）
        |               默认: output
        |
        |示例:
        |  MetadataMarkdownGenerator
        |  MetadataMarkdownGenerator lofter_dm,lofter
        |  MetadataMarkdownGenerator lofter_dm,lofter /tmp/metadata_output
        |""".stripMargin)
  }

  def main(args: Array[String]): Unit = {
    // 解析参数：args(0)=逗号分隔的库名列表，args(1)=输出目录
    val databases: List[String] = if (args.length >= 1 && args(0).nonEmpty)
      args(0).split(",").map(_.trim).filter(_.nonEmpty).toList
    else DEFAULT_DATABASES

    val outputDir: String = if (args.length >= 2 && args(1).nonEmpty)
      args(1).trim
    else DEFAULT_OUTPUT_DIR

    // 打印用法提示和实际参数
    printUsage()
    println(s"[INFO] 目标数据库: ${databases.mkString(", ")}")
    println(s"[INFO] 输出目录: $outputDir")

    val service = new MammutMetaService

    try {
      // 确保输出目录存在
      val dir = new File(outputDir)
      if (!dir.exists()) {
        dir.mkdirs()
        println(s"[INFO] 已创建输出目录: ${dir.getAbsolutePath}")
      }

      databases.foreach { db =>
        println(s"\n${"=" * 60}")
        println(s"[INFO] 开始处理数据库: $db")
        println(s"${"=" * 60}")

        try {
          // 1. 获取该库下所有表名
          println(s"[INFO] 正在获取 $db 的表列表...")
          val tableItems = service.getTableList(db)
          val tableNames = tableItems.flatMap(_.table)
          println(s"[INFO] $db 共有 ${tableNames.size} 张表")

          if (tableNames.isEmpty) {
            println(s"[WARN] $db 库下没有找到任何表，跳过")
          } else {
            // 2. 逐表获取 V3 元数据
            val tableDetails = mutable.ListBuffer.empty[TableInfoV3]
            val failedTables = mutable.ListBuffer.empty[String]

            tableNames.zipWithIndex.foreach { case (tableName, idx) =>
              val progress = s"[${idx + 1}/${tableNames.size}]"
              try {
                print(s"  $progress 获取 $tableName ... ")
                val detail = service.getTableDetailV3(db, tableName, "hive")
                tableDetails += detail
                println("✓")
              } catch {
                case e: Exception =>
                  failedTables += tableName
                  println(s"✗ (${e.getMessage})")
              }
            }

            println(s"[INFO] $db 成功获取 ${tableDetails.size} 张表元数据，失败 ${failedTables.size} 张")
            if (failedTables.nonEmpty) {
              println(s"[WARN] 失败的表: ${failedTables.mkString(", ")}")
            }

            // 3. 按 lastModifiedTime 过滤，只保留最近一个月内有变化的表
            val allTables = tableDetails.toList
            val recentTables = allTables.filter(isRecentlyModified(_))
            val skippedTables = allTables.filterNot(isRecentlyModified(_))

            println(s"[INFO] $db 最近 $RECENT_DAYS 天内有变化的表: ${recentTables.size} 张，" +
              s"已过滤掉 ${skippedTables.size} 张长期未更新的表")
            if (skippedTables.nonEmpty) {
              println(s"[INFO] 被过滤的表: ${skippedTables.flatMap(_.table).mkString(", ")}")
            }

            // 4. 生成 Markdown 文件
            if (recentTables.nonEmpty) {
              val outputFile = new File(dir, s"$db.md")
              generateMarkdown(db, recentTables, failedTables.toList, outputFile,
                totalTableCount = tableNames.size, filteredCount = skippedTables.size)
              println(s"[INFO] 已生成 Markdown 文件: ${outputFile.getAbsolutePath}")
            } else {
              println(s"[WARN] $db 过滤后没有表需要生成 Markdown")
            }
          }
        } catch {
          case e: Exception =>
            println(s"[ERROR] 处理数据库 $db 时发生错误: ${e.getMessage}")
            e.printStackTrace()
        }
      }

      println(s"\n${"=" * 60}")
      println("[INFO] 全部处理完成！")
      println(s"${"=" * 60}")

    } finally {
      service.close()
    }
  }

  /**
   * 将一个库的所有表元数据生成为 Markdown 文件。
   *
   * @param db           数据库名
   * @param tables       该库的所有 TableInfoV3 列表
   * @param failedTables 获取失败的表名列表
   * @param outputFile   输出文件
   */
  private def generateMarkdown(db: String,
                                tables: List[TableInfoV3],
                                failedTables: List[String],
                                outputFile: File,
                                totalTableCount: Int = 0,
                                filteredCount: Int = 0): Unit = {
    val writer = new PrintWriter(outputFile, "UTF-8")
    try {
      // 文件标题
      writer.println(s"# 数据库: $db")
      writer.println()
      writer.println(s"> 本文档包含数据库 `$db` 中**最近 $RECENT_DAYS 天内有更新**的 ${tables.size} 张表的元数据信息，用于 AI 训练学习。")
      if (filteredCount > 0 || failedTables.nonEmpty) {
        val parts = mutable.ListBuffer.empty[String]
        if (filteredCount > 0) parts += s"$filteredCount 张表因超过 $RECENT_DAYS 天未更新已被过滤"
        if (failedTables.nonEmpty) parts += s"${failedTables.size} 张表元数据获取失败"
        writer.println(s"> （库内共 $totalTableCount 张表，${parts.mkString("，")}）")
      }
      writer.println()

      // 表目录（TOC）
      writer.println("## 表目录")
      writer.println()
      tables.sortBy(_.table.getOrElse("")).zipWithIndex.foreach { case (t, idx) =>
        val tableName = t.table.getOrElse("unknown")
        val desc = t.tableMetaInfo.flatMap(_.description).filter(_.nonEmpty).getOrElse("无描述")
        writer.println(s"${idx + 1}. [${tableName}](#${tableName}) - $desc")
      }
      writer.println()

      if (failedTables.nonEmpty) {
        writer.println(s"> ⚠️ 以下 ${failedTables.size} 张表的元数据获取失败: ${failedTables.mkString(", ")}")
        writer.println()
      }

      writer.println("---")
      writer.println()

      // 每张表的详细信息
      tables.sortBy(_.table.getOrElse("")).foreach { tableInfo =>
        writeTableSection(writer, db, tableInfo)
      }

    } finally {
      writer.close()
    }
  }

  /** 大表判断阈值：10GB */
  private val LARGE_TABLE_THRESHOLD_BYTES: Long = 10L * 1024 * 1024 * 1024 // 10GB

  /**
   * 将 totalSize 字符串解析为字节数 Long。
   * API 返回的 totalSize 是带单位的可读格式（如 "84.8G"、"100M"、"1.5T"、"500K"），
   * 也可能是纯数字字节数（如 "22681684727"）。
   */
  private def parseTotalSizeBytes(sizeStr: Option[String]): Option[Long] = {
    sizeStr.flatMap { raw =>
      val s = raw.trim.toUpperCase
      if (s.isEmpty) return None
      try {
        // 尝试匹配带单位的格式：数字 + 单位后缀
        val pattern = """^([\d.]+)\s*([KMGT]?B?)$""".r
        s match {
          case pattern(numStr, unit) =>
            val num = numStr.toDouble
            val bytes = unit match {
              case "K" | "KB" => (num * 1024).toLong
              case "M" | "MB" => (num * 1024 * 1024).toLong
              case "G" | "GB" => (num * 1024 * 1024 * 1024).toLong
              case "T" | "TB" => (num * 1024 * 1024 * 1024 * 1024).toLong
              case "B" | ""   => num.toLong
              case _          => num.toLong
            }
            Some(bytes)
          case _ =>
            // 纯数字 fallback
            Some(s.toDouble.toLong)
        }
      } catch {
        case _: Exception => None
      }
    }
  }

  /**
   * 将字节数格式化为可读的大小字符串（如 "21.13 GB"）。
   */
  private def formatSize(bytes: Long): String = {
    if (bytes < 1024L) s"$bytes B"
    else if (bytes < 1024L * 1024) f"${bytes / 1024.0}%.2f KB"
    else if (bytes < 1024L * 1024 * 1024) f"${bytes / (1024.0 * 1024)}%.2f MB"
    else if (bytes < 1024L * 1024 * 1024 * 1024) f"${bytes / (1024.0 * 1024 * 1024)}%.2f GB"
    else f"${bytes / (1024.0 * 1024 * 1024 * 1024)}%.2f TB"
  }

  /**
   * 写入单张表的 Markdown 段落。
   */
  private def writeTableSection(writer: PrintWriter, db: String, tableInfo: TableInfoV3): Unit = {
    val tableName = tableInfo.table.getOrElse("unknown")
    val meta = tableInfo.tableMetaInfo

    // 解析表大小
    val totalSizeRaw = meta.flatMap(_.totalSize)
    val totalSizeBytes: Option[Long] = parseTotalSizeBytes(totalSizeRaw)
    // 当解析后字节数为 0 时视为无效，显示 N/A
    val totalSizeDisplay = if (totalSizeBytes.contains(0L)) "N/A"
        else totalSizeRaw.filter(_.nonEmpty)
          .orElse(totalSizeBytes
          .map(formatSize))
          .getOrElse("N/A")
    val isLargeTable = totalSizeBytes.exists(_ >= LARGE_TABLE_THRESHOLD_BYTES)
    val isPartTable = meta.flatMap(_.partTable).getOrElse(false)

    // 表名标题
    writer.println(s"## $tableName")
    writer.println()

    // 基本信息
    writer.println("### 基本信息")
    writer.println()
    writer.println(s"| 属性 | 值 |")
    writer.println(s"|------|------|")
    writer.println(s"| **数据库** | `$db` |")
    writer.println(s"| **表名** | `$tableName` |")
    writer.println(s"| **描述** | ${meta.flatMap(_.description).filter(_.nonEmpty).getOrElse("无描述")} |")
    writer.println(s"| **Owner** | ${meta.flatMap(_.owner).orElse(meta.flatMap(_.ownerPrincipal)).getOrElse("N/A")} |")
    writer.println(s"| **表类型** | ${meta.flatMap(_.tableType).getOrElse("N/A")} |")
    writer.println(s"| **表大小** | $totalSizeDisplay |")
    writer.println(s"| **是否分区表** | ${if (isPartTable) "是" else "否"} |")
    writer.println()

    // 大表提示
    if (isLargeTable && isPartTable) {
      writer.println(s"> ⚠️ **【重要提示】** 该表大小为 $totalSizeDisplay，属于大表且为分区表。" +
        s"**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），" +
        s"否则将扫描全量数据，导致查询超时或资源浪费。")
      writer.println()
    } else if (isLargeTable && !isPartTable) {
      writer.println(s"> ⚠️ **【性能提示】** 该表大小为 $totalSizeDisplay，属于大表但非分区表。" +
        s"查询该表时请注意性能，建议增加合理的 WHERE 条件以减少扫描的数据量，避免全表扫描。")
      writer.println()
    }

    // 字段详情
    val columns = meta.map(_.columnInfos).getOrElse(Nil)
    if (columns.nonEmpty) {
      writer.println("### 字段详情")
      writer.println()
      writer.println(s"共 ${columns.size} 个字段：")
      writer.println()
      writer.println("| 序号 | 字段名 | 类型 | 是否主键 | 描述 |")
      writer.println("|------|--------|------|----------|------|")

      columns.zipWithIndex.foreach { case (col, idx) =>
        val colName = col.columnName.getOrElse("N/A")
        val colType = col.columnType.getOrElse("N/A")
        val isPK = col.primaryKey.map(if (_) "✓" else "").getOrElse("")
        val desc = escapeMarkdown(col.description.filter(_.nonEmpty).getOrElse(""))
        writer.println(s"| ${idx + 1} | `$colName` | `$colType` | $isPK | $desc |")
      }
      writer.println()
    } else {
      writer.println("### 字段详情")
      writer.println()
      writer.println("暂无字段信息。")
      writer.println()
    }

    writer.println("---")
    writer.println()
  }

  /**
   * 转义 Markdown 表格中可能破坏格式的字符。
   */
  private def escapeMarkdown(text: String): String = {
    text
      .replace("|", "\\|")
      .replace("\n", " ")
      .replace("\r", "")
  }
}
