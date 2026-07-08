package com.netease.easyudf.cmd

import com.google.common.base.Charsets
import com.google.protobuf.ByteString
import com.linkedin.spark.datasources.tfrecordv2.TFRecordSerializer
import com.netease.cloudmusic.RtrsFileRecord
import com.netease.easyml.common.cmds.VoidUserDefinedCmd
import com.netease.easyml.common.util.IOUtil
import org.apache.hadoop.util.hash.Hash
import org.apache.spark.Partitioner
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.storage.StorageLevel

import java.io.OutputStream
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicLong

case class RtrsSerializerArgs(input: String, table: String, path: String, pk: String,
                              drop: String = "", overwrite: Boolean = true, numPartitions: Int = 200)

class RtrsSerializer extends VoidUserDefinedCmd[RtrsSerializerArgs] {

  override def run(spark: SparkSession, args: RtrsSerializerArgs): Unit = {
    if (IOUtil.exists(args.path)) {
      if (args.overwrite) {
        IOUtil.delete(args.path)
      } else {
        throw new Exception(s"${args.path} already exists")
      }
    }
    IOUtil.mkdirs(args.path)

    var df = spark.table(args.input)
    if (args.drop.nonEmpty) {
      df = df.drop(args.drop.split(","): _*)
    }
    val pkIdx = df.schema.map(_.name).indexOf(args.pk)
    val fields = df.schema.fields.filterNot(_.name.equals(args.pk))
    val inputSchema = StructType(fields)
    val serializer = new TFRecordSerializer(inputSchema)
    val data = df.rdd.map(row => {
      val key = row.getString(pkIdx)
      val data = Row.fromSeq(row.toSeq.zipWithIndex.filterNot(_._2.equals(pkIdx)).map(_._1))
      val example = serializer.serializeExample(data)
      val array = example.getFeatures.toByteArray
      (key, array)
    })

    data.persist(StorageLevel.MEMORY_AND_DISK)
    val count = data.count()
    // 获取
    val partitionCollector = spark.sparkContext.collectionAccumulator[(String, Long)]("TO_RTRS_PART_COUNTER")
    // 重新partition是因为需要确定写入文件的文件名
    val partitioner: Partitioner = new Partitioner {
      override def numPartitions: Int = args.numPartitions

      // 直接使用pk的Hash值取模做分片id
      override def getPartition(key: Any): Int = {
        val pk = key.toString
        val hash = Hash.getInstance(Hash.MURMUR_HASH).hash(pk.getBytes(Charsets.UTF_8)) & Int.MaxValue
        hash % numPartitions
      }
    }
    // 按照key重新打散分片
    val rdd = data.partitionBy(partitioner)

    // 通过分片之后的数据转换为Record对象
    val record = (row: (String, Array[Byte])) => {
      val (pk, value) = row
      RtrsFileRecord.Record.newBuilder()
        .setPk(pk)
        .setSk("")
        .setValue(ByteString.copyFrom(value))
        .setTtl(Int.MaxValue)
        .build()
    }

    val write = (outputStream: OutputStream, record: RtrsFileRecord.Record) => {
      if (record != null) {
        val length = record.getSerializedSize
        if (length > 0) {
          val buffer = ByteBuffer.allocate(4 + length);
          // length
          buffer.putInt(length);
          // body
          buffer.put(record.toByteArray);
          try {
            outputStream.write(buffer.array());
          } finally {
            buffer.clear();
          }
        }
      }
    }

    // 生成每个分片的文件
    rdd.foreachPartition(it => {
      // 当前分片内的数据条数计数
      val partCounter = new AtomicLong(0)
      if (it.hasNext) {
        // 拿到第一行数据，主要目的通过分片函数确定当前分片是第几个分片的生成分片文件名称
        val first = it.next()
        // 当前分片计数+1
        partCounter.addAndGet(1)
        val r = record(first)
        val part = partitioner.getPartition(r.getPk)
        val fileName = s"${args.table}_part$part.data"
        val filePath = s"${args.path}/$fileName"
        // 初始化文件工具类
        val out = IOUtil.getOutputStream(filePath)
        // 写入第一行的数据
        write(out, r)
        it.foreach(row => {
          // 依次写入剩余数据
          write(out, record(row))
          // 计数累加
          partCounter.addAndGet(1)
        })
        // flush文件到HDFS
        out.close()
        partitionCollector.add((fileName, partCounter.longValue()))
      }
    })

    //生成SUCCESS校验文件
    val sucPath = s"${args.path}/SUCCESS"
    // 初始化HDFS客户端
    if (IOUtil.exists(sucPath)) {
      // 文件存在则删除，防止重复
      IOUtil.delete(sucPath)
    }
    // 创建输出目录SUCCESS文件
    val out = IOUtil.getOutputStream(sucPath)
    try {
      // 写入统计信息
      out.write(s"total,${count}\n".getBytes(Charsets.UTF_8))
      partitionCollector.value.forEach((t: (String, Long)) => {
        out.write(s"${t._1},${t._2}\n".getBytes(Charsets.UTF_8))
      })
    } finally {
      out.close()
    }

    println(s"Serialize To Rtrs, total: $count, output: ${args.path}")
  }
}

