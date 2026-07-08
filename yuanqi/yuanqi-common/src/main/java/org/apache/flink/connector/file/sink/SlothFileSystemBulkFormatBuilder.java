package org.apache.flink.connector.file.sink;

import com.netease.yuanqi.common.pojo.config.KerberosConfig;
import java.io.IOException;
import org.apache.flink.api.common.serialization.BulkWriter;
import org.apache.flink.api.connector.sink2.Sink;
import org.apache.flink.connector.file.sink.committer.FileCommitter;
import org.apache.flink.connector.file.sink.compactor.FileCompactStrategy;
import org.apache.flink.connector.file.sink.compactor.FileCompactor;
import org.apache.flink.connector.file.sink.writer.FileWriter;
import org.apache.flink.connector.file.sink.writer.FileWriterBucketFactory;
import org.apache.flink.connector.file.sink.writer.FileWriterBucketState;
import org.apache.flink.connector.file.sink.writer.FileWriterBucketStateSerializer;
import org.apache.flink.core.fs.Path;
import org.apache.flink.core.io.SimpleVersionedSerializer;
import org.apache.flink.streaming.api.functions.sink.filesystem.BucketAssigner;
import org.apache.flink.streaming.api.functions.sink.filesystem.BucketWriter;
import org.apache.flink.streaming.api.functions.sink.filesystem.BulkBucketWriter;
import org.apache.flink.streaming.api.functions.sink.filesystem.OutputFileConfig;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.CheckpointRollingPolicy;

@Deprecated
public class SlothFileSystemBulkFormatBuilder<IN>
        extends FileSink.BulkFormatBuilder<IN, SlothFileSystemBulkFormatBuilder<IN>> {
    private static final long serialVersionUID = 1L;
    private final Path basePath;
    private final long bucketCheckInterval;
    private final BulkWriter.Factory<IN> writerFactory;
    private final FileWriterBucketFactory<IN> bucketFactory;
    private final BucketAssigner<IN, String> bucketAssigner;
    private final CheckpointRollingPolicy<IN, String> rollingPolicy;
    private final OutputFileConfig outputFileConfig;
    private final KerberosConfig kerberosConfig;
    private boolean isCompactDisabledExplicitly;
    private FileCompactStrategy fileCompactStrategy;
    private FileCompactor fileCompactor;

    public SlothFileSystemBulkFormatBuilder(
            Path basePath,
            long bucketCheckInterval,
            BulkWriter.Factory<IN> writerFactory,
            BucketAssigner<IN, String> bucketAssigner,
            CheckpointRollingPolicy<IN, String> rollingPolicy,
            FileWriterBucketFactory<IN> bucketFactory,
            OutputFileConfig outputFileConfig,
            KerberosConfig kerberosConfig) {
        super(
                basePath,
                bucketCheckInterval,
                writerFactory,
                bucketAssigner,
                rollingPolicy,
                bucketFactory,
                outputFileConfig);
        this.basePath = basePath;
        this.bucketCheckInterval = bucketCheckInterval;
        this.writerFactory = writerFactory;
        this.bucketAssigner = bucketAssigner;
        this.rollingPolicy = rollingPolicy;
        this.bucketFactory = bucketFactory;
        this.outputFileConfig = outputFileConfig;
        this.kerberosConfig = kerberosConfig;
    }

    public SlothFileSystemBulkFormatBuilder(
            Path basePath,
            long bucketCheckInterval,
            BulkWriter.Factory<IN> writerFactory,
            BucketAssigner<IN, String> bucketAssigner,
            CheckpointRollingPolicy<IN, String> rollingPolicy,
            FileWriterBucketFactory<IN> bucketFactory,
            OutputFileConfig outputFileConfig,
            KerberosConfig kerberosConfig,
            FileCompactStrategy fileCompactStrategy,
            FileCompactor fileCompactor) {
        super(
                basePath,
                bucketCheckInterval,
                writerFactory,
                bucketAssigner,
                rollingPolicy,
                bucketFactory,
                outputFileConfig);
        this.basePath = basePath;
        this.bucketCheckInterval = bucketCheckInterval;
        this.writerFactory = writerFactory;
        this.bucketAssigner = bucketAssigner;
        this.rollingPolicy = rollingPolicy;
        this.bucketFactory = bucketFactory;
        this.outputFileConfig = outputFileConfig;
        this.kerberosConfig = kerberosConfig;
        this.isCompactDisabledExplicitly = false;
        this.fileCompactStrategy = fileCompactStrategy;
        this.fileCompactor = fileCompactor;
    }

    @Override
    FileWriter<IN> createWriter(Sink.InitContext context) throws IOException {
        OutputFileConfig writerFileConfig =
                this.fileCompactStrategy == null
                        ? this.outputFileConfig
                        : OutputFileConfig.builder()
                                .withPartPrefix(
                                        "compacted-" + this.outputFileConfig.getPartPrefix())
                                .withPartSuffix(this.outputFileConfig.getPartSuffix())
                                .build();
        return new FileWriter<>(
                this.basePath,
                context.metricGroup(),
                this.bucketAssigner,
                this.bucketFactory,
                createBucketWriter(),
                this.rollingPolicy,
                writerFileConfig,
                context.getProcessingTimeService(),
                this.bucketCheckInterval);
    }

    @Override
    FileCommitter createCommitter() throws IOException {
        return new FileCommitter(createBucketWriter());
    }

    @Override
    boolean isCompactDisabledExplicitly() {
        return this.isCompactDisabledExplicitly;
    }

    @Override
    FileCompactStrategy getCompactStrategy() {
        return this.fileCompactStrategy;
    }

    @Override
    FileCompactor getFileCompactor() {
        return this.fileCompactor;
    }

    @Override
    SimpleVersionedSerializer<FileSinkCommittable> getCommittableSerializer() throws IOException {
        BucketWriter<IN, String> slothBucketWriter = createBucketWriter();
        return new FileSinkCommittableSerializer(
                slothBucketWriter.getProperties().getPendingFileRecoverableSerializer(),
                slothBucketWriter.getProperties().getInProgressFileRecoverableSerializer());
    }

    @Override
    SimpleVersionedSerializer<FileWriterBucketState> getWriterStateSerializer() throws IOException {
        BucketWriter<IN, String> slothBucketWriter = createBucketWriter();
        return new FileWriterBucketStateSerializer(
                slothBucketWriter.getProperties().getInProgressFileRecoverableSerializer(),
                slothBucketWriter.getProperties().getPendingFileRecoverableSerializer());
    }

    @Override
    BucketWriter<IN, String> createBucketWriter() throws IOException {
        return new BulkBucketWriter<>(
                SlothHadoopFileSystem.getSlothHadoopFileSystem(kerberosConfig)
                        .createRecoverableWriter(),
                this.writerFactory);
    }
}
