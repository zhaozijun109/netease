package com.netease.yuanqi.common.utils.filesystem.bucket;

import com.netease.yuanqi.common.pojo.avro.ods.ClientMdaLogAvro;
import com.netease.yuanqi.common.utils.kafka.DistributeDataToTopicUtils;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.apache.flink.core.io.SimpleVersionedSerializer;
import org.apache.flink.streaming.api.functions.sink.filesystem.BucketAssigner;
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.SimpleVersionedStringSerializer;
import org.apache.flink.util.Preconditions;

public class ClientMdaLogAvroBucketAssigner implements BucketAssigner<ClientMdaLogAvro, String> {
    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_FORMAT_STRING = "yyyy-MM-dd";
    private final String formatString;
    private final ZoneId zoneId;

    public ClientMdaLogAvroBucketAssigner() {
        this(DEFAULT_FORMAT_STRING);
    }

    public ClientMdaLogAvroBucketAssigner(String formatString) {
        this(formatString, ZoneId.systemDefault());
    }

    public ClientMdaLogAvroBucketAssigner(ZoneId zoneId) {
        this(DEFAULT_FORMAT_STRING, zoneId);
    }

    public ClientMdaLogAvroBucketAssigner(String formatString, ZoneId zoneId) {
        this.formatString = Preconditions.checkNotNull(formatString);
        this.zoneId = Preconditions.checkNotNull(zoneId);
    }

    private transient DateTimeFormatter dateTimeFormatter;

    @Override
    public String getBucketId(ClientMdaLogAvro clientMdaLogAvro, Context context) {
        if (this.dateTimeFormatter == null) {
            this.dateTimeFormatter = DateTimeFormatter.ofPattern(formatString).withZone(zoneId);
        }

        return DistributeDataToTopicUtils.getMdaLogTopicWithAppKey(
                        String.valueOf(clientMdaLogAvro.getAppKey()))
                + "/"
                + this.dateTimeFormatter.format(
                        Instant.ofEpochMilli(clientMdaLogAvro.getKafkaTime()));
    }

    @Override
    public SimpleVersionedSerializer<String> getSerializer() {
        return SimpleVersionedStringSerializer.INSTANCE;
    }
}
