package com.netease.yuanqi.common.serialization.binlog.debezium;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.cdc.debezium.DebeziumDeserializationSchema;
import org.apache.flink.util.Collector;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.json.JsonConverter;
import org.apache.kafka.connect.json.JsonConverterConfig;
import org.apache.kafka.connect.source.SourceRecord;

public class LocalTimestampJsonCdcSchema implements DebeziumDeserializationSchema<String> {

    private final boolean includeSchema;
    private final Map<String, Object> customConverterConfigs;
    private static final long EIGHT_HOURS_SECONDS = 8L * 3600; // 固定本地化修正：减 8 小时（仅 DATETIME）

    private transient JsonConverter jsonConverter;
    private transient ObjectMapper objectMapper;

    public LocalTimestampJsonCdcSchema() {
        this(false);
    }

    public LocalTimestampJsonCdcSchema(Boolean includeSchema) {
        this(includeSchema, null);
    }

    public LocalTimestampJsonCdcSchema(
            Boolean includeSchema, Map<String, Object> customConverterConfigs) {
        this.includeSchema = includeSchema != null && includeSchema;
        this.customConverterConfigs = customConverterConfigs;
    }

    private void ensureInitialized() {
        if (this.jsonConverter == null) {
            Map<String, Object> configs = new HashMap<>();
            configs.put(JsonConverterConfig.SCHEMAS_ENABLE_CONFIG, includeSchema);
            if (this.customConverterConfigs != null) {
                configs.putAll(this.customConverterConfigs);
            }
            this.jsonConverter = new JsonConverter();
            this.jsonConverter.configure(configs, /* isKey = */ false);
        }
        if (this.objectMapper == null) {
            this.objectMapper = new ObjectMapper();
        }
    }

    private static boolean isSchemaNamed(Schema schema, String name) {
        return schema != null && name.equals(schema.name());
    }

    // - DATE -> yyyy-MM-dd
    // - DATETIME(Timestamp/Micro/Nano) -> epoch 秒（减 8 小时）
    // - TIMESTAMP(ZonedTimestamp) -> epoch 秒（UTC）
    private JsonNode convertTemporal(JsonNode node, Schema schema) {
        if (node == null || schema == null || node.isNull()) {
            return null;
        }

        if (isSchemaNamed(schema, io.debezium.time.Date.SCHEMA_NAME)) {
            if (node.isNumber()) {
                long days = node.asLong();
                String date = LocalDate.ofEpochDay(days).format(DateTimeFormatter.ISO_LOCAL_DATE);
                return JsonNodeFactory.instance.textNode(date);
            }
            return null;
        }

        if (isSchemaNamed(schema, io.debezium.time.Timestamp.SCHEMA_NAME)) {
            if (node.isNumber()) {
                long millis = node.asLong();
                long seconds = (millis / 1000L) - EIGHT_HOURS_SECONDS;
                return JsonNodeFactory.instance.numberNode(seconds);
            }
            return null;
        }

        if (isSchemaNamed(schema, io.debezium.time.MicroTimestamp.SCHEMA_NAME)) {
            if (node.isNumber()) {
                long micros = node.asLong();
                long seconds = (micros / 1_000_000L) - EIGHT_HOURS_SECONDS;
                return JsonNodeFactory.instance.numberNode(seconds);
            }
            return null;
        }

        if (isSchemaNamed(schema, io.debezium.time.NanoTimestamp.SCHEMA_NAME)) {
            if (node.isNumber()) {
                long nanos = node.asLong();
                long seconds = (nanos / 1_000_000_000L) - EIGHT_HOURS_SECONDS;
                return JsonNodeFactory.instance.numberNode(seconds);
            }
            return null;
        }

        if (isSchemaNamed(schema, io.debezium.time.ZonedTimestamp.SCHEMA_NAME)) {
            if (node.isTextual()) {
                String ts = node.asText();
                try {
                    long seconds = Instant.parse(ts).getEpochSecond();
                    return JsonNodeFactory.instance.numberNode(seconds);
                } catch (Exception ignore) {
                    return null;
                }
            }
            return null;
        }

        return null;
    }

    // 在 JSON 层统一处理
    private void postProcessDateAndZonedTimestamp(JsonNode node, Schema schema) {
        if (node == null || schema == null) {
            return;
        }

        if (Objects.requireNonNull(schema.type()) == Schema.Type.STRUCT) {
            if (!(node instanceof ObjectNode)) {
                return;
            }
            ObjectNode obj = (ObjectNode) node;
            for (Field f : schema.fields()) {
                Schema fs = f.schema();
                JsonNode child = obj.get(f.name());
                if (child == null || child.isNull() || fs == null) {
                    continue;
                }

                JsonNode converted = convertTemporal(child, fs);
                if (converted != null) {
                    obj.set(f.name(), converted);
                } else {
                    // 递归处理嵌套
                    postProcessDateAndZonedTimestamp(child, fs);
                }
            }
        }
    }

    @Override
    public void deserialize(SourceRecord record, Collector<String> out) throws Exception {
        ensureInitialized();

        byte[] bytes =
                this.jsonConverter.fromConnectData(
                        record.topic(), record.valueSchema(), record.value());

        JsonNode root = objectMapper.readTree(bytes);

        JsonNode payloadNode = includeSchema ? root.get("payload") : root;
        if (payloadNode != null && record.valueSchema() != null) {
            postProcessDateAndZonedTimestamp(payloadNode, record.valueSchema());
        }

        String result = objectMapper.writeValueAsString(root);
        out.collect(result);
    }

    @Override
    public TypeInformation<String> getProducedType() {
        return BasicTypeInfo.STRING_TYPE_INFO;
    }
}
