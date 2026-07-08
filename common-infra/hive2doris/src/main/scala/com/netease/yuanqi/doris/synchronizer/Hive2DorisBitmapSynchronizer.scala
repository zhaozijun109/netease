package com.netease.yuanqi.doris.synchronizer

import com.netease.yuanqi.doris.bitmap.HiveBitmapConverter

import com.netease.yuanqi.config.DorisConfig
import com.netease.yuanqi.doris.schema.DorisDDLGenerator
import com.netease.yuanqi.doris.util.Args
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Row, SaveMode, SparkSession}
import org.slf4j.LoggerFactory

import java.sql.{Connection, DriverManager}

/**
 * Hive Bitmap → Doris 的统一导入工具。
 *
 * 将 Hive 中以 RoaringBitmap 序列化存储的 bitmap 数据导入 Doris BITMAP 列。
 * 支持两种写入模式（运行时通过 `--mode` 参数切换），均通过 Spark Doris Connector（Stream Load）写入。
 * 支持同一张表包含'''多个 bitmap 列'''（仅 StringFunc 模式）。
 *
 * == StringFunc 模式 (默认，支持多 bitmap 列) ==
 * {{{
 *   Hive Row(tag, dim1, ..., v1_bitmap[150K], v2_bitmap[30K])
 *     ↓  每列独立 deserializeToStringBatches：反序列化 + 分批转为 ID 字符串（每批 50K）
 *   (tag, dim1, ..., "v1_ids_1-50K",   "v2_ids_1-30K")
 *   (tag, dim1, ..., "v1_ids_50K-100K", "v2_lastId")     ← v2 耗尽，填充最后一批第一个 ID
 *   (tag, dim1, ..., "v1_ids_100K-150K","v2_lastId")     ← 同上
 *     ↓  Spark Doris Connector
 *   doris.write.fields = "tag,dim1,...,v1,v2,v1=bitmap_from_string(v1),v2=bitmap_from_string(v2)"
 *     ↓
 *   Doris AGGREGATE KEY 表 → BITMAP_UNION 聚合，多批次自动合并为完整 bitmap
 * }}}
 *
 * == 多列对齐策略（幂等填充） ==
 * 当同一行有多个 bitmap 列时，各列基数不同会导致批次数不一致。
 * 本工具按最长批次对齐：已耗尽的列用该列'''最后一批的第一个 ID'''填充。
 *
 * 利用 `BITMAP_UNION` 的幂等性（A ∪ B ∪ B = A ∪ B），重复写入一个已存在的 ID 不影响最终聚合结果。
 * 这样 `doris.write.fields` 保持静态不变，无需按批次动态切换函数映射，
 * 完全保持 iterator 流式处理，无需 cache、无需分组。
 *
 * == Expand 模式（仅限单 bitmap 列） ==
 * {{{
 *   Hive Row(tag, dim1, ..., bitmap_bytes)
 *     ↓  deserializeToLongIterator：反序列化 + 流式逐个产出 userId
 *   (tag, dim1, ..., userId_1)
 *   (tag, dim1, ..., userId_2)
 *   ...
 *     ↓  Spark Doris Connector
 *   doris.write.fields = "tag,dim1,...,bitmap,bitmap=to_bitmap(bitmap)"
 *     ↓
 *   Doris AGGREGATE KEY 表 → BITMAP_UNION(to_bitmap(userId)) 聚合
 * }}}
 *
 * '''注意'''：Expand 模式仅支持单个 bitmap 列。多列场景下会产生笛卡尔积导致记录数爆炸，
 * 因此在检测到多列时会直接拒绝并提示使用 StringFunc 模式。
 *
 * == 内存优化 ==
 * 两种模式均使用 [[HiveBitmapConverter]] 的融合方法（deserializeToStringBatches / deserializeToLongIterator），
 * 将反序列化和迭代消费合并为一步，bitmap 对象引用不逃逸出 flatMap 闭包，可尽早被 GC 回收。
 *
 * == Doris 目标表要求 ==
 *   - AGGREGATE KEY 模型
 *   - bitmap 列类型: `BITMAP`，聚合函数: `BITMAP_UNION`
 *   - 如果目标表不存在，会根据 DataFrame schema '''自动建表'''（AGGREGATE KEY + 动态分区 + lz4 压缩）
 *
 * == 用法 ==
 * {{{
 *   # StringFunc 模式（默认，支持多 bitmap 列）
 *   spark-submit --class com.netease.yuanqi.doris.synchronizer.Hive2DorisBitmapSynchronizer \
 *     hive2doris-assembly.jar \
 *     --source "SELECT dt, tag, dim1, dim2, bitmap AS users_bitmap
 *               FROM lofter_dm.ads_lofter_tags_data_bitmap_dd
 *               WHERE dt = '2026-04-21' AND tag = 'xxx'" \
 *     --doris-db lofter --doris-table user_portrait_datas_bitmap \
 *     --bitmap-col users_bitmap \
 *     --partition 2026-04-21 \
 *     --parallel 50
 *
 *   # 多 bitmap 列示例（StringFunc 模式）
 *   spark-submit --class com.netease.yuanqi.doris.synchronizer.Hive2DorisBitmapSynchronizer \
 *     hive2doris-assembly.jar \
 *     --source "SELECT dt, tag, uv_7d_bitmap, uv_30d_bitmap FROM ..." \
 *     --doris-db lofter --doris-table user_portrait_multi_bitmap \
 *     --bitmap-col uv_7d_bitmap,uv_30d_bitmap \
 *     --partition 2026-04-21
 *
 *   # Expand 模式（仅单 bitmap 列）
 *   spark-submit --class com.netease.yuanqi.doris.synchronizer.Hive2DorisBitmapSynchronizer \
 *     hive2doris-assembly.jar \
 *     --source "..." \
 *     --doris-db lofter --doris-table user_portrait_datas_bitmap \
 *     --bitmap-col users_bitmap \
 *     --mode expand \
 *     --partition 2026-04-21
 * }}}
 *
 * @see [[HiveBitmapConverter]] 用于 RoaringBitmap 反序列化和格式转换（含融合方法）
 */
