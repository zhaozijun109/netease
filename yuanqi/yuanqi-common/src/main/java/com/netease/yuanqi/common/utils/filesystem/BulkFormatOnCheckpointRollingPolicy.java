package com.netease.yuanqi.common.utils.filesystem;

import com.netease.yuanqi.common.utils.Preconditions;
import java.io.IOException;
import org.apache.flink.annotation.Experimental;
import org.apache.flink.streaming.api.functions.sink.filesystem.PartFileInfo;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.CheckpointRollingPolicy;

@Deprecated
@Experimental
public class BulkFormatOnCheckpointRollingPolicy<T, BucketID>
        extends CheckpointRollingPolicy<T, BucketID> {
    private final boolean rollOnCheckpoint;
    private final long rollingFileSize;
    private final long rollingTimeInterval;
    private final long inactivityInterval;

    public BulkFormatOnCheckpointRollingPolicy(
            boolean rollOnCheckpoint,
            long rollingFileSize,
            long rollingTimeInterval,
            long inactivityInterval) {
        this.rollOnCheckpoint = rollOnCheckpoint;
        Preconditions.checkArgument(rollingFileSize > 0L);
        Preconditions.checkArgument(rollingTimeInterval > 0L);
        Preconditions.checkArgument(inactivityInterval > 0L);
        this.rollingFileSize = rollingFileSize;
        this.rollingTimeInterval = rollingTimeInterval;
        this.inactivityInterval = inactivityInterval;
    }

    @Override
    public boolean shouldRollOnCheckpoint(PartFileInfo<BucketID> partFileInfo) {
        try {
            return rollOnCheckpoint || partFileInfo.getSize() > rollingFileSize;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean shouldRollOnEvent(PartFileInfo<BucketID> partFileInfo, T t) throws IOException {
        return partFileInfo.getSize() > rollingFileSize;
    }

    @Override
    public boolean shouldRollOnProcessingTime(
            PartFileInfo<BucketID> partFileState, long currentTime) {
        return currentTime - partFileState.getCreationTime() >= rollingTimeInterval
                || currentTime - partFileState.getLastUpdateTime() >= inactivityInterval;
    }
}
