package com.netease.yuanqi.unified.operator.ods.mda;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.yuanqi.common.pojo.avro.ods.ClientMdaLogAvro;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

public class HubbleMdaLogClientMdaLogAvroRichFlatMapFunction
        extends RichFlatMapFunction<String, ClientMdaLogAvro> {
    private ObjectMapper objectMapper;
    private HashSet<String> baseKeySet;

    @Override
    public void open(Configuration parameters) throws Exception {
        objectMapper = new ObjectMapper();
        baseKeySet =
                new HashSet<>(
                        Arrays.asList(
                                "eventId",
                                "deviceUdid",
                                "userId",
                                "userType",
                                "userName",
                                "appVersion",
                                "appChannel",
                                "occurTime",
                                "category",
                                "label",
                                "customUDID",
                                "sessionUuid",
                                "ip",
                                "devicePlatform",
                                "deviceOs",
                                "deviceOsVersion",
                                "deviceModel",
                                "deviceAdid",
                                "deviceIdfv",
                                "deviceIMEI",
                                "deviceCarrier",
                                "appKey",
                                "city",
                                "costTime",
                                "source",
                                "deviceAndroidId",
                                "itemId",
                                "itemType",
                                "recId",
                                "scene",
                                "action",
                                "tagName",
                                "layout",
                                "algInfo",
                                "kafkaTime",
                                "oaid",
                                "isBeta",
                                "tdid"));
    }

    @Override
    public void flatMap(String s, Collector<ClientMdaLogAvro> collector) throws Exception {
        JsonNode event = objectMapper.readTree(s);
        String dataType = event.has("dataType") ? event.get("dataType").asText() : "";
        if ("s".equals(dataType)
                || "c".equals(dataType)
                || "e".equals(dataType)
                || "ie".equals(dataType)) {
            ClientMdaLogAvro clientMdaLogAvro = new ClientMdaLogAvro();
            clientMdaLogAvro.setEventId(
                    "s".equals(dataType)
                            ? "da_session_start"
                            : "c".equals(dataType)
                                    ? "da_session_close"
                                    : event.has("eventId") ? event.get("eventId").asText() : null);
            clientMdaLogAvro.setDeviceUdid(
                    event.has("deviceUdid") ? event.get("deviceUdid").asText() : null);
            clientMdaLogAvro.setUserId(
                    event.has("userId")
                            ? !"".equals(event.get("userId").asText().split(",")[0])
                                            && event.get("userId")
                                                    .asText()
                                                    .split(",")[0]
                                                    .chars()
                                                    .allMatch(Character::isDigit)
                                    ? Long.parseLong(event.get("userId").asText())
                                    : null
                            : null);
            clientMdaLogAvro.setUserType(
                    event.has("userId")
                            ? event.get("userId").asText().split(",").length == 2
                                    ? !"".equals(event.get("userId").asText().split(",")[1])
                                            ? Integer.parseInt(
                                                    event.get("userId").asText().split(",")[1])
                                            : null
                                    : null
                            : null);
            clientMdaLogAvro.setUserName(
                    event.has("userName") ? event.get("userName").asText() : null);
            clientMdaLogAvro.setAppVersion(
                    event.has("appVersion") ? event.get("appVersion").asText() : null);
            clientMdaLogAvro.setAppChannel(
                    event.has("appChannel") ? event.get("appChannel").asText() : null);
            clientMdaLogAvro.setOccurTime(
                    "s".equals(dataType)
                            ? event.has("sessionStartTime")
                                    ? event.get("sessionStartTime").asLong() * 1000L
                                    : null
                            : "c".equals(dataType)
                                    ? event.has("sessionCloseTime")
                                            ? event.get("sessionCloseTime").asLong() * 1000L
                                            : null
                                    : event.has("occurTime")
                                            ? event.get("occurTime").asLong()
                                            : null);
            clientMdaLogAvro.setCategory(
                    event.has("category") ? event.get("category").asText() : null);
            clientMdaLogAvro.setCustomUDID(
                    event.has("customUDID") ? event.get("customUDID").asText() : null);
            clientMdaLogAvro.setSessionUuid(
                    event.has("sessionUuid") ? event.get("sessionUuid").asText() : null);
            clientMdaLogAvro.setIp(event.has("ip") ? event.get("ip").asText() : null);
            clientMdaLogAvro.setDevicePlatform(
                    event.has("devicePlatform") ? event.get("devicePlatform").asText() : null);
            clientMdaLogAvro.setDeviceOs(
                    event.has("deviceOs") ? event.get("deviceOs").asText() : null);
            clientMdaLogAvro.setDeviceOsVersion(
                    event.has("deviceOsVersion") ? event.get("deviceOsVersion").asText() : null);
            clientMdaLogAvro.setDeviceModel(
                    event.has("deviceModel") ? event.get("deviceModel").asText() : null);
            clientMdaLogAvro.setDeviceAdid(
                    event.has("deviceAdid") ? event.get("deviceAdid").asText() : null);
            clientMdaLogAvro.setDeviceIdfv(
                    event.has("deviceIdfv") ? event.get("deviceIdfv").asText() : null);
            clientMdaLogAvro.setDeviceIMEI(
                    event.has("deviceIMEI") ? event.get("deviceIMEI").asText() : null);
            clientMdaLogAvro.setDeviceCarrier(
                    event.has("deviceCarrier") ? event.get("deviceCarrier").asText() : null);
            clientMdaLogAvro.setAppKey(event.has("appKey") ? event.get("appKey").asText() : null);
            clientMdaLogAvro.setCity(event.has("city") ? event.get("city").asText() : null);
            clientMdaLogAvro.setCostTime(
                    event.has("costTime") ? event.get("costTime").asLong() : null);
            clientMdaLogAvro.setKafkaTime(
                    event.has("kafkaTime") ? event.get("kafkaTime").asLong() : null);
            clientMdaLogAvro.setOaid(
                    event.has("oaid")
                            ? "00000000-0000-0000-0000-000000000000"
                                                    .equals(event.get("oaid").asText())
                                            || "".equals(event.get("oaid").asText())
                                            || "null".equals(event.get("oaid").asText())
                                    ? null
                                    : event.get("oaid").asText()
                            : null);
            clientMdaLogAvro.setIsBeta(event.has("isBeta") ? event.get("isBeta").asText() : null);
            clientMdaLogAvro.setLabel(
                    event.has("label")
                            ? event.get("label").asText().contains(":")
                                    ? processingLabel(event.get("label").asText())
                                    : event.get("label").asText()
                            : null);
            clientMdaLogAvro.setTdid(event.has("tdid") ? event.get("tdid").asText() : null);

            Map<CharSequence, CharSequence> params = processingParams(event);
            clientMdaLogAvro.setSource(params.getOrDefault("source", null));
            clientMdaLogAvro.setDeviceAndroidId(params.getOrDefault("deviceAndroidId", null));
            clientMdaLogAvro.setItemId(processingItemId(params));
            clientMdaLogAvro.setItemType(processingItemType(params));
            clientMdaLogAvro.setRecId(params.getOrDefault("recId", null));
            clientMdaLogAvro.setScene(processingScene(params));
            clientMdaLogAvro.setAction(params.getOrDefault("action", null));
            clientMdaLogAvro.setTagName(params.getOrDefault("tagName", null));
            clientMdaLogAvro.setLayout(params.getOrDefault("layout", null));
            clientMdaLogAvro.setAlgInfo(params.getOrDefault("algInfo", null));

            // Attention! The job had enabled object reuse.
            clientMdaLogAvro.setParams(filterUselessParams(params));
            collector.collect(clientMdaLogAvro);
        }
    }

    private String processingItemId(Map<CharSequence, CharSequence> params) {
        if (params.get("itemId") != null) {
            return params.get("itemId").toString();
        }
        if (params.get("文章ID") != null) {
            return params.get("文章ID").toString();
        }
        return null;
    }

    private String processingItemType(Map<CharSequence, CharSequence> params) {
        if (params.get("itemType") != null) {
            return params.get("itemType").toString();
        }
        if (params.get("卡片类型") != null) {
            String itemType = params.get("卡片类型").toString();
            switch (itemType) {
                case "文本":
                    return "TEXT";
                case "图文":
                    return "PHOTO";
                case "视频":
                    return "VIDEO";
                case "其他":
                    return "OTHER";
                default:
                    return itemType;
            }
        }
        return null;
    }

    private String processingScene(Map<CharSequence, CharSequence> params) {
        if (params.get("scene") != null) {
            return params.get("scene").toString();
        }
        if (params.get("页面类型") != null) {
            String scene = params.get("页面类型").toString();
            switch (scene) {
                case "单日志页":
                    return "note";
                case "关注页":
                    return "attention";
                case "视频流页":
                    return "videolist";
                case "个人主页":
                    return "homepage";
                case "视频详情页":
                    return "videodetail";
                case "我的喜欢页":
                    return "mylove";
                case "收藏页":
                    return "collection";
                case "其他":
                    return "other";
                default:
                    return scene;
            }
        }
        return null;
    }

    private String processingLabel(String label) {
        String[] labels = label.split(",");
        for (String s1 : labels) {
            if (s1.contains(":") && s1.split(":").length == 2) {
                if (s1.split(":")[0].trim().equals("name")) {
                    return s1.split(":")[1].trim();
                }
            }
        }
        return null;
    }

    private Map<CharSequence, CharSequence> processingParams(JsonNode event) {
        Map<CharSequence, CharSequence> params = new HashMap<>();
        if (event.has("label") && event.get("label").asText().contains(":")) {
            String[] labels = event.get("label").asText().split(",");
            for (String s1 : labels) {
                if (s1.contains(":") && s1.split(":").length == 2) {
                    params.put(s1.split(":")[0].trim(), s1.split(":")[1].trim());
                }
            }
        }

        if (event.has("attributes")) {
            event.get("attributes")
                    .fields()
                    .forEachRemaining(
                            e -> {
                                if (e.getKey() != null && e.getValue() != null) {
                                    try {
                                        params.put(
                                                e.getKey(),
                                                e.getValue().isValueNode()
                                                        ? e.getValue().asText()
                                                        : objectMapper.writeValueAsString(
                                                                e.getValue()));
                                    } catch (JsonProcessingException ex) {
                                        throw new RuntimeException(ex);
                                    }
                                }
                            });
        }

        event.fields()
                .forEachRemaining(
                        e -> {
                            if (e.getKey() != null
                                    && e.getValue() != null
                                    && !"attributes".equals(e.getKey())) {
                                try {
                                    params.put(
                                            e.getKey(),
                                            e.getValue().isValueNode()
                                                    ? e.getValue().asText()
                                                    : objectMapper.writeValueAsString(
                                                            e.getValue()));
                                } catch (JsonProcessingException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                        });
        return params;
    }

    private Map<CharSequence, CharSequence> filterUselessParams(
            Map<CharSequence, CharSequence> params) {
        Map<CharSequence, CharSequence> filterParams = new HashMap<>();
        for (Map.Entry<CharSequence, CharSequence> entry : params.entrySet()) {
            if (!baseKeySet.contains(entry.getKey().toString())) {
                filterParams.put(entry.getKey(), entry.getValue());
            }
        }
        return filterParams;
    }
}
