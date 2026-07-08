package com.netease.yuanqi.lofter.ads.ad;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.dts.common.subscribe.SubscribeEvent;
import com.netease.yuanqi.common.config.ClusterConfigOptions;
import com.netease.yuanqi.common.pojo.config.KafkaConfig;
import com.netease.yuanqi.common.sink.kafka.KafkaBaseSink;
import com.netease.yuanqi.common.source.kafka.KafkaBaseSource;
import com.netease.yuanqi.lofter.pojo.ads.ad.AdNewUserFirstActionRecord;
import java.io.Serializable;
import java.time.Duration;
import java.util.Properties;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

public class AdNewUserFirstActionSync {
    private static final long WINDOW_MILLIS = Duration.ofHours(24).toMillis();
    private static final String ACTION_REGISTER = "REGISTER";
    private static final String ACTION_LIKE = "LIKE";
    private static final String ACTION_COMMENT = "COMMENT";

    private static final String SINK_TOPIC = "LOFTER.COMMON.ATTRIBUTION.KEY_ACTION";

    public static void main(String[] args) throws Exception {
        syncNewUserFirstActionToMusic(ParameterTool.fromArgs(args));
    }

    private static void syncNewUserFirstActionToMusic(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(120000);

        Properties sinkProperties = new Properties();
        sinkProperties.setProperty("partition.discovery.interval.ms", "60000");

        KafkaSink<String> sink =
                new KafkaBaseSink(
                                KafkaConfig.builder()
                                        .setBootstrapServers(
                                                ClusterConfigOptions.getKafkaBootStrapServers(
                                                        ClusterConfigOptions
                                                                .KafkaBootstrapServersEnum
                                                                .LOFTER_BACKEND))
                                        .setTopic(SINK_TOPIC)
                                        .setProperties(sinkProperties)
                                        .build())
                        .createLogSink();

        DataStream<UserAction> registerActions =
                env.fromSource(
                                new KafkaBaseSource(
                                                KafkaConfig.builder()
                                                        .setBootstrapServers(
                                                                ClusterConfigOptions
                                                                        .getKafkaBootStrapServers(
                                                                                ClusterConfigOptions
                                                                                        .KafkaBootstrapServersEnum
                                                                                        .LOFTER_BACKEND))
                                                        .setTopics(
                                                                "LOFTER.COMMON.ATTRIBUTION.regsiter")
                                                        .setGroupId(
                                                                "lofter_new_user_first_action_register_sync")
                                                        .build())
                                        .createLogSource(),
                                WatermarkStrategy.noWatermarks(),
                                "register-topic-source")
                        .uid("register-topic-source")
                        .flatMap(new RegisterLogToUserActionFlatMapFunction())
                        .uid("register-topic-flatmap-user-action")
                        .assignTimestampsAndWatermarks(
                                WatermarkStrategy.<UserAction>forBoundedOutOfOrderness(
                                                Duration.ofMinutes(5))
                                        .withTimestampAssigner(
                                                (SerializableTimestampAssigner<UserAction>)
                                                        (element, recordTimestamp) ->
                                                                element.getTime())
                                        .withIdleness(Duration.ofMinutes(1)))
                        .uid("register-user-action-watermark");

        DataStream<UserAction> actions =
                env.fromSource(
                                new KafkaBaseSource(
                                                KafkaConfig.builder()
                                                        .setBootstrapServers(
                                                                ClusterConfigOptions
                                                                        .getKafkaBootStrapServers(
                                                                                ClusterConfigOptions
                                                                                        .KafkaBootstrapServersEnum
                                                                                        .LOFTER_DATA))
                                                        .setTopics("lofter.binlog.ndc")
                                                        .setGroupId(
                                                                "lofter_new_user_first_action_sync")
                                                        .build())
                                        .createNdcBinlogSource(),
                                WatermarkStrategy.noWatermarks(),
                                "ndc-binlog-source")
                        .uid("ndc-binlog-source")
                        .flatMap(new SubscribeEventToUserActionFlatMapFunction())
                        .uid("ndc-binlog-flatmap-user-action")
                        .assignTimestampsAndWatermarks(
                                WatermarkStrategy.<UserAction>forBoundedOutOfOrderness(
                                                Duration.ofMinutes(5))
                                        .withTimestampAssigner(
                                                (SerializableTimestampAssigner<UserAction>)
                                                        (element, recordTimestamp) ->
                                                                element.getTime())
                                        .withIdleness(Duration.ofMinutes(1)))
                        .uid("user-action-watermark");

        registerActions
                .union(actions)
                .keyBy(UserAction::getUserId)
                .process(new NewUserFirstActionProcessFunction())
                .uid("new-user-first-action-process")
                .sinkTo(sink)
                .uid("new-user-first-action-sink");
        env.execute("lofter new user first action sync");
    }

