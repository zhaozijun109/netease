package com.netease.yuanqi.vc.dim.es;

import co.elastic.clients.elasticsearch.core.bulk.BulkOperationVariant;
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation;
import com.netease.yuanqi.common.config.ClusterConfigOptions;
import com.netease.yuanqi.common.pojo.ods.binlog.BinlogRow;
import com.netease.yuanqi.common.sink.es.Es8Emitter;
import com.netease.yuanqi.common.sink.es.EsCommonSink;
import com.netease.yuanqi.common.source.kafka.KafkaCommonSource;
import com.netease.yuanqi.vc.operator.dim.es.DimVcCharacterMomentRichFlatMapFunction;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * 同步 {@code vc_character_moment} binlog 到 3 个 ES 索引（{@code vc_circle_moment} / {@code
 * vc_exclusive_moment} / {@code vc_simulator_moment}），按 {@code circle_source} / {@code public_type}
 * 路由（参见 moment-es-sync-spec.md §2-3）.
 *
 * <p>实现策略：仅做 upsert，不做 DELETE，物理删除由查询方按 {@code status} 字段过滤处理。即使一条记录路由发生变更，旧索引中的副本也由查询方按 {@code
 * status} 自行过滤，不在此处兜底删除。
 */
public class DimVcCharacterMomentJob {

    private static final String INDEX_CIRCLE = "vc_circle_moment";
    private static final String INDEX_EXCLUSIVE = "vc_exclusive_moment";
    private static final String INDEX_SIMULATOR = "vc_simulator_moment";

    /**
     * Hive 表 {@code ods_db_vc_character_moment_nd} 的 27 个字段，加上由 {@link
     * DimVcCharacterMomentRichFlatMapFunction} 通过 JDBC 维表查询补全的 {@code character_type}（mapping 中存在但
     * binlog 不会自带），共 28 个字段，与 ES 文档体一一对应.
     */
    private static final List<String> DOC_COLUMNS =
            Arrays.asList(
                    "id",
                    "character_id",
                    "content",
                    "location",
                    "ext",
                    "public_type",
                    "create_time",
                    "update_time",
                    "release_time",
                    "comment_count",
                    "like_count",
                    "created_by",
                    "updated_by",
                    "channel",
                    "status",
                    "send_msg_flag",
                    "content_md5",
                    "post_id",
                    "author_type",
                    "author_id",
                    "post_type",
                    "user_id",
                    "circle_source",
                    "script_json",
                    "tags",
                    "target_user_id",
                    "simulator_id",
                    "character_type");

    /** 时间字段（bigint 微秒）：写入 ES 前 ÷ 1000 转毫秒，对齐 mapping 的 {@code epoch_millis} 格式. */
    private static final List<String> TIME_COLUMNS_MICROS =
            Arrays.asList("create_time", "update_time", "release_time");

    private static void dimVcCharacterMomentJob(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(120000);

        env.fromSource(
                        new KafkaCommonSource("vc.binlog.online", "DimVcCharacterMomentJob")
                                .createLogSource(),
                        WatermarkStrategy.noWatermarks(),
                        "DimVcCharacterMomentSource")
                .setParallelism(1)
                .uid("DimVcCharacterMomentKafkaSource")
                .name("DimVcCharacterMomentKafkaSource")
                .flatMap(new DimVcCharacterMomentRichFlatMapFunction())
                .setParallelism(1)
                .uid("DimVcCharacterMomentRichFlatMapFunction")
                .name("DimVcCharacterMomentRichFlatMapFunction")
                .sinkTo(
                        new EsCommonSink(ClusterConfigOptions.EsHostsEnum.VC)
                                .createCommonBinlogEs8Sink(
                                        new Es8Emitter<BinlogRow>() {
                                            @Override
                                            public BulkOperationVariant emit(BinlogRow binlogRow) {
                                                Map<String, Object> data = binlogRow.getData();
                                                String docId = data.get("id").toString();
                                                String targetIndex = route(data);
                                                Map<String, Object> doc = buildDoc(data);
                                                return new IndexOperation.Builder<>()
                                                        .index(targetIndex)
                                                        .id(docId)
                                                        .document(doc)
                                                        .build();
                                            }
                                        }))
                .setParallelism(1)
                .uid("DimVcCharacterMomentEsSink")
                .name("DimVcCharacterMomentEsSink");

        env.execute("DimVcCharacterMomentJob");
    }

    /**
     * 路由：{@code circle_source=5} 优先，其次 {@code public_type=4}，否则默认 {@link #INDEX_CIRCLE}.
     *
     * <p>历史数据兼容：路由 Key 为 NULL 时不会命中特殊索引，自然落入默认。
     */
    private static String route(Map<String, Object> data) {
        Integer circleSource = getInt(data, "circle_source");
        if (circleSource != null && circleSource == 5) {
            return INDEX_SIMULATOR;
        }
        Integer publicType = getInt(data, "public_type");
        if (publicType != null && publicType == 4) {
            return INDEX_EXCLUSIVE;
        }
        return INDEX_CIRCLE;
    }

    /** 构造 ES 文档体：hive 表 27 字段 + 维表查询补全的 {@code character_type}，时间字段微秒 → 毫秒. */
    private static Map<String, Object> buildDoc(Map<String, Object> data) {
        Map<String, Object> doc = new HashMap<>();
        for (String col : DOC_COLUMNS) {
            if (TIME_COLUMNS_MICROS.contains(col)) {
                doc.put(col, microsToMillis(data.get(col)));
            } else {
                doc.put(col, data.get(col));
            }
        }
        return doc;
    }

    /** 将 binlog 中的微秒时间值转换为毫秒；null / 解析失败返回 null. */
    private static Long microsToMillis(Object value) {
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
            return null;
        }
    }

    /** null / 空串 / 非数字均返回 null. */
    private static Integer getInt(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        String s = value.toString().trim();
        if (s.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static void main(String[] args) throws Exception {
        dimVcCharacterMomentJob(ParameterTool.fromArgs(args));
    }
}
