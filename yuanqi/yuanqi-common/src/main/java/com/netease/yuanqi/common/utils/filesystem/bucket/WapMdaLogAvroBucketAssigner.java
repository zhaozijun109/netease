package com.netease.yuanqi.common.utils.filesystem.bucket;

import com.netease.yuanqi.common.pojo.avro.ods.WapMdaLogAvro;
import com.netease.yuanqi.common.utils.kafka.DistributeDataToTopicUtils;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.apache.flink.core.io.SimpleVersionedSerializer;
import org.apache.flink.streaming.api.functions.sink.filesystem.BucketAssigner;
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.SimpleVersionedStringSerializer;
import org.apache.flink.util.Preconditions;

public class WapMdaLogAvroBucketAssigner implements BucketAssigner<WapMdaLogAvro, String> {
    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_FORMAT_STRING = "yyyy-MM-dd";
    private final String formatString;
    private final ZoneId zoneId;

    public WapMdaLogAvroBucketAssigner() {
        this(DEFAULT_FORMAT_STRING);
    }

    public WapMdaLogAvroBucketAssigner(String formatString) {
        this(formatString, ZoneId.systemDefault());
    }

    public WapMdaLogAvroBucketAssigner(ZoneId zoneId) {
        this(DEFAULT_FORMAT_STRING, zoneId);
    }

    public WapMdaLogAvroBucketAssigner(String formatString, ZoneId zoneId) {
        this.formatString = Preconditions.checkNotNull(formatString);
        this.zoneId = Preconditions.checkNotNull(zoneId);
    }

    private transient DateTimeFormatter dateTimeFormatter;

    @Override
    public String getBucketId(WapMdaLogAvro wapMdaLogAvro, Context context) {
        if (this.dateTimeFormatter == null) {
            this.dateTimeFormatter = DateTimeFormatter.ofPattern(formatString).withZone(zoneId);
        }

        return DistributeDataToTopicUtils.getMdaLogTopicWithAppKey(
                        String.valueOf(wapMdaLogAvro.getAppKey()))
                + "/"
                + this.dateTimeFormatter.format(Instant.ofEpochMilli(wapMdaLogAvro.getKafkaTime()));
    }

    @Override
    public SimpleVersionedSerializer<String> getSerializer() {
        return SimpleVersionedStringSerializer.INSTANCE;
    }
}
