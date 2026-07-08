package com.netease.yuanqi.common.pojo;

public class KafkaPayloadResult {
    public Integer messageType;
    public Object payload;

    public Integer getMessageType() {
        return messageType;
    }

    public void setMessageType(Integer messageType) {
        this.messageType = messageType;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
}
