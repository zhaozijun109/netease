package com.netease.yuanqi.common.utils.filesystem;

import com.netease.yuanqi.common.utils.Preconditions;
import java.io.IOException;
import org.apache.flink.streaming.api.functions.sink.filesystem.PartFileInfo;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.CheckpointRollingPolicy;

/**
 * Attention! ForBulkFormat only supports checkpoint rolling policy.
 *
 * @param <IN>
 * @param <BucketID>
 */
public class FileRollingPolicy<IN, BucketID> extends CheckpointRollingPolicy<IN, BucketID> {
    private final boolean rollOnCheckpoint;
    private long rollingFileSize = 0L;
    private long rollingTimeInterval = 0L;
    private long inactivityInterval = 0L;

    public FileRollingPolicy() {
        this.rollOnCheckpoint = true;
    }

    public FileRollingPolicy(
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
            return rollingFileSize == 0L
                    && (rollOnCheckpoint || partFileInfo.getSize() > rollingFileSize);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean shouldRollOnEvent(PartFileInfo<BucketID> partFileInfo, IN t) throws IOException {
        return rollingFileSize != 0L
                && (rollOnCheckpoint || partFileInfo.getSize() > rollingFileSize);
    }

    @Override
    public boolean shouldRollOnProcessingTime(
            PartFileInfo<BucketID> partFileState, long currentTime) {
        return rollingFileSize != 0L
                && (currentTime - partFileState.getCreationTime() >= rollingTimeInterval
                        || currentTime - partFileState.getLastUpdateTime() >= inactivityInterval);
    }
}
