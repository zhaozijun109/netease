package com.netease.util;

import org.apache.flink.configuration.MemorySize;
import org.apache.flink.streaming.api.functions.sink.filesystem.PartFileInfo;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.CheckpointRollingPolicy;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.DefaultRollingPolicy;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class CustomOnCheckpointRollingPolicy<T, BucketID>
        extends CheckpointRollingPolicy<T, BucketID> {
    private final DefaultRollingPolicy<T, BucketID> defaultPolicy;

    public CustomOnCheckpointRollingPolicy() {
        this.defaultPolicy =
                DefaultRollingPolicy.builder()
                        .withRolloverInterval(TimeUnit.DAYS.toMillis(1))
                        .withInactivityInterval(TimeUnit.HOURS.toMillis(1))
                        .withMaxPartSize(MemorySize.ofMebiBytes(1).getBytes())
                        .build();
    }

    @Override
    public boolean shouldRollOnCheckpoint(PartFileInfo<BucketID> partFileState) {
        try {
            return defaultPolicy.shouldRollOnCheckpoint(partFileState);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean shouldRollOnEvent(PartFileInfo<BucketID> partFileState, T t) throws IOException {
        return defaultPolicy.shouldRollOnEvent(partFileState, t);
    }

    @Override
    public boolean shouldRollOnProcessingTime(
            PartFileInfo<BucketID> partFileState, long currentTime) {
        return defaultPolicy.shouldRollOnProcessingTime(partFileState, currentTime);
    }
}
