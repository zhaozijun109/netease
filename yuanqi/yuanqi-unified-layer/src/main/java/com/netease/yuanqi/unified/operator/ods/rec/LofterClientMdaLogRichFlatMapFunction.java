package com.netease.yuanqi.unified.operator.ods.rec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.yuanqi.unified.pojo.RecParsedLogEvents;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.util.Collector;

public class LofterClientMdaLogRichFlatMapFunction
        extends RichFlatMapFunction<String, RecParsedLogEvents> {
    private ObjectMapper objectMapper;

    @Override
    public void open(OpenContext openContext) throws Exception {
        objectMapper = new ObjectMapper();
    }

    @Override
    public void flatMap(String s, Collector<RecParsedLogEvents> collector) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(s);
        String deviceUdid = jsonNode.has("deviceUdid") ? jsonNode.get("deviceUdid").asText() : null;
        if (deviceUdid != null && !deviceUdid.isEmpty()) {
            collector.collect(
                    RecParsedLogEvents.builder()
                            .setEventId(
                                    jsonNode.has("eventId") ? jsonNode.get("eventId").asText() : "")
                            .setDeviceUdid(deviceUdid)
                            .setAppVersion(
                                    jsonNode.has("appVersion")
                                            ? jsonNode.get("appVersion").asText()
                                            : null)
                            .setSource(s)
                            .build());
        }
    }
}
