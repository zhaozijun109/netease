package com.netease.yuanqi.unified.operator.ods.mda;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.yuanqi.common.pojo.avro.ods.WapMdaLogAvro;
import java.util.HashMap;
import java.util.Map;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HubbleMdaLogWapMdaLogAvroRichMapFunction
        extends RichMapFunction<String, WapMdaLogAvro> {
    private static final Logger LOG =
            LoggerFactory.getLogger(HubbleMdaLogWapMdaLogAvroRichMapFunction.class);
    private ObjectMapper objectMapper;

    @Override
    public void open(Configuration parameters) throws Exception {
        objectMapper = new ObjectMapper();
    }

    @Override
    public WapMdaLogAvro map(String s) throws Exception {
        JsonNode event = objectMapper.readTree(s);
        WapMdaLogAvro wapMdaLogAvro = new WapMdaLogAvro();
        wapMdaLogAvro.setEventId(event.has("eventId") ? event.get("eventId").asText() : null);
        wapMdaLogAvro.setDeviceUdid(
                event.has("deviceUdid") ? event.get("deviceUdid").asText() : null);
        wapMdaLogAvro.setUrlPath(event.has("urlPath") ? event.get("urlPath").asText() : null);
        wapMdaLogAvro.setUserId(event.has("userId") ? event.get("userId").asLong() : null);
        wapMdaLogAvro.setRegion(event.has("region") ? event.get("region").asText() : null);
        wapMdaLogAvro.setScreenHeight(
                event.has("screenHeight") ? event.get("screenHeight").asInt() : null);
        wapMdaLogAvro.setBrowserVersion(
                event.has("browserVersion") ? event.get("browserVersion").asText() : null);
        wapMdaLogAvro.setReferrerDomain(
                event.has("referrerDomain") ? event.get("referrerDomain").asText() : null);
        wapMdaLogAvro.setKafkaTime(event.has("kafkaTime") ? event.get("kafkaTime").asLong() : null);
        wapMdaLogAvro.setSecondLevelSource(
                event.has("secondLevelSource") ? event.get("secondLevelSource").asText() : null);
        wapMdaLogAvro.setData(event.has("data") ? event.get("data").asText() : null);
        wapMdaLogAvro.setDeviceModel(
                event.has("deviceModel") ? event.get("deviceModel").asText() : null);
        wapMdaLogAvro.setDevicePlatform(
                event.has("devicePlatform") ? event.get("devicePlatform").asText() : null);
        wapMdaLogAvro.setIpCity(event.has("ipCity") ? event.get("ipCity").asText() : null);
        wapMdaLogAvro.setHubbleId(event.has("hubbleId") ? event.get("hubbleId").asText() : null);
        wapMdaLogAvro.setCity(event.has("city") ? event.get("city").asText() : null);
        wapMdaLogAvro.setTimestamp(event.has("timestamp") ? event.get("timestamp").asText() : null);
        wapMdaLogAvro.setOccurTime(event.has("occurTime") ? event.get("occurTime").asLong() : null);
        wapMdaLogAvro.setActivationTime(
                event.has("activationTime") ? event.get("activationTime").asText() : null);
        wapMdaLogAvro.setProcessTime(
                event.has("processTime") ? event.get("processTime").asText() : null);
        wapMdaLogAvro.setServerTime(
                event.has("serverTime") ? event.get("serverTime").asText() : null);
        wapMdaLogAvro.setBrowser(event.has("browser") ? event.get("browser").asText() : null);
        wapMdaLogAvro.setDeviceOs(event.has("deviceOs") ? event.get("deviceOs").asText() : null);
        wapMdaLogAvro.setIpCountry(event.has("ipCountry") ? event.get("ipCountry").asText() : null);
        wapMdaLogAvro.setCurrentUrl(
                event.has("currentUrl") ? event.get("currentUrl").asText() : null);
        wapMdaLogAvro.setDeviceOsVersion(
                event.has("deviceOsVersion") ? event.get("deviceOsVersion").asText() : null);
        wapMdaLogAvro.setCurrentDomain(
                event.has("currentDomain") ? event.get("currentDomain").asText() : null);
        wapMdaLogAvro.setCountry(event.has("country") ? event.get("country").asText() : null);
        wapMdaLogAvro.setIp(event.has("ip") ? event.get("ip").asText() : null);
        wapMdaLogAvro.setReferrer(event.has("referrer") ? event.get("referrer").asText() : null);
        wapMdaLogAvro.setDatatype(event.has("datatype") ? event.get("datatype").asText() : null);
        wapMdaLogAvro.setProductKey(
                event.has("productKey") ? event.get("productKey").asText() : null);
        wapMdaLogAvro.setSessionUuid(
                event.has("sessionUuid") ? event.get("sessionUuid").asText() : null);
        wapMdaLogAvro.setScreenWidth(
                event.has("screenWidth") ? event.get("screenWidth").asInt() : null);
        wapMdaLogAvro.setPageTitle(event.has("pageTitle") ? event.get("pageTitle").asText() : null);
        wapMdaLogAvro.setAppChannel(
                event.has("appChannel") ? event.get("appChannel").asText() : null);
        wapMdaLogAvro.setPageOpenScene(
                event.has("pageOpenScene") ? event.get("pageOpenScene").asText() : null);
        wapMdaLogAvro.setFirstLevelSource(
                event.has("firstLevelSource") ? event.get("firstLevelSource").asText() : null);
        wapMdaLogAvro.setIpProvince(
                event.has("ipProvince") ? event.get("ipProvince").asText() : null);
        wapMdaLogAvro.setAppKey(event.has("appKey") ? event.get("appKey").asText() : null);
        wapMdaLogAvro.setCostTime(event.has("costTime") ? event.get("costTime").asLong() : null);
        wapMdaLogAvro.setAttributes(processingAttributes(event));
        wapMdaLogAvro.setUseragent(event.has("useragent") ? event.get("useragent").asText() : null);
        return wapMdaLogAvro;
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
