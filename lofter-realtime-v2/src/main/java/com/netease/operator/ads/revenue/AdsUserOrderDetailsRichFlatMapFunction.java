package com.netease.operator.ads.revenue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.pojo.revenue.UserOrderDetails;

import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

public class AdsUserOrderDetailsRichFlatMapFunction
        extends RichFlatMapFunction<String, UserOrderDetails> {
    private ObjectMapper objectMapper;

    @Override
    public void open(Configuration parameters) throws Exception {
        objectMapper = new ObjectMapper();
    }

    @Override
    public void flatMap(String s, Collector<UserOrderDetails> collector) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(s);
        if (jsonNode.get("tableName").asText() != null
                && ("Trade_GiftPresentRecord".equals(jsonNode.get("tableName").asText())
                        || "Trade_StoreVipOrder".equals(jsonNode.get("tableName").asText())
                        || "Trade_FansVipOrder".equals(jsonNode.get("tableName").asText())
                        || "Trade_PVEStaminaOrder".equals(jsonNode.get("tableName").asText()))) {
            collector.collect(objectMapper.readValue(s, UserOrderDetails.class));
        }
    }
}