object Hive2DorisBitmapSynchronizer {

  private val LOG = LoggerFactory.getLogger(getClass)

  // ─── Constants ──────────────────────────────────────────────────────────────

  /** 默认 bitmap 列名后缀，用于自动识别 */
  private val BITMAP_COL_SUFFIX = "_bitmap"

  /** 默认并行度 */
  private val DEFAULT_PARALLEL = 10

  // ─── Main Entry ─────────────────────────────────────────────────────────────

  def main(args: Array[String]): Unit = {
    val pargs = Args(args)

    // 1. 解析参数
    val sourceQuery = pargs.optional("source")
      .orElse(Option(System.getProperty("spark.source.query")))
      .getOrElse(throw new IllegalArgumentException("--source is required: Hive query SQL containing bitmap column"))

    val dorisDatabase = pargs.optional("doris-db").getOrElse(DorisConfig.database)
    val dorisTable = pargs.required("doris-table")
    // --bitmap-col 支持逗号分隔多列，如 --bitmap-col v1,v2
    val bitmapColOverride: Option[Seq[String]] = pargs.optional("bitmap-col")
      .map(_.split(",").map(_.trim).filter(_.nonEmpty).toSeq)
      .filter(_.nonEmpty)
    val mode = pargs.optional("mode").map(_.toLowerCase) match {
      case Some("expand") => "expand"
      case _ => "string" // 默认 string
    }
    val partitionVal = pargs.optional("partition")
    val parallel = pargs.optional("parallel").map(_.toInt).getOrElse(DEFAULT_PARALLEL)

    LOG.info(
      s"""Hive2DorisBitmapSynchronizer starting:
         |  source     = $sourceQuery
         |  doris      = $dorisDatabase.$dorisTable
         |  mode       = $mode
         |  partition  = ${partitionVal.getOrElse("(none)")}
         |  bitmap-col = ${bitmapColOverride.map(_.mkString(", ")).getOrElse("(auto-detect)")}
         |  parallel   = $parallel
         |""".stripMargin)

    // 2. 创建 SparkSession
    val spark = SparkSession.builder()
      .appName(s"Hive2DorisBitmapSynchronizer-$dorisTable")
      .config("spark.sql.parquet.binaryAsString", value = true)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .config("spark.speculation", "false")
      .enableHiveSupport()
      .getOrCreate()

    try {
      // 3. 执行 Hive 查询
      LOG.info(s"Executing Hive query: $sourceQuery")
      val df = spark.sql(sourceQuery)

      // 4. 识别 bitmap 列（支持多列）
      val bitmapColNames: Seq[String] = bitmapColOverride.getOrElse(detectBitmapColumns(df.schema))
      LOG.info(s"Bitmap columns identified: ${bitmapColNames.mkString(", ")}")

      // 验证所有 bitmap 列存在且为 BinaryType
      bitmapColNames.foreach { colName =>
        val field = df.schema.fields.find(_.name.equalsIgnoreCase(colName))
          .getOrElse(throw new IllegalArgumentException(
            s"Bitmap column '$colName' not found in DataFrame schema: ${df.schema.fieldNames.mkString(", ")}"))
        require(field.dataType == BinaryType,
          s"Bitmap column '$colName' must be BinaryType, but got ${field.dataType}")
      }

      // 5. 检查 Doris 目标表是否存在，不存在则自动建表
      ensureDorisTable(df.schema, bitmapColNames, dorisDatabase, dorisTable)

      // 6. 清理目标分区（可选）
      partitionVal.foreach { pv =>
        LOG.info(s"Cleaning up Doris partition: $dorisDatabase.$dorisTable partition=$pv")
        cleanupDorisPartition(dorisDatabase, dorisTable, pv)
      }

      // 7. 根据 mode 选择写入策略
      mode match {
        case "expand" =>
          // Expand 模式不支持多 bitmap 列（笛卡尔积爆炸）
          require(bitmapColNames.size == 1,
            s"Expand mode does not support multiple bitmap columns (would cause Cartesian product explosion). " +
              s"Found ${bitmapColNames.size} bitmap columns: ${bitmapColNames.mkString(", ")}. " +
              s"Please use StringFunc mode (--mode string) for multiple bitmap columns.")
          LOG.info("Using EXPAND mode: bitmap → N rows × userId → to_bitmap()")
          writeExpandMode(spark, df, bitmapColNames.head, dorisDatabase, dorisTable, parallel)
        case "string" =>
          LOG.info(s"Using STRING mode: ${bitmapColNames.size} bitmap column(s) → ID string → bitmap_from_string()")
          writeStringFuncMode(spark, df, bitmapColNames, dorisDatabase, dorisTable, parallel)
      }

      LOG.info(s"Hive2DorisBitmapSynchronizer completed successfully: $dorisDatabase.$dorisTable")
    } finally {
      spark.close()
    }
  }

