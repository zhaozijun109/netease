package com.netease.yuanqi.common.sink.file.lofter;

import com.netease.yuanqi.common.config.ClusterConfigOptions;
import com.netease.yuanqi.common.pojo.avro.ods.ClientMdaLogAvro;
import com.netease.yuanqi.common.pojo.avro.ods.WapMdaLogAvro;
import com.netease.yuanqi.common.pojo.avro.ods.WebMdaLogAvro;
import com.netease.yuanqi.common.pojo.config.KerberosConfig;
import com.netease.yuanqi.common.pojo.proto.ods.ClientMdaLogProtoBuilder;
import com.netease.yuanqi.common.pojo.proto.ods.WapMdaLogProtoBuilder;
import com.netease.yuanqi.common.pojo.proto.ods.WebMdaLogProtoBuilder;
import com.netease.yuanqi.common.sink.file.FileSystemBaseSinkOld;
import com.netease.yuanqi.common.utils.filesystem.FormatCompressWriter;
import java.io.Serializable;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.flink.annotation.Experimental;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.functions.sink.filesystem.OutputFileConfig;
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.DateTimeBucketAssigner;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.OnCheckpointRollingPolicy;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;

@Deprecated
public class LofterSlothFileSystemSink implements Serializable {
    private final String basePath;
    private final KerberosConfig kerberosConfig;

    public LofterSlothFileSystemSink(String basePath) {
        this.basePath = basePath;
        this.kerberosConfig =
                ClusterConfigOptions.getKerberosConfig(
                        ClusterConfigOptions.KerberosConfigEnum.LOFTER);
    }

    // ------------------------------------------------------------------------
    //  AvroParquet sink
    // ------------------------------------------------------------------------
    public FileSink<ClientMdaLogAvro> createClientMdaLogAvroParquetSink(
            CompressionCodecName compressionCodecName) {
        return new FileSystemBaseSinkOld<>(
                        new Path(basePath),
                        FormatCompressWriter.forAvroParquetWriterWithSpecificRecord(
                                ClientMdaLogAvro.class, compressionCodecName),
                        new DateTimeBucketAssigner<>("yyyy-MM-dd"),
                        OnCheckpointRollingPolicy.build(),
                        OutputFileConfig.builder()
                                .withPartPrefix("part-avro")
                                .withPartSuffix(compressionCodecName.getExtension() + ".parquet")
                                .build(),
                        kerberosConfig)
                .createSlothBulkFormatBuilder();
    }

    public FileSink<WapMdaLogAvro> createWapMdaLogAvroParquetSink(
            CompressionCodecName compressionCodecName) {
        return new FileSystemBaseSinkOld<>(
                        new Path(basePath),
                        FormatCompressWriter.forAvroParquetWriterWithSpecificRecord(
                                WapMdaLogAvro.class, compressionCodecName),
                        new DateTimeBucketAssigner<>("yyyy-MM-dd"),
                        OnCheckpointRollingPolicy.build(),
                        OutputFileConfig.builder()
                                .withPartPrefix("part-avro")
                                .withPartSuffix(compressionCodecName.getExtension() + ".parquet")
                                .build(),
                        kerberosConfig)
                .createSlothBulkFormatBuilder();
    }

    public FileSink<WebMdaLogAvro> createWebMdaLogAvroParquetSink(
            CompressionCodecName compressionCodecName) {
        return new FileSystemBaseSinkOld<>(
                        new Path(basePath),
                        FormatCompressWriter.forAvroParquetWriterWithSpecificRecord(
                                WebMdaLogAvro.class, compressionCodecName),
                        new DateTimeBucketAssigner<>("yyyy-MM-dd"),
                        OnCheckpointRollingPolicy.build(),
                        OutputFileConfig.builder()
                                .withPartPrefix("part-avro")
                                .withPartSuffix(compressionCodecName.getExtension() + ".parquet")
                                .build(),
                        kerberosConfig)
                .createSlothBulkFormatBuilder();
    }

    public FileSink<GenericRecord> createAvroGenericRecordParquetSink(
            String schemaString, CompressionCodecName compressionCodecName) {
        Schema schema = (new Schema.Parser()).parse(schemaString);
        return new FileSystemBaseSinkOld<>(
                        new Path(basePath),
                        FormatCompressWriter.forAvroParquetWriterWithGenericRecord(
                                schema, compressionCodecName),
                        new DateTimeBucketAssigner<>("yyyy-MM-dd"),
                        OnCheckpointRollingPolicy.build(),
                        OutputFileConfig.builder()
                                .withPartPrefix("part-avro")
                                .withPartSuffix(compressionCodecName.getExtension() + ".parquet")
                                .build(),
                        kerberosConfig)
                .createSlothBulkFormatBuilder();
    }

    // ------------------------------------------------------------------------
    //  ProtoParquet sink
    // ------------------------------------------------------------------------
    @Deprecated
    @Experimental
    public FileSink<ClientMdaLogProtoBuilder.ClientMdaLogProto> createClientMdaLogProtoParquetSink(
            CompressionCodecName compressionCodecName) {
        return new FileSystemBaseSinkOld<>(
                        new Path(basePath),
                        FormatCompressWriter.forProtoParquetWriterWithReflectRecord(
                                ClientMdaLogProtoBuilder.ClientMdaLogProto.class,
                                compressionCodecName),
                        new DateTimeBucketAssigner<>("yyyy-MM-dd"),
                        OnCheckpointRollingPolicy.build(),
                        OutputFileConfig.builder()
                                .withPartPrefix("part-proto")
                                .withPartSuffix(compressionCodecName.getExtension() + ".parquet")
                                .build(),
                        kerberosConfig)
                .createSlothBulkFormatBuilder();
    }

    @Deprecated
    @Experimental
    public FileSink<WapMdaLogProtoBuilder.WapMdaLogProto> createWapMdaLogProtoParquetSink(
            CompressionCodecName compressionCodecName) {
        return new FileSystemBaseSinkOld<>(
                        new Path(basePath),
                        FormatCompressWriter.forProtoParquetWriterWithReflectRecord(
                                WapMdaLogProtoBuilder.WapMdaLogProto.class, compressionCodecName),
                        new DateTimeBucketAssigner<>("yyyy-MM-dd"),
                        OnCheckpointRollingPolicy.build(),
                        OutputFileConfig.builder()
                                .withPartPrefix("part-proto")
                                .withPartSuffix(compressionCodecName.getExtension() + ".parquet")
                                .build(),
                        kerberosConfig)
                .createSlothBulkFormatBuilder();
    }

    @Deprecated
    @Experimental
    public FileSink<WebMdaLogProtoBuilder.WebMdaLogProto> createWebMdaLogProtoParquetSink(
            CompressionCodecName compressionCodecName) {
        return new FileSystemBaseSinkOld<>(
                        new Path(basePath),
                        FormatCompressWriter.forProtoParquetWriterWithReflectRecord(
                                WebMdaLogProtoBuilder.WebMdaLogProto.class, compressionCodecName),
                        new DateTimeBucketAssigner<>("yyyy-MM-dd"),
                        OnCheckpointRollingPolicy.build(),
                        OutputFileConfig.builder()
                                .withPartPrefix("part-proto")
                                .withPartSuffix(compressionCodecName.getExtension() + ".parquet")
                                .build(),
                        kerberosConfig)
                .createSlothBulkFormatBuilder();
    }
}
