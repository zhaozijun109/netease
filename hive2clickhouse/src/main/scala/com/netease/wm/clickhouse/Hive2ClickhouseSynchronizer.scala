package com.netease.wm.clickhouse

import clickhouseConfig._
import com.github.nscala_time.time.Imports.DateTime
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.slf4j.{Logger, LoggerFactory}

import java.sql.{Connection, DriverManager}
import java.util.concurrent._
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.util.control.NonFatal

case class SyncConfig(tableName: String, barrierTime: Int, syncMode: Int) {
  def isSyncReady: Boolean = DateTime.now().toString("HHmm").toInt >= barrierTime
}

class Hive2ClickhouseSynchronizer(val dt: String, autoFullSyncFallback: Boolean = false) {
  val SYNC_LEASE_PERIOD: Long = 120 * 1000L // 2 minutes
  val SYNC_LEASE_REFRESH_INTERVAL: Long = 90 * 1000L // 1.5 minutes
  val SYNC_TIMEOUT: Long = 1200 * 1000L // 20 minutes
  val AUTO_FALLBACK_MAX_SIZE: Long = 1024L * 1024L * 1024L // 1G

  private val LOG: Logger = LoggerFactory.getLogger(classOf[Hive2ClickhouseSynchronizer])

  val syncExecutor = Executors.newSingleThreadScheduledExecutor()

  lazy val tableConfig: Map[String, SyncConfig] = {
    val conn  = getConn
    val querySyncConfig =
      s"""
         |select table_name as tableName, sync_barrier_time as barrierTime, sync_mode as syncMode
         |from lofter.hive_sync_config
         |where channel = 'lofter'
         |""".stripMargin

    val resultSet = conn.createStatement().executeQuery(querySyncConfig)
    val configs = Iterator.continually(resultSet.next())
      .takeWhile(identity)
      .map(_ => SyncConfig(resultSet.getString(1), resultSet.getInt(2), resultSet.getInt(3)))
      .toList
    conn.close()
    configs.map(s => s.tableName -> s).toMap
  }

  lazy val syncedTables: Set[String] = {
    val conn  = getConn
    val querySyncedTables = s"""
       |select table
       |from system.parts
       |where database='hive' and partition = '$dt' and active and rows > 0
       |group by table
       |""".stripMargin
    val resultSet = conn.createStatement().executeQuery(querySyncedTables)
    val tables = Iterator.continually(resultSet.next())
      .takeWhile(identity)
      .map(_ => resultSet.getString(1))
      .toList
    conn.close()
    tables.toSet
  }

  private def getConn(url: String): Connection = {
    Class.forName(clickHouseDriverName)
    DriverManager.getConnection(url, clickHouseUser, clickHousePassword)
  }

  private def getConn: Connection = getConn(getMasterClickhouseJdbcUrl)

  // clickhouse table lock
  private def applyOrRefreshTableUpdateToken(table: String, uuidOption: Option[String] = None): Option[String] = {
    val conn = getConn
    val uuid = uuidOption.getOrElse(java.util.UUID.randomUUID.toString)
    val applyTokenSql =
      s"""
         |insert into lofter.hive_sync_worklog
         |with (
         |  select count(1)
         |  from lofter.hive_sync_worklog final
         |  where table_name = '$table' and action = 'sync_lease' and uuid != '$uuid' and
         |        lease_end_time > toUnixTimestamp64Milli(now64())
         |) as pending_leases,
         |$SYNC_LEASE_PERIOD as lease_period
         |select '$dt', '$uuid', '$table', 'sync_lease', toUnixTimestamp64Milli(now64()) + lease_period
         |from system.one array join if(pending_leases > 0, [], [1]) as apply_lease
         |""".stripMargin

    conn.createStatement().executeUpdate(applyTokenSql)
    val result = conn.createStatement().executeQuery(s"select count(1) v from lofter.hive_sync_worklog final where uuid = '$uuid'")
    val retVal = if(result.next() && result.getLong(1) > 0) Some(uuid) else None
    conn.close()
    retVal
  }

  private def releaseUpdateToken(table: String, uuid: String): Unit = {
    val conn  = getConn
    val endTokenPeriodSql =
      s"""
         |insert into lofter.hive_sync_worklog values ('$dt', '$uuid', '$table', 'sync_lease_end', toUnixTimestamp64Milli(now64()))
         |""".stripMargin

    conn.createStatement().executeUpdate(endTokenPeriodSql)
    conn.close()
  }