  // ─── Bitmap Column Detection ────────────────────────────────────────────────

  /**
   * 自动识别 DataFrame 中的所有 bitmap 列。
   * 规则：BinaryType 且列名以 `_bitmap` 结尾（不区分大小写）。
   *
   * @return bitmap 列名序列（至少一个，否则抛异常）
   */
  private def detectBitmapColumns(schema: StructType): Seq[String] = {
    val candidates = schema.fields.filter { f =>
      f.dataType == BinaryType && f.name.toLowerCase.endsWith(BITMAP_COL_SUFFIX)
    }
    if (candidates.isEmpty) {
      throw new IllegalArgumentException(
        s"No bitmap column found (expected BinaryType column with '$BITMAP_COL_SUFFIX' suffix). " +
          s"Schema: ${schema.fieldNames.mkString(", ")}. Use --bitmap-col to specify explicitly.")
    }
    val names = candidates.map(_.name).toSeq
    LOG.info(s"Auto-detected ${names.size} bitmap column(s): ${names.mkString(", ")}")
    names
  }

  // ─── Auto Create Table ──────────────────────────────────────────────────────

  /**
   * 检查 Doris 目标表是否存在，不存在则根据 DataFrame schema 自动建表。
   *
   * 建表规则：
   *   - 使用 AGGREGATE KEY 模型
   *   - 所有 bitmap 列声明为 `BITMAP BITMAP_UNION`，按原序放在最后
   *   - 其他所有列构成 AGGREGATE KEY
   *   - 如有 dt 列（DateType），使用 AUTO PARTITION
   */
  private def ensureDorisTable(
    schema: StructType,
    bitmapColNames: Seq[String],
    database: String,
    tableName: String
  ): Unit = {
    val conn = getConnection
    try {
      // 检查表是否存在
      val checkSql = s"SHOW TABLES FROM `$database` LIKE '$tableName'"
      val rs = conn.createStatement().executeQuery(checkSql)
      val exists = rs.next()
      rs.close()

      if (exists) {
        LOG.info(s"Doris table $database.$tableName already exists, skipping auto-create")
        return
      }

      LOG.info(s"Doris table $database.$tableName does not exist, auto-creating...")

      // 分离普通列和 bitmap 列（bitmap 列名集合，不区分大小写）
      val bitmapColNameSet = bitmapColNames.map(_.toLowerCase).toSet
      val (bitmapFields, regularFields) = schema.fields.partition(f => bitmapColNameSet.contains(f.name.toLowerCase))
      require(bitmapFields.nonEmpty, s"Bitmap columns '${bitmapColNames.mkString(", ")}' not found in schema")

      // 检测分区列（dt, DateType）
      val partitionCol = regularFields.find { f =>
        f.name.toLowerCase == "dt" && (f.dataType == DateType || f.dataType == StringType)
      }

      // 构建列定义 — 分区列放首位，普通列按原序，所有 bitmap 列放最后
      val partCols = partitionCol.toSeq
      val otherCols = regularFields.filterNot(f => partitionCol.exists(_.name == f.name))

      val orderedCols = partCols ++ otherCols
      val keyColumnDefs = orderedCols.map { f =>
        val dorisType = sparkTypeToDorisAggType(f)
        s"  `${f.name}` $dorisType"
      }
      // 所有 bitmap 列按原序排列在最后
      val bitmapColDefs = bitmapFields.map { f =>
        s"  `${f.name}` BITMAP BITMAP_UNION COMMENT 'bitmap column'"
      }

      val allColumnDefs = (keyColumnDefs ++ bitmapColDefs).mkString(",\n")

      // AGGREGATE KEY = 所有非 bitmap 列
      val aggKeyClause = orderedCols.map(f => s"`${f.name}`").mkString(", ")

      // PARTITION 子句
      val partitionClause = partitionCol match {
        case Some(pc) => s"\nAUTO PARTITION BY RANGE (date_trunc(`${pc.name}`, 'day')) ()"
        case None => ""
      }

      // 分桶列选择（第一个非分区列，或分区列本身）
      val bucketCol = otherCols.headOption.orElse(partitionCol).map(_.name).getOrElse("dt")

      // 构建 PROPERTIES — 与 DorisDDLGenerator 保持一致
      val propsBuilder = scala.collection.mutable.LinkedHashMap[String, String](
        "replication_num" -> "3",
        "compression" -> "lz4"
      )
      // 分区表额外添加 estimate_partition_size + 动态分区生命周期管理参数
      if (partitionCol.isDefined) {
        propsBuilder += ("estimate_partition_size" -> "10G")
        propsBuilder += ("dynamic_partition.enable" -> "true")
        propsBuilder += ("dynamic_partition.time_unit" -> "day")
        propsBuilder += ("dynamic_partition.start" -> "-7")
        propsBuilder += ("dynamic_partition.end" -> "0")
        propsBuilder += ("dynamic_partition.prefix" -> "p")
      }
      val propsStr = propsBuilder.map { case (k, v) => s"""  "$k" = "$v"""" }.mkString(",\n")

      val ddl =
        s"""CREATE TABLE IF NOT EXISTS `$database`.`$tableName`
           |(
           |$allColumnDefs
           |)
           |AGGREGATE KEY($aggKeyClause)$partitionClause
           |DISTRIBUTED BY HASH(`$bucketCol`) BUCKETS AUTO
           |PROPERTIES (
           |$propsStr
           |)""".stripMargin

      LOG.info(s"Auto-creating Doris table:\n$ddl")
      conn.createStatement().execute(ddl)
      LOG.info(s"Doris table $database.$tableName created successfully")
    } finally {
      conn.close()
    }
  }

  /**
   * Spark 类型 → Doris AGGREGATE KEY 列类型映射。
   * AGGREGATE KEY 列需要 NOT NULL + REPLACE 或直接声明为 KEY 列。
   */
  private def sparkTypeToDorisAggType(field: StructField): String = {
    val baseType = field.dataType match {
      case DateType => "DATE NOT NULL"
      case TimestampType => "DATETIME NOT NULL"
      case StringType =>
        if (field.name.toLowerCase == "dt") "DATE NOT NULL"
        else "VARCHAR(256) NOT NULL DEFAULT ''"
      case IntegerType => "INT NOT NULL DEFAULT '0'"
      case LongType => "BIGINT NOT NULL DEFAULT '0'"
      case FloatType => "FLOAT NOT NULL DEFAULT '0'"
      case DoubleType => "DOUBLE NOT NULL DEFAULT '0'"
      case BooleanType => "BOOLEAN NOT NULL DEFAULT '0'"
      case _: DecimalType => "DECIMAL(27, 9) NOT NULL DEFAULT '0'"
      case _ => "VARCHAR(256) NOT NULL DEFAULT ''"
    }
    baseType
  }

  // ─── Expand Mode ────────────────────────────────────────────────────────────

  /**
   * Expand 模式：将 bitmap 展开为逐行 userId，通过 Spark Doris Connector 写入。
   *
   * 数据转换：
   * {{{
   *   原始 Row: (dt, tag, dim1, dim2, dim3, dim4, grp, bitmap_bytes)
   *     ↓ flatMap: 展开为 N 行
   *   (dt, tag, dim1, dim2, dim3, dim4, grp, userId_1: Long)
   *   (dt, tag, dim1, dim2, dim3, dim4, grp, userId_2: Long)
   *   ...
   *     ↓ Doris Connector: doris.write.fields = "..., to_bitmap(users_bitmap)"
   *   Doris 自动执行 bitmap_union(to_bitmap(userId))
   * }}}
   */
  private def writeExpandMode(
    spark: SparkSession,
    df: DataFrame,
    bitmapColName: String,
    dorisDatabase: String,
    dorisTable: String,
    parallel: Int
  ): Unit = {
    val schema = df.schema
    val bitmapColIdx = schema.fieldIndex(bitmapColName)
    val nonBitmapFields = schema.fields.zipWithIndex.filter(_._2 != bitmapColIdx)

    // 构建新 schema：非 bitmap 列（保持原序）+ bitmap 列改为 LongType（userId）
    val newFields = nonBitmapFields.map(_._1) :+ StructField(bitmapColName, LongType, nullable = false)
    val newSchema = StructType(newFields)

    // flatMap：每行展开为 N 行
    // 使用融合方法 deserializeToLongIterator：反序列化 + 立即流式迭代，bitmap 对象不逃逸、可尽早 GC
    val expandedRDD = df.repartition(parallel).rdd.flatMap { row =>
      val bitmapBytes = if (row.isNullAt(bitmapColIdx)) null else row.getAs[Array[Byte]](bitmapColIdx)
      val userIdIter = HiveBitmapConverter.deserializeToLongIterator(bitmapBytes)

      if (!userIdIter.hasNext) {
        Iterator.empty
      } else {
        // 提取非 bitmap 列的值（只取一次，复用）
        val nonBitmapValues = nonBitmapFields.map { case (_, idx) => row.get(idx) }
        userIdIter.map { userId =>
          Row.fromSeq(nonBitmapValues :+ userId)
        }
      }
    }

    val expandedDF = spark.createDataFrame(expandedRDD, newSchema)

    // 构建 doris.write.fields：bitmap 列需出现两次（先声明数据字段引用，再做函数映射）
    // 示例: dt,tag,grp,bitmap,bitmap=to_bitmap(bitmap)
    val normalFields = newFields.map(_.name).mkString(",")
    val writeFields = s"$normalFields,$bitmapColName=to_bitmap($bitmapColName)"

    LOG.info(s"Expand mode: doris.write.fields = $writeFields")

    writeToDoris(expandedDF, dorisDatabase, dorisTable, writeFields)
  }

  // ─── StringFunc Mode ────────────────────────────────────────────────────────

  /**
   * StringFunc 模式：将 bitmap 分批转为 ID 字符串，通过 Spark Doris Connector 写入。
   * 支持多 bitmap 列，每列独立分批，按最长批次对齐。
   *
   * 为避免大 bitmap（基数 >50K）生成超长字符串导致 OOM 和 Stream Load 限制，
   * 使用 flatMap + `toIdStringBatches()` 将每个 bitmap 列拆分为多行小批次字符串。
   * Doris AGGREGATE KEY + `BITMAP_UNION` 聚合会自动将多批次结果合并为完整 bitmap。
   *
   * === 多列对齐策略 ===
   *
   * 当存在多个 bitmap 列时，各列的批次数可能不同（基数不同）。
   * 按最长批次对齐，已耗尽的列用该列'''最后一批的第一个 ID'''填充。
   *
   * 利用 `BITMAP_UNION` 的幂等性：重复写入一个已存在的 ID 不影响最终聚合结果。
   * 这样 `doris.write.fields` 始终不变，统一使用 `bitmap_from_string()` 处理所有 bitmap 列，
   * 无需分组、无需 cache、完全保持 iterator 流式处理。
   *
   * {{{
   *   原始 Row: (dt, tag, v1[150K], v2[30K])
   *     ↓ v1: 3批(50K×3)，v2: 1批(30K)
   *   批次1: (dt, tag, "v1_ids_1-50K",  "v2_ids_1-30K")   ← 两列都有数据
   *   批次2: (dt, tag, "v1_ids_50K-100K", "v2_lastId")    ← v2 耗尽，填充 v2 最后一批的第一个 ID
   *   批次3: (dt, tag, "v1_ids_100K-150K","v2_lastId")    ← 同上，BITMAP_UNION 幂等不影响结果
   *     ↓ doris.write.fields 始终不变: dt,tag,v1,v2,v1=bitmap_from_string(v1),v2=bitmap_from_string(v2)
   * }}}
   */
  private def writeStringFuncMode(
    spark: SparkSession,
    df: DataFrame,
    bitmapColNames: Seq[String],
    dorisDatabase: String,
    dorisTable: String,
    parallel: Int
  ): Unit = {
    val schema = df.schema
    // 每个 bitmap 列的索引
    val bitmapColIndices: Seq[(String, Int)] = bitmapColNames.map(name => (name, schema.fieldIndex(name)))
    val bitmapIdxSet = bitmapColIndices.map(_._2).toSet

    // 构建新 schema：所有 bitmap 列改为 StringType
    val newFields = schema.fields.map { f =>
      if (bitmapIdxSet.contains(schema.fieldIndex(f.name))) StructField(f.name, StringType, nullable = true)
      else f
    }
    val newSchema = StructType(newFields)

    // flatMap：每行的所有 bitmap 列分批转为 ID 字符串，按最长批次对齐
    // 已耗尽的列用 null 填充，后续通过不同的 doris.write.fields 组分批写入
    val mappedRDD = df.repartition(parallel).rdd.flatMap { row =>
      // 为每个 bitmap 列生成分批 Iterator
      val bitmapIters: Seq[(Int, Iterator[String])] = bitmapColIndices.map { case (name, idx) =>
        val bytes = if (row.isNullAt(idx)) null else row.getAs[Array[Byte]](idx)
        (idx, HiveBitmapConverter.deserializeToStringBatches(bytes))
      }

      // 提取非 bitmap 列的值（复用）
      val baseValues = row.toSeq.toArray

      // 每个 bitmap 列维护 lastFirstElement：该列最后一批字符串的第一个 ID
      // 当列耗尽时用 lastFirstElement 填充，BITMAP_UNION 幂等性保证不影响最终结果
      val lastFirstElements = new Array[String](bitmapIters.size)

      // 对齐迭代：只要有任一列还有批次，就继续产出行
      new Iterator[Row] {
        override def hasNext: Boolean = bitmapIters.exists(_._2.hasNext)

        override def next(): Row = {
          val values = baseValues.clone()
          bitmapIters.zipWithIndex.foreach { case ((idx, iter), i) =>
            if (iter.hasNext) {
              val batch = iter.next()
              // 记录该列最新批次的第一个元素（逗号前的部分）
              val firstComma = batch.indexOf(',')
              lastFirstElements(i) = if (firstComma > 0) batch.substring(0, firstComma) else batch
              values(idx) = batch
            } else {
              // 列已耗尽：用最后一批的第一个元素填充
              // bitmap_from_string("12345") 产出的 bitmap 已在之前批次中存在，
              // BITMAP_UNION 聚合后不影响最终结果（幂等性）
              values(idx) = lastFirstElements(i)
            }
          }
          Row.fromSeq(values.toSeq)
        }
      }
    }

    val mappedDF = spark.createDataFrame(mappedRDD, newSchema)

    // 构建 doris.write.fields
    // 普通列（非 bitmap）直接列出名称
    val normalColNames = newFields.filterNot(f => bitmapIdxSet.contains(schema.fieldIndex(f.name))).map(_.name)
    // bitmap 列先列出名称（声明数据字段引用），再追加函数映射
    // 当某列为 null 时，bitmap_from_string(null) 在 Doris 中返回 NULL，BITMAP_UNION 聚合会忽略 NULL
    val allFieldNames = newFields.map(_.name)
    val bitmapMappings = bitmapColNames.map(name => s"$name=bitmap_from_string($name)")
    val writeFields = (allFieldNames ++ bitmapMappings).mkString(",")

    LOG.info(s"StringFunc mode (${bitmapColNames.size} bitmap col(s)): doris.write.fields = $writeFields")

    writeToDoris(mappedDF, dorisDatabase, dorisTable, writeFields)
  }

  // ─── Doris Writer ───────────────────────────────────────────────────────────

  /**
   * 通过 Spark Doris Connector 写入 Doris。
   * 两种模式共享此方法，仅 `doris.write.fields` 参数不同。
   */
  private def writeToDoris(
    df: DataFrame,
    dorisDatabase: String,
    dorisTable: String,
    writeFields: String
  ): Unit = {
    val dorisTarget = s"$dorisDatabase.$dorisTable"
    val labelPrefix = s"bitmap_${dorisTable}_${System.currentTimeMillis()}"
    LOG.info(s"Writing to Doris: $dorisTarget (label=$labelPrefix, writeFields=$writeFields)")

    df.write
      .format("doris")
      .option("doris.fenodes", DorisConfig.fenodes)
      .option("doris.table.identifier", dorisTarget)
      .option("user", DorisConfig.user)
      .option("password", DorisConfig.password)
      .option("doris.query.port", DorisConfig.feQueryPort)
      .option("doris.fe.auto.fetch", "true")
      .option("doris.sink.auto-redirect", "true")
      .option("doris.sink.batch.size", "1000000")
      .option("doris.sink.batch.interval.ms", "30000")
      .option("doris.sink.max-retries", "3")
      .option("doris.sink.label.prefix", labelPrefix)
      .option("doris.sink.properties.format", "json")
      .option("doris.sink.properties.read_json_by_line", "true")
      .option("doris.write.fields", writeFields)
      .mode(SaveMode.Append)
      .save()

    LOG.info(s"Successfully wrote data to Doris: $dorisTarget")
  }

  // ─── Partition Cleanup ──────────────────────────────────────────────────────

  /**
   * 清理 Doris 目标分区（写入前调用）。
   * 使用 [[DorisDDLGenerator.dropPartitionBothFormats]] 同时删除
   * AUTO PARTITION (pYYYYMMDD000000) 和 DYNAMIC PARTITION (pYYYYMMDD) 两种格式。
   */
  private def cleanupDorisPartition(
    dorisDatabase: String,
    dorisTable: String,
    partitionVal: String
  ): Unit = {
    val conn = getConnection
    try {
      DorisDDLGenerator.dropPartitionBothFormats(conn, dorisDatabase, dorisTable, partitionVal)
      LOG.info(s"Partition cleanup completed: $dorisDatabase.$dorisTable partition=$partitionVal")
    } finally {
      conn.close()
    }
  }

  // ─── JDBC Connection ────────────────────────────────────────────────────────

  /** 获取 Doris JDBC 连接 */
  private def getConnection: Connection = {
    Class.forName("com.mysql.cj.jdbc.Driver")
    DriverManager.getConnection(
      s"${DorisConfig.jdbcUrl}/${DorisConfig.database}",
      DorisConfig.user,
      DorisConfig.password
    )
  }
}
