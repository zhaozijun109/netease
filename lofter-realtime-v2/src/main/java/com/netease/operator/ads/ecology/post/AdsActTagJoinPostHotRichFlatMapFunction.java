package com.netease.operator.ads.ecology.post;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.pojo.ecology.post.PostHot;

import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdsActTagJoinPostHotRichFlatMapFunction extends RichFlatMapFunction<String, PostHot> {
    private static final Logger LOG =
            LoggerFactory.getLogger(AdsActTagJoinPostHotRichFlatMapFunction.class);
    private ObjectMapper objectMapper;

    @Override
    public void open(Configuration parameters) throws Exception {
        objectMapper = new ObjectMapper();
    }

    @Override
    public void flatMap(String s, Collector<PostHot> collector) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(s);
        if (jsonNode.get("tableName") != null
                && "PostHot".equals(jsonNode.get("tableName").asText())) {
            PostHot postHot = objectMapper.readValue(s, PostHot.class);
            collector.collect(postHot);
        }
    }
}
