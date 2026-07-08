package com.netease.wm.hubble.common

import org.apache.avro.specific.SpecificRecordBase
import org.apache.flink.api.common.serialization.{BulkWriter, SimpleStringEncoder}
import org.apache.flink.connector.file.sink.writer.DefaultFileWriterBucketFactory
import org.apache.flink.connector.file.sink.{FileSink, SlothBulkFormatBuilder}
import org.apache.flink.core.fs.{FSDataOutputStream, Path}
import org.apache.flink.core.io.SimpleVersionedSerializer
import org.apache.flink.streaming.api.functions.sink.filesystem._
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.{DateTimeBucketAssigner, SimpleVersionedStringSerializer}
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.{CheckpointRollingPolicy, DefaultRollingPolicy}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.io.compress.{CompressionCodec, CompressionCodecFactory, CompressionOutputStream}
import org.apache.parquet.hadoop.metadata.CompressionCodecName
import org.json4s.DefaultFormats

import java.io.IOException
import java.sql.Timestamp
import java.time.format.{DateTimeFormatter, DateTimeParseException}
import java.time.{Instant, ZoneId, ZoneOffset}
import java.util.Date
import java.util.concurrent.TimeUnit
import scala.reflect.ClassTag

object FileSinkHelper {
  val SLOTH_MODE = true

  def createTextFileSink(outputPath: String): FileSink[String] = {
    if(SLOTH_MODE) createSlothFileSink(outputPath) else createSimpleFileSink(outputPath)
  }

  def createBinlogFileSink(outputPath: String): FileSink[BinlogRow] = {
    if(SLOTH_MODE) createSlothBinlogSink(outputPath) else createBinlogSink(outputPath)
  }

  def createPartitionBinlogFileSink(outputPath: String): FileSink[BinlogRow] = {
    if(SLOTH_MODE) createSlothPartitionBinlogSink(outputPath) else createPartitionBinlogSink(outputPath)
  }

  def createHubbleLogFileSink(outputPath: String): FileSink[HubbleEvent] = {
    if(SLOTH_MODE) createSlothHubbleLogSink(outputPath) else createHubbleLogSink(outputPath)
  }

  def createBackendLogFileSink(outputPath: String): FileSink[BackendEvent] = {
    if(SLOTH_MODE) createSlothBackendLogSink(outputPath) else createBackendLogSink(outputPath)
  }

  def createParquetFileSink[T <: SpecificRecordBase :ClassTag](outputPath: String): FileSink[T] = {
    if(SLOTH_MODE) {
      createSlothParquetSink[T](outputPath, new DatePartitionBucketAssigner[T]("yyyy-MM-dd"))
    } else {
      createParquetSink[T](outputPath, new DatePartitionBucketAssigner[T]("yyyy-MM-dd"))
    }
  }

  def createParquetFileSink[T <: SpecificRecordBase :ClassTag](outputPath: String, bucketAssigner: BucketAssigner[T, String]): FileSink[T] = {
    if(SLOTH_MODE) {
      createSlothParquetSink[T](outputPath, bucketAssigner)
    } else {
      createParquetSink[T](outputPath, bucketAssigner)
    }
  }

  private def createSlothFileSink(outputPath: String): FileSink[String] = {
    new SlothBulkFormatBuilder(
      basePath = new Path(outputPath),
      writerFactory = new CompressWriterFactory("gzip", new SimpleStringEncoder[String]),
      assigner = new DateTimeBucketAssigner[String]("yyyy-MM-dd"),
      outputFileConfig = new OutputFileConfig("part", ".txt.gz"),
      policy = new DelegateCheckpointRollingPolicy(
        DefaultRollingPolicy.builder()
          .withRolloverInterval(TimeUnit.MINUTES.toMillis(30))
          .withInactivityInterval(TimeUnit.MINUTES.toMillis(10))
          .withMaxPartSize(1024 * 1024 * 1024)
          .build()),
      bucketFactory = new DefaultFileWriterBucketFactory()
    ).build()
  }

