package com.netease.yuanqi.common.sink.file;

import com.netease.yuanqi.common.pojo.config.KerberosConfig;
import java.io.Serializable;
import org.apache.flink.api.common.serialization.BulkWriter;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.connector.file.sink.SlothFileSystemBulkFormatBuilder;
import org.apache.flink.connector.file.sink.compactor.FileCompactStrategy;
import org.apache.flink.connector.file.sink.compactor.FileCompactor;
import org.apache.flink.connector.file.sink.writer.DefaultFileWriterBucketFactory;
import org.apache.flink.connector.file.sink.writer.FileWriterBucketFactory;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.functions.sink.filesystem.BucketAssigner;
import org.apache.flink.streaming.api.functions.sink.filesystem.OutputFileConfig;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.CheckpointRollingPolicy;

@Deprecated
public class FileSystemBaseSinkOld<IN> implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Path basePath;
    private final long bucketCheckInterval;
    private final BulkWriter.Factory<IN> writerFactory;
    private final FileWriterBucketFactory<IN> bucketFactory;
    private final BucketAssigner<IN, String> bucketAssigner;
    private final CheckpointRollingPolicy<IN, String> rollingPolicy;
    private final OutputFileConfig outputFileConfig;
    private final KerberosConfig kerberosConfig;
    private final boolean enableCompact;
    private final FileCompactStrategy fileCompactStrategy;
    private final FileCompactor fileCompactor;

    public FileSystemBaseSinkOld(
            Path basePath,
            BulkWriter.Factory<IN> writerFactory,
            BucketAssigner<IN, String> bucketAssigner,
            CheckpointRollingPolicy<IN, String> rollingPolicy,
            OutputFileConfig outputFileConfig,
            KerberosConfig kerberosConfig) {
        this(
                basePath,
                writerFactory,
                bucketAssigner,
                rollingPolicy,
                new DefaultFileWriterBucketFactory<>(),
                outputFileConfig,
                120000L,
                kerberosConfig,
                false,
                null,
                null);
    }

    public FileSystemBaseSinkOld(
            Path basePath,
            BulkWriter.Factory<IN> writerFactory,
            BucketAssigner<IN, String> bucketAssigner,
            CheckpointRollingPolicy<IN, String> rollingPolicy,
            OutputFileConfig outputFileConfig,
            KerberosConfig kerberosConfig,
            boolean enableCompact,
            FileCompactStrategy fileCompactStrategy,
            FileCompactor fileCompactor) {
        this(
                basePath,
                writerFactory,
                bucketAssigner,
                rollingPolicy,
                new DefaultFileWriterBucketFactory<>(),
                outputFileConfig,
                120000L,
                kerberosConfig,
                true,
                fileCompactStrategy,
                fileCompactor);
    }

    public FileSystemBaseSinkOld(
            Path basePath,
            BulkWriter.Factory<IN> writerFactory,
            BucketAssigner<IN, String> bucketAssigner,
            CheckpointRollingPolicy<IN, String> rollingPolicy,
            FileWriterBucketFactory<IN> bucketFactory,
            OutputFileConfig outputFileConfig,
            long bucketCheckInterval,
            KerberosConfig kerberosConfig,
            boolean enableCompact,
            FileCompactStrategy fileCompactStrategy,
            FileCompactor fileCompactor) {
        this.basePath = basePath;
        this.bucketCheckInterval = bucketCheckInterval;
        this.writerFactory = writerFactory;
        this.bucketFactory = bucketFactory;
        this.bucketAssigner = bucketAssigner;
        this.rollingPolicy = rollingPolicy;
        this.outputFileConfig = outputFileConfig;
        this.kerberosConfig = kerberosConfig;
        this.enableCompact = enableCompact;
        this.fileCompactStrategy = fileCompactStrategy;
        this.fileCompactor = fileCompactor;
    }

    public FileSink<IN> createSlothBulkFormatBuilder() {
        return fileCompactStrategy != null && fileCompactor != null
                ? enableCompact
                        ? new SlothFileSystemBulkFormatBuilder<>(
                                        basePath,
                                        bucketCheckInterval,
                                        writerFactory,
                                        bucketAssigner,
                                        rollingPolicy,
                                        bucketFactory,
                                        outputFileConfig,
                                        kerberosConfig,
                                        fileCompactStrategy,
                                        fileCompactor)
                                .enableCompact(fileCompactStrategy, fileCompactor)
                                .build()
                        : new SlothFileSystemBulkFormatBuilder<>(
                                        basePath,
                                        bucketCheckInterval,
                                        writerFactory,
                                        bucketAssigner,
                                        rollingPolicy,
                                        bucketFactory,
                                        outputFileConfig,
                                        kerberosConfig,
                                        fileCompactStrategy,
                                        fileCompactor)
                                .disableCompact()
                                .build()
                : new SlothFileSystemBulkFormatBuilder<>(
                                basePath,
                                bucketCheckInterval,
                                writerFactory,
                                bucketAssigner,
                                rollingPolicy,
                                bucketFactory,
                                outputFileConfig,
                                kerberosConfig)
                        .build();
    }

    public FileSink<IN> createDefaultBulkFormatBuilder() {
        return FileSink.forBulkFormat(basePath, writerFactory)
                .withBucketAssigner(bucketAssigner)
                .withBucketCheckInterval(bucketCheckInterval)
                .withRollingPolicy(rollingPolicy)
                .withOutputFileConfig(outputFileConfig)
                .build();
    }
}
