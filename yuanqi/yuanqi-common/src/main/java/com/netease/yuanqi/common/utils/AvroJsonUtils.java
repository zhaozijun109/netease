package com.netease.yuanqi.common.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.avro.AvroRuntimeException;
import org.apache.avro.JsonProperties;
import org.apache.avro.Schema;

public class AvroJsonUtils {
    private AvroJsonUtils() {}

    public static JsonNode toJsonNode(Object datum) {
        if (datum == null) {
            return null;
        } else {
            try {
                TokenBuffer generator = new TokenBuffer(new ObjectMapper(), false);
                toJson(datum, generator);
                return (JsonNode) (new ObjectMapper()).readTree(generator.asParser());
            } catch (IOException e) {
                throw new AvroRuntimeException(e);
            }
        }
    }

    static void toJson(Object datum, JsonGenerator generator) throws IOException {
        if (datum == JsonProperties.NULL_VALUE) {
            generator.writeNull();
        } else if (datum instanceof Map) {
            generator.writeStartObject();

            for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) datum).entrySet()) {
                generator.writeFieldName(entry.getKey().toString());
                toJson(entry.getValue(), generator);
            }

            generator.writeEndObject();
        } else if (datum instanceof Collection) {
            generator.writeStartArray();

            for (Object element : (Collection) datum) {
                toJson(element, generator);
            }

            generator.writeEndArray();
        } else if (datum instanceof byte[]) {
            generator.writeString(new String((byte[]) datum, StandardCharsets.ISO_8859_1));
        } else if (!(datum instanceof CharSequence) && !(datum instanceof Enum)) {
            if (datum instanceof Double) {
                generator.writeNumber((Double) datum);
            } else if (datum instanceof Float) {
                generator.writeNumber((Float) datum);
            } else if (datum instanceof Long) {
                generator.writeNumber((Long) datum);
            } else if (datum instanceof Integer) {
                generator.writeNumber((Integer) datum);
            } else if (datum instanceof Boolean) {
                generator.writeBoolean((Boolean) datum);
            } else if (datum instanceof BigInteger) {
                generator.writeNumber((BigInteger) datum);
            } else {
                if (!(datum instanceof BigDecimal)) {
                    throw new AvroRuntimeException("Unknown datum class: " + datum.getClass());
                }

                generator.writeNumber((BigDecimal) datum);
            }
        } else {
            generator.writeString(datum.toString());
        }
    }

    public static Object toObject(JsonNode jsonNode) {
        return toObject(jsonNode, (Schema) null);
    }

    public static Object toObject(JsonNode jsonNode, Schema schema) {
        if (schema != null && schema.getType().equals(Schema.Type.UNION)) {
            return toObject(jsonNode, (Schema) schema.getTypes().get(0));
        } else if (jsonNode == null) {
            return null;
        } else if (jsonNode.isNull()) {
            return JsonProperties.NULL_VALUE;
        } else if (jsonNode.isBoolean()) {
            return jsonNode.asBoolean();
        } else {
            if (jsonNode.isInt()) {
                if (schema == null || schema.getType().equals(Schema.Type.INT)) {
                    return jsonNode.asInt();
                }

                if (schema.getType().equals(Schema.Type.LONG)) {
                    return jsonNode.asLong();
                }

                if (schema.getType().equals(Schema.Type.FLOAT)) {
                    return (float) jsonNode.asDouble();
                }

                if (schema.getType().equals(Schema.Type.DOUBLE)) {
                    return jsonNode.asDouble();
                }
            } else if (jsonNode.isLong()) {
                if (schema == null || schema.getType().equals(Schema.Type.LONG)) {
                    return jsonNode.asLong();
                }

                if (schema.getType().equals(Schema.Type.INT)) {
                    if (jsonNode.canConvertToInt()) {
                        return jsonNode.asInt();
                    }

                    return jsonNode.asLong();
                }

                if (schema.getType().equals(Schema.Type.FLOAT)) {
                    return (float) jsonNode.asDouble();
                }

                if (schema.getType().equals(Schema.Type.DOUBLE)) {
                    return jsonNode.asDouble();
                }
            } else if (!jsonNode.isDouble() && !jsonNode.isFloat()) {
                if (jsonNode.isTextual()) {
                    if (schema == null
                            || schema.getType().equals(Schema.Type.STRING)
                            || schema.getType().equals(Schema.Type.ENUM)) {
                        return jsonNode.asText();
                    }

                    if (schema.getType().equals(Schema.Type.BYTES)
                            || schema.getType().equals(Schema.Type.FIXED)) {
                        return jsonNode.textValue().getBytes(StandardCharsets.ISO_8859_1);
                    }
                } else {
                    if (jsonNode.isArray()) {
                        List<Object> l = new ArrayList();

                        for (JsonNode node : jsonNode) {
                            l.add(toObject(node, schema == null ? null : schema.getElementType()));
                        }

                        return l;
                    }

                    if (jsonNode.isObject()) {
                        Map<Object, Object> m = new LinkedHashMap();
                        Iterator<String> it = jsonNode.fieldNames();

                        while (it.hasNext()) {
                            String key = (String) it.next();
                            Schema s;
                            if (schema != null && schema.getType().equals(Schema.Type.MAP)) {
                                s = schema.getValueType();
                            } else if (schema != null
                                    && schema.getType().equals(Schema.Type.RECORD)) {
                                s = schema.getField(key).schema();
                            } else {
                                s = null;
                            }

                            Object value = toObject(jsonNode.get(key), s);
                            m.put(key, value);
                        }

                        return m;
                    }
                }
            } else {
                if (schema == null || schema.getType().equals(Schema.Type.DOUBLE)) {
                    return jsonNode.asDouble();
                }

                if (schema.getType().equals(Schema.Type.FLOAT)) {
                    return (float) jsonNode.asDouble();
                }
            }

            return null;
        }
    }

    public static Map objectToMap(Object datum) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        return (Map) mapper.convertValue(datum, Map.class);
    }
}
