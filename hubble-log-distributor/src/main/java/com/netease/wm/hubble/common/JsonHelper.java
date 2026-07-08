package com.netease.wm.hubble.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class JsonHelper {
    public static Map<String, Object> parseMap(ObjectMapper mapper, JsonNode object) {
        return object == null ? null : mapper.convertValue(object, Map.class);
    }
}