    private static class SubscribeEventToUserActionFlatMapFunction
            extends RichFlatMapFunction<SubscribeEvent, UserAction> {

        @Override
        public void open(OpenContext openContext) throws Exception {
            super.open(openContext);
        }

        @Override
        public void flatMap(SubscribeEvent subscribeEvent, Collector<UserAction> collector) {
            for (SubscribeEvent.OneRowChange row : subscribeEvent.getRowChanges()) {
                String tableName = row.getTableName() != null ? row.getTableName() : "";
                if ("posthot".equalsIgnoreCase(tableName)
                        && row.getType() == SubscribeEvent.RowChangeType.INSERT) {
                    SubscribeEvent.OneColumnChange typeColumn = row.getColumn("Type");
                    Integer type = typeColumn != null ? (Integer) typeColumn.getNewValue() : null;
                    if (type == null || type != 1) {
                        continue;
                    }

                    SubscribeEvent.OneColumnChange userIdColumn = row.getColumn("PublisherUserID");
                    SubscribeEvent.OneColumnChange opTimeColumn = row.getColumn("OpTime");
                    if (userIdColumn == null || opTimeColumn == null) {
                        continue;
                    }
                    Long userId = (Long) userIdColumn.getNewValue();
                    Long opTime = (Long) opTimeColumn.getNewValue();
                    if (userId == null || userId <= 0 || opTime == null || opTime <= 0) {
                        continue;
                    }
                    collector.collect(UserAction.like(userId, opTime, null));
                    continue;
                }

                if ("postresponse".equalsIgnoreCase(tableName)
                        && row.getType() == SubscribeEvent.RowChangeType.INSERT) {
                    SubscribeEvent.OneColumnChange userIdColumn = row.getColumn("PublisherUserID");
                    SubscribeEvent.OneColumnChange publishTimeColumn = row.getColumn("PublishTime");
                    if (userIdColumn == null || publishTimeColumn == null) {
                        continue;
                    }
                    Long userId = (Long) userIdColumn.getNewValue();
                    Long publishTime = (Long) publishTimeColumn.getNewValue();
                    if (userId == null || userId <= 0 || publishTime == null || publishTime <= 0) {
                        continue;
                    }
                    collector.collect(UserAction.comment(userId, publishTime, null));
                }
            }
        }
    }

    private static class RegisterLogToUserActionFlatMapFunction
            extends RichFlatMapFunction<String, UserAction> {
        private transient ObjectMapper objectMapper;

        @Override
        public void open(OpenContext openContext) throws Exception {
            super.open(openContext);
            objectMapper = new ObjectMapper();
        }

        @Override
        public void flatMap(String value, Collector<UserAction> out) {
            if (value == null || value.isEmpty()) {
                return;
            }
            int start = value.indexOf('{');
            if (start < 0) {
                return;
            }
            int end = value.lastIndexOf('}');
            if (end < start) {
                return;
            }
            String json = value.substring(start, end + 1);

            JsonNode node;
            try {
                node = objectMapper.readTree(json);
            } catch (Exception e) {
                return;
            }

            JsonNode actionIdNode = node.get("actionId");
            if (actionIdNode == null) {
                return;
            }
            long actionId = actionIdNode.asLong();
            JsonNode registerUserIdNode = node.get("registerUserId");
            if (registerUserIdNode == null) {
                return;
            }
            long registerUserId = registerUserIdNode.asLong();
            if (registerUserId <= 0) {
                return;
            }

            JsonNode registerTimeNode = node.get("time");
            if (registerTimeNode == null) {
                return;
            }
            long registerTime = registerTimeNode.asLong();
            if (registerTime <= 0) {
                return;
            }
            out.collect(UserAction.register(registerUserId, registerTime, actionId));
        }
    }

