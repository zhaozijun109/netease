package com.netease.yuanqi.doris

import com.netease.yuanqi.config.DorisConfig
import com.netease.yuanqi.doris.synchronizer.{Hive2DorisSynchronizer, SyncMode}
import com.netease.yuanqi.doris.util.Args
import com.netease.yuanqi.metaservice.MammutMetaService
import org.apache.spark.sql.SparkSession
import org.slf4j.{Logger, LoggerFactory}

import scala.util.control.NonFatal

// ────────────────────────────────────────────────────────────────────────────────
// Hive → Doris Manual / One-Shot Sync
//
// 用于临时数据修补的一次性同步入口。
// 串行执行指定表的同步，执行完毕即退出。
//
// 通过 --mode 显式指定同步模式，支持全部 4 种：
//   - full:             全量重刷（DROP + CREATE + 全量导入）
//   - partition:        指定分区补数（支持一个或多个日期，逗号分隔）
//   - partition_range:  分区范围补数（指定多个日期）
//   - auto:             自动发现当天变更分区并同步
// ────────────────────────────────────────────────────────────────────────────────

private class Hive2DorisManualSync

object Hive2DorisManualSync {

  private val LOG: Logger = LoggerFactory.getLogger(classOf[Hive2DorisManualSync])

  /**
   * == Arguments ==
   *
   * {{{
   *   --table            Comma-separated table names (required)
   *   --hive-db          Source Hive database (required)
   *   --mode             Sync mode: full / partition / partition_range / auto (required)
   *   --doris-db         Target Doris database (default: DorisConfig.database)
   *   --date             Partition date(s) to sync, comma-separated
   *                      (required for partition / partition_range; ignored for full / auto)
   *   --partition-col    Partition column name (default: "dt")
   * }}}
   *
   * == Usage ==
   *
   * 单分区补数：
   * {{{
   *   spark-submit --class com.netease.yuanqi.hive2doris.Hive2DorisManualSync \
   *     hive2doris-assembly.jar \
   *     --hive-db lofter_dm --doris-db lofter_doris \
   *     --table user_report_di \
   *     --mode partition --date 2026-03-11
   * }}}
   *
   * 多分区补数：
   * {{{
   *   spark-submit --class com.netease.yuanqi.hive2doris.Hive2DorisManualSync \
   *     hive2doris-assembly.jar \
   *     --hive-db lofter_dm --doris-db lofter_doris \
   *     --table user_report_di \
   *     --mode partition --date 2026-03-02,2026-03-01,2026-04-01
   * }}}
   *
   * 分区范围补数（闭区间 [start, end]，同步范围内所有 Hive 分区）：
   * {{{
   *   spark-submit --class com.netease.yuanqi.hive2doris.Hive2DorisManualSync \
   *     hive2doris-assembly.jar \
   *     --hive-db lofter_dm --doris-db lofter_doris \
   *     --table user_report_di,order_report_di \
   *     --mode partition_range --date 2026-03-01,2026-04-01
   * }}}
   *
   * 全量重刷：
   * {{{
   *   spark-submit --class com.netease.yuanqi.hive2doris.Hive2DorisManualSync \
   *     hive2doris-assembly.jar \
   *     --hive-db lofter_dm --doris-db lofter_doris \
   *     --table user_report_di \
   *     --mode full
   * }}}
   *
   * 自动发现当天变更分区：
   * {{{
   *   spark-submit --class com.netease.yuanqi.hive2doris.Hive2DorisManualSync \
   *     hive2doris-assembly.jar \
   *     --hive-db lofter_dm --doris-db lofter_doris \
   *     --table user_report_di,order_report_di \
   *     --mode auto
   * }}}
   */
  def main(args: Array[String]): Unit = {
    LOG.info("╔══════════════════════════════════════════════════════════════╗")
    LOG.info("║          Hive2Doris Sync — One-Shot Manual Sync              ║")
    LOG.info("╚══════════════════════════════════════════════════════════════╝")

    // ── Parse arguments ─────────────────────────────────────────────────────
    val pargs = Args(args)

    val hiveDb       = pargs.required("hive-db")
    val dorisDb      = pargs.optional("doris-db").getOrElse(DorisConfig.database)
    val tableArg     = pargs.optional("table").getOrElse("")
    val modeArg      = pargs.required("mode")
    val dateArg      = pargs.optional("date").getOrElse("")
    val partitionCol = pargs.optional("partition-col").getOrElse("dt")

    val tables = tableArg.split(",").map(_.trim).filter(_.nonEmpty)
    val dates  = dateArg.split(",").map(_.trim).filter(_.nonEmpty).toList

    // ── Resolve sync mode ───────────────────────────────────────────────────
    val syncMode = SyncMode.fromString(modeArg)

    // ── Validation ──────────────────────────────────────────────────────────
    require(tables.nonEmpty, "At least one table must be specified via --table")

    syncMode match {
      case SyncMode.Full =>
        if (dates.nonEmpty) LOG.warn("--date is ignored in full mode")
      case SyncMode.Partition =>
        require(dates.nonEmpty,
          "partition mode requires at least one --date value")
      case SyncMode.PartitionRange =>
        require(dates.size == 2,
          s"partition_range mode requires exactly two --date values (start,end), got ${dates.size}: ${dates.mkString(",")}")
      case SyncMode.Auto =>
        if (dates.nonEmpty) LOG.warn("--date is ignored in auto mode (partitions detected by updateTime)")
    }

    LOG.info(s"Arguments parsed:")
    LOG.info(s"  hive-db         = $hiveDb")
    LOG.info(s"  doris-db        = $dorisDb")
    LOG.info(s"  tables          = ${tables.mkString(", ")}")
    LOG.info(s"  mode            = $syncMode")
    LOG.info(s"  dates           = ${if (dates.nonEmpty) dates.mkString(", ") else "(N/A)"}")
    LOG.info(s"  partition-col   = $partitionCol")

    // ── Build SparkSession ──────────────────────────────────────────────────
    val spark = SparkSession.builder()
      .appName(s"Hive2DorisManualSync[${tables.mkString(",")}][$syncMode]")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.sql.storeAssignmentPolicy", "ANSI")
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .config("spark.speculation", "false")
      .enableHiveSupport()
      .getOrCreate()

    LOG.info(s"SparkSession created: ${spark.sparkContext.applicationId}")

    // Skip scheduling node
    if (tables.length == 1 && tables(0) == "true") {
      LOG.error("IllegalArgumentException requirement failed: At least one table must be specified via --table")
      LOG.info("╔══════════════════════════════════════════════════════════════╗")
      LOG.info("║          Hive2Doris Sync — Summary                           ║")
      LOG.info("╠══════════════════════════════════════════════════════════════╣")
      LOG.info("║  Total tables:  0                                            ║")
      LOG.info("║  Mode:          Ignore                                       ║")
      LOG.info("║  Success:       0                                            ║")
      LOG.info("║  Failed:        0                                            ║")
      LOG.info("╚══════════════════════════════════════════════════════════════╝")
      spark.close()
      return
    }

    // ── Build synchronizer ──────────────────────────────────────────────────
    val metaService = new MammutMetaService()
    val synchronizer = new Hive2DorisSynchronizer(
      hiveDatabase  = hiveDb,
      dorisDatabase = dorisDb,
      partitionCol  = if (partitionCol.nonEmpty) Some(partitionCol) else None,
      metaService   = metaService
    )

    // ── Serial sync ─────────────────────────────────────────────────────────
    var successCount = 0
    val failedTables = scala.collection.mutable.ArrayBuffer.empty[String]

    try {
      tables.foreach { table =>
        try {
          LOG.info("────────────────────────────────────────")
          LOG.info(s"Syncing table: $hiveDb.$table → $dorisDb.$table [$syncMode]")

          // full / auto 模式不传分区列表，由 Synchronizer 内部处理
          val partitions = syncMode match {
            case SyncMode.Full => List.empty[String]
            case SyncMode.Auto => List.empty[String]
            case _             => dates
          }

          synchronizer.syncTable(
            table      = table,
            partitions = partitions,
            syncMode   = syncMode,
            spark      = spark,
            force      = true  // ManualSync 用于临时修补数据，强制重新同步（忽略 worklog sync_done 检查）
          )

          successCount += 1
          LOG.info(s"✓ Table $table sync completed")
        } catch {
          case NonFatal(e) =>
            LOG.error(s"✗ Table $table sync failed: ${e.getMessage}", e)
            failedTables += table
        }
      }
    } finally {
      spark.close()
      LOG.info("SparkSession closed")
    }

    // ── Summary ─────────────────────────────────────────────────────────────
    LOG.info("╔══════════════════════════════════════════════════════════════╗")
    LOG.info("║          Hive2Doris Sync — Summary                         ║")
    LOG.info("╠══════════════════════════════════════════════════════════════╣")
    LOG.info(s"║  Total tables:  ${tables.length}")
    LOG.info(s"║  Mode:          $syncMode")
    LOG.info(s"║  Success:       $successCount")
    LOG.info(s"║  Failed:        ${failedTables.size}")
    if (failedTables.nonEmpty) {
      LOG.info(s"║  Failed tables: ${failedTables.mkString(", ")}")
    }
    LOG.info("╚══════════════════════════════════════════════════════════════╝")

    if (failedTables.nonEmpty) {
      throw new RuntimeException(
        s"Hive2DorisManualSync failed for ${failedTables.size} table(s): ${failedTables.mkString(", ")}"
      )
    }
  }
}