package com.netease.yuanqi.unified.operator.ods.mda;

import static com.netease.yuanqi.common.utils.kafka.DistributeDataToTopicUtils.getMdaLogTopicWithAppKey;
import static com.netease.yuanqi.common.utils.kafka.DistributeDataToTopicUtils.getMdaLogTopicWithEventType;
import static com.netease.yuanqi.common.utils.kafka.DistributeDataToTopicUtils.getUselessMdaLogTopic;
import static com.netease.yuanqi.unified.ods.HubbleMdaLogEtlJob.CLIENT_MDA_LOG_OUTPUT_TAG;
import static com.netease.yuanqi.unified.ods.HubbleMdaLogEtlJob.USELESS_MDA_LOG_OUTPUT_TAG;
import static com.netease.yuanqi.unified.ods.HubbleMdaLogEtlJob.WAP_MDA_LOG_OUTPUT_TAG;
import static com.netease.yuanqi.unified.ods.HubbleMdaLogEtlJob.WEB_MDA_LOG_OUTPUT_TAG;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Arrays;
import java.util.List;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HubbleMdaLogProcessFunction extends ProcessFunction<String, Tuple2<String, String>> {
    private static final Logger LOG = LoggerFactory.getLogger(HubbleMdaLogProcessFunction.class);
    private static final Integer LOG_LIMIT_SIZE = 700000;
    private static final List<String> HEADER_OBSOLETE_FIELDS =
            Arrays.asList(
                    "uploadNum",
                    "uploadTime",
                    "persistedTime",
                    "sdkVersion",
                    "sdkType",
                    "deviceMacAddr",
                    "deviceOldMacAddr",
                    "wifiSsid",
                    "wifiBssid",
                    "timeZone",
                    "deviceResolution",
                    "localeLanguage",
                    "localeCountry");
    private ObjectMapper objectMapper;

    @Override
    public void open(Configuration parameters) throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
    }

    @Override
    public void processElement(
            String s,
            ProcessFunction<String, Tuple2<String, String>>.Context context,
            Collector<Tuple2<String, String>> collector)
            throws Exception {
        try {
            String dataBodyJsonString = s.startsWith("{") ? s : s.split(" - dataBody:")[1];

            if (dataBodyJsonString.length() <= LOG_LIMIT_SIZE) {
                JsonNode dataBodyJsonNode = objectMapper.readTree(dataBodyJsonString);
                if (dataBodyJsonNode.has("dataBody")
                        && dataBodyJsonNode.get("dataBody").isArray()) {
                    String ip =
                            dataBodyJsonNode.has("ip") ? dataBodyJsonNode.get("ip").asText() : "";
                    long receiveTime =
                            dataBodyJsonNode.has("receiveTime")
                                    ? dataBodyJsonNode.get("receiveTime").asLong()
                                    : System.currentTimeMillis();

                    ObjectNode headerNode = null;
                    String defaultAppKey = null;
                    for (JsonNode node : dataBodyJsonNode.get("dataBody")) {
                        JsonNode eventNode = objectMapper.readTree(node.asText());
                        if (eventNode.has("dataType")
                                && "h".equals(eventNode.get("dataType").asText())) {
                            headerNode = ((ObjectNode) eventNode).without(HEADER_OBSOLETE_FIELDS);
                            break;
                        }
                        if (defaultAppKey == null) {
                            defaultAppKey =
                                    eventNode.has("appKey")
                                            ? eventNode.get("appKey").asText()
                                            : null;
                        }
                    }

                    for (JsonNode node : dataBodyJsonNode.get("dataBody")) {
                        JsonNode eventNode = objectMapper.readTree(node.asText());
                        ObjectNode resultNode =
                                headerNode != null
                                        ? headerNode.deepCopy().setAll((ObjectNode) eventNode)
                                        : (ObjectNode) eventNode;

                        resultNode.put("ip", ip);
                        resultNode.put("kafkaTime", receiveTime);

                        if (eventNode.has("eventId")
                                && !"da_screen".equals(eventNode.get("eventId").asText())
                                && eventNode.get("eventId").asText().startsWith("da_")) {
                            resultNode.put("dataType", "ie");
                        }

                        long occurTime =
                                eventNode.has("time")
                                        ? eventNode.get("time").asLong()
                                        : eventNode.has("occurTime")
                                                ? eventNode.get("occurTime").asLong() * 1000L
                                                : 0L;
                        long nowTime = System.currentTimeMillis();
                        // 7d || 30min
                        if (nowTime - occurTime > 7 * 24 * 60 * 60 * 1000L
                                || occurTime - nowTime > 30 * 60 * 1000L) {
                            resultNode.put("occurTime", receiveTime);
                        } else {
                            resultNode.put("occurTime", occurTime);
                        }

                        if (eventNode.has("costTime") && eventNode.get("costTime").asLong() < 0) {
                            resultNode.put("costTime", -1);
                        }

                        // App key must not be null when header is not null
                        String appKey =
                                headerNode != null
                                        ? headerNode.get("appKey").asText()
                                        : eventNode.has("appKey")
                                                ? eventNode.get("appKey").asText()
                                                : defaultAppKey;
                        String eventId =
                                resultNode.has("eventId")
                                        ? resultNode.get("eventId").asText()
                                        : null;
                        String category =
                                resultNode.has("category")
                                        ? resultNode.get("category").asText()
                                        : null;

                        if (appKey != null) {
                            // Extra event processing of ad
                            if ("b1-45".equals(eventId)
                                    || "ad-1".equals(eventId)
                                    || "ad-82".equals(eventId)) {
                                collector.collect(
                                        Tuple2.of(
                                                getMdaLogTopicWithEventType("AdEvent"),
                                                objectMapper.writeValueAsString(resultNode)));
                            }

                            if ("rd-2".equals(eventId)
                                    || "lofter_exception".equals(category)
                                    || "lofter_apm".equals(category)
                                    || ("log_monitor".equals(category))) {
                                collector.collect(
                                        specialMdaLogTopicWithEventType(
                                                eventId,
                                                category,
                                                objectMapper.writeValueAsString(resultNode)));
                            } else {
                                String topic = getMdaLogTopicWithAppKey(appKey);
                                if ("lofter.mda.online".equals(topic)
                                        || "ycy.mda.online".equals(topic)
                                        || "ycy.na.mda.online".equals(topic)) {
                                    context.output(
                                            CLIENT_MDA_LOG_OUTPUT_TAG,
                                            objectMapper.writeValueAsString(resultNode));
                                } else if ("lofter.wap.online".equals(topic)) {
                                    context.output(
                                            WAP_MDA_LOG_OUTPUT_TAG,
                                            objectMapper.writeValueAsString(resultNode));
                                } else if ("lofter.web.online".equals(topic)) {
                                    context.output(
                                            WEB_MDA_LOG_OUTPUT_TAG,
                                            objectMapper.writeValueAsString(resultNode));
                                } else {
                                    collector.collect(
                                            Tuple2.of(
                                                    topic,
                                                    objectMapper.writeValueAsString(resultNode)));
                                }
                            }
                        } else {
                            context.output(USELESS_MDA_LOG_OUTPUT_TAG, s);
                        }
                    }
                } else {
                    context.output(USELESS_MDA_LOG_OUTPUT_TAG, s);
                }
            } else {
                context.output(USELESS_MDA_LOG_OUTPUT_TAG, s);
            }
        } catch (JsonProcessingException | ArrayIndexOutOfBoundsException e) {
            // LOG.error("Log parsing error: {}", s);
            context.output(USELESS_MDA_LOG_OUTPUT_TAG, s);
            // throw new RuntimeException(e);
        }
    }

    private Tuple2<String, String> specialMdaLogTopicWithEventType(
            String eventId, String category, String event) {
        if ("rd-2".equals(eventId)) {
            return Tuple2.of(getMdaLogTopicWithEventType("LofterPushEvent"), event);
        }
        if ("lofter_exception".equals(category) || "lofter_apm".equals(category)) {
            return Tuple2.of(getMdaLogTopicWithEventType("LofterExceptionEvent"), event);
        }
        if ("log_monitor".equals(category)) {
            return Tuple2.of(getMdaLogTopicWithEventType("LofterMonitorEvent"), event);
        }
        return Tuple2.of(getUselessMdaLogTopic(), event);
    }
}