  private def markSyncDone(table: String, dt: String): Unit = {
    val conn  = getConn
    val uuid = java.util.UUID.randomUUID.toString
    val endTokenPeriodSql =
      s"""
         |insert into lofter.hive_sync_worklog values ('$dt', '$uuid', '$table', 'sync_done', toUnixTimestamp64Milli(now64()))
         |""".stripMargin

    conn.createStatement().executeUpdate(endTokenPeriodSql)
    conn.close()
  }

  def getConfiguredTables(): Set[String] = tableConfig.map(_._1).toSet

  def isTableSynced(table: String): Boolean = {
    if(isTableSyncModeFull(table)) {
      val conn = getConn
      val queryTableSynced =
        s"""
         |select *
         |from lofter.hive_sync_worklog final
         |where table_name = '$table' and action = 'sync_done' and dt = '$dt'
         |""".stripMargin
      val resultSet = conn.createStatement().executeQuery(queryTableSynced)
      val returnVal = resultSet.next()
      conn.close()
      returnVal
    } else {
      syncedTables(table)
    }
  }

  def isTableReadyForSync(table: String): Boolean = {
    tableConfig.get(table).map(_.isSyncReady).getOrElse(true)
  }

  def isTableSyncModeFull(table: String): Boolean = {
    tableConfig.get(table).map(_.syncMode).exists(_ == 1)
  }

  def getTableEstimateSize(table: String): Long = {
    val conn = getConn
    val queryTableSize = s"""
                            |select sum(data_compressed_bytes) as total
                            |from system.parts
                            |where database='hive' and active and
                            |      table = '$table'
                            |""".stripMargin
    val resultSet = conn.createStatement().executeQuery(queryTableSize)
    val returnVal = if(resultSet.next()) resultSet.getLong(1) else 0L
    conn.close()
    returnVal
  }

  protected def syncHiveTableToClickhouse(sourceDB: String, sourceTable: String,
                                          destDB: String, workingTable: String,
                                          uuid: String, spark: SparkSession, fullMode: Boolean = false): Unit = {

    val refreshUpdateTokenTask = new Runnable {
      def run() = {
        applyOrRefreshTableUpdateToken(sourceTable, Some(uuid))
      }
    }

    val refreshTokenFuture = syncExecutor.scheduleAtFixedRate(refreshUpdateTokenTask, SYNC_LEASE_REFRESH_INTERVAL , SYNC_LEASE_REFRESH_INTERVAL, TimeUnit.MILLISECONDS)

    import scala.concurrent.ExecutionContext.Implicits.global

    val result = Future {
      if(fullMode) {
        val writeSql =
         s"""
            |insert into table clickhouse.$destDB.$workingTable
            |select * from $sourceDB.$sourceTable
            |""".stripMargin

        spark.sql(writeSql)
      } else {
        val writeSql =
          s"""
             |insert into table clickhouse.$destDB.$workingTable
             |select * from $sourceDB.$sourceTable where dt = '$dt'
             |""".stripMargin

        spark.sql(writeSql)
      }
    }

    try {
      Await.result(result, Duration(SYNC_TIMEOUT, TimeUnit.MILLISECONDS))
    } catch {
      case e: scala.concurrent.TimeoutException =>
        LOG.error("sync execution for table {} time out({} seconds)", sourceTable, SYNC_TIMEOUT)
        throw e
    }
    finally {
      refreshTokenFuture.cancel(true)
    }

  }

  def syncTable(sourceDB: String, sourceTable: String, destDB: String, spark: SparkSession): Unit = {
    if(isTableSyncModeFull(sourceTable)) {
      doSyncTable(sourceDB, sourceTable, destDB, spark, fullMode = true)
    } else {
      try {
        doSyncTable(sourceDB, sourceTable, destDB, spark, fullMode = false)
      } catch {
        case NonFatal(e) =>
          LOG.error("sync incrementally failed for table " + sourceTable, e)
          if(autoFullSyncFallback && getTableEstimateSize(sourceTable) < AUTO_FALLBACK_MAX_SIZE) {
            LOG.error("retry fully sync for table " + sourceTable)
            doSyncTable(sourceDB, sourceTable, destDB, spark, fullMode = true)
          } else {
            throw new RuntimeException(e)
          }
      }
    }
  }

  def syncTable(sourceDB: String, sourceTable: String, destDB: String, spark: SparkSession, fullMode: Boolean): Unit = {
      try {
        doSyncTable(sourceDB, sourceTable, destDB, spark, fullMode)
      } catch {
        case NonFatal(e) =>
          LOG.error("sync incrementally failed for table " + sourceTable, e)
          if(autoFullSyncFallback) {
            LOG.error("retry fully sync for table " + sourceTable)
            // retry fullMode=true
            doSyncTable(sourceDB, sourceTable, destDB, spark, fullMode = true)
          } else {
            throw new RuntimeException(e)
          }
      }
  }

