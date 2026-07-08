package com.netease.util;

import org.apache.flink.api.common.serialization.BulkWriter;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.connector.file.sink.SlothFileSystemBulkFormatBuilder;
import org.apache.flink.connector.file.sink.writer.DefaultFileWriterBucketFactory;
import org.apache.flink.connector.file.sink.writer.FileWriterBucketFactory;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.functions.sink.filesystem.BucketAssigner;
import org.apache.flink.streaming.api.functions.sink.filesystem.OutputFileConfig;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.CheckpointRollingPolicy;

public class FileSystemSinkUtils<IN> {
    private final Path basePath;
    private final long bucketCheckInterval;
    private final BulkWriter.Factory<IN> writerFactory;
    private final FileWriterBucketFactory<IN> bucketFactory;
    private final BucketAssigner<IN, String> bucketAssigner;
    private final CheckpointRollingPolicy<IN, String> rollingPolicy;
    private final OutputFileConfig outputFileConfig;

    public FileSystemSinkUtils(
            Path basePath,
            BulkWriter.Factory<IN> writerFactory,
            BucketAssigner<IN, String> bucketAssigner,
            CheckpointRollingPolicy<IN, String> rollingPolicy,
            FileWriterBucketFactory<IN> bucketFactory,
            OutputFileConfig outputFileConfig,
            long bucketCheckInterval) {
        this.basePath = basePath;
        this.bucketCheckInterval = bucketCheckInterval;
        this.writerFactory = writerFactory;
        this.bucketFactory = bucketFactory;
        this.bucketAssigner = bucketAssigner;
        this.rollingPolicy = rollingPolicy;
        this.outputFileConfig = outputFileConfig;
    }

    public FileSystemSinkUtils(
            Path basePath,
            BulkWriter.Factory<IN> writerFactory,
            BucketAssigner<IN, String> bucketAssigner) {
        this(
                basePath,
                writerFactory,
                bucketAssigner,
                new CustomOnCheckpointRollingPolicy<>(),
                new DefaultFileWriterBucketFactory<>(),
                OutputFileConfig.builder()
                        .withPartPrefix("part")
                        .withPartSuffix(".gz.parquet")
                        .build(),
                120000L);
    }

    public FileSink<IN> createSlothBulkFormatBuilder() {
        return new SlothFileSystemBulkFormatBuilder<>(
                        basePath,
                        bucketCheckInterval,
                        writerFactory,
                        bucketAssigner,
                        rollingPolicy,
                        bucketFactory,
                        outputFileConfig)
                .build();
    }
}
