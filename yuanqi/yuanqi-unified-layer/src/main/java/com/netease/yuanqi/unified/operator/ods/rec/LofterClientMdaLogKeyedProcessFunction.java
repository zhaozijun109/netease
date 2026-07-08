package com.netease.yuanqi.unified.operator.ods.rec;

import com.alibaba.druid.pool.DruidDataSource;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.netease.yuanqi.common.config.ClusterConfigOptions;
import com.netease.yuanqi.common.utils.CommonUtils;
import com.netease.yuanqi.unified.pojo.RecParsedLogEvents;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LofterClientMdaLogKeyedProcessFunction
        extends KeyedProcessFunction<Integer, RecParsedLogEvents, RecParsedLogEvents> {
    private static final Logger LOG =
            LoggerFactory.getLogger(LofterClientMdaLogKeyedProcessFunction.class);
    private static final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor =
            new ScheduledThreadPoolExecutor(1);
    private static final Long REPEAT_WINDOW = 5 * 60 * 1000L;

    private DruidDataSource dataSource;
    private Cache<String, Tuple2<Integer, Integer>> recBuriedPointCache;
    private MapState<Integer, Long> repeatState;
    private ObjectMapper objectMapper;

    @Override
    public void open(OpenContext openContext) throws Exception {
        dataSource = new DruidDataSource();
        dataSource.setDriverClassName(
                ClusterConfigOptions.getMysqlDriverClassName(
                        ClusterConfigOptions.MysqlConnectionEnum.LOFTER_RECOMMEND));
        dataSource.setUrl(
                ClusterConfigOptions.getMysqlConnection(
                        ClusterConfigOptions.MysqlConnectionEnum.LOFTER_RECOMMEND));
        dataSource.setLoginTimeout(5000);
        dataSource.setTestOnBorrow(true);
        dataSource.setTestOnReturn(true);
        dataSource.setFailFast(true);

        recBuriedPointCache =
                Caffeine.newBuilder()
                        .initialCapacity(1)
                        .maximumSize(10000)
                        .expireAfterWrite(Duration.ofHours(7))
                        .build();

        scheduledThreadPoolExecutor.scheduleWithFixedDelay(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            getRecBuriedPointFromDb();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                },
                0,
                6,
                TimeUnit.HOURS); // Restart the job and the configuration will take effect
        // immediately when encountering emergency

        MapStateDescriptor<Integer, Long> repeatStateDescriptor =
                new MapStateDescriptor<>(
                        "RepeatStateDescriptor",
                        BasicTypeInfo.INT_TYPE_INFO,
                        BasicTypeInfo.LONG_TYPE_INFO);
        StateTtlConfig stateTtlConfig =
                StateTtlConfig.newBuilder(Duration.ofMinutes(15))
                        .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
                        .setStateVisibility(
                                StateTtlConfig.StateVisibility.ReturnExpiredIfNotCleanedUp)
                        .cleanupInRocksdbCompactFilter(10000)
                        .build();
        repeatStateDescriptor.enableTimeToLive(stateTtlConfig);
        repeatState = getRuntimeContext().getMapState(repeatStateDescriptor);

        objectMapper = new ObjectMapper();
    }

    @Override
    public void processElement(
            RecParsedLogEvents recParsedLogEvents,
            KeyedProcessFunction<Integer, RecParsedLogEvents, RecParsedLogEvents>.Context context,
            Collector<RecParsedLogEvents> collector)
            throws Exception {
        Integer versionNumber = CommonUtils.getAppVersionNumber(recParsedLogEvents.getAppVersion());
        Tuple2<Integer, Integer> eventMeta =
                recBuriedPointCache.getIfPresent(recParsedLogEvents.getEventId());
        if (eventMeta != null && versionNumber >= eventMeta.f0) {
            JsonNode root = objectMapper.readTree(recParsedLogEvents.getSource());
            JsonNode attributes = root.get("attributes");
            JsonNode ext = null;
            if (attributes.get("ext") != null) {
                try {
                    ext = objectMapper.readTree(attributes.get("ext").asText());
                } catch (JsonProcessingException ignored) {
                }
            }
            String eventId = recParsedLogEvents.getEventId();
            Integer newActionCode = eventMeta.f1;
            String scene =
                    attributes.get("scene") != null
                            ? attributes.get("scene").asText().toLowerCase()
                            : "";
            String itemId = assignmentItemIdValue(attributes, ext);
            String itemType = assignmentItemTypeValue(attributes, ext);
            String text = assignmentTextValue(attributes, ext);
            // String actionOption = attributes.get("action") != null ?
            // attributes.get("action").asText().toLowerCase() : "";
            long occurTime = root.get("kafkaTime") != null ? root.get("kafkaTime").asLong() : 0L;
            int uk = getUniqueKey(eventId, scene, itemType, itemId, text);
            long currentStateOccurTime = 0L;
            try {
                currentStateOccurTime = repeatState.get(uk) != null ? repeatState.get(uk) : 0L;
            } catch (NullPointerException ignored) {
                // LOG.info("Expired repeat state: {}", recParsedLogEvents.getEventId());
            }

            if (occurTime > currentStateOccurTime + 20000 && !scene.isEmpty()) {
                repeatState.put(uk, occurTime);
            }
            boolean isRepeat =
                    currentStateOccurTime > 0
                            && occurTime < currentStateOccurTime + REPEAT_WINDOW
                            && !scene.isEmpty();
            if (isRepeat) {
                ObjectNode objectNode = root.deepCopy();
                objectNode.put("repeat", 1);
                collector.collect(
                        RecParsedLogEvents.builder()
                                .setEventId(recParsedLogEvents.getEventId())
                                .setDeviceUdid(recParsedLogEvents.getDeviceUdid())
                                .setAppVersion(recParsedLogEvents.getAppVersion())
                                .setSource(objectMapper.writeValueAsString(objectNode))
                                .setAction(newActionCode)
                                .build());
            } else {
                collector.collect(
                        RecParsedLogEvents.builder()
                                .setEventId(recParsedLogEvents.getEventId())
                                .setDeviceUdid(recParsedLogEvents.getDeviceUdid())
                                .setAppVersion(recParsedLogEvents.getAppVersion())
                                .setSource(recParsedLogEvents.getSource())
                                .setAction(newActionCode)
                                .build());
            }
        }
    }

    private String assignmentItemIdValue(JsonNode attributes, JsonNode extJson) {
        String itemIdOption =
                attributes.get("itemId") != null
                        ? attributes.get("itemId").asText().toLowerCase()
                        : "";
        if (itemIdOption.isEmpty() && extJson != null) {
            return extJson.hasNonNull("itemId") ? extJson.get("itemId").asText().toLowerCase() : "";
        }
        return itemIdOption;
    }

    private String assignmentItemTypeValue(JsonNode attributes, JsonNode extJson) {
        String itemTypeOption =
                attributes.get("itemType") != null
                        ? attributes.get("itemType").asText().toLowerCase()
                        : "";
        if (itemTypeOption.isEmpty() && extJson != null) {
            return extJson.hasNonNull("itemType")
                    ? extJson.get("itemType").asText().toLowerCase()
                    : "";
        }
        return itemTypeOption;
    }

    private String assignmentTextValue(JsonNode attributes, JsonNode extJson) {
        String textOption =
                attributes.get("text") != null ? attributes.get("text").asText().toLowerCase() : "";
        if (textOption.isEmpty() && extJson != null) {
            return extJson.hasNonNull("text") ? extJson.get("text").asText().toLowerCase() : "";
        }
        return textOption;
    }

    private void getRecBuriedPointFromDb() throws SQLException {
        Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement =
                connection.prepareStatement(
                        "SELECT eventId, appVersion, actionCode FROM Yq_Rec_Buried_Point WHERE enable = 1 AND businessName = ? AND dataSource = ? AND id > ? ORDER BY id ASC LIMIT 50000");
        long preId = Long.MIN_VALUE;
        int count = 0;
        do {
            int res = 0;
            preparedStatement.setString(1, "lofter");
            preparedStatement.setString(2, "client");
            preparedStatement.setLong(3, preId);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                recBuriedPointCache.put(
                        rs.getString("eventId"),
                        Tuple2.of(
                                CommonUtils.getAppVersionNumber(rs.getString("appVersion")),
                                rs.getInt("actionCode")));
                res += 1;
            }
            count = res;
            LOG.info("Get rec buried point from db, count: {}.", count);
        } while (count == 50000);
        preparedStatement.close();
        connection.close();
        if (recBuriedPointCache.estimatedSize() == 0) {
            throw new RuntimeException("Rec buried point is empty.");
        }
    }

    private int getUniqueKey(
            String eventId, String scene, String itemType, String itemId, String text) {
        int hashCode = Objects.hashCode(eventId);
        hashCode = 31 * hashCode + Objects.hashCode(scene);
        hashCode = 31 * hashCode + Objects.hashCode(itemType);
        hashCode = 31 * hashCode + Objects.hashCode(itemId);
        hashCode = 31 * hashCode + Objects.hashCode(text);
        return hashCode;
    }
}
