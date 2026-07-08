package com.netease.yuanqi.common.sink.file;

import com.google.protobuf.Message;
import com.netease.yuanqi.common.pojo.archive.ArchiveFormatRow;
import com.netease.yuanqi.common.pojo.archive.ycy.kafka.KafkaArchiveRecord;
import com.netease.yuanqi.common.pojo.avro.ods.ClientMdaLogAvro;
import com.netease.yuanqi.common.pojo.avro.ods.WapMdaLogAvro;
import com.netease.yuanqi.common.pojo.avro.ods.WebMdaLogAvro;
import com.netease.yuanqi.common.pojo.proto.ods.ClientMdaLogProtoBuilder;
import com.netease.yuanqi.common.pojo.proto.ods.WapMdaLogProtoBuilder;
import com.netease.yuanqi.common.pojo.proto.ods.WebMdaLogProtoBuilder;
import com.netease.yuanqi.common.utils.Preconditions;
import com.netease.yuanqi.common.utils.filesystem.FileRollingPolicy;
import com.netease.yuanqi.common.utils.filesystem.FormatCompressWriter;
import com.netease.yuanqi.common.utils.filesystem.bucket.ArchiveFormatRowBucketAssigner;
import com.netease.yuanqi.common.utils.filesystem.bucket.ClientMdaLogAvroBucketAssigner;
import com.netease.yuanqi.common.utils.filesystem.bucket.WapMdaLogAvroBucketAssigner;
import com.netease.yuanqi.common.utils.filesystem.bucket.WebMdaLogAvroBucketAssigner;
import com.netease.yuanqi.common.utils.filesystem.bucket.YcyKafkaLogBucketAssigner;
import java.util.concurrent.TimeUnit;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.flink.annotation.Experimental;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.functions.sink.filesystem.BucketAssigner;
import org.apache.flink.streaming.api.functions.sink.filesystem.OutputFileConfig;
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.DateTimeBucketAssigner;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;

public class FileSystemCommonSink {
    private final String basePath;

    public FileSystemCommonSink(String basePath) {
        this.basePath = basePath;
    }

    // ------------------------------------------------------------------------
    //  ArchiveFormat sink
    // ------------------------------------------------------------------------
    public FileSink<ArchiveFormatRow> createArchiveFormatRowRecordSink() {
        return new FileSystemBaseSink<>(
                        new Path(basePath),
                        FormatCompressWriter.forArchiveFormatRowEncoder(),
                        60000L,
                        new ArchiveFormatRowBucketAssigner("yyyy-MM-dd"),
                        new FileRollingPolicy<>(
                                false,
                                1024 * 1024 * 1024,
                                TimeUnit.MINUTES.toMillis(30),
                                TimeUnit.MINUTES.toMillis(10)),
                        OutputFileConfig.builder()
                                .withPartPrefix("part")
                                .withPartSuffix(".txt")
                                .build())
                .createRowFormatBuilder();
    }

    public FileSink<ArchiveFormatRow> createArchiveFormatRowBulkSink(
            CompressionCodecName compressionCodecName) {
        return createArchiveFormatRowBulkSink(
                compressionCodecName, new ArchiveFormatRowBucketAssigner("yyyy-MM-dd"));
    }

    public FileSink<ArchiveFormatRow> createArchiveFormatRowBulkSink(
            CompressionCodecName compressionCodecName,
            BucketAssigner<ArchiveFormatRow, String> bucketAssigner) {
        return new FileSystemBaseSink<>(
                        new Path(basePath),
                        FormatCompressWriter.forArchiveFormatRowBulkWriter(compressionCodecName),
                        60000L,
                        bucketAssigner,
                        new FileRollingPolicy<>(),
                        OutputFileConfig.builder()
                                .withPartPrefix("part")
                                .withPartSuffix(".txt" + compressionCodecName.getExtension())
                                .build())
                .createBulkFormatBuilder();
    }

    // ------------------------------------------------------------------------
    //  AvroParquet sink
    // ------------------------------------------------------------------------
    public FileSink<ClientMdaLogAvro> createMultiTopicClientMdaLogAvroParquetSink(
            CompressionCodecName compressionCodecName) {
        return new FileSystemBaseSink<>(
                        new Path(basePath),
                        FormatCompressWriter.forAvroParquetWriterWithSpecificRecord(
                                ClientMdaLogAvro.class, compressionCodecName),
                        60000L,
                        new ClientMdaLogAvroBucketAssigner("yyyy-MM-dd"),
                        new FileRollingPolicy<>(),
                        OutputFileConfig.builder()
                                .withPartPrefix("part-avro")
                                .withPartSuffix(compressionCodecName.getExtension() + ".parquet")
                                .build())
                .createBulkFormatBuilder();
    }