    private static class NewUserFirstActionProcessFunction
            extends KeyedProcessFunction<Long, UserAction, String> {
        private transient ObjectMapper objectMapper;
        private transient ValueState<Long> registerTimeState;
        private transient ValueState<Boolean> hasEmittedState;
        private transient ValueState<Long> registerActionIdState;

        @Override
        public void open(OpenContext openContext) throws Exception {
            super.open(openContext);
            objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            StateTtlConfig ttlConfig =
                    StateTtlConfig.newBuilder(Duration.ofHours(48))
                            .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
                            .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
                            .build();

            ValueStateDescriptor<Long> registerTimeDescriptor =
                    new ValueStateDescriptor<>("registerTimeState", BasicTypeInfo.LONG_TYPE_INFO);
            registerTimeDescriptor.enableTimeToLive(ttlConfig);
            registerTimeState = getRuntimeContext().getState(registerTimeDescriptor);

            ValueStateDescriptor<Boolean> hasEmittedDescriptor =
                    new ValueStateDescriptor<>("hasEmittedState", BasicTypeInfo.BOOLEAN_TYPE_INFO);
            hasEmittedDescriptor.enableTimeToLive(ttlConfig);
            hasEmittedState = getRuntimeContext().getState(hasEmittedDescriptor);

            ValueStateDescriptor<Long> registerActionIdDescriptor =
                    new ValueStateDescriptor<>(
                            "registerActionIdState", BasicTypeInfo.LONG_TYPE_INFO);
            registerActionIdDescriptor.enableTimeToLive(ttlConfig);
            registerActionIdState = getRuntimeContext().getState(registerActionIdDescriptor);
        }

        @Override
        public void processElement(
                UserAction value,
                KeyedProcessFunction<Long, UserAction, String>.Context ctx,
                Collector<String> out)
                throws Exception {
            Long registerTimeValue = registerTimeState.value();
            long registerTime = registerTimeValue == null ? 0L : registerTimeValue;
            Boolean hasEmittedValue = hasEmittedState.value();
            boolean hasEmitted = Boolean.TRUE.equals(hasEmittedValue);
            Long registerActionId = registerActionIdState.value();

            if (value == null) {
                return;
            }

            if (ACTION_REGISTER.equals(value.getAction())) {
                Long timeValue = value.getTime();
                long time = timeValue == null ? 0L : timeValue;
                if (time <= 0) {
                    return;
                }
                Long actionId = value.getActionId();
                if (actionId == null || actionId <= 0) {
                    return;
                }
                if (registerTime == 0 || time < registerTime) {
                    registerTimeState.update(time);
                    registerActionIdState.update(actionId);
                }
                return;
            }

            if (registerTime == 0) {
                return;
            }
            if (registerActionId == null || registerActionId <= 0) {
                return;
            }
            if (hasEmitted) {
                return;
            }

            String actionType = value.getAction();
            if (!ACTION_LIKE.equals(actionType) && !ACTION_COMMENT.equals(actionType)) {
                return;
            }
            Long actionTimeValue = value.getTime();
            long actionTime = actionTimeValue == null ? 0L : actionTimeValue;
            if (actionTime <= 0) {
                return;
            }
            boolean isInStimulusPeriod =
                    actionTime > registerTime && actionTime - registerTime < WINDOW_MILLIS;
            if (!isInStimulusPeriod) {
                return;
            }

            AdNewUserFirstActionRecord record = new AdNewUserFirstActionRecord();
            record.setUserId(ctx.getCurrentKey());
            record.setTime(actionTime);
            record.setActionId(registerActionId);
            record.setActionType(actionType);
            out.collect(objectMapper.writeValueAsString(record));
            hasEmittedState.update(true);
        }
    }

    private static class UserAction implements Serializable {
        private Long userId;
        private String action;
        private Long time;
        private Long actionId;

        public UserAction() {}

        private UserAction(Long userId, String action, Long time, Long actionId) {
            this.userId = userId;
            this.action = action;
            this.time = time;
            this.actionId = actionId;
        }

        public static UserAction register(Long userId, Long time, Long actionId) {
            return new UserAction(userId, ACTION_REGISTER, time, actionId);
        }

        public static UserAction like(Long userId, Long time, Long actionId) {
            return new UserAction(userId, ACTION_LIKE, time, actionId);
        }

        public static UserAction comment(Long userId, Long time, Long actionId) {
            return new UserAction(userId, ACTION_COMMENT, time, actionId);
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public Long getTime() {
            return time;
        }

        public void setTime(Long time) {
            this.time = time;
        }

        public Long getActionId() {
            return actionId;
        }

        public void setActionId(Long actionId) {
            this.actionId = actionId;
        }

        @Override
        public String toString() {
            return "UserAction{"
                    + "userId="
                    + userId
                    + ", action='"
                    + action
                    + '\''
                    + ", time="
                    + time
                    + ", actionId="
                    + actionId
                    + '}';
        }
    }
}
