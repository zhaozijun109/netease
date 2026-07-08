package com.netease.yuanqi.common.utils.filesystem.bucket;

import com.netease.yuanqi.common.pojo.archive.ycy.kafka.KafkaArchiveRecord;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.apache.flink.core.io.SimpleVersionedSerializer;
import org.apache.flink.streaming.api.functions.sink.filesystem.BucketAssigner;
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.SimpleVersionedStringSerializer;
import org.apache.flink.util.Preconditions;

public class YcyKafkaLogBucketAssigner<T extends KafkaArchiveRecord>
        implements BucketAssigner<T, String> {
    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_FORMAT_STRING = "yyyy-MM-dd";
    private final String formatString;
    private final ZoneId zoneId;

    public YcyKafkaLogBucketAssigner() {
        this(DEFAULT_FORMAT_STRING);
    }

    public YcyKafkaLogBucketAssigner(String formatString) {
        this(formatString, ZoneId.systemDefault());
    }

    public YcyKafkaLogBucketAssigner(String formatString, ZoneId zoneId) {
        this.formatString = Preconditions.checkNotNull(formatString);
        this.zoneId = Preconditions.checkNotNull(zoneId);
    }

    private transient DateTimeFormatter dateTimeFormatter;

    @Override
    public String getBucketId(T element, Context context) {
        if (this.dateTimeFormatter == null) {
            this.dateTimeFormatter = DateTimeFormatter.ofPattern(formatString).withZone(zoneId);
        }

        return this.dateTimeFormatter.format(Instant.ofEpochMilli(element.getKafkaTime()));
    }

    @Override
    public SimpleVersionedSerializer<String> getSerializer() {
        return SimpleVersionedStringSerializer.INSTANCE;
    }
}
