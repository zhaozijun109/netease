package com.netease.yuanqi.doris.bitmap

import org.roaringbitmap.longlong.Roaring64NavigableMap
import org.slf4j.LoggerFactory

import java.io.{ByteArrayInputStream, DataInputStream}

/**
 * Hive RoaringBitmap 二进制数据的反序列化和格式转换工具。
 *
 * Hive bitmap UDF (com.netease.wm.udf.bitmap.ToBitmapUDAF) 使用
 * [[Roaring64NavigableMap]] + PORTABLE 序列化模式存储 bitmap。
 *
 * 本工具提供三种输出格式，分别对应 Doris 的不同 bitmap 导入方式：
 *   - `toArray`    → 展开为 Long 数组，用于 Expand 模式（to_bitmap）
 *   - `toIdString` → 逗号分隔的 ID 字符串，用于 StringFunc 模式（bitmap_from_string）
 */
object HiveBitmapConverter {

  private val LOG = LoggerFactory.getLogger(getClass)

  /** 当 bitmap 基数超过此阈值时，toIdString 会打印 WARN 日志 */
  private val LARGE_BITMAP_WARN_THRESHOLD = 100000L

  /** StringFunc 模式的默认分批大小，每批最多包含的 ID 数量 */
  val DEFAULT_STRING_BATCH_SIZE = 50000

  /**
   * 反序列化 Hive BinaryType 中的 RoaringBitmap。
   *
   * Hive bitmap UDF 使用 PORTABLE 序列化模式（跨语言兼容），
   * 因此需要设置 `SERIALIZATION_MODE = SERIALIZATION_MODE_PORTABLE`。
   *
   * @param bytes Hive 列中的 BinaryType 字节数组
   * @return Roaring64NavigableMap 对象；bytes 为 null/empty 时返回空 bitmap
   */
  def deserialize(bytes: Array[Byte]): Roaring64NavigableMap = {
    Roaring64NavigableMap.SERIALIZATION_MODE = Roaring64NavigableMap.SERIALIZATION_MODE_PORTABLE
    if (bytes == null || bytes.isEmpty) {
      new Roaring64NavigableMap()
    } else {
      val in = new DataInputStream(new ByteArrayInputStream(bytes))
      val bitmap = new Roaring64NavigableMap()
      bitmap.deserialize(in)
      bitmap
    }
  }

  /**
   * 获取 bitmap 中的元素数量。
   *
   * @param bitmap RoaringBitmap 对象
   * @return 元素数量
   */
  def cardinality(bitmap: Roaring64NavigableMap): Long = {
    bitmap.getLongCardinality
  }

  /**
   * 将 bitmap 展开为 Long ID 数组。
   *
   * '''注意'''：此方法会一次性分配整个 Long 数组，大基数 bitmap 可能导致内存压力。
   * 高基数场景建议使用 [[toIterator]] 代替。
   *
   * @param bitmap RoaringBitmap 对象
   * @return Long 数组，包含 bitmap 中的所有元素
   */
  def toArray(bitmap: Roaring64NavigableMap): Array[Long] = {
    bitmap.toArray
  }

  /**
   * 返回 bitmap 元素的懒加载迭代器，逐个产出 Long ID。
   *
   * 用于 '''Expand 模式'''内存优化：避免 `toArray` 一次性分配整个 Long 数组，
   * 改为流式逐个消费，配合 Doris `to_bitmap(userId)` + AGGREGATE KEY `BITMAP_UNION` 聚合。
   *
   * @param bitmap RoaringBitmap 对象
   * @return Long 元素的 Iterator
   */
  def toIterator(bitmap: Roaring64NavigableMap): Iterator[Long] = {
    val iter = bitmap.iterator()
    new Iterator[Long] {
      override def hasNext: Boolean = iter.hasNext
      override def next(): Long = iter.next()
    }
  }

  // ─── 融合方法：deserialize + iterate 一步完成 ───────────────────────────────

  /**
   * 【Expand 模式融合方法】反序列化 + 立即返回 Long ID 迭代器。
   *
   * 将 deserialize → toIterator 两步融合为一步，bitmap 对象引用不逃逸出方法，
   * 使 Roaring64NavigableMap 内部已消费的 container 能更快被 GC 回收。
   *
   * @param bytes Hive BinaryType 字节数组
   * @return Long 元素的懒加载 Iterator；bytes 为 null/empty 时返回空迭代器
   */
  def deserializeToLongIterator(bytes: Array[Byte]): Iterator[Long] = {
    val bitmap = deserialize(bytes)
    if (bitmap.getLongCardinality == 0) return Iterator.empty
    toIterator(bitmap)
  }