  private def createSimpleFileSink(outputPath: String): FileSink[String] = {
        FileSink.forBulkFormat(new Path(outputPath), new CompressWriterFactory("gzip", new SimpleStringEncoder[String]))
          .withBucketAssigner(new DateTimeBucketAssigner[String]("yyyy-MM-dd"))
          .withOutputFileConfig(new OutputFileConfig("part", ".txt.gz"))
          .withRollingPolicy(
            new DelegateCheckpointRollingPolicy(
              DefaultRollingPolicy.builder()
                .withRolloverInterval(TimeUnit.MINUTES.toMillis(30))
                .withInactivityInterval(TimeUnit.MINUTES.toMillis(10))
                .withMaxPartSize(1024 * 1024 * 1024)
                .build())
          )
          .build()
  }

  private def createSlothBinlogSink(outputPath: String): FileSink[BinlogRow] = {
    new SlothBulkFormatBuilder(
      basePath = new Path(outputPath),
      writerFactory = BinlogCompressWriterFactory("gzip"),
      assigner = new DateTimeBucketAssigner[BinlogRow]("yyyy-MM-dd"),
      outputFileConfig = new OutputFileConfig("part", ".txt.gz"),
      policy = BinlogDelegateCheckpointRollingPolicy(
        DefaultRollingPolicy.builder()
          .withRolloverInterval(TimeUnit.MINUTES.toMillis(30))
          .withInactivityInterval(TimeUnit.MINUTES.toMillis(10))
          .withMaxPartSize(1024 * 1024 * 1024)
          .build()),
      bucketFactory = new DefaultFileWriterBucketFactory()
    ).build()
  }

  private def createBinlogSink(outputPath: String): FileSink[BinlogRow] = {
    FileSink.forBulkFormat(new Path(outputPath), BinlogCompressWriterFactory("gzip"))
      .withBucketAssigner(new DateTimeBucketAssigner[BinlogRow]("yyyy-MM-dd"))
      .withOutputFileConfig(new OutputFileConfig("part", ".txt.gz"))
      .withRollingPolicy(
        BinlogDelegateCheckpointRollingPolicy(
          DefaultRollingPolicy.builder()
            .withRolloverInterval(TimeUnit.MINUTES.toMillis(30))
            .withInactivityInterval(TimeUnit.MINUTES.toMillis(10))
            .withMaxPartSize(1024 * 1024 * 1024)
            .build())
      )
      .build()
  }


  private def createSlothPartitionBinlogSink(outputPath: String): FileSink[BinlogRow] = {
    new SlothBulkFormatBuilder(
      basePath = new Path(outputPath),
      writerFactory = BinlogCompressWriterFactory("gzip"),
      assigner = new BinlogBucketAssigner("yyyy-MM-dd"),
      outputFileConfig = new OutputFileConfig("part", ".txt.gz"),
      policy = BinlogDelegateCheckpointRollingPolicy(
        DefaultRollingPolicy.builder()
          .withRolloverInterval(TimeUnit.MINUTES.toMillis(30))
          .withInactivityInterval(TimeUnit.MINUTES.toMillis(10))
          .withMaxPartSize(1024 * 1024 * 1024)
          .build()),
      bucketFactory = new DefaultFileWriterBucketFactory()
    ).build()
  }

  private def createPartitionBinlogSink(outputPath: String): FileSink[BinlogRow] = {
    FileSink.forBulkFormat(new Path(outputPath), BinlogCompressWriterFactory("gzip"))
      .withBucketAssigner(new BinlogBucketAssigner("yyyy-MM-dd"))
      .withOutputFileConfig(new OutputFileConfig("part", ".txt.gz"))
      .withRollingPolicy(
        BinlogDelegateCheckpointRollingPolicy(
          DefaultRollingPolicy.builder()
            .withRolloverInterval(TimeUnit.MINUTES.toMillis(30))
            .withInactivityInterval(TimeUnit.MINUTES.toMillis(10))
            .withMaxPartSize(1024 * 1024 * 1024)
            .build())
      )
      .build()
  }

  private def createParquetSink[T <: SpecificRecordBase :ClassTag](outputPath: String, bucketAssigner: BucketAssigner[T, String]): FileSink[T] = {
    val schema = implicitly[reflect.ClassTag[T]].runtimeClass.newInstance().asInstanceOf[SpecificRecordBase].getSchema
    val bucketBuilder = FileSink
      .forBulkFormat(
        new Path(outputPath),
        CompressParquetWriterFactory.forScala[T](schema, CompressionCodecName.SNAPPY)
      )

    bucketBuilder.withBucketAssigner(bucketAssigner)
    bucketBuilder.withOutputFileConfig(new OutputFileConfig("part",".snappy.parquet"))
    val policy = new DelegateCheckpointRollingPolicy[T](
      DefaultRollingPolicy.builder()
        .withRolloverInterval(TimeUnit.MINUTES.toMillis(30))
        .withInactivityInterval(TimeUnit.MINUTES.toMillis(10))
        .withMaxPartSize(1024 * 1024 * 1024)
        .build())
    bucketBuilder.withRollingPolicy(policy)
    bucketBuilder.build()
  }

