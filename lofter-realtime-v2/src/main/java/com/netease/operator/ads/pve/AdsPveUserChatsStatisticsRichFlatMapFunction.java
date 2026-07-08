package com.netease.operator.ads.pve;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.pojo.pve.PveUserRoleDialogueLogs;
import com.netease.util.DateTimeFormatterUtils;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

import java.util.List;

public class AdsPveUserChatsStatisticsRichFlatMapFunction
        extends RichFlatMapFunction<String, PveUserRoleDialogueLogs> {
    private ObjectMapper objectMapper;

    @Override
    public void open(Configuration parameters) throws Exception {
        objectMapper = new ObjectMapper();
    }

    @Override
    public void flatMap(String s, Collector<PveUserRoleDialogueLogs> collector) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(s);
        if (jsonNode.get("tableName") != null
                && ("PVE_UserDialogue".equals(jsonNode.get("tableName").asText())
                        || "PVE_RoleGroupDialogue".equals(jsonNode.get("tableName").asText()))
                && jsonNode.get("sender") != null
                && jsonNode.get("sender").asInt() == 1) {
            DateTimeFormatterUtils dateTimeFormatterUtils = new DateTimeFormatterUtils();
            PveUserRoleDialogueLogs pveUserRoleDialogueLogs = new PveUserRoleDialogueLogs();
            pveUserRoleDialogueLogs.setUserId(jsonNode.get("userId").asLong());

            String timeStamp =
                    dateTimeFormatterUtils.dateTimeHourFormat(jsonNode.get("createTime").asLong());
            pveUserRoleDialogueLogs.setDt(timeStamp.substring(0, 10));
            pveUserRoleDialogueLogs.setHour(Integer.valueOf(timeStamp.substring(11, 13)));

            if ("PVE_UserDialogue".equals(jsonNode.get("tableName").asText())) {
                pveUserRoleDialogueLogs.setRoleId(jsonNode.get("roleId").asLong());
                pveUserRoleDialogueLogs.setRoleType(jsonNode.get("roleType").asInt());
            } else { // PVE_RoleGroupDialogue
                if (jsonNode.get("groupType") != null
                        && jsonNode.get("groupType").asInt() == 0
                        && jsonNode.get("targetRoleIds") != null) {
                    List<RoleInfo> targetRoleIdsList =
                            objectMapper.readValue(
                                    jsonNode.get("targetRoleIds").asText(),
                                    new TypeReference<List<RoleInfo>>() {});
                    if (!targetRoleIdsList.isEmpty()) {
                        pveUserRoleDialogueLogs.setRoleId(targetRoleIdsList.get(0).getRoleId());
                        pveUserRoleDialogueLogs.setRoleType(targetRoleIdsList.get(0).getRoleType());
                    }
                }
            }

            if (pveUserRoleDialogueLogs.getRoleId() != null
                    && pveUserRoleDialogueLogs.getRoleType() != null) {
                collector.collect(pveUserRoleDialogueLogs);
            }
        }
    }

    private static class RoleInfo {
        private Long roleId;
        private Integer roleType;

        public Long getRoleId() {
            return roleId;
        }

        public void setRoleId(Long roleId) {
            this.roleId = roleId;
        }

        public Integer getRoleType() {
            return roleType;
        }

        public void setRoleType(Integer roleType) {
            this.roleType = roleType;
        }
    }
}
