package com.netease.yuanqi.unified.operator.ods.mda;

import static com.netease.yuanqi.common.utils.kafka.DistributeDataToTopicUtils.getMdaLogTopicWithAppKey;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

public class LofterMdaLogTransformJsonRichFlatMapFunction
        extends RichFlatMapFunction<String, String> {
    private ObjectMapper objectMapper;

    @Override
    public void open(Configuration parameters) throws Exception {
        objectMapper = new ObjectMapper();
    }

    @Override
    public void flatMap(String s, Collector<String> collector) throws Exception {
        JsonNode event = objectMapper.readTree(s);
        String dataType = event.has("dataType") ? event.get("dataType").asText() : "";
        String topic =
                getMdaLogTopicWithAppKey(event.has("appKey") ? event.get("appKey").asText() : "");
        if (("s".equals(dataType)
                        || "c".equals(dataType)
                        || "e".equals(dataType)
                        || "ie".equals(dataType))
                && "lofter.mda.online".equals(topic)) {
            collector.collect(s);
        }
    }
}