  private def createSlothParquetSink[T <: SpecificRecordBase :ClassTag](outputPath: String, bucketAssigner: BucketAssigner[T, String]): FileSink[T] = {
    val schema = implicitly[reflect.ClassTag[T]].runtimeClass.newInstance().asInstanceOf[SpecificRecordBase].getSchema

    val policy = new DelegateCheckpointRollingPolicy[T](
      DefaultRollingPolicy.builder()
        .withRolloverInterval(TimeUnit.MINUTES.toMillis(30))
        .withInactivityInterval(TimeUnit.MINUTES.toMillis(10))
        .withMaxPartSize(1024 * 1024 * 1024)
        .build())
    new SlothBulkFormatBuilder(
      basePath = new Path(outputPath),
      writerFactory = CompressParquetWriterFactory.forScala[T](schema, CompressionCodecName.SNAPPY),
      assigner = bucketAssigner,
      outputFileConfig = new OutputFileConfig("part", ".snappy.parquet"),
      policy = policy,
      bucketFactory = new DefaultFileWriterBucketFactory()
    ).build()
  }

  class DatePartitionBucketAssigner[T](dateFormat: String) extends BucketAssigner[T, String] {
    @transient var formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat).withZone(ZoneId.systemDefault())

    override def getBucketId(element: T, context: BucketAssigner.Context): String = {
      if(formatter == null) {
        formatter = DateTimeFormatter.ofPattern(dateFormat).withZone(ZoneId.systemDefault())
      }
      "dt=" + formatter.format(Instant.ofEpochMilli(context.currentProcessingTime()))
    }

