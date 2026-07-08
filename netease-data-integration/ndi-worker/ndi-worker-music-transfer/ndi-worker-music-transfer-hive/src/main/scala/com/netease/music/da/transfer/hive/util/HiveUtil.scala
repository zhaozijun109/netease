package com.netease.music.da.transfer.hive.util

import org.apache.hadoop.hive.conf.HiveConf
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient
import org.apache.hadoop.hive.metastore.api.Table
import org.apache.spark.sql.catalyst.TableIdentifier

import scala.util.Try

object HiveUtil {
  def createMSC(): HiveMetaStoreClient = {
    val conf = new HiveConf()
    new HiveMetaStoreClient(conf)
  }

  def closeMSCQuietly(msc: HiveMetaStoreClient): Unit = {
    Try {
      msc.close()
    }
  }

  def getTable(msc: HiveMetaStoreClient, table: TableIdentifier): Table = {
    msc.getTable(table.database.get, table.table)
  }

  def getOwner(msc: HiveMetaStoreClient, table: TableIdentifier): String = getTable(msc, table).getOwner

  def alertOwner(msc: HiveMetaStoreClient, table: TableIdentifier, owner: String): Boolean = {
    val tableMeta = getTable(msc, table)
    if (tableMeta.getOwner != owner) {
      tableMeta.setOwner(owner)
      msc.alter_table(table.database.get, table.table, tableMeta)
      true
    } else {
      false
    }
  }
}
