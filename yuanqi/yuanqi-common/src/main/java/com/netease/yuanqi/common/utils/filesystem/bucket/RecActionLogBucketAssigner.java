package com.netease.yuanqi.common.utils.filesystem.bucket;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.apache.flink.core.io.SimpleVersionedSerializer;
import org.apache.flink.streaming.api.functions.sink.filesystem.BucketAssigner;
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.SimpleVersionedStringSerializer;
import org.apache.flink.util.Preconditions;
import rs.basic.upload.parse.dto.ActionHiveDto;

public class RecActionLogBucketAssigner implements BucketAssigner<ActionHiveDto, String> {
    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_FORMAT_STRING = "yyyy-MM-dd";
    private final String formatString;
    private final ZoneId zoneId;

    public RecActionLogBucketAssigner() {
        this(DEFAULT_FORMAT_STRING);
    }

    public RecActionLogBucketAssigner(String formatString) {
        this(formatString, ZoneId.systemDefault());
    }

    public RecActionLogBucketAssigner(ZoneId zoneId) {
        this(DEFAULT_FORMAT_STRING, zoneId);
    }

    public RecActionLogBucketAssigner(String formatString, ZoneId zoneId) {
        this.formatString = Preconditions.checkNotNull(formatString);
        this.zoneId = Preconditions.checkNotNull(zoneId);
    }

    private transient DateTimeFormatter dateTimeFormatter;

    @Override
    public String getBucketId(ActionHiveDto actionHiveDto, Context context) {
        if (this.dateTimeFormatter == null) {
            this.dateTimeFormatter = DateTimeFormatter.ofPattern(formatString).withZone(zoneId);
        }

        return this.dateTimeFormatter.format(
                Instant.ofEpochMilli(Long.parseLong(actionHiveDto.getT())));
    }

    @Override
    public SimpleVersionedSerializer<String> getSerializer() {
        return SimpleVersionedStringSerializer.INSTANCE;
    }
}
