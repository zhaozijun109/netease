package com.netease.yuanqi.lofter.operator.ads.pve;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.yuanqi.common.pojo.ods.binlog.BinlogRow;
import com.netease.yuanqi.common.utils.DateTimeFormatterUtils;
import com.netease.yuanqi.lofter.pojo.ads.pve.PveUserRoleDialogueLogs;
import java.util.List;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

public class AdsPveUserChatsStatisticsRichFlatMapFunction
        extends RichFlatMapFunction<String, PveUserRoleDialogueLogs> {
    private ObjectMapper objectMapper;

    @Override
    public void open(Configuration parameters) throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Override
    public void flatMap(String s, Collector<PveUserRoleDialogueLogs> collector) throws Exception {
        BinlogRow binlogRow = objectMapper.readValue(s, BinlogRow.class);
        if ((binlogRow.getOp() == 0 && "PVE_UserDialoguePartition".equals(binlogRow.get_tbl()))
                || (binlogRow.getOp() == 2
                        && "PVE_RoleGroupDialogue".equals(binlogRow.get_tbl())
                        && binlogRow.getOld().get("targetRoleIds") != null
                        && "[]".equals(binlogRow.getOld().get("targetRoleIds").toString())
                        && binlogRow.getData().get("targetRoleIds") != null
                        && !"[]".equals(binlogRow.getData().get("targetRoleIds").toString()))) {
            if (binlogRow.getData().get("sender") != null
                    && Integer.parseInt(binlogRow.getData().get("sender").toString()) == 1) {
                PveUserRoleDialogueLogs pveUserRoleDialogueLogs = new PveUserRoleDialogueLogs();
                pveUserRoleDialogueLogs.setUserId(
                        Long.parseLong(binlogRow.getData().get("userId").toString()));

                String timeStamp =
                        DateTimeFormatterUtils.dateTimeHourFormat(
                                Long.parseLong(binlogRow.getData().get("createTime").toString()));
                pveUserRoleDialogueLogs.setDt(timeStamp.substring(0, 10));
                pveUserRoleDialogueLogs.setHour(Integer.valueOf(timeStamp.substring(11, 13)));

                if ("PVE_UserDialoguePartition".equals(binlogRow.get_tbl())) {
                    pveUserRoleDialogueLogs.setRoleId(
                            Long.parseLong(binlogRow.getData().get("roleId").toString()));
                    pveUserRoleDialogueLogs.setRoleType(
                            Integer.parseInt(binlogRow.getData().get("roleType").toString()));
                } else { // PVE_RoleGroupDialogue
                    if (binlogRow.getData().get("groupType") != null
                            && Integer.parseInt(binlogRow.getData().get("groupType").toString())
                                    == 0) {
                        List<RoleInfo> targetRoleIdsList =
                                objectMapper.readValue(
                                        binlogRow.getData().get("targetRoleIds").toString(),
                                        new TypeReference<List<RoleInfo>>() {});
                        if (!targetRoleIdsList.isEmpty()) {
                            pveUserRoleDialogueLogs.setRoleId(targetRoleIdsList.get(0).getRoleId());
                            pveUserRoleDialogueLogs.setRoleType(
                                    targetRoleIdsList.get(0).getRoleType());
                        }
                    }
                }

                if (pveUserRoleDialogueLogs.getRoleId() != null
                        && pveUserRoleDialogueLogs.getRoleType() != null) {
                    collector.collect(pveUserRoleDialogueLogs);
                }
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
