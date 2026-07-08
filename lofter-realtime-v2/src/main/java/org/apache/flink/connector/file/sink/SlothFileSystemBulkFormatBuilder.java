package org.apache.flink.connector.file.sink;

import org.apache.flink.api.common.serialization.BulkWriter;
import org.apache.flink.api.connector.sink.Sink;
import org.apache.flink.connector.file.sink.committer.FileCommitter;
import org.apache.flink.connector.file.sink.writer.FileWriter;
import org.apache.flink.connector.file.sink.writer.FileWriterBucketFactory;
import org.apache.flink.connector.file.sink.writer.FileWriterBucketState;
import org.apache.flink.connector.file.sink.writer.FileWriterBucketStateSerializer;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.core.fs.Path;
import org.apache.flink.core.io.SimpleVersionedSerializer;
import org.apache.flink.streaming.api.functions.sink.filesystem.BucketAssigner;
import org.apache.flink.streaming.api.functions.sink.filesystem.BucketWriter;
import org.apache.flink.streaming.api.functions.sink.filesystem.BulkBucketWriter;
import org.apache.flink.streaming.api.functions.sink.filesystem.OutputFileConfig;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.CheckpointRollingPolicy;

import java.io.IOException;

public class SlothFileSystemBulkFormatBuilder<IN>
        extends FileSink.BulkFormatBuilder<IN, SlothFileSystemBulkFormatBuilder<IN>> {
    private final Path basePath;
    private final long bucketCheckInterval;
    private final BulkWriter.Factory<IN> writerFactory;
    private final FileWriterBucketFactory<IN> bucketFactory;
    private final BucketAssigner<IN, String> bucketAssigner;
    private final CheckpointRollingPolicy<IN, String> rollingPolicy;
    private final OutputFileConfig outputFileConfig;

    public SlothFileSystemBulkFormatBuilder(
            Path basePath,
            long bucketCheckInterval,
            BulkWriter.Factory<IN> writerFactory,
            BucketAssigner<IN, String> bucketAssigner,
            CheckpointRollingPolicy<IN, String> rollingPolicy,
            FileWriterBucketFactory<IN> bucketFactory,
            OutputFileConfig outputFileConfig) {
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
        this.bucketFactory = bucketFactory;
        this.bucketAssigner = bucketAssigner;
        this.rollingPolicy = rollingPolicy;
        this.outputFileConfig = outputFileConfig;
    }

    @Override
    FileWriter<IN> createWriter(Sink.InitContext context) throws IOException {
        return new FileWriter<>(
                this.basePath,
                context.metricGroup(),
                this.bucketAssigner,
                this.bucketFactory,
                createSlothBucketWriter(),
                this.rollingPolicy,
                this.outputFileConfig,
                context.getProcessingTimeService(),
                this.bucketCheckInterval);
    }

    @Override
    FileCommitter createCommitter() throws IOException {
        return new FileCommitter(createSlothBucketWriter());
    }

    @Override
    SimpleVersionedSerializer<FileSinkCommittable> getCommittableSerializer() throws IOException {
        BucketWriter<IN, String> slothBucketWriter = createSlothBucketWriter();
        return new FileSinkCommittableSerializer(
                slothBucketWriter.getProperties().getPendingFileRecoverableSerializer(),
                slothBucketWriter.getProperties().getInProgressFileRecoverableSerializer());
    }

    @Override
    SimpleVersionedSerializer<FileWriterBucketState> getWriterStateSerializer() throws IOException {
        BucketWriter<IN, String> slothBucketWriter = createSlothBucketWriter();
        return new FileWriterBucketStateSerializer(
                slothBucketWriter.getProperties().getInProgressFileRecoverableSerializer(),
                slothBucketWriter.getProperties().getPendingFileRecoverableSerializer());
    }

    private BucketWriter<IN, String> createSlothBucketWriter() throws IOException {
        FileSystem fs = SlothHadoopFileSystemUtils.getSlothHadoopFileSystem();
        return new BulkBucketWriter<>(fs.createRecoverableWriter(), this.writerFactory);
    }
}
