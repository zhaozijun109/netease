package com.netease.yuanqi.test

import com.netease.yuanqi.metaservice.MammutMetaService

object MammutMetaServiceTest {
  def main(args: Array[String]): Unit = {
    val s = new MammutMetaService
    val testDb = if (args.length > 0) args(0) else "lofter_dm"
    val testTable = if (args.length > 1) args(1) else "ads_ecology_easy_fetch_creator_wide_table_dd"

    println(s"=======: ${s.getSig}")


    try {
      // ─── 1. getDatasourceList ────────────────────────────────────────────
      println("=== 1. getDatasourceList (datasourceId=832) ===")
      val datasources = s.getDatasourceList(datasourceId = Some(832), product = Some("da_lofter"))
      println(s"Total datasources: ${datasources.size}")
      datasources.foreach { ds =>
        println(s"  [${ds.datasourceId}] ${ds.datasourceName.getOrElse("N/A")} " +
          s"(type=${ds.datasourceType.getOrElse("N/A")}, product=${ds.product.getOrElse("N/A")})")
        println(s"  catalogName=${ds.catalogName.getOrElse("N/A")}, info=${ds.info}")
        println(s"  rawJson=${ds.rawJson}")
      }
      println()

      // ─── 2. getTableList ────────────────────────────────────────────────
      println(s"=== 2. getTableList (db=$testDb) ===")
      val tables = s.getTableList(testDb)
      println(s"Total tables: ${tables.size}")
      tables.take(5).foreach { t =>
        println(s"  ${t.table.getOrElse("N/A")} (type=${t.tableType.getOrElse("N/A")}, " +
          s"owner=${t.owner.getOrElse("N/A")}, creator=${t.creator.getOrElse("N/A")})")
        println(s"    comment=${t.comment.getOrElse("N/A")}, createdTime=${t.createdTime.getOrElse("N/A")}")
        println(s"    fields count=${t.fields.size}")
        t.fields.take(3).foreach { f =>
          println(s"      ${f.fieldName.getOrElse("N/A")} (${f.fieldType.getOrElse("N/A")}) " +
            s"partition=${f.partitionKey.getOrElse(false)} - ${f.comment.getOrElse("")}")
        }
        println(s"    rawJson=${t.rawJson}")
      }
      if (tables.size > 5) println(s"  ... and ${tables.size - 5} more")
      println()

      // ─── 3. getTableDetail (V1) ──────────────────────────────────────────
      println(s"=== 3. getTableDetail (db=$testDb, table=$testTable) ===")
      val detail = s.getTableDetail(testDb, testTable)
      println(s"  db: ${detail.db.getOrElse("N/A")}")
      println(s"  table: ${detail.table.getOrElse("N/A")}")
      println(s"  tableType: ${detail.tableType.getOrElse("N/A")}")
      println(s"  owner: ${detail.owner.map(o => s"${o.user.getOrElse("N/A")} (${o.fullName.getOrElse("N/A")})").getOrElse("N/A")}")
      println(s"  product: ${detail.product.getOrElse("N/A")}, groupName: ${detail.groupName.getOrElse("N/A")}")
      println(s"  datasource: ${detail.datasourceName.getOrElse("N/A")} (type=${detail.datasourceType.getOrElse("N/A")})")
      println(s"  primaryKeys: ${detail.primaryKeys.mkString(", ")}")
      println(s"  referCount: ${detail.referCount.getOrElse(0)}, readCount: ${detail.readCount.getOrElse(0)}")
      detail.storageInfo.foreach { si =>
        println(s"  storageInfo:")
        println(s"    location: ${si.location.getOrElse("N/A")}")
        println(s"    totalSize: ${si.totalSize.getOrElse("N/A")}, filesNumber: ${si.filesNumber.getOrElse(0)}")
        println(s"    storageType: ${si.storageType.getOrElse("N/A")}, serializationType: ${si.serializationType.getOrElse("N/A")}")
        println(s"    partition: ${si.partition.getOrElse(false)}, partitionLifeCycle: ${si.partitionLifeCycle.getOrElse("N/A")}")
        println(s"    createTime: ${si.createTime.getOrElse("N/A")}, updateTime: ${si.updateTime.getOrElse("N/A")}")
      }
      detail.businessInfo.foreach { bi =>
        println(s"  businessInfo:")
        println(s"    impalaSync: ${bi.impalaSync.getOrElse(false)}, core: ${bi.core.getOrElse(false)}")
        println(s"    hasCollected: ${bi.hasCollected.getOrElse(false)}, hasSubscribed: ${bi.hasSubscribed.getOrElse(false)}")
      }
      println(s"  rawJsonV1: ${detail.rawJson}")
      println()

      // ─── 4. getTableDetailV3 ────────────────────────────────────────────
      println(s"=== 4. getTableDetailV3 (db=$testDb, table=$testTable, type=hive) ===")
      val detailV3 = s.getTableDetailV3(testDb, testTable, "hive")
      println(s"  db: ${detailV3.db.getOrElse("N/A")}")
      println(s"  table: ${detailV3.table.getOrElse("N/A")}")
      println(s"  catalog: ${detailV3.catalog.getOrElse("N/A")}")
      println(s"  datasourceType: ${detailV3.datasourceType.getOrElse("N/A")}")
      println(s"  product: ${detailV3.product.getOrElse("N/A")}")
      detailV3.tableMetaInfo.foreach { tmi =>
        println(s"  tableMetaInfo:")
        println(s"    description: ${tmi.description.getOrElse("N/A")}")
        println(s"    owner: ${tmi.owner.getOrElse("N/A")}")
        println(s"    storagePath: ${tmi.storagePath.getOrElse("N/A")}")
        println(s"    totalSize: ${tmi.totalSize.getOrElse("N/A")}, filesNumber: ${tmi.filesNumber.getOrElse(0)}")
        println(s"    tableType: ${tmi.tableType.getOrElse("N/A")}, tableCategory: ${tmi.tableCategory.getOrElse("N/A")}")
        println(s"    partTable: ${tmi.partTable.getOrElse(false)}")
        println(s"    tableLifeCycle: ${tmi.tableLifeCycle.getOrElse("N/A")}, partitionLifeCycle: ${tmi.partitionLifeCycle.getOrElse("N/A")}")
        println(s"    serializationType: ${tmi.serializationType.getOrElse("N/A")}, compressionType: ${tmi.compressionType.getOrElse("N/A")}")
        println(s"    columnInfos count: ${tmi.columnInfos.size}")
        tmi.columnInfos.take(3).foreach { col =>
          println(s"      ${col.columnName.getOrElse("N/A")} (${col.columnType.getOrElse("N/A")}) " +
            s"pk=${col.primaryKey.getOrElse(false)} - ${col.description.getOrElse("")}")
        }
        if (tmi.columnInfos.size > 3) println(s"      ... and ${tmi.columnInfos.size - 3} more columns")
      }
      println(s"  rawJsonV3: ${detailV3.rawJson}")
      println()

      // ─── 5. getFieldInfo ────────────────────────────────────────────────
      println(s"=== 5. getFieldInfo (db=$testDb, table=$testTable) ===")
      val fields = s.getFieldInfo(testDb, testTable)
      println(s"  total fields: ${fields.size}")
      fields.take(5).foreach { f =>
        println(s"    [${f.id.getOrElse(0)}] ${f.name.getOrElse("N/A")} (${f.fieldType.getOrElse("N/A")}) " +
          s"partition=${f.partitionKey.getOrElse(false)}, primary=${f.primaryKey.getOrElse(false)}, " +
          s"enum=${f.enumKey.getOrElse(false)} - ${f.comment.getOrElse("")}")
      }
      if (fields.size > 5) println(s"    ... and ${fields.size - 5} more")
      println()

      // ─── 6. getPartitionCount ───────────────────────────────────────────
      println(s"=== 6. getPartitionCount (db=$testDb, table=$testTable) ===")
      val partCount = s.getPartitionCount(testDb, testTable)
      println(s"  partition count: $partCount")
      println()

      // ─── 7. getPartitionList ────────────────────────────────────────────
      println(s"=== 7. getPartitionList (db=$testDb, table=$testTable) ===")
      val partitions = s.getPartitionList(testDb, testTable)
      println(s"  total partitions: ${partitions.size}")
      partitions.take(5).foreach { p =>
        println(s"    ${p.partition.getOrElse("N/A")} " +
          s"(size=${p.totalSize.getOrElse(0L)}, rows=${p.numRows.getOrElse(0L)}, " +
          s"files=${p.numFiles.getOrElse(0L)}, location=${p.location.getOrElse("N/A")})")
        println(s"      catalog=${p.catalog.getOrElse("N/A")}, db=${p.db.getOrElse("N/A")}, " +
          s"table=${p.table.getOrElse("N/A")}")
        println(s"      created=${p.createTime.getOrElse("N/A")}, updated=${p.updateTime.getOrElse("N/A")}")
      }
      if (partitions.size > 5) println(s"    ... and ${partitions.size - 5} more")

    } catch {
      case e: Exception =>
        println(s"ERROR: ${e.getMessage}")
        e.printStackTrace()
    } finally {
      s.close()
    }
  }
}