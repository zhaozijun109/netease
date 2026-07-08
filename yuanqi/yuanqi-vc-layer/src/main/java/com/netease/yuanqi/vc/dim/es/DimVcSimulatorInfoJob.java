package com.netease.yuanqi.vc.dim.es;

import co.elastic.clients.elasticsearch.core.bulk.BulkOperationVariant;
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation;
import com.netease.yuanqi.common.config.ClusterConfigOptions;
import com.netease.yuanqi.common.pojo.ods.binlog.BinlogRow;
import com.netease.yuanqi.common.sink.es.Es8Emitter;
import com.netease.yuanqi.common.sink.es.EsCommonSink;
import com.netease.yuanqi.common.source.kafka.KafkaCommonSource;
import com.netease.yuanqi.common.utils.DateTimeFormatterUtils;
import com.netease.yuanqi.vc.operator.dim.es.DimVcSimulatorInfoRichFlatMapFunction;
import java.util.HashMap;
import java.util.Map;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class DimVcSimulatorInfoJob {
    private static void dimVcSimulatorInfoJob(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(120000);

        env.fromSource(
                        new KafkaCommonSource("vc.binlog.online", "DimVcSimulatorInfoJob")
                                .createLogSource(),
                        WatermarkStrategy.noWatermarks(),
                        "DimVcSimulatorInfoSource")
                .setParallelism(1)
                .uid("DimVcSimulatorInfoKafkaSource")
                .name("DimVcSimulatorInfoKafkaSource")
                .flatMap(new DimVcSimulatorInfoRichFlatMapFunction())
                .setParallelism(1)
                .uid("DimVcSimulatorInfoRichFlatMapFunction")
                .name("DimVcSimulatorInfoRichFlatMapFunction")
                .sinkTo(
                        new EsCommonSink(ClusterConfigOptions.EsHostsEnum.VC)
                                .createCommonBinlogEs8Sink(
                                        new Es8Emitter<BinlogRow>() {
                                            @Override
                                            public BulkOperationVariant emit(BinlogRow binlogRow) {
                                                Map<String, Object> data = binlogRow.getData();
                                                String id = data.get("id").toString();

                                                Map<String, Object> doc = new HashMap<>();
                                                doc.put(
                                                        "create_time",
                                                        parseTimestampMillis(
                                                                data.get("create_time")));
                                                doc.put(
                                                        "update_time",
                                                        parseTimestampMillis(
                                                                data.get("update_time")));
                                                doc.put("name", getString(data, "name"));
                                                doc.put(
                                                        "description",
                                                        getString(data, "description"));
                                                doc.put(
                                                        "cover_image_url",
                                                        getString(data, "cover_image_url"));
                                                doc.put("category", getInt(data, "category"));
                                                doc.put("type", getInt(data, "type"));
                                                doc.put("weight", getLong(data, "weight"));
                                                doc.put("status", getLong(data, "status"));
                                                doc.put(
                                                        "config_ext",
                                                        getString(data, "config_ext"));
                                                doc.put("is_trial", getInt(data, "is_trial"));
                                                doc.put(
                                                        "simulator_id",
                                                        getString(data, "simulator_id"));
                                                doc.put("version", getLong(data, "version"));
                                                doc.put(
                                                        "business_ext",
                                                        getString(data, "business_ext"));
                                                doc.put("create_by", getString(data, "create_by"));
                                                doc.put("update_by", getString(data, "update_by"));
                                                doc.put(
                                                        "effective_time",
                                                        parseTimestampMillis(
                                                                data.get("effective_time")));
                                                doc.put(
                                                        "expiration_time",
                                                        parseTimestampMillis(
                                                                data.get("expiration_time")));
                                                doc.put(
                                                        "content_type",
                                                        getInt(data, "content_type"));
                                                doc.put(
                                                        "front_tags",
                                                        getString(data, "front_tags"));
                                                doc.put(
                                                        "backend_tags",
                                                        getString(data, "backend_tags"));
                                                doc.put("channel", getInt(data, "channel"));
                                                doc.put(
                                                        "public_scope",
                                                        getInt(data, "public_scope"));
                                                doc.put("user_id", getLong(data, "user_id"));
                                                doc.put(
                                                        "user_commit",
                                                        getString(data, "user_commit"));
                                                doc.put(
                                                        "push_start_time",
                                                        parseTimestampMillis(
                                                                data.get("push_start_time")));
                                                doc.put(
                                                        "push_end_time",
                                                        parseTimestampMillis(
                                                                data.get("push_end_time")));
                                                doc.put(
                                                        "game_user_id",
                                                        getLong(data, "game_user_id"));

                                                return new IndexOperation.Builder<>()
                                                        .index("vc_binlog_simulator_info")
                                                        .id(id)
                                                        .document(doc)
                                                        .build();
                                            }
                                        }))
                .setParallelism(1)
                .uid("DimVcSimulatorInfoEsSink")
                .name("DimVcSimulatorInfoEsSink");

        env.execute("DimVcSimulatorInfoJob");
    }

    private static String getString(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value == null ? null : value.toString();
    }

    private static Integer getInt(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        String s = value.toString().trim();
        return s.isEmpty() ? null : Integer.parseInt(s);
    }

    private static Long getLong(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        String s = value.toString().trim();
        return s.isEmpty() ? null : Long.parseLong(s);
    }

    /**
     * 将 binlog 中的时间字段统一解析为毫秒级时间戳，与 ES mapping 中的 {@code epoch_millis} 格式对齐.
     *
     * <p>binlog 中时间字段存在两种形式：
     *
     * <ul>
     *   <li>数字（Number 或纯数字字符串）：业务侧使用微秒（例如 1812454724766000），除以 1000 得毫秒
     *   <li>ISO-8601 字符串（例如 2026-06-08T09:17:27Z）：通过 {@link
     *       DateTimeFormatterUtils#isoZonedDateTimeFormatTransformTime} 解析为毫秒
     * </ul>
     */
    private static Long parseTimestampMillis(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue() / 1000L;
        }
        String s = value.toString().trim();
        if (s.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(s) / 1000L;
        } catch (NumberFormatException ignore) {
            return DateTimeFormatterUtils.isoZonedDateTimeFormatTransformTime(s);
        }
    }

    public static void main(String[] args) throws Exception {
        dimVcSimulatorInfoJob(ParameterTool.fromArgs(args));
    }
}
