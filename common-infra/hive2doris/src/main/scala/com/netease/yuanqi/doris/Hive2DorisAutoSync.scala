package com.netease.yuanqi.doris

import com.netease.yuanqi.config.DorisConfig
import com.netease.yuanqi.doris.config.LoadConfig
import com.netease.yuanqi.doris.synchronizer.{Hive2DorisSynchronizer, SyncMode}
import com.netease.yuanqi.doris.util.Args
import com.netease.yuanqi.metaservice.MammutMetaService
import org.apache.spark.sql.SparkSession
import org.slf4j.{Logger, LoggerFactory}

import java.util.concurrent.{Executors, TimeUnit}
import scala.collection.mutable.ArrayBuffer
import scala.util.control.NonFatal

// ────────────────────────────────────────────────────────────────────────────────
// Hive → Doris Automatic Batch Sync
//
// Discovers incremental tables in a Hive database via MammutMetaService,
// filters by naming convention (suffix pattern) and include/exclude rules,
// then syncs each table to Doris using Auto mode (delta detection based on
// partition updateTime).
//
// This is a single-pass application — no polling loop.
// ────────────────────────────────────────────────────────────────────────────────

private class Hive2DorisAutoSync

object Hive2DorisAutoSync {

  private val LOG: Logger = LoggerFactory.getLogger(classOf[Hive2DorisAutoSync])

  /**
   * Regex matching incremental / snapshot table name suffixes.
   *
   * Recognised suffixes:
   *   - `_di` / `_wi` / `_mi`  — daily / weekly / monthly incremental
   *   - `_dd` / `_wd` / `_md`  — daily / weekly / monthly full-snapshot (dimension)
   *   - `_<N>d`                 — N-day rolling window (e.g. `_7d`, `_30d`)
   */
  private val DEFAULT_SUFFIX_PATTERN = """.*_(di|wi|mi|dd|wd|md|[0-9]+d)$"""

  // ─── Entry Point ──────────────────────────────────────────────────────────────

