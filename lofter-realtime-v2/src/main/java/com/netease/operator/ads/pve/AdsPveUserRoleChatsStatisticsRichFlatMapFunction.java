package com.netease.operator.ads.pve;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.pojo.pve.PveDialogueHourStatistics;
import com.netease.pojo.pve.PveRoleDialogueHourStatistics;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

public class AdsPveUserRoleChatsStatisticsRichFlatMapFunction
        extends RichFlatMapFunction<PveRoleDialogueHourStatistics, String> {
    private ObjectMapper objectMapper;

    @Override
    public void open(Configuration parameters) throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public void flatMap(
            PveRoleDialogueHourStatistics pveRoleDialogueHourStatistics,
            Collector<String> collector)
            throws Exception {
        if (pveRoleDialogueHourStatistics.getRoleType() == 0
                || pveRoleDialogueHourStatistics.getRoleType() == 4
                || pveRoleDialogueHourStatistics.getRoleType() == 20) {
            PveDialogueHourStatistics pveDialogueHourStatistics = new PveDialogueHourStatistics();
            pveDialogueHourStatistics.setDt(pveRoleDialogueHourStatistics.getDt());
            pveDialogueHourStatistics.setHour(pveRoleDialogueHourStatistics.getHour());
            pveDialogueHourStatistics.setRoleId(pveRoleDialogueHourStatistics.getRoleId());
            pveDialogueHourStatistics.setRoleType(pveRoleDialogueHourStatistics.getRoleType());
            pveDialogueHourStatistics.setDialoguePv(pveRoleDialogueHourStatistics.getDialoguePv());
            pveDialogueHourStatistics.setMessageType(
                    pveRoleDialogueHourStatistics.getMessageType());
            collector.collect(objectMapper.writeValueAsString(pveDialogueHourStatistics));
        }
    }
}
