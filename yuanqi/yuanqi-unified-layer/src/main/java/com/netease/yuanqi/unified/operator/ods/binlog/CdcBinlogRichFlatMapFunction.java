package com.netease.yuanqi.unified.operator.ods.binlog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.yuanqi.common.pojo.ods.binlog.BinlogRow;
import java.util.HashMap;
import java.util.Map;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.Counter;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CdcBinlogRichFlatMapFunction extends RichFlatMapFunction<String, String> {
    private static final Logger LOG = LoggerFactory.getLogger(CdcBinlogRichFlatMapFunction.class);
    private ObjectMapper objectMapper;
    private transient Counter dirtyCounter;
    // 是否需要基于库名与原主键 id 重写主键 id
    private final boolean rewritePrimaryKey;

    public CdcBinlogRichFlatMapFunction() {
        this(false);
    }

    public CdcBinlogRichFlatMapFunction(boolean rewritePrimaryKey) {
        this.rewritePrimaryKey = rewritePrimaryKey;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        objectMapper = new ObjectMapper();
        dirtyCounter = getRuntimeContext().getMetricGroup().counter("cdc_binlog_dirty_dropped");
    }

    @Override
    public void flatMap(String value, Collector<String> collector) throws Exception {
        try {
            JsonNode root = objectMapper.readTree(value);
            Map<String, Object> before = parseMap(objectMapper, root.get("before"));
            Map<String, Object> after = parseMap(objectMapper, root.get("after"));

            String op = root.path("op").asText("");
            int opType =
                    "c".equals(op) || "r".equals(op)
                            ? 0
                            : "d".equals(op) ? 1 : "u".equals(op) ? 2 : -1;

            // 基础字段校验
            String table = root.path("source").path("table").asText(null);
            long ts = root.path("ts_ms").asLong(0L);

            if (opType == -1 || table == null || table.isEmpty() || ts <= 0L) {
                markDirtyAndLog("invalid basic fields", value);
                return; // 丢弃
            }

            // 不同操作的内容校验
            Map<String, Object> columns;
            Map<String, Object> old = new HashMap<>();
            if (opType == 0) { // insert/read
                if (after == null || after.isEmpty()) {
                    markDirtyAndLog("insert/read without after", value);
                    return;
                }
                columns = after;
            } else if (opType == 1) { // delete
                if (before == null || before.isEmpty()) {
                    markDirtyAndLog("delete without before", value);
                    return;
                }
                columns = before;
            } else { // update
                if (before == null || after == null) {
                    markDirtyAndLog("update without before/after", value);
                    return;
                }
                columns = after;
                for (Map.Entry<String, Object> entry : before.entrySet()) {
                    String key = entry.getKey();
                    Object v = entry.getValue();
                    Object av = after.get(key);
                    if (!java.util.Objects.equals(v, av)) {
                        old.put(key, v);
                    }
                }
            }

            // 判断是否需要重写主键 id
            if (rewritePrimaryKey) {
                // 解析库与表分片信息，并将输出表名标准化（去掉末尾 _num）
                String dbName = root.path("source").path("db").asText(null);
                int dbShard = extractShardCode(dbName);
                int tableShard = extractShardCode(table);
                String shardFullName = (dbName == null ? "" : dbName) + "." + table;

                maybeRewritePrimaryKey(columns, dbShard, tableShard);

                columns.put("shardFullName", shardFullName);
                columns.put("dbShard", dbShard);
                columns.put("tableShard", tableShard);

                table = stripTableShardSuffix(table);
            }

            long seq = buildSeqnoFromFilePos(root.path("source"));

            BinlogRow binlogRow =
                    BinlogRow.builder()
                            .setTable(table)
                            .setOp(opType)
                            .setOpTime(ts)
                            .setSeqno(seq)
                            .setPartitionId(0)
                            .setData(columns)
                            .setOld(old)
                            .build();

            collector.collect(objectMapper.writeValueAsString(binlogRow));
        } catch (Exception e) {
            markDirtyAndLog("exception during processing", value, e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> parseMap(ObjectMapper mapper, JsonNode object) {
        return object == null ? null : mapper.convertValue(object, Map.class);
    }

    // 根据库/表分片与原主键 id 生成新的 Long 主键 id ，并覆盖到 map 的 "id" 字段
    private void maybeRewritePrimaryKey(Map<String, Object> map, int dbShard, int tableShard) {
        if (map == null) {
            return;
        }
        Object idVal = map.get("id");
        if (idVal == null) {
            return;
        }
        Long originalId = null;
        if (idVal instanceof Number) {
            originalId = ((Number) idVal).longValue();
        } else {
            String s = String.valueOf(idVal).trim();
            try {
                originalId = Long.parseLong(s);
            } catch (NumberFormatException ignore) {
                return;
            }
        }

        long newId = buildLongIdFromShardsAndOriginal(dbShard, tableShard, originalId);
        map.put("id", newId);
        map.put("originalId", originalId);
    }

    // 提取库名分片（db_num）：不存在或非法返回 0
    private int extractShardCode(String dbName) {
        if (dbName == null) {
            return 0;
        }
        int idx = dbName.lastIndexOf('_');
        if (idx < 0 || idx + 1 >= dbName.length()) {
            return 0;
        }
        String suffix = dbName.substring(idx + 1);
        try {
            long v = Long.parseLong(suffix);
            if (v < 0) {
                return 0;
            }
            if (v > 0xFFFFL) {
                return (int) (v & 0xFFFFL);
            } // 截断到 16 位
            return (int) v;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // 去掉表名分片后缀：table_num -> table；若无或非法则返回原表名
    private String stripTableShardSuffix(String tableName) {
        if (tableName == null) {
            return null;
        }
        int idx = tableName.lastIndexOf('_');
        if (idx < 0 || idx + 1 >= tableName.length()) {
            return tableName;
        }
        String suffix = tableName.substring(idx + 1);
        for (int i = 0; i < suffix.length(); i++) {
            if (!Character.isDigit(suffix.charAt(i))) {
                return tableName;
            }
        }
        return tableName.substring(0, idx);
    }

    // 位拼接生成全局唯一 Long：高 16 位为库分片，次高 16 位为表分片，低 32 位为原始 ID（int11 范围）
    private long buildLongIdFromShardsAndOriginal(int dbShard, int tableShard, long originalId) {
        long highDb = ((long) (dbShard & 0xFFFF)) << 48;
        long highTb = ((long) (tableShard & 0xFFFF)) << 32;
        long low = Math.abs(originalId) & 0xFFFFFFFFL;
        return highDb | highTb | low;
    }

    private void markDirtyAndLog(String reason, String value) {
        if (dirtyCounter != null) {
            dirtyCounter.inc();
        }
        if (LOG.isWarnEnabled()) {
            LOG.warn("Dropping dirty CDC record: reason={}, value={}", reason, value);
        }
    }

    private void markDirtyAndLog(String reason, String value, Exception e) {
        if (dirtyCounter != null) {
            dirtyCounter.inc();
        }
        if (LOG.isWarnEnabled()) {
            LOG.warn("Dropping dirty CDC record: reason={}, value={}", reason, value, e);
        }
    }

    // 从 source 节点中解析 binlog 文件名和位置，构建一个大致递增的 seqno 值
    // seqno 结构（从高位到低位）：
    // 20 位：binlog 文件索引（假设文件名格式为 xxx.000001）
    // 32 位：binlog 文件内的字节偏移位置
    // 12 位：同一位置的行号（用于区分同一事务内的多行变更）
    private long buildSeqnoFromFilePos(JsonNode source) {
        if (source == null || source.isMissingNode()) {
            return 0L; // 无法解析时返回 0
        }
        String file = source.path("file").asText("");
        long pos = source.path("pos").asLong(0L);
        int row = source.path("row").asInt(0);

        long fileIndex = 0L;
        if (file != null && !file.isEmpty()) {
            int dot = file.lastIndexOf('.');
            if (dot >= 0 && dot + 1 < file.length()) {
                try {
                    fileIndex = Long.parseLong(file.substring(dot + 1));
                } catch (NumberFormatException ex) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Unexpected binlog file format for seqno: {}", file);
                    }
                }
            }
        }
        if (fileIndex > ((1L << 20) - 1)) {
            fileIndex = fileIndex & ((1L << 20) - 1); // 截断
        }
        return (fileIndex << 44) | ((pos & 0xFFFFFFFFL) << 12) | (row & 0xFFFL);
    }
}