    public FileSink<WapMdaLogAvro> createMultiTopicWapMdaLogAvroParquetSink(
            CompressionCodecName compressionCodecName) {
        return new FileSystemBaseSink<>(
                        new Path(basePath),
                        FormatCompressWriter.forAvroParquetWriterWithSpecificRecord(
                                WapMdaLogAvro.class, compressionCodecName),
                        60000L,
                        new WapMdaLogAvroBucketAssigner("yyyy-MM-dd"),
                        new FileRollingPolicy<>(),
                        OutputFileConfig.builder()
                                .withPartPrefix("part-avro")
                                .withPartSuffix(compressionCodecName.getExtension() + ".parquet")
                                .build())
                .createBulkFormatBuilder();
    }

    public FileSink<WebMdaLogAvro> createMultiTopicWebMdaLogAvroParquetSink(
            CompressionCodecName compressionCodecName) {
        return new FileSystemBaseSink<>(
                        new Path(basePath),
                        FormatCompressWriter.forAvroParquetWriterWithSpecificRecord(
                                WebMdaLogAvro.class, compressionCodecName),
                        60000L,
                        new WebMdaLogAvroBucketAssigner("yyyy-MM-dd"),
                        new FileRollingPolicy<>(),
                        OutputFileConfig.builder()
                                .withPartPrefix("part-avro")
                                .withPartSuffix(compressionCodecName.getExtension() + ".parquet")
                                .build())
                .createBulkFormatBuilder();
    }

    public FileSink<ClientMdaLogAvro> createClientMdaLogAvroParquetSink(
            CompressionCodecName compressionCodecName) {
        return new FileSystemBaseSink<>(
                        new Path(basePath),
                        FormatCompressWriter.forAvroParquetWriterWithSpecificRecord(
                                ClientMdaLogAvro.class, compressionCodecName),
                        60000L,
                        new DateTimeBucketAssigner<>("yyyy-MM-dd"),
                        new FileRollingPolicy<>(),
                        OutputFileConfig.builder()
                                .withPartPrefix("part-avro")
                                .withPartSuffix(compressionCodecName.getExtension() + ".parquet")
                                .build())
                .createBulkFormatBuilder();
    }

    public FileSink<WapMdaLogAvro> createWapMdaLogAvroParquetSink(
            CompressionCodecName compressionCodecName) {
        return new FileSystemBaseSink<>(
                        new Path(basePath),
                        FormatCompressWriter.forAvroParquetWriterWithSpecificRecord(
                                WapMdaLogAvro.class, compressionCodecName),
                        60000L,
                        new DateTimeBucketAssigner<>("yyyy-MM-dd"),
                        new FileRollingPolicy<>(),
                        OutputFileConfig.builder()
                                .withPartPrefix("part-avro")
                                .withPartSuffix(compressionCodecName.getExtension() + ".parquet")
                                .build())
                .createBulkFormatBuilder();
    }

    public FileSink<WebMdaLogAvro> createWebMdaLogAvroParquetSink(
            CompressionCodecName compressionCodecName) {
        return new FileSystemBaseSink<>(
                        new Path(basePath),
                        FormatCompressWriter.forAvroParquetWriterWithSpecificRecord(
                                WebMdaLogAvro.class, compressionCodecName),
                        60000L,
                        new DateTimeBucketAssigner<>("yyyy-MM-dd"),
                        new FileRollingPolicy<>(),
                        OutputFileConfig.builder()
                                .withPartPrefix("part-avro")
                                .withPartSuffix(compressionCodecName.getExtension() + ".parquet")
                                .build())
                .createBulkFormatBuilder();
    }

    public <T> FileSink<T> createCommonReflectAvroParquetSink(
            Class<T> clazz,
            CompressionCodecName compressionCodecName,
            BucketAssigner<T, String> bucketAssigner) {
        Preconditions.checkNotNull(clazz, "Reflect avro class must not be null");
        return new FileSystemBaseSink<>(
                        new Path(basePath),
                        FormatCompressWriter.forAvroParquetWriterWithReflectRecord(
                                clazz, compressionCodecName),
                        60000L,
                        bucketAssigner,
                        new FileRollingPolicy<>(),
                        OutputFileConfig.builder()
                                .withPartPrefix("part-avro")
                                .withPartSuffix(compressionCodecName.getExtension() + ".parquet")
                                .build())
                .createBulkFormatBuilder();
    }

    public <T extends KafkaArchiveRecord> FileSink<T> createYcyKafkaLogParquetSink(
            Class<T> clazz, CompressionCodecName compressionCodecName) {
        return createCommonReflectAvroParquetSink(
                clazz, compressionCodecName, new YcyKafkaLogBucketAssigner<>("'dt='yyyy-MM-dd"));
    }

