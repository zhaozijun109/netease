package com.netease.yuanqi.unified.operator.archive;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.yuanqi.common.pojo.archive.ArchiveFormatRow;
import com.netease.yuanqi.common.pojo.archive.ycy.kafka.AbPlatformSdkLogDto;
import com.netease.yuanqi.common.pojo.archive.ycy.kafka.AuthorUpdateDetailsDto;
import com.netease.yuanqi.common.pojo.archive.ycy.kafka.CommentLogDto;
import com.netease.yuanqi.common.pojo.archive.ycy.kafka.CommentRecognitionDto;
import com.netease.yuanqi.common.pojo.archive.ycy.kafka.GameClickRecordDto;
import com.netease.yuanqi.common.pojo.archive.ycy.kafka.KafkaArchiveRecord;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArchiveYcyKafkaLogProcessFunction
        extends ProcessFunction<Tuple3<String, Long, String>, Object> {
    private static final Logger LOG =
            LoggerFactory.getLogger(ArchiveYcyKafkaLogProcessFunction.class);

    public static final OutputTag<ArchiveFormatRow> FALLBACK_TAG =
            new OutputTag<ArchiveFormatRow>("Fallback") {};

    private static final Map<String, TopicSinkConfig<?>> TOPIC_REGISTRY;

    static {
        Map<String, TopicSinkConfig<?>> map = new LinkedHashMap<>();
        map.put(
                "a13_comment_recognize_receive",
                new TopicSinkConfig<>(
                        new OutputTag<CommentRecognitionDto>("CommentRecognition") {},
                        CommentRecognitionDto.class));
        map.put(
                "a13_game_click_record",
                new TopicSinkConfig<>(
                        new OutputTag<GameClickRecordDto>("GameClickRecord") {},
                        GameClickRecordDto.class));
        map.put(
                "grand_a13_abtest_log",
                new TopicSinkConfig<>(
                        new OutputTag<AbPlatformSdkLogDto>("AbPlatformSdkLog") {},
                        AbPlatformSdkLogDto.class));
        map.put(
                "a13_realtime_stats",
                new TopicSinkConfig<>(
                        new OutputTag<AuthorUpdateDetailsDto>("AuthorUpdateDetails") {},
                        AuthorUpdateDetailsDto.class));
        map.put(
                "a13_comment_recognize_send",
                new TopicSinkConfig<>(
                        new OutputTag<CommentLogDto>("CommentLog") {}, CommentLogDto.class));
        TOPIC_REGISTRY = Collections.unmodifiableMap(map);
    }

    public static Map<String, TopicSinkConfig<?>> getRegistry() {
        return TOPIC_REGISTRY;
    }

    private ObjectMapper objectMapper;

    @Override
    public void open(Configuration parameters) throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void processElement(
            Tuple3<String, Long, String> value,
            ProcessFunction<Tuple3<String, Long, String>, Object>.Context ctx,
            Collector<Object> out)
            throws Exception {
        String topic = value.f0;
        Long kafkaTime = value.f1;
        String jsonData = value.f2;

        TopicSinkConfig<?> config = TOPIC_REGISTRY.get(topic);
        if (config != null) {
            try {
                emitRecord(ctx, config, jsonData, kafkaTime);
            } catch (JsonProcessingException e) {
                LOG.warn("Failed to parse JSON from topic {}: {}", topic, e.getMessage());
            }
        } else {
            LOG.warn("Unknown topic: {}, falling back to raw JSON archive", topic);
            ctx.output(
                    FALLBACK_TAG,
                    ArchiveFormatRow.builder()
                            .setArchiveDir(topic)
                            .setArchiveTime(kafkaTime)
                            .setData(jsonData)
                            .build());
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends KafkaArchiveRecord> void emitRecord(
            ProcessFunction<Tuple3<String, Long, String>, Object>.Context ctx,
            TopicSinkConfig<?> config,
            String jsonData,
            Long kafkaTime)
            throws JsonProcessingException {
        TopicSinkConfig<T> typedConfig = (TopicSinkConfig<T>) config;
        T dto = objectMapper.readValue(jsonData, typedConfig.getClazz());
        dto.setKafkaTime(kafkaTime);
        ctx.output(typedConfig.getTag(), dto);
    }

    public static class TopicSinkConfig<T extends KafkaArchiveRecord> {
        private final OutputTag<T> tag;
        private final Class<T> clazz;

        public TopicSinkConfig(OutputTag<T> tag, Class<T> clazz) {
            this.tag = tag;
            this.clazz = clazz;
        }

        public OutputTag<T> getTag() {
            return tag;
        }

        public Class<T> getClazz() {
            return clazz;
        }
    }
}
