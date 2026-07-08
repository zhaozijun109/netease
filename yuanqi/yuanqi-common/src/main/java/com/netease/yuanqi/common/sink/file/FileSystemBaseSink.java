package com.netease.yuanqi.common.sink.file;

import com.netease.yuanqi.common.utils.Preconditions;
import java.io.Serializable;
import org.apache.flink.api.common.serialization.BulkWriter;
import org.apache.flink.api.common.serialization.Encoder;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.connector.file.sink.compactor.FileCompactStrategy;
import org.apache.flink.connector.file.sink.compactor.FileCompactor;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.functions.sink.filesystem.BucketAssigner;
import org.apache.flink.streaming.api.functions.sink.filesystem.OutputFileConfig;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.CheckpointRollingPolicy;

/**
 * File compact with offline jobs, so there is no need to do it.
 *
 * @param <T>
 */
public class FileSystemBaseSink<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Path basePath;
    private final BulkWriter.Factory<T> writerFactory;
    private final Encoder<T> encoder;
    private final long bucketCheckInterval;
    private final BucketAssigner<T, String> bucketAssigner;
    private final CheckpointRollingPolicy<T, String> rollingPolicy;
    private final OutputFileConfig outputFileConfig;
    private boolean enableCompact;
    private FileCompactStrategy fileCompactStrategy;
    private FileCompactor fileCompactor;

    public FileSystemBaseSink(
            Path basePath,
            BulkWriter.Factory<T> writerFactory,
            long bucketCheckInterval,
            BucketAssigner<T, String> bucketAssigner,
            CheckpointRollingPolicy<T, String> rollingPolicy,
            OutputFileConfig outputFileConfig) {
        this(
                basePath,
                writerFactory,
                null,
                bucketCheckInterval,
                bucketAssigner,
                rollingPolicy,
                outputFileConfig);
    }

    public FileSystemBaseSink(
            Path basePath,
            Encoder<T> encoder,
            long bucketCheckInterval,
            BucketAssigner<T, String> bucketAssigner,
            CheckpointRollingPolicy<T, String> rollingPolicy,
            OutputFileConfig outputFileConfig) {
        this(
                basePath,
                null,
                encoder,
                bucketCheckInterval,
                bucketAssigner,
                rollingPolicy,
                outputFileConfig);
    }

    public FileSystemBaseSink(
            Path basePath,
            BulkWriter.Factory<T> writerFactory,
            Encoder<T> encoder,
            long bucketCheckInterval,
            BucketAssigner<T, String> bucketAssigner,
            CheckpointRollingPolicy<T, String> rollingPolicy,
            OutputFileConfig outputFileConfig) {
        this.basePath = basePath;
        this.writerFactory = writerFactory;
        this.encoder = encoder;
        this.bucketCheckInterval = bucketCheckInterval;
        this.bucketAssigner = bucketAssigner;
        this.rollingPolicy = rollingPolicy;
        this.outputFileConfig = outputFileConfig;
    }

    public FileSink<T> createBulkFormatBuilder() {
        Preconditions.checkNotNull(writerFactory, "BulkWriter Factory must not be null");
        return FileSink.forBulkFormat(basePath, writerFactory)
                .withBucketAssigner(bucketAssigner)
                .withBucketCheckInterval(bucketCheckInterval)
                .withRollingPolicy(rollingPolicy)
                .withOutputFileConfig(outputFileConfig)
                .build();
    }

    public FileSink<T> createRowFormatBuilder() {
        Preconditions.checkNotNull(encoder, "Encoder must not be null");
        return FileSink.forRowFormat(basePath, encoder)
                .withBucketAssigner(bucketAssigner)
                .withBucketCheckInterval(bucketCheckInterval)
                .withRollingPolicy(rollingPolicy)
                .withOutputFileConfig(outputFileConfig)
                .build();
    }
}