  /**
   * == Arguments ==
   *
   * {{{
   *   --hive-db          Source Hive database (required)
   *   --doris-db         Target Doris database (default: DorisConfig.database)
   *   --doris-url        Doris JDBC URL (default: DorisConfig.jdbcUrl)
   *   --doris-user       Doris user (default: DorisConfig.user)
   *   --doris-password   Doris password (default: DorisConfig.password)
   *   --partition-col    Partition column name (default: "dt")
   *   --includes         Comma-separated table names or keywords used as a whitelist filter
   *                      on suffix-pattern-matched tables. Only pattern-matched tables that
   *                      also match an include entry will be synced.
   *                      If --includes is NOT specified, NO pattern-matched tables will be synced
   *                      (only historyLoadTables from LoadConfig are imported).
   *                      Each entry is matched in two ways:
   *                        1) Exact match  — entry equals a table name
   *                        2) Contains match — entry is a substring of a table name (keyword mode)
   *                      (e.g. --includes "user_profile,_report_" will keep only pattern-matched
   *                       tables whose name equals `user_profile` OR contains `_report_`).
   *   --excludes         Comma-separated substrings; any table whose name contains a keyword is excluded
   *                      (e.g. "_wide_table_,_tmp_")
   *   --suffix-pattern   Custom regex for table suffix matching
   *                      (default: .*_(di|wi|mi|dd|wd|md|[0-9]+d)$)
   *   --parallelism      Number of tables to sync concurrently (default: 1)
   * }}}
   *
   * == Example ==
   *
   * {{{
   *   spark-submit --class com.netease.yuanqi.hive2doris.Hive2DorisAutoSync \
   *     hive2doris-assembly.jar \
   *     --hive-db lofter_dm \
   *     --doris-db lofter_doris \
   *     --includes "dim_user_base,_report_" \
   *     --excludes "_wide_table_,_tmp_" \
   *     --parallelism 4
   * }}}
   */
  def main(args: Array[String]): Unit = {
    LOG.info("╔══════════════════════════════════════════════════════════════╗")
    LOG.info("║          Hive2Doris AutoSync — Starting                      ║")
    LOG.info("╚══════════════════════════════════════════════════════════════╝")

    // ── Parse arguments ─────────────────────────────────────────────────────
    val pargs = Args(args)

    val hiveDb       = pargs.required("hive-db")
    val dorisDb      = pargs.optional("doris-db").getOrElse(DorisConfig.database)
    val partitionCol = pargs.optional("partition-col").getOrElse("dt")

    val includes: Set[String] = pargs.optional("includes")
      .getOrElse("_report_").split(",").map(_.trim).filterNot(_.isEmpty).toSet
    val excludes: Set[String] = pargs.optional("excludes")
      .getOrElse("").split(",").map(_.trim).filterNot(_.isEmpty).toSet

    val suffixPattern = pargs.optional("suffix-pattern").getOrElse(DEFAULT_SUFFIX_PATTERN).r
    val parallelism   = pargs.optional("parallelism").map(_.toInt).getOrElse(1)

    LOG.info(s"Arguments parsed:")
    LOG.info(s"  hive-db         = $hiveDb")
    LOG.info(s"  doris-db        = $dorisDb")
    LOG.info(s"  partition-col   = $partitionCol")
    LOG.info(s"  includes        = ${if (includes.nonEmpty) includes.mkString(", ") else "(none)"}")
    LOG.info(s"  excludes        = ${if (excludes.nonEmpty) excludes.mkString(", ") else "(none)"}")
    LOG.info(s"  suffix-pattern  = ${suffixPattern.pattern.pattern()}")
    LOG.info(s"  parallelism     = $parallelism")

    // ── Build SparkSession ──────────────────────────────────────────────────
    val spark = SparkSession.builder()
      .appName(s"Hive2DorisAutoSync[$hiveDb→$dorisDb]")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.sql.storeAssignmentPolicy", "ANSI")
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .config("spark.speculation", "false")
      .enableHiveSupport()
      .getOrCreate()

    LOG.info(s"SparkSession created: ${spark.sparkContext.applicationId}")

    // ── Discover tables via MammutMetaService ───────────────────────────────
    val metaService = new MammutMetaService()
    val allTableItems = metaService.getTableList(hiveDb)
    val allTableNames = allTableItems.flatMap(_.table).toSet

    LOG.info(s"Discovered ${allTableNames.size} tables in Hive database '$hiveDb' via MammutMetaService")

    // ── Filter chain ────────────────────────────────────────────────────────
    //
    //   Step 1: suffix pattern → patternMatched (coarse screening)
    //   Step 2: includes filter → keep only tables in patternMatched that match
    //           an include keyword (exact or contains); if no includes specified,
    //           result is empty (no pattern-matched table will be synced).
    //   Step 3: merge with historyLoadTables (always imported)
    //   Step 4: remove excludes (substring match)

    // Step 1: Match suffix pattern (e.g. _di, _dd, _7d, ...)
    val patternMatched: Set[String] = allTableNames.filter { t =>
      suffixPattern.pattern.matcher(t).matches()
    }
    LOG.info(s"Step 1 — Suffix pattern matched: ${patternMatched.size} tables")

    // Step 2: Apply includes as a whitelist filter on patternMatched.
    //         If --includes is not specified, no pattern-matched tables are kept.
    //         Each include entry is tried as:
    //           a) exact table name match
    //           b) substring (contains) keyword match
    val includeFiltered: Set[String] = if (includes.nonEmpty) {
      val matched = patternMatched.filter { t =>
        includes.exists(keyword => t == keyword || t.contains(keyword))
      }
      val unusedKeywords = includes.filterNot { keyword =>
        patternMatched.exists(t => t == keyword || t.contains(keyword))
      }
      if (unusedKeywords.nonEmpty) {
        LOG.warn(s"Include keywords matched no pattern-matched tables in '$hiveDb': ${unusedKeywords.mkString(", ")}")
      }
      matched
    } else {
      LOG.info("No --includes specified; pattern-matched tables will NOT be synced (only historyLoadTables)")
      Set.empty[String]
    }
    LOG.info(s"Step 2 — After includes filter: ${includeFiltered.size} tables " +
      s"(from ${patternMatched.size} pattern-matched)")

    // Step 3: Merge with historyLoadTables (always imported regardless of includes)
    //         根据 --hive-db 自动选择对应业务线的 load 表集合：
    //           - hive-db 含 "lofter" → lofterHistoryLoadTables
    //           - hiveDb  含 "vc"     → vcHistoryLoadTables（预留）
    //           - 其他                → 空集合
    val historyLoadTables = LoadConfig.getHistoryLoadTables(hiveDb)
    val merged: Set[String] = includeFiltered ++ historyLoadTables
    LOG.info(s"Step 3 — After merge with historyLoadTables: ${merged.size} tables " +
      s"(includeFiltered=${includeFiltered.size}, history=${historyLoadTables.size}, hiveDb=$hiveDb)")

    // Step 4: Apply excludes filter (substring match)
    val candidateTables: List[String] = merged.filterNot { t =>
      excludes.exists(keyword => t.contains(keyword))
    }.toList.sorted
    LOG.info(s"Step 4 — Final candidate tables (${candidateTables.size}): ${candidateTables.mkString(", ")}")

    if (candidateTables.isEmpty) {
      LOG.info("No tables to sync. Exiting.")
      spark.close()
      return
    }

    // ── Build synchronizer ──────────────────────────────────────────────────
    val synchronizer = new Hive2DorisSynchronizer(
      hiveDatabase  = hiveDb,
      dorisDatabase = dorisDb,
      partitionCol  = if (partitionCol.nonEmpty) Some(partitionCol) else None,
      metaService   = metaService
    )

    // ── Pre-sync: 恢复上次残留的 sync_running 记录 ──────────────────────────
    // 无论上次进程是正常退出、超时、kill -9、OOM 还是 YARN preemption，
    // 只要 update_time 超过 LEASE_REFRESH_INTERVAL_SEC 未刷新，就将 sync_running 恢复为 sync_failed，
    // 使得本次调度能重新导入这些分区。
    synchronizer.ensureWorklogTable()
    val recovered = synchronizer.recoverExpiredRunningTasks()
    if (recovered > 0) {
      LOG.info(s"Pre-sync cleanup: recovered $recovered stale running tasks from previous runs")
    }

    // ── Sync tables ─────────────────────────────────────────────────────────
    val failedTables  = new ArrayBuffer[String]()
    val skippedTables = new ArrayBuffer[String]()
    var successCount  = 0

    try {
      if (parallelism <= 1) {
        // Serial execution
        candidateTables.foreach { table =>
          syncOneTable(table, synchronizer, spark, failedTables, skippedTables) match {
            case true  => successCount += 1
            case false => // already tracked in failedTables/skippedTables
          }
        }
      } else {
        // Parallel execution
        LOG.info(s"Using parallel execution with $parallelism threads")
        val executor = Executors.newFixedThreadPool(parallelism)
        val lock = new Object

        candidateTables.foreach { table =>
          executor.submit(new Runnable {
            override def run(): Unit = {
              val success = syncOneTable(table, synchronizer, spark, failedTables, skippedTables)
              if (success) {
                lock.synchronized { successCount += 1 }
              }
            }
          })
        }

        executor.shutdown()
        val finished = executor.awaitTermination(4, TimeUnit.HOURS)

        if (!finished) {
          LOG.warn("Thread pool did not terminate within 4 hours — forcing shutdown and recovering stale tasks")
          executor.shutdownNow()
          // 恢复过期的 sync_running 记录为 sync_failed
          // 当前 uuid → 直接改为 sync_failed
          // 其他 uuid → 仅当 update_time 超过 4 小时未刷新时改为 sync_failed
          val recovered = synchronizer.recoverExpiredRunningTasks(expireSeconds=4 * 60 * 60)
          LOG.warn(s"Recovered $recovered expired running tasks after timeout")
        }
      }
    } finally {
      spark.close()
      LOG.info(s"SparkSession closed")
    }

    // ── Summary ─────────────────────────────────────────────────────────────
    val uniqueFailedTables = failedTables.distinct
    LOG.info("╔══════════════════════════════════════════════════════════════╗")
    LOG.info("║          Hive2Doris AutoSync — Summary                     ║")
    LOG.info("╠══════════════════════════════════════════════════════════════╣")
    LOG.info(s"║  Total candidates:  ${candidateTables.size}")
    LOG.info(s"║  Success:           $successCount")
    LOG.info(s"║  Skipped (no data): ${skippedTables.size}")
    LOG.info(s"║  Failed:            ${uniqueFailedTables.size}")
    if (uniqueFailedTables.nonEmpty) {
      LOG.info(s"║  Failed tables: ${uniqueFailedTables.mkString(", ")}")
    }
    LOG.info("╚══════════════════════════════════════════════════════════════╝")

    if (uniqueFailedTables.nonEmpty) {
      throw new RuntimeException(
        s"Hive2Doris AutoSync failed for ${uniqueFailedTables.size} table(s): ${uniqueFailedTables.mkString(", ")}"
      )
    }
  }

  // ─── Helper: Sync one table ─────────────────────────────────────────────────

  /**
   * Sync a single table using Auto mode.
   *
   * @return true if sync succeeded (or table was skipped due to no changed partitions),
   *         false if an error occurred.
   */
  private def syncOneTable(
    table: String,
    synchronizer: Hive2DorisSynchronizer,
    spark: SparkSession,
    failedTables: ArrayBuffer[String],
    skippedTables: ArrayBuffer[String]
  ): Boolean = {
    try {
      LOG.info("────────────────────────────────────────")
      LOG.info(s"AutoSync: syncing table $table (mode=auto)")
      synchronizer.syncTable(
        table      = table,
        partitions = List.empty,
        syncMode   = SyncMode.Auto,
        spark      = spark
      )
      true
    } catch {
      case NonFatal(e) =>
        LOG.error(s"AutoSync: failed to sync table $table: ${e.getMessage}", e)
        failedTables.synchronized { failedTables.append(table) }
        false
    }
  }
}