package com.netease.operator.ads.pve;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.pojo.pve.PveRolePropsCostResult;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

public class AdsPveUserPropsStatisticsProcessWindowFunction
        extends ProcessWindowFunction<PveRolePropsCostResult, String, String, TimeWindow> {
    private ObjectMapper objectMapper;

    @Override
    public void open(Configuration parameters) throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public void process(
            String s,
            ProcessWindowFunction<PveRolePropsCostResult, String, String, TimeWindow>.Context
                    context,
            Iterable<PveRolePropsCostResult> iterable,
            Collector<String> collector)
            throws Exception {
        PveRolePropsCostResult pveRolePropsCostResult = iterable.iterator().next();

        KafkaResultMessage kafkaResultMessage = new KafkaResultMessage();
        kafkaResultMessage.setDt(pveRolePropsCostResult.getDt());
        kafkaResultMessage.setHour(pveRolePropsCostResult.getHour());
        kafkaResultMessage.setRoleId(pveRolePropsCostResult.getRoleId());
        kafkaResultMessage.setDialoguePv(pveRolePropsCostResult.getCostStamina());
        kafkaResultMessage.setMessageType(pveRolePropsCostResult.getMessageType());
        collector.collect(objectMapper.writeValueAsString(kafkaResultMessage));
    }

    private static class KafkaResultMessage {
        private String dt;
        private Integer hour;
        private Long roleId;
        private Long dialoguePv;
        private Integer messageType;

        public String getDt() {
            return dt;
        }

        public void setDt(String dt) {
            this.dt = dt;
        }

        public Integer getHour() {
            return hour;
        }

        public void setHour(Integer hour) {
            this.hour = hour;
        }

        public Long getRoleId() {
            return roleId;
        }

        public void setRoleId(Long roleId) {
            this.roleId = roleId;
        }

        public Long getDialoguePv() {
            return dialoguePv;
        }

        public void setDialoguePv(Long dialoguePv) {
            this.dialoguePv = dialoguePv;
        }

        public Integer getMessageType() {
            return messageType;
        }

        public void setMessageType(Integer messageType) {
            this.messageType = messageType;
        }
    }
}