    public <AVRO extends SpecificRecordBase> FileSink<AVRO> createCommonSpecificAvroParquetSink(
            Class<AVRO> clazz,
            CompressionCodecName compressionCodecName,
            BucketAssigner<AVRO, String> bucketAssigner) {
        Preconditions.checkNotNull(clazz, "Specific avro class must not be null");
        return new FileSystemBaseSink<>(
                        new Path(basePath),
                        FormatCompressWriter.forAvroParquetWriterWithSpecificRecord(
                                clazz, compressionCodecName),
                        60000L,
                        bucketAssigner,
                        new FileRollingPolicy<>(),
                        OutputFileConfig.builder()
                                .withPartPrefix("part-avro")
                                .withPartSuffix(compressionCodecName.getExtension() + ".parquet")
                                .build())
                .createBulkFormatBuilder();
    }

    public FileSink<GenericRecord> createCommonGenericAvroRecordParquetSink(
            String schemaString,
            CompressionCodecName compressionCodecName,
            BucketAssigner<GenericRecord, String> bucketAssigner) {
        Schema schema = (new Schema.Parser()).parse(schemaString);
        return new FileSystemBaseSink<>(
                        new Path(basePath),
                        FormatCompressWriter.forAvroParquetWriterWithGenericRecord(
                                schema, compressionCodecName),
                        60000L,
                        bucketAssigner,
                        new FileRollingPolicy<>(),
                        OutputFileConfig.builder()
                                .withPartPrefix("part-avro")
                                .withPartSuffix(compressionCodecName.getExtension() + ".parquet")
                                .build())
                .createBulkFormatBuilder();
    }

    // ------------------------------------------------------------------------
    //  ProtoParquet sink
    // ------------------------------------------------------------------------
    @Deprecated
    @Experimental
    public <PROTO extends Message> FileSink<PROTO> createCommonProtoParquetSink(
            Class<PROTO> clazz,
            CompressionCodecName compressionCodecName,
            BucketAssigner<PROTO, String> bucketAssigner) {
        Preconditions.checkNotNull(clazz, "Reflect proto class must not be null");
        return new FileSystemBaseSink<>(
                        new Path(basePath),
                        FormatCompressWriter.forProtoParquetWriterWithReflectRecord(
                                clazz, compressionCodecName),
                        60000L,
                        bucketAssigner,
                        new FileRollingPolicy<>(),
                        OutputFileConfig.builder()
                                .withPartPrefix("part-proto")
                                .withPartSuffix(compressionCodecName.getExtension() + ".parquet")
                                .build())
                .createBulkFormatBuilder();
    }

    @Deprecated
    @Experimental
    public FileSink<ClientMdaLogProtoBuilder.ClientMdaLogProto> createClientMdaLogProtoParquetSink(
            CompressionCodecName compressionCodecName) {
        return new FileSystemBaseSink<>(
                        new Path(basePath),
                        FormatCompressWriter.forProtoParquetWriterWithReflectRecord(
                                ClientMdaLogProtoBuilder.ClientMdaLogProto.class,
                                compressionCodecName),
                        60000L,
                        new DateTimeBucketAssigner<>("yyyy-MM-dd"),
                        new FileRollingPolicy<>(),
                        OutputFileConfig.builder()
                                .withPartPrefix("part-proto")
                                .withPartSuffix(compressionCodecName.getExtension() + ".parquet")
                                .build())
                .createBulkFormatBuilder();
    }

    @Deprecated
    @Experimental
    public FileSink<WapMdaLogProtoBuilder.WapMdaLogProto> createWapMdaLogProtoParquetSink(
            CompressionCodecName compressionCodecName) {
        return new FileSystemBaseSink<>(
                        new Path(basePath),
                        FormatCompressWriter.forProtoParquetWriterWithReflectRecord(
                                WapMdaLogProtoBuilder.WapMdaLogProto.class, compressionCodecName),
                        60000L,
                        new DateTimeBucketAssigner<>("yyyy-MM-dd"),
                        new FileRollingPolicy<>(),
                        OutputFileConfig.builder()
                                .withPartPrefix("part-proto")
                                .withPartSuffix(compressionCodecName.getExtension() + ".parquet")
                                .build())
                .createBulkFormatBuilder();
    }

    @Deprecated
    @Experimental
    public FileSink<WebMdaLogProtoBuilder.WebMdaLogProto> createWebMdaLogProtoParquetSink(
            CompressionCodecName compressionCodecName) {
        return new FileSystemBaseSink<>(
                        new Path(basePath),
                        FormatCompressWriter.forProtoParquetWriterWithReflectRecord(
                                WebMdaLogProtoBuilder.WebMdaLogProto.class, compressionCodecName),
                        60000L,
                        new DateTimeBucketAssigner<>("yyyy-MM-dd"),
                        new FileRollingPolicy<>(),
                        OutputFileConfig.builder()
                                .withPartPrefix("part-proto")
                                .withPartSuffix(compressionCodecName.getExtension() + ".parquet")
                                .build())
                .createBulkFormatBuilder();
    }
}