  /**
   * 【StringFunc 模式融合方法】反序列化 + 立即返回分批 ID 字符串迭代器。
   *
   * 将 deserialize → toIdStringBatches 两步融合为一步，bitmap 对象引用不逃逸出方法，
   * 使 Roaring64NavigableMap 内部已消费的 container 能更快被 GC 回收。
   *
   * @param bytes     Hive BinaryType 字节数组
   * @param batchSize 每批最大 ID 数量（默认 50000）
   * @return ID 字符串的懒加载 Iterator；bytes 为 null/empty 时返回空迭代器
   */
  def deserializeToStringBatches(bytes: Array[Byte], batchSize: Int = DEFAULT_STRING_BATCH_SIZE): Iterator[String] = {
    val bitmap = deserialize(bytes)
    if (bitmap.getLongCardinality == 0) return Iterator.empty
    toIdStringBatches(bitmap, batchSize)
  }

  /**
   * 将 bitmap 转换为逗号分隔的 ID 字符串。
   *
   * 用于 '''StringFunc 模式'''：将 bitmap 转为 `"1,2,3,..."` 格式，
   * 配合 Doris `bitmap_from_string("1,2,3,...")` 函数。
   *
   * '''注意'''：当 bitmap 基数很大（>100K）时字符串会非常长，
   * 可能导致 Stream Load 性能问题。此时建议使用 Expand 模式。
   *
   * @param bitmap RoaringBitmap 对象
   * @return 逗号分隔的 ID 字符串；空 bitmap 返回空字符串 ""
   */
  def toIdString(bitmap: Roaring64NavigableMap): String = {
    val card = bitmap.getLongCardinality
    if (card == 0) return ""

    if (card > LARGE_BITMAP_WARN_THRESHOLD) {
      LOG.warn(s"Large bitmap with $card elements being converted to ID string. " +
        s"Consider using Expand mode (--mode expand) or toIdStringBatches() for better performance.")
    }

    val sb = new StringBuilder((card * 10).toInt.min(1024 * 1024)) // 预估容量
    val iter = bitmap.iterator()
    var first = true
    while (iter.hasNext) {
      if (!first) sb.append(',')
      sb.append(iter.next())
      first = false
    }
    sb.toString()
  }

  /**
   * 将 bitmap 分批转换为逗号分隔的 ID 字符串序列。
   *
   * 用于 '''StringFunc 模式'''的安全替代方案：将一个大 bitmap 拆分为多个小批次字符串，
   * 每批最多包含 `batchSize` 个 ID，避免单行数据过长导致 OOM 或 Stream Load 限制。
   *
   * 配合 Doris AGGREGATE KEY + `BITMAP_UNION` 聚合，多批次 `bitmap_from_string()`
   * 的结果会自动合并为完整 bitmap。
   *
   * {{{
   *   bitmap = {1,2,3,...,150000}，batchSize = 50000
   *     ↓ 拆分为 3 批
   *   Seq("1,2,3,...,50000", "50001,...,100000", "100001,...,150000")
   *     ↓ 每批生成一行 → Doris bitmap_from_string() → BITMAP_UNION 聚合
   * }}}
   *
   * @param bitmap    RoaringBitmap 对象
   * @param batchSize 每批最大 ID 数量（默认 50000）
   * @return ID 字符串序列；空 bitmap 返回空序列
   */
  def toIdStringBatches(bitmap: Roaring64NavigableMap, batchSize: Int = DEFAULT_STRING_BATCH_SIZE): Iterator[String] = {
    val card = bitmap.getLongCardinality
    if (card == 0) return Iterator.empty

    val bitmapIter = bitmap.iterator()

    // 懒加载 Iterator：每次 next() 只构建一批字符串，前一批可被 GC 回收
    new Iterator[String] {
      override def hasNext: Boolean = bitmapIter.hasNext

      override def next(): String = {
        val sb = new StringBuilder(batchSize * 12)
        var count = 0
        while (bitmapIter.hasNext && count < batchSize) {
          if (count > 0) sb.append(',')
          sb.append(bitmapIter.next())
          count += 1
        }
        sb.toString()
      }
    }
  }
}