    override def getSerializer: SimpleVersionedSerializer[String] = SimpleVersionedStringSerializer.INSTANCE
  }

  case class BinlogBucketAssigner(dateFormat: String) extends BucketAssigner[BinlogRow, String] {
    @transient var formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat).withZone(ZoneId.systemDefault())

    override def getBucketId(element: BinlogRow, context: BucketAssigner.Context): String = {
      if(formatter == null) {
        formatter = DateTimeFormatter.ofPattern(dateFormat).withZone(ZoneId.systemDefault())
      }
      element.table + "/" + formatter.format(Instant.ofEpochMilli(element.opTime))
    }

    override def getSerializer: SimpleVersionedSerializer[String] = SimpleVersionedStringSerializer.INSTANCE
  }

  case class BinlogDelegateCheckpointRollingPolicy(delegate: RollingPolicy[BinlogRow, String]) extends CheckpointRollingPolicy[BinlogRow, String] {

    override def shouldRollOnEvent(partFileState: PartFileInfo[String], element: BinlogRow): Boolean = {
      delegate.shouldRollOnEvent(partFileState, element)
    }

    override def shouldRollOnProcessingTime(partFileState: PartFileInfo[String], currentTime: Long): Boolean = {
      delegate.shouldRollOnProcessingTime(partFileState, currentTime)
    }
  }

  case class BinlogCompressWriterFactory(codecName: String) extends BulkWriter.Factory[BinlogRow]{

    override def create(out: FSDataOutputStream): BulkWriter[BinlogRow] = {
      val hadoopCodec = new CompressionCodecFactory(new Configuration()).getCodecByName(codecName)
      new BinlogCompressionBulkWriter(out, hadoopCodec)
    }
  }

  class BinlogCompressionBulkWriter(val outputStream: FSDataOutputStream, val compressionCodec: CompressionCodec) extends BulkWriter[BinlogRow]{
    private val compressor: CompressionOutputStream = compressionCodec.createOutputStream(outputStream)

    @throws[IOException]
    def addElement(element: BinlogRow): Unit = {
      import org.json4s.jackson.Serialization.write
      implicit val format = DefaultFormats
      import element._

      val oldv2 = old.map {
        case (key, value: Date) => key -> value.getTime
        case (key, value: Timestamp) => key -> value.getTime
        case (key: String, value: String) if ((key == "update_time" || key == "create_time") && value.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$")) =>
          key -> Instant.parse(value).getEpochSecond
        case (key, value) => key -> value
      }

      val flattedRow = data.map {
        case (key, value: java.util.Date) => key -> value.getTime
        case (key, value: java.sql.Timestamp) => key -> value.getTime
        case (key: String, value: String) if ((key == "update_time" || key == "create_time") && value.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$")) =>
          key -> Instant.parse(value).getEpochSecond
        case (key, value) => key -> value
      } ++ Map("_tbl" -> table, "_bin_op" -> op, "_bin_op_time" -> opTime, "_bin_op_seqno" -> seqno, "_bin_old" -> oldv2)

      val content = write(flattedRow)
      compressor.write(content.getBytes("utf-8"))
      compressor.write('\n')
    }

    @throws[IOException]
    def flush(): Unit = {
      compressor.flush()
      outputStream.flush()
    }

    @throws[IOException]
    def finish(): Unit = {
      compressor.finish()
      outputStream.sync()
    }
  }

  private def createSlothHubbleLogSink(outputPath: String): FileSink[HubbleEvent] = {
    new SlothBulkFormatBuilder(
      basePath = new Path(outputPath),
      writerFactory = new CompressWriterFactory("gzip", new HubbleEventEncoder),
      assigner = HubbleEventBucketAssigner("yyyy-MM-dd"),
      outputFileConfig = new OutputFileConfig("part", ".txt.gz"),
      policy = new DelegateCheckpointRollingPolicy(
        DefaultRollingPolicy.builder()
          .withRolloverInterval(TimeUnit.MINUTES.toMillis(30))
          .withInactivityInterval(TimeUnit.MINUTES.toMillis(10))
          .withMaxPartSize(1024 * 1024 * 1024)
          .build()),
      bucketFactory = new DefaultFileWriterBucketFactory()
    ).build()
  }

  private def createHubbleLogSink(outputPath: String): FileSink[HubbleEvent] = {
    FileSink.forBulkFormat(new Path(outputPath), new CompressWriterFactory("gzip", new HubbleEventEncoder))
      .withBucketAssigner(new HubbleEventBucketAssigner("yyyy-MM-dd"))
      .withOutputFileConfig(new OutputFileConfig("part", ".txt.gz"))
      .withRollingPolicy(
        new DelegateCheckpointRollingPolicy(
          DefaultRollingPolicy.builder()
            .withRolloverInterval(TimeUnit.MINUTES.toMillis(30))
            .withInactivityInterval(TimeUnit.MINUTES.toMillis(10))
            .withMaxPartSize(1024 * 1024 * 1024)
            .build())
      )
      .build()
  }

  private def createSlothBackendLogSink(outputPath: String): FileSink[BackendEvent] = {
    new SlothBulkFormatBuilder(
      basePath = new Path(outputPath),
      writerFactory = new CompressWriterFactory("gzip", new BackendEventEncoder),
      assigner = BackendEventBucketAssigner("yyyy-MM-dd"),
      outputFileConfig = new OutputFileConfig("part", ".txt.gz"),
      policy = new DelegateCheckpointRollingPolicy(
        DefaultRollingPolicy.builder()
          .withRolloverInterval(TimeUnit.MINUTES.toMillis(30))
          .withInactivityInterval(TimeUnit.MINUTES.toMillis(10))
          .withMaxPartSize(1024 * 1024 * 1024)
          .build()),
      bucketFactory = new DefaultFileWriterBucketFactory()
    ).build()
  }

  private def createBackendLogSink(outputPath: String): FileSink[BackendEvent] = {
    FileSink.forBulkFormat(new Path(outputPath), new CompressWriterFactory("gzip", new BackendEventEncoder))
      .withBucketAssigner(new BackendEventBucketAssigner("yyyy-MM-dd"))
      .withOutputFileConfig(new OutputFileConfig("part", ".txt.gz"))
      .withRollingPolicy(
        new DelegateCheckpointRollingPolicy(
          DefaultRollingPolicy.builder()
            .withRolloverInterval(TimeUnit.MINUTES.toMillis(30))
            .withInactivityInterval(TimeUnit.MINUTES.toMillis(10))
            .withMaxPartSize(1024 * 1024 * 1024)
            .build())
      )
      .build()
  }

}
