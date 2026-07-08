package com.netease.yuanqi.vc.operator.dim.es;

import com.alibaba.druid.pool.DruidDataSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.netease.yuanqi.common.config.ClusterConfigOptions;
import com.netease.yuanqi.common.pojo.ods.binlog.BinlogRow;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 消费 {@code vc_character_moment} 表 binlog，只透传 INSERT / UPDATE 事件，物理 DELETE 由查询方按 {@code status}
 * 字段过滤 处理（参见 moment-es-sync-spec.md，业务侧明确"通过状态过滤查询数据"）.
 *
 * <p>vc binlog op 编码：0=INSERT、1=DELETE、2=UPDATE。
 *
 * <p>同时通过 JDBC + Caffeine 维表查询补全 {@code character_type}：以 {@code character_id} 为 key，命中缓存即返回， 未命中查
 * {@code vcharacter.vc_character}；结果以 sentinel {@link #CHARACTER_TYPE_MISSING} 标记 DB 中找不到的 id，
 * 避免缓存穿透；查询失败时不阻塞 binlog 写入（character_type 字段写 null）。
 */
public class DimVcCharacterMomentRichFlatMapFunction
        extends RichFlatMapFunction<String, BinlogRow> {
    private static final Logger LOG =
            LoggerFactory.getLogger(DimVcCharacterMomentRichFlatMapFunction.class);

    private static final String TABLE_NAME = "vc_character_moment";

    /** vc_character 表中找不到对应 id 时缓存的 sentinel 值，对外（写 ES 时）会被还原为 null. */
    private static final Integer CHARACTER_TYPE_MISSING = -1;

    /** vc_character 维表查询 SQL（按 id 单点查 type，保留库名前缀以支持跨库查询）. */
    private static final String LOOKUP_SQL =
            "SELECT type AS character_type FROM vcharacter.vc_character WHERE id = ?";

    private transient ObjectMapper objectMapper;
    private transient DruidDataSource dataSource;
    private transient Cache<Long, Integer> characterTypeCache;

    @Override
    public void open(OpenContext openContext) throws Exception {
        objectMapper = new ObjectMapper();

        dataSource = new DruidDataSource();
        dataSource.setDriverClassName(
                ClusterConfigOptions.getMysqlDriverClassName(
                        ClusterConfigOptions.MysqlConnectionEnum.VC));
        dataSource.setUrl(
                ClusterConfigOptions.getMysqlConnection(
                        ClusterConfigOptions.MysqlConnectionEnum.VC));
        dataSource.setLoginTimeout(5000);
        dataSource.setTestOnBorrow(true);
        dataSource.setTestOnReturn(true);
        dataSource.setFailFast(true);

        characterTypeCache =
                Caffeine.newBuilder()
                        .initialCapacity(1024)
                        .maximumSize(10000)
                        .expireAfterWrite(Duration.ofHours(24))
                        .build();
    }

    @Override
    public void close() throws Exception {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @Override
    public void flatMap(String s, Collector<BinlogRow> collector) throws Exception {
        BinlogRow binlogRow = objectMapper.readValue(s, BinlogRow.class);
        // 只保留目标表的 INSERT (0) / UPDATE (2) 事件，DELETE 不下发到 sink
        if (TABLE_NAME.equalsIgnoreCase(binlogRow.getTable())
                && (binlogRow.getOp() == 0 || binlogRow.getOp() == 2)) {
            // 拷贝 data 副本并补全 character_type，再通过 builder 重建 BinlogRow，
            // 避免 mutate 上游对象——在 Flink 启用 objectReuse 等场景下保持算子之间不共享可变状态.
            Map<String, Object> enrichedData = new HashMap<>(binlogRow.getData());
            enrichedData.put(
                    "character_type", lookupCharacterType(enrichedData.get("character_id")));

            BinlogRow enrichedRow =
                    BinlogRow.builder()
                            .setTable(binlogRow.getTable())
                            .setOp(binlogRow.getOp())
                            .setOpTime(binlogRow.getOpTime())
                            .setSeqno(binlogRow.getSeqno())
                            .setPartitionId(binlogRow.getPartitionId())
                            .setData(enrichedData)
                            .setOld(binlogRow.getOld())
                            .build();

            collector.collect(enrichedRow);
        }
    }

    /**
     * 查询 character_type：Caffeine 命中直接返回；未命中走 JDBC，结果（含 sentinel）回填缓存。返回 null 表示无法确定（id 缺失 / 找不到 /
     * 查询异常）.
     */
    private Integer lookupCharacterType(Object characterIdObj) {
        if (characterIdObj == null) {
            return null;
        }
        Long characterId;
        try {
            characterId =
                    characterIdObj instanceof Number
                            ? ((Number) characterIdObj).longValue()
                            : Long.parseLong(characterIdObj.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }

        Integer cached =
                characterTypeCache.get(
                        characterId,
                        id -> {
                            try {
                                Integer fromDb = queryCharacterTypeFromDb(id);
                                return fromDb == null ? CHARACTER_TYPE_MISSING : fromDb;
                            } catch (Exception e) {
                                LOG.warn(
                                        "Failed to query character_type for character_id={}",
                                        id,
                                        e);
                                // 查询异常时不缓存，下次还会重试；返回 null 走 ES 字段为空
                                return null;
                            }
                        });

        if (cached == null || CHARACTER_TYPE_MISSING.equals(cached)) {
            return null;
        }
        return cached;
    }

    private Integer queryCharacterTypeFromDb(Long characterId) throws Exception {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(LOOKUP_SQL)) {
            ps.setLong(1, characterId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int v = rs.getInt("character_type");
                    return rs.wasNull() ? null : v;
                }
            }
        }
        return null;
    }
}