  private def generateSyncTempTable(sourceTable: String): String = {
    val uuid = java.util.UUID.randomUUID.toString
    s"_sync_tmp_${sourceTable}_$uuid"
  }

  protected def doSyncTable(sourceDB: String, sourceTable: String, destDB: String, spark: SparkSession, fullMode: Boolean): Unit = {
    LOG.info("applying update token for table {}", sourceTable)
    val uuid = applyOrRefreshTableUpdateToken(sourceTable)
    if(uuid.nonEmpty) {
      try {
        val workingTable = s"_sync_tmp_${sourceTable}"
        LOG.info("got update token, start syncing table {}", sourceTable)
        preSyncTable(sourceDB, sourceTable, destDB, workingTable, fullMode)
        syncHiveTableToClickhouse(sourceDB, sourceTable, destDB, workingTable, uuid.get, spark, fullMode)
        postSyncTable(sourceDB, sourceTable, destDB, workingTable, fullMode)
        LOG.info("end syncing table {}", sourceTable)
      } finally {
        try {
          releaseUpdateToken(sourceTable, uuid.get)
        } catch {
          case NonFatal(e) =>
            throw new RuntimeException("can't release update token for table " + sourceTable, e)
        }
      }
    } else {
      LOG.warn("failed to apply update token for table {}", sourceTable)
    }  }

  def preSyncTable(sourceDB: String, sourceTable: String, destDB: String, destWorkingTable: String, fullMode: Boolean = false): Unit = {
    val conn = getConn
    if(fullMode) {
      conn.createStatement().execute(s"drop table if exists $destDB.$destWorkingTable")
      conn.createStatement().execute(s"create table $destDB.$destWorkingTable Engine=MergeTree ORDER BY tuple() partition by dt SETTINGS storage_policy = 'policy1' as select toDate(assumeNotNull(dt)) as dt, * except dt from jdbc('$hiveUrl', 'select * from $sourceDB.$sourceTable where 0=1')")
      conn.createStatement().execute(s"create table if not exists $destDB.$sourceTable Engine=MergeTree ORDER BY tuple() partition by dt SETTINGS storage_policy = 'policy1' as select toDate(assumeNotNull(dt)) as dt, * except dt from jdbc('$hiveUrl', 'select * from $sourceDB.$sourceTable where 0=1')")
    } else {
      conn.createStatement().execute(s"drop table if exists $destDB.$destWorkingTable")
      conn.createStatement().execute(s"create table $destDB.$destWorkingTable Engine=MergeTree ORDER BY tuple() partition by dt SETTINGS storage_policy = 'policy1' as select toDate(assumeNotNull(dt)) as dt, * except dt from jdbc('$hiveUrl', 'select * from $sourceDB.$sourceTable where 0=1')")
      conn.createStatement().execute(s"create table if not exists $destDB.$sourceTable Engine=MergeTree ORDER BY tuple() partition by dt SETTINGS storage_policy = 'policy1' as select toDate(assumeNotNull(dt)) as dt, * except dt from jdbc('$hiveUrl', 'select * from $sourceDB.$sourceTable where 0=1')")
    }
    conn.close()
  }

  def postSyncTable(sourceDB: String, sourceTable: String, destDB: String, destWorkingTable: String, fullMode: Boolean = false): Unit = {
    if(fullMode) {
      val conn = getConn
      conn.createStatement().execute(s"exchange tables $destDB.$sourceTable and $destDB.$destWorkingTable")
      conn.createStatement().execute(s"drop table $destDB.$destWorkingTable")
      conn.close()
      markSyncDone(sourceTable, dt)
    } else {
      val conn = getConn
      conn.createStatement().execute(s"alter table $destDB.$sourceTable drop partition '$dt' ")
      conn.createStatement().execute(s"alter table $destDB.$destWorkingTable move partition '$dt' to table $destDB.$sourceTable ")
      conn.createStatement().execute(s"drop table $destDB.$destWorkingTable")
      conn.close()
    }

    getSlaveClickhouseJdbcUrl.foreach { clickHouseJdbcUrl =>
      val conn: Connection = getConn(clickHouseJdbcUrl)
      if(fullMode) {
        conn.createStatement().execute(s"drop view if exists $destDB.$sourceTable")
        conn.createStatement().execute(s"create view $destDB.$sourceTable as select * from remote('$clickHouseTableHost', '$destDB', '$sourceTable', '$clickHouseUser', '$clickHousePassword')")
      } else {
        conn.createStatement().execute(s"create view if not exists $destDB.$sourceTable as select * from remote('$clickHouseTableHost', '$destDB', '$sourceTable', '$clickHouseUser', '$clickHousePassword')")
      }

      conn.close()
    }
  }

}
