package com.netease.operator.ads.pve;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.pojo.pve.PveRolePropsCostResult;
import com.netease.util.DateTimeFormatterUtils;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

public class AdsPveUserPropsStatisticsRichFlatMapFunction
        extends RichFlatMapFunction<String, PveRolePropsCostResult> {
    private ObjectMapper objectMapper;

    @Override
    public void open(Configuration parameters) throws Exception {
        objectMapper = new ObjectMapper();
    }

    @Override
    public void flatMap(String s, Collector<PveRolePropsCostResult> collector) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(s);
        if (jsonNode.get("tableName").asText() != null
                && "PVE_UserPropsLog".equals(jsonNode.get("tableName").asText())) {
            DateTimeFormatterUtils dateTimeFormatterUtils = new DateTimeFormatterUtils();
            String timeStamp =
                    dateTimeFormatterUtils.dateTimeHourFormat(jsonNode.get("createTime").asLong());

            PveRolePropsCostResult pveRolePropsCostResult = new PveRolePropsCostResult();
            pveRolePropsCostResult.setDt(timeStamp.substring(0, 10));
            pveRolePropsCostResult.setHour(Integer.valueOf(timeStamp.substring(11, 13)));
            pveRolePropsCostResult.setRoleId(jsonNode.get("roleId").asLong());
            pveRolePropsCostResult.setPropsId(jsonNode.get("propsId").asLong());
            pveRolePropsCostResult.setCostStamina(jsonNode.get("costStamina").asLong());
            pveRolePropsCostResult.setMessageType(1);

            if (pveRolePropsCostResult.getRoleId() != null
                    && pveRolePropsCostResult.getCostStamina() != null) {
                collector.collect(pveRolePropsCostResult);
            }
        }
    }
}
