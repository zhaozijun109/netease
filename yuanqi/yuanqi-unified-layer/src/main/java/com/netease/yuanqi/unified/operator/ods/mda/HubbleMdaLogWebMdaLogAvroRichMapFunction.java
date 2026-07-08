package com.netease.yuanqi.unified.operator.ods.mda;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.yuanqi.common.pojo.avro.ods.WebMdaLogAvro;
import java.util.HashMap;
import java.util.Map;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HubbleMdaLogWebMdaLogAvroRichMapFunction
        extends RichMapFunction<String, WebMdaLogAvro> {
    private static final Logger LOG =
            LoggerFactory.getLogger(HubbleMdaLogWebMdaLogAvroRichMapFunction.class);
    private ObjectMapper objectMapper;

    @Override
    public void open(Configuration parameters) throws Exception {
        objectMapper = new ObjectMapper();
    }

    @Override
    public WebMdaLogAvro map(String s) throws Exception {
        JsonNode event = objectMapper.readTree(s);
        WebMdaLogAvro webMdaLogAvro = new WebMdaLogAvro();
        webMdaLogAvro.setEventId(event.has("eventId") ? event.get("eventId").asText() : null);
        webMdaLogAvro.setDeviceUdid(
                event.has("deviceUdid") ? event.get("deviceUdid").asText() : null);
        webMdaLogAvro.setUrlPath(event.has("urlPath") ? event.get("urlPath").asText() : null);
        webMdaLogAvro.setUserId(event.has("userId") ? event.get("userId").asLong() : null);
        webMdaLogAvro.setRegion(event.has("region") ? event.get("region").asText() : null);
        webMdaLogAvro.setScreenHeight(
                event.has("screenHeight") ? event.get("screenHeight").asInt() : null);
        webMdaLogAvro.setBrowserVersion(
                event.has("browserVersion") ? event.get("browserVersion").asText() : null);
        webMdaLogAvro.setReferrerDomain(
                event.has("referrerDomain") ? event.get("referrerDomain").asText() : null);
        webMdaLogAvro.setKafkaTime(event.has("kafkaTime") ? event.get("kafkaTime").asLong() : null);
        webMdaLogAvro.setSecondLevelSource(
                event.has("secondLevelSource") ? event.get("secondLevelSource").asText() : null);
        webMdaLogAvro.setData(event.has("data") ? event.get("data").asText() : null);
        webMdaLogAvro.setDeviceModel(
                event.has("deviceModel") ? event.get("deviceModel").asText() : null);
        webMdaLogAvro.setDevicePlatform(
                event.has("devicePlatform") ? event.get("devicePlatform").asText() : null);
        webMdaLogAvro.setIpCity(event.has("ipCity") ? event.get("ipCity").asText() : null);
        webMdaLogAvro.setHubbleId(event.has("hubbleId") ? event.get("hubbleId").asText() : null);
        webMdaLogAvro.setCity(event.has("city") ? event.get("city").asText() : null);
        webMdaLogAvro.setTimestamp(event.has("timestamp") ? event.get("timestamp").asText() : null);
        webMdaLogAvro.setOccurTime(event.has("occurTime") ? event.get("occurTime").asLong() : null);
        webMdaLogAvro.setActivationTime(
                event.has("activationTime") ? event.get("activationTime").asText() : null);
        webMdaLogAvro.setProcessTime(
                event.has("processTime") ? event.get("processTime").asText() : null);
        webMdaLogAvro.setServerTime(
                event.has("serverTime") ? event.get("serverTime").asText() : null);
        webMdaLogAvro.setBrowser(event.has("browser") ? event.get("browser").asText() : null);
        webMdaLogAvro.setDeviceOs(event.has("deviceOs") ? event.get("deviceOs").asText() : null);
        webMdaLogAvro.setIpCountry(event.has("ipCountry") ? event.get("ipCountry").asText() : null);
        webMdaLogAvro.setCurrentUrl(
                event.has("currentUrl") ? event.get("currentUrl").asText() : null);
        webMdaLogAvro.setDeviceOsVersion(
                event.has("deviceOsVersion") ? event.get("deviceOsVersion").asText() : null);
        webMdaLogAvro.setCurrentDomain(
                event.has("currentDomain") ? event.get("currentDomain").asText() : null);
        webMdaLogAvro.setCountry(event.has("country") ? event.get("country").asText() : null);
        webMdaLogAvro.setIp(event.has("ip") ? event.get("ip").asText() : null);
        webMdaLogAvro.setReferrer(event.has("referrer") ? event.get("referrer").asText() : null);
        webMdaLogAvro.setDatatype(event.has("datatype") ? event.get("datatype").asText() : null);
        webMdaLogAvro.setProductKey(
                event.has("productKey") ? event.get("productKey").asText() : null);
        webMdaLogAvro.setSessionUuid(
                event.has("sessionUuid") ? event.get("sessionUuid").asText() : null);
        webMdaLogAvro.setScreenWidth(
                event.has("screenWidth") ? event.get("screenWidth").asInt() : null);
        webMdaLogAvro.setPageTitle(event.has("pageTitle") ? event.get("pageTitle").asText() : null);
        webMdaLogAvro.setAppChannel(
                event.has("appChannel") ? event.get("appChannel").asText() : null);
        webMdaLogAvro.setPageOpenScene(
                event.has("pageOpenScene") ? event.get("pageOpenScene").asText() : null);
        webMdaLogAvro.setFirstLevelSource(
                event.has("firstLevelSource") ? event.get("firstLevelSource").asText() : null);
        webMdaLogAvro.setIpProvince(
                event.has("ipProvince") ? event.get("ipProvince").asText() : null);
        webMdaLogAvro.setAppKey(event.has("appKey") ? event.get("appKey").asText() : null);
        webMdaLogAvro.setCostTime(event.has("costTime") ? event.get("costTime").asLong() : null);
        webMdaLogAvro.setAttributes(processingAttributes(event));
        webMdaLogAvro.setUseragent(event.has("useragent") ? event.get("useragent").asText() : null);
        return webMdaLogAvro;
    }

    private Map<CharSequence, CharSequence> processingAttributes(JsonNode event) {
        Map<CharSequence, CharSequence> attributesMap = new HashMap<>();
        if (event.has("attributes")
                && event.get("attributes") != null
                && !event.get("attributes").isValueNode()) {
            event.get("attributes")
                    .fields()
                    .forEachRemaining(
                            e -> {
                                if (e.getKey() != null && e.getValue() != null) {
                                    try {
                                        // Attention! Convert key to lowercase.
                                        attributesMap.put(
                                                e.getKey().toLowerCase(),
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
        return attributesMap;
    }
}
