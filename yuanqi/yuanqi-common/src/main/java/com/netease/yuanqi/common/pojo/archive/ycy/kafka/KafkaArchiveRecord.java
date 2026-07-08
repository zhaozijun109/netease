package com.netease.yuanqi.common.pojo.archive.ycy.kafka;

import java.io.Serializable;

public interface KafkaArchiveRecord extends Serializable {
    Long getKafkaTime();

    void setKafkaTime(Long kafkaTime);
}
