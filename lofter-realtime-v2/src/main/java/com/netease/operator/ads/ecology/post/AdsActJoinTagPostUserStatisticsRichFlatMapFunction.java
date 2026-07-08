package com.netease.operator.ads.ecology.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.pojo.KafkaPayloadResult;
import com.netease.pojo.ecology.post.ActJoinTagPostHot;
import com.netease.pojo.ecology.post.ActJoinTagPostUserStatistics;

import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class AdsActJoinTagPostUserStatisticsRichFlatMapFunction
        extends RichFlatMapFunction<ActJoinTagPostHot, String> {
    private static final Logger LOG =
            LoggerFactory.getLogger(AdsActJoinTagPostUserStatisticsRichFlatMapFunction.class);
    private ObjectMapper objectMapper;
    private ValueState<ActJoinTagPostUserStatistics> statisticsUserState;

    @Override
    public void open(Configuration parameters) throws Exception {
        objectMapper = new ObjectMapper();
        StateTtlConfig stateTtlConfig =
                StateTtlConfig.newBuilder(Time.days(35))
                        .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
                        .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
                        .cleanupInRocksdbCompactFilter(100000)
                        .build();
        ValueStateDescriptor<ActJoinTagPostUserStatistics> statisticsUserStateDescriptor =
                new ValueStateDescriptor<>(
                        "ActJoinTagPostUserStatisticsState",
                        TypeInformation.of(ActJoinTagPostUserStatistics.class));
        statisticsUserStateDescriptor.enableTimeToLive(stateTtlConfig);
        statisticsUserState = getRuntimeContext().getState(statisticsUserStateDescriptor);
    }

    @Override
    public void flatMap(ActJoinTagPostHot actJoinTagPostHot, Collector<String> collector)
            throws Exception {
        ActJoinTagPostUserStatistics actJoinTagPostUserStatistics =
                new ActJoinTagPostUserStatistics();
        actJoinTagPostUserStatistics.setTag(actJoinTagPostHot.getTag());
        actJoinTagPostUserStatistics.setTaskId(actJoinTagPostHot.getActTaskId());
        actJoinTagPostUserStatistics.setUserId(actJoinTagPostHot.getBlogId());

        if (statisticsUserState.value() != null) {
            Set<Long> validPostIds = statisticsUserState.value().getValidPostIds();
            Set<Long> qualityPostIds = statisticsUserState.value().getQualityPostIds();
            Set<Long> masterpiecePostIds = statisticsUserState.value().getMasterpiecePostIds();

            if (actJoinTagPostHot.getHot() >= 10 && actJoinTagPostHot.getHot() < 100) {
                if (validPostIds != null && !validPostIds.isEmpty()) {
                    if (actJoinTagPostHot.getPostStatus() == 1) {
                        validPostIds.add(actJoinTagPostHot.getPostId());
                    } else if (actJoinTagPostHot.getPostStatus() == -1) {
                        validPostIds.remove(actJoinTagPostHot.getPostId());
                    }
                } else {
                    if (actJoinTagPostHot.getPostStatus() == 1) {
                        validPostIds = new HashSet<>(1);
                        validPostIds.add(actJoinTagPostHot.getPostId());
                    }
                }

                if (qualityPostIds != null && !qualityPostIds.isEmpty()) {
                    qualityPostIds.remove(actJoinTagPostHot.getPostId());
                }

                if (masterpiecePostIds != null && !masterpiecePostIds.isEmpty()) {
                    masterpiecePostIds.remove(actJoinTagPostHot.getPostId());
                }
            } else if (actJoinTagPostHot.getHot() >= 100 && actJoinTagPostHot.getHot() < 1000) {
                if (validPostIds != null && !validPostIds.isEmpty()) {
                    validPostIds.remove(actJoinTagPostHot.getPostId());
                }

                if (qualityPostIds != null && !qualityPostIds.isEmpty()) {
                    if (actJoinTagPostHot.getPostStatus() == 1) {
                        qualityPostIds.add(actJoinTagPostHot.getPostId());
                    } else if (actJoinTagPostHot.getPostStatus() == -1) {
                        qualityPostIds.remove(actJoinTagPostHot.getPostId());
                    }
                } else {
                    if (actJoinTagPostHot.getPostStatus() == 1) {
                        qualityPostIds = new HashSet<>(1);
                        qualityPostIds.add(actJoinTagPostHot.getPostId());
                    }
                }

                if (masterpiecePostIds != null && !masterpiecePostIds.isEmpty()) {
                    masterpiecePostIds.remove(actJoinTagPostHot.getPostId());
                }
            } else if (actJoinTagPostHot.getHot() >= 1000) {
                if (validPostIds != null && !validPostIds.isEmpty()) {
                    validPostIds.remove(actJoinTagPostHot.getPostId());
                }

                if (qualityPostIds != null && !qualityPostIds.isEmpty()) {
                    qualityPostIds.remove(actJoinTagPostHot.getPostId());
                }

                if (masterpiecePostIds != null && !masterpiecePostIds.isEmpty()) {
                    if (actJoinTagPostHot.getPostStatus() == 1) {
                        masterpiecePostIds.add(actJoinTagPostHot.getPostId());
                    } else if (actJoinTagPostHot.getPostStatus() == -1) {
                        masterpiecePostIds.remove(actJoinTagPostHot.getPostId());
                    }
                } else {
                    if (actJoinTagPostHot.getPostStatus() == 1) {
                        masterpiecePostIds = new HashSet<>(1);
                        masterpiecePostIds.add(actJoinTagPostHot.getPostId());
                    }
                }
            } else {
                if (validPostIds != null && !validPostIds.isEmpty()) {
                    validPostIds.remove(actJoinTagPostHot.getPostId());
                }

                if (qualityPostIds != null && !qualityPostIds.isEmpty()) {
                    qualityPostIds.remove(actJoinTagPostHot.getPostId());
                }

                if (masterpiecePostIds != null && !masterpiecePostIds.isEmpty()) {
                    masterpiecePostIds.remove(actJoinTagPostHot.getPostId());
                }
            }

            actJoinTagPostUserStatistics.setValidPostIds(validPostIds);
            actJoinTagPostUserStatistics.setQualityPostIds(qualityPostIds);
            actJoinTagPostUserStatistics.setMasterpiecePostIds(masterpiecePostIds);
        } else {
            if (actJoinTagPostHot.getPostStatus() == 1) {
                Set<Long> postSet = new HashSet<>();
                postSet.add(actJoinTagPostHot.getPostId());
                if (actJoinTagPostHot.getHot() >= 10 && actJoinTagPostHot.getHot() < 100) {
                    actJoinTagPostUserStatistics.setValidPostIds(postSet);
                } else if (actJoinTagPostHot.getHot() >= 100 && actJoinTagPostHot.getHot() < 1000) {
                    actJoinTagPostUserStatistics.setQualityPostIds(postSet);
                } else if (actJoinTagPostHot.getHot() >= 1000) {
                    actJoinTagPostUserStatistics.setMasterpiecePostIds(postSet);
                }
            }
        }

        KafkaPayloadResult kafkaPayloadResult = new KafkaPayloadResult();
        kafkaPayloadResult.setMessageType(7);
        kafkaPayloadResult.setPayload(actJoinTagPostUserStatistics);

        Set<Long> currentValidPostIds = actJoinTagPostUserStatistics.getValidPostIds();
        int validPostIdsSize = 0;
        if (currentValidPostIds != null && !currentValidPostIds.isEmpty()) {
            validPostIdsSize = currentValidPostIds.size();
        }

        Set<Long> currentQualityPostIds = actJoinTagPostUserStatistics.getQualityPostIds();
        int qualityPostIdsSize = 0;
        if (currentQualityPostIds != null && !currentQualityPostIds.isEmpty()) {
            qualityPostIdsSize = currentQualityPostIds.size();
        }

        Set<Long> currentMasterpiecePostIds = actJoinTagPostUserStatistics.getMasterpiecePostIds();
        int masterpiecePostIdsSize = 0;
        if (currentMasterpiecePostIds != null && !currentMasterpiecePostIds.isEmpty()) {
            masterpiecePostIdsSize = currentMasterpiecePostIds.size();
        }

        if (statisticsUserState.value() == null
                && (validPostIdsSize > 0 || qualityPostIdsSize > 0 || masterpiecePostIdsSize > 0)) {
            collector.collect(objectMapper.writeValueAsString(kafkaPayloadResult));
        } else if (statisticsUserState.value() != null) {
            Set<Long> stateValidPostIds = statisticsUserState.value().getValidPostIds();
            Set<Long> stateQualityPostIds = statisticsUserState.value().getQualityPostIds();
            Set<Long> stateMasterpiecePostIds = statisticsUserState.value().getMasterpiecePostIds();

            if (!setsEquals(currentValidPostIds, stateValidPostIds)
                    || !setsEquals(currentQualityPostIds, stateQualityPostIds)
                    || !setsEquals(currentMasterpiecePostIds, stateMasterpiecePostIds)) {
                if (!(currentValidPostIds == null
                        && currentQualityPostIds == null
                        && currentMasterpiecePostIds == null)) {
                    collector.collect(objectMapper.writeValueAsString(kafkaPayloadResult));
                }
            }
        }
        statisticsUserState.update(actJoinTagPostUserStatistics);
    }

    private boolean setsEquals(Set<Long> currentSet, Set<Long> stateSet) {
        if (currentSet == null || stateSet == null) {
            return false;
        }

        if (currentSet.size() != stateSet.size()) {
            return false;
        }

        return currentSet.containsAll(stateSet);
    }
}
