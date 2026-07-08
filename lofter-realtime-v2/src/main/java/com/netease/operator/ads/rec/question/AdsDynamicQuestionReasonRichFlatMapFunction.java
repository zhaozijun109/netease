package com.netease.operator.ads.rec.question;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.pojo.KafkaPayloadResult;

import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class AdsDynamicQuestionReasonRichFlatMapFunction
        extends RichFlatMapFunction<String, String> {
    private static final Logger LOG =
            LoggerFactory.getLogger(AdsDynamicQuestionReasonRichFlatMapFunction.class);
    private ObjectMapper objectMapper;

    @Override
    public void open(Configuration parameters) throws Exception {
        objectMapper = new ObjectMapper();
    }

    @Override
    public void flatMap(String s, Collector<String> collector) throws Exception {
        KafkaPayloadResult kafkaPayloadResult = objectMapper.readValue(s, KafkaPayloadResult.class);
        if (kafkaPayloadResult != null && kafkaPayloadResult.getMessageType() == 6) {
            Map<String, Object> map = (Map<String, Object>) kafkaPayloadResult.getPayload();
            if (map.get("priority") != null
                    && ((Integer) map.get("priority") == 2 || (Integer) map.get("priority") == 4)) {
                collector.collect(s);
            }
        }
    }
}
